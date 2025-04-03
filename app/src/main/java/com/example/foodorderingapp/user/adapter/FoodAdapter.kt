package com.example.foodorderingapp.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.foodorderingapp.R
import com.example.foodorderingapp.user.viewmodel.FoodItem

class FoodAdapter(
    private var foodList: MutableList<FoodItem>,
    private val onAddToCartClick: (FoodItem) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    inner class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImage: ImageView = view.findViewById(R.id.foodImage)
        val foodName: TextView = view.findViewById(R.id.foodName)
        val foodCategory: TextView = view.findViewById(R.id.foodCategory)
//        val foodDescription: TextView = view.findViewById(R.id.foodDescription)
        val foodPrice: TextView = view.findViewById(R.id.foodPrice)
        val foodWeight: TextView = view.findViewById(R.id.foodWeight)
        val btnAddToCart: Button = view.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodList[position]

        // Set food details
        holder.foodName.text = foodItem.name
        holder.foodCategory.text = "Category: ${foodItem.category}"
//        holder.foodDescription.text = foodItem.description
        holder.foodPrice.text = "₹${foodItem.price}"
        holder.foodWeight.text = "Weight: ${foodItem.weight}"

        // ✅ Load images using Glide
        Glide.with(holder.itemView.context)
            .load(foodItem.imageUrl) // Ensure this is a valid URL
            .apply(RequestOptions().placeholder(R.drawable.default_img).error(R.drawable.default_img))
            .into(holder.foodImage)

        // ✅ Add to cart button click
        holder.btnAddToCart.setOnClickListener {
            onAddToCartClick(foodItem)
        }
    }

    override fun getItemCount() = foodList.size

    // ✅ Function to update the list dynamically
    fun updateList(newList: List<FoodItem>) {
        foodList.clear()
        foodList.addAll(newList)
        notifyDataSetChanged()
    }
}
