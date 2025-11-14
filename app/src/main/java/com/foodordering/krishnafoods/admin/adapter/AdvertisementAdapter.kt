package com.foodordering.krishnafoods.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.model.Advertisement
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class AdvertisementAdapter(
    private val onActionClick: (Advertisement, String) -> Unit // "edit", "delete"
) : ListAdapter<Advertisement, AdvertisementAdapter.ViewHolder>(AdvertisementDiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cardView: MaterialCardView = view.findViewById(R.id.cardView)
        private val ivBanner: ImageView = view.findViewById(R.id.ivBanner)
        private val btnDelete: MaterialButton = view.findViewById(R.id.btnDelete)

        fun bind(ad: Advertisement) {
            // Load image
            Glide.with(itemView.context)
                .load(ad.imageUrl)
                .placeholder(R.drawable.default_img)
                .error(R.drawable.default_img)
                .into(ivBanner)

            // Click listeners
            cardView.setOnClickListener { onActionClick(ad, "edit") }
            btnDelete.setOnClickListener { onActionClick(ad, "delete") }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_advertisement, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AdvertisementDiffCallback : DiffUtil.ItemCallback<Advertisement>() {
        override fun areItemsTheSame(oldItem: Advertisement, newItem: Advertisement): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Advertisement, newItem: Advertisement): Boolean {
            return oldItem == newItem
        }
    }
}
