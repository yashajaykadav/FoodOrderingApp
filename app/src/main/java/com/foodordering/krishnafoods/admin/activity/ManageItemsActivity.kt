// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.FoodItemAdapter
import com.foodordering.krishnafoods.admin.repository.FoodItemsRepository
import com.foodordering.krishnafoods.admin.ui.FoodItemSheetHelper
import com.foodordering.krishnafoods.admin.viewmodel.ManageItemsViewModel
import com.foodordering.krishnafoods.admin.viewmodel.ManageItemsViewModelFactory
import com.foodordering.krishnafoods.databinding.AdminActivityManageItemsBinding
import com.foodordering.krishnafoods.core.util.applyEdgeToEdge
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ManageItemsActivity : AppCompatActivity() {

    private lateinit var binding: AdminActivityManageItemsBinding
    private lateinit var viewModel: ManageItemsViewModel
    private val sheetHelper by lazy { FoodItemSheetHelper(this) }
    private val adapter by lazy {
        FoodItemAdapter { foodItem ->
            sheetHelper.showDetails(
                foodItem,
                onUpdate = { id, orig, offer -> viewModel.updateFoodItemPrice(id, orig, offer) },
                onDelete = { id -> viewModel.deleteFoodItem(id) }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminActivityManageItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupUI()
        setupWindowInsets()
        observeViewModel()
    }

    private fun setupViewModel() {
        val repository = FoodItemsRepository()
        val factory = ManageItemsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ManageItemsViewModel::class.java]
    }

    private fun setupUI() {
        // Apply Color & Theme
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        binding.apply {
            OrderManage.setNavigationOnClickListener { finish() }

            // RecyclerView
            recyclerViewFoodItems.layoutManager = LinearLayoutManager(this@ManageItemsActivity)
            recyclerViewFoodItems.adapter = adapter

            // FAB
            btnAddItem.setOnClickListener {
                startActivity(Intent(this@ManageItemsActivity, AddItemActivity::class.java))
            }

            // Search
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.filterFoodItems(newText.orEmpty())
                    return true
                }
            })

            SwipeRefresh.setOnRefreshListener {
                // ViewModel uses real-time flow, just stop refreshing visually
                SwipeRefresh.isRefreshing = false
            }
        }
    }

    private fun setupWindowInsets() {
        // Use the reusable extension for basic edge-to-edge
        applyEdgeToEdge(binding.coordinatorLayout)

        // Custom logic for FAB and Toolbar specific to this layout
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.coordinatorLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.appBarLayout.updatePadding(top = systemBars.top)

            binding.btnAddItem.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                bottomMargin = systemBars.bottom + resources.getDimensionPixelSize(R.dimen.fab_margin)
                rightMargin = resources.getDimensionPixelSize(R.dimen.fab_margin)
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.filteredFoodItems.collectLatest { items ->
                adapter.submitList(items)
            }
        }
    }
}