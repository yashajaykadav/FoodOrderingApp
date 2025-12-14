// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.model.FoodItem
import com.foodordering.krishnafoods.databinding.AdminItemOptionBinding
import com.foodordering.krishnafoods.admin.util.bindPriceView
import com.foodordering.krishnafoods.admin.util.loadUrl

class FoodItemAdapter(
    private val onItemClick: (FoodItem) -> Unit
) : ListAdapter<FoodItem, FoodItemAdapter.FoodViewHolder>(FoodItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = AdminItemOptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    class FoodViewHolder(private val binding: AdminItemOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(food: FoodItem) {
            binding.apply {
                tvFoodName.text = food.name
                tvFoodCategory.text = food.category
                tvFoodWeight.text = root.context.getString(R.string.weight_format, food.weight)

                // Use the modular extensions
                ivFoodImage.loadUrl(food.imgUrl)
                bindPriceView(tvOriginalPrice, tvOfferPrice, food.originalPrice, food.offerPrice)
            }
        }
    }
}

// DiffCallback remains the same (it is already efficient)
class FoodItemDiffCallback : DiffUtil.ItemCallback<FoodItem>() {
    override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem) = oldItem == newItem
}