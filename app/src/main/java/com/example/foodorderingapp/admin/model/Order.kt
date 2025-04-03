package com.example.foodorderingapp.admin.model

import com.example.foodorderingapp.user.viewmodel.FoodItem

data class Order(
    var userId: String = "",
    var orderId: String = "",
    var userName: String = "",
    var shopName: String = "",
    var userAddress: String = "",
    var totalAmount: Int = 0,
    var status: String = "",
    var items: List<FoodItem> = emptyList(),
    var orderDate: String = "",
    var contact: String = ""
)
