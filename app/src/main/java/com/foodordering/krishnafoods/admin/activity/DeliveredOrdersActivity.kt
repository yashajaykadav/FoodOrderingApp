package com.foodordering.krishnafoods.admin.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.OrderAdapter
import com.foodordering.krishnafoods.admin.model.FoodItem
import com.foodordering.krishnafoods.admin.model.Order
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class DeliveredOrdersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderAdapter
    private lateinit var lottieLoading: LottieAnimationView
    private lateinit var tvEmpty: TextView

    private val db = FirebaseFirestore.getInstance()
    private var ordersList = mutableListOf<Order>()
    private val pagesize = 10
    private var lastVisibleOrder: DocumentSnapshot? = null
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.admin_activity_delivered_orders)

        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarDeliveredOrders)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerViewDeliveredOrders)
        recyclerView.layoutManager = LinearLayoutManager(this)
        lottieLoading = findViewById(R.id.lottieLoading)
        tvEmpty = findViewById(R.id.tvEmpty)

        adapter = OrderAdapter(ordersList, this, ::fetchDeliveredOrders)
        recyclerView.adapter = adapter

        // Lazy loading when scrolling
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rv.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    fetchDeliveredOrders(loadMore = true)
                }
            }
        })

        // Initial fetch
        fetchDeliveredOrders()
    }

    private fun fetchDeliveredOrders(loadMore: Boolean = false) {
        if (isLoading) return
        isLoading = true

        if (!loadMore) {
            lottieLoading.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            ordersList.clear()
            lastVisibleOrder = null
        }

        var query = db.collection("orders")
            .whereEqualTo("status", "Delivered")
            .limit(pagesize.toLong())

        lastVisibleOrder?.let { query = query.startAfter(it) }

        query.get()
            .addOnSuccessListener { documents ->
                lottieLoading.visibility = View.GONE
                isLoading = false

                if (documents.isEmpty) {
                    if (!loadMore && ordersList.isEmpty()) tvEmpty.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                lastVisibleOrder = documents.documents.last()

                for (doc in documents) {
                    val order = Order(
                        orderId = doc.id,
                        shopName = doc.getString("shopName") ?: "Unknown",
                        totalAmount = (doc.get("totalAmount") as? Number)?.toInt() ?: 0,
                        status = doc.getString("status") ?: "Delivered",
                        userId = doc.getString("userId") ?: "N/A",
                        address = doc.getString("address") ?: "No Address",
                        contact = doc.getString("contact") ?: "No Contact",
                        orderDate = doc.getString("orderDate") ?: "No Date",
                        items = parseItems(doc.get("items"))
                    )
                    ordersList.add(order)
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                lottieLoading.visibility = View.GONE
                isLoading = false
                Toast.makeText(this, "Failed to fetch orders!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun parseItems(itemsObj: Any?): List<FoodItem> {
        return try {
            val itemsList = mutableListOf<FoodItem>()
            val itemsArray = itemsObj as? List<Map<String, Any>> ?: emptyList()
            for (item in itemsArray) {
                itemsList.add(
                    FoodItem(
                        id = item["id"] as? String ?: "",
                        name = item["name"] as? String ?: "Unknown",
                        originalPrice = (item["originalPrice"] as? Number)?.toInt() ?: 0,
                        offerPrice = (item["offerPrice"] as? Number)?.toInt(),
                        quantity = (item["quantity"] as? Number)?.toInt() ?: 0,
                        weight = item["weight"] as? String ?: "",
                        category = item["category"] as? String ?: ""
                    )
                )
            }
            itemsList
        } catch (_: Exception) {
            emptyList()
        }
    }
}
