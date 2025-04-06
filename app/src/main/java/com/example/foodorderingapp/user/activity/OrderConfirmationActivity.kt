package com.example.foodorderingapp.user.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.user.adapter.OrderSummaryAdapter
import com.example.foodorderingapp.user.manager.CartManager
import com.example.foodorderingapp.user.viewmodel.FoodItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class OrderConfirmationActivity : AppCompatActivity() {

    private lateinit var recyclerViewOrder: RecyclerView
    private lateinit var btnConfirmOrder: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var totalAmountText: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirmation)

        // Initialize Views
        recyclerViewOrder = findViewById(R.id.recyclerViewOrder)
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder)
        progressBar = findViewById(R.id.progressBar)
        totalAmountText = findViewById(R.id.totalAmountText)
        val toolbar = findViewById<MaterialToolbar>(R.id.ConfirmOrder)
        toolbar.setNavigationOnClickListener{finish()}

        val cartItems = CartManager.getCartItems()
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val orderAdapter = OrderSummaryAdapter(cartItems)
        recyclerViewOrder.layoutManager = LinearLayoutManager(this)
        recyclerViewOrder.adapter = orderAdapter

        val totalAmount = cartItems.sumOf { it.price * it.quantity }
        totalAmountText.text = "Total: ₹$totalAmount"

        btnConfirmOrder.setOnClickListener {
            checkUserProfileBeforeOrder(cartItems, totalAmount)
        }
    }

    private fun checkUserProfileBeforeOrder(cartItems: List<FoodItem>, totalAmount: Int) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userRef = db.collection("users").document(user.uid)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userInfo = document.data ?: return@addOnSuccessListener
                    val contact = userInfo["contact"] as? String ?: ""
                    val shopName = userInfo["shopName"] as? String ?: ""
                    val address = userInfo["address"] as? String ?: ""

                    if (contact.isNotEmpty() && shopName.isNotEmpty() && address.isNotEmpty()) {
                        saveOrderToFirestore(cartItems, totalAmount)
                    } else {
                        Toast.makeText(this, "Please complete your profile before ordering!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, ProfileSetupActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Please complete your profile before ordering!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ProfileSetupActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching profile details!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveOrderToFirestore(cartItems: List<FoodItem>, totalAmount: Int) {
        btnConfirmOrder.isEnabled = false
        progressBar.visibility = View.VISIBLE

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            btnConfirmOrder.isEnabled = true
            progressBar.visibility = View.GONE
            return
        }

        val userRef = db.collection("users").document(user.uid)
        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userInfo = document.data ?: return@addOnSuccessListener
                    val contact = userInfo["contact"] as? String ?: ""
                    val shopName = userInfo["shopName"] as? String ?: ""
                    val address = userInfo["address"] as? String ?: ""

                    if (contact.isEmpty() || shopName.isEmpty() || address.isEmpty()) {
                        Toast.makeText(this, "Complete your profile before ordering!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, ProfileSetupActivity::class.java))
                        finish()
                        return@addOnSuccessListener
                    }

                    val orderData = hashMapOf(
                        "userId" to user.uid,
                        "contact" to contact,
                        "shopName" to shopName,
                        "address" to address,
                        "items" to cartItems.map {
                            mapOf(
                                "id" to it.id,
                                "name" to it.name,
                                "price" to it.price,
                                "quantity" to it.quantity,
                                "weight" to it.weight
                            )
                        },
                        "totalAmount" to totalAmount,
                        "orderDate" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    )

                    db.collection("orders").add(orderData)
                        .addOnSuccessListener {
                            CartManager.clearCart()
                            startActivity(Intent(this, OrderSuccessActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Order failed!", Toast.LENGTH_SHORT).show()
                            btnConfirmOrder.isEnabled = true
                            progressBar.visibility = View.GONE
                        }

                } else {
                    Toast.makeText(this, "Please complete your profile before ordering!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ProfileSetupActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get user info!", Toast.LENGTH_SHORT).show()
                btnConfirmOrder.isEnabled = true
                progressBar.visibility = View.GONE
            }
    }
}
