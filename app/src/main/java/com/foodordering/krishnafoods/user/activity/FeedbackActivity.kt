package com.foodordering.krishnafoods.user.activity

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.foodordering.krishnafoods.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackActivity : AppCompatActivity() {

    private lateinit var ratingBar: RatingBar
    private lateinit var feedbackInput: EditText
    private lateinit var btnSubmitFeedback: Button
    private lateinit var btnSkipFeedback: Button
    private lateinit var progressBar: ProgressBar

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        window.statusBarColor = ContextCompat.getColor(this,R.color.sky_color)

        // Initialize views
        ratingBar = findViewById(R.id.ratingBar)
        feedbackInput = findViewById(R.id.feedbackInput)
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback)
        btnSkipFeedback = findViewById(R.id.btnSkipFeedback)
        progressBar = findViewById(R.id.progressBar)

        // Submit Feedback Button Click
        btnSubmitFeedback.setOnClickListener { submitFeedback() }

        // Skip Feedback Button Click
        btnSkipFeedback.setOnClickListener { skipFeedback() }
    }

    private fun submitFeedback() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please log in to submit feedback", Toast.LENGTH_SHORT).show()
            return
        }

        val rating = ratingBar.rating
        val feedback = feedbackInput.text.toString().trim()

        // Validate rating
        if (rating == 0f) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate feedback (optional)
        if (feedback.isEmpty()) {
            Toast.makeText(this, "Please provide some feedback", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress bar
        progressBar.visibility = View.VISIBLE
        btnSubmitFeedback.isEnabled = false

        val feedbackData = mapOf(
            "userId" to userId,
            "rating" to rating,
            "feedback" to feedback,
            "timestamp" to System.currentTimeMillis()
        )

        // Save feedback to Firebase Firestore
        firestore.collection("feedback")
            .add(feedbackData)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                btnSubmitFeedback.isEnabled = true
                Toast.makeText(this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show()
                finish() // Close feedback screen after submission
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnSubmitFeedback.isEnabled = true
                Toast.makeText(this, "Failed to submit feedback: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun skipFeedback() {
        AlertDialog.Builder(this)
            .setTitle("Skip Feedback?")
            .setMessage("Are you sure you want to skip providing feedback?")
            .setPositiveButton("Yes") { _, _ ->
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
