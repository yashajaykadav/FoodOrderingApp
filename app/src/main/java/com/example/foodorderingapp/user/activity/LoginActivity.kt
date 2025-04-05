package com.example.foodorderingapp.user.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.foodorderingapp.R
import com.example.foodorderingapp.admin.activity.AdminMainActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var signInClient: SignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val logo = findViewById<ImageView>(R.id.logo)
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

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.progressBar)

        firebaseAuth.currentUser?.let {
            checkUserInFirestore(it)
            return
        }

        signInClient = Identity.getSignInClient(this)

        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        findViewById<Button>(R.id.btnGoogleSignIn).setOnClickListener {
            showLoading(true)
            signInClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    googleSignInLauncher.launch(
                        IntentSenderRequest.Builder(result.pendingIntent).build()
                    )
                }
                .addOnFailureListener { e ->
                    Log.e("GoogleSignIn", "Sign-in failed", e)
                    showLoading(false)
                    Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            showLoading(false)
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential = signInClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        firebaseAuthWithGoogle(idToken)
                    }
                } catch (e: ApiException) {
                    Log.e("GoogleSignIn", "Sign-in failed", e)
                    Toast.makeText(this, "Authentication failed, try again!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    firebaseAuth.currentUser?.let { checkUserInFirestore(it) }
                } else {
                    Log.e("FirebaseAuth", "Sign-in failed", task.exception)
                    showLoading(false)
                    Toast.makeText(this, "Sign-in failed, try again!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserInFirestore(user: FirebaseUser) {
        val userRef = firestore.collection("users").document(user.uid)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val email = document.getString("email")
                val shopName = document.getString("shopName")
                val contact = document.getString("contact")
                val address = document.getString("address")
                val role = document.getString("role") ?: "user"

                if (email.isNullOrEmpty() || shopName.isNullOrEmpty() || contact.isNullOrEmpty() || address.isNullOrEmpty()) {
                    navigateToProfileSetup(user)
                } else {
                    navigateBasedOnRole(role)
                }

            } else {
                // ✅ New user: Save all data including Google photo URL
                val newUser = hashMapOf(
                    "email" to (user.email ?: ""),
                    "role" to "user",
                    "shopName" to "",
                    "contact" to "",
                    "address" to "",
                    "photoUrl" to (user.photoUrl?.toString() ?: ""),
                    "createdAt" to System.currentTimeMillis()
                )

                userRef.set(newUser).addOnSuccessListener {
                    navigateToProfileSetup(user)
                }.addOnFailureListener { e ->
                    Log.e("Firestore", "Error saving new user", e)
                    Toast.makeText(this, "Failed to save user data!", Toast.LENGTH_SHORT).show()
                }
            }

        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error checking user", e)
            Toast.makeText(this, "Failed to check user data!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateToProfileSetup(user: FirebaseUser) {
        val intent = Intent(this, ProfileSetupActivity::class.java).apply {
            putExtra("USER_NAME", user.displayName)
            putExtra("USER_EMAIL", user.email)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateBasedOnRole(role: String) {
        if (role == "admin") {
            startActivity(Intent(this, AdminMainActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}
