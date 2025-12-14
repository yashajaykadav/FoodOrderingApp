package com.foodordering.krishnafoods.admin.util
// Author: Yash Kadav
// Email: yashkadav52@gmail.com


import android.widget.TextView
import androidx.core.content.ContextCompat
import com.foodordering.krishnafoods.R
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Locale

// Module: Status Coloring Logic
fun MaterialCardView.setOrderStatusColor(status: String, tvStatus: TextView) {
    val context = this.context
    val (bgColor, textColor) = when (status) {
        "Pending" -> Pair(R.color.lightYellow, R.color.darkYellow)
        "Accepted" -> Pair(R.color.lightBlue, R.color.darkBlue)
        "Rejected" -> Pair(R.color.lightRed, R.color.darkRed)
        "Delivered" -> Pair(R.color.lightGreen, R.color.darkGreen)
        else -> Pair(R.color.card_default, R.color.black)
    }

    this.setCardBackgroundColor(ContextCompat.getColor(context, bgColor))
    tvStatus.setTextColor(ContextCompat.getColor(context, textColor))
}

// Module: Date Formatter
fun String.formatToReadableDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(this)
        date?.let { outputFormat.format(it) } ?: this
    } catch (e: Exception) {
        this
    }
}