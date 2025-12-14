// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.repository

import com.foodordering.krishnafoods.admin.model.Order
import com.foodordering.krishnafoods.admin.util.FirestoreMapper
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val pageSize = 10L

    suspend fun getOrdersByStatus(
        status: String,
        lastDoc: DocumentSnapshot?
    ): Pair<List<Order>, DocumentSnapshot?> {

        var query = db.collection("orders")
            .whereEqualTo("status", status)
            .limit(pageSize)

        if (lastDoc != null) {
            query = query.startAfter(lastDoc)
        }

        val snapshot = query.get().await()
        if (snapshot.isEmpty) return Pair(emptyList(), lastDoc)

        val orders = snapshot.documents.map { doc ->
            Order(
                orderId = doc.id,
                shopName = doc.getString("shopName") ?: "Unknown",
                totalAmount = (doc.get("totalAmount") as? Number)?.toInt() ?: 0,
                status = doc.getString("status") ?: status,
                userId = doc.getString("userId") ?: "N/A",
                address = doc.getString("address") ?: "No Address",
                contact = doc.getString("contact") ?: "No Contact",
                orderDate = doc.getString("orderDate") ?: "No Date",
                items = FirestoreMapper.parseFoodItems(doc.get("items"))
            )
        }

        return Pair(orders, snapshot.documents.last())
    }
}