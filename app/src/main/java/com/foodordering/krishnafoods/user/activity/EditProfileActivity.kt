package com.foodordering.krishnafoods.user.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.ActivityEditProfileBinding
import com.foodordering.krishnafoods.user.util.loadUrl
import com.foodordering.krishnafoods.user.util.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // UI Setup
        window.statusBarColor = ContextCompat.getColor(this, R.color.lightRed)
        setupToolbar()

        // Firebase Setup
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid
        if (userId == null) {
            showToast("Session expired. Please log in again.")
            finish()
            return
        }

        fetchUserData(userId)
        setupListeners()
    }

    private fun setupToolbar() {
        binding.profileToolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            validateAndSaveProfile()
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchUserData(userId: String) {
        // UI Feedback: Disable editing while loading (optional)
        binding.saveButton.isEnabled = false

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (isDestroyed || isFinishing) return@addOnSuccessListener // Prevent Crash

                if (document.exists()) {
                    binding.apply {
                        userNameEditText.setText(document.getString("name"))
                        shopNameEditText.setText(document.getString("shopName"))
                        userContactEditText.setText(document.getString("contact"))
                        userAddressEditText.setText(document.getString("address"))

                        // Use our modular extension
                        profileImage.loadUrl(document.getString("photoUrl"))
                    }
                } else {
                    showToast("User details not found.")
                }
                binding.saveButton.isEnabled = true
            }
            .addOnFailureListener { e ->
                if (!isDestroyed) {
                    showToast("Failed to load data: ${e.localizedMessage}")
                    binding.saveButton.isEnabled = true
                }
            }
    }

    private fun validateAndSaveProfile() {
        val name = binding.userNameEditText.text.toString().trim()
        val shop = binding.shopNameEditText.text.toString().trim()
        val contact = binding.userContactEditText.text.toString().trim()
        val address = binding.userAddressEditText.text.toString().trim()

        // Input Validation
        if (name.isEmpty()) {
            binding.userNameEditText.error = "Name is required"
            return
        }
        if (contact.length < 10) {
            binding.userContactEditText.error = "Valid contact required"
            return
        }

        saveToFirebase(name, shop, contact, address)
    }

    private fun saveToFirebase(name: String, shop: String, contact: String, address: String) {
        val userId = auth.currentUser?.uid ?: return

        4
        binding.saveButton.text = getString(R.string.save_profile) // Ensure this string exists or use "Saving..."
        binding.saveButton.isEnabled = false

        val updates = mapOf(
            "name" to name,
            "shopName" to shop,
            "contact" to contact,
            "address" to address
        )

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                showToast("Profile updated successfully!")
                finish() // Close activity on success
            }
            .addOnFailureListener { e ->
                showToast("Update failed: ${e.localizedMessage}")
                // Reset button state on failure
                binding.saveButton.text = getString(R.string.saving_info) // Ensure "Save" string exists
                binding.saveButton.isEnabled = true
            }
    }
}