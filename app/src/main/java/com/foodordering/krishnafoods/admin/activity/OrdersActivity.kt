// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.OrderAdapter
import com.foodordering.krishnafoods.admin.viewmodel.AllOrdersViewModel
import com.foodordering.krishnafoods.core.util.applyEdgeToEdge
import com.foodordering.krishnafoods.databinding.AdminActivityOrdersBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OrdersActivity : AppCompatActivity() {

    // ViewBinding
    private lateinit var binding: AdminActivityOrdersBinding
    private val viewModel: AllOrdersViewModel by viewModels()
    private lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        setupSearchAndFilter()

        // Initial Load (Select 'All' by default)
        binding.chipGroupFilter.check(R.id.chipAll)
        viewModel.setStatusFilter(null)

        observeViewModel()
    }

    private fun setupUI() {
        applyEdgeToEdge(binding.root, binding.toolbarOrders)

        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        binding.toolbarOrders.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        // FIX 1: Pass mutableListOf() instead of undefined 'orderList'
        adapter = OrderAdapter(mutableListOf(), this) { order, action, reason ->
            // Move logic to ViewModel
            viewModel.updateOrderStatus(order, action, reason) { success ->
                if (success) {
                    Toast.makeText(this, "Order Updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.recyclerViewOrders.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewOrders.adapter = adapter
    }

    private fun setupSearchAndFilter() {
        // 1. Chip Filtering
        binding.chipGroupFilter.setOnCheckedChangeListener { _, checkedId ->
            val status = when (checkedId) {
                R.id.chipPending -> "Pending"
                R.id.chipAccepted -> "Accepted"
                R.id.chipRejected -> "Rejected"
                else -> null // All
            }
            viewModel.setStatusFilter(status)
        }

        // 2. Search Filtering
        binding.searchViewOrders.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiOrders.collectLatest { orders ->
                // FIX 2: Use correct method name 'updateList'
                adapter.updateList(orders)

                // Toggle Empty State
                if (orders.isEmpty()) {
                    binding.emptyStateView.visibility = View.VISIBLE
                    binding.recyclerViewOrders.visibility = View.GONE
                } else {
                    binding.emptyStateView.visibility = View.GONE
                    binding.recyclerViewOrders.visibility = View.VISIBLE
                }
            }
        }
    }
}