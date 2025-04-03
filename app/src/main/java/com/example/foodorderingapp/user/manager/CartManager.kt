package com.example.foodorderingapp.user.manager

import com.example.foodorderingapp.user.viewmodel.FoodItem

object CartManager {
    private val cartItems = mutableListOf<FoodItem>()

    fun addToCart(foodItem: FoodItem) {
        val existingItem = cartItems.find { it.name == foodItem.name
                && it.weight==foodItem.weight && it.category == foodItem.category }
        if (existingItem != null) {
            existingItem.quantity += 1
        } else {
            cartItems.add(foodItem.copy(quantity = 1))
        }
    }

    fun removeFromCart(foodItem: FoodItem) {
        val iterator = cartItems.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.name == foodItem.name) {
                iterator.remove() // ✅ Ensure the item is fully removed
                break
            }
        }
    }

    fun getCartItems(): MutableList<FoodItem> = cartItems.toMutableList()

    fun clearCart() {
        cartItems.clear()
    }
}
