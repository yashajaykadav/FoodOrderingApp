/*
 * Developed by: Yash Kadav
 * Email: yashkadav52@gmail.com
 */

package com.foodordering.krishnafoods.user.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.UserActivityMainBinding
import com.foodordering.krishnafoods.user.manager.CartManager
import com.foodordering.krishnafoods.user.util.LoadingDialog
import com.foodordering.krishnafoods.user.util.NavigationUtils
import com.foodordering.krishnafoods.user.util.ViewPagerAdapter
import com.foodordering.krishnafoods.user.util.loadToolbarIcon
import com.foodordering.krishnafoods.user.util.loadUserAvatar
import com.foodordering.krishnafoods.user.util.showLogoutDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: UserActivityMainBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = UserActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)
        CartManager.loadCart(this)

        setupWindowInsets()
        setupUI()
    }

    private fun setupWindowInsets() {
        val fabParams = binding.fabEnquiry.layoutParams as ViewGroup.MarginLayoutParams
        val initialFabMargin = fabParams.bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(binding.drawerLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.updatePadding(top = systemBars.top)
            binding.bottomNavigationView.updatePadding(bottom = systemBars.bottom)
            fabParams.bottomMargin = initialFabMargin + systemBars.bottom
            binding.fabEnquiry.layoutParams = fabParams
            insets
        }
    }

    private fun setupUI() {
        // 1. MODULAR: Load Toolbar Icon
        val photoUrl = auth.currentUser?.photoUrl
        loadToolbarIcon(photoUrl?.toString()) { drawable ->
            binding.topAppBar.navigationIcon = drawable
        }

        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        setupNavigation()
        setupDrawer()

        binding.fabEnquiry.setOnClickListener {
            startActivity(Intent(this, UserEnquiryActivity::class.java))
        }
    }

    private fun setupDrawer() {
        val headerBinding = com.foodordering.krishnafoods.databinding.NavHeaderBinding.bind(binding.navigationView.getHeaderView(0))
        auth.currentUser?.let { user ->
            headerBinding.userName.text = user.displayName
            headerBinding.profileImage.loadUserAvatar(user.photoUrl.toString())
        }

        binding.navigationView.setNavigationItemSelectedListener { item ->
            binding.drawerLayout.closeDrawers()
            when (item.itemId) {
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, AboutActivity::class.java))
                // 3. MODULAR: Admin Check & Logout
                R.id.nav_admin_dashboard -> NavigationUtils.verifyAdminAccess(this, loadingDialog)
                R.id.nav_logout -> showLogoutDialog { NavigationUtils.logoutUser(this) }
            }
            true
        }

        val uid = auth.currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener {
                binding.navigationView.menu.findItem(R.id.nav_admin_dashboard).isVisible = (it.getString("role") == "admin")
            }
    }
    private fun setupNavigation() {
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),   // State: Selected
                intArrayOf(-android.R.attr.state_checked)   // State: Unselected
            ),
            intArrayOf(
                ContextCompat.getColor(this, R.color.colorAccent),

                ContextCompat.getColor(this, android.R.color.white)
            )
        )

        binding.bottomNavigationView.itemIconTintList = colorStateList
        binding.bottomNavigationView.itemTextColor = colorStateList

        // 3. Setup ViewPager
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 3

        // 4. Link BottomNav with ViewPager
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_menu -> binding.viewPager.currentItem = 0
                R.id.nav_cart -> binding.viewPager.currentItem = 1
                R.id.nav_orders -> binding.viewPager.currentItem = 2
            }
            true
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.bottomNavigationView.menu[position].isChecked = true

                // Animate FAB visibility
                if (position == 0) binding.fabEnquiry.show() else binding.fabEnquiry.hide()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val count = CartManager.getCartItems().size
        binding.bottomNavigationView.getOrCreateBadge(R.id.nav_cart).apply {
            isVisible = count > 0
            number = count
        }
    }
}