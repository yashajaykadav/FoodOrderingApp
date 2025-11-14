package com.foodordering.krishnafoods.user.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.foodordering.krishnafoods.R
import com.google.android.material.appbar.MaterialToolbar
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
    private lateinit var editProfileButton: MaterialButton
    private lateinit var logoutButton: MaterialButton
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_profile)

        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)

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

        // Toolbar setup
        setupToolbar()

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
        editProfileButton = findViewById(R.id.editProfileButton)
        logoutButton = findViewById(R.id.logoutButton)
        toolbar = findViewById(R.id.topAppBar)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.editProfileButton -> {
                    startActivity(Intent(this, EditProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupButtonListeners() {
        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        logoutButton.setOnClickListener {
            confirmLogout()
        }
    }

    private fun fetchUserData(userId: String) {
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Unknown"
                    val email = document.getString("email") ?: "N/A"
                    val shop = document.getString("shopName") ?: "No Shop Name"
                    val contact = document.getString("contact") ?: "N/A"
                    val address = document.getString("address") ?: "N/A"
                    val profileImageUrl = document.getString("photoUrl")

                    // Update UI
                    userName.text = name
                    userEmail.text = email
                    shopName.text = shop
                    userContact.text = contact
                    userAddress.text = address

                    // Load profile image
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .circleCrop()
                            .into(profileImage)
                    }

                } else {
                    showToast("User not found!")
                }
            }
            .addOnFailureListener { exception ->
                showToast("Error fetching user data: ${exception.message}")
            }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(true)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
