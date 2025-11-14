// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.user.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.user.activity.OrderConfirmationActivity
import com.foodordering.krishnafoods.user.adapter.CartAdapter
import com.foodordering.krishnafoods.user.manager.CartManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class CartFragment : Fragment() {

    private companion object {
        private const val LOADING_DELAY_MS = 300L
    }

    // View properties
    private lateinit var recyclerViewCart: RecyclerView
    private lateinit var btnCheckout: MaterialButton
    private lateinit var totalConfirmAmount: TextView
    private lateinit var textEmptyCart: TextView
    private lateinit var loadingAnim: LottieAnimationView
    private lateinit var checkoutCard: MaterialCardView
    private lateinit var btnStartShopping: MaterialButton

    // View groups for managing state
    private lateinit var loadingGroup: View
    private lateinit var emptyGroup: View
    private lateinit var contentGroup: View

    // Other properties
    private lateinit var cartAdapter: CartAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var isFirstLoad = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        // The call to setupItemTouchHelper() has been removed from here.
        setupClickListeners()
        updateCart()
        setupEdgeToEdgeHandling()
    }

    override fun onResume() {
        super.onResume()
        isFirstLoad = true
        updateCart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }

    private fun setupEdgeToEdgeHandling() {
        val initialMarginBottom = (checkoutCard.layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
        ViewCompat.setOnApplyWindowInsetsListener(requireActivity().window.decorView) { _, insets ->
            val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            checkoutCard.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = initialMarginBottom + navBarInsets.bottom
            }
            insets
        }
    }

    private fun initViews(view: View) {
        recyclerViewCart = view.findViewById(R.id.recyclerViewCart)
        btnCheckout = view.findViewById(R.id.btnCheckout)
        totalConfirmAmount = view.findViewById(R.id.totalConfirmAmount)
        textEmptyCart = view.findViewById(R.id.textEmptyCart)
        loadingAnim = view.findViewById(R.id.lottieLoadingCart)
        checkoutCard = view.findViewById(R.id.checkoutCard)
        btnStartShopping = view.findViewById(R.id.btnStartShopping)

        loadingGroup = view.findViewById(R.id.loadingGroup)
        emptyGroup = view.findViewById(R.id.emptyGroup)
        contentGroup = view.findViewById(R.id.contentGroup)
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            cartItems = mutableListOf(),
            onRemoveClick = {
                updateTotalPrice()
                checkEmptyState()
            },
            onQuantityChanged = { updateTotalPrice() }
        )
        recyclerViewCart.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewCart.adapter = cartAdapter
    }

    // The entire setupItemTouchHelper() function has been removed.

    private fun setupClickListeners() {
        btnCheckout.setOnClickListener {
            if (cartAdapter.itemCount > 0) {
                if (isAdded) {
                    startActivity(Intent(requireContext(), OrderConfirmationActivity::class.java))
                }
            } else {
                if (isAdded) {
                    Toast.makeText(requireContext(), getString(R.string.cart_empty), Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnStartShopping.setOnClickListener {
            Toast.makeText(context, "Navigating to menu...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCart() {
        if (isFirstLoad) {
            showLoadingState()
            isFirstLoad = false
        }
        handler.postDelayed({
            val cartItems = CartManager.getCartItems().toMutableList()
            cartAdapter.updateCartItems(cartItems)
            updateTotalPrice()
            checkEmptyState()
        }, LOADING_DELAY_MS)
    }

    private fun updateTotalPrice() {
        val total = cartAdapter.getTotalAmount()
        totalConfirmAmount.text = getString(R.string.total_0, total)
    }

    private fun checkEmptyState() {
        if (cartAdapter.itemCount == 0) {
            showEmptyState()
        } else {
            showContentState()
        }
    }

    private fun showLoadingState() {
        loadingGroup.visibility = View.VISIBLE
        emptyGroup.visibility = View.GONE
        contentGroup.visibility = View.GONE
        loadingAnim.playAnimation()
    }

    private fun showEmptyState() {
        loadingGroup.visibility = View.GONE
        emptyGroup.visibility = View.VISIBLE
        contentGroup.visibility = View.GONE
        loadingAnim.cancelAnimation()
    }

    private fun showContentState() {
        if (contentGroup.visibility != View.VISIBLE) {
            val slideUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
            checkoutCard.startAnimation(slideUpAnimation)
        }
        loadingGroup.visibility = View.GONE
        emptyGroup.visibility = View.GONE
        contentGroup.visibility = View.VISIBLE
        loadingAnim.cancelAnimation()
    }
}