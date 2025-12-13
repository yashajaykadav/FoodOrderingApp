// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.user.adapter

import android.content.Context
import android.graphics.Paint
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.UserItemCartBinding
import com.foodordering.krishnafoods.user.manager.CartManager
import com.foodordering.krishnafoods.user.util.CartDiffCallback
import com.foodordering.krishnafoods.user.viewmodel.FoodItem

class CartAdapter(
    private var cartItems: MutableList<FoodItem>,
    private val onRemoveClick: () -> Unit,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // ViewHolder now takes the generated Binding class
    inner class CartViewHolder(val binding: UserItemCartBinding) : RecyclerView.ViewHolder(binding.root) {

        fun clearListeners() {
            (binding.quantityText.tag as? TextWatcher)?.let {
                binding.quantityText.removeTextChangedListener(it)
            }
            binding.quantityText.onFocusChangeListener = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = UserItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val foodItem = cartItems[position]
        val binding = holder.binding
        val context = binding.root.context

        holder.clearListeners()

        // Bind data using binding object
        binding.foodName.text = foodItem.name
        binding.foodWeight.text = context.getString(R.string.label_weight, foodItem.weight)
        binding.foodCategory.text = context.getString(R.string.label_category, foodItem.category)

        Glide.with(context)
            .load(foodItem.imageUrl)
            .placeholder(R.drawable.default_img)
            .into(binding.foodImage)

        updatePriceViews(binding, foodItem)

        binding.quantityText.setText(foodItem.quantity.toString())
        binding.quantityText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        binding.quantityText.filters = arrayOf(InputFilter.LengthFilter(2))

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val adapterPosition = holder.adapterPosition
                if (adapterPosition == RecyclerView.NO_POSITION) return

                val item = cartItems[adapterPosition]
                val input = s?.toString() ?: "1"
                var quantityChanged = false

                // Prevent recursive updates or empty states
                if (input.isEmpty()) {
                    // Optional: handle empty state or wait for focus loss
                    return
                }

                if (input.length > 1 && input.startsWith("0")) {
                    s?.replace(0, input.length, input.substring(1))
                    return
                }

                val newQuantity = input.toIntOrNull() ?: 1

                when {
                    newQuantity < 1 -> {
                        binding.quantityText.setText("1")
                        if (item.quantity != 1) {
                            item.quantity = 1
                            quantityChanged = true
                        }
                    }
                    newQuantity > 99 -> {
                        binding.quantityText.setText("99")
                        if (item.quantity != 99) {
                            item.quantity = 99
                            quantityChanged = true
                        }
                        Toast.makeText(context, R.string.msg_max_quantity, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        if (newQuantity != item.quantity) {
                            item.quantity = newQuantity
                            quantityChanged = true
                        }
                    }
                }

                if (quantityChanged) {
                    CartManager.updateItemQuantity(context, item.id, item.quantity)
                    updatePriceViews(binding, item)
                    onQuantityChanged()
                }
            }
        }

        binding.quantityText.addTextChangedListener(textWatcher)
        binding.quantityText.tag = textWatcher

        binding.quantityText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.quantityText.text.toString().isEmpty()) {
                val adapterPosition = holder.adapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val item = cartItems[adapterPosition]
                    if (item.quantity != 1) {
                        item.quantity = 1
                        binding.quantityText.setText("1")
                        CartManager.updateItemQuantity(context, item.id, item.quantity)
                        updatePriceViews(binding, item)
                        onQuantityChanged()
                    }
                }
            }
        }

        binding.btnIncrease.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            val item = cartItems[adapterPosition]

            if (item.quantity < 99) {
                item.quantity++
                binding.quantityText.setText(item.quantity.toString())
                CartManager.updateItemQuantity(context, item.id, item.quantity)
                updatePriceViews(binding, item)
                onQuantityChanged()
                animateQuantityChange(binding.quantityText)
            } else {
                Toast.makeText(context, R.string.msg_max_quantity_reached, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDecrease.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            val item = cartItems[adapterPosition]

            if (item.quantity > 1) {
                item.quantity--
                binding.quantityText.setText(item.quantity.toString())
                CartManager.updateItemQuantity(context, item.id, item.quantity)
                updatePriceViews(binding, item)
                onQuantityChanged()
                animateQuantityChange(binding.quantityText)
            } else {
                removeItem(context, adapterPosition)
            }
        }

        binding.btnRemove.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                removeItem(context, adapterPosition)
            }
        }
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateCartItems(newItems: MutableList<FoodItem>) {
        val diffCallback = CartDiffCallback(cartItems, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        cartItems.clear()
        cartItems.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    fun removeItem(context: Context, position: Int) {
        if (position in cartItems.indices) {
            val removedItem = cartItems.removeAt(position)
            CartManager.removeFromCart(context, removedItem)
            notifyItemRemoved(position)
            onRemoveClick()
        }
    }

    fun getTotalAmount(): Int = cartItems.sumOf {
        (if (it.offerPrice < it.originalPrice) it.offerPrice else it.originalPrice) * it.quantity
    }

    private fun animateQuantityChange(view: View) {
        view.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    // Pass Binding instead of ViewHolder to helper methods
    private fun updatePriceViews(binding: UserItemCartBinding, item: FoodItem) {
        val context = binding.root.context
        if (item.offerPrice < item.originalPrice) {
            binding.foodOriginalPrice.visibility = View.VISIBLE
            binding.foodOriginalPrice.text = context.getString(R.string.currency_format, item.originalPrice * item.quantity)
            binding.foodOriginalPrice.paintFlags = binding.foodOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            binding.foodOfferPrice.text = context.getString(R.string.currency_format, item.offerPrice * item.quantity)
            binding.foodOfferPrice.setTextColor(context.getColor(R.color.colorPrimary))
        } else {
            binding.foodOriginalPrice.visibility = View.GONE
            binding.foodOfferPrice.text = context.getString(R.string.currency_format, item.originalPrice * item.quantity)
            binding.foodOfferPrice.setTextColor(context.getColor(R.color.gray))
        }
    }
}