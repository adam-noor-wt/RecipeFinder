package com.example.recipefinder.database

import com.example.recipefinder.data.PublicProfile
import com.example.recipefinder.data.Recipe
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import java.util.concurrent.atomic.AtomicInteger

private const val PROFILES = "PUBLIC_PROFILES"

/** Social state stored on USERS/{uid}: followed user ids and the recipe ids already viewed. */
data class SocialState(val follows: List<String>, val viewed: Set<Int>)

fun readPublicProfile(db: FirebaseFirestore, uid: String, onResult: (PublicProfile?) -> kotlin.Unit) {
    db.collection(PROFILES).document(uid).get()
        .addOnSuccessListener { doc ->
            if (doc.exists()) {
                onResult(
                    PublicProfile(
                        uid = uid,
                        displayName = doc.getString("displayName") ?: "",
                        isPublic = doc.getBoolean("isPublic") ?: false
                    )
                )
            } else onResult(null)
        }
        .addOnFailureListener { onResult(null) }
}

fun readPublicProfiles(db: FirebaseFirestore, uids: Collection<String>, onResult: (Map<String, PublicProfile>) -> kotlin.Unit) {
    val distinct = uids.distinct()
    if (distinct.isEmpty()) { onResult(emptyMap()); return }
    val remaining = AtomicInteger(distinct.size)
    val result = java.util.concurrent.ConcurrentHashMap<String, PublicProfile>()
    distinct.forEach { uid ->
        readPublicProfile(db, uid) { profile ->
            if (profile != null) result[uid] = profile
            if (remaining.decrementAndGet() == 0) onResult(result.toMap())
        }
    }
}

/** Creates/updates the public profile fields. Only non-null fields are written. */
fun writePublicProfile(db: FirebaseFirestore, uid: String, displayName: String? = null, isPublic: Boolean? = null) {
    val data = HashMap<String, Any>()
    displayName?.let { data["displayName"] = it }
    isPublic?.let { data["isPublic"] = it }
    if (data.isEmpty()) return
    db.collection(PROFILES).document(uid).set(data, SetOptions.merge())
        .addOnFailureListener { println("Error writing public profile: ${it.message}") }
}

/** Creates a Private public-profile document for accounts that don't have one yet (new accounts and backfill). */
fun ensurePublicProfile(db: FirebaseFirestore, uid: String, displayName: String?) {
    val ref = db.collection(PROFILES).document(uid)
    ref.get().addOnSuccessListener { doc ->
        if (!doc.exists()) {
            ref.set(mapOf("displayName" to (displayName ?: ""), "isPublic" to false), SetOptions.merge())
        }
    }
}

/** Public profiles whose name contains [query] (case-insensitive), or whose uid equals [query]. */
fun searchPublicProfiles(db: FirebaseFirestore, query: String, excludeUid: String?, onResult: (List<PublicProfile>) -> kotlin.Unit) {
    val q = query.trim()
    if (q.isEmpty()) { onResult(emptyList()); return }
    db.collection(PROFILES).whereEqualTo("isPublic", true).get()
        .addOnSuccessListener { snap ->
            val matches = snap.documents.mapNotNull { doc ->
                val name = doc.getString("displayName") ?: ""
                if (doc.id != excludeUid && (doc.id == q || name.contains(q, ignoreCase = true)))
                    PublicProfile(doc.id, name, true) else null
            }
            onResult(matches)
        }
        .addOnFailureListener { onResult(emptyList()) }
}

fun readSocialState(db: FirebaseFirestore, uid: String, onResult: (SocialState) -> kotlin.Unit) {
    db.collection("USERS").document(uid).get()
        .addOnSuccessListener { doc ->
            val follows = (doc.get("follows") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val viewed = (doc.get("viewedRecipes") as? List<*>)?.mapNotNull { it?.toString()?.toIntOrNull() }?.toSet() ?: emptySet()
            onResult(SocialState(follows, viewed))
        }
        .addOnFailureListener {
            println("Error reading social state: ${it.message}")
            onResult(SocialState(emptyList(), emptySet()))
        }
}

/** Follows [targetUid]; their existing recipes ([existingRecipeIds]) are marked viewed so only later ones notify. */
fun followUser(db: FirebaseFirestore, uid: String, targetUid: String, existingRecipeIds: List<Int>) {
    val data = hashMapOf<String, Any>("follows" to FieldValue.arrayUnion(targetUid))
    if (existingRecipeIds.isNotEmpty())
        data["viewedRecipes"] = FieldValue.arrayUnion(*existingRecipeIds.map { it.toString() }.toTypedArray())
    db.collection("USERS").document(uid).set(data, SetOptions.merge())
        .addOnFailureListener { println("Error following user: ${it.message}") }
}

fun unfollowUser(db: FirebaseFirestore, uid: String, targetUid: String) {
    db.collection("USERS").document(uid)
        .set(mapOf("follows" to FieldValue.arrayRemove(targetUid)), SetOptions.merge())
        .addOnFailureListener { println("Error unfollowing user: ${it.message}") }
}

fun markRecipesViewed(db: FirebaseFirestore, uid: String, recipeIds: List<Int>) {
    if (recipeIds.isEmpty()) return
    db.collection("USERS").document(uid)
        .set(
            mapOf("viewedRecipes" to FieldValue.arrayUnion(*recipeIds.map { it.toString() }.toTypedArray())),
            SetOptions.merge()
        )
        .addOnFailureListener { println("Error marking recipes viewed: ${it.message}") }
}

/** Real-time listener on all recipes (drives new-recipe notifications). */
fun listenToRecipes(db: FirebaseFirestore, onChange: (List<Recipe>) -> kotlin.Unit): ListenerRegistration =
    db.collection("RECIPES").addSnapshotListener { snap, error ->
        if (error != null || snap == null) return@addSnapshotListener
        val recipes = snap.documents.mapNotNull { doc ->
            runCatching { doc.toObject(SafeRecipe::class.java)?.let { safeRecipeConverter(it) } }.getOrNull()
        }
        onChange(recipes)
    }
