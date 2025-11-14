package com.foodordering.krishnafoods.user.fragment

import android.content.Context
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.user.adapter.BannerAdapter
import com.foodordering.krishnafoods.user.adapter.FoodAdapter
import com.foodordering.krishnafoods.user.manager.CartManager
import com.foodordering.krishnafoods.user.viewmodel.FoodItem
import com.foodordering.krishnafoods.user.viewmodel.FoodViewModel
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import kotlinx.coroutines.*
import androidx.core.view.isVisible

@ExperimentalBadgeUtils
class MenuFragment : Fragment() {

    private companion object {
        const val GRID_SPAN_COUNT = 4
        const val GRID_SPACING_DP = 8
        const val AUTO_SCROLL_INTERVAL_MS = 3000L
        const val BANNER_LIMIT = 10
        const val ITEM_FADE_DURATION_MS = 200L
        const val CART_TAB_INDEX = 1
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var foodAdapter: FoodAdapter
    private lateinit var categoryContainer: LinearLayout
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var emptyStateView: LinearLayout
    private lateinit var viewCartButton: MaterialButton
    private lateinit var heroViewPager: ViewPager2
    private lateinit var dotsIndicator: DotsIndicator

    private var bannerAdapter: BannerAdapter? = null
    private var cartBadge: BadgeDrawable? = null

    // ViewModel and Coroutines
    private val foodViewModel: FoodViewModel by viewModels()
    private var autoScrollJob: Job? = null
    private var selectedCategory: String? = null

    // Animation listener reference for cleanup
    private var currentAnimationListener: Animation.AnimationListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.user_fragment_menu, container, false)
        initializeViews(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupCartBadge()
        setupViewCartButton()
        setupEdgeToEdgeHandling()
//        setupScrollListener()
        heroViewPager.isNestedScrollingEnabled = false
        heroViewPager.getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER
        showLoading(true)
        loadInitialData()
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        categoryContainer = view.findViewById(R.id.categoryContainer)
        loadingAnimation = view.findViewById(R.id.loadingAnimation)
        emptyStateView = view.findViewById(R.id.emptyStateView)
        viewCartButton = view.findViewById(R.id.viewCartButton)
        heroViewPager = view.findViewById(R.id.heroViewPager)

        dotsIndicator = view.findViewById(R.id.dotsIndicator)
    }

    private fun loadInitialData() {
        foodViewModel.startListeningToFoodItems()
        foodViewModel.fetchCategories()
        fetchBannerImages()
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(mutableListOf()) { foodItem ->
            showFoodDetailDialog(foodItem)
        }

        recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), GRID_SPAN_COUNT)
            adapter = foodAdapter
            addItemDecoration(
                GridSpacingItemDecoration(GRID_SPAN_COUNT, GRID_SPACING_DP, includeEdge = true)
            )
        }
    }

