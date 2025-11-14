package com.foodordering.krishnafoods.user.manager

import android.content.Context
import com.foodordering.krishnafoods.user.viewmodel.FoodItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

object CartManager {
    private val cartItems = mutableListOf<FoodItem>()

    private const val PREFS_NAME = "cart_prefs"
    private const val KEY_CART_ITEMS = "cart_data"

    fun addToCart(context: Context, foodItem: FoodItem) {
        if (foodItem.id.isBlank()) {
            throw IllegalArgumentException("FoodItem must have a valid ID before adding to cart.")
        }

        val existingItemIndex = cartItems.indexOfFirst { it.id == foodItem.id }
        if (existingItemIndex != -1) {
            val existingItem = cartItems[existingItemIndex]
            val updatedQuantity = existingItem.quantity + foodItem.quantity
            cartItems[existingItemIndex] = existingItem.copy(quantity = updatedQuantity)
        } else {
            cartItems.add(foodItem.copy(quantity = foodItem.quantity))
        }

        saveCart(context) // persist immediately after adding
    }

    fun removeFromCart(context: Context, foodItem: FoodItem) {
        val iterator = cartItems.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.name == foodItem.name &&
                item.weight == foodItem.weight &&
                item.category == foodItem.category) {
                iterator.remove()
                break
            }
        }
        saveCart(context) // ✅ ensures persistence
    }
    // Add this function inside your 'CartManager' object
    fun updateItemQuantity(context: Context, itemId: String, newQuantity: Int) {
        val itemIndex = cartItems.indexOfFirst { it.id == itemId }
        if (itemIndex != -1) {
            val item = cartItems[itemIndex]
            // Only update and save if the quantity has actually changed
            if (item.quantity != newQuantity) {
                cartItems[itemIndex] = item.copy(quantity = newQuantity)
                saveCart(context) // Persist the change
            }
        }
    }


    fun getCartItems(): MutableList<FoodItem> = cartItems.toMutableList()

    fun clearCart(context: Context) { // Add context parameter
        cartItems.clear()
        saveCart(context) // Add this line to save the empty cart
    }

    // Save the current cartItems list to SharedPreferences as JSON
    fun saveCart(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(cartItems)
        prefs.edit { putString(KEY_CART_ITEMS, json) }
    }

    // Load the cartItems list from SharedPreferences JSON
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
