package com.foodordering.krishnafoods.admin.viewmodel

// Author: Yash Kadav
// Email: yashkadav52@gmail.com

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderDetailsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _statusUpdateResult = MutableStateFlow<Result<String>?>(null)
    val statusUpdateResult = _statusUpdateResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun updateOrderStatus(orderId: String, status: String, reason: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updates = mutableMapOf<String, Any>(
                    "status" to status,
                    "updatedAt" to System.currentTimeMillis()
                )
                if (!reason.isNullOrBlank()) {
                    updates["rejectionReason"] = reason
                }

                db.collection("orders").document(orderId).update(updates).await()
                _statusUpdateResult.value = Result.success(status)
            } catch (e: Exception) {
                _statusUpdateResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}