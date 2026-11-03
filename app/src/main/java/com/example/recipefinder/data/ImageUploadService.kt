package com.example.recipefinder.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * Service for uploading images to Firebase Storage.
 */
class ImageUploadService(private val storage: FirebaseStorage = FirebaseStorage.getInstance()) {

    companion object {
        private const val MAX_FILE_SIZE_BYTES = 3 * 1024 * 1024 // 3 MB
    }

    /**
     * Checks if a file size is within the allowed limit.
     *
     * @param context Application context to access content resolver
     * @param uri The URI of the file to check
     * @return true if file is within limit, false otherwise
     */
    private fun isFileSizeValid(context: Context, uri: Uri): Boolean {
        return try {
            val fileSize = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.available().toLong()
            } ?: 0L
            fileSize <= MAX_FILE_SIZE_BYTES
        } catch (e: Exception) {
            // If we can't determine size, allow it to proceed
            // The upload will fail if there's actually a problem
            true
        }
    }

    /**
     * Uploads an image to Firebase Storage in the "recipe_images" folder.
     *
     * @param context Application context (needed for file size check)
     * @param imageUri The local URI of the image to upload
     * @param onSuccess Callback with the download URL on successful upload
     * @param onFailure Callback with exception on failure
     * @param onProgress Optional callback with upload progress (0-100)
     */
    fun uploadRecipeImage(
        context: Context,
        imageUri: Uri,
        onSuccess: (String) -> kotlin.Unit,
        onFailure: (Exception) -> kotlin.Unit,
        onProgress: ((Int) -> kotlin.Unit)? = null
    ) {
        // Check file size first
        if (!isFileSizeValid(context, imageUri)) {
            onFailure(Exception("Image file size exceeds 3 MB limit. Please choose a smaller image or crop it."))
            return
        }

        // Generate a unique filename using UUID
        val fileName = "recipe_${UUID.randomUUID()}.jpg"
        val storageRef: StorageReference = storage.reference.child("recipe_images/$fileName")

        val uploadTask = storageRef.putFile(imageUri)

        uploadTask
            .addOnProgressListener { taskSnapshot ->
                // Calculate upload progress
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress?.invoke(progress)
            }
            .addOnSuccessListener {
                // Upload successful, now get the download URL
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }.addOnFailureListener { exception ->
                    onFailure(exception)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Uploads a bitmap image to Firebase Storage in the "recipe_images" folder.
     * The bitmap is compressed to JPEG before uploading.
     *
     * @param bitmap The bitmap to upload (typically a cropped square image)
     * @param onSuccess Callback with the download URL on successful upload
     * @param onFailure Callback with exception on failure
     * @param onProgress Optional callback with upload progress (0-100)
     */
    fun uploadRecipeBitmap(
        bitmap: Bitmap,
        onSuccess: (String) -> kotlin.Unit,
        onFailure: (Exception) -> kotlin.Unit,
        onProgress: ((Int) -> kotlin.Unit)? = null
    ) {
        // Generate a unique filename using UUID
        val fileName = "recipe_${UUID.randomUUID()}.jpg"
        val storageRef: StorageReference = storage.reference.child("recipe_images/$fileName")

        // Compress bitmap to JPEG byte array
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val data = baos.toByteArray()

        // Check compressed size
        if (data.size > MAX_FILE_SIZE_BYTES) {
            onFailure(Exception("Compressed image exceeds 3 MB limit. Please try a smaller or simpler image."))
            return
        }

        val uploadTask = storageRef.putBytes(data)

        uploadTask
            .addOnProgressListener { taskSnapshot ->
                // Calculate upload progress
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress?.invoke(progress)
            }
            .addOnSuccessListener {
                // Upload successful, now get the download URL
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }.addOnFailureListener { exception ->
                    onFailure(exception)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Deletes an image from Firebase Storage given its URL.
     * Call this when a recipe is deleted or its image is replaced.
     *
     * @param imageUrl The download URL of the image to delete
     * @param onSuccess Callback on successful deletion
     * @param onFailure Callback with exception on failure
     */
    fun deleteRecipeImage(
        imageUrl: String,
        onSuccess: () -> kotlin.Unit = {},
        onFailure: (Exception) -> kotlin.Unit = {}
    ) {
        if (imageUrl.isEmpty()) {
            onSuccess()
            return
        }

        try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { exception -> onFailure(exception) }
        } catch (e: Exception) {
            // Invalid URL or not a Firebase Storage URL
            onFailure(e)
        }
    }
}
