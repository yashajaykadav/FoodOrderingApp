// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.OrderAdapter
import com.foodordering.krishnafoods.admin.viewmodel.DeliveredOrdersViewModel
import com.foodordering.krishnafoods.core.util.EndlessScrollListener
import com.foodordering.krishnafoods.core.util.applyEdgeToEdge
import com.foodordering.krishnafoods.databinding.AdminActivityDeliveredOrdersBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DeliveredOrdersActivity : AppCompatActivity() {

    private lateinit var binding: AdminActivityDeliveredOrdersBinding
    private val viewModel: DeliveredOrdersViewModel by viewModels()
    private lateinit var adapter: OrderAdapter
    private lateinit var scrollListener: EndlessScrollListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminActivityDeliveredOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupUI() {
        // Apply Edge-to-Edge extension
        applyEdgeToEdge(binding.root, binding.toolbarDeliveredOrders)

        // Status Bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        binding.toolbarDeliveredOrders.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)

        // FIX: Removed the invalid "as (Order, ...)" cast.
        // We define the lambda arguments explicitly { order, action, reason -> ... }
        adapter = OrderAdapter(mutableListOf(), this) { order, action, reason ->
            // Delivered orders typically don't require Accept/Reject actions.
            // Leave empty or add logic like "Print Receipt" if needed later.
        }

        // Reuse the EndlessScrollListener module
        scrollListener = EndlessScrollListener(layoutManager) {
            viewModel.fetchOrders(isLoadMore = true)
        }

        binding.recyclerViewDeliveredOrders.apply {
            this.layoutManager = layoutManager
            this.adapter = this@DeliveredOrdersActivity.adapter
            addOnScrollListener(scrollListener)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.orders.collectLatest { orders ->
                // Ensure your Adapter has this method defined
                adapter.updateList(orders)

                if (orders.isNotEmpty()) {
                    binding.tvEmpty.isVisible = false
                    binding.recyclerViewDeliveredOrders.isVisible = true
                } else if (!viewModel.isLoading.value) {
                    binding.tvEmpty.isVisible = true
                    binding.recyclerViewDeliveredOrders.isVisible = false
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                scrollListener.setLoading(isLoading)
                // Only show main loader on initial load (adapter empty), not during pagination
                if (isLoading && adapter.itemCount == 0) {
                    binding.lottieLoading.visibility = View.VISIBLE
                } else {
                    binding.lottieLoading.visibility = View.GONE
                }
            }
        }
    }
}