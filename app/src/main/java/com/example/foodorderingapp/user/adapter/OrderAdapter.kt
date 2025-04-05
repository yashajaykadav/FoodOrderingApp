package com.example.foodorderingapp.user.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.user.util.OrderDiffCallback
import com.example.foodorderingapp.user.viewmodel.OrderItem
import com.google.firebase.firestore.FirebaseFirestore

class OrderAdapter(
    private var orders: List<OrderItem>,
    private val onCancelClick: (OrderItem) -> Unit,
    private val onTrackClick: (OrderItem) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {


    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderDate: TextView = view.findViewById(R.id.orderDate)
        val totalAmount: TextView = view.findViewById(R.id.totalAmount)
        val orderItemsContainer: LinearLayout = view.findViewById(R.id.orderItemsContainer)
        val orderStatus: TextView = view.findViewById(R.id.orderStatus)
        val rejectionReason: TextView = view.findViewById(R.id.rejectionReason)
        val btnCancelOrder: Button = view.findViewById(R.id.btnCancelOrder)
        val btnTrackDelivery: Button = view.findViewById(R.id.btnTrackDelivery)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.orderDate.text = "Order Date: ${order.orderDate.ifBlank { "N/A" }}"
        holder.totalAmount.text = "Total: ₹${order.totalAmount}"
        holder.orderStatus.text = "Status: ${order.status.ifBlank { "Pending" }}"

        // Clear old views (keep header row at index 0)
        holder.orderItemsContainer.removeViews(1, holder.orderItemsContainer.childCount - 1)

        order.items.forEach { item ->
            val itemName = item["name"]?.toString() ?: "Unknown"
            val quantity = item["quantity"]?.toString() ?: "1"
            val weight = item["weight"]?.toString() ?: "N/A"
            val price = item["price"]?.toString() ?: "0.0"

            val row = LinearLayout(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 8)
            }

            val nameView = TextView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                text = itemName
            }

            val qtyView = TextView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = quantity
                gravity = Gravity.CENTER
            }

            val weightView = TextView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = weight
                gravity = Gravity.CENTER
            }

            val priceView = TextView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = price
                gravity = Gravity.END
            }

            row.addView(nameView)
            row.addView(qtyView)
            row.addView(weightView)
            row.addView(priceView)

            holder.orderItemsContainer.addView(row)
        }


        // Rejection reason
        if (order.status == "Rejected") {
            holder.rejectionReason.visibility = View.VISIBLE
            holder.rejectionReason.text = "Reason: ${order.rejectionReason ?: "No reason provided"}"
        } else {
            holder.rejectionReason.visibility = View.GONE
        }

        // Cancel button only visible if Pending
        holder.btnCancelOrder.apply {
            visibility = if (order.status.equals("Pending", true)) View.VISIBLE else View.GONE
            setOnClickListener { onCancelClick(order) }
        }

        // Track Delivery button
        holder.btnTrackDelivery.setOnClickListener {
            onTrackClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<OrderItem>) {
        val diffCallback = OrderDiffCallback(orders, newOrders)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        orders = newOrders
        diffResult.dispatchUpdatesTo(this)
    }
}
