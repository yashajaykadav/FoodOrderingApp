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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class OrderAdapter(
    private var orderList: MutableList<Order>,
    private val context: Context,
    private val onOrderUpdated: () -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.admin_item_order, parent, false)
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
        private val acceptButton: Button = view.findViewById(R.id.btnAcceptOrder)
        private val rejectButton: Button = view.findViewById(R.id.btnRejectOrder)
        private val deliverButton: Button = view.findViewById(R.id.btnDeliverOrder)
        private val cardView: View = view.findViewById(R.id.orderCard)

        fun bind(order: Order) {
            orderId.text = "Order ID: ${order.orderId}"
            userName.text = "User: ${order.userName}"
            shopName.text = "Shop: ${order.shopName}"
            userAddress.text = "Address: ${order.userAddress}"
            totalAmount.text = "Total: ₹${order.totalAmount}"
            orderStatus.text = "Status: ${order.status}"
            contact.text = "Contact: ${order.contact}"
            orderDate.text = "Date: ${order.orderDate}"

            // Display ordered items
            itemsList.text = order.items.joinToString("\n") { "• ${it.name} (${it.quantity} × ₹${it.price})" }

            // Update UI based on status
            updateButtonVisibility(order.status)
            setCardColor(order.status)

            // Button click listeners
            acceptButton.setOnClickListener { updateOrderStatus(order, "Accepted") }
            rejectButton.setOnClickListener { showRejectDialog(order) }
            deliverButton.setOnClickListener { updateOrderStatus(order, "Delivered") }
        }

        private fun updateButtonVisibility(status: String) {
            acceptButton.visibility = if (status == "Pending") View.VISIBLE else View.GONE
            rejectButton.visibility = if (status == "Pending") View.VISIBLE else View.GONE
            deliverButton.visibility = if (status == "Accepted") View.VISIBLE else View.GONE
        }

        private fun setCardColor(status: String) {
            val colorRes = when (status) {
                "Pending" -> R.color.colorYellow
                "Accepted" -> R.color.colorGreen
                "Rejected" -> R.color.colorRed
                "Delivered" -> R.color.card_blue
                else -> R.color.card_default
            }
            cardView.setBackgroundColor(context.getColor(colorRes))
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
