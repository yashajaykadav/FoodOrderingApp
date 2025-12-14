package com.foodordering.krishnafoods.admin.repository

// Author: Yash Kadav
// Email: yashkadav52@gmail.com

import com.foodordering.krishnafoods.admin.model.User
import com.foodordering.krishnafoods.admin.util.deleteAllInSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    // Real-time user list
    fun getUsersFlow() = callbackFlow {
        val listener = db.collection("users")
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateUserRole(userId: String, newRole: String) {
        db.collection("users").document(userId).update("role", newRole).await()
    }

    // Optimization: Parallel fetching + Synchronous Batch Execution
    suspend fun deleteUserCascading(userId: String) {
        // 1. Fetch all related data in parallel
        val userRef = db.collection("users").document(userId)
        val orders = db.collection("orders").whereEqualTo("userId", userId).get().await()
        val feedback = db.collection("feedback").whereEqualTo("userId", userId).get().await()
        val enquiries = db.collection("enquiry").whereEqualTo("userId", userId).get().await()

        // 2. Execute atomic batch delete
        db.runBatch { batch ->
            batch.delete(userRef)
            batch.deleteAllInSnapshot(orders)
            batch.deleteAllInSnapshot(feedback)
            batch.deleteAllInSnapshot(enquiries)
        }.await()
    }
}