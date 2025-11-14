package com.foodordering.krishnafoods.admin.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var id: String = "",                // Firestore document ID (set manually after fetching)
    var uid: String = "",                // Firebase Authentication UID
    var name: String = "",               // Full name
    var email: String = "",              // Email address
    var role: String = "user",           // "admin" or "user"
    var photoUrl: String? = null,        // Profile image URL (we'll map from photoUrl)
    var address: String? = null,         // Full address
    var contact: String? = null,         // Phone number
    var shopName: String? = null,        // Shop name (if applicable)
    var fcmToken: String? = null,        // Device push notification token
    var createdAt: Long? = null,         // Epoch millis timestamp
    var updatedAt: Long? = null          // Epoch millis timestamp if updated
)
