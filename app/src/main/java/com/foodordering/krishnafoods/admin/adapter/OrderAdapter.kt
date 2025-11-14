package com.foodordering.krishnafoods.admin.adapter


import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.message.NotificationService
import com.foodordering.krishnafoods.admin.model.FoodItem
import com.foodordering.krishnafoods.admin.model.Order
import com.google.android.material.button.MaterialButton
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

    fun updateOrders(newOrders: MutableList<Order>) {
        orderList = newOrders
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderId: TextView = itemView.findViewById(R.id.textOrderId)
        private val userName: TextView = itemView.findViewById(R.id.textUserName)
        private val shopName: TextView = itemView.findViewById(R.id.textShopName)
        private val userAddress: TextView = itemView.findViewById(R.id.textUserAddress)
        private val totalAmount: TextView = itemView.findViewById(R.id.textTotalAmount)
        private val orderStatus: TextView = itemView.findViewById(R.id.textOrderStatus)
        private val contact: TextView = itemView.findViewById(R.id.textContact)
        private val orderDate: TextView = itemView.findViewById(R.id.textOrderDate)
        private val itemsTableContainer: LinearLayout = itemView.findViewById(R.id.itemsTableContainer)
        private val rejectionReason: TextView = itemView.findViewById(R.id.textRejectionReason)
        private val acceptButton: MaterialButton = itemView.findViewById(R.id.btnAcceptOrder)
        private val rejectButton: MaterialButton = itemView.findViewById(R.id.btnRejectOrder)
        private val deliverButton: MaterialButton = itemView.findViewById(R.id.btnDeliverOrder)
        private val buttonContainer: View = itemView.findViewById(R.id.btnContainer)
        private val cardView: CardView = itemView.findViewById(R.id.orderCard)

        fun bind(order: Order) {
            // Basic Info
            orderId.text = context.getString(R.string.order_id_format, order.orderId.takeLast(6).uppercase())
            shopName.text = context.getString(R.string.shop_format, order.shopName.ifEmpty { "N/A" })
            totalAmount.text = "₹${order.totalAmount}"
            orderStatus.text = order.status
            contact.text = context.getString(R.string.contact_format, order.contact.ifEmpty { "N/A" })
            orderDate.text = context.getString(R.string.date_format, formatDate(order.orderDate))
            userName.text = context.getString(R.string.user_loading)
            userAddress.text = context.getString(R.string.address_loading)

            // Fetch user details if userId exists
            if (order.userId.isNotEmpty()) {
                fetchUserDetails(order.userId) { name, address ->
                    userName.text = context.getString(R.string.user_format, name ?: "Unknown")
                    userAddress.text = context.getString(R.string.address_format, address ?: "Not specified")
                }
            } else {
                userName.text = context.getString(R.string.user_format, "Unknown")
                userAddress.text = context.getString(R.string.address_format, "Not specified")
            }

            // Populate items
            itemsTableContainer.removeAllViews()
            if (order.items.isNotEmpty()) {
                addTableHeader()
                order.items.forEach { addFoodItemRow(it) }
            } else {
                val emptyView = TextView(context).apply {
                    text = context.getString(R.string.no_items)
                    setPadding(8.dpToPx(), 16.dpToPx(), 8.dpToPx(), 16.dpToPx())
                    setTextColor(context.getColor(R.color.text_secondary))
                    textSize = 14f
                }
                itemsTableContainer.addView(emptyView)
            }

            // Rejection reason
            rejectionReason.visibility = if (order.status == "Rejected" && !order.rejectionReason.isNullOrEmpty()) {
                rejectionReason.text = context.getString(R.string.reason_format, order.rejectionReason)
                View.VISIBLE
            } else View.GONE

            // Buttons & Card
            updateButtonVisibility(order.status)
            setCardColor(order.status)

            acceptButton.setOnClickListener { updateOrderStatus(order, "Accepted") }
            rejectButton.setOnClickListener { showRejectDialog(order) }
            deliverButton.setOnClickListener { updateOrderStatus(order, "Delivered") }
        }

        private fun addTableHeader() {
            val headerView = LayoutInflater.from(context)
                .inflate(R.layout.item_food_row, itemsTableContainer, false)
            itemsTableContainer.addView(headerView)
        }

        private fun addFoodItemRow(item: FoodItem) {
            val rowView = LayoutInflater.from(context)
                .inflate(R.layout.item_food_row, itemsTableContainer, false)
            rowView.apply {
                findViewById<TextView>(R.id.itemName)?.text = item.name
                findViewById<TextView>(R.id.itemQuantity)?.text = item.quantity.toString()
                findViewById<TextView>(R.id.itemWeight)?.text = context.getString(R.string.weight_format, item.weight)
                val totalPrice = (item.offerPrice ?: 0) * item.quantity
                findViewById<TextView>(R.id.itemPrice)?.text = context.getString(R.string.price_format, totalPrice)
            }
            itemsTableContainer.addView(rowView)
        }

        private fun formatDate(dateString: String): String = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { dateFormat.format(it) } ?: "Invalid Date"
        } catch (_: Exception) { dateString }

        private fun updateButtonVisibility(status: String) {
            buttonContainer.visibility = when (status) {
                "Pending", "Accepted" -> View.VISIBLE
                else -> View.GONE
            }
            acceptButton.visibility = if (status == "Pending") View.VISIBLE else View.GONE
            rejectButton.visibility = if (status == "Pending") View.VISIBLE else View.GONE
            deliverButton.visibility = if (status == "Accepted") View.VISIBLE else View.GONE
        }

        private fun setCardColor(status: String) {
            val colorMap = mapOf(
                "Pending" to R.color.lightYellow,
                "Accepted" to R.color.lightBlue,
                "Rejected" to R.color.lightRed,
                "Delivered" to R.color.lightGreen
            )
            val textColorMap = mapOf(
                "Pending" to R.color.darkYellow,
                "Accepted" to R.color.darkBlue,
                "Rejected" to R.color.darkRed,
                "Delivered" to R.color.darkGreen
            )
            cardView.setCardBackgroundColor(context.getColor(colorMap[status] ?: R.color.card_default))
            orderStatus.setTextColor(context.getColor(textColorMap[status] ?: R.color.black))
        }

        private fun fetchUserDetails(userId: String, callback: (String?, String?) -> Unit) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { doc -> callback(doc.getString("name"), doc.getString("address")) }
                .addOnFailureListener { callback(null, null) }
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
                if (reason.isNotEmpty()) updateOrderStatus(order, "Rejected", reason)
                else Toast.makeText(context, R.string.enter_reason, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun updateOrderStatus(order: Order, status: String, rejectionReason: String? = null) {
        val updateData = mutableMapOf<String, Any>("status" to status, "updatedAt" to System.currentTimeMillis())
        rejectionReason?.let { updateData["rejectionReason"] = it }

        db.collection("orders").document(order.orderId).update(updateData)
            .addOnSuccessListener {
                // ✅ Correct: The service is now created WITHOUT context
                NotificationService(context).sendOrderStatusNotification(order.userId, order.orderId, status, rejectionReason)
                onOrderUpdated()
                showSnackbar(context.getString(R.string.order_status_updated))
            }
            .addOnFailureListener { e ->
                Log.e("OrderAdapter", "Update failed: ${e.message}")
                showSnackbar(context.getString(R.string.order_failed))
            }
    }

    private fun showSnackbar(message: String) {
        (context as? android.app.Activity)?.findViewById<View>(android.R.id.content)?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}
