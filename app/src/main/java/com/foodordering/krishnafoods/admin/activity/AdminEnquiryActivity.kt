package com.foodordering.krishnafoods.admin.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.EnquiryAdapter
import com.foodordering.krishnafoods.admin.model.Enquiry
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class AdminEnquiryActivity : AppCompatActivity() {

    private lateinit var adapter: EnquiryAdapter
    private val db = FirebaseFirestore.getInstance()
    private var enquiryListener: ListenerRegistration? = null

    // Cache for user data to avoid repeated fetches
    private val userCache = mutableMapOf<String, String>()
    private val enquiryList = mutableListOf<Enquiry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.admin_activity_enquiry)

        // Setup Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.enqueryTollbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Set status bar color using the modern, recommended approach
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        // Setup RecyclerView
        // Pass the handler function for when a reply is sent from the adapter
        adapter = EnquiryAdapter { enquiry, replyText ->
            sendReply(enquiry, replyText)
        }
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewEnquiries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupEnquiryListener()
    }
    private fun setupEnquiryListener() {
        enquiryListener = db.collectionGroup("enquiries")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) {
                    return@addSnapshotListener
                }

                for (change in snapshots.documentChanges) {
                    val doc = change.document
                    val userId = doc.reference.parent.parent?.id ?: continue
                    val enquiry = doc.toObject(Enquiry::class.java).copy(id = doc.id, userId = userId)

                    when (change.type) {
                        DocumentChange.Type.ADDED -> {
                            enquiryList.add(enquiry)
                            fetchUserNameAndRefresh(enquiry)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val index = enquiryList.indexOfFirst { it.id == enquiry.id }
                            if (index != -1) {
                                enquiryList[index] = enquiry
                                fetchUserNameAndRefresh(enquiry)
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            enquiryList.removeAll { it.id == enquiry.id }
                        }
                    }
                }
                updateAdapter()
            }
    }

    /**
     * Handles the logic for sending a reply to Firestore.
     * This is called from the adapter via the callback.
     */
    private fun sendReply(enquiry: Enquiry, replyText: String) {
        val enquiryDocRef = db.collection("users")
            .document(enquiry.userId)
            .collection("enquiries")
            .document(enquiry.id)

        enquiryDocRef.update("reply", replyText)
            .addOnSuccessListener {
                Toast.makeText(this, "Reply sent!", Toast.LENGTH_SHORT).show()
                // No manual list update needed. The listener will handle it automatically.
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send reply!", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Fetches the user's name if not already cached and triggers a UI update.
     */
    private fun fetchUserNameAndRefresh(enquiry: Enquiry) {
        val userId = enquiry.userId
        if (userCache.containsKey(userId)) {
            val index = enquiryList.indexOfFirst { it.id == enquiry.id }
            if (index != -1) {
                enquiryList[index].userName = userCache[userId]!!
                updateAdapter()
            }
        } else {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { userDoc ->
                    val name = userDoc.getString("name") ?: "Unknown User"
                    userCache[userId] = name
                    enquiryList.filter { it.userId == userId }.forEach { it.userName = name }
                    updateAdapter()
                }
        }
    }

    /**
     * Submits the sorted list to the adapter.
     */
    private fun updateAdapter() {
        adapter.submitList(enquiryList.sortedBy { it.timestamp }.toList())
    }

    override fun onDestroy() {
        super.onDestroy()
        // Important: Remove the listener to prevent memory leaks
        enquiryListener?.remove()
    }
}