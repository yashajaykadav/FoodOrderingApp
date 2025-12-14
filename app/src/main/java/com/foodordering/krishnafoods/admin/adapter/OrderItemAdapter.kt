// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.model.FoodItem
import com.foodordering.krishnafoods.databinding.ItemOrderItemBinding

// Optimization: Use the shared 'FoodItem' model instead of creating a new 'OrderItem' class
class OrderItemAdapter(private val items: List<FoodItem>) :
    RecyclerView.Adapter<OrderItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FoodItem) {
            binding.apply {
                tvItemName.text = item.name
                tvQuantity.text = root.context.getString(R.string.quantity_format, item.quantity)

                // Logic: Handle Nullable Offer Price & Calculate Row Total
                val unitPrice = item.offerPrice ?: item.originalPrice
                val rowTotal = unitPrice * item.quantity

                tvPrice.text = root.context.getString(R.string.price_format, rowTotal)
            }
        }
    }
}