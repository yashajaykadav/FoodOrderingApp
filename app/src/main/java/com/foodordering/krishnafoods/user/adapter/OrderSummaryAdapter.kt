package com.foodordering.krishnafoods.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.user.viewmodel.FoodItem

class OrderSummaryAdapter(private val cartItems: List<FoodItem>) :
    RecyclerView.Adapter<OrderSummaryAdapter.OrderSummaryViewHolder>() {

    inner class OrderSummaryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImage: ImageView = view.findViewById(R.id.foodImage)
        val foodName: TextView = view.findViewById(R.id.foodName)
        val originalPrice: TextView = view.findViewById(R.id.foodOriginalPrice)
        val offerPrice: TextView = view.findViewById(R.id.foodOfferPrice)
        val quantityText: TextView = view.findViewById(R.id.quantityText)
        val weightText: TextView = view.findViewById(R.id.weightText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderSummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item_order_summary, parent, false)
        return OrderSummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderSummaryViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = cartItems[position]
        val quantity = item.quantity

        // Set basic item info
        holder.foodName.text = item.name
        holder.quantityText.text = context.getString(R.string.quantity_format, quantity)
        holder.weightText.text = context.getString(R.string.weight_format, item.weight)

        // Calculate total prices
        val totalOriginal = item.originalPrice * quantity
        val totalOffer = item.offerPrice?.let { it * quantity }

        // Apply consistent pricing display logic
        PriceFormatter.formatPrices(
            originalPrice = totalOriginal,
            offerPrice = totalOffer,
            originalPriceView = holder.originalPrice,
            offerPriceView = holder.offerPrice,
            context = context
        )

        // Load image
        Glide.with(context)
            .load(item.imageUrl)
            .placeholder(R.drawable.default_img)
            .error(R.drawable.default_img)
            .into(holder.foodImage)
    }

    override fun getItemCount(): Int = cartItems.size
}