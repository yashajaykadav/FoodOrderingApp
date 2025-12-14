// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.repository

import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.foodordering.krishnafoods.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AdminRepository {

    private val db = FirebaseFirestore.getInstance()

    // Converts the Cloudinary callback into a Coroutine
    suspend fun uploadImageToCloudinary(filePath: String): String = suspendCancellableCoroutine { cont ->
        MediaManager.get().upload(filePath)
            .option("public_id", "food_${System.currentTimeMillis()}")
            .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET)
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"]?.toString()
                    if (url != null) {
                        cont.resume(url)
                    } else {
                        cont.resumeWithException(Exception("Upload successful but URL is null"))
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    cont.resumeWithException(Exception(error.description))
                }

                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, total: Long) {}
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    suspend fun addFoodItem(foodItem: HashMap<String, Any>) {
        db.collection("foods").add(foodItem).await()
    }

    suspend fun fetchOptions(collection: String, docId: String): List<String> {
        return try {
            val snapshot = db.collection(collection).document(docId).get().await()
            val values = snapshot.get("values") as? List<*>
            values?.filterIsInstance<String>() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchCategories(): List<String> {
        return try {
            val snapshot = db.collection("categories").get().await()
            snapshot.documents.mapNotNull { it.getString("name") }
        } catch (e: Exception) {
            emptyList()
        }
    }
}