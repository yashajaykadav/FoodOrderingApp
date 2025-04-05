package com.example.foodorderingapp.admin.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.admin.model.Order
import com.example.foodorderingapp.user.viewmodel.FoodItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(
    private var orderList: MutableList<Order>,
    private val context: Context,
    private val onOrderUpdated: () -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orderList[position])
    }

    override fun getItemCount(): Int = orderList.size

    fun updateOrders(newOrders: List<Order>) {
        orderList.clear()
        orderList.addAll(newOrders)
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val orderId: TextView = view.findViewById(R.id.textOrderId)
        private val userName: TextView = view.findViewById(R.id.textUserName)
        private val shopName: TextView = view.findViewById(R.id.textShopName)
        private val userAddress: TextView = view.findViewById(R.id.textUserAddress)
        private val totalAmount: TextView = view.findViewById(R.id.textTotalAmount)
        private val orderStatus: TextView = view.findViewById(R.id.textOrderStatus)
        private val contact: TextView = view.findViewById(R.id.textContact)
        private val orderDate: TextView = view.findViewById(R.id.textOrderDate)
        private val itemsList: TextView = view.findViewById(R.id.textItemsList)
        private val rejectionReason: TextView = view.findViewById(R.id.textRejectionReason)
        private val acceptButton: Button = view.findViewById(R.id.btnAcceptOrder)
        private val rejectButton: Button = view.findViewById(R.id.btnRejectOrder)
        private val deliverButton: Button = view.findViewById(R.id.btnDeliverOrder)
        private val buttonContainer: View = view.findViewById(R.id.btnContainer)
        private val cardView: View = view.findViewById(R.id.orderCard)

        fun bind(order: Order) {
            // Basic order info
            orderId.text = "Order ID: ${order.orderId}"
            shopName.text = "Shop: ${order.shopName ?: "N/A"}"
            totalAmount.text = "Total: ₹${String.format("%.2f", order.totalAmount.toDouble())}"
            orderStatus.text = "Status: ${order.status}"
            contact.text = "Contact: ${order.contact ?: "N/A"}"
            orderDate.text = "Date: ${formatDate(order.orderDate)}"

            // Initialize user info with loading state
            userName.text = "User: Loading..."
            userAddress.text = "Address: Loading..."

            // Fetch user details from Firestore
            fetchUserDetails(order.userId) { name, address ->
                userName.text = "User: ${name ?: "Unknown"}"
                userAddress.text = "Address: ${address ?: "Not specified"}"
            }

            // Format items list
            itemsList.text = formatItemsList(order.items)

            // Handle rejection reason visibility
            if (order.status == "Rejected" && !order.rejectionReason.isNullOrEmpty()) {
                rejectionReason.visibility = View.VISIBLE
                rejectionReason.text = "Reason: ${order.rejectionReason}"
            } else {
                rejectionReason.visibility = View.GONE
            }

            // Update button visibility based on status
            updateButtonVisibility(order.status)

            // Set card color based on status
            setCardColor(order.status)

            // Set click listeners
            acceptButton.setOnClickListener { updateOrderStatus(order, "Accepted") }
            rejectButton.setOnClickListener { showRejectDialog(order) }
            deliverButton.setOnClickListener { updateOrderStatus(order, "Delivered") }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateString)
                date?.let { dateFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                dateString
            }
        }

        private fun formatItemsList(items: List<FoodItem>): String {
            return items.joinToString("\n") { item ->
                val totalPrice = item.price * item.quantity
                "• ${item.name} (${item.quantity} × ₹${String.format("%.2f", item.price)}) = ₹${String.format("%.2f", totalPrice)}"
            }
        }

        private fun updateButtonVisibility(status: String) {
            buttonContainer.visibility = View.VISIBLE
            acceptButton.visibility = if (status == "Pending") View.VISIBLE else View.GONE
            rejectButton.visibility = if (status == "Pending") View.VISIBLE else View.GONE
            deliverButton.visibility = if (status == "Accepted") View.VISIBLE else View.GONE
        }

        private fun setCardColor(status: String) {
            val colorRes = when (status) {
                "Pending" -> R.color.lightYellow
                "Accepted" -> R.color.lightBlue
                "Rejected" -> R.color.lightRed
                "Delivered" -> R.color.lightGreen
                else -> R.color.card_default
            }
            cardView.setBackgroundColor(context.getColor(colorRes))
        }

        private fun fetchUserDetails(userId: String, callback: (String?, String?) -> Unit) {
            if (userId.isBlank()) {
                callback(null, null)
                return
            }

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name")
                        val address = document.getString("address")
                        callback(name, address)
                    } else {
                        callback(null, null)
                    }
                }
                .addOnFailureListener {
                    callback(null, null)
                }
        }
    }

    private fun showRejectDialog(order: Order) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reject_order, null)
        val input = dialogView.findViewById<EditText>(R.id.editRejectReason)

        AlertDialog.Builder(context)
            .setTitle("Reject Order")
            .setView(dialogView)
            .setPositiveButton("Reject") { _, _ ->
                val reason = input.text.toString().trim()
                if (reason.isNotEmpty()) {
                    updateOrderStatus(order, "Rejected", reason)
                } else {
                    Toast.makeText(context, "Please enter a reason", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateOrderStatus(order: Order, status: String, rejectionReason: String? = null) {
        val updateData = mutableMapOf<String, Any>(
            "status" to status,
            "updatedAt" to System.currentTimeMillis()
        )

        rejectionReason?.let { updateData["rejectionReason"] = it }

        db.collection("orders").document(order.orderId)
            .update(updateData)
            .addOnSuccessListener {
                order.status = status
                order.rejectionReason = rejectionReason
                notifyItemChanged(orderList.indexOf(order))
                onOrderUpdated()
                showSnackbar("Order $status" + (rejectionReason?.let { ": $it" } ?: ""))
            }
            .addOnFailureListener { e ->
                showSnackbar("Failed: ${e.localizedMessage}")
            }
    }

    private fun showSnackbar(message: String) {
        (context as? android.app.Activity)?.window?.decorView?.rootView?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }
}