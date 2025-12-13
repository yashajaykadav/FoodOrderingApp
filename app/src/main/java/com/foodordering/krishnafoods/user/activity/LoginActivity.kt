package com.foodordering.krishnafoods.user.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.foodordering.krishnafoods.BuildConfig
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.ActivityLoginBinding
import com.foodordering.krishnafoods.user.util.*
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var signInClient: SignInClient
    private lateinit var loadingDialog: LoadingDialog

    // Dependencies
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdgeUI(
            rootView = binding.rootLayout,
            topView = binding.logo,
            bottomView = binding.footer
        )

        loadingDialog = LoadingDialog(this)
        signInClient = Identity.getSignInClient(this)

        setupAnimations()
        setupListeners()
        checkAutoLogin()
    }

    private fun setupListeners() {
        binding.btnGoogleSignIn.setOnClickListener {
            if (!binding.acceptTerms.isChecked) {
                showToast("Please accept Terms & Privacy Policy first.")
                return@setOnClickListener
            }

            // 2. REUSABLE: Network Check
            if (NetworkUtil.isInternetAvailable(this)) {
                startGoogleSignIn()
            } else {
                NetworkUtil.showInternetDialog(this) { startGoogleSignIn() }
            }
        }

        binding.privacyPolicy.setOnClickListener { openExternalUrl("https://github.com/yashajaykadav/privacy-policy") }
        binding.termsOfService.setOnClickListener { openExternalUrl("https://github.com/yashajaykadav/privacy-policy") }
    }

    private fun checkAutoLogin() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadingDialog.show("Welcome back...")
            fetchUserProfile(currentUser)
        }
    }

    // --- Google Auth Flow ---

    private fun startGoogleSignIn() {
        loadingDialog.show("Connecting to Google...")
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(BuildConfig.DEFAULT_WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        signInClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    googleSignInLauncher.launch(IntentSenderRequest.Builder(result.pendingIntent).build())
                } catch (e: Exception) {
                    loadingDialog.dismiss()
                    showToast("Error launching sign-in: ${e.message}")
                }
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                showToast("Sign-in failed: ${it.message}")
            }
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val credential = signInClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                firebaseAuthWithGoogle(idToken)
            } else {
                loadingDialog.dismiss()
                showToast("Google Error: ID Token missing")
            }
        } else {
            loadingDialog.dismiss()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                result.user?.let { fetchUserProfile(it) }
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                showToast("Authentication Failed: ${it.message}")
            }
    }

    // --- Firestore Logic ---

    private fun fetchUserProfile(user: FirebaseUser) {
        // Fetch FCM Token first (for notifications)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val token = if (task.isSuccessful) task.result else ""
            checkUserDatabase(user, token)
        }
    }

    private fun checkUserDatabase(user: FirebaseUser, fcmToken: String) {
        val userRef = firestore.collection("users").document(user.uid)

        userRef.get().addOnSuccessListener { document ->
            if (isDestroyed || isFinishing) return@addOnSuccessListener

            if (document.exists()) {
                // Existing User: Update Token
                if (fcmToken.isNotEmpty()) {
                    userRef.set(mapOf("fcmToken" to fcmToken), SetOptions.merge())
                }

                // Check if profile is complete
                val isProfileComplete = !document.getString("contact").isNullOrEmpty() &&
                        !document.getString("address").isNullOrEmpty()

                loadingDialog.dismiss()

                if (isProfileComplete) {
                    val role = document.getString("role")
                    NavigationUtils.navigateBasedOnRole(this, role)
                } else {
                    NavigationUtils.navigateToProfileSetup(this, user)
                }
            } else {
                createUserProfile(user, fcmToken)
            }
        }.addOnFailureListener {
            loadingDialog.dismiss()
            showToast("Database Error: ${it.message}")
        }
    }

    private fun createUserProfile(user: FirebaseUser, fcmToken: String) {
        val newUser = hashMapOf(
            "email" to (user.email ?: ""),
            "role" to "user",
            "shopName" to "",
            "contact" to "",
            "address" to "",
            "photoUrl" to (user.photoUrl?.toString() ?: ""),
            "createdAt" to System.currentTimeMillis(),
            "fcmToken" to fcmToken
        )

        firestore.collection("users").document(user.uid)
            .set(newUser)
            .addOnSuccessListener {
                loadingDialog.dismiss()

                NavigationUtils.navigateToProfileSetup(this, user)
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                showToast("Account Creation Failed: ${it.message}")
            }
    }

    private fun setupAnimations() {
        val animationSet = AnimationSet(true).apply {
            addAnimation(AnimationUtils.loadAnimation(this@LoginActivity, R.anim.scale_up))
            addAnimation(AnimationUtils.loadAnimation(this@LoginActivity, R.anim.fade_in))
            addAnimation(AnimationUtils.loadAnimation(this@LoginActivity, R.anim.slide_up))
            duration = 1500
        }
        Handler(Looper.getMainLooper()).postDelayed({
            binding.logo.startAnimation(animationSet)
        }, 500)
    }
}