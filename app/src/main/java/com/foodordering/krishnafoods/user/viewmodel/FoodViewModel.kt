package com.foodordering.krishnafoods.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.foodordering.krishnafoods.user.repository.FoodRepository

class FoodViewModel : ViewModel() {
    private val repository = FoodRepository()

    private val _foodItems = MutableLiveData<List<FoodItem>>()
    val foodItems: LiveData<List<FoodItem>> get() = _foodItems

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> get() = _categories

    fun startListeningToFoodItems() {
        repository.listenToFoodItems(
            onResult = { foodList -> _foodItems.value = foodList },
            onError = { _foodItems.value = emptyList() }
        )
    }

    fun stopListening() {
        repository.stopListening()
    }

    fun fetchCategories() {
        repository.getCategories { categoryList ->
            _categories.value = categoryList
        }
    }
}
