package com.example.foodorderingapp.user.viewmodel

import com.google.firebase.firestore.PropertyName

data class FoodItem(
    @get:PropertyName("imgUrl") val imageUrl: String = "",
    @get:PropertyName("id") var id: String = "",
    @get:PropertyName("name") val name: String = "",
    @get:PropertyName("price") val price: Int = 0,
    @get:PropertyName("quantity") var quantity: Int = 0,
    @get:PropertyName("stock") var stock: Int = 0,
    @get:PropertyName("weight") val weight: String = "",
    @get:PropertyName("category") val category: String = ""
) {
    constructor() : this("","", "", 0, 0, 0, "", "")
}
