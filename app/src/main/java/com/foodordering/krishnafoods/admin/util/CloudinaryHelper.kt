package com.foodordering.krishnafoods.admin.util

// Author: Yash Kadav
// Email: yashkadav52@gmail.com

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.foodordering.krishnafoods.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CloudinaryHelper {

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        try {
            val config = mapOf(
                "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                "api_key" to BuildConfig.CLOUDINARY_API_KEY
            )
            MediaManager.init(context, config)
            isInitialized = true
        } catch (e: Exception) {
            // Already initialized or config error
            isInitialized = true
        }
    }

    // Optimization: Converted Callback-Hell to Coroutines (Suspend function)
    suspend fun uploadImage(uri: Uri): String = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .option("public_id", "ad_${System.currentTimeMillis()}")
            .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET) // Ensure this is set in Gradle
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) {
                        continuation.resume(url)
                    } else {
                        continuation.resumeWithException(Exception("Upload successful but URL is null"))
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    // Handle reschedule if needed
                }
            })
            .dispatch()
    }
}