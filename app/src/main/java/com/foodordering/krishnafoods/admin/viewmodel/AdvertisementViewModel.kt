package com.foodordering.krishnafoods.admin.viewmodel
// Author: Yash Kadav
// Email: yashkadav52@gmail.com

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodordering.krishnafoods.admin.model.Advertisement
import com.foodordering.krishnafoods.admin.util.CloudinaryHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdvertisementViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _ads = MutableStateFlow<List<Advertisement>>(emptyList())
    val ads = _ads.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _operationStatus = MutableStateFlow<Result<String>?>(null)
    val operationStatus = _operationStatus.asStateFlow()

    init {
        loadAdvertisements()
    }

    private fun loadAdvertisements() {
        db.collection("advertisements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                val adList = snapshots?.toObjects(Advertisement::class.java) ?: emptyList()
                _ads.value = adList
            }
    }

    fun saveAdvertisement(uri: Uri?, currentId: String?, currentUrl: String?) {
        if (uri == null && currentUrl == null) {
            _operationStatus.value = Result.failure(Exception("Please select an image"))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Upload Image if new Uri exists
                val finalUrl = if (uri != null) {
                    CloudinaryHelper.uploadImage(uri)
                } else {
                    currentUrl!!
                }

                // 2. Save to Firestore
                val adId = currentId ?: db.collection("advertisements").document().id
                val adData = Advertisement(
                    id = adId,
                    imageUrl = finalUrl,
                    isActive = true,
                    timestamp = System.currentTimeMillis()
                )

                db.collection("advertisements").document(adId).set(adData)
                _operationStatus.value = Result.success("Advertisement Saved")
            } catch (e: Exception) {
                _operationStatus.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAdvertisement(adId: String) {
        viewModelScope.launch {
            db.collection("advertisements").document(adId).delete()
                .addOnSuccessListener {
                    _operationStatus.value = Result.success("Deleted Successfully")
                }
                .addOnFailureListener { e ->
                    _operationStatus.value = Result.failure(e)
                }
        }
    }

    fun resetStatus() {
        _operationStatus.value = null
    }
}