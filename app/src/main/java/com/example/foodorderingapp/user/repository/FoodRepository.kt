package com.example.foodorderingapp.user.repository

import com.example.foodorderingapp.user.viewmodel.FoodItem
import com.google.firebase.firestore.FirebaseFirestore

class FoodRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getFoodItems(onResult: (List<FoodItem>) -> Unit) {
        db.collection("foods").get()
            .addOnSuccessListener { result ->
                val foodList = result.documents.mapNotNull { it.toObject(FoodItem::class.java) }
                onResult(foodList)
            }
    }

    fun getCategories(onResult: (List<String>) -> Unit) {
        db.collection("foods").get()
            .addOnSuccessListener { result ->
                val categories = result.documents.mapNotNull { it.getString("category") }
                    .distinct() // ✅ Get unique categories
                onResult(categories)
            }
    }
}
