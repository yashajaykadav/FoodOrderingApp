///*
// * Developed by: Yash Kadav
// * Email: yashkadav52@gmail.com
// * Project: Krishna Foods (ADCET CSE 2026)
// */
//
//package com.foodordering.krishnafoods.user.viewmodel
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import com.foodordering.krishnafoods.user.repository.EnquiryRepository
//import com.google.firebase.firestore.ListenerRegistration
//import com.foodordering.krishnafoods.core.model.Enquiry
//
//class EnquiryViewModel : ViewModel() {
//
//    private val _enquiries = MutableLiveData<List<Enquiry>>()
//    val enquiries: LiveData<List<Enquiry>> = _enquiries
//
//    private val _isLoading = MutableLiveData<Boolean>()
//    val isLoading: LiveData<Boolean> = _isLoading
//
//    private val _toastMessage = MutableLiveData<String?>()
//    val toastMessage: LiveData<String?> = _toastMessage
//
//    private var listener: ListenerRegistration? = null
//
//    // Start listening to Firestore changes
//    fun startListening(userId: String) {
//        if (listener != null) return // Prevent duplicate listeners
//
//        _isLoading.value = true
//        listener = EnquiryRepository.getEnquiryQuery(userId)
//            .addSnapshotListener { snapshots, e ->
//                _isLoading.value = false
//                if (e != null) {
//                    _toastMessage.value = "Error: ${e.localizedMessage}"
//                    return@addSnapshotListener
//                }
//
//                if (snapshots != null) {
//                    // @DocumentId automatically populates the id field here!
//                    val list = snapshots.toObjects(Enquiry::class.java)
//                    _enquiries.value = list
//                }
//            }
//    }
//
//    fun sendMessage(userId: String, message: String) {
//        _isLoading.value = true
//        EnquiryRepository.sendEnquiry(userId, message,
//            onSuccess = {
//                _isLoading.value = false
//                // No need to manually update list; the Listener above will catch the new data automatically
//            },
//            onFailure = { e ->
//                _isLoading.value = false
//                _toastMessage.value = "Failed: ${e.localizedMessage}"
//            }
//        )
//    }
//
//    // Clear toast after showing to prevent showing it again on rotation
//    fun clearToast() {
//        _toastMessage.value = null
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        listener?.remove() // Automatic cleanup! No memory leaks.
//    }
//}