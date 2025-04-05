package com.example.foodorderingapp.admin.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.admin.adapter.OrderAdapter
import com.example.foodorderingapp.admin.model.Order
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class OrdersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderAdapter
    private val db = FirebaseFirestore.getInstance()
    private var ordersListener: ListenerRegistration? = null
    private var currentQuery: Query? = null

    // Filter chips
    private lateinit var chipAll: Chip
    private lateinit var chipPending: Chip
    private lateinit var chipAccepted: Chip
    private lateinit var chipRejected: Chip
    private lateinit var chipDelivered: Chip

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_orders)

        initViews()
        setupRecyclerView()
        setupChipListeners()
        loadAllOrders()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewOrders)
        chipAll = findViewById(R.id.chipAll)
        chipPending = findViewById(R.id.chipPending)
        chipAccepted = findViewById(R.id.chipAccepted)
        chipRejected = findViewById(R.id.chipRejected)
        chipDelivered = findViewById(R.id.chipDelivered)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OrderAdapter(mutableListOf(), this) {
            // Refresh data when order is updated
            currentQuery?.let { loadOrders(it) }
        }
        recyclerView.adapter = adapter
    }

    private fun setupChipListeners() {
        chipAll.setOnClickListener { loadAllOrders() }
        chipPending.setOnClickListener { loadOrdersByStatus("Pending") }
        chipAccepted.setOnClickListener { loadOrdersByStatus("Accepted") }
        chipRejected.setOnClickListener { loadOrdersByStatus("Rejected") }
        chipDelivered.setOnClickListener {
            startActivity(Intent(this, DeliveredOrdersActivity::class.java))
        }
    }

    private fun loadAllOrders() {
        currentQuery = db.collection("orders").orderBy("orderDate", Query.Direction.DESCENDING)
        loadOrders(currentQuery!!)
        updateSelectedChip(chipAll)
    }

    private fun loadOrdersByStatus(status: String) {
        currentQuery = db.collection("orders")
            .whereEqualTo("status", status)
            .orderBy("orderDate", Query.Direction.DESCENDING)
        loadOrders(currentQuery!!)

        val selectedChip = when(status) {
            "Pending" -> chipPending
            "Accepted" -> chipAccepted
            "Rejected" -> chipRejected
            else -> chipAll
        }
        updateSelectedChip(selectedChip)
    }

    private fun loadOrders(query: Query) {
        ordersListener?.remove()
        ordersListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                showError("Error loading orders")
                return@addSnapshotListener
            }

            val orders = snapshot?.documents?.map { doc ->
                Order(
                    orderId = doc.id,
                    userName = doc.getString("userName") ?: "",
                    shopName = doc.getString("shopName") ?: "",
                    address = doc.getString("userAddress") ?: "",
                    totalAmount = doc.get("totalAmount") as? Int ?: 0,
                    status = doc.getString("status") ?: "Pending",
                    contact = doc.getString("contact") ?: "",
                    orderDate = doc.getString("orderDate") ?: "",
                    items = emptyList() // You'll need to implement proper item parsing
                )
            } ?: emptyList()

            adapter.updateOrders(orders)
        }
    }

    private fun updateSelectedChip(selectedChip: Chip) {
        listOf(chipAll, chipPending, chipAccepted, chipRejected).forEach { chip ->
            chip.isChecked = chip == selectedChip
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