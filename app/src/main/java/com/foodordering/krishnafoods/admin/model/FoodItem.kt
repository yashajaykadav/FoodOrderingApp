package com.foodordering.krishnafoods.admin.model
import com.google.firebase.firestore.PropertyName

data class FoodItem(
    @get:PropertyName("foodId") @set:PropertyName("foodId")
    var id: String = "",

    // Change val to var here to allow the setter annotation
    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl")
    var imgUrl :String = "",

    var name: String = "",
    var originalPrice: Int = 0,
    var quantity: Int = 0,
    var offerPrice: Int? = null,
    val weight: String = "",
    val category: String = "",
    var totalAmount: Int?  = 0
) {
    // This is still required for Firestore's automatic deserialization
    constructor() : this("","", "", 0, 0, null, "", "",0)
}