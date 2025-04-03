package com.example.foodorderingapp.admin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.admin.model.Enquiry
import com.google.firebase.firestore.FirebaseFirestore

class EnquiryAdapter(private val enquiryList: MutableList<Enquiry>, private val context: Context) :
    RecyclerView.Adapter<EnquiryAdapter.EnquiryViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnquiryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.admin_item_enquiry, parent, false)
        return EnquiryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EnquiryViewHolder, position: Int) {
        val enquiry = enquiryList[position]

        holder.tvUserName.text = "User: ${enquiry.userName}" // 🔥 Display userName
        holder.tvUserMessage.text = "User Message:\n${enquiry.message}"

        if (enquiry.reply.isNotEmpty()) {
            holder.tvAdminReply.visibility = View.VISIBLE
            holder.tvAdminReply.text = "Admin Reply:\n${enquiry.reply}"
            holder.etReply.visibility = View.GONE
            holder.btnSendReply.visibility = View.GONE
        } else {
            holder.tvAdminReply.visibility = View.GONE
            holder.etReply.visibility = View.VISIBLE
            holder.btnSendReply.visibility = View.VISIBLE
        }

        holder.btnSendReply.setOnClickListener {
            val replyText = holder.etReply.text.toString().trim()
            if (replyText.isNotEmpty()) {
                db.collection("enquiries").document(enquiry.id)
                    .update("reply", replyText)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Reply sent!", Toast.LENGTH_SHORT).show()
                        enquiry.reply = replyText
                        holder.tvAdminReply.visibility = View.VISIBLE
                        holder.tvAdminReply.text = "Admin Reply:\n$replyText"
                        holder.etReply.visibility = View.GONE
                        holder.btnSendReply.visibility = View.GONE
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to send reply!", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun getItemCount(): Int = enquiryList.size

    class EnquiryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvUserMessage: TextView = view.findViewById(R.id.tvUserMessage)
        val tvAdminReply: TextView = view.findViewById(R.id.tvAdminReply)
        val etReply: EditText = view.findViewById(R.id.etReply)
        val btnSendReply: Button = view.findViewById(R.id.btnSendReply)
    }
}
