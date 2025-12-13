/*
 * Developed by: Yash Kadav
 * Email: yashkadav52@gmail.com
 * Project: Krishna Foods (ADCET CSE 2026)
 */

package com.foodordering.krishnafoods.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.core.model.Enquiry
import com.foodordering.krishnafoods.databinding.AdminItemEnquiryBinding // Make sure this is generated

class EnquiryAdapter(
    private val onReplyClicked: (enquiry: Enquiry, replyText: String) -> Unit
) : ListAdapter<Enquiry, EnquiryAdapter.EnquiryViewHolder>(EnquiryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnquiryViewHolder {
        val binding = AdminItemEnquiryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EnquiryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EnquiryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EnquiryViewHolder(private val binding: AdminItemEnquiryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(enquiry: Enquiry) {
            binding.apply {
                // Set User Details
                tvUserName.text = enquiry.userName.ifEmpty { "User" }
                tvUserMessage.text = enquiry.message

                // Check if already replied
                val hasReplied = enquiry.reply.isNotEmpty()

                // Logic: Show Reply Text if replied, otherwise show Input Field
                if (hasReplied) {
                    tvAdminReply.isVisible = true
                    tvAdminReply.text = "You: ${enquiry.reply}" // "You" indicates Admin

                    layoutReplyInput.isVisible = false // Hide input area
                } else {
                    tvAdminReply.isVisible = false
                    layoutReplyInput.isVisible = true // Show input area
                    etReply.setText("") // Clear previous text
                }

                // Click Listener
                btnSendReply.setOnClickListener {
                    val replyText = etReply.text.toString().trim()
                    if (replyText.isNotEmpty()) {
                        onReplyClicked(enquiry, replyText)
                    } else {
                        etReply.error = "Enter a reply"
                    }
                }
            }
        }
    }

    class EnquiryDiffCallback : DiffUtil.ItemCallback<Enquiry>() {
        override fun areItemsTheSame(oldItem: Enquiry, newItem: Enquiry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Enquiry, newItem: Enquiry) = oldItem == newItem
    }
}