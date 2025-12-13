package com.foodordering.krishnafoods.user.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun fetchUserData(uid: String): Map<String, Any>? {
        val doc = db.collection("users").document(uid).get().await()
        return if (doc.exists()) doc.data else null
    }
}
