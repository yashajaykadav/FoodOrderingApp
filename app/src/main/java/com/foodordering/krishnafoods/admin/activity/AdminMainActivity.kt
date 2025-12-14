// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.AdminOptionAdapter
import com.foodordering.krishnafoods.admin.model.AdminOption
import com.foodordering.krishnafoods.core.util.applyEdgeToEdge
import com.foodordering.krishnafoods.core.util.getCountOrDash
import com.foodordering.krishnafoods.databinding.ActivityAdminMainBinding
import com.foodordering.krishnafoods.user.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class AdminMainActivity : AppCompatActivity() {

    // ViewBinding replaces findViewById
    private lateinit var binding: ActivityAdminMainBinding
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecycler()
        fetchDashboardStats()
    }

    private fun setupUI() {
        // Modularized Edge-to-Edge call
        applyEdgeToEdge(binding.mainConstraintLayout, binding.adminToolbar)

        // Status bar styling
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        binding.adminToolbar.apply {
            title = "Admin Dashboard"
            setTitleTextColor(ContextCompat.getColor(this@AdminMainActivity, R.color.white))
        }

        binding.btnBackToUser.setOnClickListener { switchUserRole() }
    }

    private fun setupRecycler() {
        val options = listOf(
            AdminOption("Manage Items", R.drawable.ic_food, ManageItemsActivity::class.java),
            AdminOption("View Orders", R.drawable.ic_orders, OrdersActivity::class.java),
            AdminOption("Delivered Orders", R.drawable.ic_delivered_order, DeliveredOrdersActivity::class.java),
            AdminOption("Feedback", R.drawable.ic_feedback, FeedbackActivity::class.java),
            AdminOption("User Enquiries", R.drawable.ic_chat, AdminEnquiryActivity::class.java),
            AdminOption("Settings", R.drawable.ic_admin_manage, AdminSettingsActivity::class.java),
            AdminOption("Manage Ads", R.drawable.ic_advertise, ManageAdvertisementActivity::class.java)
        )
        binding.recyclerViewAdminOptions.apply {
            layoutManager = GridLayoutManager(this@AdminMainActivity, 3)
            adapter = AdminOptionAdapter(this@AdminMainActivity, options)
        }
    }

    // Optimization: Parallel fetching using Coroutines
    private fun fetchDashboardStats() = lifecycleScope.launch {
        binding.statsCard.alpha = 0f

        // Fetch all data in parallel
        val pendingCount = firestore.collection("orders").whereEqualTo("status", "Pending").getCountOrDash()
        val deliveredCount = firestore.collection("orders").whereEqualTo("status", "Delivered").getCountOrDash()
        val menuCount = firestore.collection("foods").getCountOrDash()

        // Update UI
        binding.apply {
            txtPending.text = pendingCount
            txtDelivered.text = deliveredCount
            txtMenuItems.text = menuCount
            statsCard.animate().alpha(1f).setDuration(500).start()
        }
    }

    private fun switchUserRole() {
        if (firebaseAuth.currentUser == null) {
            firebaseAuth.signOut() // Ensure clean state
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}