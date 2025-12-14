// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.FeedbackAdapter
import com.foodordering.krishnafoods.admin.model.Feedback
import com.foodordering.krishnafoods.admin.repository.FeedbackRepository
import com.foodordering.krishnafoods.core.util.EndlessScrollListener
import com.foodordering.krishnafoods.core.util.applyEdgeToEdge
import com.foodordering.krishnafoods.databinding.AdminActivityFeedbackBinding
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.launch

class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: AdminActivityFeedbackBinding
    private lateinit var adapter: FeedbackAdapter
    private lateinit var scrollListener: EndlessScrollListener

    // Dependencies
    private val repository = FeedbackRepository()
    private val feedbackList = mutableListOf<Feedback>()
    private var lastVisible: DocumentSnapshot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup ViewBinding
        binding = AdminActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        loadFeedbackData(isLoadMore = false)
    }

    private fun setupUI() {
        // Edge to Edge Helper
        applyEdgeToEdge(binding.root, binding.feedToolbar)

        // Toolbar & StatusBar
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        binding.feedToolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        adapter = FeedbackAdapter(feedbackList)

        // Modular Scroll Listener
        scrollListener = EndlessScrollListener(layoutManager) {
            loadFeedbackData(isLoadMore = true)
        }

        binding.recyclerViewFeedback.apply {
            this.layoutManager = layoutManager
            this.adapter = adapter
            addOnScrollListener(scrollListener)
        }
    }

    private fun loadFeedbackData(isLoadMore: Boolean) {
        // Prevent multiple calls
        scrollListener.setLoading(true)
        if (!isLoadMore) showLoading(true)

        lifecycleScope.launch {
            try {
                // Fetch Data via Repository
                val (newItems, newLastDoc) = repository.getFeedback(if (isLoadMore) lastVisible else null)

                if (!isLoadMore) {
                    feedbackList.clear()
                }

                if (newItems.isNotEmpty()) {
                    lastVisible = newLastDoc
                    feedbackList.addAll(newItems)
                    adapter.notifyDataSetChanged()

                    showEmptyState(false)
                } else {
                    // No more data to load
                    scrollListener.setLastPage(true)
                    if (feedbackList.isEmpty()) showEmptyState(true)
                }

            } catch (e: Exception) {
                Toast.makeText(this@FeedbackActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                if (feedbackList.isEmpty()) showEmptyState(true)
            } finally {
                // Reset states
                scrollListener.setLoading(false)
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.lottieLoadingFeedback.visibility = View.VISIBLE
            binding.recyclerViewFeedback.visibility = View.GONE
        } else {
            binding.lottieLoadingFeedback.visibility = View.GONE
            binding.recyclerViewFeedback.visibility = View.VISIBLE
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvEmptyFeedback.visibility = View.VISIBLE
            binding.ivEmptyFeedback.visibility = View.VISIBLE
            binding.recyclerViewFeedback.visibility = View.GONE
        } else {
            binding.tvEmptyFeedback.visibility = View.GONE
            binding.ivEmptyFeedback.visibility = View.GONE
            binding.recyclerViewFeedback.visibility = View.VISIBLE
        }
    }
}