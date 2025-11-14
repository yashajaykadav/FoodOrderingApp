package com.foodordering.krishnafoods.user.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView // Import TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.foodordering.krishnafoods.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // UI Components
    private lateinit var profileToolbar: MaterialToolbar
    private lateinit var userNameEditText: EditText
    private lateinit var shopNameEditText: EditText
    private lateinit var userContactEditText: EditText
    private lateinit var userAddressEditText: EditText
    private lateinit var profileImage: ImageView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: TextView // Changed to TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize all UI components from the layout
        initViews()

        // Set up the toolbar
        setupToolbar()

        // Hide the default action bar and set status bar color
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.lightRed)
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showToast("User not logged in!")
            finish()
            return
        }

        // Fetch user data
        fetchUserData(userId)

        // Set up button listeners
        setupButtonListeners()
    }

    private fun initViews() {
        // Initialize UI components
        profileToolbar = findViewById(R.id.profileToolbar)
        userNameEditText = findViewById(R.id.userNameEditText)
        // userEmailEditText = findViewById(R.id.userEmailEditText) // This view is not in your original XML, so I commented it out
        shopNameEditText = findViewById(R.id.shopNameEditText)
        userContactEditText = findViewById(R.id.userContactEditText)
        userAddressEditText = findViewById(R.id.userAddressEditText)

        // This is correct. The code finds the ImageView with the ID 'profileImage'
        // which is located inside the CardView in your XML. No changes are needed here.
        profileImage = findViewById(R.id.profileImage)

        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
    }

    private fun setupToolbar() {
        // Now profileToolbar is guaranteed to be initialized
        profileToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupButtonListeners() {
        saveButton.setOnClickListener {
            saveProfileChanges()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchUserData(userId: String) {
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Unknown"
                    val shop = document.getString("shopName") ?: "No Shop Name"
                    val contact = document.getString("contact") ?: "N/A"
                    val address = document.getString("address") ?: "N/A"
                    val profileImageUrl = document.getString("photoUrl")

                    // Update UI
                    userNameEditText.setText(name)
                    shopNameEditText.setText(shop)
                    userContactEditText.setText(contact)
                    userAddressEditText.setText(address)

                    // Load profile image
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
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

    private fun saveProfileChanges() {
        val userId = auth.currentUser?.uid ?: return

        val name = userNameEditText.text.toString().trim()
        val shop = shopNameEditText.text.toString().trim()
        val contact = userContactEditText.text.toString().trim()
        val address = userAddressEditText.text.toString().trim()

        val userRef = db.collection("users").document(userId)

        userRef.update(
            mapOf(
                "name" to name,
                "shopName" to shop,
                "contact" to contact,
                "address" to address
            )
        )
            .addOnSuccessListener {
                showToast("Profile updated successfully")
                finish()
            }
            .addOnFailureListener { exception ->
                showToast("Error updating profile: ${exception.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
