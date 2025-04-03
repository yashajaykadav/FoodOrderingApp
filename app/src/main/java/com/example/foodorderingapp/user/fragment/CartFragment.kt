package com.example.foodorderingapp.user.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.user.activity.OrderConfirmationActivity
import com.example.foodorderingapp.user.adapter.CartAdapter
import com.example.foodorderingapp.user.manager.CartManager

class CartFragment : Fragment() {

    private lateinit var recyclerViewCart: RecyclerView
    private lateinit var btnCheckout: Button
    private lateinit var totalConfirmAmount: TextView
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.user_fragment_cart, container, false)

        recyclerViewCart = view.findViewById(R.id.recyclerViewCart)
        btnCheckout = view.findViewById(R.id.btnCheckout)
        totalConfirmAmount = view.findViewById(R.id.totalConfirmAmount)

        setupRecyclerView()
        setupItemTouchHelper()
        setupCheckoutButton()

        return view
    }

    override fun onResume() {
        super.onResume()
        updateCart() // ✅ Ensures cart refresh when fragment becomes visible
    }

    private fun setupRecyclerView() {
        recyclerViewCart.layoutManager = LinearLayoutManager(requireContext())

        cartAdapter = CartAdapter(
            CartManager.getCartItems(),
            onRemoveClick = { updateCart() },  // ✅ Ensures UI updates correctly
            onQuantityChanged = { updateTotalPrice() }
        )

        recyclerViewCart.adapter = cartAdapter
        updateCart() // ✅ Load initial cart data
    }

    private fun setupItemTouchHelper() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val removedItem = cartAdapter.getItemAt(position)

                if (removedItem != null) {
                    CartManager.removeFromCart(removedItem)
                    cartAdapter.removeItem(position)
                    updateCart()
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerViewCart)
    }

    private fun setupCheckoutButton() {
        btnCheckout.setOnClickListener {
            if (CartManager.getCartItems().isEmpty()) {
                Toast.makeText(requireContext(), "Cart is empty!", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(requireContext(), OrderConfirmationActivity::class.java))
            }
        }
    }

    private fun updateCart() {
        val cartItems = CartManager.getCartItems()
        cartAdapter.updateCartItems(cartItems) // ✅ Update RecyclerView
        updateTotalPrice()

        btnCheckout.visibility = if (cartItems.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateTotalPrice() {
        val totalAmount = cartAdapter.getTotalAmount()
        totalConfirmAmount.text = getString(R.string.total_0, totalAmount)
    }


}
