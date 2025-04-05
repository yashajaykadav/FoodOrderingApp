package com.example.foodorderingapp.admin.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.admin.model.AdminOption
import com.example.foodorderingapp.admin.adapter.AdminOptionAdapter
import com.example.foodorderingapp.user.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth

class AdminMainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminOptionAdapter
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        firebaseAuth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recyclerViewAdminOptions)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns

        val options = listOf(
            AdminOption("Manage Items", R.drawable.food, ManageItemsActivity::class.java),
            AdminOption("View Orders", R.drawable.order, OrdersActivity::class.java),
            AdminOption("Delivered Orders", R.drawable.delivered, DeliveredOrdersActivity::class.java),
            AdminOption("Feedback", R.drawable.feedback, FeedbackActivity::class.java),
            AdminOption("User Enquiries", R.drawable.chat, AdminEnquiryActivity::class.java),
            AdminOption("Settings", R.drawable.setting, AdminSettingsActivity::class.java)
        )

        adapter = AdminOptionAdapter(this, options)
        recyclerView.adapter = adapter

        val btnBackToUserApp = findViewById<Button>(R.id.btnBackToUser)
        btnBackToUserApp.setOnClickListener {
            checkUserRoleBeforeSwitching()
        }
    }

    override fun onStop() {
        super.onStop()
        // Release resources or stop ongoing operations
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources to prevent memory leaks
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.admin_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                logoutAdmin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logoutAdmin() {
        firebaseAuth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun checkUserRoleBeforeSwitching() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // Admin cannot switch to user side if not logged in
            logoutAdmin()
        }
    }
}