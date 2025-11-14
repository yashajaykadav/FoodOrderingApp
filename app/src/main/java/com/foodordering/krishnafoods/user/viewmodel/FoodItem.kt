// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.user.viewmodel

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class FoodItem(
    @DocumentId
    var id: String = "",

    @get:PropertyName("imageUrl")
    @set:PropertyName("imageUrl")
    var imageUrl: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("originalPrice")
    @set:PropertyName("originalPrice")
    var originalPrice: Int = 0,

    @get:PropertyName("offerPrice")
    @set:PropertyName("offerPrice")
    var offerPrice: Int = 0,

    @get:PropertyName("weight")
    @set:PropertyName("weight")
    var weight: String = "",

    @get:PropertyName("category")
    @set:PropertyName("category")
    var category: String = "",

    // ✅ This is now an in-memory property, not stored in Firestore
    var quantity: Int = 1, // Default to 1, useful for adding to cart

    // This property is also not stored in Firestore.
    // If you want it stored, add @PropertyName("description")
    var description: String = ""
) {
    // A no-arg constructor is required by Firestore for deserialization.
    constructor() : this(id = "")
}