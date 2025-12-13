//package com.foodordering.krishnafoods.admin.viewmodel
//
///*
// * Developed by: Yash Kadav
// * Email: yashkadav52@gmail.com
// * Project: Krishna Foods (ADCET CSE 2026)
// */
//
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import com.foodordering.krishnafoods.core.model.Enquiry
//import com.foodordering.krishnafoods.admin.repository.EnquiryRepository // <--- Repository Used Here
//import com.google.firebase.firestore.DocumentChange
//import com.google.firebase.firestore.ListenerRegistration
//
//class AdminEnquiryViewModel : ViewModel() {
//
//    private val _enquiries = MutableLiveData<List<Enquiry>>()
//    val enquiries: LiveData<List<Enquiry>> = _enquiries
//
//    private val userCache = mutableMapOf<String, String>()
//    private val currentList = mutableListOf<Enquiry>()
//    private var listener: ListenerRegistration? = null
//
//    fun startListening() {
//        if (listener != null) return
//
//        // REPOSITORY USAGE #1: Fetching Data
//        listener = EnquiryRepository.getAdminEnquiryQuery()
//            .addSnapshotListener { snapshots, e ->
//                if (e != null || snapshots == null) return@addSnapshotListener
//
//                for (change in snapshots.documentChanges) {
//                    val doc = change.document
//                    val userId = doc.reference.parent.parent?.id ?: continue
//                    val enquiry = doc.toObject(Enquiry::class.java).copy(id = doc.id, userId = userId)
//
//                    when (change.type) {
//                        DocumentChange.Type.ADDED -> {
//                            currentList.add(enquiry)
//                            resolveUserName(enquiry)
//                        }
//                        DocumentChange.Type.MODIFIED -> {
//                            val index = currentList.indexOfFirst { it.id == enquiry.id }
//                            if (index != -1) {
//                                val existingName = currentList[index]
//                                currentList[index] = enquiry.copy(
//                                    userName = existingName.userName
//                                )
//                            }
//                        }
//                        DocumentChange.Type.REMOVED -> {
//                            currentList.removeAll { it.id == enquiry.id }
//                        }
//                    }
//                }
//                updateLiveData()
//            }
//    }
//
//    private fun resolveUserName(enquiry: Enquiry) {
//        if (userCache.containsKey(enquiry.userId)) {
//            enquiry.userName = userCache[enquiry.userId]!!
//            updateLiveData()
//        } else {
//            // REPOSITORY USAGE #2: Fetching User Names
//            EnquiryRepository.getUserName(enquiry.userId) { name ->
//                userCache[enquiry.userId] = name
//                currentList.find { it.id == enquiry.id }?.userName = name
//                updateLiveData()
//            }
//        }
//    }
//
//    private fun updateLiveData() {
//        _enquiries.value = currentList.sortedBy { it.timestamp }.toList()
//    }
//
//    fun sendReply(enquiry: Enquiry, reply: String, onResult: (String) -> Unit) {
//        // REPOSITORY USAGE #3: Sending Reply
//        EnquiryRepository.sendReply(enquiry.userId, enquiry.id, reply) { success ->
//            if (success) onResult("Reply sent successfully")
//            else onResult("Failed to send reply")
//        }
//    }
//
//    override fun onCleared() {
//        listener?.remove()
//        super.onCleared()
//    }
//}