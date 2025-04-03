package com.example.foodorderingapp.admin.model

data class Feedback(
    val feedbackId: String = "",
    val userId: String = "",
    var userName: String = "",
    val message: String = "",
    val rating: Int = 0,
    val timestamp: Long? = null // Optional: for sorting by date
)   