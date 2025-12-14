// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.model

import android.os.Parcelable
import com.foodordering.krishnafoods.admin.util.FirestoreMapper
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

@Parcelize
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
) : Parcelable {

    companion object {
        const val STATUS_PENDING = "Pending"
        const val STATUS_ACCEPTED = "Accepted"
        const val STATUS_REJECTED = "Rejected"
        const val STATUS_DELIVERED = "Delivered"

        // Factory method using the reusable Mapper
        fun fromDocument(doc: DocumentSnapshot): Order {
            return Order(
                orderId = doc.id,
                userId = doc.getString("userId") ?: "",
                shopName = doc.getString("shopName") ?: "",
                totalAmount = (doc.get("totalAmount") as? Number)?.toInt() ?: 0,
                status = doc.getString("status") ?: STATUS_PENDING,
                // Module: Uses the shared parser we created earlier
                items = FirestoreMapper.parseFoodItems(doc.get("items")),
                orderDate = doc.getString("orderDate") ?: "",
                contact = doc.getString("contact") ?: "",
                rejectionReason = doc.getString("rejectionReason"),
                userName = doc.getString("userName") ?: "",
                address = doc.getString("address") ?: ""
            )
        }
    }

    // Helper properties
//    val isPending get() = status == STATUS_PENDING
//    val isAccepted get() = status == STATUS_ACCEPTED
    val formattedTotal get() = "₹$totalAmount"
//    fun isPending() = status == STATUS_PENDING
//    fun isAccepted() = status == STATUS_ACCEPTED
    fun isRejected() = status == STATUS_REJECTED
    fun isDelivered() = status == STATUS_DELIVERED
}