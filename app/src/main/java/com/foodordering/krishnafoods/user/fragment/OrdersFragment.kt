package com.foodordering.krishnafoods.user.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.user.adapter.OrderAdapter
import com.foodordering.krishnafoods.user.viewmodel.OrderItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class OrdersFragment : Fragment() {

    private lateinit var orderAdapter: OrderAdapter
    private lateinit var recyclerViewOrders: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loadingState: View
    private lateinit var emptyState: View
    private lateinit var emptyIcon: ImageView
    private lateinit var emptyTitle: TextView
    private lateinit var emptyDescription: TextView
    private lateinit var btnAction: Button
    private lateinit var progressBar: ProgressBar

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    private var orderListenerRegistration: ListenerRegistration? = null
    private var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_orders, container, false)

        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        loadingState = view.findViewById(R.id.loadingState)
        emptyState = view.findViewById(R.id.emptyState)
        emptyIcon = view.findViewById(R.id.emptyIcon)
        emptyTitle = view.findViewById(R.id.emptyTitle)
        emptyDescription = view.findViewById(R.id.emptyDescription)
        btnAction = view.findViewById(R.id.btnAction)
        progressBar = view.findViewById(R.id.progressBar)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        loadOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderListenerRegistration?.remove()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(
            orders = emptyList(),
            onCancelClick = { order ->
                showCancelConfirmationDialog(order)
            }
        )

        recyclerViewOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        swipeRefreshLayout.setOnRefreshListener {
            loadOrders()
        }

        btnAction.setOnClickListener {
            if (emptyTitle.text == "Error Loading Orders") {
                loadOrders()
            } else {
                val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
                viewPager.setCurrentItem(0, true)
            }
        }
    }

    private fun loadOrders() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showErrorState("User not authenticated")
            swipeRefreshLayout.isRefreshing = false
            return
        }
        if (isFirstLoad) {
            showLoadingState()
            isFirstLoad = false
        }

        orderListenerRegistration?.remove()

        orderListenerRegistration = db.collection("orders")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { documents, e ->

                swipeRefreshLayout.isRefreshing = false

                if (e != null) {
                    showErrorState("Failed to load orders: ${e.message}")
                    return@addSnapshotListener
                }

                if (documents != null) {
                    val orders = documents.mapNotNull { doc ->
                        try {
                            val totalAmount = when (val amount = doc.get("totalAmount")) {
                                is Long -> amount.toInt()
                                is Double -> amount.toInt()
                                is Int -> amount
                                else -> {
                                    0
                                }
                            }

                            if (totalAmount == 0) {
                            }

                            OrderItem(
                                id = doc.id,
                                userId = doc.getString("userId") ?: "",
                                orderDate = doc.getString("orderDate") ?: "",
                                totalAmount = totalAmount,
                                status = doc.getString("status") ?: "Pending",
                                rejectionReason = doc.getString("rejectionReason"),
                                items = parseOrderItems(doc.get("items"))
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }.sortedByDescending { it.orderDate }

                    if (orders.isEmpty()) {
                        showEmptyState()
                    } else {
                        showContentState(orders)
                    }
                }
            }
    }

    private fun parseOrderItems(items: Any?): List<Map<String, Any>> {
        return try {
            when (items) {
                is List<*> -> items.filterIsInstance<Map<String, Any>>()
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun showCancelConfirmationDialog(order: OrderItem) {
        if (!order.status.equals("Pending", ignoreCase = true)) {
            Snackbar.make(requireView(),
                "Only pending orders can be cancelled",
                Snackbar.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Order")
            .setMessage("Are you sure you want to cancel this order?")
            .setPositiveButton("Yes") { _, _ ->
                cancelOrder(order)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun cancelOrder(order: OrderItem) {
        db.collection("orders").document(order.id)
            .update("status", "Cancelled")
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Order cancelled successfully", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Snackbar.make(requireView(), "Failed to cancel order", Snackbar.LENGTH_LONG).show()
            }
    }

    private fun showLoadingState() {
        loadingState.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
        swipeRefreshLayout.visibility = View.GONE
    }

    private fun showErrorState(error: String) {
        loadingState.visibility = View.GONE
        progressBar.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
        swipeRefreshLayout.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = false

        emptyIcon.setImageResource(R.drawable.ic_error)
        emptyTitle.text = "Error Loading Orders"
        emptyDescription.text = error
        btnAction.text = "Retry"
    }

    private fun showEmptyState() {
        loadingState.visibility = View.GONE
        progressBar.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
        swipeRefreshLayout.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = false

        emptyIcon.setImageResource(R.drawable.ic_empty_orders)
        emptyTitle.text = "No Orders Found"
        emptyDescription.text = "You haven't placed any orders yet"
        btnAction.text = "Start Shopping"
    }

    private fun showContentState(orders: List<OrderItem>) {
        loadingState.visibility = View.GONE
        progressBar.visibility = View.GONE
        emptyState.visibility = View.GONE
        swipeRefreshLayout.visibility = View.VISIBLE
        swipeRefreshLayout.isRefreshing = false

        orderAdapter.updateOrders(orders)
    }
}