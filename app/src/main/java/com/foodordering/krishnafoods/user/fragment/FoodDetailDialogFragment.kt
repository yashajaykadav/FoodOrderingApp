// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.user.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.FragmentFoodDetailDialogBinding
import com.foodordering.krishnafoods.user.manager.CartManager
import com.foodordering.krishnafoods.user.util.vibrateDevice
import com.foodordering.krishnafoods.user.viewmodel.FoodItem

class FoodDetailDialogFragment(
    private val foodItem: FoodItem,
    private val onCartUpdated: () -> Unit
) : DialogFragment() {

    companion object {
        private const val KEY_QUANTITY = "quantity"
        private const val MAX_QUANTITY = 99
        private const val DEBOUNCE_DELAY_MS = 300L
    }

    // View Binding property
    private var _binding: FragmentFoodDetailDialogBinding? = null
    private val binding get() = _binding!!

    private var quantity = 1
    private var lastClickTime = 0L

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        quantity = savedInstanceState?.getInt(KEY_QUANTITY, 1) ?: 1

        _binding = FragmentFoodDetailDialogBinding.inflate(LayoutInflater.from(context))

        // Initialize UI
        updateUI()
        updateQuantityUI()

        // Click listeners
        binding.plusButton.setOnClickListener { handleClick { handlePlusClick() } }
        binding.minusButton.setOnClickListener { handleClick { handleMinusClick() } }
        binding.addToCartButton.setOnClickListener { handleClick { handleAddToCart() } }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setCancelable(true)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI() {
        val context = binding.root.context

        // Basic info
        binding.foodName.text = foodItem.name.takeIf { it.isNotEmpty() } ?: "Unknown"
        binding.foodCategory.text = foodItem.category.takeIf { it.isNotEmpty() } ?: "N/A"
        binding.foodWeight.text = foodItem.weight.takeIf { it.isNotEmpty() } ?: "N/A"
        binding.foodDescription.text = foodItem.description.takeIf { it.isNotEmpty() } ?: "No description"

        // Price logic
        if (foodItem.offerPrice < foodItem.originalPrice) {
            binding.foodOriginalPrice.apply {
                text = context.getString(R.string.currency_format, foodItem.originalPrice)
                paintFlags = paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                visibility = View.VISIBLE
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }
            binding.foodOfferPrice.apply {
                text = context.getString(R.string.currency_format, foodItem.offerPrice)
                visibility = View.VISIBLE
                setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
            }
        } else {
            binding.foodOriginalPrice.visibility = View.GONE
            binding.foodOfferPrice.apply {
                text = context.getString(R.string.currency_format, foodItem.originalPrice)
                visibility = View.VISIBLE
                setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
            }
        }

        // Load image
        Glide.with(context)
            .load(foodItem.imageUrl)
            .placeholder(R.drawable.default_img)
            .error(R.drawable.ic_placeholder_image)
            .into(binding.foodImage)
    }

    private fun updateQuantityUI() {
        if (_binding == null) return
        binding.quantityText.text = quantity.toString()
        // Ensure button text is refreshed (optional, if text changes dynamically)
        binding.addToCartButton.text = getString(R.string.add_to_cart)
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

        val itemToAdd = foodItem.copy(quantity = quantity)

        try {
            // Updated: Removed manual saveCart call as addToCart handles it
            CartManager.addToCart(requireContext(), itemToAdd)
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
            requireContext().vibrateDevice(50)
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