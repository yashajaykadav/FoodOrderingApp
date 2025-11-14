package com.foodordering.krishnafoods.user.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.user.adapter.EnquiryAdapter
import com.foodordering.krishnafoods.user.viewmodel.Enquiry
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class UserEnquiryActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private var enquiryListener: ListenerRegistration? = null

    private lateinit var etMessage: EditText
    private lateinit var btnSend: MaterialButton  // Changed to MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EnquiryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_user_enquiry)

        window.statusBarColor = ContextCompat.getColor(this, R.color.lightRed)

        etMessage = findViewById(R.id.etUserMessage)
        btnSend = findViewById(R.id.btnSendMessage)
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerViewEnquiries)

        val toolbar = findViewById<MaterialToolbar>(R.id.btnToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Setup RecyclerView
        adapter = EnquiryAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = adapter

        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendEnquiry(message)
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setupRealtimeListener()
    }

    override fun onStop() {
        super.onStop()
        enquiryListener?.remove()
    }

    private fun sendEnquiry(message: String) {
        progressBar.visibility = View.VISIBLE
        btnSend.isEnabled = false
        btnSend.icon?.let { it.alpha = 0 } // hide icon safely

        if (message.length > 500) {
            Toast.makeText(this, "Message too long (max 500 chars)", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            btnSend.isEnabled = true
            btnSend.icon?.let { it.alpha = 255 } // restore icon
            return
        }

        val enquiry = Enquiry(
            userId = userId,
            message = message,
            timestamp = System.currentTimeMillis(),
            reply = "" // initially no reply
        )

        val enquiriesRef = db.collection("users").document(userId).collection("enquiries")

        enquiriesRef.add(enquiry)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                btnSend.isEnabled = true
                btnSend.icon?.let { it.alpha = 255 } // restore icon

                etMessage.text.clear()
                recyclerView.post {
                    recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnSend.isEnabled = true
                btnSend.icon?.let { it.alpha = 255 } // restore icon
                Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRealtimeListener() {
        val enquiriesRef = db.collection("users").document(userId).collection("enquiries")

        enquiryListener = enquiriesRef
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("UserEnquiryActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val latestEnquiries = snapshots.toObjects(Enquiry::class.java)
                    adapter.submitList(latestEnquiries)

                    if (latestEnquiries.isNotEmpty()) {
                        recyclerView.post {
                            recyclerView.smoothScrollToPosition(latestEnquiries.size - 1)
                        }
                    }
                }
            }
    }
}
