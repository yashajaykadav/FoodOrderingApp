package com.foodordering.krishnafoods.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.user.viewmodel.OrderItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(
    private var orders: List<OrderItem>,
    private val onCancelClick: (OrderItem) -> Unit,
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderDate: TextView = itemView.findViewById(R.id.orderDate)
        private val totalAmount: TextView = itemView.findViewById(R.id.totalAmount)
        private val orderStatus: TextView = itemView.findViewById(R.id.orderStatus)
        private val orderItemsContainer: LinearLayout = itemView.findViewById(R.id.orderItemsContainer)
        private val rejectionReason: TextView = itemView.findViewById(R.id.rejectionReason)
        private val btnCancelOrder: Button = itemView.findViewById(R.id.btnCancelOrder)

        fun bind(order: OrderItem) {
            // Format date
            val formattedDate = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(order.orderDate)
                val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                outputFormat.format(date!!)
            } catch (_: Exception) {
                order.orderDate
            }

            orderDate.text = "Order Date: $formattedDate"
            totalAmount.text = "Total: ${formatCurrency(order.totalAmount)}"

            // Set status with appropriate color
            orderStatus.text = order.status
            when (order.status.lowercase(Locale.ROOT)) {
                "pending" -> {
                    orderStatus.setBackgroundResource(R.drawable.bg_status_pending)
                    orderStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
                "completed" -> {
                    orderStatus.setBackgroundResource(R.drawable.bg_status_accepted)
                    orderStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
                "cancelled" -> {
                    orderStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                    orderStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
                "rejected" -> {
                    orderStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                    orderStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
                else -> {
                    orderStatus.setBackgroundResource(R.drawable.bg_status_pending)
                    orderStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                }
            }

            // Clear previous items (keep header)
            orderItemsContainer.apply {
                if (childCount > 1) {
                    removeViews(1, childCount - 1)
                }
            }

            // Add order items
            order.items.forEach { item ->
                val itemView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.item_food_row, orderItemsContainer, false)

                val name = item["name"]?.toString() ?: "Unknown"
                val quantity = item["quantity"]?.toString() ?: "1"
                val weight = item["weight"]?.toString() ?: "N/A"
                val price = item["offerPrice"]?.toString() ?: "0"  // Use price instead of totalAmount

                itemView.findViewById<TextView>(R.id.itemName).text = name
                itemView.findViewById<TextView>(R.id.itemQuantity).text = quantity
                itemView.findViewById<TextView>(R.id.itemWeight).text = weight
                itemView.findViewById<TextView>(R.id.itemPrice).text = "₹$price"  // Show individual item price

                orderItemsContainer.addView(itemView)
            }

            // Handle rejection reason
            if (order.status.equals("Rejected", true)) {
                rejectionReason.text = "Reason: ${order.rejectionReason ?: "No reason provided"}"
                rejectionReason.visibility = View.VISIBLE
            } else {
                rejectionReason.visibility = View.GONE
            }

            // Cancel button visibility
            btnCancelOrder.visibility =
                if (order.status.equals("Pending", true)) View.VISIBLE else View.GONE

            // Set click listeners
            btnCancelOrder.setOnClickListener { onCancelClick(order) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<OrderItem>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = orders.size
            override fun getNewListSize(): Int = newOrders.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
                return orders[oldPos].id == newOrders[newPos].id
            }
            override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
                return orders[oldPos] == newOrders[newPos]
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        orders = newOrders
        diffResult.dispatchUpdatesTo(this)
    }

    private fun formatCurrency(amount: Int): String {
        return try {
            val format = NumberFormat.getCurrencyInstance()
            format.currency = Currency.getInstance("INR")
            format.format(amount.toDouble())
        } catch (_: Exception) {
            "₹$amount" // Fallback if currency formatting fails
        }
    }
}