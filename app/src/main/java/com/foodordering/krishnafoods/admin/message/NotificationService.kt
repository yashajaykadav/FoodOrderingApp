// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.message

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class NotificationService() {

    private val client = OkHttpClient()

    // ✅ YOUR RENDER URL (Added /send-notification endpoint)
    private val BACKEND_URL = "https://notification-backend-wwor.onrender.com/send-notification"

    /**
     * Call this function from ViewModel when status updates
     */
    suspend fun sendOrderStatusNotification(
        userId: String,
        orderId: String,
        status: String,
        reason: String? = null
    ) = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch User's FCM Token from Firestore
            // We need this because your backend sends the message to a specific token
            val db = FirebaseFirestore.getInstance()
            val userSnap = db.collection("users").document(userId).get().await()
            val fcmToken = userSnap.getString("fcmToken")

            if (fcmToken.isNullOrEmpty()) {
                Log.e("NotificationService", "User $userId has no FCM token")
                return@withContext
            }

            // 2. Prepare JSON Payload matching your Node.js code
            val json = JSONObject().apply {
                put("userId", userId)
                put("orderId", orderId)
                put("status", status)
                put("fcmToken", fcmToken)
                if (reason != null) put("reason", reason)
            }

            // 3. Send POST Request to Render
            val request = Request.Builder()
                .url(BACKEND_URL)
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Log.d("NotificationService", "Notification Sent Successfully")
            } else {
                Log.e("NotificationService", "Failed: ${response.code} ${response.message}")
            }
            response.close()

        } catch (e: Exception) {
            Log.e("NotificationService", "Error sending notification", e)
        }
    }
}