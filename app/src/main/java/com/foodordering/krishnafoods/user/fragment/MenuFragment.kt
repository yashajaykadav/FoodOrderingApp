/*
 * Author: Yash Kadav
 * Email: yashkadav52@gmail.com
 * ADCET CSE 2026
 */

package com.foodordering.krishnafoods.user.fragment

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.UserFragmentMenuBinding
import com.foodordering.krishnafoods.user.adapter.BannerAdapter
import com.foodordering.krishnafoods.user.adapter.FoodAdapter
import com.foodordering.krishnafoods.user.manager.BannerManager
import com.foodordering.krishnafoods.user.manager.CartManager
import com.foodordering.krishnafoods.user.util.GridSpacingItemDecoration
import com.foodordering.krishnafoods.user.util.fadeIn
import com.foodordering.krishnafoods.user.util.slideDownAndHide
import com.foodordering.krishnafoods.user.util.slideUpAndShow
import com.foodordering.krishnafoods.user.viewmodel.FoodItem
import com.foodordering.krishnafoods.user.viewmodel.FoodViewModel
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.firebase.firestore.FirebaseFirestore

@ExperimentalBadgeUtils
class MenuFragment : Fragment() {

    private companion object {
        const val CART_TAB_INDEX = 1
        const val GRID_SPAN = 3
        const val GRID_SPACING = 8
    }

    private var _binding: UserFragmentMenuBinding? = null
    private val binding get() = _binding!!

    // External Logic Managers
    private lateinit var bannerManager: BannerManager
    private lateinit var foodAdapter: FoodAdapter

    // UI State
    private var cartBadge: BadgeDrawable? = null
    private var selectedCategory: String? = null

    private val foodViewModel: FoodViewModel by viewModels()

    // ---------------------------
    // Lifecycle
    // ---------------------------

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = UserFragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Manager
        bannerManager = BannerManager(binding.heroViewPager, viewLifecycleOwner.lifecycleScope)

