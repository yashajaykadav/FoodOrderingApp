package com.foodordering.krishnafoods.admin.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.FoodItemAdapter
import com.foodordering.krishnafoods.admin.model.FoodItem
import com.foodordering.krishnafoods.admin.repository.FoodItemsRepository
import com.foodordering.krishnafoods.admin.viewmodel.ManageItemsViewModel
import com.foodordering.krishnafoods.admin.viewmodel.ManageItemsViewModelFactory
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ManageItemsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FoodItemAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var searchView: SearchView
    private lateinit var btnAddItem: ExtendedFloatingActionButton
    private lateinit var toolbar: MaterialToolbar
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var coordinatorLayout: CoordinatorLayout

    private lateinit var viewModel: ManageItemsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.admin_activity_manage_items)
        supportActionBar?.hide()

        // Set status bar color to colorAccent
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)

        // Set status bar icons to light color (use true if colorAccent is light)
        WindowCompat.getInsetsController(window, window.decorView)?.apply {
            isAppearanceLightStatusBars = false
        }

        val repository = FoodItemsRepository()
        viewModel = ViewModelProvider(this, ManageItemsViewModelFactory(repository))[ManageItemsViewModel::class.java]

        initViews()
        setupWindowInsets()
        setupListeners()
        setupRecyclerView()
        observeViewModel()
    }

    private fun initViews() {
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        appBarLayout = findViewById(R.id.appBarLayout)
        toolbar = findViewById(R.id.OrderManage)
        recyclerView = findViewById(R.id.recyclerViewFoodItems)
        swipeRefreshLayout = findViewById(R.id.SwipeRefresh)
        searchView = findViewById(R.id.searchView)
        btnAddItem = findViewById(R.id.btnAddItem)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(coordinatorLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply top padding to AppBarLayout for status bar
            appBarLayout.updatePadding(top = systemBars.top)

            // Apply bottom margin to FAB for navigation bar
            val fabParams = btnAddItem.layoutParams as CoordinatorLayout.LayoutParams
            fabParams.bottomMargin = systemBars.bottom + resources.getDimensionPixelSize(R.dimen.fab_margin)
            fabParams.rightMargin = resources.getDimensionPixelSize(R.dimen.fab_margin)
            btnAddItem.layoutParams = fabParams

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener { finish() }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterFoodItems(newText.orEmpty())
                return true
            }
        })

        swipeRefreshLayout.setOnRefreshListener {
            // The ViewModel's Flow handles real-time updates, so we can just stop the spinner.
            swipeRefreshLayout.isRefreshing = false
        }

        btnAddItem.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = FoodItemAdapter { foodItem ->
            showFoodDetailsBottomSheet(foodItem)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.filteredFoodItems.collect { items ->
                adapter.submitList(items)
            }
        }
    }

    private fun showFoodDetailsBottomSheet(foodItem: FoodItem) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_food_details, null)

        val tvName = view.findViewById<TextView>(R.id.tvFoodName)
        val etOriginalPrice = view.findViewById<EditText>(R.id.etOriginalPrice)
        val etOfferPrice = view.findViewById<EditText>(R.id.etOfferPrice)
        val etWeight = view.findViewById<EditText>(R.id.etFoodWeight)
        val etCategory = view.findViewById<EditText>(R.id.etFoodCategory)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdateFood)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        val btnDelete = view.findViewById<Button>(R.id.btnDeleteFood)

        tvName.text = foodItem.name
        etOriginalPrice.setText(foodItem.originalPrice.toString())
        etOfferPrice.setText(foodItem.offerPrice?.toString() ?: "")
        etWeight.setText(foodItem.weight)
        etCategory.setText(foodItem.category)

        etWeight.isEnabled = false
        etCategory.isEnabled = false

        btnUpdate.setOnClickListener {
            val newOriginalPrice = etOriginalPrice.text.toString().toIntOrNull()
            val newOfferPrice = etOfferPrice.text.toString().toIntOrNull()

            if (newOriginalPrice == null || newOriginalPrice <= 0) {
                Toast.makeText(this, "Original price must be a positive number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newOfferPrice != null && newOfferPrice >= newOriginalPrice) {
                Toast.makeText(this, "Offer price must be less than original price", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateFoodItemPrice(foodItem.id, newOriginalPrice, newOfferPrice)
            bottomSheetDialog.dismiss()
        }

        btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete '${foodItem.name}'?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.deleteFoodItem(foodItem.id)
                    bottomSheetDialog.dismiss()
                }
                .setNegativeButton("No", null)
                .show()
        }

        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }
}