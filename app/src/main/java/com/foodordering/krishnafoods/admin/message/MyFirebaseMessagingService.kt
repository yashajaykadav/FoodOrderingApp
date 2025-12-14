// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.message

import com.foodordering.krishnafoods.admin.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 1. Check for standard Notification Payload
        remoteMessage.notification?.let {
            NotificationHelper.showNotification(
                applicationContext,
                it.title ?: "New Update",
                it.body ?: "Check dashboard",
                remoteMessage.data["orderId"] // Extract Order ID if available
            )
        }

        // 2. Fallback: Check Data Payload if Notification Payload is empty
        if (remoteMessage.notification == null && remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "Order Update"
            val body = remoteMessage.data["body"] ?: "Status changed"
            val orderId = remoteMessage.data["orderId"]

            NotificationHelper.showNotification(applicationContext, title, body, orderId)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        updateTokenOnServer(token)
    }

    private fun updateTokenOnServer(token: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .update("fcmToken", token)
                .addOnFailureListener {
                    // Log error or retry logic here
                }
        }
    }
}