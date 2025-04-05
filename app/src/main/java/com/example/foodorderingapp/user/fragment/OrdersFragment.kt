package com.example.foodorderingapp.user.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.foodorderingapp.R
import com.example.foodorderingapp.user.adapter.OrderAdapter
import com.example.foodorderingapp.user.viewmodel.OrderItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrdersFragment : Fragment() {

    private lateinit var orderAdapter: OrderAdapter
    private lateinit var recyclerViewOrders: RecyclerView
    private lateinit var progressBarOrders: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var txtEmptyState: TextView

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_orders, container, false)

        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders)
        progressBarOrders = view.findViewById(R.id.progressBarOrders)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        txtEmptyState = view.findViewById(R.id.txtEmptyState)

        setupRecyclerView()

        swipeRefreshLayout.setOnRefreshListener {
            listenForOrderUpdates()
        }

        listenForOrderUpdates()

        return view
    }

    private fun setupRecyclerView() {
        recyclerViewOrders.layoutManager = LinearLayoutManager(requireContext())
        orderAdapter = OrderAdapter(
            orders = emptyList(),
            onCancelClick = { order -> cancelOrder(order) },
            onTrackClick = { order ->
                Toast.makeText(requireContext(), "Tracking for ${order.id} coming soon!", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerViewOrders.adapter = orderAdapter
    }

    private fun listenForOrderUpdates() {
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        progressBarOrders.visibility = View.VISIBLE
        txtEmptyState.visibility = View.GONE
        recyclerViewOrders.visibility = View.GONE

        db.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                progressBarOrders.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false

                if (error != null) {
                    Toast.makeText(requireContext(), "Error loading orders", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val newOrders = snapshot?.documents?.mapNotNull { doc ->
                    OrderItem.fromDocument(doc) // ✅ safer deserialization
                } ?: emptyList()

                if (newOrders.isEmpty()) {
                    recyclerViewOrders.visibility = View.GONE
                    txtEmptyState.visibility = View.VISIBLE
                } else {
                    recyclerViewOrders.visibility = View.VISIBLE
                    txtEmptyState.visibility = View.GONE
                }

                orderAdapter.updateOrders(newOrders)
            }
    }

    private fun cancelOrder(order: OrderItem) {
        if (!order.status.equals("Pending", ignoreCase = true)) {
            Toast.makeText(requireContext(), "Only pending orders can be cancelled.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("orders").document(order.id)
            .update("status", "Cancelled")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Order Cancelled", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to cancel order!", Toast.LENGTH_SHORT).show()
            }
    }
}
