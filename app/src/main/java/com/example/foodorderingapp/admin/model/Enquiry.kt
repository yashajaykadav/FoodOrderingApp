package com.example.foodorderingapp.admin.model

data class Enquiry(
    var id: String = "",
    var reply: String = "",
    var message: String = "",
    var userId: String = "",
    var userName: String = "Loading..." // 🔥 Default value while fetching
)
