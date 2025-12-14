// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.model.FoodItem
import com.foodordering.krishnafoods.admin.model.Order
import com.foodordering.krishnafoods.admin.util.UserCache
import com.foodordering.krishnafoods.admin.util.formatToReadableDate
import com.foodordering.krishnafoods.databinding.AdminItemOrderBinding
import com.foodordering.krishnafoods.databinding.ItemFoodRowBinding
import com.foodordering.krishnafoods.admin.util.setOrderStatusColor


// Define Actions clearly
enum class OrderAction { ACCEPT, REJECT, DELIVER }

class OrderAdapter(
    private var orderList: MutableList<Order>,
    private val context: Context,
    private val onAction: (Order, OrderAction, String?) -> Unit // String? is for rejection reason
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = AdminItemOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orderList[position])
    }

    override fun getItemCount(): Int = orderList.size

    fun updateList(newOrders: List<Order>) {
        orderList.clear()
        orderList.addAll(newOrders)
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(private val binding: AdminItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.apply {
                // 1. Basic Info
                textOrderId.text = context.getString(R.string.order_id_format, order.orderId.takeLast(6).uppercase())
                textShopName.text = context.getString(R.string.shop_format, order.shopName.ifEmpty { "N/A" })
                textTotalAmount.text = context.getString(R.string.price_format, order.totalAmount)
                textOrderStatus.text = order.status
                textContact.text = context.getString(R.string.contact_format, order.contact.ifEmpty { "N/A" })
                textOrderDate.text = context.getString(R.string.date_format, order.orderDate.formatToReadableDate())

                // 2. Optimized User Fetching (Uses Cache)
                if (order.userId.isNotEmpty()) {
                    UserCache.getUserDetails(order.userId) { name, address ->
                        textUserName.text = context.getString(R.string.user_format, name)
                        textUserAddress.text = context.getString(R.string.address_format, address)
                    }
                } else {
                    textUserName.text = "User: Unknown"
                    textUserAddress.text = "Address: N/A"
                }

                // 3. Populate Items (Modularized)
                populateFoodItems(order.items)

                // 4. Rejection Reason Visibility
                val isRejected = order.status == "Rejected"
                textRejectionReason.isVisible = isRejected && !order.rejectionReason.isNullOrEmpty()
                if (isRejected) {
                    textRejectionReason.text = context.getString(R.string.reason_format, order.rejectionReason)
                }

                // 5. Visuals & Buttons
                orderCard.setOrderStatusColor(order.status, textOrderStatus)
                setupButtons(order)
            }
        }

        private fun populateFoodItems(items: List<FoodItem>) {
            binding.itemsTableContainer.removeAllViews()

            if (items.isNotEmpty()) {
                // Add Header (You can move header layout to XML if static, but inflating is fine)
                val header = ItemFoodRowBinding.inflate(LayoutInflater.from(context), binding.itemsTableContainer, true)
                // Header text is usually set in XML, assuming item_food_row has defaults or you set them here

                // Add Items
                items.forEach { item ->
                    val row = ItemFoodRowBinding.inflate(LayoutInflater.from(context), binding.itemsTableContainer, true)
                    row.itemName.text = item.name
                    row.itemQuantity.text = item.quantity.toString()
                    row.itemWeight.text = context.getString(R.string.weight_format, item.weight)

                    val total = (item.offerPrice ?: 0) * item.quantity
                    row.itemPrice.text = context.getString(R.string.price_format, total)
                }
            } else {
                val emptyView = TextView(context).apply {
                    text = context.getString(R.string.no_items)
                    setPadding(16, 16, 16, 16)
                }
                binding.itemsTableContainer.addView(emptyView)
            }
        }

        private fun setupButtons(order: Order) {
            binding.apply {
                // Logic: Only show container if actions are available
                val isPending = order.status == "Pending"
                val isAccepted = order.status == "Accepted"

                btnContainer.isVisible = isPending || isAccepted
                btnAcceptOrder.isVisible = isPending
                btnRejectOrder.isVisible = isPending
                btnDeliverOrder.isVisible = isAccepted

                btnAcceptOrder.setOnClickListener { onAction(order, OrderAction.ACCEPT, null) }
                btnDeliverOrder.setOnClickListener { onAction(order, OrderAction.DELIVER, null) }
                btnRejectOrder.setOnClickListener { showRejectDialog(order) }
            }
        }

        private fun showRejectDialog(order: Order) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reject_order, null)
            val input = dialogView.findViewById<EditText>(R.id.editRejectReason)

            AlertDialog.Builder(context)
                .setTitle(R.string.reject_order_title)
                .setView(dialogView)
                .setPositiveButton(R.string.reject) { _, _ ->
                    val reason = input.text.toString().trim()
                    if (reason.isNotEmpty()) {
                        onAction(order, OrderAction.REJECT, reason)
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }
}