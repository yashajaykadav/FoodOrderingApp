package com.example.foodorderingapp.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.user.util.OrderDiffCallback
import com.example.foodorderingapp.user.viewmodel.OrderItem

class OrderAdapter(
    private var orders: List<OrderItem>,
    private val onCancelClick: (OrderItem) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderDate: TextView = view.findViewById(R.id.orderDate)
        val totalAmount: TextView = view.findViewById(R.id.totalAmount)
        val orderItems: TextView = view.findViewById(R.id.orderItems)
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

        // ✅ Ensure `orderDate` is properly formatted
        holder.orderDate.text = "Order Date: ${order.orderDate.ifBlank { "N/A" }}"

        holder.totalAmount.text = "Total: ₹${order.totalAmount}"
        holder.orderStatus.text = "Status: ${order.status}"

        // ✅ Display item details safely
        val itemsFormatted = order.items.joinToString("\n") { item ->
            val itemName = item["name"]?.toString() ?: "Unknown Item"
            val quantity = item["quantity"]?.toString() ?: "1"
            val weight = item["weight"]?.toString() ?: "N/A"
            "$itemName - Qty: $quantity - $weight"
        }
        holder.orderItems.text = itemsFormatted.ifBlank { "No items found" }

        // ✅ Show rejection reason only if the order is rejected
        if (order.status == "Rejected") {
            holder.rejectionReason.visibility = View.VISIBLE
            holder.rejectionReason.text = "Reason: ${order.rejectionReason ?: "No reason provided"}"
        } else {
            holder.rejectionReason.visibility = View.GONE
        }

        // ✅ Optimize button visibility logic
        holder.btnCancelOrder.apply {
            visibility = if (order.status == "Pending") View.VISIBLE else View.GONE
            setOnClickListener { onCancelClick(order) }
        }

        holder.btnTrackDelivery.apply {
            visibility = if (order.status == "Accepted") View.VISIBLE else View.GONE
            setOnClickListener {
                Toast.makeText(holder.itemView.context, "Tracking coming soon...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = orders.size

    // ✅ Efficiently updates the list using DiffUtil
    fun updateOrders(newOrders: List<OrderItem>) {
        val diffCallback = OrderDiffCallback(orders, newOrders)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        orders = newOrders
        diffResult.dispatchUpdatesTo(this)
    }
}
