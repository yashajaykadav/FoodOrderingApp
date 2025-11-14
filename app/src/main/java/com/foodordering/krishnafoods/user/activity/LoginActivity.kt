// Author: Yash Kadav
// Email: yashkadav52@gmail.com
package com.foodordering.krishnafoods.user.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.foodordering.krishnafoods.BuildConfig
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.admin.activity.AdminMainActivity
import com.foodordering.krishnafoods.user.util.LoadingDialog
import com.foodordering.krishnafoods.user.util.NetworkUtil
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var signInClient: SignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        // *** START: EDGE-TO-EDGE IMPLEMENTATION ***
        // This enables the app to draw behind the system bars.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // *** END: EDGE-TO-EDGE IMPLEMENTATION ***

        setContentView(R.layout.activity_login)
        val logo = findViewById<ImageView>(R.id.logo)
        val footer = findViewById<View>(R.id.footer)

        // *** DELETED THIS LINE ***
        // window.statusBarColor = ContextCompat.getColor(this, R.color.google_blue)

        // *** START: HANDLE WINDOW INSETS ***
        // Find the root view you gave an ID to in the XML.
        val rootView = findViewById<View>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val logoParams = logo.layoutParams as ViewGroup.MarginLayoutParams
            // We add 8dp (the original margin) to the status bar height.
            logoParams.topMargin = systemBars.top + (8 * resources.displayMetrics.density).toInt()
            logo.layoutParams = logoParams

            // Apply the bottom inset as a margin to the footer.
            val footerParams = footer.layoutParams as ViewGroup.MarginLayoutParams
            // We add 16dp (the original margin) to the navigation bar height.
            footerParams.bottomMargin = systemBars.bottom + (16 * resources.displayMetrics.density).toInt()
            footer.layoutParams = footerParams
            insets
        }
        // *** END: HANDLE WINDOW INSETS ***

        // Init
        loadingDialog = LoadingDialog(this)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        signInClient = Identity.getSignInClient(this)

        val signInButton = findViewById<MaterialButton>(R.id.btnGoogleSignIn)
        val acceptTerms = findViewById<CheckBox>(R.id.acceptTerms)
        val privacyPolicy = findViewById<TextView>(R.id.privacyPolicy)
        val termsOfService = findViewById<TextView>(R.id.termsOfService)

        // Animations
        val scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        val animationSet = AnimationSet(true).apply {
            addAnimation(scaleUp)
            addAnimation(fadeIn)
            addAnimation(slideUp)
            duration = 1500
        }
        Handler(Looper.getMainLooper()).postDelayed({
            logo.startAnimation(animationSet)
        }, 500)

        // Auto-login if already signed in
        firebaseAuth.currentUser?.let {
            if (NetworkUtil.isInternetAvailable(this)) {
                loadingDialog.show("Logging you in...")
                checkUserInFirestore(it)
            } else {
                NetworkUtil.showInternetDialog(this) {
                    loadingDialog.show("Logging you in...")
                    checkUserInFirestore(it)
                }
            }
        }

        // One Tap sign-in request
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(BuildConfig.DEFAULT_WEB_CLIENT_ID) // from google-services.json
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        // Links
        privacyPolicy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, "https://github.com/yashajaykadav/privacy-policy".toUri()))
        }
        termsOfService.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, "https://github.com/yashajaykadav/privacy-policy".toUri()))
        }

        // Google Sign-In button
        signInButton.setOnClickListener {
            if (acceptTerms.isChecked) {
                if (NetworkUtil.isInternetAvailable(this)) {
                    loadingDialog.show("Signing in with Google...")
                    signInClient.beginSignIn(signInRequest)
                        .addOnSuccessListener { result ->
                            googleSignInLauncher.launch(
                                IntentSenderRequest.Builder(result.pendingIntent).build()
                            )
                        }
                        .addOnFailureListener { e: Exception ->
                            loadingDialog.dismiss()
                            Toast.makeText(
                                this,
                                "Google Sign-In failed: ${e.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    NetworkUtil.showInternetDialog(this) {
                        loadingDialog.show("Signing in with Google...")
                        signInClient.beginSignIn(signInRequest)
                            .addOnSuccessListener { result ->
                                googleSignInLauncher.launch(
                                    IntentSenderRequest.Builder(result.pendingIntent).build()
                                )
                            }
                            .addOnFailureListener { e: Exception ->
                                loadingDialog.dismiss()
                                Toast.makeText(
                                    this,
                                    "Google Sign-In failed: ${e.localizedMessage}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                }
            } else {
                Toast.makeText(this, "Please accept the Terms & Privacy Policy to continue.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Google Sign-In Result
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            loadingDialog.dismiss()
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential = signInClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        firebaseAuthWithGoogle(idToken)
                    } else {
                        Toast.makeText(this, "No ID token received. Please try again.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: ApiException) {
                    Toast.makeText(this, "Authentication failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Sign-in cancelled. Please try again.", Toast.LENGTH_LONG).show()
            }
        }

    private fun firebaseAuthWithGoogle(idToken: String) {
        loadingDialog.show("Authenticating with Firebase...")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    firebaseAuth.currentUser?.let { checkUserInFirestore(it) }
                } else {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Firebase authentication failed: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun checkUserInFirestore(user: FirebaseUser) {
        val userRef = firestore.collection("users").document(user.uid)

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                userRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val email = document.getString("email")
                        val shopName = document.getString("shopName")
                        val contact = document.getString("contact")
                        val address = document.getString("address")
                        val role = document.getString("role") ?: "user"

                        // update FCM token
                        userRef.set(mapOf("fcmToken" to token), SetOptions.merge())

                        if (email.isNullOrEmpty() || shopName.isNullOrEmpty() ||
                            contact.isNullOrEmpty() || address.isNullOrEmpty()
                        ) {
                            loadingDialog.dismiss()
                            Toast.makeText(this, getString(R.string.profile_setup_prompt), Toast.LENGTH_LONG).show()
                            navigateToProfileSetup(user)
                        } else {
                            loadingDialog.dismiss()
                            navigateBasedOnRole(role)
                        }
                    } else {
                        // New user
                        val newUser = hashMapOf(
                            "email" to (user.email ?: ""),
                            "role" to "user",
                            "shopName" to "",
                            "contact" to "",
                            "address" to "",
                            "photoUrl" to (user.photoUrl?.toString() ?: ""),
                            "createdAt" to System.currentTimeMillis(),
                            "fcmToken" to token
                        )
                        userRef.set(newUser).addOnSuccessListener {
                            loadingDialog.dismiss()
                            Toast.makeText(this, getString(R.string.profile_setup_prompt), Toast.LENGTH_LONG).show()
                            navigateToProfileSetup(user)
                        }.addOnFailureListener { e: Exception ->
                            loadingDialog.dismiss()
                            Toast.makeText(this, "Failed to save user data: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }.addOnFailureListener { e: Exception ->
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Failed to check user data: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener { e: Exception ->
                loadingDialog.dismiss()
                Toast.makeText(this, "Failed to retrieve FCM token: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun navigateToProfileSetup(user: FirebaseUser) {
        startActivity(Intent(this, ProfileSetupActivity::class.java).apply {
            putExtra("USER_NAME", user.displayName)
            putExtra("USER_EMAIL", user.email)
        })
        finish()
    }

    private fun navigateBasedOnRole(role: String) {
        startActivity(Intent(this,
            if (role == "admin") AdminMainActivity::class.java else MainActivity::class.java
        ))
        finish()
    }
}