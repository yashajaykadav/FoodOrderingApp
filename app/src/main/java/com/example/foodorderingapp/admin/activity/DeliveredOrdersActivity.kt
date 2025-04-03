package com.example.foodorderingapp.admin.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.admin.adapter.OrderAdapter
import com.example.foodorderingapp.R
import com.example.foodorderingapp.user.viewmodel.FoodItem
import com.example.foodorderingapp.admin.model.Order
import com.google.firebase.firestore.FirebaseFirestore

class DeliveredOrdersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderAdapter
    private val db = FirebaseFirestore.getInstance()
    private var ordersList = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_delivered_orders)

        // 🔹 Toolbar with Back Button
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarDeliveredOrders)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerViewDeliveredOrders)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = OrderAdapter(ordersList, this, ::fetchDeliveredOrders)
        recyclerView.adapter = adapter

        fetchDeliveredOrders()
    }

    private fun fetchDeliveredOrders() {
        db.collection("orders").whereEqualTo("status", "Delivered").get()
            .addOnSuccessListener { documents ->
                ordersList.clear()
                for (doc in documents) {
                    val order = Order(
                        orderId = doc.id,
                        shopName = doc.getString("shopName") ?: "Unknown",
                        totalAmount = when (val amount = doc.get("totalAmount")) {
                            is Long -> amount.toInt()
                            is Double -> amount.toInt()
                            else -> 0
                        }, // ✅ Convert totalAmount properly
                        status = doc.getString("status") ?: "Delivered",
                        userId = doc.getString("userId") ?: "N/A",
                        userAddress = doc.getString("address") ?: "No Address",
                        contact = doc.getString("contact") ?: "No Contact",
                        orderDate = doc.getString("orderDate") ?: "No Date",
                        items = try {
                            val itemsList = mutableListOf<FoodItem>()
                            val itemsArray = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                            for (item in itemsArray) {
                                val name = item["name"] as? String ?: "Unknown"
                                val quantity = (item["quantity"] as? Number)?.toInt() ?: 0
                                val price = (item["price"] as? Number)?.toInt() ?: 0
                                itemsList.add(FoodItem(id = "", name = name, price = price, quantity = quantity, stock = 0, weight = ""))
                            }
                            itemsList
                        } catch (e: Exception) {
                            Log.e("DeliveredOrders", "Error parsing items", e)
                            emptyList()
                        }
                    )
                    ordersList.add(order)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("DeliveredOrders", "Error fetching delivered orders", e)
                Toast.makeText(this, "Failed to fetch orders!", Toast.LENGTH_SHORT).show()
            }
    }
}
