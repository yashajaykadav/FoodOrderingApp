package com.foodordering.krishnafoods.user.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.ActivityFeedbackBinding
import com.foodordering.krishnafoods.user.util.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindow()
        setupListeners()
    }

    private fun setupWindow() {
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.lightRed)
    }

    private fun setupListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }
            btnSubmitFeedback.setOnClickListener { validateAndSubmit() }
            btnSkipFeedback.setOnClickListener { showSkipDialog() }
        }
    }

    private fun validateAndSubmit() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showToast("Please log in to submit feedback")
            return
        }

        val rating = binding.ratingBar.rating
        val feedbackText = binding.feedbackInput.text.toString().trim()

        if (rating == 0f) {
            showToast("Please provide a rating")
            return
        }
        if (feedbackText.isEmpty()) {
            binding.feedbackInput.error = "Feedback cannot be empty"
            showToast("Please provide some feedback")
            return
        }

        val feedbackData = hashMapOf<String, Any>(
            "userId" to userId,
            "rating" to rating,
            "feedback" to feedbackText,
            "timestamp" to System.currentTimeMillis(),
            "device" to android.os.Build.MODEL
        )

        sendToFirestore(feedbackData)
    }

    // FIX: Changed parameter from 'HashMap' to 'Map' to be more flexible
    private fun sendToFirestore(data: Map<String, Any>) {
        setLoadingState(true)

        firestore.collection("feedback")
            .add(data)
            .addOnSuccessListener {
                if (!isDestroyed && !isFinishing) {
                    setLoadingState(false)
                    showToast("Feedback submitted successfully!")
                    finish()
                }
            }
            .addOnFailureListener { e ->
                if (!isDestroyed && !isFinishing) {
                    setLoadingState(false)
                    showToast("Failed: ${e.localizedMessage}")
                }
            }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSubmitFeedback.isEnabled = !isLoading
            btnSkipFeedback.isEnabled = !isLoading
            feedbackInput.isEnabled = !isLoading
            ratingBar.isEnabled = !isLoading
        }
    }

    private fun showSkipDialog() {
        AlertDialog.Builder(this)
            .setTitle("Skip Feedback?")
            .setMessage("Are you sure you want to skip providing feedback?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }
}