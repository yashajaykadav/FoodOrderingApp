// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.user.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.UserItemOrderSummaryBinding
import com.foodordering.krishnafoods.user.viewmodel.FoodItem

class OrderSummaryAdapter(private val cartItems: List<FoodItem>) :
    RecyclerView.Adapter<OrderSummaryAdapter.OrderSummaryViewHolder>() {

    inner class OrderSummaryViewHolder(val binding: UserItemOrderSummaryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderSummaryViewHolder {
        val binding = UserItemOrderSummaryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderSummaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderSummaryViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = cartItems[position]
        val binding = holder.binding

        // Bind basic info
        binding.foodName.text = item.name
        binding.quantityText.text = context.getString(R.string.quantity_format, item.quantity)
        binding.weightText.text = context.getString(R.string.weight_format, item.weight)

        // Calculate Totals
        val totalOriginal = item.originalPrice * item.quantity
        val totalOffer = item.offerPrice * item.quantity

        // Price Display Logic
        if (item.offerPrice < item.originalPrice) {
            // Offer exists: Show original (struck) and offer (highlighted)
            binding.foodOriginalPrice.apply {
                visibility = View.VISIBLE
                text = context.getString(R.string.currency_format, totalOriginal)
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            binding.foodOfferPrice.apply {
                text = context.getString(R.string.currency_format, totalOffer)
                setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            }
        } else {
            // No offer: Hide original view, show standard price in offer view
            binding.foodOriginalPrice.visibility = View.GONE
            binding.foodOfferPrice.apply {
                text = context.getString(R.string.currency_format, totalOriginal)
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            }
        }

        // Load image
        Glide.with(context)
            .load(item.imageUrl)
            .placeholder(R.drawable.default_img)
            .error(R.drawable.default_img)
            .into(binding.foodImage)
    }

    override fun getItemCount(): Int = cartItems.size
}