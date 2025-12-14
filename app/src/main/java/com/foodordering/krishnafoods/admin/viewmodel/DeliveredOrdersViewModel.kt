// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodordering.krishnafoods.admin.model.Order
import com.foodordering.krishnafoods.admin.repository.OrderRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeliveredOrdersViewModel : ViewModel() {
    private val repository = OrderRepository()

    // StateFlow for UI updates
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var lastVisible: DocumentSnapshot? = null
    private var isLastPage = false

    init {
        fetchOrders(isLoadMore = false)
    }

    fun fetchOrders(isLoadMore: Boolean) {
        if (_isLoading.value || (isLastPage && isLoadMore)) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val (newOrders, newLastDoc) = repository.getOrdersByStatus(
                    status = "Delivered",
                    lastDoc = if (isLoadMore) lastVisible else null
                )

                if (newOrders.isNotEmpty()) {
                    lastVisible = newLastDoc
                    if (isLoadMore) {
                        _orders.value += newOrders
                    } else {
                        _orders.value = newOrders
                    }
                } else {
                    isLastPage = true
                }
            } catch (e: Exception) {
                // Handle error (expose via another flow if needed)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}