package com.example.foodorderingapp.user.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.foodorderingapp.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // UI Components
    private lateinit var userEmail: TextView
    private lateinit var userName: TextView
    private lateinit var shopName: TextView
    private lateinit var userContact: TextView
    private lateinit var userAddress: TextView
    private lateinit var profileImage: ImageView
    private lateinit var backButton: MaterialButton
    private lateinit var editProfileButton: MaterialButton
    private lateinit var logoutButton: MaterialButton
    private lateinit var settingsButton: MaterialButton
    private lateinit var facebookButton: ImageButton
    private lateinit var instagramButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid
        if (userId == null) {
            showToast("User not logged in!")
            finish()
            return
        }

        // Initialize UI components
        initializeViews()

        // Fetch user data
        fetchUserData(userId)

        // Set up button listeners
        setupButtonListeners()
    }

    private fun initializeViews() {
        userName = findViewById(R.id.userName)
        userEmail = findViewById(R.id.userEmail)
        shopName = findViewById(R.id.shopName)
        userContact = findViewById(R.id.userContact)
        userAddress = findViewById(R.id.userAddress)
        profileImage = findViewById(R.id.profileImage)
        backButton = findViewById(R.id.backButton)
        editProfileButton = findViewById(R.id.editProfileButton)
        logoutButton = findViewById(R.id.logoutButton)
        settingsButton = findViewById(R.id.settingsButton)
        facebookButton = findViewById(R.id.facebookButton)
        instagramButton = findViewById(R.id.instagramButton)
    }

    private fun setupButtonListeners() {
        backButton.setOnClickListener {
            onBackPressed()
        }

        editProfileButton.setOnClickListener {
//            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        logoutButton.setOnClickListener {
            confirmLogout()
        }

        settingsButton.setOnClickListener {
//            startActivity(Intent(this, SettingsActivity::class.java))
        }

        facebookButton.setOnClickListener {
            openSocialMedia("facebook")
        }

        instagramButton.setOnClickListener {
            openSocialMedia("instagram")
        }
    }

    private fun fetchUserData(userId: String) {
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("ProfileActivity", "User Data: ${document.data}")

                    val name = document.getString("name") ?: "Unknown"
                    val email = document.getString("email") ?: "N/A"
                    val shop = document.getString("shopName") ?: "No Shop Name"
                    val contact = document.getString("contact") ?: "N/A"
                    val address = document.getString("address") ?: "N/A"
                    val profileImageUrl = document.getString("photoUrl")
                    val facebookUrl = document.getString("facebookUrl")
                    val instagramUrl = document.getString("instagramUrl")

                    // Update UI
                    userName.text = name
                    userEmail.text = email
                    shopName.text = shop
                    userContact.text = "Contact: $contact"
                    userAddress.text = "Address: $address"

                    // Load profile image
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(profileImage)
                    }

                    // Enable social media buttons if URLs exist
                    facebookButton.isEnabled = !facebookUrl.isNullOrEmpty()
                    instagramButton.isEnabled = !instagramUrl.isNullOrEmpty()

                } else {
                    showToast("User not found!")
                }
            }
            .addOnFailureListener { exception ->
                showToast("Error fetching user data: ${exception.message}")
                Log.e("ProfileActivity", "Error fetching data", exception)
            }
    }

    private fun openSocialMedia(platform: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val url = when (platform) {
                    "facebook" -> document.getString("facebookUrl")
                    "instagram" -> document.getString("instagramUrl")
                    else -> null
                }

                if (!url.isNullOrEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                    startActivity(intent)
                } else {
                    showToast("No $platform profile linked")
                }
            }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity() // Close all activities
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(true)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}