package com.example.recipefinder.database

import com.example.recipefinder.data.Ingredient
import com.example.recipefinder.data.Recipe
import com.example.recipefinder.data.Step
import com.example.recipefinder.data.Tag
import com.example.recipefinder.data.Unit
import com.google.firebase.firestore.FirebaseFirestore


data class SafeRecipe(
    val id: Int = 0,
    val title: String = "Something went wrong",
    val description: String = "",
    val preparationTime: Int = 0,
    val steps : List<Step> = emptyList(),
    val imageUrl: String = "",
    val ingredients: List<SafeIngredient> = emptyList(),
    val style: String = "",
    val tags: List<SafeTag> = emptyList(),
    val owner: String = "Anonymous"
)

data class SafeIngredient(
    val id: Int = 0,
    val name: String = "Something went wrong",
    val quantity: Double = 0.0,
    val unit: String = "",
    val note: String = ""
)

data class SafeTag(
    val tag: String = ""
)

/**
 * Convert a SafeRecipe (database object) into a Recipe
 */
fun safeRecipeConverter(recipe: SafeRecipe): Recipe
{
    return Recipe(
        id = recipe.id,
        title = recipe.title,
        description = recipe.description,
        preparationTime = recipe.preparationTime,
        steps = recipe.steps,
        imageUrl = recipe.imageUrl,
        ingredients = recipe.ingredients.map { ingredient ->
            Ingredient(
                id = ingredient.id,
                name = ingredient.name,
                quantity = ingredient.quantity,
                unit = Unit.valueOf(ingredient.unit), // Convert String to enum
                note = ingredient.note
            )
        },
        style = recipe.style,
        tags = recipe.tags.map { tag ->
            Tag.valueOf(tag.tag) // Convert String to enum
        },
        owner = recipe.owner
    )
}

/**
 * Convert a Recipe into a SafeRecipe (database object)
 */
fun recipeConverter(recipe: Recipe): SafeRecipe
{
    return SafeRecipe(
        id = recipe.id,
        title = recipe.title,
        description = recipe.description,
        preparationTime = recipe.preparationTime,
        steps = recipe.steps,
        imageUrl = recipe.imageUrl,
        ingredients = recipe.ingredients.map { ingredient ->
            SafeIngredient(
                id = ingredient.id,
                name = ingredient.name,
                quantity = ingredient.quantity,
                unit = ingredient.unit.name, // Convert enum to String
                note = ingredient.note
            )
        },
        style = recipe.style,
        tags = recipe.tags.map { tag ->
            SafeTag(tag.name) // Convert enum to String
        },
        owner = recipe.owner
    )
}

fun writeRecipeToDatabase(
    db: FirebaseFirestore,
    recipe: Recipe,
    onSuccess: (Recipe) -> kotlin.Unit = {},
    onFailure: (Exception) -> kotlin.Unit = {}
) {
    val newDocRef = db.collection("RECIPES").document()
    val recipeWithId = recipe.copy(id = newDocRef.id.hashCode())
    newDocRef.set(recipeConverter(recipeWithId))
        .addOnSuccessListener {
            // Write was successful
            println("Recipe written successfully: ${recipe.title}")
            onSuccess(recipeWithId)
        }
        .addOnFailureListener { exception ->
            // Write failed
            println("Error writing recipe: ${exception.message}")
            onFailure(exception)
        }
}


fun readRecipesFromDatabase(db: FirebaseFirestore, onSuccess: (List<Recipe>) -> kotlin.Unit, onFailure: (Exception) -> kotlin.Unit) {
    db.collection("RECIPES").get()
        .addOnSuccessListener { dataSnapshot ->
            // Read was successful
            val recipes = dataSnapshot.documents.mapNotNull { document ->
                val safeRecipe = document.toObject(SafeRecipe::class.java)
                safeRecipe?.let { safeRecipeConverter(it) }
            }
            onSuccess(recipes)
        }
        .addOnFailureListener { exception ->
            // Read failed
            onFailure(exception)
        }
}

