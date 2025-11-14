package com.foodordering.krishnafoods.user.util

import androidx.recyclerview.widget.DiffUtil
import com.foodordering.krishnafoods.user.viewmodel.OrderItem

class OrderDiffCallback(
    private val oldList: List<OrderItem>,
    private val newList: List<OrderItem>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return oldList[oldPosition].id == newList[newPosition].id // ✅ Check by Firestore ID
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return oldList[oldPosition] == newList[newPosition] // ✅ Check by data comparison
    }
}
