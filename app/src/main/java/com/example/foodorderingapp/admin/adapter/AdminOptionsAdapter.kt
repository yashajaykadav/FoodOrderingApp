package com.example.foodorderingapp.admin.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.admin.model.AdminOption
import java.lang.ref.WeakReference

data class AdminOption(val title: String, val iconResId: Int, val activityClass: Class<*>)

class AdminOptionAdapter(context: Context, private val options: List<AdminOption>) :
    RecyclerView.Adapter<AdminOptionAdapter.ViewHolder>() {

    private val contextRef = WeakReference(context) // ✅ FIX: Prevents Memory Leaks

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.optionIcon)
        val title: TextView = view.findViewById(R.id.optionTitle)
        val cardView: CardView = view.findViewById(R.id.cardAdminOption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_option, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.title.text = option.title
        holder.icon.setImageResource(option.iconResId)

        holder.cardView.setOnClickListener {
            holder.cardView.isEnabled = false // ✅ Prevents multiple fast clicks

            val context = contextRef.get() ?: return@setOnClickListener
            val intent = Intent(context, option.activityClass)
            ContextCompat.startActivity(context, intent, null)

            // ✅ Re-enable button after a short delay to prevent double clicks
            holder.cardView.postDelayed({ holder.cardView.isEnabled = true }, 500)
        }
    }

    override fun getItemCount(): Int = options.size
}
