/*
 * Developed by: Yash Kadav
 * Email: yashkadav52@gmail.com
 * Project: Krishna Foods (ADCET CSE 2026)
 */

package com.foodordering.krishnafoods.user.adapter

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.ItemBannerBinding

class BannerAdapter(
    private val onBannerClick: ((String) -> Unit)? = null
) : ListAdapter<String, BannerAdapter.BannerViewHolder>(BannerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BannerViewHolder(private val binding: ItemBannerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String) {
            val context = binding.root.context
            // Convert 16dp to pixels for consistent looking corners on all devices
            val radiusPx = (16 * Resources.getSystem().displayMetrics.density).toInt()

            // Inside onBind in BannerAdapter.kt

            Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop() // REMOVED RoundedCorners, let XML handle it
                .placeholder(R.drawable.default_img)
                .error(R.drawable.default_img)
                .into(binding.bannerImage)

            binding.root.setOnClickListener { view ->
                // Small bounce animation on click
                view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    onBannerClick?.invoke(imageUrl)
                }.start()
            }
        }
    }

    class BannerDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}