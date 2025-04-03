package com.example.foodorderingapp.user.repository

import com.example.foodorderingapp.user.viewmodel.FoodItem
import com.google.firebase.firestore.FirebaseFirestore

class FoodRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getFoodItems(onResult: (List<FoodItem>) -> Unit) {
        db.collection("foods").get()
            .addOnSuccessListener { result ->
                val foodList = mutableListOf<FoodItem>()
                for (document in result) {
                    val food = document.toObject(FoodItem::class.java)
                    foodList.add(food)
                }
                onResult(foodList)
            }
            .addOnFailureListener {
                onResult(emptyList())  // Handle failure
            }
    }
}
