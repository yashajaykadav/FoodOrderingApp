package com.foodordering.krishnafoods.core.repository

import com.foodordering.krishnafoods.core.model.Enquiry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object EnquiryRepository {

    private val db = FirebaseFirestore.getInstance()

    private const val COLL_USERS = "users"
    private const val COLL_ENQUIRIES = "enquiries"

    // =====================================================
    // USER
    // =====================================================

    fun sendEnquiry(
        userId: String,
        message: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val docRef = db.collection(COLL_USERS)
            .document(userId)
            .collection(COLL_ENQUIRIES)
            .document()

        val enquiry = Enquiry(
            id = docRef.id,
            userId = userId,
            message = message,
            reply = "",
            timestamp = System.currentTimeMillis()
        )

        docRef.set(enquiry)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUserEnquiriesQuery(userId: String): Query {
        return db.collection(COLL_USERS)
            .document(userId)
            .collection(COLL_ENQUIRIES)
            .orderBy("timestamp", Query.Direction.ASCENDING)
    }

    // =====================================================
    // ADMIN
    // =====================================================

    fun getAdminEnquiriesQuery(): Query {
        return db.collectionGroup(COLL_ENQUIRIES)
            .orderBy("timestamp", Query.Direction.ASCENDING)
    }

    fun sendReply(
        userId: String,
        enquiryId: String,
        reply: String,
        onResult: (Boolean) -> Unit
    ) {
        db.collection(COLL_USERS)
            .document(userId)
            .collection(COLL_ENQUIRIES)
            .document(enquiryId)
            .update("reply", reply)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getUserName(userId: String, onResult: (String) -> Unit) {
        db.collection(COLL_USERS)
            .document(userId)
            .get()
            .addOnSuccessListener {
                onResult(it.getString("name") ?: "User")
            }
            .addOnFailureListener {
                onResult("User")
            }
    }
}
