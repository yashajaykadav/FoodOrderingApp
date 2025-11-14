package com.foodordering.krishnafoods.admin.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.AdminOptionAdapter
import com.foodordering.krishnafoods.admin.model.AdminOption
import com.foodordering.krishnafoods.user.activity.MainActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminMainActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var btnSwitchUser: Button
    private lateinit var txtPending: TextView
    private lateinit var txtDelivered: TextView
    private lateinit var txtMenuItems: TextView
    private lateinit var statsCard: MaterialCardView
    private lateinit var mainLayout: ConstraintLayout
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_admin_main)
        supportActionBar?.hide()

        // Set status bar color to colorAccent
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)

        // Make status bar icons dark or light based on your accent color
        WindowCompat.getInsetsController(window, window.decorView)?.apply {
            isAppearanceLightStatusBars = false // Set to true if colorAccent is light
        }

        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize views
        toolbar = findViewById(R.id.adminToolbar)
        recyclerView = findViewById(R.id.recyclerViewAdminOptions)
        btnSwitchUser = findViewById(R.id.btnBackToUser)
        txtPending = findViewById(R.id.txtPending)
        txtDelivered = findViewById(R.id.txtDelivered)
        txtMenuItems = findViewById(R.id.txtMenuItems)
        statsCard = findViewById(R.id.statsCard)
        mainLayout = findViewById(R.id.mainConstraintLayout)

        // Handle window insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply top padding to toolbar to account for status bar
            toolbar.updatePadding(top = systemBars.top)

            // Apply bottom padding to the main layout for navigation bar
            view.updatePadding(bottom = systemBars.bottom)

            WindowInsetsCompat.CONSUMED
        }

        toolbar.title = "Admin Dashboard"
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))

        setupRecycler()
        btnSwitchUser.setOnClickListener { checkUserRoleBeforeSwitching() }

        fetchDashboardStats()
    }

    private fun setupRecycler() {
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        val options = listOf(
            AdminOption("Manage Items", R.drawable.ic_food, ManageItemsActivity::class.java),
            AdminOption("View Orders", R.drawable.ic_orders, OrdersActivity::class.java),
            AdminOption("Delivered Orders", R.drawable.ic_delivered_order, DeliveredOrdersActivity::class.java),
            AdminOption("Feedback", R.drawable.ic_feedback, FeedbackActivity::class.java),
            AdminOption("User Enquiries", R.drawable.ic_chat, AdminEnquiryActivity::class.java),
            AdminOption("Settings", R.drawable.ic_admin_manage, AdminSettingsActivity::class.java),
            AdminOption("Manage Ads", R.drawable.ic_advertise, ManageAdvertisementActivity::class.java)
        )
        recyclerView.adapter = AdminOptionAdapter(this, options)
    }

    private fun fetchDashboardStats() {
        txtPending.text = "..."
        txtDelivered.text = "..."
        txtMenuItems.text = "..."
        statsCard.alpha = 0f

        // Pending orders
        firestore.collection("orders")
            .whereEqualTo("status", "Pending")
            .get()
            .addOnSuccessListener { snapshot ->
                txtPending.text = snapshot.size().toString()
                fadeInStats()
            }.addOnFailureListener {
                txtPending.text = "-"
                fadeInStats()
            }

        // Delivered orders
        firestore.collection("orders")
            .whereEqualTo("status", "Delivered")
            .get()
            .addOnSuccessListener { snapshot ->
                txtDelivered.text = snapshot.size().toString()
            }.addOnFailureListener {
                txtDelivered.text = "-"
            }

        // Menu items
        firestore.collection("foods")
            .get()
            .addOnSuccessListener { snapshot ->
                txtMenuItems.text = snapshot.size().toString()
            }.addOnFailureListener {
                txtMenuItems.text = "-"
            }
    }

    private fun fadeInStats() {
        statsCard.animate().alpha(1f).setDuration(500).start()
    }

    private fun checkUserRoleBeforeSwitching() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            logoutAdmin()
        }
    }

    private fun logoutAdmin() {
        firebaseAuth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}