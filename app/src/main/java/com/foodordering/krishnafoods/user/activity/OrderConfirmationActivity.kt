// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.user.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
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
import com.foodordering.krishnafoods.user.util.LoadingDialog
import com.foodordering.krishnafoods.user.util.NetworkUtil
import com.foodordering.krishnafoods.user.viewmodel.FoodItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.foodordering.krishnafoods.user.viewmodel.OrderStatus as order

class OrderConfirmationActivity : AppCompatActivity() {

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

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var loadingDialog: LoadingDialog

    private var cartItemsWithDetails = emptyList<FoodItem>()
    private var customerContact = ""
    private var customerShopName = ""
    private var customerAddress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_order_confirmation)

        window.statusBarColor = ContextCompat.getColor(this, R.color.lightRed)
        supportActionBar?.hide()

        initViews()
        setupWindowInsets()
        setupClickListeners()

        loadingDialog = LoadingDialog(this)
        fetchCustomerDetails()
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
            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }
    }

    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener { finish() }

        btnConfirmOrder.setOnClickListener {
            if (cartItemsWithDetails.isNotEmpty()) {
                showOrderConfirmationDialog()
            } else {
                Toast.makeText(this, R.string.cart_empty, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showOrderConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_order_title)
            .setMessage(R.string.confirm_order_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val totalAmount = calculateOrderTotals(cartItemsWithDetails).second
                confirmOrder(cartItemsWithDetails, totalAmount)
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setCancelable(true)
            .show()
    }

    private fun fetchCustomerDetails() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, R.string.user_not_logged_in, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, com.foodordering.krishnafoods.user.activity.LoginActivity::class.java))
            finish()
            return
        }

        if (!NetworkUtil.isInternetAvailable(this)) {
            NetworkUtil.showInternetDialog(this) { fetchCustomerDetails() }
            return
        }

        loadingDialog.show(getString(R.string.loading_message))

        lifecycleScope.launch {
            try {
                val userDoc = db.collection("users").document(user.uid).get().await()

                if (userDoc.exists()) {
                    val userInfo = userDoc.data ?: emptyMap()
                    customerContact = userInfo["contact"] as? String ?: ""
                    customerShopName = userInfo["shopName"] as? String ?: ""
                    customerAddress = userInfo["address"] as? String ?: ""

                    updateCustomerDetailsUI()

                    if (customerContact.isEmpty() || customerShopName.isEmpty() || customerAddress.isEmpty()) {
                        loadingDialog.dismiss()
                        promptProfileIncomplete()
                    } else {
                        fetchCartDetails()
                    }
                } else {
                    loadingDialog.dismiss()
                    promptProfileIncomplete()
                }
            } catch (_: Exception) {
                loadingDialog.dismiss()
                Toast.makeText(this@OrderConfirmationActivity, R.string.error_fetching_profile, Toast.LENGTH_SHORT).show()
                handleCartError()
                finish()
            }
        }
    }

    private fun updateCustomerDetailsUI() {
        tvShopName.text = customerShopName.ifEmpty { getString(R.string.no_data) }
        tvContact.text = customerContact.ifEmpty { getString(R.string.no_data) }
        tvAddress.text = customerAddress.ifEmpty { getString(R.string.no_data) }

        val slideInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_top)
        customerDetailsCard.startAnimation(slideInAnimation)
    }

    private fun fetchCartDetails() {
        loadingDialog.show(getString(R.string.loading_cart))

        lifecycleScope.launch {
            try {
                val incompleteCartItems = CartManager.getCartItems()
                if (incompleteCartItems.isEmpty()) {
                    handleEmptyCart()
                    return@launch
                }

                val fullCartItems = fetchFullItemDetails(incompleteCartItems)

                if (fullCartItems.isEmpty()) {
                    handleEmptyCart()
                    return@launch
                }

                cartItemsWithDetails = fullCartItems
                loadingDialog.dismiss()
                setupRecyclerView()
                updatePriceSummary()

            } catch (_: Exception) {
                loadingDialog.dismiss()
                Toast.makeText(this@OrderConfirmationActivity, R.string.error_loading_cart, Toast.LENGTH_SHORT).show()
                handleCartError()
            }
        }
    }

    private suspend fun fetchFullItemDetails(items: List<FoodItem>): List<FoodItem> {
        val fullItems = mutableListOf<FoodItem>()
        for (cartItem in items) {
            if (cartItem.id.isNotBlank()) {
                try {
                    val foodSnapshot = db.collection("foods").document(cartItem.id).get().await()
                    foodSnapshot.toObject(FoodItem::class.java)?.copy(quantity = cartItem.quantity)?.let {
                        fullItems.add(it)
                    }
                } catch (_: Exception) {
                    // Log error but continue processing other items
                }
            }
        }
        return fullItems
    }

    private fun handleEmptyCart() {
        loadingDialog.dismiss()
        Toast.makeText(this, R.string.cart_empty, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun setupRecyclerView() {
        recyclerViewOrder.layoutManager = LinearLayoutManager(this)
        recyclerViewOrder.adapter = OrderSummaryAdapter(cartItemsWithDetails)
        recyclerViewOrder.isNestedScrollingEnabled = false
    }

    private fun updatePriceSummary() {
        val (totalOriginalPrice, totalAmount, totalSavings) = calculateOrderTotals(cartItemsWithDetails)

        subtotalValue.text = getString(R.string.currency_format, totalOriginalPrice.toInt())

        if (totalSavings > 0) {
            savingsRow.visibility = View.VISIBLE
            savingsValue.text = getString(R.string.savings_format, totalSavings.toInt())
        } else {
            savingsRow.visibility = View.GONE
        }

        totalAmountValue.text = getString(R.string.currency_format, totalAmount.toInt())
    }

    private fun calculateOrderTotals(cartItems: List<FoodItem>): Triple<Double, Double, Double> {
        var totalOriginalPrice = 0.0
        var totalAmount = 0.0

        cartItems.forEach { item ->
            val originalPrice = item.originalPrice.toString().toDoubleOrNull() ?: 0.0
            val offerPrice = if (item.offerPrice > 0 && item.offerPrice < originalPrice)
                item.offerPrice.toDouble() else originalPrice
            val quantity = item.quantity.toDouble()

            totalOriginalPrice += originalPrice * quantity
            totalAmount += offerPrice * quantity
        }
        val totalSavings = totalOriginalPrice - totalAmount
        return Triple(totalOriginalPrice, totalAmount, totalSavings)
    }

    private fun confirmOrder(cartItems: List<FoodItem>, totalAmount: Double) {
        if (!NetworkUtil.isInternetAvailable(this)) {
            NetworkUtil.showInternetDialog(this) { showOrderConfirmationDialog() }
            return
        }

        btnConfirmOrder.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click))
        btnConfirmOrder.isEnabled = false
        loadingDialog.show(getString(R.string.placing_order))

        lifecycleScope.launch {
            try {
                val orderId = saveOrderToFirestore(cartItems, totalAmount)
                CartManager.clearCart(this@OrderConfirmationActivity)
                loadingDialog.dismiss()

                val intent = Intent(this@OrderConfirmationActivity, com.foodordering.krishnafoods.user.activity.OrderSuccessActivity::class.java)
                intent.putExtra("orderId", orderId)
                intent.putExtra("totalAmount", totalAmount)
                startActivity(intent)
                finish()

            } catch (_: Exception) {
                loadingDialog.dismiss()
                Toast.makeText(this@OrderConfirmationActivity, R.string.order_failed, Toast.LENGTH_SHORT).show()
                btnConfirmOrder.isEnabled = true
            }
        }
    }

    private fun handleCartError() {
        Snackbar.make(recyclerViewOrder, R.string.error_loading_cart, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.retry) { fetchCartDetails() }
            .show()
    }

    private suspend fun saveOrderToFirestore(cartItems: List<FoodItem>, totalAmount: Double): String {
        val user = auth.currentUser ?: throw IllegalStateException("User cannot be null when saving an order")

        val orderData = hashMapOf(
            "userId" to user.uid,
            "contact" to customerContact,
            "shopName" to customerShopName,
            "address" to customerAddress,
            "items" to cartItems.map {
                mapOf(
                    "id" to it.id,
                    "name" to it.name,
                    "originalPrice" to it.originalPrice,
                    "offerPrice" to it.offerPrice,
                    "quantity" to it.quantity,
                    "weight" to it.weight
                )
            },
            "totalAmount" to totalAmount,
            "orderDate" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            "status" to order.PENDING
        )

        val documentReference = db.collection("orders").add(orderData).await()
        return documentReference.id
    }

    private fun promptProfileIncomplete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.profile_incomplete_title)
            .setMessage(R.string.profile_incomplete)
            .setPositiveButton(R.string.update_profile) { _, _ ->
                startActivity(Intent(this, com.foodordering.krishnafoods.user.activity.ProfileSetupActivity::class.java).apply {
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