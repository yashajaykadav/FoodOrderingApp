/*
 * Developed by: Yash Kadav
 * Email: yashkadav52@gmail.com
 * Project: Krishna Foods (ADCET CSE 2026)
 */

package com.foodordering.krishnafoods.user.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.ActivityProfileSetupBinding
import com.foodordering.krishnafoods.user.util.NetworkUtil
import com.foodordering.krishnafoods.user.util.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSetupBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Setup UI & Binding
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupWindowInsets()
        setupListeners()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Adjust Avatar Top Margin
            binding.lottieAvatar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top + (40 * resources.displayMetrics.density).toInt()
            }

            // Adjust Save Button Bottom Margin
            binding.btnSave.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom + (32 * resources.displayMetrics.density).toInt()
            }
            insets
        }
    }

    private fun setupListeners() {
        val userName = intent.getStringExtra("USER_NAME") ?: ""
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        if(userName.isNotEmpty()) {
            binding.textViewSubtitle.text = getString(R.string.welcome_message, userName)
        }

        binding.btnSave.setOnClickListener {
            if (NetworkUtil.isInternetAvailable(this)) {
                validateAndSave(userName, userEmail)
            } else {
                NetworkUtil.showInternetDialog(this) { validateAndSave(userName, userEmail) }
            }
        }
    }

    private fun validateAndSave(userName: String, userEmail: String) {
        val contact = binding.editTextContact.text.toString().trim()
        val shopName = binding.editTextShopName.text.toString().trim()
        val address = binding.editTextAddress.text.toString().trim()
        val authPhone = auth.currentUser?.phoneNumber ?: ""

        // Reset Errors
        binding.layoutContact.error = null
        binding.layoutShopName.error = null
        binding.layoutAddress.error = null

        var isValid = true

        // Validate Contact (Must be 10 digits OR user logged in via phone)
        if (contact.isEmpty() && authPhone.isEmpty()) {
            binding.layoutContact.error = "Phone number is required"
            isValid = false
        } else if (contact.isNotEmpty() && !contact.matches(Regex("^[0-9]{10}$"))) {
            binding.layoutContact.error = "Enter a valid 10-digit number"
            isValid = false
        }

        // Validate Shop Name (Min 3 chars)
        if (shopName.isEmpty() || shopName.length < 3) {
            binding.layoutShopName.error = "Shop name must be at least 3 characters"
            isValid = false
        }

        // Validate Address (Min 10 chars)
        if (address.isEmpty() || address.length < 10) {
            binding.layoutAddress.error = "Please provide a complete address"
            isValid = false
        }

        if (isValid) {
            val finalContact = contact.ifEmpty { authPhone }
            saveToFirestore(userName, userEmail, finalContact, shopName, address)
        }
    }

    private fun saveToFirestore(name: String, email: String, contact: String, shop: String, address: String) {
        val user = auth.currentUser ?: return
        setLoading(true)

        val userData = hashMapOf(
            "uid" to user.uid,
            "name" to name,
            "email" to email,
            "contact" to contact,
            "shopName" to shop,
            "address" to address,
            "role" to "user",
            "profileCompleted" to true,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore.collection("users").document(user.uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                if (!isDestroyed) {
                    setLoading(false)
                    showToast("Profile Setup Complete!")
                    navigateToHome()
                }
            }
            .addOnFailureListener { e ->
                if (!isDestroyed) {
                    setLoading(false)
                    showToast("Error: ${e.localizedMessage}")
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnSave.text = "" // Hide text
            binding.btnSave.isEnabled = false
            binding.lottieLoading.visibility = View.VISIBLE
        } else {
            binding.btnSave.text = getString(R.string.save_profile)
            binding.btnSave.isEnabled = true
            binding.lottieLoading.visibility = View.GONE
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}