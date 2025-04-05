package com.example.foodorderingapp.admin.model

import com.example.foodorderingapp.user.viewmodel.FoodItem

data class Order(
    var userId: String = "",
    var orderId: String = "",
    var shopName: String = "",
    var totalAmount: Int = 0,
    var status: String = "Pending",
    var items: List<FoodItem> = emptyList(),
    var orderDate: String = "",
    var contact: String = "",
    var rejectionReason: String? = null,
    var userName: String = "Loading...",
    var address: String = "Loading..."
)


