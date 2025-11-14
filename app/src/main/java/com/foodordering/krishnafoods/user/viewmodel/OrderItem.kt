package com.foodordering.krishnafoods.user.viewmodel

data class OrderItem(
    var id: String = "",
    val userId: String = "",
    val items: List<Map<String, Any>> = emptyList(),
    val totalAmount: Int = 0,
    var orderDate: String = "", // Keep as String
    var status: String = "Pending",
    var weight: String = "",
    var rejectionReason: String? = null
)