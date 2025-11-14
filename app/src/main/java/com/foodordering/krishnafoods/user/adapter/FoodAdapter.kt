package com.foodordering.krishnafoods.user.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.user.viewmodel.FoodItem

class FoodAdapter(
    private var foodList: MutableList<FoodItem>,
    private val onItemClick: (FoodItem) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    inner class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImage: ImageView = view.findViewById(R.id.foodImage)
        val foodName: TextView = view.findViewById(R.id.foodName)
        val foodOriginalPrice: TextView = view.findViewById(R.id.foodOriginalPrice)
        val foodOfferPrice: TextView = view.findViewById(R.id.foodOfferPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodList[position]
        val context = holder.itemView.context

        // --- Name ---
        holder.foodName.text = foodItem.name

        // --- Price logic ---
        if (foodItem.offerPrice < foodItem.originalPrice) {
            holder.foodOriginalPrice.apply {
                text = "₹${foodItem.originalPrice}"
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                visibility = View.VISIBLE
            }

            holder.foodOfferPrice.apply {
                text = "₹${foodItem.offerPrice}"
                setTextColor(context.getColor(R.color.colorAccent))
            }
        } else {
            holder.foodOriginalPrice.visibility = View.GONE
            holder.foodOfferPrice.apply {
                text = "₹${foodItem.originalPrice}"
                setTextColor(context.getColor(R.color.gray))
            }
        }

        // --- Image ---
        if (!foodItem.imageUrl.isNullOrBlank()) {
            Glide.with(context)
                .load(foodItem.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.default_img)
                        .error(R.drawable.ic_placeholder_image)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .into(holder.foodImage)
        } else {
            holder.foodImage.setImageResource(R.drawable.default_img)
        }

        // --- Click ---
        holder.itemView.setOnClickListener { onItemClick(foodItem) }
    }

    override fun getItemCount(): Int = foodList.size

    fun updateList(newList: List<FoodItem>) {
        foodList.clear()
        foodList.addAll(newList)
        notifyDataSetChanged()
    }
}
