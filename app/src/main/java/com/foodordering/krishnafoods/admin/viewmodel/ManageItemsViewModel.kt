package com.foodordering.krishnafoods.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodordering.krishnafoods.admin.model.FoodItem
import com.foodordering.krishnafoods.admin.repository.FoodItemsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ManageItemsViewModel(private val repository: FoodItemsRepository) : ViewModel() {

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    private val _filteredFoodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val filteredFoodItems: StateFlow<List<FoodItem>> = _filteredFoodItems.asStateFlow()

    init {
        fetchFoodItems()
    }

    fun fetchFoodItems() {
        viewModelScope.launch {
            repository.getFoodItems().collect { list ->
                _foodItems.value = list
                filterFoodItems("") // Initially show all items
            }
        }
    }

    fun filterFoodItems(query: String) {
        val searchQuery = query.lowercase().trim()
        val newList = if (searchQuery.isBlank()) {
            _foodItems.value
        } else {
            _foodItems.value.filter { foodItem ->
                foodItem.name.lowercase().contains(searchQuery) ||
                        foodItem.category.lowercase().contains(searchQuery) ||
                        foodItem.weight.lowercase().contains(searchQuery) ||
                        foodItem.originalPrice.toString().contains(searchQuery) ||
                        (foodItem.offerPrice?.toString()?.contains(searchQuery) ?: false)
            }
        }
        _filteredFoodItems.value = newList
    }

    fun updateFoodItemPrice(foodId: String, originalPrice: Int, offerPrice: Int?) {
        viewModelScope.launch {
            val updates = mutableMapOf<String, Any>(
                "originalPrice" to originalPrice
            )
            updates["offerPrice"] = offerPrice ?: ""
            repository.updateFoodItem(foodId, updates)
        }
    }

    fun deleteFoodItem(foodId: String) {
        viewModelScope.launch {
            repository.deleteFoodItem(foodId)
        }
    }
}
