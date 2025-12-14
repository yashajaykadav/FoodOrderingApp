// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.foodordering.krishnafoods.admin.adapter.OrderAction
import com.foodordering.krishnafoods.admin.message.NotificationService
import com.foodordering.krishnafoods.admin.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AllOrdersViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    // Initialize the Notification Service using the Application Context
    private val notificationService = NotificationService()

    // 1. Raw Data from Firestore
    private val _rawOrders = MutableStateFlow<List<Order>>(emptyList())

    // 2. Search Query State
    private val _searchQuery = MutableStateFlow("")

    // 3. Combined UI State
    val uiOrders = combine(_rawOrders, _searchQuery) { orders, query ->
        if (query.isBlank()) {
            orders
        } else {
            orders.filter {
                it.shopName.contains(query, ignoreCase = true) ||
                        it.orderId.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setStatusFilter(status: String?) {
        listener?.remove()

        var query: Query = db.collection("orders")
            .orderBy("orderDate", Query.Direction.DESCENDING)

        if (status != null) {
            query = query.whereEqualTo("status", status)
        }

        listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            val parsedOrders = snapshot?.documents?.mapNotNull { doc ->
                try { Order.fromDocument(doc) } catch (_: Exception) { null }
            } ?: emptyList()

            _rawOrders.value = parsedOrders
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateOrderStatus(order: Order, action: OrderAction, reason: String?, onResult: (Boolean) -> Unit) {
        val updates = mutableMapOf<String, Any>()

        // Determine new status string for DB and Notification
        val newStatus = when (action) {
            OrderAction.ACCEPT -> "Accepted"
            OrderAction.DELIVER -> "Delivered"
            OrderAction.REJECT -> "Rejected"
        }

        updates["status"] = newStatus
        if (action == OrderAction.REJECT && reason != null) {
            updates["rejectionReason"] = reason
        }

        // 1. Update Firestore
        db.collection("orders").document(order.orderId)
            .update(updates)
            .addOnSuccessListener {
                onResult(true)

                // 2. Send Notification on Success
                // Launch in viewModelScope so it runs asynchronously
                viewModelScope.launch {
                    notificationService.sendOrderStatusNotification(
                        userId = order.userId,
                        orderId = order.orderId,
                        status = newStatus,
                        reason = reason
                    )
                }
            }
            .addOnFailureListener { onResult(false) }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}