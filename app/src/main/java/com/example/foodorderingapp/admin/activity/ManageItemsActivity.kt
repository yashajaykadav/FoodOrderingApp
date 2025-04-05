package com.example.foodorderingapp.admin.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.admin.adapter.FoodItemAdapter
import com.example.foodorderingapp.user.viewmodel.FoodItem
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class ManageItemsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FoodItemAdapter
    private val foodList = mutableListOf<FoodItem>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_manage_items)

        recyclerView = findViewById(R.id.recyclerViewFoodItems)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = FoodItemAdapter(foodList) { foodItem ->
            showFoodDetailsBottomSheet(foodItem)
        }
        recyclerView.adapter = adapter

        fetchFoodItemsRealtime()
    }

    private fun fetchFoodItemsRealtime() {
        db.collection("foods").addSnapshotListener { documents, error ->
            if (error != null) {
                Toast.makeText(this, "Error fetching food items", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            foodList.clear()
            if (documents != null) {
                for (document in documents) {
                    val food = document.toObject<FoodItem>()
                    food.id = document.id
                    foodList.add(food)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showFoodDetailsBottomSheet(foodItem: FoodItem) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_food_details, null)

        // Bind views
        val tvName = view.findViewById<TextView>(R.id.tvFoodName)
        val etPrice = view.findViewById<EditText>(R.id.etFoodPrice)
        val etStock = view.findViewById<EditText>(R.id.etFoodStock)
        val etQuantity = view.findViewById<EditText>(R.id.etFoodQuantity)
        val etWeight = view.findViewById<EditText>(R.id.etFoodWeight)
        val etCategory = view.findViewById<EditText>(R.id.etFoodCategory)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdateFood)
        val btnClose = view.findViewById<Button>(R.id.btnClose)

        // Set initial values
        tvName.text = foodItem.name
        etPrice.setText(foodItem.price.toString())
        etStock.setText(foodItem.stock.toString())
        etQuantity.setText(foodItem.quantity.toString())
        etWeight.setText(foodItem.weight)
        etCategory.setText(foodItem.category)

        // Update button click
        btnUpdate.setOnClickListener {
            val newPrice = etPrice.text.toString().toIntOrNull() ?: foodItem.price
            val newStock = etStock.text.toString().toIntOrNull() ?: foodItem.stock
            val newQuantity = etQuantity.text.toString().toIntOrNull() ?: foodItem.quantity
            val newWeight = etWeight.text.toString()
            val newCategory = etCategory.text.toString()

            updateFoodItem(foodItem.id, newPrice, newStock, newQuantity, newWeight, newCategory)
            bottomSheetDialog.dismiss()
        }

        // Close button click
        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun updateFoodItem(foodId: String, price: Int, stock: Int, quantity: Int, weight: String, category: String) {
        val updates = mapOf(
            "price" to price,
            "stock" to stock,
            "quantity" to quantity,
            "weight" to weight,
            "category" to category
        )

        db.collection("foods").document(foodId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Food item updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating food: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun openAddItemActivity(view: View) {
        startActivity(Intent(this, AddItemActivity::class.java))
    }
}