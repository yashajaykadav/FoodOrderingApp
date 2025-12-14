// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.model

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodItem(
    @get:PropertyName("foodId") @set:PropertyName("foodId")
    var id: String = "",

    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl")
    var imgUrl: String? = null,

    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("originalPrice") @set:PropertyName("originalPrice")
    var originalPrice: Int = 0,

    @get:PropertyName("quantity") @set:PropertyName("quantity")
    var quantity: Int = 0,

    @get:PropertyName("offerPrice") @set:PropertyName("offerPrice")
    var offerPrice: Int? = null,

    @get:PropertyName("weight") @set:PropertyName("weight")
    var weight: String = "",

    @get:PropertyName("category") @set:PropertyName("category")
    var category: String = "",

    @get:PropertyName("totalAmount") @set:PropertyName("totalAmount")
    var totalAmount: Int? = 0
) : Parcelable