fun readSavedRecipesFromDatabase(db: FirebaseFirestore, recipes: List<Recipe>, userId: String, onSuccess: (List<Recipe>) -> kotlin.Unit) {
    db.collection("USERS").document(userId).get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val savedRecipeIds = document.get("savedRecipes") as? List<String> ?: emptyList()
                val savedRecipes = recipes.filter { it.id.toString() in savedRecipeIds }
                onSuccess(savedRecipes)
            } else {
                // User document does not exist, return empty list
                onSuccess(emptyList())
            }
        }
        .addOnFailureListener { exception ->
            // Read failed
            println("Error reading saved recipes: ${exception.message}")
            onSuccess(emptyList())
        }
}

fun updateSavedRecipesInDatabase(db: FirebaseFirestore, userId: String, savedRecipes: List<Recipe>) {
    val savedRecipeIds = savedRecipes.map { it.id.toString() }
    db.collection("USERS").document(userId).set(mapOf("savedRecipes" to savedRecipeIds), com.google.firebase.firestore.SetOptions.merge())
        .addOnSuccessListener {
            // Update was successful
            println("Saved recipes updated successfully for user: $userId")
        }
        .addOnFailureListener { exception ->
            // Update failed
            println("Error updating saved recipes: ${exception.message}")
        }
}

fun updateRecipeInDatabase(
    db: FirebaseFirestore,
    recipe: Recipe,
    onSuccess: (Recipe) -> kotlin.Unit = {},
    onFailure: (Exception) -> kotlin.Unit = {}
) {
    // Query for the document where the 'id' field matches the recipe's id
    db.collection("RECIPES")
        .whereEqualTo("id", recipe.id)
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                // Found the document, update it
                val documentId = querySnapshot.documents[0].id
                db.collection("RECIPES").document(documentId).set(recipeConverter(recipe))
                    .addOnSuccessListener {
                        println("Recipe updated successfully: ${recipe.title}")
                        onSuccess(recipe)
                    }
                    .addOnFailureListener { exception ->
                        println("Error updating recipe: ${exception.message}")
                        onFailure(exception)
                    }
            } else {
                // Document not found, this shouldn't happen
                val error = Exception("Recipe with id ${recipe.id} not found in database")
                println("Error: ${error.message}")
                onFailure(error)
            }
        }
        .addOnFailureListener { exception ->
            println("Error querying for recipe: ${exception.message}")
            onFailure(exception)
        }
}

/**
 * Read the user's custom saved-recipe folders (folder name -> recipe ids).
 * Stored on USERS/{uid} as `folders: Map<String, List<String>>`.
 */
fun readFoldersFromDatabase(db: FirebaseFirestore, userId: String, onSuccess: (Map<String, List<Int>>) -> kotlin.Unit) {
    db.collection("USERS").document(userId).get()
        .addOnSuccessListener { document ->
            val raw = document.get("folders") as? Map<*, *> ?: emptyMap<Any, Any>()
            val folders = LinkedHashMap<String, List<Int>>()
            raw.forEach { (k, v) ->
                val ids = (v as? List<*>)?.mapNotNull { it?.toString()?.toIntOrNull() } ?: emptyList()
                if (k is String) folders[k] = ids
            }
            onSuccess(folders)
        }
        .addOnFailureListener { exception ->
            println("Error reading folders: ${exception.message}")
            onSuccess(emptyMap())
        }
}

fun updateFoldersInDatabase(db: FirebaseFirestore, userId: String, folders: Map<String, List<Int>>) {
    val data = folders.mapValues { (_, ids) -> ids.map { it.toString() } }
    // update() replaces the whole `folders` field (so deleted folders are removed); fall back to merge-set if the doc is missing
    db.collection("USERS").document(userId).set(mapOf("folders" to data), com.google.firebase.firestore.SetOptions.mergeFields("folders"))
        .addOnFailureListener { exception ->
            println("Error updating folders: ${exception.message}")
        }
}


/** Removes [recipeId] from every folder in [model] and persists the result to Firestore. */
fun removeRecipeFromAllFolders(
    model: com.example.recipefinder.data.RecipeViewModel,
    db: FirebaseFirestore,
    userId: String?,
    recipeId: Int
) {
    val current = model.folders.value
    if (current.values.none { recipeId in it }) return
    val pruned = current.mapValues { (_, ids) -> ids.filter { it != recipeId } }
    model.setFolders(pruned)
    if (!userId.isNullOrEmpty()) updateFoldersInDatabase(db, userId, pruned)
}
