package com.example.foodorderingapp.admin.model

import com.google.firebase.firestore.DocumentSnapshot

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val shopName: String = "",
    val totalAmount: Int = 0,
    var status: String = STATUS_PENDING,
    val items: List<FoodItem> = emptyList(),
    val orderDate: String = "",
    val contact: String = "",
    var rejectionReason: String? = null,
    val userName: String = "",
    val address: String = ""
) {
    companion object {
        const val STATUS_PENDING = "Pending"
        const val STATUS_ACCEPTED = "Accepted"
        const val STATUS_REJECTED = "Rejected"
        const val STATUS_DELIVERED = "Delivered"

        fun fromDocument(doc: DocumentSnapshot): Order? {
            return try {
                val items = (doc.get("items") as? List<Map<String, Any>>)?.mapNotNull { item ->
                    FoodItem(
                        id = item["id"] as? String ?: "",
                        name = item["name"] as? String ?: return@mapNotNull null,
                        price = (item["price"] as? Number)?.toInt() ?: 0,
                        quantity = (item["quantity"] as? Number)?.toInt() ?: 0,
                        stock = (item["stock"] as? Number)?.toInt() ?: 0,
                        weight = item["weight"] as? String ?: ""
                    )
                } ?: emptyList()

                Order(
                    orderId = doc.id,
                    userId = doc.getString("userId") ?: "",
                    shopName = doc.getString("shopName") ?: "",
                    totalAmount = (doc.get("totalAmount") as? Number)?.toInt() ?: 0,
                    status = doc.getString("status") ?: STATUS_PENDING,
                    items = items,
                    orderDate = doc.getString("orderDate") ?: "",
                    contact = doc.getString("contact") ?: "",
                    rejectionReason = doc.getString("rejectionReason"),
                    userName = doc.getString("userName") ?: "",
                    address = doc.getString("address") ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun isPending() = status == STATUS_PENDING
    fun isAccepted() = status == STATUS_ACCEPTED
    fun isRejected() = status == STATUS_REJECTED
    fun isDelivered() = status == STATUS_DELIVERED

    fun getFormattedTotal(): String {
        return "₹${totalAmount.toDouble().format(2)}"
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)
}