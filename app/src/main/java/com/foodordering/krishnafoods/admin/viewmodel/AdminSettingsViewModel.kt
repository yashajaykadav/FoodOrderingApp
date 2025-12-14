package com.foodordering.krishnafoods.admin.viewmodel

// Author: Yash Kadav
// Email: yashkadav52@gmail.com


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodordering.krishnafoods.admin.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminSettingsViewModel : ViewModel() {
    private val repository = UserRepository()
    private val _searchQuery = MutableStateFlow("")

    // Auto-updates UI when DB changes OR Search text changes
    val users = combine(repository.getUsersFlow(), _searchQuery) { users, query ->
        if (query.isEmpty()) users else users.filter {
            it.name.contains(query, true) || it.email.contains(query, true)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query.trim()
    }

    fun updateUserRole(userId: String, role: String) = viewModelScope.launch {
        repository.updateUserRole(userId, role)
    }

    fun deleteUser(userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        try {
            repository.deleteUserCascading(userId)
            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "Delete failed")
        }
    }
}