//    private fun setupScrollListener() {
//        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                val shouldHide = recyclerView.computeVerticalScrollOffset() > SCROLL_THRESHOLD
//                toggleBannerVisibility(!shouldHide)
//            }
//        })
//    }

    private fun setupObservers() {
        foodViewModel.foodItems.observe(viewLifecycleOwner) { foodList ->
            showLoading(false)
            handleFoodListUpdate(foodList)
        }

        foodViewModel.categories.observe(viewLifecycleOwner) { categories ->
            updateCategoryButtons(categories)
        }
    }

    private fun handleFoodListUpdate(foodList: List<FoodItem>) {
        if (foodList.isEmpty()) {
            showEmptyState(true)
        } else {
            showEmptyState(false)
            foodAdapter.updateList(foodList)
            filterFoodByCategory(selectedCategory)
        }
    }

    private fun updateCategoryButtons(categories: List<String>) {
        categoryContainer.removeAllViews()
        selectedCategory = null

        // Add "All" button
        categoryContainer.addView(createCategoryButton("All"))

        // Add category buttons
        categories.forEach { category ->
            categoryContainer.addView(createCategoryButton(category))
        }
    }

    private fun setupCartBadge() {
        try {
            cartBadge = BadgeDrawable.create(requireContext()).apply {
                badgeGravity = BadgeDrawable.TOP_END
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.red)
                maxCharacterCount = 3
                isVisible = false
            }

            cartBadge?.let { badge ->
                // Now passing the button as both view and anchor
                BadgeUtils.attachBadgeDrawable(badge, viewCartButton)
            }
        } catch (e: Exception) {
            // Handle badge creation failure gracefully
            e.printStackTrace()
        }
    }

    private fun setupEdgeToEdgeHandling() {
        val initialMarginBottom = (viewCartButton.layoutParams as? ViewGroup.MarginLayoutParams)
            ?.bottomMargin ?: 0

        ViewCompat.setOnApplyWindowInsetsListener(viewCartButton) { _, insets ->
            val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            viewCartButton.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = initialMarginBottom + navBarInsets.bottom
            }
            insets
        }
    }

    private fun setupViewCartButton() {
        updateCartBadgeAndButton()
        viewCartButton.setOnClickListener {
            navigateToCart()
        }
    }

    private fun navigateToCart() {
        activity?.findViewById<ViewPager2>(R.id.viewPager)?.apply {
            setCurrentItem(CART_TAB_INDEX, true)
        }
    }

    private fun updateCartBadgeAndButton() {
        val cartItems = CartManager.getCartItems()
        val cartSize = cartItems.size

        updateBadge(cartSize)
        updateButtonVisibility(cartSize > 0)
    }

    private fun updateBadge(count: Int) {
        cartBadge?.let { badge ->
            if (count > 0) {
                badge.number = count
                badge.isVisible = true
                viewCartButton.announceForAccessibility("Cart has $count items")
            } else {
                badge.isVisible = false
            }
        }
    }

    private fun updateButtonVisibility(shouldBeVisible: Boolean) {
        val isCurrentlyVisible = viewCartButton.isVisible

        when {
            shouldBeVisible && !isCurrentlyVisible -> animateButtonIn()
            !shouldBeVisible && isCurrentlyVisible -> animateButtonOut()
        }
    }

    private fun animateButtonIn() {
        context?.let { ctx ->
            val slideUp = AnimationUtils.loadAnimation(ctx, R.anim.slide_up)
            viewCartButton.startAnimation(slideUp)
            viewCartButton.visibility = View.VISIBLE
        }
    }

    private fun animateButtonOut() {
        context?.let { ctx ->
            val slideDown = AnimationUtils.loadAnimation(ctx, R.anim.slide_down)
            currentAnimationListener = null
            currentAnimationListener = object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    if (isAdded) {
                        viewCartButton.visibility = View.GONE
                    }
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            }
            slideDown.setAnimationListener(currentAnimationListener)
            viewCartButton.startAnimation(slideDown)
        }
    }
