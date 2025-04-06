package com.example.foodorderingapp.admin.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.admin.adapter.FeedbackAdapter
import com.example.foodorderingapp.admin.model.Feedback
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeedbackAdapter
    private val db = FirebaseFirestore.getInstance()
    private var feedbackList = mutableListOf<Feedback>()

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_feedback)

        recyclerView = findViewById(R.id.recyclerViewFeedback)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = FeedbackAdapter(feedbackList)
        recyclerView.adapter = adapter

        fetchFeedback()
    }

    private fun fetchFeedback() {
        db.collection("feedback").get().addOnSuccessListener { documents ->
            feedbackList.clear()
            for (doc in documents) {
                val userId = doc.getString("userId") ?: ""
                val feedbackId = doc.id

                if (userId.isBlank()) continue  // ✅ Prevent crash

                val feedback = Feedback(
                    feedbackId = feedbackId,
                    userId = userId,
                    message = doc.getString("feedback") ?: "",
                    rating = doc.getLong("rating")?.toInt() ?: 0
                )

                feedbackList.add(feedback)

                // Fetch user name from users collection
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { userDoc ->
                        feedbackList.find { it.feedbackId == feedbackId }?.userName =
                            userDoc.getString("name") ?: "Unknown User"
                        adapter.notifyDataSetChanged()
                    }
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener { e ->
            Log.e("Feedback", "Error fetching feedback", e)
        }
    }

//    private fun showEmptyState() {
//        recyclerView.visibility = View.GONE
//        tvEmptyState.visibility = View.VISIBLE
//        findViewById<ImageView>(R.id.ivEmptyState).visibility = View.VISIBLE
//        tvEmptyState.text = "No feedback to review yet"
//    }
}
