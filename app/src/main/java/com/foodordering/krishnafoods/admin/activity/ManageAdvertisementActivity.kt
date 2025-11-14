package com.foodordering.krishnafoods.admin.activity

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.foodordering.krishnafoods.BuildConfig
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.adapter.AdvertisementAdapter
import com.foodordering.krishnafoods.admin.model.Advertisement
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.HashMap

class ManageAdvertisementActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivAdImage: ImageView
    private lateinit var btnSelectImage: MaterialButton
    private lateinit var btnAddAd: MaterialButton
    private lateinit var rvAdvertisements: RecyclerView
    private lateinit var tvEmptyState: TextView

    private var selectedImageUri: Uri? = null
    private var currentAdId: String? = null
    private var currentAdImageUrl: String? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: AdvertisementAdapter

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.default_img)
                .error(R.drawable.default_img)
                .into(ivAdImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_advertisment)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)

        initializeCloudinary()
        initViews()
        setupRecyclerView()
        setupListeners()
        loadAdvertisements()

        savedInstanceState?.let { restoreState(it) }
    }

    private fun initializeCloudinary() {
        try {
            // For newer versions of Cloudinary Android SDK
            val config = HashMap<String, String>().apply {
                put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME)
                put("api_key", BuildConfig.CLOUDINARY_API_KEY)
            }

            MediaManager.init(this, config)
        } catch (_: IllegalStateException) {
            // Handle case where Cloudinary is already initialized
            Toast.makeText(this, "Cloudinary already initialized", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        toolbar = findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
        }
        ivAdImage = findViewById(R.id.ivAdImage)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnAddAd = findViewById(R.id.btnAddAd)
        rvAdvertisements = findViewById(R.id.rvAdvertisements)
        tvEmptyState = findViewById(R.id.tvEmptyState)
    }

    private fun setupRecyclerView() {
        adapter = AdvertisementAdapter { ad, action ->
            when (action) {
                "edit" -> setupEditMode(ad)
                "delete" -> confirmDelete(ad.id)
            }
        }
        rvAdvertisements.apply {
            layoutManager = LinearLayoutManager(this@ManageAdvertisementActivity)
            adapter = this@ManageAdvertisementActivity.adapter
        }
    }


    private fun setupListeners() {
        btnSelectImage.setOnClickListener { pickImageLauncher.launch("image/*") }
        btnAddAd.setOnClickListener { validateAndUploadAd() }
    }

    private fun restoreState(state: Bundle) {
        currentAdId = state.getString("currentAdId")
        currentAdImageUrl = state.getString("currentAdImageUrl")
        selectedImageUri = state.getParcelable("selectedImageUri")

        selectedImageUri?.let { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.default_img)
                .into(ivAdImage)
        }

        if (currentAdId != null) {
            btnAddAd.text = getString(R.string.update_advertisement)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putString("currentAdId", currentAdId)
            putString("currentAdImageUrl", currentAdImageUrl)
            putParcelable("selectedImageUri", selectedImageUri)
        }
    }

    private fun validateAndUploadAd() {
        when {
            selectedImageUri == null && currentAdId == null -> {
                toast(getString(R.string.select_image_warning))
                return
            }
            selectedImageUri != null -> uploadImageToCloudinary()
            else -> currentAdImageUrl?.let { saveAdvertisement(it) } ?: run {
                toast(getString(R.string.image_url_error))
                resetForm()
            }
        }
    }

    private fun uploadImageToCloudinary() {
        btnAddAd.apply {
            isEnabled = false
            text = getString(R.string.uploading)
        }

        MediaManager.get().upload(selectedImageUri)
            .option("public_id", "ad_${System.currentTimeMillis()}")
            .unsigned("food_upload")
            .callback(uploadCallback)
            .dispatch()
    }

    private val uploadCallback = object : UploadCallback {
        override fun onStart(requestId: String) {}
        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
            resultData["secure_url"]?.toString()?.let { imageUrl ->
                saveAdvertisement(imageUrl)
            } ?: run {
                handleUploadError("No image URL returned")
            }
        }

        override fun onError(requestId: String, error: ErrorInfo) {
            handleUploadError(error.description)
        }

        override fun onReschedule(requestId: String, error: ErrorInfo) {
            toast(getString(R.string.upload_retry))
        }
    }

    private fun handleUploadError(error: String?) {
        btnAddAd.apply {
            isEnabled = true
            text = getString(R.string.upload_advertisement)
        }
        toast(getString(R.string.upload_failed, error ?: "Unknown error"))
    }

    private fun saveAdvertisement(imageUrl: String) {
        val adId = currentAdId ?: db.collection("advertisements").document().id
        val adData = hashMapOf(
            "id" to adId,
            "imageUrl" to imageUrl,
            "isActive" to true,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("advertisements").document(adId)
            .set(adData)
            .addOnSuccessListener {
                toast(getString(R.string.ad_save_success))
                resetForm()
            }
            .addOnFailureListener { e ->
                btnAddAd.apply {
                    isEnabled = true
                    text = getString(R.string.upload_advertisement)
                }
                toast(getString(R.string.ad_save_failed, e.message))
            }
    }

    private fun loadAdvertisements() {
        db.collection("advertisements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                error?.let {
                    toast(getString(R.string.ad_load_error, it.message))
                    return@addSnapshotListener
                }

                val ads = snapshots?.toObjects(Advertisement::class.java) ?: emptyList()
                updateUI(ads)
            }
    }

    private fun updateUI(ads: List<Advertisement>) {
        tvEmptyState.visibility = if (ads.isEmpty()) View.VISIBLE else View.GONE
        rvAdvertisements.visibility = if (ads.isEmpty()) View.GONE else View.VISIBLE
        adapter.submitList(ads)
    }

    private fun setupEditMode(ad: Advertisement) {
        currentAdId = ad.id
        currentAdImageUrl = ad.imageUrl
        btnAddAd.text = getString(R.string.update_advertisement)

        Glide.with(this)
            .load(ad.imageUrl.ifEmpty { R.drawable.default_img })
            .placeholder(R.drawable.default_img)
            .into(ivAdImage)

        findViewById<NestedScrollView>(R.id.scrollView)?.scrollTo(0, 0)
    }

    private fun confirmDelete(adId: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_advertisement)
            .setMessage(R.string.delete_ad_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteAdvertisement(adId)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteAdvertisement(adId: String) {
        db.collection("advertisements").document(adId)
            .delete()
            .addOnSuccessListener {
                toast(getString(R.string.ad_delete_success))
            }
            .addOnFailureListener { e ->
                toast(getString(R.string.ad_delete_failed, e.message))
            }
    }

    private fun resetForm() {
        currentAdId = null
        currentAdImageUrl = null
        selectedImageUri = null
        ivAdImage.setImageResource(R.drawable.default_img)
        btnAddAd.apply {
            text = getString(R.string.upload_advertisement)
            isEnabled = true
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}