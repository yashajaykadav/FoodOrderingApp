package com.foodordering.krishnafoods.admin.model

data class Feedback(
    val feedbackId: String = "",
    val userId: String = "",
    var userName: String? = null ,// allow update after fetching
    val message: String = "",
    val rating: Int = 0,
    val timestamp: Long? = null // Optional: for sorting by date
)   