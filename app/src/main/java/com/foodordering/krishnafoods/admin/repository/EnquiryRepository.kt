//package com.foodordering.krishnafoods.admin.repository
//
///*
// * Developed by: Yash Kadav
// * Email: yashkadav52@gmail.com
// * Project: Krishna Foods (ADCET CSE 2026)
// */
//
//import com.foodordering.krishnafoods.core.model.Enquiry
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//
//object EnquiryRepository {
//
//    private val db = FirebaseFirestore.getInstance()
//    private const val COLL_USERS = "users"
//    private const val COLL_ENQUIRIES = "enquiries"
//
//    // ==========================================
//    // USER FUNCTIONS
//    // ==========================================
//
//    /**
//     * Sends a new enquiry for a specific user.
//     * Uses .document().set() to ensure the ID is saved inside the object immediately.
//     */
//    fun sendEnquiry(userId: String, message: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
//        val docRef = db.collection(COLL_USERS).document(userId).collection(COLL_ENQUIRIES).document()
//
//        val enquiry = Enquiry(
//            id = docRef.id, // Save ID explicitly for DiffUtil
//            userId = userId,
//            message = message,
//            reply = "",
//            timestamp = System.currentTimeMillis()
//        )
//
//        docRef.set(enquiry)
//            .addOnSuccessListener { onSuccess() }
//            .addOnFailureListener { onFailure(it) }
//    }
//
//    /**
//     * user-side query: only shows their own enquiries.
//     */
//    fun getEnquiriesQuery(userId: String): Query {
//        return db.collection(COLL_USERS).document(userId).collection(COLL_ENQUIRIES)
//            .orderBy("timestamp", Query.Direction.ASCENDING)
//    }
//
//    // ==========================================
//    // ADMIN FUNCTIONS
//    // ==========================================
//
//    /**
//     * Admin-side query: collectionGroup searches ALL users' enquiry sub-collections.
//     * Requires "enquiries" Collection Group Index in Firebase Console.
//     */
//    fun getAdminEnquiryQuery(): Query {
//        return db.collectionGroup(COLL_ENQUIRIES)
//            .orderBy("timestamp", Query.Direction.ASCENDING)
//    }
//
//    /**
//     * Updates the 'reply' field of a specific enquiry.
//     */
//    fun sendReply(userId: String, enquiryId: String, reply: String, onResult: (Boolean) -> Unit) {
//        db.collection(COLL_USERS).document(userId).collection(COLL_ENQUIRIES).document(enquiryId)
//            .update("reply", reply)
//            .addOnSuccessListener { onResult(true) }
//            .addOnFailureListener { onResult(false) }
//    }
//
//    /**
//     * Helper to fetch User Name for the Admin UI.
//     */
//    fun getUserName(userId: String, onResult: (String) -> Unit) {
//        db.collection(COLL_USERS).document(userId).get()
//            .addOnSuccessListener { document ->
//                val name = document.getString("name") ?: "Unknown User"
//                onResult(name)
//            }
//            .addOnFailureListener {
//                onResult("Unknown User")
//            }
//    }
//}