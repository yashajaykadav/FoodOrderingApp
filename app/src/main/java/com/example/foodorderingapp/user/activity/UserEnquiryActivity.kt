package com.example.foodorderingapp.user.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.user.viewmodel.Enquiry
import com.example.foodorderingapp.user.adapter.EnquiryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class UserEnquiryActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageButton
    private lateinit var recyclerView: RecyclerView
//    private lateinit var tvNoEnquiries: TextView
    private lateinit var adapter: EnquiryAdapter
    private val enquiryList = mutableListOf<Enquiry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_enquiry)

        // Initialize views
        etMessage = findViewById(R.id.etUserMessage)
        btnSend = findViewById(R.id.btnSendMessage)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)
        recyclerView = findViewById(R.id.recyclerViewEnquiries)
//        tvNoEnquiries = findViewById(R.id.tvNoEnquiries)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Start from bottom
        }

        adapter = EnquiryAdapter(enquiryList)
        recyclerView.adapter = adapter

        btnBack.setOnClickListener { onBackPressed() }

        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendEnquiry(message)
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }

        // Start real-time listener
        setupRealtimeListener()
    }

    private fun sendEnquiry(message: String) {
        progressBar.visibility = View.VISIBLE
        btnSend.isEnabled = false

        val enquiry = Enquiry(
            userId = userId,
            message = message,
            reply = ""
        )

        db.collection("enquiries").add(enquiry)
            .addOnSuccessListener { documentRef ->
                progressBar.visibility = View.GONE
                btnSend.isEnabled = true
                Toast.makeText(this, "Message sent successfully", Toast.LENGTH_SHORT).show()
                etMessage.text.clear()

                // Add new enquiry to list
                enquiryList.add(enquiry.copy(id = documentRef.id))
                adapter.notifyItemInserted(enquiryList.size - 1)
                recyclerView.smoothScrollToPosition(enquiryList.size - 1)
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnSend.isEnabled = true
                Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRealtimeListener() {
        db.collection("enquiries")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("UserEnquiryActivity", "Firestore listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    enquiryList.clear()
                    for (document in snapshots.documents) {
                        document.toObject<Enquiry>()?.let { enquiry ->
                            enquiryList.add(enquiry.copy(id = document.id))
                        }
                    }

                    adapter.notifyDataSetChanged()

                    // 🔥 Prevent crash by checking if the list is empty
                    if (enquiryList.isNotEmpty()) {
                        recyclerView.post {
                            recyclerView.smoothScrollToPosition(enquiryList.size - 1)
                        }
                    }
                }
            }
    }

}
