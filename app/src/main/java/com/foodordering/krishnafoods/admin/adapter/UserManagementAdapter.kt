// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.model.User
import com.foodordering.krishnafoods.admin.util.loadUrl
import com.foodordering.krishnafoods.databinding.ItemUserManagementBinding

// Enum stays the same
enum class UserAction { DELETE, MAKE_ADMIN, MAKE_USER }

class UserManagementAdapter(
    private val onActionClick: (User, UserAction) -> Unit
) : ListAdapter<User, UserManagementAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        // Inflate using ViewBinding
        val binding = ItemUserManagementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position), onActionClick)
    }

    class UserViewHolder(private val binding: ItemUserManagementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, onAction: (User, UserAction) -> Unit) {
            binding.apply {
                tvUserName.text = user.name
                tvUserEmail.text = user.email

                // Optimization: Use the modular extension for images
                ivProfilePhoto.loadUrl(user.photoUrl)

                // Logic: Handle Role Display
                val isAdmin = user.role.equals("admin", ignoreCase = true)

                tvUserRole.text = if (isAdmin) "Admin" else "User"
                tvUserRole.setTextColor(
                    ContextCompat.getColor(root.context, if (isAdmin) R.color.colorAccent else R.color.gray)
                )

                // Logic: Toggle Button Visibility using KTX
                btnMakeAdmin.isVisible = !isAdmin
                btnMakeUser.isVisible = isAdmin

                // Click Listeners
                btnMakeAdmin.setOnClickListener { onAction(user, UserAction.MAKE_ADMIN) }
                btnMakeUser.setOnClickListener { onAction(user, UserAction.MAKE_USER) }
                btnDeleteUser.setOnClickListener { onAction(user, UserAction.DELETE) }
            }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
}