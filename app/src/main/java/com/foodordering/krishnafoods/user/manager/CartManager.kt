// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.user.manager

import android.content.Context
import androidx.core.content.edit
import com.foodordering.krishnafoods.user.viewmodel.FoodItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CartManager {
    private val cartItems = mutableListOf<FoodItem>()

    private const val PREFS_NAME = "cart_prefs"
    private const val KEY_CART_ITEMS = "cart_data"

    fun addToCart(context: Context, foodItem: FoodItem) {
        if (foodItem.id.isBlank()) {
            throw IllegalArgumentException("FoodItem must have a valid ID.")
        }

        val index = cartItems.indexOfFirst { it.id == foodItem.id }
        if (index != -1) {
            // Update quantity if item exists
            val existing = cartItems[index]
            cartItems[index] = existing.copy(quantity = existing.quantity + foodItem.quantity)
        } else {
            // Add new item
            cartItems.add(foodItem.copy(quantity = foodItem.quantity))
        }
        saveCart(context)
    }

    fun removeFromCart(context: Context, foodItem: FoodItem) {
        // Use ID for consistent removal
        val removed = cartItems.removeAll { it.id == foodItem.id }
        if (removed) {
            saveCart(context)
        }
    }

    fun updateItemQuantity(context: Context, itemId: String, newQuantity: Int) {
        val index = cartItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            if (newQuantity <= 0) {
                // Remove item if quantity drops to 0 or less
                cartItems.removeAt(index)
            } else {
                // Update specific item quantity
                cartItems[index] = cartItems[index].copy(quantity = newQuantity)
            }
            saveCart(context)
        }
    }

    // Returns a copy to prevent external modification of the source list
    fun getCartItems(): MutableList<FoodItem> = cartItems.toMutableList()

    fun clearCart(context: Context) {
        cartItems.clear()
        saveCart(context)
    }

    // Persist list to SharedPreferences
    fun saveCart(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(cartItems)
        prefs.edit { putString(KEY_CART_ITEMS, json) }
    }

    // Load list from SharedPreferences
    fun loadCart(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CART_ITEMS, null)

        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<FoodItem>>() {}.type
            val loadedItems: List<FoodItem> = Gson().fromJson(json, type)
            cartItems.clear()
            cartItems.addAll(loadedItems)
        }
    }
}