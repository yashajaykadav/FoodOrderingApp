// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.admin.model.Advertisement
import com.foodordering.krishnafoods.admin.util.loadUrl
import com.foodordering.krishnafoods.databinding.ItemAdvertisementBinding

class AdvertisementAdapter(
    private val onActionClick: (Advertisement, String) -> Unit // "edit" or "delete"
) : ListAdapter<Advertisement, AdvertisementAdapter.ViewHolder>(AdvertisementDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdvertisementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAdvertisementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ad: Advertisement) {
            // Use modular extension for image loading
            binding.ivBanner.loadUrl(ad.imageUrl)

            // Click Listeners
            binding.root.setOnClickListener { onActionClick(ad, "edit") }
            binding.btnDelete.setOnClickListener { onActionClick(ad, "delete") }
        }
    }

    class AdvertisementDiffCallback : DiffUtil.ItemCallback<Advertisement>() {
        override fun areItemsTheSame(oldItem: Advertisement, newItem: Advertisement) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Advertisement, newItem: Advertisement) = oldItem == newItem
    }
}