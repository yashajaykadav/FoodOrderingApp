package com.example.foodorderingapp.admin.activity

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.foodorderingapp.R
import com.google.firebase.firestore.FirebaseFirestore

class AddItemActivity : AppCompatActivity() {

    private lateinit var etFoodName: EditText
    private lateinit var etFoodPrice: EditText
    private lateinit var etFoodQuantity: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerWeight: Spinner
    private lateinit var btnAddFood: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_add_item)

        etFoodName = findViewById(R.id.etFoodName)
        etFoodPrice = findViewById(R.id.etFoodPrice)
        etFoodQuantity = findViewById(R.id.etFoodQuantity)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerWeight = findViewById(R.id.spinnerWeight)
        btnAddFood = findViewById(R.id.btnAddFood)

        // Setup Spinner (Dropdown) for Categories
        val categories = arrayOf("Namkeen", "Farsan", "Drinks", "Sweets")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = categoryAdapter

        // Setup Spinner (Dropdown) for Weights
        val weights = arrayOf("250g", "500g", "1kg", "2kg")
        val weightAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, weights)
        spinnerWeight.adapter = weightAdapter

        btnAddFood.setOnClickListener {
            addFoodItem()
        }
    }

    private fun addFoodItem() {
        val name = etFoodName.text.toString().trim()
        val price = etFoodPrice.text.toString().trim().toDoubleOrNull()
        val stock = etFoodQuantity.text.toString().trim().toIntOrNull()
        val category = spinnerCategory.selectedItem.toString()
        val weight = spinnerWeight.selectedItem.toString()

        if (name.isEmpty()) {
            etFoodName.error = "Food name is required"
            return
        }
        if (price == null || price <= 0) {
            etFoodPrice.error = "Enter a valid price"
            return
        }
        if (stock == null || stock <= 0) {
            etFoodQuantity.error = "Enter a valid quantity"
            return
        }

        btnAddFood.isEnabled = false

        val foodId = "${name}_${weight}"

        val foodItem = hashMapOf(
            "name" to name,
            "price" to price,
            "stock" to stock,
            "category" to category,
            "weight" to weight
        )

        db.collection("foods").document(foodId)
            .set(foodItem)
            .addOnSuccessListener {
                Toast.makeText(this, "$name ($weight) added", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                btnAddFood.isEnabled = true
            }
    }
}