        setupUI()
        setupObservers()
        loadData()
    }

    override fun onStart() {
        super.onStart()
        foodViewModel.startListeningToFoodItems()
    }

    override fun onResume() {
        super.onResume()
        bannerManager.startAutoScroll()
        updateCartUI()
    }

    override fun onPause() {
        super.onPause()
        bannerManager.stopAutoScroll()
    }

    override fun onStop() {
        super.onStop()
        foodViewModel.stopListening()
    }

    override fun onDestroyView() {
        try {
            cartBadge?.let { BadgeUtils.detachBadgeDrawable(it, binding.viewCartButton) }
        } catch (e: Exception) { /* Safe detach */ }

        binding.recyclerView.adapter = null
        binding.heroViewPager.adapter = null
        _binding = null
        super.onDestroyView()
    }

    // ---------------------------
    // UI Setup
    // ---------------------------

    private fun setupUI() {
        // RecyclerView Setup
        foodAdapter = FoodAdapter(mutableListOf()) { foodItem ->
            showFoodDetailDialog(foodItem)
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), GRID_SPAN)
            adapter = foodAdapter
            addItemDecoration(GridSpacingItemDecoration(GRID_SPAN, GRID_SPACING, true))
        }

        // Click Listeners
        binding.viewCartButton.setOnClickListener { navigateToCart() }

        // Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, sysBars.top, 0, 0)
            insets
        }
    }

    private fun setupObservers() {
        foodViewModel.foodItems.observe(viewLifecycleOwner) { foodList ->
            showLoading(false)
            if (foodList.isEmpty()) {
                showEmptyState(true)
            } else {
                showEmptyState(false)
                foodAdapter.updateList(foodList)
                // Re-apply filter if category was selected
                filterFoodByCategory(selectedCategory)
            }
        }

        foodViewModel.categories.observe(viewLifecycleOwner) { categories ->
            setupCategoryButtons(categories)
        }
    }

    // ---------------------------
    // Data Loading
    // ---------------------------

    private fun loadData() {
        showLoading(true)
        foodViewModel.fetchCategories()
        fetchBanners()
    }

    private fun fetchBanners() {
        if (!isNetworkAvailable()) {
            binding.heroViewPager.isVisible = false
            return
        }

        FirebaseFirestore.getInstance().collection("advertisements")
            .whereEqualTo("isActive", true)
            .orderBy("timestamp")
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                val urls = result.documents.mapNotNull { it.getString("imageUrl") }
                if (urls.isNotEmpty()) {
                    val adapter = BannerAdapter { /* Handle banner click if needed */ }
                    binding.heroViewPager.adapter = adapter
                    adapter.submitList(urls)
                    binding.dotsIndicator.setViewPager2(binding.heroViewPager)

                    // Delegate complex logic to BannerManager
                    bannerManager.setupTransformer()
                    bannerManager.attachPageCallback()
                    bannerManager.startAutoScroll()

                    binding.heroViewPager.fadeIn() // Using Extension
                } else {
                    binding.heroViewPager.isVisible = false
                }
            }
            .addOnFailureListener { binding.heroViewPager.isVisible = false }
    }

    // ---------------------------
    // Category Logic
    // ---------------------------

    private fun setupCategoryButtons(categories: List<String>) {
        binding.categoryContainer.removeAllViews()
        selectedCategory = null

        // Add "All" button
        binding.categoryContainer.addView(createCategoryButton("All"))

        // Add dynamic buttons
        categories.forEach { category ->
            binding.categoryContainer.addView(createCategoryButton(category))
        }
    }

    private fun createCategoryButton(category: String): Button {
        return Button(requireContext()).apply {
            text = category
            textSize = 14f
            setPadding(16, 8, 16, 8)

            // Layout params with margin
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = 12 }

            // Initial Style
            val isSelected = category == "All" && selectedCategory == null
            applyButtonStyle(this, isSelected)

            setOnClickListener { onCategorySelected(category) }
        }
    }

    private fun onCategorySelected(category: String) {
        selectedCategory = if (category == "All") null else category

        // Loop through all buttons to update styles
        for (i in 0 until binding.categoryContainer.childCount) {
            val button = binding.categoryContainer.getChildAt(i) as? Button ?: continue
            val isBtnSelected = button.text == category
            applyButtonStyle(button, isBtnSelected)
        }

        filterFoodByCategory(selectedCategory)
    }

    private fun applyButtonStyle(button: Button, isSelected: Boolean) {
        val bgRes = if (isSelected) R.drawable.category_selected_bg else R.drawable.category_unselected_bg
        val textColorRes = if (isSelected) R.color.white else R.color.colorAccent

        button.setBackgroundResource(bgRes)
        button.setTextColor(ContextCompat.getColor(requireContext(), textColorRes))
    }

    private fun filterFoodByCategory(category: String?) {
        val allItems = foodViewModel.foodItems.value ?: return
        val filtered = if (category == null) allItems else allItems.filter { it.category == category }

        foodAdapter.updateList(filtered)
        binding.recyclerView.fadeIn() // Using Extension
        showEmptyState(filtered.isEmpty())
    }

    // ---------------------------
    // Cart & Navigation
    // ---------------------------

    private fun updateCartUI() {
        val count = CartManager.getCartItems().size

        // Initialize Badge if null
        if (cartBadge == null) {
            cartBadge = BadgeDrawable.create(requireContext()).apply {
                badgeGravity = BadgeDrawable.TOP_END
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.red)
                isVisible = true
            }
            BadgeUtils.attachBadgeDrawable(cartBadge!!, binding.viewCartButton)
        }

        cartBadge?.number = count
        cartBadge?.isVisible = count > 0

        // Use Extension functions for smooth visibility
        if (count > 0) binding.viewCartButton.slideUpAndShow()
        else binding.viewCartButton.slideDownAndHide()
    }

    private fun navigateToCart() {
        activity?.findViewById<ViewPager2>(R.id.viewPager)?.setCurrentItem(CART_TAB_INDEX, true)
    }

    private fun showFoodDetailDialog(foodItem: FoodItem) {
        FoodDetailDialogFragment(foodItem) { updateCartUI() }
            .apply { setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialogTheme) }
            .show(parentFragmentManager, "FoodDetailDialogFragment")
    }

    // ---------------------------
    // Utilities
    // ---------------------------

    private fun showLoading(isLoading: Boolean) {
        binding.loadingAnimation.isVisible = isLoading
        if (isLoading) {
            binding.recyclerView.isVisible = false
            binding.emptyStateView.isVisible = false
        } else {
            binding.loadingAnimation.cancelAnimation()
            updateCartUI()
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.emptyStateView.isVisible = isEmpty
        binding.recyclerView.isVisible = !isEmpty
        if (!isEmpty) updateCartUI()
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}