// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.user.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.foodordering.krishnafoods.core.util.applyEdgeToEdge
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.UserFragmentCartBinding
import com.foodordering.krishnafoods.user.activity.OrderConfirmationActivity
import com.foodordering.krishnafoods.user.adapter.CartAdapter
import com.foodordering.krishnafoods.user.manager.CartManager
import com.foodordering.krishnafoods.user.util.vibrateDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CartFragment : Fragment() {

    private companion object {
        private const val LOADING_DELAY_MS = 300L
        private const val MENU_TAB_INDEX = 0
    }

    private var _binding: UserFragmentCartBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartAdapter
    private var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = UserFragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        updateCart()
        requireActivity().applyEdgeToEdge(binding.root)
    }

    override fun onResume() {
        super.onResume()
        isFirstLoad = true
        updateCart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        binding.recyclerViewCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnCheckout.setOnClickListener {
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

        binding.btnStartShopping.setOnClickListener {
            requireContext().vibrateDevice(50)
            activity?.findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager)?.apply {
                setCurrentItem(MENU_TAB_INDEX, true)
            }
        }
    }

    private fun updateCart() {
        if (isFirstLoad) {
            showLoadingState()
            isFirstLoad = false
        }

        viewLifecycleOwner.lifecycleScope.launch {
            delay(LOADING_DELAY_MS)
            val cartItems = CartManager.getCartItems().toMutableList()
            cartAdapter.updateCartItems(cartItems)
            updateTotalPrice()
            checkEmptyState()
        }
    }

    private fun updateTotalPrice() {
        val total = cartAdapter.getTotalAmount()
        binding.totalConfirmAmount.text = getString(R.string.total_0, total)
    }

    private fun checkEmptyState() {
        if (cartAdapter.itemCount == 0) {
            showEmptyState()
        } else {
            showContentState()
        }
    }

    private fun showLoadingState() {
        binding.apply {
            loadingGroup.visibility = View.VISIBLE
            emptyGroup.visibility = View.GONE
            contentGroup.visibility = View.GONE
            lottieLoadingCart.playAnimation()
        }
    }

    private fun showEmptyState() {
        binding.apply {
            loadingGroup.visibility = View.GONE
            emptyGroup.visibility = View.VISIBLE
            contentGroup.visibility = View.GONE
            lottieLoadingCart.cancelAnimation()
        }
    }

    private fun showContentState() {
        binding.apply {
            if (contentGroup.visibility != View.VISIBLE) {
                val slideUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
                checkoutCard.startAnimation(slideUpAnimation)
            }
            loadingGroup.visibility = View.GONE
            emptyGroup.visibility = View.GONE
            contentGroup.visibility = View.VISIBLE
            lottieLoadingCart.cancelAnimation()
        }
    }
}