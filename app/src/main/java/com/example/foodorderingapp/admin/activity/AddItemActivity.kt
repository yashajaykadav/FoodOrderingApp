package com.example.foodorderingapp.admin.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.foodorderingapp.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddItemActivity : AppCompatActivity() {

    private lateinit var etFoodName: EditText
    private lateinit var etFoodPrice: EditText
    private lateinit var etFoodQuantity: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerWeight: Spinner
    private lateinit var btnAddFood: Button
    private lateinit var ivFoodImage: ImageView
    private lateinit var btnSelectImage: Button

    private var selectedImageUri: Uri? = null
    private var imageUrl: String? = null

    private val db = FirebaseFirestore.getInstance()
    private val PICK_IMAGE_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_add_item)

        val toolbar = findViewById<MaterialToolbar>(R.id.AddFood)
        toolbar.setNavigationOnClickListener{finish()}

        initializeViews()
        setupSpinners()
        setupClickListeners()
        initCloudinary()
    }

    private fun initializeViews() {
        etFoodName = findViewById(R.id.etFoodName)
        etFoodPrice = findViewById(R.id.etFoodPrice)
        etFoodQuantity = findViewById(R.id.etFoodQuantity)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerWeight = findViewById(R.id.spinnerWeight)
        btnAddFood = findViewById(R.id.btnAddFood)
        ivFoodImage = findViewById(R.id.ivFoodImages)
        btnSelectImage = findViewById(R.id.btnSelectImage)
    }

    private fun setupSpinners() {
        // Setup Spinner (Dropdown) for Categories
        val categories = arrayOf("Namkeen", "Farsan", "Drinks", "Sweets")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = categoryAdapter

        // Setup Spinner (Dropdown) for Weights
        val weights = arrayOf("250g", "500g", "1kg", "2kg")
        val weightAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, weights)
        spinnerWeight.adapter = weightAdapter
    }

    private fun setupClickListeners() {
        btnSelectImage.setOnClickListener {
            openImageChooser()
        }

        btnAddFood.setOnClickListener {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uploadImageToCloudinary()
        }
    }

    private fun initCloudinary() {
        val config = HashMap<String, String>()
        config["cloud_name"] = "dxwtfkzdx" // Replace with your Cloudinary cloud name
        config["api_key"] = "629759762631632"      // Replace with your Cloudinary API key
        config["api_secret"] = "2mXLW0cJOD7QRZTgAvA7nbRUGRA" // Replace with your Cloudinary API secret
        MediaManager.init(this, config)
    }

    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Food Image"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            ivFoodImage.setImageURI(selectedImageUri)
        }
    }

    private fun uploadImageToCloudinary() {
        selectedImageUri?.let { uri ->
            val fileName = "food_${System.currentTimeMillis()}"

            MediaManager.get().upload(uri)
                .option("public_id", fileName)
                .unsigned("food_upload") // Only if using unsigned uploads
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        btnAddFood.isEnabled = false
                        btnAddFood.text = "Uploading Image..."
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        imageUrl = resultData["secure_url"].toString()
                        addFoodItemToFirestore()
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("Cloudinary", "Upload Error: ${error.description}")
                        Toast.makeText(this@AddItemActivity, "Image upload failed", Toast.LENGTH_SHORT).show()
                        btnAddFood.isEnabled = true
                        btnAddFood.text = "Add Food Item"
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.e("Cloudinary", "Upload Rescheduled: ${error.description}")
                    }
                })
                .dispatch()
        }
    }


    private fun addFoodItemToFirestore() {
        val name = etFoodName.text.toString().trim()
        val price = etFoodPrice.text.toString().trim().toDoubleOrNull()
        val stock = etFoodQuantity.text.toString().trim().toIntOrNull()
        val category = spinnerCategory.selectedItem.toString()
        val weight = spinnerWeight.selectedItem.toString()

        if (name.isEmpty()) {
            etFoodName.error = "Food name is required"
            btnAddFood.isEnabled = true
            btnAddFood.text = "Add Food Item"
            return
        }
        if (price == null || price <= 0) {
            etFoodPrice.error = "Enter a valid price"
            btnAddFood.isEnabled = true
            btnAddFood.text = "Add Food Item"
            return
        }
        if (stock == null || stock <= 0) {
            etFoodQuantity.error = "Enter a valid quantity"
            btnAddFood.isEnabled = true
            btnAddFood.text = "Add Food Item"
            return
        }

        val foodId = "${name}_${weight}".replace(" ", "_").lowercase(Locale.getDefault())

        val foodItem = hashMapOf(
            "name" to name,
            "price" to price,
            "stock" to stock,
            "category" to category,
            "weight" to weight,
            "imageUrl" to imageUrl,
            "foodId" to foodId
        )

        db.collection("foods").document(foodId)
            .set(foodItem)
            .addOnSuccessListener {
                Toast.makeText(this, "$name ($weight) added", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                btnAddFood.isEnabled = true
                btnAddFood.text = "Add Food Item"
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}