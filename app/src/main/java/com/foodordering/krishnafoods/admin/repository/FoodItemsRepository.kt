package com.foodordering.krishnafoods.admin.repository

import com.foodordering.krishnafoods.admin.model.FoodItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FoodItemsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val foodCollection = db.collection("foods")

    fun getFoodItems(): Flow<List<FoodItem>> = callbackFlow {
        val listenerRegistration = foodCollection.addSnapshotListener { documents, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val foodList = documents?.map { document ->
                // Firestore's toObject requires a no-arg constructor
                document.toObject(FoodItem::class.java).apply {
                    id = document.id // Manually set the ID from the document reference
                }
            } ?: emptyList()

            trySend(foodList)
        }
        awaitClose {
            listenerRegistration.remove()
        }
    }

    suspend fun updateFoodItem(foodId: String, updates: Map<String, Any>) {
        foodCollection.document(foodId).update(updates).await()
    }

    suspend fun deleteFoodItem(foodId: String) {
        foodCollection.document(foodId).delete().await()
    }
}