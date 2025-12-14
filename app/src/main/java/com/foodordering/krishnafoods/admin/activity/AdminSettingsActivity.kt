// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodordering.krishnafoods.admin.adapter.UserAction
import com.foodordering.krishnafoods.admin.adapter.UserManagementAdapter
import com.foodordering.krishnafoods.admin.model.User
import com.foodordering.krishnafoods.admin.viewmodel.AdminSettingsViewModel
import com.foodordering.krishnafoods.core.util.applyEdgeToEdge
import com.foodordering.krishnafoods.databinding.ActivityAdminSettingsBinding
import com.foodordering.krishnafoods.user.activity.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminSettingsBinding
    private val viewModel: AdminSettingsViewModel by viewModels()
    private val auth by lazy { FirebaseAuth.getInstance() }

    // Adapter with click callbacks
    private val adapter by lazy {
        UserManagementAdapter { user, action ->
            when (action) {
                UserAction.DELETE -> confirmDelete(user)
                UserAction.MAKE_ADMIN -> updateRole(user, "admin")
                UserAction.MAKE_USER -> updateRole(user, "user")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeData()
    }

    private fun setupUI() {
        applyEdgeToEdge(binding.root, binding.toolbar) // Custom Extension

        binding.apply {
            rvUserManagement.layoutManager = LinearLayoutManager(this@AdminSettingsActivity)
            rvUserManagement.adapter = adapter

            toolbar.setNavigationOnClickListener { finish() }
            btnLogout.setOnClickListener { logout() }

            etSearch.addTextChangedListener { text ->
                viewModel.setSearchQuery(text.toString())
            }
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.users.collectLatest { userList ->
                adapter.submitList(userList)
            }
        }
    }

    private fun updateRole(user: User, role: String) {
        if (user.id == auth.currentUser?.uid) {
            showToast("Cannot change your own role")
            return
        }
        viewModel.updateUserRole(user.id, role)
        showToast("Role updated to $role")
    }

    private fun confirmDelete(user: User) {
        if (user.id == auth.currentUser?.uid) {
            showToast("Cannot delete your own account")
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete ${user.name}?")
            .setMessage("This will permanently remove the user and all their orders/feedback.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteUser(user.id,
                    onSuccess = { showToast("User deleted successfully") },
                    onError = { showToast("Error: $it") }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finishAffinity()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}