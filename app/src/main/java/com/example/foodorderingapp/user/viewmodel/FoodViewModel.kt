package com.example.foodorderingapp.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foodorderingapp.user.repository.FoodRepository

class FoodViewModel : ViewModel() {
    private val repository = FoodRepository()

    private val _foodItems = MutableLiveData<List<FoodItem>>()
    val foodItems: LiveData<List<FoodItem>> get() = _foodItems

    fun fetchFoodItems() {
        repository.getFoodItems { foodList ->
            _foodItems.value = foodList
        }
    }
}
