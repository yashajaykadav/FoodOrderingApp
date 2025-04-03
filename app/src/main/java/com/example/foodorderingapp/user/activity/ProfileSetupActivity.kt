package com.example.foodorderingapp.user.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.foodorderingapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var editTextContact: EditText
    private lateinit var editTextShopName: EditText
    private lateinit var editTextAddress: EditText
    private lateinit var btnSave: Button
    private lateinit var lottieLoading: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        // Initialize UI elements
        editTextContact = findViewById(R.id.editTextContact)
        editTextShopName = findViewById(R.id.editTextShopName)
        editTextAddress = findViewById(R.id.editTextAddress)
        btnSave = findViewById(R.id.btnSave)
        lottieLoading = findViewById(R.id.lottieLoading)


        // Get user details from intent
        val userName = intent.getStringExtra("USER_NAME") ?: ""
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        // Pre-fill Shop Name with user's display name
        editTextShopName.setText(userName)

        btnSave.setOnClickListener {
            saveProfileData(userName, userEmail)
        }
    }

    private fun saveProfileData(userName: String, userEmail: String) {
        val contact = editTextContact.text.toString().trim()
        val shopName = editTextShopName.text.toString().trim()
        val address = editTextAddress.text.toString().trim()
        val phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: "" // ✅ Get phone if available

        // ✅ Improved Validation
        if (!isValidContact(contact) && phoneNumber.isEmpty()) {
            Toast.makeText(this, "Enter a valid contact number (10 digits)", Toast.LENGTH_SHORT).show()
            return
        }
        if (shopName.isBlank()) {
            editTextShopName.error = "Shop Name is required"
            return
        }
        if (address.isBlank()) {
            editTextAddress.error = "Shop Address is required"
            return
        }

        showLoading(true)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("ProfileSetup", "User is not logged in")
            showLoading(false)
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user.uid)

        val userData = mapOf(
            "uid" to user.uid,
            "name" to userName,
            "email" to userEmail,
            "contact" to contact.ifEmpty { phoneNumber }, // ✅ Use Firebase phone if empty
            "address" to address,
            "shopName" to shopName
        )

        userRef.set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firestore", "Profile updated successfully!")
                Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving profile", e)
                Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show()
                showLoading(false)
            }
    }

    private fun isValidContact(contact: String): Boolean {
        return contact.matches(Regex("^[0-9]{10}$"))
    }

    private fun showLoading(isLoading: Boolean) {
        lottieLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSave.isEnabled = !isLoading
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
