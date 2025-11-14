package com.foodordering.krishnafoods.user.activity

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.activity.AdminMainActivity
import com.foodordering.krishnafoods.user.manager.CartManager
import com.foodordering.krishnafoods.user.util.LoadingDialog
import com.foodordering.krishnafoods.user.util.NetworkUtil
import com.foodordering.krishnafoods.user.util.ViewPagerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    // View properties
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var viewPager: ViewPager2
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var fabCart: FloatingActionButton
    private lateinit var appBarLayout: AppBarLayout


    // Other properties
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        // Enable edge-to-edge display (removes deprecated status bar code)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.user_activity_main)

        // Initialize components
        firebaseAuth = FirebaseAuth.getInstance()
        loadingDialog = LoadingDialog(this)
        CartManager.loadCart(this)
        sharedPrefs = getSharedPreferences("settings", MODE_PRIVATE)
        appBarLayout = findViewById(R.id.appBarLayout) // Add this line

        // Initialize views using findViewById
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        topAppBar = findViewById(R.id.topAppBar)
        viewPager = findViewById(R.id.viewPager)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        fabCart = findViewById(R.id.fabEnquiry)

        handleWindowInsets()
        setupAll()
    }

    private fun handleWindowInsets() {
        val initialFabMargin = (fabCart.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply TOP padding to the AppBarLayout to push it down from the status bar
            appBarLayout.updatePadding(top = systemBars.top)

            // Apply BOTTOM padding for the BottomNavigationView and FAB
            bottomNavigationView.updatePadding(bottom = systemBars.bottom)
            (fabCart.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = initialFabMargin + systemBars.bottom

            // Return the insets so other views can use them
            insets
        }
    }

    private fun setupAll() {
        setupToolbarProfile()
        setupBottomNavigation()
        setupDrawerHeader()
        setupViewPager()
        setupFAB()
        setupDrawerNavigation()
        updateCartBadge()
        checkUserRole()
    }

    private fun setupToolbarProfile() {
        val user: FirebaseUser? = firebaseAuth.currentUser
        user?.photoUrl?.let { uri ->
            Glide.with(this)
                .load(uri)
                .circleCrop()
                .placeholder(R.drawable.user)
                .into(object : CustomTarget<Drawable>(65, 65) {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        topAppBar.navigationIcon = resource
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        topAppBar.navigationIcon = ContextCompat.getDrawable(this@MainActivity, R.drawable.user)
                    }
                })
        } ?: run {
            topAppBar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.user)
        }

        topAppBar.setNavigationContentDescription(R.string.user_format)
        topAppBar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupBottomNavigation() {
        // RESTORED: Previous programmatic logic for setting item colors.
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.white)
            )
        )
        bottomNavigationView.itemTextColor = colorStateList
        bottomNavigationView.itemIconTintList = colorStateList
        bottomNavigationView.itemBackground = ContextCompat.getDrawable(this, R.drawable.bottom_nav_item_background)
        bottomNavigationView.elevation = 8f

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_menu -> viewPager.setCurrentItem(0, true)
                R.id.nav_cart -> viewPager.setCurrentItem(1, true)
                R.id.nav_orders -> viewPager.setCurrentItem(2, true)
            }
            true
        }
    }

    private fun setupViewPager() {
        viewPager.adapter = ViewPagerAdapter(this)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNavigationView.menu[position].isChecked = true
                fabCart.visibility = if (position == 0) View.VISIBLE else View.GONE
            }
        })
    }

    private fun setupFAB() {
        fabCart.setOnClickListener {
            it.animate().rotationBy(360f).setDuration(300).start()
            startActivity(Intent(this, UserEnquiryActivity::class.java))
        }
    }

    private fun setupDrawerHeader() {
        val headerView = navigationView.getHeaderView(0)
        val profileImage: ImageView = headerView.findViewById(R.id.profileImage)
        val userName: TextView = headerView.findViewById(R.id.userName)

        firebaseAuth.currentUser?.let { user ->
            userName.text = user.displayName ?: "Guest"
            user.photoUrl?.let { uri ->
                Glide.with(this).load(uri).placeholder(R.drawable.user).circleCrop().into(profileImage)
            }
        }
    }

    private fun setupDrawerNavigation() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawers()
            navigationView.postDelayed({
                handleNavigationClick(menuItem.itemId)
            }, 250)
            true
        }
    }

    private fun handleNavigationClick(itemId: Int) {
        when (itemId) {
            R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
            R.id.nav_settings -> startActivity(Intent(this, AboutActivity::class.java))
            R.id.nav_logout -> showLogoutConfirmation()
            R.id.nav_admin_dashboard -> checkAdminAccess()
        }
    }

    fun updateCartBadge() {
        val badge = bottomNavigationView.getOrCreateBadge(R.id.nav_cart)
        val cartSize = CartManager.getCartItems().size
        badge.isVisible = cartSize > 0
        badge.number = cartSize
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_confirm))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                firebaseAuth.signOut()
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun checkAdminAccess() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        if (NetworkUtil.isInternetAvailable(this)) {
            loadingDialog.show(getString(R.string.verifying))
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    val role = doc.getString("role") ?: "user"
                    if (role == "admin") {
                        startActivity(Intent(this, AdminMainActivity::class.java))
                    } else {
                        Toast.makeText(this, getString(R.string.admin_access_denied), Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show() }
                .addOnCompleteListener { loadingDialog.dismiss() }
        } else {
            NetworkUtil.showInternetDialog(this) { checkAdminAccess() }
        }
    }

    private fun checkUserRole() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        if (NetworkUtil.isInternetAvailable(this)) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    val role = doc.getString("role") ?: "user"
                    navigationView.menu.findItem(R.id.nav_admin_dashboard).isVisible = (role == "admin")
                }
                .addOnFailureListener { Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onResume() {
        super.onResume()
        updateCartBadge()
        fabCart.visibility = if (viewPager.currentItem == 0) View.VISIBLE else View.GONE
    }
}