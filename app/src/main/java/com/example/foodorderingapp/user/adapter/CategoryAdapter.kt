package com.example.foodorderingapp.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R

class CategoryAdapter(
    private val categories: List<String>,
    private var selectedCategory: String,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryText: TextView = view.findViewById(R.id.categoryTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryText.text = category

        val context = holder.itemView.context
        val bgRes = if (category == selectedCategory) R.drawable.category_selected_bg
        else R.drawable.category_unselected_bg

        holder.categoryText.setBackgroundResource(bgRes)

        holder.categoryText.setTextColor(
            ContextCompat.getColor(context,
                if (category == selectedCategory) R.color.white else R.color.black)
        )

        holder.itemView.setOnClickListener {
            selectedCategory = category
            onCategoryClick(category)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = categories.size
}
