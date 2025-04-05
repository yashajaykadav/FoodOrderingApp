package com.example.foodorderingapp.user.viewmodel

import com.google.firebase.firestore.PropertyName
// FoodItem.kt
data class FoodItem(
    @get:PropertyName("imageUrl")  // Changed to match variable name
    @set:PropertyName("imageUrl")
    var imageUrl: String = "",

    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("price")
    @set:PropertyName("price")
    var price: Int = 0,

    @get:PropertyName("quantity")
    @set:PropertyName("quantity")
    var quantity: Int = 0,

    @get:PropertyName("stock")
    @set:PropertyName("stock")
    var stock: Int = 0,

    @get:PropertyName("weight")
    @set:PropertyName("weight")
    var weight: String = "",

    @get:PropertyName("category")
    @set:PropertyName("category")
    var category: String = ""
) {
    constructor() : this("", "", "", 0, 0, 0, "", "")
}