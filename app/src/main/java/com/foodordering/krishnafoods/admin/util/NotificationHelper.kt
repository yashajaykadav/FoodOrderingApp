// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.activity.AdminMainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "admin_order_updates"
    private const val CHANNEL_NAME = "Admin Order Alerts"
    private const val CHANNEL_DESC = "Notifications for new orders and updates"

    // Added orderId parameter for deep linking
    fun showNotification(context: Context, title: String, message: String, orderId: String? = null) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(notificationManager)

        // 1. Setup Intent with Data
        val intent = Intent(context, AdminMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // Pass the Order ID so the Activity can load the specific order
            if (orderId != null) {
                putExtra("ORDER_ID", orderId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            orderId.hashCode(), // Unique ID ensures different intents for different orders
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Use a unique ID (System.currentTimeMillis) to show multiple notifications
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun createNotificationChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }
}