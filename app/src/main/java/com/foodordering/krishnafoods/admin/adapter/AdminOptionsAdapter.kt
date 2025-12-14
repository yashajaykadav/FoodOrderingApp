// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.admin.model.AdminOption
import com.foodordering.krishnafoods.databinding.ItemAdminOptionBinding

class AdminOptionAdapter(
    private val context: Context,
    private val options: List<AdminOption>
) : RecyclerView.Adapter<AdminOptionAdapter.ViewHolder>() {

    // ViewBinding makes the ViewHolder strictly typed
    inner class ViewHolder(val binding: ItemAdminOptionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminOptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]

        holder.binding.apply {
            tvOptionTitle.text = option.title
            ivOptionIcon.setImageResource(option.iconResId)

            // Optimization: Set content description for accessibility
            ivOptionIcon.contentDescription = option.title

            // Handle Click
            root.setOnClickListener {
                try {
                    val intent = Intent(context, option.activityClass)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getItemCount(): Int = options.size
}