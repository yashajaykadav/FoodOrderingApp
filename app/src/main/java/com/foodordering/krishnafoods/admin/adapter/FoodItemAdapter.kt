package com.foodordering.krishnafoods.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.model.FoodItem

class FoodItemAdapter(
    private val onItemClick: (FoodItem) -> Unit
) : ListAdapter<FoodItem, FoodItemAdapter.FoodViewHolder>(FoodItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_item_option, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = getItem(position)
        holder.bind(food)
        holder.itemView.setOnClickListener { onItemClick(food) }
    }

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivFoodImage: ImageView = itemView.findViewById(R.id.ivFoodImage)
        private val tvFoodName: TextView = itemView.findViewById(R.id.tvFoodName)
        private val tvOriginalPrice: TextView = itemView.findViewById(R.id.tvOriginalPrice)
        private val tvOfferPrice: TextView = itemView.findViewById(R.id.tvOfferPrice)
        private val tvFoodCategory: TextView = itemView.findViewById(R.id.tvFoodCategory)
        private val tvFoodWeight: TextView = itemView.findViewById(R.id.tvFoodWeight)

        fun bind(food: FoodItem) {
            // Fix 1: Handle Glide load failure by checking for a valid URL
            if (food.imgUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(food.imgUrl)
                    .placeholder(R.drawable.default_img)
                    .error(R.drawable.default_img)
                    .into(ivFoodImage)
            } else {
                ivFoodImage.setImageResource(R.drawable.default_img)
            }

            tvFoodName.text = food.name
            tvFoodCategory.text = food.category
            tvFoodWeight.text = itemView.context.getString(R.string.weight_format, food.weight)

            // Fix 2: Resolve the "Smart cast" error by using a local immutable variable
            val offerPriceValue = food.offerPrice
            if (offerPriceValue != null && offerPriceValue < food.originalPrice) {
                tvOriginalPrice.text = itemView.context.getString(
                    R.string.price_format,
                    food.originalPrice
                )
                tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                tvOriginalPrice.visibility = View.VISIBLE

                tvOfferPrice.text = itemView.context.getString(
                    R.string.price_format,
                    offerPriceValue
                )
                tvOfferPrice.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.colorAccent)
                )
            } else {
                tvOriginalPrice.visibility = View.GONE

                tvOfferPrice.text = itemView.context.getString(
                    R.string.price_format,
                    food.originalPrice
                )
                tvOfferPrice.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.text_primary)
                )
            }
        }
    }
}

class FoodItemDiffCallback : DiffUtil.ItemCallback<FoodItem>() {
    override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
        return oldItem == newItem
    }
}