package com.foodordering.krishnafoods.user.repository

import com.foodordering.krishnafoods.user.viewmodel.FoodItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

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
