package com.foodordering.krishnafoods.admin.repository

// Author: Yash Kadav
// Email: yashkadav52@gmail.com

import com.foodordering.krishnafoods.admin.model.Feedback
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FeedbackRepository {

    private val db = FirebaseFirestore.getInstance()
    private val pageSize = 10L

    suspend fun getFeedback(lastDoc: DocumentSnapshot?): Pair<List<Feedback>, DocumentSnapshot?> {

        // 1. Construct Query
        var query = db.collection("feedback")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Usually want newest first
            .limit(pageSize)

        if (lastDoc != null) {
            query = query.startAfter(lastDoc)
        }

        // 2. Fetch Feedbacks
        val snapshot = query.get().await()
        if (snapshot.isEmpty) return Pair(emptyList(), lastDoc)

        val newLastDoc = snapshot.documents.last()
        val feedbacks = snapshot.documents.map { doc ->
            Feedback(
                feedbackId = doc.id,
                userId = doc.getString("userId") ?: "",
                message = doc.getString("feedback") ?: "",
                rating = doc.getLong("rating")?.toInt() ?: 0,
                timestamp = doc.getLong("timestamp") ?: 0L // Assuming you have this
            )
        }

        // 3. Fetch User Names (Client-side Join)
        // Optimization: chunked(10) prevents crashes because 'whereIn' supports max 10 items
        val userIds = feedbacks.map { it.userId }.filter { it.isNotEmpty() }.distinct()

        if (userIds.isNotEmpty()) {
            val userMap = mutableMapOf<String, String>()

            userIds.chunked(10).forEach { batchIds ->
                try {
                    val users = db.collection("users")
                        .whereIn(FieldPath.documentId(), batchIds)
                        .get()
                        .await()

                    users.forEach { userDoc ->
                        userMap[userDoc.id] = userDoc.getString("name") ?: "Unknown"
                    }
                } catch (e: Exception) {
                    // Handle partial failure smoothly
                    e.printStackTrace()
                }
            }

            // Map names back to feedback objects
            feedbacks.forEach { feedback ->
                feedback.userName = userMap[feedback.userId] ?: "Unknown User"
            }
        }

        return Pair(feedbacks, newLastDoc)
    }
}