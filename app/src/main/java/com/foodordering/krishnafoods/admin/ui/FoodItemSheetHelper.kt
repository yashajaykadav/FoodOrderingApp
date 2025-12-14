package com.foodordering.krishnafoods.admin.ui

// Author: Yash Kadav
// Email: yashkadav52@gmail.com

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.foodordering.krishnafoods.admin.model.FoodItem
import com.foodordering.krishnafoods.databinding.BottomSheetFoodDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FoodItemSheetHelper(private val context: Context) {

    fun showDetails(
        foodItem: FoodItem,
        onUpdate: (id: String, orig: Int, offer: Int?) -> Unit,
        onDelete: (id: String) -> Unit
    ) {
        val dialog = BottomSheetDialog(context)
        val binding = BottomSheetFoodDetailsBinding.inflate(LayoutInflater.from(context))

        binding.apply {
            tvFoodName.text = foodItem.name
            etOriginalPrice.setText(foodItem.originalPrice.toString())
            etOfferPrice.setText(foodItem.offerPrice?.toString() ?: "")
            etFoodWeight.setText(foodItem.weight)
            etFoodCategory.setText(foodItem.category)

            // Read-only fields
            etFoodWeight.isEnabled = false
            etFoodCategory.isEnabled = false

            btnClose.setOnClickListener { dialog.dismiss() }

            btnDeleteFood.setOnClickListener {
                confirmDelete(foodItem.name) {
                    onDelete(foodItem.id)
                    dialog.dismiss()
                }
            }

            btnUpdateFood.setOnClickListener {
                val newOriginal = etOriginalPrice.text.toString().toIntOrNull()
                val newOffer = etOfferPrice.text.toString().toIntOrNull()

                if (validateInput(newOriginal, newOffer)) {
                    onUpdate(foodItem.id, newOriginal!!, newOffer)
                    dialog.dismiss()
                }
            }
        }
        dialog.setContentView(binding.root)
        dialog.show()
    }

    private fun confirmDelete(name: String, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete '$name'?")
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun validateInput(original: Int?, offer: Int?): Boolean {
        if (original == null || original <= 0) {
            Toast.makeText(context, "Original price must be positive", Toast.LENGTH_SHORT).show()
            return false
        }
        if (offer != null && offer >= original) {
            Toast.makeText(context, "Offer price must be less than original", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}