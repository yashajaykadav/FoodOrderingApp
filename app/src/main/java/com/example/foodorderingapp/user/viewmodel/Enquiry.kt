package com.example.foodorderingapp.user.viewmodel

data class Enquiry(
   val userId: String = "",
   val message: String = "",
   val reply: String = "",
   var id: String = "" // Document ID
)
