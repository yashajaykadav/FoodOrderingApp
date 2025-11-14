package com.foodordering.krishnafoods.user.viewmodel

import com.google.firebase.firestore.DocumentId

data class Enquiry(
   val userId: String = "",
   val message: String = "",
   val reply: String = "",
   @DocumentId
   var id: String = "",
   val timestamp: Long = System.currentTimeMillis()
)
