package com.foodordering.krishnafoods.admin.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.model.AdminOption

class AdminOptionAdapter(
    private val context: Context,
    private val options: List<AdminOption>
) : RecyclerView.Adapter<AdminOptionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivOptionIcon: ImageView = itemView.findViewById(R.id.ivOptionIcon)
        val tvOptionTitle: TextView = itemView.findViewById(R.id.tvOptionTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_option, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.tvOptionTitle.text = option.title
        holder.ivOptionIcon.setImageResource(option.iconResId)
        holder.ivOptionIcon.contentDescription = "${option.title} icon"
        holder.itemView.setOnClickListener {
            context.startActivity(Intent(context, option.activityClass))
        }
    }

    override fun getItemCount(): Int = options.size
}