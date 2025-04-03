package com.example.foodorderingapp.user.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodorderingapp.R
import com.example.foodorderingapp.user.manager.CartManager
import com.example.foodorderingapp.user.viewmodel.FoodItem

class CartAdapter(
    private var cartItems: MutableList<FoodItem>,
    private val onRemoveClick: () -> Unit,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImage: ImageView = view.findViewById(R.id.foodImage)
        val foodName: TextView = view.findViewById(R.id.foodName)
        val foodPrice: TextView = view.findViewById(R.id.foodPrice)
        val foodWeight: TextView = view.findViewById(R.id.foodWeight)
        val foodCategory: TextView = view.findViewById(R.id.foodCategory)
        val quantityText: EditText = view.findViewById(R.id.quantityText)
        val btnIncrease: ImageButton = view.findViewById(R.id.btnIncrease)
        val btnDecrease: ImageButton = view.findViewById(R.id.btnDecrease)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val foodItem = cartItems[position]

        // ✅ Bind Data
        holder.foodName.text = foodItem.name
        holder.foodPrice.text = "₹${foodItem.price * foodItem.quantity}"
        holder.foodWeight.text = "Weight: ${foodItem.weight}"
        holder.foodCategory.text = "Category: ${foodItem.category}"

        Glide.with(holder.itemView.context)
            .load(foodItem.imageUrl)
            .placeholder(R.drawable.default_img)
            .into(holder.foodImage)

        // ✅ Prevent Duplicate TextWatcher Issues
        (holder.quantityText.tag as? TextWatcher)?.let {
            holder.quantityText.removeTextChangedListener(it)
        }

        // ✅ Set Quantity Properly
        holder.quantityText.setText(foodItem.quantity.toString())

        // ✅ Handle Manual Quantity Input
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val newQuantity = s?.toString()?.toIntOrNull() ?: 1
                if (newQuantity < 1) {
                    holder.quantityText.setText("1")
                    foodItem.quantity = 1
                } else {
                    foodItem.quantity = newQuantity
                }
                holder.foodPrice.text = "₹${foodItem.price * foodItem.quantity}"
                onQuantityChanged()
            }
        }

        holder.quantityText.addTextChangedListener(textWatcher)
        holder.quantityText.tag = textWatcher

        // ✅ Handle Increase Button
        holder.btnIncrease.setOnClickListener {
            foodItem.quantity++
            holder.quantityText.setText(foodItem.quantity.toString())
            holder.foodPrice.text = "₹${foodItem.price * foodItem.quantity}"
            onQuantityChanged()
        }

        // ✅ Handle Decrease Button
        holder.btnDecrease.setOnClickListener {
            if (foodItem.quantity > 1) {
                foodItem.quantity--
                holder.quantityText.setText(foodItem.quantity.toString())
                holder.foodPrice.text = "₹${foodItem.price * foodItem.quantity}"
                onQuantityChanged()
            } else {
                removeItem(position)
            }
        }

        // ✅ Handle Remove Button
        holder.btnRemove.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val removedItem = cartItems[pos]
                CartManager.removeFromCart(removedItem)
                cartItems.removeAt(pos)
                notifyItemRemoved(pos)
                notifyItemRangeChanged(pos, cartItems.size)
                onRemoveClick()
            }
        }
    }

    override fun getItemCount() = cartItems.size

    // ✅ Update Cart Items
    fun updateCartItems(newItems: MutableList<FoodItem>) {
        cartItems.clear()
        cartItems.addAll(newItems)
        notifyDataSetChanged()
    }

    // ✅ Remove Item from Cart
    fun removeItem(position: Int) {
        if (position in cartItems.indices) {
            cartItems.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, cartItems.size)
            onRemoveClick()
        }
    }

    // ✅ Get Total Price
    fun getTotalAmount(): Int = cartItems.sumOf { it.price * it.quantity }

    // ✅ Get Item at Position
    fun getItemAt(position: Int): FoodItem? = cartItems.getOrNull(position)
}
