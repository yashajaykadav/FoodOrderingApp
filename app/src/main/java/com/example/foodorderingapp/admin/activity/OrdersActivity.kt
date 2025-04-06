package com.example.foodorderingapp.admin.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.admin.adapter.OrderAdapter
import com.example.foodorderingapp.admin.model.Order
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class OrdersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderAdapter
    private lateinit var chipGroupFilter: ChipGroup
    private lateinit var emptyStateView: View
    private val db = FirebaseFirestore.getInstance()
    private var ordersListener: ListenerRegistration? = null
    private var currentQuery: Query? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_orders)
        supportActionBar?.hide()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarOrders)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Better than finish()
        }

        initViews()
        setupRecyclerView()
        setupChipGroup()
        observeOrders()
    }


    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewOrders)
        emptyStateView = findViewById(R.id.emptyStateView)
        chipGroupFilter = findViewById(R.id.chipGroupFilter)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OrderAdapter(mutableListOf(), this) {
            currentQuery?.let { loadOrders(it) }
        }
        recyclerView.adapter = adapter
    }

    private fun setupChipGroup() {
        chipGroupFilter.isSingleSelection = true
        chipGroupFilter.check(R.id.chipAll)

        chipGroupFilter.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipAll -> loadAllOrders()
                R.id.chipPending -> loadOrdersByStatus("Pending")
                R.id.chipAccepted -> loadOrdersByStatus("Accepted")
                R.id.chipRejected -> loadOrdersByStatus("Rejected")
                R.id.chipDelivered -> loadOrdersByStatus("Delivered")
                else -> loadAllOrders()
            }
        }
    }

    private fun observeOrders() {
        loadAllOrders()

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                checkEmptyState()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                checkEmptyState()
            }

            override fun onChanged() {
                checkEmptyState()
            }
        })
    }

    private fun checkEmptyState() {
        emptyStateView.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        recyclerView.visibility = if (adapter.itemCount == 0) View.GONE else View.VISIBLE
    }

    private fun loadAllOrders() {
        currentQuery = db.collection("orders")
            .orderBy("orderDate", Query.Direction.DESCENDING)
        loadOrders(currentQuery!!)
    }

    private fun loadOrdersByStatus(status: String) {
        currentQuery = db.collection("orders")
            .whereEqualTo("status", status)
            .orderBy("orderDate", Query.Direction.DESCENDING)
        loadOrders(currentQuery!!)
    }

    private fun loadOrders(query: Query) {
        ordersListener?.remove()
        ordersListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("OrdersActivity", "Error loading orders", error)
                showError("Error loading orders: ${error.message}")
                return@addSnapshotListener
            }

            val orders = snapshot?.documents?.mapNotNull { doc ->
                try {
                    Order.fromDocument(doc)
                } catch (e: Exception) {
                    Log.e("OrdersActivity", "Error parsing order ${doc.id}", e)
                    null
                }
            }?.toMutableList() ?: mutableListOf()

            adapter.updateOrders(orders)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        ordersListener?.remove()
    }
}