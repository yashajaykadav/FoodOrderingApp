package com.foodordering.krishnafoods.admin.util

// Author: Yash Kadav
// Email: yashkadav52@gmail.com

import com.google.firebase.firestore.FirebaseFirestore

object UserCache {
    private val cache = mutableMapOf<String, Pair<String, String>>() // ID -> (Name, Address)
    private val db = FirebaseFirestore.getInstance()

    fun getUserDetails(userId: String, onResult: (name: String, address: String) -> Unit) {
        if (cache.containsKey(userId)) {
            val (name, address) = cache[userId]!!
            onResult(name, address)
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Unknown"
                val address = doc.getString("address") ?: "Not Specified"
                cache[userId] = Pair(name, address)
                onResult(name, address)
            }
            .addOnFailureListener {
                onResult("Unknown", "Not Specified")
            }
    }
}
