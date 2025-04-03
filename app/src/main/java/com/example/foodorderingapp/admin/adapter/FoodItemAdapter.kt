package com.example.foodorderingapp.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.user.viewmodel.FoodItem

class FoodItemAdapter(
    private val foodList: MutableList<FoodItem>,
    private val onItemClick: (FoodItem) -> Unit = {}
) : RecyclerView.Adapter<FoodItemAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.admin_item_option, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]

        holder.tvFoodName.text = food.name
        holder.tvFoodPrice.text = "Price: ₹${food.price}"
        holder.tvFoodCategory.text = "Category: ${food.category}"
        holder.tvFoodWeight.text = "Weight: ${food.weight}"
        holder.tvFoodQuantity.text = "Stock: ${food.stock}"

        holder.itemView.setOnClickListener {
            onItemClick(food)
        }
    }

    override fun getItemCount(): Int = foodList.size

    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFoodName: TextView = view.findViewById(R.id.tvFoodName)
        val tvFoodPrice: TextView = view.findViewById(R.id.tvFoodPrice)
        val tvFoodCategory: TextView = view.findViewById(R.id.tvFoodCategory)
        val tvFoodWeight: TextView = view.findViewById(R.id.tvFoodWeight)
        val tvFoodQuantity: TextView = view.findViewById(R.id.tvFoodQuantity)
    }
}