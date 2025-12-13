///*
// * Developed by: Yash Kadav
// * Email: yashkadav52@gmail.com
// * Project: Krishna Foods (ADCET CSE 2026)
// */
//
//package com.foodordering.krishnafoods.user.repository
//import com.foodordering.krishnafoods.core.model.Enquiry
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//
//object EnquiryRepository {
//    private val db = FirebaseFirestore.getInstance()
//
//    fun sendEnquiry(userId: String, message: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
//        val enquiry = Enquiry(
//            userId = userId,
//            message = message,
//            timestamp = System.currentTimeMillis(),
//            reply = ""
//        )
//
//        db.collection("users").document(userId).collection("enquiries")
//            .add(enquiry)
//            .addOnSuccessListener { onSuccess() }
//            .addOnFailureListener { onFailure(it) }
//    }
//
//    fun getEnquiryQuery(userId: String): Query {
//        return db.collection("users").document(userId).collection("enquiries")
//            .orderBy("timestamp", Query.Direction.ASCENDING)
//    }
//}