package com.example.foodorderingapp.user.viewmodel

import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.*

data class OrderItem(
    var id: String = "",
    val userId: String = "",
    val items: List<Map<String, Any>> = emptyList(),
    val totalAmount: Int = 0,
    var orderDate: String = "", // Keep as String
    var status: String = "",
    var weight: String = "",
    var rejectionReason: String? = null
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): OrderItem {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val orderDate = when (val dateField = document.get("orderDate")) {
                is com.google.firebase.Timestamp -> dateFormat.format(dateField.toDate()) // Convert Timestamp to String
                is String -> dateField // Already a string
                else -> ""
            }

            return OrderItem(
                id = document.id,
                userId = document.getString("userId") ?: "",
                items = document.get("items") as? List<Map<String, Any>> ?: emptyList(),
                totalAmount = document.get("totalAmount", Int::class.java) ?: 0,
                orderDate = orderDate, // ✅ Always store as String
                status = document.getString("status") ?: "",
                weight = document.getString("weight") ?: "",
                rejectionReason = document.getString("rejectionReason")
            )
        }
    }
}
