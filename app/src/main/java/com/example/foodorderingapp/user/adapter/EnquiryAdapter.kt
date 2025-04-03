package com.example.foodorderingapp.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.user.viewmodel.Enquiry

class EnquiryAdapter(private val enquiryList: List<Enquiry>) :
    RecyclerView.Adapter<EnquiryAdapter.EnquiryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnquiryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item_enquiry, parent, false)
        return EnquiryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EnquiryViewHolder, position: Int) {
        val enquiry = enquiryList[position]
        holder.userMessage.text = "You: ${enquiry.message}"

        // Show reply if available
        if (enquiry.reply.isNotEmpty()) {
            holder.adminReply.visibility = View.VISIBLE
            holder.adminReply.text = "Admin: ${enquiry.reply}"
        } else {
            holder.adminReply.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = enquiryList.size

    class EnquiryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userMessage: TextView = view.findViewById(R.id.tvUserMessage)
        val adminReply: TextView = view.findViewById(R.id.tvAdminReply)
    }
}
