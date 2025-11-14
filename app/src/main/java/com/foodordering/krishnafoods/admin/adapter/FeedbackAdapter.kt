package com.foodordering.krishnafoods.admin.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.model.Feedback

class FeedbackAdapter(
    private val feedbackList: List<Feedback>,
    private val onItemClick: ((Feedback) -> Unit)? = null // optional click listener
) : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    inner class FeedbackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(feedbackList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_item_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbackList[position]

        // Log username for debugging
        Log.d("FeedbackAdapter", "UserName: ${feedback.userName}")

        // Safely display username with fallback
        holder.tvUserName.text = feedback.userName?.takeIf { it.isNotEmpty() }?.let {
            holder.itemView.context.getString(R.string.user_format, it)
        } ?: "Anonymous User"

        // Set rating (ensures value stays between 0-5)
        holder.ratingBar.rating = feedback.rating.coerceIn(0, 5).toFloat()

        // Display feedback message with fallback
        holder.tvMessage.text = feedback.message?.ifEmpty { "No feedback provided." } ?: "No feedback provided."
    }

    override fun getItemCount(): Int = feedbackList.size
}