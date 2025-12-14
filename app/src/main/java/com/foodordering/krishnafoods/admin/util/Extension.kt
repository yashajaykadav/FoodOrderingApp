// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.util

import android.graphics.Paint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.bumptech.glide.Glide
import com.foodordering.krishnafoods.R
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.QuerySnapshot

// Module: Image Loader
fun ImageView.loadUrl(url: String?) {
    if (!url.isNullOrEmpty()) {
        Glide.with(this.context)
            .load(url)
            .placeholder(R.drawable.default_img)
            .error(R.drawable.default_img)
            .into(this)
    } else {
        this.setImageResource(R.drawable.default_img)
    }
}

// Module: Price Formatter (Handles Strike-through & Colors)
fun bindPriceView(
    tvOriginal: TextView,
    tvOffer: TextView,
    originalPrice: Int,
    offerPrice: Int?
) {
    val context = tvOriginal.context

    if (offerPrice != null && offerPrice < originalPrice) {
        // Show Offer
        tvOriginal.visibility = View.VISIBLE
        tvOriginal.text = context.getString(R.string.price_format, originalPrice)
        tvOriginal.paintFlags = tvOriginal.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        tvOffer.text = context.getString(R.string.price_format, offerPrice)
        tvOffer.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
    } else {
        // Show Normal
        tvOriginal.visibility = View.GONE
        tvOffer.text = context.getString(R.string.price_format, originalPrice)
        tvOffer.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
    }
}

fun WriteBatch.deleteAllInSnapshot(snapshot: QuerySnapshot): WriteBatch {
    for (doc in snapshot.documents) {
        this.delete(doc.reference)
    }
    return this
}
fun CardView.setOrderStatusColor(status: String, tvStatus: TextView) {
    val (bgColor, textColor) = when (status) {
        "Pending" -> Pair("#FFF3CD", "#856404")
        "Accepted" -> Pair("#D4EDDA", "#155724")
        "Rejected" -> Pair("#F8D7DA", "#721C24")
        "Delivered" -> Pair("#D1ECF1", "#0C5460")
        else -> Pair("#E0E0E0", "#424242")
    }

    setCardBackgroundColor(bgColor.toColorInt())
    tvStatus.setTextColor(textColor.toColorInt())
}


