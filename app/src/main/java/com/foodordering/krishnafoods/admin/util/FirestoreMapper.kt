// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.util

import com.foodordering.krishnafoods.admin.model.FoodItem

object FirestoreMapper {
    fun parseFoodItems(itemsObj: Any?): List<FoodItem> {
        val itemsList = mutableListOf<FoodItem>()

        // 1. Safe Cast to List<*>
        val rawList = itemsObj as? List<*> ?: return emptyList()

        for (rawItem in rawList) {
            // 2. Safe Cast to Map
            val item = rawItem as? Map<*, *> ?: continue

            itemsList.add(
                FoodItem(
                    // Look for 'foodId' first, fallback to 'id' just in case
                    id = (item["foodId"] ?: item["id"]) as? String ?: "",

                    // Look for 'imageUrl' as defined in your model
                    imgUrl = (item["imageUrl"] ?: item["imgUrl"]) as? String,

                    name = item["name"] as? String ?: "Unknown",
                    originalPrice = (item["originalPrice"] as? Number)?.toInt() ?: 0,
                    offerPrice = (item["offerPrice"] as? Number)?.toInt(),
                    quantity = (item["quantity"] as? Number)?.toInt() ?: 0,
                    weight = item["weight"] as? String ?: "",
                    category = item["category"] as? String ?: "",
                    totalAmount = (item["totalAmount"] as? Number)?.toInt()
                )
            )
        }
        return itemsList
    }
}