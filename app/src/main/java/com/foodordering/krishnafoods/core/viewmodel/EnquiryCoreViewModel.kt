/*
 * Developed by: Yash Kadav
 * Email: yashkadav52@gmail.com
 * Project: Krishna Foods (ADCET CSE 2026)
 */

package com.foodordering.krishnafoods.core.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.foodordering.krishnafoods.core.model.Enquiry
import com.foodordering.krishnafoods.core.repository.EnquiryRepository
import com.google.firebase.firestore.ListenerRegistration

class EnquiryCoreViewModel : ViewModel() {

    private val _enquiries = MutableLiveData<List<Enquiry>>()
    val enquiries: LiveData<List<Enquiry>> = _enquiries

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val userCache = mutableMapOf<String, String>()
    private var listener: ListenerRegistration? = null

    /**
     * UNIFIED LISTENER
     * - Pass `userId` for USER MODE (Only sees own chat)
     * - Pass `null` for ADMIN MODE (Sees everyone's chats)
     */
    fun startListening(userId: String? = null) {
        if (listener != null) return

        _loading.value = true

        // FIXED: Matched function names with Repository
        val query = if (userId == null) {
            EnquiryRepository.getAdminEnquiriesQuery()
        } else {
            EnquiryRepository.getUserEnquiriesQuery(userId)
        }

        listener = query.addSnapshotListener { snapshots, e ->
            _loading.value = false

            if (e != null || snapshots == null) {
                _message.value = "Failed to load data: ${e?.localizedMessage}"
                return@addSnapshotListener
            }

            // Parse documents and extract User ID from path if missing
            val list = snapshots.documents.mapNotNull { doc ->
                // Robust way to get ID: users/{userId}/enquiries/{docId}
                val parentUserId = doc.reference.parent.parent?.id ?: userId ?: return@mapNotNull null

                doc.toObject(Enquiry::class.java)?.copy(
                    id = doc.id,
                    userId = parentUserId
                )
            }

            val sortedList = list.sortedBy { it.timestamp }
            _enquiries.value = sortedList

            // If Admin mode, fetch names for the UI
            if (userId == null) {
                resolveUserNames(sortedList)
            }
        }
    }

    /**
     * USER: Send a Message
     */
    fun sendEnquiry(userId: String, message: String) {
        if (message.isBlank()) return

        _loading.value = true
        EnquiryRepository.sendEnquiry(
            userId,
            message,
            onSuccess = {
                _loading.value = false
                // No toast needed here usually, UI updates automatically via listener
            },
            onFailure = { e ->
                _loading.value = false
                _message.value = "Failed to send: ${e.localizedMessage}"
            }
        )
    }

    /**
     * ADMIN: Reply to a Message
     */
    fun sendReply(enquiry: Enquiry, reply: String) {
        if (reply.isBlank()) return

        EnquiryRepository.sendReply(enquiry.userId, enquiry.id, reply) { success ->
            if (success) _message.value = "Reply sent"
            else _message.value = "Failed to send reply"
        }
    }

    /**
     * ADMIN HELPER: Fetches User Names lazily
     */
    private fun resolveUserNames(list: List<Enquiry>) {
        val uniqueUserIds = list.map { it.userId }.distinct()

        uniqueUserIds.forEach { userId ->
            if (userCache.containsKey(userId)) {
                // Apply cached name immediately
                updateLocalList(userId, userCache[userId]!!)
            } else {
                // Fetch from network
                EnquiryRepository.getUserName(userId) { name ->
                    userCache[userId] = name
                    updateLocalList(userId, name)
                }
            }
        }
    }

    private fun updateLocalList(userId: String, name: String) {
        val currentList = _enquiries.value ?: return
        val updatedList = currentList.map {
            if (it.userId == userId) it.copy(userName = name) else it
        }
        // Only update LiveData if something actually changed to avoid flickering
        if (currentList != updatedList) {
            _enquiries.value = updatedList
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    override fun onCleared() {
        listener?.remove()
        super.onCleared()
    }
}