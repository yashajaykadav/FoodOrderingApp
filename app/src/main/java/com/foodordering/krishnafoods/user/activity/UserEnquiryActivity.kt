/*
 * Developed by: Yash Kadav
 * Email: yashkadav52@gmail.com
 * Project: Krishna Foods (ADCET CSE 2026)
 */

package com.foodordering.krishnafoods.user.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.core.viewmodel.EnquiryCoreViewModel
import com.foodordering.krishnafoods.databinding.ActivityUserEnquiryBinding
import com.foodordering.krishnafoods.user.adapter.EnquiryAdapter
import com.foodordering.krishnafoods.user.util.gone
import com.foodordering.krishnafoods.user.util.playSound
import com.foodordering.krishnafoods.user.util.showToast
import com.foodordering.krishnafoods.user.util.visible
import com.google.firebase.auth.FirebaseAuth

class UserEnquiryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserEnquiryBinding

    // 1. Initialize the Core ViewModel
    private val viewModel: EnquiryCoreViewModel by viewModels()
    private val adapter by lazy { EnquiryAdapter() }

    private val userId: String by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserEnquiryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (userId.isEmpty()) {
            showToast("Login required")
            finish()
            return
        }

        setupUI()
        setupObservers()

        // 2. Start Listening in "User Mode" (pass userId)
        viewModel.startListening(userId)
    }

    private fun setupUI() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.lightRed)
        binding.btnToolbar.setNavigationOnClickListener { finish() }

        binding.recyclerViewEnquiries.apply {
            layoutManager = LinearLayoutManager(this@UserEnquiryActivity).apply {
                stackFromEnd = true
            }
            adapter = this@UserEnquiryActivity.adapter
        }

        binding.btnSendMessage.setOnClickListener {
            val msg = binding.etUserMessage.text.toString().trim()
            if (msg.isNotEmpty()) {
                // 3. Use Core function 'sendEnquiry'
                viewModel.sendEnquiry(userId, msg)
                applicationContext.playSound(R.raw.message)
                binding.etUserMessage.text?.clear()
            }
        }
    }

    private fun setupObservers() {
        // A. Observe List
        viewModel.enquiries.observe(this) { list ->
            adapter.submitList(list) {
                // Scroll to bottom when new message arrives
                if (list.isNotEmpty()) {
                    binding.recyclerViewEnquiries.smoothScrollToPosition(list.size - 1)
                }
            }
        }

        // B. Observe Loading (Renamed from isLoading -> loading)
        viewModel.loading.observe(this) { isLoading ->
            if (isLoading) binding.progressBar.visible() else binding.progressBar.gone()
            binding.btnSendMessage.isEnabled = !isLoading
        }

        // C. Observe Message (Renamed from toastMessage -> message)
        viewModel.message.observe(this) { msg ->
            msg?.let {
                showToast(it)
                viewModel.clearMessage()
            }
        }
    }
}