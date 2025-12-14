// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.user.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.FragmentOrdersBinding
import com.foodordering.krishnafoods.user.adapter.OrderAdapter
import com.foodordering.krishnafoods.user.viewmodel.OrderItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderAdapter: OrderAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var orderListenerRegistration: ListenerRegistration? = null
    private var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
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
        _binding = null
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(
            orders = emptyList(),
            onCancelClick = { order -> showCancelConfirmationDialog(order) }
        )

        binding.recyclerViewOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadOrders()
        }

        binding.btnAction.setOnClickListener {
            if (binding.emptyTitle.text == "Error Loading Orders") {
                loadOrders()
            } else {
                // Navigate to Menu/Home tab (Index 0)
                requireActivity().findViewById<ViewPager2>(R.id.viewPager)?.setCurrentItem(0, true)
            }
        }
    }

    private fun loadOrders() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showErrorState("User not authenticated")
            binding.swipeRefreshLayout.isRefreshing = false
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
                binding.swipeRefreshLayout.isRefreshing = false

                if (e != null) {
                    showErrorState("Failed to load orders: ${e.message}")
                    return@addSnapshotListener
                }

                if (documents != null) {
                    val orders = documents.mapNotNull { mapDocumentToOrder(it) }
                        .sortedByDescending { it.orderDate }

                    if (orders.isEmpty()) {
                        showEmptyState()
                    } else {
                        showContentState(orders)
                    }
                }
            }
    }

    private fun mapDocumentToOrder(doc: DocumentSnapshot): OrderItem? {
        return try {
            val totalAmount = (doc.get("totalAmount") as? Number)?.toInt() ?: 0

            OrderItem(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                orderDate = doc.getString("orderDate") ?: "",
                totalAmount = totalAmount,
                status = doc.getString("status") ?: "Pending",
                rejectionReason = doc.getString("rejectionReason"),
                items = parseOrderItems(doc.get("items"))
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseOrderItems(items: Any?): List<Map<String, Any>> {
        return (items as? List<*>)?.filterIsInstance<Map<String, Any>>() ?: emptyList()
    }

    private fun showCancelConfirmationDialog(order: OrderItem) {
        if (!order.status.equals("Pending", ignoreCase = true)) {
            Snackbar.make(binding.root, "Only pending orders can be cancelled", Snackbar.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Order")
            .setMessage("Are you sure you want to cancel this order?")
            .setPositiveButton("Yes") { _, _ -> cancelOrder(order) }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun cancelOrder(order: OrderItem) {
        db.collection("orders").document(order.id)
            .update("status", "Cancelled")
            .addOnSuccessListener {
                Snackbar.make(binding.root, "Order cancelled successfully", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Failed to cancel order", Snackbar.LENGTH_LONG).show()
            }
    }

    private fun showLoadingState() {
        binding.apply {
            loadingState.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            swipeRefreshLayout.visibility = View.GONE
        }
    }

    private fun showErrorState(error: String) {
        binding.apply {
            loadingState.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            swipeRefreshLayout.visibility = View.GONE

            emptyIcon.setImageResource(R.drawable.ic_error)
            emptyTitle.text = "Error Loading Orders"
            emptyDescription.text = error
            btnAction.text = "Retry"
        }
    }

    private fun showEmptyState() {
        binding.apply {
            loadingState.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            swipeRefreshLayout.visibility = View.GONE

            emptyIcon.setImageResource(R.drawable.ic_empty_orders)
            emptyTitle.text = "No Orders Found"
            emptyDescription.text = "You haven't placed any orders yet"
            btnAction.text = "Start Shopping"
        }
    }

    private fun showContentState(orders: List<OrderItem>) {
        binding.apply {
            loadingState.visibility = View.GONE
            emptyState.visibility = View.GONE
            swipeRefreshLayout.visibility = View.VISIBLE
            orderAdapter.updateOrders(orders)
        }
    }
}