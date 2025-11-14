package com.foodordering.krishnafoods.user.util

import androidx.recyclerview.widget.DiffUtil
import com.foodordering.krishnafoods.user.viewmodel.FoodItem

class CartDiffCallback(
    private val oldList: List<FoodItem>,
    private val newList: List<FoodItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // If FoodItem has unique ID, compare that. Otherwise compare name.
        return oldList[oldItemPosition].name == newList[newItemPosition].name
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
