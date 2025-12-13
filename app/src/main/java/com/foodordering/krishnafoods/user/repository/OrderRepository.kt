package com.foodordering.krishnafoods.user.repository

import com.foodordering.krishnafoods.user.viewmodel.FoodItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun saveOrder(
        userId: String,
        contact: String,
        shopName: String,
        address: String,
        items: List<FoodItem>,
        totalAmount: Double,
        status: String
    ): String {
        val orderMap = hashMapOf(
            "userId" to userId,
            "contact" to contact,
            "shopName" to shopName,
            "address" to address,
            "items" to items.map {
                mapOf(
                    "id" to it.id,
                    "name" to it.name,
                    "originalPrice" to it.originalPrice,
                    "offerPrice" to it.offerPrice,
                    "quantity" to it.quantity,
                    "weight" to it.weight
                )
            },
            "totalAmount" to totalAmount,
            "orderDate" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            "status" to status
        )

        val docRef = db.collection("orders").add(orderMap).await()
        return docRef.id
    }
}
