package com.example.foodorderingapp.admin.model

import com.example.foodorderingapp.user.viewmodel.FoodItem

data class FoodItem(
    var id: String = "",
    var name: String = "",
    var price: Int = 0,
    var quantity: Int = 0, // ✅ Now always an Integer
    var stock: Int = 0,
    val weight: String="",
    val category: String="",
) {
    constructor() : this("", "", 0, 0, 0,"","")

    companion object {
        fun fromFirestore(map: Map<String, Any>): FoodItem {
            return FoodItem(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                price = (map["price"] as? Number)?.toInt() ?: 0,
                quantity = (map["quantity"] as? Number)?.toInt() ?: 0, // ✅ Converts both Long & Int correctly
                stock = (map["availableQuantity"] as? Number)?.toInt() ?: 0,
                weight = (map["weight"] as? String ?: ""),
                category = (map["category"] as? String ?: "")
            )
        }
    }
}
