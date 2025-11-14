package com.foodordering.krishnafoods.user.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.foodordering.krishnafoods.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var editTextContact: TextInputEditText
    private lateinit var editTextShopName: TextInputEditText
    private lateinit var editTextAddress: TextInputEditText
    private lateinit var layoutContact: TextInputLayout
    private lateinit var layoutShopName: TextInputLayout
    private lateinit var layoutAddress: TextInputLayout
    private lateinit var btnSave: MaterialButton
    private lateinit var lottieLoading: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_profile_setup)

        // MODIFIED: Initialize all views together first
        editTextContact = findViewById(R.id.editTextContact)
        editTextShopName = findViewById(R.id.editTextShopName)
        editTextAddress = findViewById(R.id.editTextAddress)
        layoutContact = findViewById(R.id.layoutContact)
        layoutShopName = findViewById(R.id.layoutShopName)
        layoutAddress = findViewById(R.id.layoutAddress)
        btnSave = findViewById(R.id.btnSave) // Initialized here once
        lottieLoading = findViewById(R.id.lottieLoading)
        val lottieAvatar = findViewById<View>(R.id.lottieAvatar)

        // Handle the insets using the initialized properties
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            (lottieAvatar.layoutParams as ViewGroup.MarginLayoutParams).topMargin =
                systemBars.top + (60 * resources.displayMetrics.density).toInt()

            // MODIFIED: Re-use the 'btnSave' property instead of a new local variable
            (btnSave.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                systemBars.bottom + (50 * resources.displayMetrics.density).toInt()

            insets
        }

        // The rest of your setup logic
        val userName = intent.getStringExtra("USER_NAME") ?: ""
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        btnSave.setOnClickListener {
            validateAndSave(userName, userEmail)
        }
    }

    private fun validateAndSave(userName: String, userEmail: String) {
        val contact = editTextContact.text.toString().trim()
        val shopName = editTextShopName.text.toString().trim()
        val address = editTextAddress.text.toString().trim()
        val phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""

        layoutContact.error = null
        layoutShopName.error = null
        layoutAddress.error = null

        var valid = true

        // Validate phone number
        if (contact.isBlank() && phoneNumber.isBlank()) {
            layoutContact.error = getString(R.string.phone_error)
            valid = false
        } else if (contact.isNotBlank() && !contact.matches(Regex("^[0-9]{10}$"))) {
            layoutContact.error = getString(R.string.phone_error)
            valid = false
        }

        // Validate shop name
        if (shopName.isBlank() || shopName.length < 3) {
            layoutShopName.error = getString(R.string.shop_name_error)
            valid = false
        }

        // Validate address (basic check for length and content)
        if (address.isBlank() || address.length < 10) {
            layoutAddress.error = getString(R.string.address_error)
            valid = false
        }

        if (!valid) return

        showLoading(true)
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Authentication error. Please sign in again.", Toast.LENGTH_LONG).show()
            showLoading(false)
            return
        }

        val userData = mapOf(
            "uid" to user.uid,
            "name" to userName,
            "email" to userEmail,
            "contact" to contact.ifEmpty { phoneNumber },
            "shopName" to shopName,
            "address" to address,
            "role" to "user",
            "updatedAt" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_LONG).show()
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to save profile: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
                showLoading(false)
            }
    }

    private fun showLoading(isLoading: Boolean) {
        lottieLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSave.isEnabled = !isLoading
        btnSave.alpha = if (isLoading) 0.5f else 1.0f
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}