// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.activity

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.AdvertisementAdapter
import com.foodordering.krishnafoods.admin.model.Advertisement
import com.foodordering.krishnafoods.admin.util.CloudinaryHelper
import com.foodordering.krishnafoods.admin.util.loadUrl
import com.foodordering.krishnafoods.admin.viewmodel.AdvertisementViewModel
import com.foodordering.krishnafoods.core.util.applyEdgeToEdge
import com.foodordering.krishnafoods.databinding.ActivityManageAdvertismentBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ManageAdvertisementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageAdvertismentBinding
    private val viewModel: AdvertisementViewModel by viewModels()
    private lateinit var adapter: AdvertisementAdapter

    // State
    private var selectedUri: Uri? = null
    private var currentAdId: String? = null
    private var currentUrl: String? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedUri = it
            binding.ivAdImage.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageAdvertismentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Init Utils
        CloudinaryHelper.init(this)

        setupUI()
        setupRecycler()
        observeViewModel()
    }

    private fun setupUI() {
        applyEdgeToEdge(binding.root, binding.toolbar)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }

            btnSelectImage.setOnClickListener { imagePicker.launch("image/*") }

            btnAddAd.setOnClickListener {
                viewModel.saveAdvertisement(selectedUri, currentAdId, currentUrl)
            }
        }
    }

    private fun setupRecycler() {
        adapter = AdvertisementAdapter { ad, action ->
            when (action) {
                "edit" -> fillFormForEdit(ad)
                "delete" -> confirmDelete(ad)
            }
        }
        binding.rvAdvertisements.apply {
            layoutManager = LinearLayoutManager(this@ManageAdvertisementActivity)
            adapter = this@ManageAdvertisementActivity.adapter
        }
    }

    private fun observeViewModel() {
        // Observe Ads List
        lifecycleScope.launch {
            viewModel.ads.collectLatest { ads ->
                adapter.submitList(ads)
                binding.tvEmptyState.visibility = if (ads.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        // Observe Loading State
        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { loading ->
                binding.btnAddAd.isEnabled = !loading
                binding.btnAddAd.text = if (loading) "Processing..." else
                    (if (currentAdId == null) "Upload Advertisement" else "Update Advertisement")
            }
        }

        // Observe Success/Failure Messages
        lifecycleScope.launch {
            viewModel.operationStatus.collectLatest { result ->
                result?.let {
                    if (it.isSuccess) {
                        Toast.makeText(this@ManageAdvertisementActivity, it.getOrNull(), Toast.LENGTH_SHORT).show()
                        resetForm()
                    } else {
                        Toast.makeText(this@ManageAdvertisementActivity, "Error: ${it.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.resetStatus()
                }
            }
        }
    }

    private fun fillFormForEdit(ad: Advertisement) {
        currentAdId = ad.id
        currentUrl = ad.imageUrl
        selectedUri = null // Reset selected local file

        binding.ivAdImage.loadUrl(ad.imageUrl) // Using your extension
        binding.btnAddAd.text = "Update Advertisement"
        binding.scrollView.scrollTo(0, 0)
    }

    private fun confirmDelete(ad: Advertisement) {
        AlertDialog.Builder(this)
            .setTitle("Delete Ad")
            .setMessage("Are you sure?")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteAdvertisement(ad.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetForm() {
        currentAdId = null
        currentUrl = null
        selectedUri = null
        binding.ivAdImage.setImageResource(R.drawable.default_img)
        binding.btnAddAd.text = "Upload Advertisement"
    }
}