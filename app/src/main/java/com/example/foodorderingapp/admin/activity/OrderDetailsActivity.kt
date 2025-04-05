package com.example.foodorderingapp.admin.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.admin.adapter.OrderItem
import com.example.foodorderingapp.admin.adapter.OrderItemAdapter
import com.example.foodorderingapp.R
import com.example.foodorderingapp.R.id.etReason
import com.google.firebase.firestore.FirebaseFirestore

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var tvShopName: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvContact: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvStatus: TextView  // Added status TextView
    private lateinit var recyclerViewItems: RecyclerView
    private lateinit var btnUpdateStatus: Button

    private val db = FirebaseFirestore.getInstance()
    private lateinit var orderId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_order_details)

        // Initialize views
        tvShopName = findViewById(R.id.tvShopName)
        tvAddress = findViewById(R.id.tvAddress)
        tvContact = findViewById(R.id.tvContact)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvStatus = TextView(this).apply {
            id = R.id.tvStatus // Make sure to add this ID to your layout
        }
        recyclerViewItems = findViewById(R.id.recyclerViewItems)
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus)

        recyclerViewItems.layoutManager = LinearLayoutManager(this)

        orderId = intent.getStringExtra("orderId") ?: ""

        fetchOrderDetails()

        btnUpdateStatus.setOnClickListener { showStatusUpdateDialog() }
    }

    private fun fetchOrderDetails() {
        db.collection("orders").document(orderId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    tvShopName.text = "Shop Name: ${document.getString("shopName")}"
                    tvAddress.text = "Address: ${document.getString("address")}"
                    tvContact.text = "Contact: ${document.getString("contact")}"
                    tvTotalAmount.text = "Total: ₹${document.getDouble("totalAmount")}"
                    tvStatus.text = "Status: ${document.getString("status") ?: "Pending"}"

                    val itemsList = mutableListOf<OrderItem>()
                    val itemsArray = document.get("items") as? List<Map<String, Any>> ?: emptyList()
                    for (item in itemsArray) {
                        val name = item["name"] as? String ?: ""
                        val quantity = (item["quantity"] as? Long)?.toInt() ?: 0
                        val price = item["price"] as? Double ?: 0.0
                        itemsList.add(OrderItem(name, quantity, price))
                    }

                    recyclerViewItems.adapter = OrderItemAdapter(itemsList)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showStatusUpdateDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_status, null)
        val etReason = dialogView.findViewById<EditText>(etReason)

        AlertDialog.Builder(this)
            .setTitle("Update Order Status")
            .setView(dialogView)
            .setPositiveButton("Accept") { _, _ ->
                updateOrderStatus("Accepted", etReason.text.toString())
            }
            .setNegativeButton("Reject") { _, _ ->
                val reason = etReason.text.toString()
                if (reason.isEmpty()) {
                    Toast.makeText(this, "Please provide a reason for rejection", Toast.LENGTH_SHORT).show()
                } else {
                    updateOrderStatus("Rejected", reason)
                }
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun updateOrderStatus(status: String, reason: String = "") {
        val updates = mutableMapOf<String, Any>(
            "status" to status
        )
        if (reason.isNotEmpty()) {
            updates["statusReason"] = reason
        }

        db.collection("orders").document(orderId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Order $status${if (reason.isNotEmpty()) " - $reason" else ""}",
                    Toast.LENGTH_SHORT).show()
                fetchOrderDetails() // Refresh the display
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}