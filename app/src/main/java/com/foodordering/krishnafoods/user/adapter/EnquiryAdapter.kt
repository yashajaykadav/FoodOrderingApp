package com.foodordering.krishnafoods.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.user.viewmodel.Enquiry

class EnquiryAdapter :
    ListAdapter<Enquiry, RecyclerView.ViewHolder>(EnquiryDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_ADMIN = 1
    }

    override fun getItemViewType(position: Int): Int {
        val enquiry = getItem(position)
        return if (enquiry.reply.isEmpty()) VIEW_TYPE_USER else VIEW_TYPE_ADMIN
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            val view = inflater.inflate(R.layout.item_message_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_message_admin, parent, false)
            AdminViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val enquiry = getItem(position)
        when (holder) {
            is UserViewHolder -> holder.bind(enquiry)
            is AdminViewHolder -> holder.bind(enquiry)
        }
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val userMessage: TextView = view.findViewById(R.id.tvUserMessage)
        fun bind(enquiry: Enquiry) {
            userMessage.text = enquiry.message
        }
    }

    class AdminViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val adminReply: TextView = view.findViewById(R.id.tvAdminReply)
        fun bind(enquiry: Enquiry) {
            adminReply.text = enquiry.reply
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
