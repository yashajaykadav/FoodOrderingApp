package com.example.foodorderingapp.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodorderingapp.R
import com.example.foodorderingapp.user.viewmodel.FoodItem

class OrderSummaryAdapter(private val cartItems: List<FoodItem>) :
    RecyclerView.Adapter<OrderSummaryAdapter.OrderSummaryViewHolder>() {

    inner class OrderSummaryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImage: ImageView = view.findViewById(R.id.foodImage)
        val foodName: TextView = view.findViewById(R.id.foodName)
        val foodPrice: TextView = view.findViewById(R.id.foodPrice)
        val quantityText: TextView = view.findViewById(R.id.quantityText)
        val weightText: TextView = view.findViewById(R.id.weightText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderSummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item_order_summary, parent, false)
        return OrderSummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderSummaryViewHolder, position: Int) {
        val foodItem = cartItems[position]
        holder.foodName.text = foodItem.name
        holder.foodPrice.text = "₹${foodItem.price * foodItem.quantity}"
        holder.quantityText.text = "Qty: ${foodItem.quantity}"
        holder.weightText.text = "Weight: ${foodItem.weight}"

        // ✅ Load image using Glide from URL
        Glide.with(holder.itemView.context)
            .load(foodItem.imageUrl)
            .placeholder(R.drawable.default_img) // default image
            .error(R.drawable.default_img) // if image fails to load
            .into(holder.foodImage)
    }

    override fun getItemCount() = cartItems.size
}
