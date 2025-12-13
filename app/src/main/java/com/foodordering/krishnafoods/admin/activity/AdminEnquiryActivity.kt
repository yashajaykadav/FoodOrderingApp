/*
 * Developed by: Yash Kadav
 * Email: yashkadav52@gmail.com
 * Project: Krishna Foods (ADCET CSE 2026)
 */

package com.foodordering.krishnafoods.admin.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.EnquiryAdapter
import com.foodordering.krishnafoods.core.viewmodel.EnquiryCoreViewModel
import com.foodordering.krishnafoods.databinding.AdminActivityEnquiryBinding
import com.foodordering.krishnafoods.user.util.showToast

class AdminEnquiryActivity : AppCompatActivity() {

    // 1. Initialize the Core ViewModel
    private val viewModel: EnquiryCoreViewModel by viewModels()
    private lateinit var binding: AdminActivityEnquiryBinding
    private lateinit var adapter: EnquiryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AdminActivityEnquiryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()

        // 2. Start Listening in "Admin Mode" (userId = null)
        viewModel.startListening(userId = null)
    }

    private fun setupUI() {
        // Status Bar Styling
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        binding.enqueryTollbar.setNavigationOnClickListener { finish() }

        // Adapter Setup
        adapter = EnquiryAdapter { enquiry, replyText ->
            if (replyText.isBlank()) {
                showToast("Reply cannot be empty")
            } else {
                // 3. Send Reply (No callback needed here, we observe the result below)
                viewModel.sendReply(enquiry, replyText)
            }
        }

        binding.recyclerViewEnquiries.apply {
            layoutManager = LinearLayoutManager(this@AdminEnquiryActivity)
            adapter = this@AdminEnquiryActivity.adapter
        }
    }

    private fun setupObservers() {
        // A. Observe List Data
        viewModel.enquiries.observe(this) { list ->
            adapter.submitList(list)
        }

        // B. Observe Loading State (Optional, if you have a ProgressBar in XML)
        viewModel.loading.observe(this) { isLoading ->
            // if (isLoading) binding.progressBar.visible() else binding.progressBar.gone()
        }

        // C. Observe Success/Error Messages (Toast Logic)
        viewModel.message.observe(this) { msg ->
            msg?.let {
                showToast(it)
                viewModel.clearMessage() // Clear immediately so it doesn't show again on rotation
            }
        }
    }
}