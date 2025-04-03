package com.example.foodorderingapp.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.admin.model.Feedback
import java.util.Date

class FeedbackAdapter(private val feedbackList: List<Feedback>) :
    RecyclerView.Adapter<FeedbackAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.admin_item_feedback, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feedback = feedbackList[position]
        holder.tvUserName.text = "User: ${feedback.userName ?: "Loading..."}"
        holder.ratingBar.rating = feedback.rating.toFloat()
        holder.tvMessage.text = feedback.message
        feedback.timestamp?.let {
            holder.itemView.findViewById<TextView>(R.id.tvTimestamp).text =
                "Submitted: ${java.text.SimpleDateFormat("MMM dd, yyyy").format(Date(it))}"
        }
    }

    override fun getItemCount() = feedbackList.size
}