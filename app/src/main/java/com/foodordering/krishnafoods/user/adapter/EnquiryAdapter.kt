/*
 * Developed by: Yash Kadav
 * Email: yashkadav52@gmail.com
 * Project: Krishna Foods (ADCET CSE 2026)
 */

package com.foodordering.krishnafoods.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.core.model.Enquiry

class EnquiryAdapter : ListAdapter<Enquiry, EnquiryAdapter.EnquiryViewHolder>(EnquiryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnquiryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_enquiry_message, parent, false)
        return EnquiryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EnquiryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EnquiryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // We use one layout that contains both User and Admin bubbles (controlled by visibility)
        private val tvUserMsg: TextView = view.findViewById(R.id.tvUserMessage)
        private val layoutAdminReply: LinearLayout = view.findViewById(R.id.layoutAdminReply)
        private val tvAdminMsg: TextView = view.findViewById(R.id.tvAdminMessage)

        fun bind(enquiry: Enquiry) {
            // Always show user query
            tvUserMsg.text = enquiry.message

            // Show admin reply only if it exists
            if (enquiry.reply.isNotEmpty()) {
                layoutAdminReply.isVisible = true
                tvAdminMsg.text = enquiry.reply
            } else {
                layoutAdminReply.isVisible = false
            }
        }
    }

    class EnquiryDiffCallback : DiffUtil.ItemCallback<Enquiry>() {
        override fun areItemsTheSame(oldItem: Enquiry, newItem: Enquiry): Boolean {
            // Ensure your Firestore Enquiry object has an ID field!
            // If not, compare timestamps or use objects directly
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: Enquiry, newItem: Enquiry): Boolean {
            return oldItem == newItem
        }
    }
}