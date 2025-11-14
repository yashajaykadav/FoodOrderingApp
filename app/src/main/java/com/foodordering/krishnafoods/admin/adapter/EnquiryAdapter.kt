
package com.foodordering.krishnafoods.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.model.Enquiry

// Step 1: Add a listener to the constructor
class EnquiryAdapter(private val onReplyClicked: (enquiry: Enquiry, replyText: String) -> Unit)
    : ListAdapter<Enquiry, EnquiryAdapter.EnquiryViewHolder>(EnquiryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnquiryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.admin_item_enquiry, parent, false)
        return EnquiryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EnquiryViewHolder, position: Int) {
        val enquiry = getItem(position)
        holder.bind(enquiry)
    }

    // Inner class to prevent memory leaks and handle binding logic
    inner class EnquiryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        private val tvUserMessage: TextView = view.findViewById(R.id.tvUserMessage)
        private val tvAdminReply: TextView = view.findViewById(R.id.tvAdminReply)
        private val etReply: EditText = view.findViewById(R.id.etReply)
        private val btnSendReply: Button = view.findViewById(R.id.btnSendReply)

        fun bind(enquiry: Enquiry) {
            tvUserName.text = "User: ${enquiry.userName}"
            tvUserMessage.text = "User Message:\n${enquiry.message}"

            val hasReplied = enquiry.reply.isNotEmpty()
            tvAdminReply.visibility = if (hasReplied) View.VISIBLE else View.GONE
            etReply.visibility = if (hasReplied) View.GONE else View.VISIBLE
            btnSendReply.visibility = if (hasReplied) View.GONE else View.VISIBLE

            if (hasReplied) {
                tvAdminReply.text = "Admin Reply:\n${enquiry.reply}"
            }

            btnSendReply.setOnClickListener {
                val replyText = etReply.text.toString().trim()
                if (replyText.isNotEmpty()) {
                    // Step 2: Use the callback to pass the event to the Activity.
                    // The adapter no longer updates Firestore itself.
                    onReplyClicked(enquiry, replyText)
                }
            }
        }
    }

    class EnquiryDiffCallback : DiffUtil.ItemCallback<Enquiry>() {
        override fun areItemsTheSame(oldItem: Enquiry, newItem: Enquiry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Enquiry, newItem: Enquiry): Boolean {
            return oldItem == newItem
        }
    }
}