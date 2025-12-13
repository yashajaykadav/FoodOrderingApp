package com.foodordering.krishnafoods.user.repository

import com.foodordering.krishnafoods.user.viewmodel.FoodItem
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class FoodRepository {
    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    fun listenToFoodItems(onResult: (List<FoodItem>) -> Unit, onError: (Exception) -> Unit) {
        // Remove old listener if any
        listener?.remove()

        listener = db.collection("foods")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                if (snapshot == null || snapshot.isEmpty) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                val foodList = snapshot.documents.mapNotNull { document ->
                    document.toObject(FoodItem::class.java)?.apply {
                        id = document.id // ensure ID is set
                    }
                }
                onResult(foodList)
            }
    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }
    suspend fun fetchFoodsByIds(ids: List<String>): List<FoodItem> {
        if (ids.isEmpty()) return emptyList()

        val CHUNK = 10
        val results = mutableListOf<FoodItem>()

        val chunks = ids.chunked(CHUNK)
        for (chunk in chunks) {
            val q = db.collection("foods")
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .await()

            q.documents.forEach { snap ->
                snap.toObject(FoodItem::class.java)?.let { results.add(it) }
            }
        }
        return results
    }

    fun getCategories(onResult: (List<String>) -> Unit) {
        db.collection("foods").get()
            .addOnSuccessListener { result ->
                val categories = result.documents
                    .mapNotNull { it.getString("category") }
                    .distinct()
                onResult(categories)
            }
    }
}
