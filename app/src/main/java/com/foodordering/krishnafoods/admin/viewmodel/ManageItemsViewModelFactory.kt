package com.foodordering.krishnafoods.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.foodordering.krishnafoods.admin.repository.FoodItemsRepository

class ManageItemsViewModelFactory(private val repository: FoodItemsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageItemsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManageItemsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}