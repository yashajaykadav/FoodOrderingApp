// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.admin.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.foodordering.krishnafoods.admin.repository.AdminRepository
import com.foodordering.krishnafoods.admin.util.ImageHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File

class AddItemViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AdminRepository()
    private val auth = FirebaseAuth.getInstance()
    private val context = application.applicationContext

    // Defaults (Fallback if DB fails)
    private val defaultCategories = listOf("Namkeen", "Farsan", "Drinks", "Sweets", "Juices")
    private val defaultLiquidCats = listOf("Drinks", "Juices", "Milk", "Liquid")
    private val defaultSolidWeights = listOf("250g", "500g", "1kg", "2kg", "5kg")
    private val defaultLiquidVolumes = listOf("250ml", "500ml", "1L", "2L")

    // State
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _statusMessage = MutableLiveData<Result<String>>()
    val statusMessage: LiveData<Result<String>> = _statusMessage

    // Data lists
    val categories = MutableLiveData<List<String>>()
    val liquidCategories = MutableLiveData<List<String>>()
    val solidWeightOptions = MutableLiveData<List<String>>()
    val liquidVolumeOptions = MutableLiveData<List<String>>()

    init {
        loadDropdownData()
    }

    private fun loadDropdownData() {
        viewModelScope.launch {
            // 1. Fetch Categories
            val fetchedCategories = repository.fetchCategories()
            categories.postValue(fetchedCategories.ifEmpty { defaultCategories })

            // 2. Fetch Liquid Categories (to determine if we show ml or kg)
            val fetchedLiquids = repository.fetchOptions("options", "liquidCategories")
            liquidCategories.postValue(fetchedLiquids.ifEmpty { defaultLiquidCats })

            // 3. Fetch Solid Weights (kg/g)
            val fetchedSolid = repository.fetchOptions("options", "solidWeightOptions")
            solidWeightOptions.postValue(fetchedSolid.ifEmpty { defaultSolidWeights })

            // 4. Fetch Liquid Volumes (L/ml)
            val fetchedLiquidVol = repository.fetchOptions("options", "liquidVolumeOptions")
            liquidVolumeOptions.postValue(fetchedLiquidVol.ifEmpty { defaultLiquidVolumes })
        }
    }

    fun uploadFoodItem(
        name: String,
        origPrice: Int?,
        offPrice: Int?,
        category: String,
        weight: String,
        imageUri: Uri?
    ) {
        if (name.isBlank() || origPrice == null || offPrice == null || category.isBlank() || weight.isBlank()) {
            _statusMessage.value = Result.failure(Exception("Please fill all fields"))
            return
        }
        if (imageUri == null) {
            _statusMessage.value = Result.failure(Exception("Please select an image"))
            return
        }
        val user = auth.currentUser
        if (user == null) {
            _statusMessage.value = Result.failure(Exception("User not authenticated"))
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            var compressedPath: String? = null
            try {
                // 1. Compress
                compressedPath = ImageHelper.compressImage(context, imageUri)
                    ?: throw Exception("Failed to compress image")

                // 2. Upload Image
                val imageUrl = repository.uploadImageToCloudinary(compressedPath)

                // 3. Create Data Object
                val foodItem = hashMapOf<String, Any>(
                    "name" to name,
                    "originalPrice" to origPrice,
                    "offerPrice" to offPrice,
                    "category" to category,
                    "weight" to weight,
                    "imageUrl" to imageUrl,
                    "createdAt" to System.currentTimeMillis(),
                    "createdBy" to user.uid
                )

                // 4. Save to Firestore
                repository.addFoodItem(foodItem)

                _statusMessage.value = Result.success("Food Item Added Successfully")

            } catch (e: Exception) {
                _statusMessage.value = Result.failure(e)
            } finally {
                compressedPath?.let { File(it).delete() }
                _isLoading.value = false
            }
        }
    }
}