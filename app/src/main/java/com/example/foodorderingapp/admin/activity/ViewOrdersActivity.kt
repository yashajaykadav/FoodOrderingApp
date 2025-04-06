package com.example.foodorderingapp.admin.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.admin.adapter.OrderAdapter
import com.example.foodorderingapp.admin.model.FoodItem
import com.example.foodorderingapp.admin.model.Order
import com.google.firebase.firestore.FirebaseFirestore

class ViewOrdersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderAdapter
    private val db = FirebaseFirestore.getInstance()
    private var ordersList = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_view_orders)

        // ✅ Set up toolbar with back button
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarViewOrders)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerViewOrders)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = OrderAdapter(ordersList, this, ::fetchOrders)
        recyclerView.adapter = adapter

        fetchOrders()
    }

    private fun fetchOrders() {
        db.collection("orders").get().addOnSuccessListener { documents ->
            ordersList.clear()
            for (doc in documents) {
                val order = Order(
                    orderId = doc.id,
                    shopName = doc.getString("shopName") ?: "Unknown",
                    totalAmount = doc.getDouble("totalAmount")?.toInt() ?: 0,
                    status = doc.getString("status") ?: "Pending",
                    userId = doc.getString("userId") ?: "N/A",
                    address = doc.getString("address") ?: "No Address",
                    contact = doc.getString("contact") ?: "No Contact",
                    orderDate = doc.getString("orderDate") ?: "No Date",
                    items = try {
                        val itemsList = mutableListOf<FoodItem>()
                        val itemsArray = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                        for (item in itemsArray) {
                            val name = item["name"] as? String ?: ""
                            val quantity = (item["quantity"] as? Number)?.toInt() ?: 0
                            val price = (item["price"] as? Number)?.toInt() ?: 0
                            itemsList.add(
                                FoodItem(
                                    id = "",
                                    name = name,
                                    price = price,
                                    quantity = quantity,
                                    stock = 0,
                                    weight = ""
                                )
                            )
                        }
                        itemsList
                    } catch (e: Exception) {
                        Log.e("ViewOrders", "Error parsing items", e)
                        emptyList()
                    }
                )
                ordersList.add(order)
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener { e ->
            Log.e("ViewOrders", "Error fetching orders", e)
            Toast.makeText(this, "Failed to fetch orders!", Toast.LENGTH_SHORT).show()
        }
    }
}
