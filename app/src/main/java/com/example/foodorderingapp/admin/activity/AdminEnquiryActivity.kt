package com.example.foodorderingapp.admin.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorderingapp.R
import com.example.foodorderingapp.admin.adapter.EnquiryAdapter
import com.example.foodorderingapp.admin.model.Enquiry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminEnquiryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EnquiryAdapter
    private val db = FirebaseFirestore.getInstance()
    private val enquiryList = mutableListOf<Enquiry>()
    private val userCache = mutableMapOf<String, String>() // 🔥 Cache for usernames

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_activity_enquiry)

        recyclerView = findViewById(R.id.recyclerViewEnquiries)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = EnquiryAdapter(enquiryList, this)
        recyclerView.adapter = adapter

        fetchEnquiriesRealtime()
    }

    private fun fetchEnquiriesRealtime() {
        db.collection("enquiries")
            .orderBy("id", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("AdminEnquiry", "Error fetching enquiries", e)
                    Toast.makeText(this, "Failed to load enquiries", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    enquiryList.clear()
                    val tempList = mutableListOf<Enquiry>()

                    for (document in snapshots.documents) {
                        val enquiry = document.toObject(Enquiry::class.java)?.apply { id = document.id }
                        if (enquiry != null) {
                            tempList.add(enquiry)
                        }
                    }

                    fetchUserNames(tempList)
                }
            }
    }

    private fun fetchUserNames(tempList: MutableList<Enquiry>) {
        var counter = 0

        if (tempList.isEmpty()) {
            updateRecyclerView(tempList)
            return
        }

        tempList.forEach { enquiry ->
            if (userCache.containsKey(enquiry.userId)) {
                enquiry.userName = userCache[enquiry.userId]!! // 🔥 Get from cache
                counter++
                if (counter == tempList.size) updateRecyclerView(tempList)
            } else {
                db.collection("users").document(enquiry.userId)
                    .get()
                    .addOnSuccessListener { document ->
                        val userName = document.getString("name") ?: "Unknown User"
                        userCache[enquiry.userId] = userName // 🔥 Cache it
                        enquiry.userName = userName
                        counter++
                        if (counter == tempList.size) updateRecyclerView(tempList)
                    }
                    .addOnFailureListener {
                        enquiry.userName = "Unknown User"
                        counter++
                        if (counter == tempList.size) updateRecyclerView(tempList)
                    }
            }
        }
    }

    private fun updateRecyclerView(tempList: MutableList<Enquiry>) {
        enquiryList.clear()
        enquiryList.addAll(tempList)
        adapter.notifyDataSetChanged()
    }
}
