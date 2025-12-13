package com.foodordering.krishnafoods.user.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.user.adapter.OrderSummaryAdapter
import com.foodordering.krishnafoods.user.manager.CartManager
import com.foodordering.krishnafoods.user.viewmodel.OrderTotals
import com.foodordering.krishnafoods.user.repository.FoodRepository
import com.foodordering.krishnafoods.user.repository.OrderRepository
import com.foodordering.krishnafoods.user.repository.UserRepository
import com.foodordering.krishnafoods.user.util.AnimHelper
import com.foodordering.krishnafoods.user.util.NetworkHelper
import com.foodordering.krishnafoods.user.util.safeCall
import com.foodordering.krishnafoods.user.viewmodel.FoodItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class OrderConfirmationActivity : AppCompatActivity() {

    // Views
    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerViewOrder: RecyclerView
    private lateinit var btnConfirmOrder: MaterialButton
    private lateinit var subtotalValue: TextView
    private lateinit var totalAmountValue: TextView
    private lateinit var customerDetailsCard: MaterialCardView
    private lateinit var tvShopName: TextView
    private lateinit var tvContact: TextView
    private lateinit var tvAddress: TextView
    private lateinit var savingsRow: LinearLayout
    private lateinit var savingsValue: TextView

    // Helpers / repos
    private val userRepo = UserRepository()
    private val foodRepo = FoodRepository()
    private val orderRepo = OrderRepository()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var loadingDialog: com.foodordering.krishnafoods.user.util.LoadingDialog

    private var cartItemsWithDetails: List<FoodItem> = emptyList()
    private var customerContact = ""
    private var customerShopName = ""
    private var customerAddress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_order_confirmation)

        window.statusBarColor = ContextCompat.getColor(this, R.color.lightRed)
        supportActionBar?.hide()

        initViews()
        setupWindowInsets()
        setupClicks()

        loadingDialog = com.foodordering.krishnafoods.user.util.LoadingDialog(this)

        // Kickoff - require internet, then load user -> cart -> items
        NetworkHelper.requireInternet(this) { fetchCustomerDataAndCart() }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerViewOrder = findViewById(R.id.recyclerViewOrder)
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder)
        subtotalValue = findViewById(R.id.subtotalValue)
        totalAmountValue = findViewById(R.id.totalAmountValue)
        customerDetailsCard = findViewById(R.id.customerDetailsCard)
        tvShopName = findViewById(R.id.tvShopName)
        tvContact = findViewById(R.id.tvContact)
        tvAddress = findViewById(R.id.tvAddress)
        savingsRow = findViewById(R.id.savingsRow)
        savingsValue = findViewById(R.id.savingsValue)
    }

    private fun setupWindowInsets() {
        val appBarLayout = findViewById<com.google.android.material.appbar.AppBarLayout>(R.id.appBarLayout)
        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, systemBars.top, view.paddingRight, view.paddingBottom)
            insets
        }
    }

    private fun setupClicks() {
        toolbar.setNavigationOnClickListener { finish() }

        btnConfirmOrder.setOnClickListener {
            if (cartItemsWithDetails.isEmpty()) {
                Toast.makeText(this, R.string.cart_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showOrderConfirmationDialog()
        }
    }

    private fun fetchCustomerDataAndCart() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, R.string.user_not_logged_in, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        loadingDialog.show(getString(R.string.loading_message))

        lifecycleScope.launch {
            // 1. fetch user profile
            val userMap = safeCall(onError = { loadingDialog.dismiss() }) {
                userRepo.fetchUserData(user.uid)
            }

            if (userMap == null) {
                loadingDialog.dismiss()
                promptProfileIncomplete()
                return@launch
            }

            customerContact = userMap["contact"] as? String ?: ""
            customerShopName = userMap["shopName"] as? String ?: ""
            customerAddress = userMap["address"] as? String ?: ""

            updateCustomerUI()

            if (customerContact.isEmpty() || customerShopName.isEmpty() || customerAddress.isEmpty()) {
                loadingDialog.dismiss()
                promptProfileIncomplete()
                return@launch
            }

            // 2. load cart items (local manager)
            val localCart = CartManager.getCartItems()
            if (localCart.isEmpty()) {
                loadingDialog.dismiss()
                handleEmptyCart()
                return@launch
            }

            // 3. fetch full food details in bulk
            val ids = localCart.mapNotNull { it.id }.distinct()
            val foods = safeCall(onError = { loadingDialog.dismiss() }) {
                foodRepo.fetchFoodsByIds(ids)
            } ?: emptyList()

            // map and restore quantities from local cart
            val foodsMap = foods.associateBy { it.id }
            val finalList = localCart.mapNotNull { cart ->
                foodsMap[cart.id]?.copy(quantity = cart.quantity)
            }

            if (finalList.isEmpty()) {
                loadingDialog.dismiss()
                handleEmptyCart()
                return@launch
            }

            cartItemsWithDetails = finalList
            loadingDialog.dismiss()
            setupRecyclerView()
            updatePriceSummary()
        }
    }

    private fun updateCustomerUI() {
        tvShopName.text = customerShopName.ifEmpty { getString(R.string.no_data) }
        tvContact.text = customerContact.ifEmpty { getString(R.string.no_data) }
        tvAddress.text = customerAddress.ifEmpty { getString(R.string.no_data) }

        AnimHelper.slideTop(this, customerDetailsCard)
    }

    private fun setupRecyclerView() {
        recyclerViewOrder.layoutManager = LinearLayoutManager(this)
        recyclerViewOrder.adapter = OrderSummaryAdapter(cartItemsWithDetails)
        recyclerViewOrder.isNestedScrollingEnabled = false
    }

    private fun updatePriceSummary() {
        val totals = calculateTotals(cartItemsWithDetails)

        subtotalValue.text = getString(R.string.currency_format, totals.original.toInt())
        if (totals.savings > 0.0) {
            savingsRow.visibility = View.VISIBLE
            savingsValue.text = getString(R.string.savings_format, totals.savings.toInt())
        } else {
            savingsRow.visibility = View.GONE
        }
        totalAmountValue.text = getString(R.string.currency_format, totals.finalAmount.toInt())
    }

    private fun calculateTotals(items: List<FoodItem>): OrderTotals {
        var original = 0.0
        var finalAmt = 0.0

        items.forEach { item ->
            val op = item.originalPrice.toString().toDoubleOrNull() ?: 0.0
            val fp = if (item.offerPrice > 0 && item.offerPrice < op) item.offerPrice.toDouble() else op
            val qty = item.quantity.toDouble()

            original += op * qty
            finalAmt += fp * qty
        }
        return OrderTotals(original, finalAmt, original - finalAmt)
    }

    private fun showOrderConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_order_title)
            .setMessage(R.string.confirm_order_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                placeOrder()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun placeOrder() {
        if (!com.foodordering.krishnafoods.user.util.NetworkUtil.isInternetAvailable(this)) {
            com.foodordering.krishnafoods.user.util.NetworkUtil.showInternetDialog(this) { placeOrder() }
            return
        }

        AnimHelper.click(this, btnConfirmOrder)
        btnConfirmOrder.isEnabled = false
        loadingDialog.show(getString(R.string.placing_order))

        lifecycleScope.launch {
            val totals = calculateTotals(cartItemsWithDetails)
            val user = auth.currentUser
            if (user == null) {
                loadingDialog.dismiss()
                Toast.makeText(this@OrderConfirmationActivity, R.string.user_not_logged_in, Toast.LENGTH_SHORT).show()
                btnConfirmOrder.isEnabled = true
                return@launch
            }

            val orderId = safeCall(onError = {
                loadingDialog.dismiss()
            }) {
                orderRepo.saveOrder(
                    userId = user.uid,
                    contact = customerContact,
                    shopName = customerShopName,
                    address = customerAddress,
                    items = cartItemsWithDetails,
                    totalAmount = totals.finalAmount,
                    status = com.foodordering.krishnafoods.user.viewmodel.OrderStatus.PENDING
                )
            }

            if (orderId == null) {
                // failed
                loadingDialog.dismiss()
                Toast.makeText(this@OrderConfirmationActivity, R.string.order_failed, Toast.LENGTH_SHORT).show()
                btnConfirmOrder.isEnabled = true
                return@launch
            }

            // success
            CartManager.clearCart(this@OrderConfirmationActivity)
            loadingDialog.dismiss()

            startActivity(Intent(this@OrderConfirmationActivity, OrderSuccessActivity::class.java).apply {
                putExtra("orderId", orderId)
                putExtra("totalAmount", totals.finalAmount)
            })
            finish()
        }
    }

    private fun handleEmptyCart() {
        Snackbar.make(recyclerViewOrder, R.string.cart_empty, Snackbar.LENGTH_SHORT).show()
        finish()
    }

    private fun promptProfileIncomplete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.profile_incomplete_title)
            .setMessage(R.string.profile_incomplete)
            .setPositiveButton(R.string.update_profile) { _, _ ->
                startActivity(Intent(this, ProfileSetupActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                finish()
            }
            .setNegativeButton(R.string.cancel) { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::loadingDialog.isInitialized && loadingDialog.isShowing()) {
            loadingDialog.dismiss()
        }
    }
}
