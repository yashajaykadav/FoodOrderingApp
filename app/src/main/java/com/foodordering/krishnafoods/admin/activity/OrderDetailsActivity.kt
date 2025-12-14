// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.OrderItemAdapter
import com.foodordering.krishnafoods.admin.model.Order
import com.foodordering.krishnafoods.admin.util.formatToReadableDate
import com.foodordering.krishnafoods.admin.util.setOrderStatusColor
import com.foodordering.krishnafoods.admin.viewmodel.OrderDetailsViewModel
import com.foodordering.krishnafoods.core.util.applyEdgeToEdge
import com.foodordering.krishnafoods.databinding.AdminActivityOrderDetailsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var binding: AdminActivityOrderDetailsBinding
    private val viewModel: OrderDetailsViewModel by viewModels()
    private var currentOrder: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receive Order Object Safely
        currentOrder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("order_data", Order::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("order_data")
        }

        if (currentOrder == null) {
            Toast.makeText(this, "Error loading order details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        populateData(currentOrder!!)
        observeViewModel()
    }

    private fun setupUI() {
        applyEdgeToEdge(binding.root, binding.toolbar)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnUpdateStatus.setOnClickListener { showStatusUpdateDialog() }
    }

    private fun populateData(order: Order) {
        binding.apply {
            tvShopName.text = order.shopName
            tvAddress.text = order.address
            tvContact.text = order.contact
            tvTotalAmount.text = order.formattedTotal
            tvStatus.text = order.status
            tvDate.text = order.orderDate.formatToReadableDate()

            // Reuse the extension for status colors
            statusCard.setOrderStatusColor(order.status, tvStatus)

            // Setup Items Recycler
            recyclerViewItems.layoutManager = LinearLayoutManager(this@OrderDetailsActivity)
            // Reuse standard models -> No need for custom parsing here
            recyclerViewItems.adapter = OrderItemAdapter(order.items)

            // Hide update button if already Delivered/Rejected
            if (order.isDelivered() || order.isRejected()) {
                btnUpdateStatus.visibility = View.GONE
            }
        }
    }

    private fun showStatusUpdateDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_status, null)
        val etReason = dialogView.findViewById<EditText>(R.id.etReason)

        AlertDialog.Builder(this)
            .setTitle("Update Order Status")
            .setView(dialogView)
            .setPositiveButton("Accept") { _, _ ->
                currentOrder?.let { viewModel.updateOrderStatus(it.orderId, "Accepted", null) }
            }
            .setNegativeButton("Reject") { _, _ ->
                val reason = etReason.text.toString()
                if (reason.isBlank()) {
                    Toast.makeText(this, "Rejection reason required", Toast.LENGTH_SHORT).show()
                } else {
                    currentOrder?.let { viewModel.updateOrderStatus(it.orderId, "Rejected", reason) }
                }
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.statusUpdateResult.collectLatest { result ->
                result?.onSuccess { status ->
                    Toast.makeText(this@OrderDetailsActivity, "Status updated to $status", Toast.LENGTH_SHORT).show()
                    binding.tvStatus.text = status

                    // Update visual color immediately
                    binding.statusCard.setOrderStatusColor(status, binding.tvStatus)

                    if (status == "Rejected" || status == "Delivered") {
                        binding.btnUpdateStatus.visibility = View.GONE
                    }
                }?.onFailure {
                    Toast.makeText(this@OrderDetailsActivity, "Update Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.btnUpdateStatus.isEnabled = !isLoading
            }
        }
    }
}