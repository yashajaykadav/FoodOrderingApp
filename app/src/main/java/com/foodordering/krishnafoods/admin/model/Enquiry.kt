package com.foodordering.krishnafoods.admin.model

data class Enquiry(
    var id: String = "",
    var reply: String = "",
    var message: String = "",
    var userId: String = "",
    var userName: String = "Loading...", // Default while fetching
    var timestamp: Long = 0L             // Important for sorting messages chronologically
)
