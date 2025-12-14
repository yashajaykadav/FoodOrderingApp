// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.ItemFoodRowBinding
import com.foodordering.krishnafoods.databinding.UserItemOrderBinding
import com.foodordering.krishnafoods.user.viewmodel.OrderItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(
    private var orders: List<OrderItem>,
    private val onCancelClick: (OrderItem) -> Unit,
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(private val binding: UserItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderItem) {
            val context = binding.root.context

            // Format date
            val formattedDate = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(order.orderDate)
                val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                outputFormat.format(date!!)
            } catch (_: Exception) {
                order.orderDate
            }

            binding.orderDate.text = "Order Date: $formattedDate"
            binding.totalAmount.text = "Total: ${formatCurrency(order.totalAmount)}"
            binding.orderStatus.text = order.status

            // Set status styling
            when (order.status.lowercase(Locale.ROOT)) {
                "pending" -> {
                    binding.orderStatus.setBackgroundResource(R.drawable.bg_status_pending)
                    binding.orderStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
                }
                "completed" -> {
                    binding.orderStatus.setBackgroundResource(R.drawable.bg_status_accepted)
                    binding.orderStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
                }
                "cancelled", "rejected" -> {
                    binding.orderStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                    binding.orderStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
                }
                else -> {
                    binding.orderStatus.setBackgroundResource(R.drawable.bg_status_pending)
                    binding.orderStatus.setTextColor(ContextCompat.getColor(context, R.color.black))
                }
            }

            // Clear previous items (keep the header, assuming header is child index 0)
            if (binding.orderItemsContainer.childCount > 1) {
                binding.orderItemsContainer.removeViews(1, binding.orderItemsContainer.childCount - 1)
            }

            // Add order items dynamically
            order.items.forEach { item ->
                // Inflate the row using View Binding
                val rowBinding = ItemFoodRowBinding.inflate(LayoutInflater.from(context), binding.orderItemsContainer, false)

                val name = item["name"]?.toString() ?: "Unknown"
                val quantity = item["quantity"]?.toString() ?: "1"
                val weight = item["weight"]?.toString() ?: "N/A"
                val price = item["offerPrice"]?.toString() ?: "0"

                rowBinding.itemName.text = name
                rowBinding.itemQuantity.text = quantity
                rowBinding.itemWeight.text = weight
                rowBinding.itemPrice.text = "₹$price"

                // Add the bound view to the container
                binding.orderItemsContainer.addView(rowBinding.root)
            }

            // Handle rejection reason
            if (order.status.equals("Rejected", true)) {
                binding.rejectionReason.text = "Reason: ${order.rejectionReason ?: "No reason provided"}"
                binding.rejectionReason.visibility = View.VISIBLE
            } else {
                binding.rejectionReason.visibility = View.GONE
            }

            // Cancel button visibility
            binding.btnCancelOrder.visibility =
                if (order.status.equals("Pending", true)) View.VISIBLE else View.GONE

            binding.btnCancelOrder.setOnClickListener { onCancelClick(order) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = UserItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
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
            val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            format.format(amount)
        } catch (_: Exception) {
            "₹$amount"
        }
    }
}