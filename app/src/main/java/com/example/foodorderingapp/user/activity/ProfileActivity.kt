package com.example.foodorderingapp.user.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.foodorderingapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var userEmail: TextView
    private lateinit var userName : TextView
    private lateinit var shopName: TextView
    private lateinit var userContact: TextView
    private lateinit var userAddress: TextView
    private lateinit var logoutButton: Button
    private lateinit var profileImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_profile)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            showToast("User not logged in!")
            finish()
            return
        }

        // Initialize UI components
        userName = findViewById(R.id.userName)
        userEmail = findViewById(R.id.userEmail)
        shopName = findViewById(R.id.shopName)
        userContact = findViewById(R.id.userContact)
        userAddress = findViewById(R.id.userAddress)
        logoutButton = findViewById(R.id.logoutButton)
        profileImage = findViewById(R.id.profileImage)

        // Fetch user data
        fetchUserData(userId)

        // Logout Button Click
        logoutButton.setOnClickListener {
            confirmLogout()
        }
    }

    private fun fetchUserData(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("ProfileActivity", "User Data: ${document.data}")

                    val name = document.getString("name")?:"Unknown"
                    val email = document.getString("email") ?: "N/A"
                    val shop = document.getString("shopName") ?: "No Shop Name"
                    val contact = document.getString("contact") ?: "N/A"
                    val address = document.getString("address") ?: "N/A"
                    val profileImageUrl = document.getString("profileImageUrl")

                    userEmail.text = "Email: $email"
                    shopName.text = "Shop Name : $shop"
                    userContact.text = "Contact: $contact"
                    userAddress.text = "Address: $address"
                    userName.text = "Name : $name"


                    // Load profile image
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(profileImageUrl).into(profileImage)
                    } else {
                        profileImage.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                } else {
                    showToast("User not found!")
                }
            }
            .addOnFailureListener {
                showToast("Error fetching user data.")
            }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}