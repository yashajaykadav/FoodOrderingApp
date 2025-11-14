package com.foodordering.krishnafoods.admin.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.FeedbackAdapter
import com.foodordering.krishnafoods.admin.model.Feedback
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeedbackAdapter
    private lateinit var lottieLoading: LottieAnimationView
    private lateinit var tvEmpty: TextView
    private lateinit var ivEmpty: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val feedbackList = mutableListOf<Feedback>()
    private val pagesize = 10
    private var lastVisible: DocumentSnapshot? = null
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.admin_activity_feedback)

        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)

        recyclerView = findViewById(R.id.recyclerViewFeedback)
        lottieLoading = findViewById(R.id.lottieLoadingFeedback)
        tvEmpty = findViewById(R.id.tvEmptyFeedback)
        ivEmpty = findViewById(R.id.ivEmptyFeedback)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FeedbackAdapter(feedbackList)
        recyclerView.adapter = adapter

        val toolbar = findViewById<MaterialToolbar>(R.id.feed_toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rv.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisible = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading &&
                    (visibleItemCount + firstVisible >= totalItemCount) &&
                    firstVisible >= 0
                ) {
                    fetchFeedback(loadMore = true)
                }
            }
        })

        fetchFeedback()
    }

    private fun fetchFeedback(loadMore: Boolean = false) {
        if (isLoading) return
        isLoading = true

        if (!loadMore) {
            lottieLoading.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            ivEmpty.visibility = View.GONE
            feedbackList.clear()
            adapter.notifyDataSetChanged()
            lastVisible = null
        }

        var query = db.collection("feedback")
            .orderBy("timestamp")
            .limit(pagesize.toLong())

        lastVisible?.let { query = query.startAfter(it) }

        query.get()
            .addOnSuccessListener { documents ->
                lottieLoading.visibility = View.GONE
                isLoading = false

                if (documents.isEmpty) {
                    if (!loadMore && feedbackList.isEmpty()) showEmptyState()
                    return@addOnSuccessListener
                }

                lastVisible = documents.documents.last()

                val userIdsToFetch = mutableSetOf<String>()
                val newFeedbacks = mutableListOf<Feedback>()

                for (doc in documents) {
                    val userId = doc.getString("userId") ?: continue
                    userIdsToFetch.add(userId)

                    newFeedbacks.add(
                        Feedback(
                            feedbackId = doc.id,
                            userId = userId,
                            message = doc.getString("feedback") ?: "",
                            rating = doc.getLong("rating")?.toInt() ?: 0
                        )
                    )
                }

                if (userIdsToFetch.isEmpty()) {
                    feedbackList.addAll(newFeedbacks)
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                // Batch fetch usernames
                db.collection("users").whereIn(FieldPath.documentId(), userIdsToFetch.toList())
                    .get()
                    .addOnSuccessListener { userDocs ->
                        for (userDoc in userDocs) {
                            val id = userDoc.id
                            val name = userDoc.getString("name") ?: "Unknown User"
                            newFeedbacks.filter { it.userId == id }.forEach { it.userName = name }
                        }

                        feedbackList.addAll(newFeedbacks)
                        adapter.notifyDataSetChanged()
                        recyclerView.visibility = View.VISIBLE
                    }
                    .addOnFailureListener {
                        feedbackList.addAll(newFeedbacks)
                        adapter.notifyDataSetChanged()
                        recyclerView.visibility = View.VISIBLE
                    }
            }
            .addOnFailureListener {
                lottieLoading.visibility = View.GONE
                isLoading = false
                Toast.makeText(this, "Failed to fetch feedback!", Toast.LENGTH_SHORT).show()
                if (feedbackList.isEmpty()) showEmptyState()
            }
    }

    private fun showEmptyState() {
        tvEmpty.visibility = View.VISIBLE
        ivEmpty.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        lottieLoading.visibility = View.GONE
    }
}
