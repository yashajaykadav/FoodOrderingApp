package com.foodordering.krishnafoods.user.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.UserActivityProfileBinding
import com.foodordering.krishnafoods.user.util.loadUrl
import com.foodordering.krishnafoods.user.util.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: UserActivityProfileBinding

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup System Bar Color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorError)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        checkUserSession()
    }

    private fun checkUserSession() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showToast("Session expired. Please log in.")
            redirectToLogin()
        } else {
            setupViews()
            fetchUserData(userId)
        }
    }

    private fun setupViews() {
        // Toolbar Navigation
        binding.topAppBar.setNavigationOnClickListener { finish() }

        // Edit Profile Button
        binding.editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Logout Button
        binding.logoutButton.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun fetchUserData(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                // CRITICAL FIX: Check if activity is still alive to prevent crashes
                if (isDestroyed || isFinishing) return@addOnSuccessListener

                if (document.exists()) {
                    binding.apply {
                        userName.text = document.getString("name") ?: "Unknown User"
                        userEmail.text = document.getString("email") ?: auth.currentUser?.email ?: "N/A"
                        shopName.text = document.getString("shopName") ?: "No Shop Name Set"
                        userContact.text = document.getString("contact") ?: "N/A"
                        userAddress.text = document.getString("address") ?: "N/A"

                        // Using our new extension function
                        profileImage.loadUrl(document.getString("photoUrl"))
                    }
                } else {
                    showToast("User profile not found.")
                }
            }
            .addOnFailureListener { e ->
                if (!isDestroyed && !isFinishing) {
                    showToast("Error loading profile: ${e.localizedMessage}")
                }
            }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                redirectToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}