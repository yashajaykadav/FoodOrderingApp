package com.foodordering.krishnafoods.admin.message

import android.content.Context
import com.foodordering.krishnafoods.R
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import java.util.concurrent.TimeUnit

class NotificationService(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun sendOrderStatusNotification(
        userId: String,
        orderId: String,
        status: String,
        reason: String? = null
    ) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val token = document.getString("fcmToken") ?: return@addOnSuccessListener

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val credentials = getGoogleCredentials()
                        credentials.refreshIfExpired()
                        val accessToken = credentials.accessToken?.tokenValue ?: return@launch

                        val messageJson = buildFcmMessage(token, orderId, status, reason)
                        sendFcmNotification(accessToken, messageJson)
                    } catch (_: Exception) {
                        // ignored in release
                    }
                }
            }
            .addOnFailureListener {
                // ignored in release
            }
    }

    private fun getGoogleCredentials(): GoogleCredentials {
        return try {
            val inputStream: InputStream = context.resources.openRawResource(R.raw.service_account)
            GoogleCredentials.fromStream(inputStream)
                .createScoped("https://www.googleapis.com/auth/cloud-platform")
        } catch (e: Exception) {
            throw RuntimeException("Failed to load service account credentials", e)
        }
    }

    private fun buildFcmMessage(
        token: String,
        orderId: String,
        status: String,
        reason: String?
    ): String {
        val shortId = orderId.takeLast(6).uppercase()

        val (title, body) = when (status) {
            "Accepted" -> context.getString(R.string.notification_order_accepted_title) to
                    context.getString(R.string.notification_order_accepted_body, shortId)

            "Rejected" -> context.getString(R.string.notification_order_rejected_title) to
                    context.getString(
                        R.string.notification_order_rejected_body,
                        shortId,
                        reason ?: "No reason provided"
                    )

            "Delivered" -> context.getString(R.string.notification_order_delivered_title) to
                    context.getString(R.string.notification_order_delivered_body, shortId)

            else -> context.getString(R.string.notification_order_updated_title) to
                    context.getString(R.string.notification_order_updated_body, shortId, status)
        }

        return """
        {
            "message": {
                "token": "$token",
                "notification": {
                    "title": "$title",
                    "body": "$body"
                },
                "data": {
                    "orderId": "$orderId",
                    "status": "$status",
                    "click_action": "FLUTTER_NOTIFICATION_CLICK"
                }
            }
        }
        """.trimIndent()
    }

    private fun sendFcmNotification(accessToken: String, message: String) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = message.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://fcm.googleapis.com/v1/projects/krishnafoods-aaac1/messages:send")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.close() // no logs, just cleanup
            }

            override fun onFailure(call: Call, e: java.io.IOException) {
                // ignored in release
            }
        })
    }
}