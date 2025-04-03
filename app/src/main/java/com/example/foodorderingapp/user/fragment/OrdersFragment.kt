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

        // Initialize RecyclerView
        recyclerViewOrders.layoutManager = LinearLayoutManager(requireContext())
        orderAdapter = OrderAdapter(mutableListOf()) { order ->
            cancelOrder(order)
        }
        recyclerViewOrders.adapter = orderAdapter

        // Load orders in real-time
        listenForOrderUpdates()

        // Handle pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener {
            listenForOrderUpdates()
        }

        return view
    }

    private fun listenForOrderUpdates() {
        if (userId == null) return

        progressBarOrders.visibility = View.VISIBLE
        db.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("orderDate", com.google.firebase.firestore.Query.Direction.DESCENDING) // ✅ Sort by latest first
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    progressBarOrders.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(requireContext(), "Error loading orders", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val newOrders = snapshot.documents.map { doc ->
                        val order = doc.toObject(OrderItem::class.java)
                        order?.id = doc.id // Store Firestore Document ID
                        order
                    }.filterNotNull()

                    progressBarOrders.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false
                    orderAdapter.updateOrders(newOrders)

                    // ✅ Show empty state if no orders exist
                    if (newOrders.isEmpty()) {
                        recyclerViewOrders.visibility = View.GONE
                        txtEmptyState.visibility = View.VISIBLE
                    } else {
                        recyclerViewOrders.visibility = View.VISIBLE
                        txtEmptyState.visibility = View.GONE
                    }
                }
            }
    }


    // ✅ Allow users to cancel orders, but NOT if status is "Accepted"
    private fun cancelOrder(order: OrderItem) {
        if (order.status == "Accepted") {
            Toast.makeText(requireContext(), "Cannot cancel an accepted order!", Toast.LENGTH_SHORT).show()
            return
        }

        val orderRef = FirebaseFirestore.getInstance().collection("orders").document(order.id)

        orderRef.update("status", "Cancelled")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Order Cancelled", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to cancel order!", Toast.LENGTH_SHORT).show()
            }
    }
}
