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
import com.foodordering.krishnafoods.admin.model.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

// Enum to define the actions
enum class UserAction {
    DELETE, MAKE_ADMIN, MAKE_USER
}

class UserManagementAdapter(
    private val onActionClick: (User, UserAction) -> Unit
) : ListAdapter<User, UserManagementAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_management, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Find views based on your XML layout IDs
        private val tvUserName: MaterialTextView = itemView.findViewById(R.id.tvUserName)
        private val tvUserEmail: MaterialTextView = itemView.findViewById(R.id.tvUserEmail)
        private val tvUserRole: MaterialTextView = itemView.findViewById(R.id.tvUserRole)
        private val ivUserImage: ImageView = itemView.findViewById(R.id.ivProfilePhoto)
        // Find the buttons
        private val btnMakeAdmin: MaterialButton = itemView.findViewById(R.id.btnMakeAdmin)
        private val btnMakeUser: MaterialButton = itemView.findViewById(R.id.btnMakeUser)
        private val btnDeleteUser: MaterialButton = itemView.findViewById(R.id.btnDeleteUser)


        fun bind(user: User) {
            tvUserName.text = user.name
            tvUserEmail.text = user.email
            tvUserRole.text = user.role.replaceFirstChar { it.uppercase() } // "Admin" or "User"

            // Set role text color
            val roleColor = if (user.role == "admin") {
                itemView.context.getColor(R.color.colorAccent)
            } else {
                itemView.context.getColor(R.color.gray) // Assuming you have a 'grey' color
            }
            tvUserRole.setTextColor(roleColor)

            // Load user image
            Glide.with(itemView.context)
                .load(user.photoUrl)
                .placeholder(R.drawable.user) // A default placeholder
                .error(R.drawable.default_img)       // An error placeholder
                .into(ivUserImage)

            // --- Set Button Visibility ---
            if (user.role == "admin") {
                btnMakeAdmin.visibility = View.GONE
                btnMakeUser.visibility = View.VISIBLE
            } else {
                btnMakeAdmin.visibility = View.VISIBLE
                btnMakeUser.visibility = View.GONE
            }

            // --- Set Button Click Listeners ---
            btnMakeAdmin.setOnClickListener {
                onActionClick(user, UserAction.MAKE_ADMIN)
            }

            btnMakeUser.setOnClickListener {
                onActionClick(user, UserAction.MAKE_USER)
            }

            btnDeleteUser.setOnClickListener {
                onActionClick(user, UserAction.DELETE)
            }
        }
    }

    // DiffUtil for efficient list updates
    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}