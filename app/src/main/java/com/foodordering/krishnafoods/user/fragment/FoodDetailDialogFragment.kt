package com.foodordering.krishnafoods.user.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.user.manager.CartManager
import com.foodordering.krishnafoods.user.viewmodel.FoodItem
import com.google.android.material.button.MaterialButton

class FoodDetailDialogFragment(
    private val foodItem: FoodItem,
    private val onCartUpdated: () -> Unit
) : DialogFragment() {

    companion object {
        private const val KEY_QUANTITY = "quantity"
        private const val MAX_QUANTITY = 99
        private const val DEBOUNCE_DELAY_MS = 300L
    }

    private var quantity = 1
    private var lastClickTime = 0L

    private lateinit var quantityText: TextView
    private lateinit var addToCartBtn: MaterialButton

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        quantity = savedInstanceState?.getInt(KEY_QUANTITY, 1) ?: 1

        val view = LayoutInflater.from(context).inflate(R.layout.fragment_food_detail_dialog, null)

        // Views
        val imageView = view.findViewById<ImageView>(R.id.foodImage)
        val nameView = view.findViewById<TextView>(R.id.foodName)
        val categoryView = view.findViewById<TextView>(R.id.foodCategory)
        val weightView = view.findViewById<TextView>(R.id.foodWeight)
        val descView = view.findViewById<TextView>(R.id.foodDescription)
        val originalPriceView = view.findViewById<TextView>(R.id.foodOriginalPrice)
        val offerPriceView = view.findViewById<TextView>(R.id.foodOfferPrice)
        quantityText = view.findViewById(R.id.quantityText)
        val plusBtn = view.findViewById<ImageButton>(R.id.plusButton)
        val minusBtn = view.findViewById<ImageButton>(R.id.minusButton)
        addToCartBtn = view.findViewById(R.id.addToCartButton)

        // Initialize UI
        updateUI(
            nameView,
            categoryView,
            weightView,
            descView,
            originalPriceView,
            offerPriceView,
            imageView
        )
        updateQuantityUI()

        // Click listeners
        plusBtn.setOnClickListener { handleClick { handlePlusClick() } }
        minusBtn.setOnClickListener { handleClick { handleMinusClick() } }
        addToCartBtn.setOnClickListener { handleClick { handleAddToCart() } }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(true)
            .create()
    }

    private fun updateUI(
        nameView: TextView?,
        categoryView: TextView?,
        weightView: TextView?,
        descView: TextView?,
        originalPriceView: TextView?,
        offerPriceView: TextView?,
        imageView: ImageView?
    ) {
        // Basic info
        nameView?.text = foodItem.name.takeIf { it.isNotEmpty() } ?: "Unknown"
        categoryView?.text = foodItem.category.takeIf { it.isNotEmpty() } ?: "N/A"
        weightView?.text = foodItem.weight.takeIf { it.isNotEmpty() } ?: "N/A"
        descView?.text = foodItem.description.takeIf { it.isNotEmpty() } ?: "No description"

        // Price logic
        if (foodItem.offerPrice < foodItem.originalPrice) {
            originalPriceView?.apply {
                text = "₹${foodItem.originalPrice}"
                paintFlags = paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                visibility = View.VISIBLE
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            }
            offerPriceView?.apply {
                text = "₹${foodItem.offerPrice}"
                visibility = View.VISIBLE
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
            }
        } else {
            originalPriceView?.visibility = View.GONE
            offerPriceView?.apply {
                text = "₹${foodItem.originalPrice}"
                visibility = View.VISIBLE
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
            }
        }

        // Load image
        imageView?.let {
            context?.let { ctx ->
                Glide.with(ctx)
                    .load(foodItem.imageUrl)
                    .placeholder(R.drawable.default_img)
                    .error(R.drawable.ic_placeholder_image)
                    .into(it)
            }
        }
    }

    private fun updateQuantityUI() {
        quantityText.text = quantity.toString()
        addToCartBtn.text = getString(R.string.add_to_cart)
    }


    private fun handlePlusClick() {
        if (quantity < MAX_QUANTITY) {
            quantity++
            updateQuantityUI()
        } else {
            showToast(getString(R.string.max_quantity_reached))
        }
    }

    private fun handleMinusClick() {
        if (quantity > 1) {
            quantity--
            updateQuantityUI()
        }
    }

    private fun handleAddToCart() {
        if (foodItem.id.isBlank()) {
            showToast(getString(R.string.failed_to_add_to_cart))
            return
        }

        val currentPrice = foodItem.offerPrice
        val itemToAdd = foodItem.copy(
            originalPrice = currentPrice,
            quantity = quantity
        )

        try {
            CartManager.addToCart(requireContext(),itemToAdd)
            CartManager.saveCart(requireContext())
            onCartUpdated()
            showToast(getString(R.string.item_added_to_cart, foodItem.name, quantity))
            dismiss()
        } catch (_: Exception) {
            showToast(getString(R.string.failed_to_add_to_cart))
        }
    }


    private fun handleClick(action: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > DEBOUNCE_DELAY_MS) {
            lastClickTime = currentTime
            action()
        }
    }

    private fun showToast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_QUANTITY, quantity)
    }
}