package com.example.foodorderingapp.user.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.foodorderingapp.R
import com.example.foodorderingapp.admin.activity.AdminMainActivity
import com.example.foodorderingapp.user.util.ViewPagerAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var viewPager: ViewPager2
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var fabCart: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_main)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize UI elements
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        topAppBar = findViewById(R.id.topAppBar)
        viewPager = findViewById(R.id.viewPager)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        fabCart = findViewById(R.id.fabCart)

        checkUserRole() // 🔐 Hide Admin Dashboard for non-admins

        // ✅ Setup Badge for Cart
        setupCartBadge()

        // ✅ Open Drawer when clicking Navigation Icon
        topAppBar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // ✅ Set up ViewPager for Swipe Navigation
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        // ✅ Sync ViewPager with Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_menu -> viewPager.setCurrentItem(0, true)
                R.id.nav_cart -> viewPager.setCurrentItem(1, true)
                R.id.nav_orders -> viewPager.setCurrentItem(2, true)
            }
            true
        }

        // ✅ Sync Bottom Navigation with ViewPager Swiping
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })

        // ✅ Handle Navigation Drawer Item Clicks (with delay to prevent multiple taps)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawers()
            navigationView.postDelayed({
                handleNavigationClick(menuItem.itemId)
            }, 250) // Small delay to avoid quick multiple clicks
            true
        }

// ✅ Floating Action Button Click Listener (Navigate to User Enquiry Activity)
        fabCart.setOnClickListener {
            val intent = Intent(this, UserEnquiryActivity::class.java) // 🔥 Redirect user to their Enquiry page
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

    }

    // ✅ Update Cart Badge
    private fun setupCartBadge() {
        val badge: BadgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.nav_cart)
        badge.isVisible = true
        badge.number = 0 // Example count, update dynamically
    }

    private fun handleNavigationClick(itemId: Int) {
        when (itemId) {
            R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
            R.id.nav_settings -> Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show()
            R.id.nav_logout -> showLogoutConfirmation()
            R.id.nav_admin_dashboard -> checkAdminAccess() // 🔐 Check before opening admin panel
        }
    }
    private fun checkUserRole() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId.isNullOrEmpty()) {
            return
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: "user"
                val menu = navigationView.menu
                menu.findItem(R.id.nav_admin_dashboard).isVisible = (role == "admin")
            }
    }

    // ✅ Improved Logout Handling
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                firebaseAuth.signOut()

                // Redirect to LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun checkAdminAccess() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: "user"

                if (role == "admin") {
                    startActivity(Intent(this, AdminMainActivity::class.java))
                } else {
                    Toast.makeText(this, "Access Denied! Admins Only", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error verifying access", Toast.LENGTH_SHORT).show()
            }
    }


}
