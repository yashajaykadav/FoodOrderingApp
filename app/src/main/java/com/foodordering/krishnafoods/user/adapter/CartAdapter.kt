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
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.user.manager.CartManager
import com.foodordering.krishnafoods.user.util.CartDiffCallback
import com.foodordering.krishnafoods.user.viewmodel.FoodItem

class CartAdapter(
    private var cartItems: MutableList<FoodItem>,
    private val onRemoveClick: () -> Unit,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodImage: ImageView = view.findViewById(R.id.foodImage)
        val foodName: TextView = view.findViewById(R.id.foodName)
        val foodOriginalPrice: TextView = view.findViewById(R.id.foodOriginalPrice)
        val foodOfferPrice: TextView = view.findViewById(R.id.foodOfferPrice)
        val foodWeight: TextView = view.findViewById(R.id.foodWeight)
        val foodCategory: TextView = view.findViewById(R.id.foodCategory)
        val quantityText: EditText = view.findViewById(R.id.quantityText)
        val btnIncrease: ImageButton = view.findViewById(R.id.btnIncrease)
        val btnDecrease: ImageButton = view.findViewById(R.id.btnDecrease)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemove)

        fun clearListeners() {
            (quantityText.tag as? TextWatcher)?.let {
                quantityText.removeTextChangedListener(it)
            }
            quantityText.onFocusChangeListener = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val foodItem = cartItems[position]

        holder.clearListeners()

        holder.foodName.text = foodItem.name
        holder.foodWeight.text = holder.itemView.context.getString(R.string.label_weight, foodItem.weight)
        holder.foodCategory.text = holder.itemView.context.getString(R.string.label_category, foodItem.category)

        Glide.with(holder.itemView.context)
            .load(foodItem.imageUrl)
            .placeholder(R.drawable.default_img)
            .into(holder.foodImage)

        updatePriceViews(holder, foodItem)

        holder.quantityText.setText(foodItem.quantity.toString())
        holder.quantityText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        holder.quantityText.filters = arrayOf(InputFilter.LengthFilter(2))

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val adapterPosition = holder.adapterPosition
                if (adapterPosition == RecyclerView.NO_POSITION) return

                val item = cartItems[adapterPosition]
                val input = s?.toString() ?: "1"
                var quantityChanged = false

                if (input.isEmpty()) {
                    holder.quantityText.setText("1")
                    return
                }

                if (input.length > 1 && input.startsWith("0")) {
                    s?.replace(0, input.length, input.substring(1))
                    return
                }

                val newQuantity = input.toIntOrNull() ?: 1

                when {
                    newQuantity < 1 -> {
                        holder.quantityText.setText("1")
                        if (item.quantity != 1) {
                            item.quantity = 1
                            quantityChanged = true
                        }
                    }
                    newQuantity > 99 -> {
                        holder.quantityText.setText("99")
                        if (item.quantity != 99) {
                            item.quantity = 99
                            quantityChanged = true
                        }
                        Toast.makeText(holder.itemView.context, R.string.msg_max_quantity, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        if (newQuantity != item.quantity) {
                            item.quantity = newQuantity
                            quantityChanged = true
                        }
                    }
                }

                if (quantityChanged) {
                    CartManager.updateItemQuantity(holder.itemView.context, item.id, item.quantity)
                    updatePriceViews(holder, item)
                    onQuantityChanged()
                }
            }
        }

        holder.quantityText.addTextChangedListener(textWatcher)
        holder.quantityText.tag = textWatcher

        holder.quantityText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && holder.quantityText.text.toString().isEmpty()) {
                val adapterPosition = holder.adapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val item = cartItems[adapterPosition]
                    if (item.quantity != 1) {
                        item.quantity = 1
                        holder.quantityText.setText("1")
                        CartManager.updateItemQuantity(holder.itemView.context, item.id, item.quantity)
                        updatePriceViews(holder, item)
                        onQuantityChanged()
                    }
                }
            }
        }

        holder.btnIncrease.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            val item = cartItems[adapterPosition]

            if (item.quantity < 99) {
                item.quantity++
                holder.quantityText.setText(item.quantity.toString())
                CartManager.updateItemQuantity(holder.itemView.context, item.id, item.quantity)
                updatePriceViews(holder, item)
                onQuantityChanged()
                animateQuantityChange(holder.quantityText)
            } else {
                Toast.makeText(holder.itemView.context, R.string.msg_max_quantity_reached, Toast.LENGTH_SHORT).show()
            }
        }

        holder.btnDecrease.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            val item = cartItems[adapterPosition]

            if (item.quantity > 1) {
                item.quantity--
                holder.quantityText.setText(item.quantity.toString())
                CartManager.updateItemQuantity(holder.itemView.context, item.id, item.quantity)
                updatePriceViews(holder, item)
                onQuantityChanged()
                animateQuantityChange(holder.quantityText)
            } else {
                removeItem(holder.itemView.context, adapterPosition)
            }
        }

        holder.btnRemove.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                removeItem(holder.itemView.context, adapterPosition)
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

    // ✅ Restored original function
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

    // ✅ Restored original function
    private fun updatePriceViews(holder: CartViewHolder, item: FoodItem) {
        val context = holder.itemView.context
        if (item.offerPrice < item.originalPrice) {
            holder.foodOriginalPrice.visibility = View.VISIBLE
            holder.foodOriginalPrice.text = context.getString(R.string.currency_format, item.originalPrice * item.quantity)
            holder.foodOriginalPrice.paintFlags = holder.foodOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            holder.foodOfferPrice.text = context.getString(R.string.currency_format, item.offerPrice * item.quantity)
            holder.foodOfferPrice.setTextColor(context.getColor(R.color.colorPrimary))
        } else {
            holder.foodOriginalPrice.visibility = View.GONE
            holder.foodOfferPrice.text = context.getString(R.string.currency_format, item.originalPrice * item.quantity)
            holder.foodOfferPrice.setTextColor(context.getColor(R.color.gray))
        }
    }
}