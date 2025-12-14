// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.user.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.ActivityOrderConfirmationBinding
import com.foodordering.krishnafoods.user.adapter.OrderSummaryAdapter
import com.foodordering.krishnafoods.user.manager.CartManager
import com.foodordering.krishnafoods.user.repository.FoodRepository
import com.foodordering.krishnafoods.user.repository.OrderRepository
import com.foodordering.krishnafoods.user.repository.UserRepository
import com.foodordering.krishnafoods.user.util.*
import com.foodordering.krishnafoods.user.viewmodel.FoodItem
import com.foodordering.krishnafoods.user.viewmodel.OrderStatus
import com.foodordering.krishnafoods.user.viewmodel.OrderTotals
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class OrderConfirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderConfirmationBinding
    private lateinit var loadingDialog: LoadingDialog

    // Repositories
    private val userRepo = UserRepository()
    private val foodRepo = FoodRepository()
    private val orderRepo = OrderRepository()
    private val auth = FirebaseAuth.getInstance()

    // Data
    private var cartItemsWithDetails: List<FoodItem> = emptyList()
    private var customerContact = ""
    private var customerShopName = ""
    private var customerAddress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityOrderConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.lightRed)
        supportActionBar?.hide()

        loadingDialog = LoadingDialog(this)

        setupWindowInsets()
        setupClicks()

        NetworkHelper.requireInternet(this) { fetchCustomerDataAndCart() }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, systemBars.top, view.paddingRight, view.paddingBottom)
            insets
        }
    }

    private fun setupClicks() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnConfirmOrder.setOnClickListener {
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
            // 1. Fetch User Profile
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

            // 2. Load Local Cart
            val localCart = CartManager.getCartItems()
            if (localCart.isEmpty()) {
                loadingDialog.dismiss()
                handleEmptyCart()
                return@launch
            }

            // 3. Fetch Fresh Food Details
            val ids = localCart.mapNotNull { it.id }.distinct()
            val foods = safeCall(onError = { loadingDialog.dismiss() }) {
                foodRepo.fetchFoodsByIds(ids)
            } ?: emptyList()

            // Merge details with local quantities
            val foodsMap = foods.associateBy { it.id }
            val finalList = localCart.mapNotNull { cartItem ->
                foodsMap[cartItem.id]?.copy(quantity = cartItem.quantity)
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
        binding.tvShopName.text = customerShopName.ifEmpty { getString(R.string.no_data) }
        binding.tvContact.text = customerContact.ifEmpty { getString(R.string.no_data) }
        binding.tvAddress.text = customerAddress.ifEmpty { getString(R.string.no_data) }

        AnimHelper.slideTop(this, binding.customerDetailsCard)
    }

    private fun setupRecyclerView() {
        binding.recyclerViewOrder.apply {
            layoutManager = LinearLayoutManager(this@OrderConfirmationActivity)
            adapter = OrderSummaryAdapter(cartItemsWithDetails)
            isNestedScrollingEnabled = false
        }
    }

    private fun updatePriceSummary() {
        val totals = calculateTotals(cartItemsWithDetails)

        binding.subtotalValue.text = getString(R.string.currency_format, totals.original.toInt())

        if (totals.savings > 0.0) {
            binding.savingsRow.visibility = View.VISIBLE
            binding.savingsValue.text = getString(R.string.savings_format, totals.savings.toInt())
        } else {
            binding.savingsRow.visibility = View.GONE
        }

        binding.totalAmountValue.text = getString(R.string.currency_format, totals.finalAmount.toInt())
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
            .setPositiveButton(R.string.confirm) { _, _ -> placeOrder() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun placeOrder() {
        if (!NetworkUtil.isInternetAvailable(this)) {
            NetworkUtil.showInternetDialog(this) { placeOrder() }
            return
        }

        AnimHelper.click(this, binding.btnConfirmOrder)
        binding.btnConfirmOrder.isEnabled = false
        loadingDialog.show(getString(R.string.placing_order))

        lifecycleScope.launch {
            val totals = calculateTotals(cartItemsWithDetails)
            val user = auth.currentUser

            if (user == null) {
                loadingDialog.dismiss()
                Toast.makeText(this@OrderConfirmationActivity, R.string.user_not_logged_in, Toast.LENGTH_SHORT).show()
                binding.btnConfirmOrder.isEnabled = true
                return@launch
            }

            val orderId = safeCall(onError = { loadingDialog.dismiss() }) {
                orderRepo.saveOrder(
                    userId = user.uid,
                    contact = customerContact,
                    shopName = customerShopName,
                    address = customerAddress,
                    items = cartItemsWithDetails,
                    totalAmount = totals.finalAmount,
                    status = OrderStatus.PENDING
                )
            }

            if (orderId == null) {
                loadingDialog.dismiss()
                Toast.makeText(this@OrderConfirmationActivity, R.string.order_failed, Toast.LENGTH_SHORT).show()
                binding.btnConfirmOrder.isEnabled = true
                return@launch
            }

            // Success
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
        Snackbar.make(binding.recyclerViewOrder, R.string.cart_empty, Snackbar.LENGTH_SHORT).show()
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