//
//    private fun toggleBannerVisibility(show: Boolean) {
//        heroCarouselContainer.animate()
//            .alpha(if (show) 1f else 0f)
//            .setDuration(FADE_DURATION_MS)
//            .withEndAction {
//                if (isAdded) {
//                    heroCarouselContainer.visibility = if (show) View.VISIBLE else View.GONE
//                }
//            }
//    }

    private fun fetchBannerImages() {
        if (!isNetworkAvailable()) {
            heroViewPager.visibility = View.GONE
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            heroViewPager.visibility = View.GONE
            return
        }

        FirebaseFirestore.getInstance()
            .collection("advertisements")
            .whereEqualTo("isActive", true)
            .orderBy("timestamp")
            .limit(BANNER_LIMIT.toLong())
            .get()
            .addOnSuccessListener { result ->
                handleBannerSuccess(result.documents.mapNotNull { doc ->
                    doc.getString("imageUrl")?.takeIf { it.isNotBlank() }
                })
            }
            .addOnFailureListener { exception ->
                handleBannerFailure(exception)
            }
    }

    private fun handleBannerSuccess(imageUrls: List<String>) {
        if (!isAdded) return
        if (imageUrls.isNotEmpty()) {
            bannerAdapter = BannerAdapter(imageUrls)
            heroViewPager.adapter = bannerAdapter
            dotsIndicator.setViewPager2(heroViewPager)
            heroViewPager.visibility = View.VISIBLE
            startAutoScroll()
        } else {
            heroViewPager.visibility = View.GONE
        }
    }

    private fun handleBannerFailure(exception: Exception) {
        if (!isAdded) return
        heroViewPager.visibility = View.GONE
        exception.printStackTrace()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val activeNetwork = connectivityManager.activeNetworkInfo
            activeNetwork != null && activeNetwork.isConnected
        }
    }

    private fun showLoading(show: Boolean) {
        loadingAnimation.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            loadingAnimation.playAnimation()
            recyclerView.visibility = View.GONE
            emptyStateView.visibility = View.GONE
            viewCartButton.visibility = View.GONE
        } else {
            loadingAnimation.cancelAnimation()
            updateCartBadgeAndButton()
        }
    }

    private fun showEmptyState(show: Boolean) {
        emptyStateView.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
        if (!show) {
            updateCartBadgeAndButton()
        }
    }

    private fun createCategoryButton(category: String): Button {
        return Button(requireContext()).apply {
            text = category
            contentDescription = "Select $category category"
            textSize = 14f
            setPadding(16, 8, 16, 8)
            val isSelected = category == selectedCategory || (category == "All" && selectedCategory == null)
            applyButtonStyle(isSelected)
            setOnClickListener {
                onCategorySelected(category)
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 12
            }
        }
    }

    private fun Button.applyButtonStyle(isSelected: Boolean) {
        setBackgroundResource(
            if (isSelected) R.drawable.category_selected_bg
            else R.drawable.category_unselected_bg
        )
        setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isSelected) R.color.white else R.color.colorAccent
            )
        )
    }

    private fun onCategorySelected(category: String) {
        selectedCategory = if (category == "All") null else category
        highlightSelectedCategory(category)
        filterFoodByCategory(selectedCategory)
    }

    private fun filterFoodByCategory(category: String?) {
        val allItems = foodViewModel.foodItems.value ?: return
        val filtered = if (category == null) {
            allItems
        } else {
            allItems.filter { it.category == category }
        }
        foodAdapter.updateList(filtered)
        animateRecyclerView()
        showEmptyState(filtered.isEmpty())
    }

    private fun animateRecyclerView() {
        recyclerView.apply {
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(ITEM_FADE_DURATION_MS)
                .start()
        }
    }

    private fun highlightSelectedCategory(category: String) {
        for (i in 0 until categoryContainer.childCount) {
            val button = categoryContainer.getChildAt(i) as? Button ?: continue
            val isSelected = button.text == category
            button.applyButtonStyle(isSelected)
        }
    }

    private fun showFoodDetailDialog(foodItem: FoodItem) {
        val dialog = FoodDetailDialogFragment(
            foodItem = foodItem,
            onCartUpdated = {
                updateCartBadgeAndButton()
            }
        )
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialogTheme)
        dialog.show(parentFragmentManager, "FoodDetailDialogFragment")
    }

    private fun startAutoScroll() {
        stopAutoScroll()
        autoScrollJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                delay(AUTO_SCROLL_INTERVAL_MS)
                scrollToNextBanner()
            }
        }
    }

    private fun scrollToNextBanner() {
        if (!isAdded) return
        bannerAdapter?.let { adapter ->
            if (adapter.itemCount > 1) {
                val nextItem = (heroViewPager.currentItem + 1) % adapter.itemCount
                heroViewPager.setCurrentItem(nextItem, true)
            }
        }
    }

    private fun stopAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = null
    }

    override fun onStart() {
        super.onStart()
        foodViewModel.startListeningToFoodItems()
    }

    override fun onResume() {
        super.onResume()
        startAutoScroll()
        updateCartBadgeAndButton()
    }

    override fun onPause() {
        super.onPause()
        stopAutoScroll()
    }

    override fun onStop() {
        super.onStop()
        foodViewModel.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cleanupResources()
    }

    private fun cleanupResources() {
        cartBadge?.let { badge ->
            try {
                BadgeUtils.detachBadgeDrawable(badge, viewCartButton)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        cartBadge = null
        currentAnimationListener = null
        bannerAdapter = null
        recyclerView.adapter = null
        loadingAnimation.cancelAnimation()
        viewCartButton.clearAnimation()
//        heroCarouselContainer.clearAnimation()
        stopAutoScroll()
    }

    inner class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean,
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State,
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) return
            val column = position % spanCount
            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount
                if (position < spanCount) outRect.top = spacing
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) outRect.top = spacing
            }
        }
    }
}