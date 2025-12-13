/*
 * Author: Yash Kadav
 * Email: yashkadav52@gmail.com
 */
package com.foodordering.krishnafoods.admin.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.OrderAdapter
import com.foodordering.krishnafoods.admin.model.Order
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class OrdersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderAdapter
    private lateinit var chipGroupFilter: ChipGroup
    private lateinit var searchView: SearchView
    private lateinit var emptyStateView: View
    private val db = FirebaseFirestore.getInstance()

    private var ordersListener: ListenerRegistration? = null

    private val statusFilteredOrders = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_orders)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarOrders)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        initViews()
        setupRecyclerView()
        setupSearchAndFilter()
        // Set the "All" chip as checked initially and load orders
        chipGroupFilter.check(R.id.chipAll)
        startOrdersListener()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewOrders)
        emptyStateView = findViewById(R.id.emptyStateView)
        chipGroupFilter = findViewById(R.id.chipGroupFilter)
        searchView = findViewById(R.id.searchViewOrders)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)


        adapter = OrderAdapter(mutableListOf(), this) { }

        recyclerView.adapter = adapter


        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() = checkEmptyState()
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = checkEmptyState()
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = checkEmptyState()
        })
    }

    private fun setupSearchAndFilter() {
        // When a chip is clicked, re-run the Firestore query
        chipGroupFilter.setOnCheckedChangeListener { _, _ ->
            startOrdersListener()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                applySearchFilter(newText.orEmpty())
                return true
            }
        })
    }

    private fun startOrdersListener() {
        // Remove any previous listener to avoid multiple streams
        ordersListener?.remove()

        val selectedStatus = when (chipGroupFilter.checkedChipId) {
            R.id.chipPending -> "Pending"
            R.id.chipAccepted -> "Accepted"
            R.id.chipRejected -> "Rejected"
            else -> null // For R.id.chipAll
        }

        // Start with the base query
        var query: Query = db.collection("orders")
            .orderBy("orderDate", Query.Direction.DESCENDING)

        // Add a status filter IF one is selected
        if (selectedStatus != null) {
            query = query.whereEqualTo("status", selectedStatus)
        }

        // Attach the new listener
        ordersListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                showError("Error loading orders: ${error.message}")
                return@addSnapshotListener
            }

            val fetched = snapshot?.documents?.mapNotNull {
                try {
                    Order.fromDocument(it)
                } catch (_: Exception) {
                    null // Ignore malformed data
                }
            } ?: emptyList()

            // Update our base list
            statusFilteredOrders.clear()
            statusFilteredOrders.addAll(fetched)

            // Now, apply any active search query to this new list
            applySearchFilter(searchView.query.toString())
        }
    }

    private fun applySearchFilter(query: String) {
        val lowerQuery = query.lowercase()

        val searchResult = if (lowerQuery.isEmpty()) {
            // No search query, show all items from the status filter
            statusFilteredOrders
        } else {
            // Filter by name or ID
            statusFilteredOrders.filter { order ->
                order.shopName.lowercase().contains(lowerQuery) ||
                        order.orderId.lowercase().contains(lowerQuery)
            }.toMutableList()
        }

        adapter.updateOrders(searchResult)
    }

    private fun checkEmptyState() {
        val isEmpty = adapter.itemCount == 0
        emptyStateView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        ordersListener?.remove()
    }
}