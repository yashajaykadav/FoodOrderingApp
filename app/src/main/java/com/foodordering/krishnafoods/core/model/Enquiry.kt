package com.foodordering.krishnafoods.core.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Enquiry(

    @DocumentId
    val id: String = "",

    val userId: String = "",
    val message: String = "",
    val reply: String = "",
    val timestamp: Long = System.currentTimeMillis(),

    // Admin-only, local field
    @get:Exclude
    val userName: String = ""
)
