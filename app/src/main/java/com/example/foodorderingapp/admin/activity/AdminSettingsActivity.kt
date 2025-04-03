package com.example.foodorderingapp.admin.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.example.foodorderingapp.R
import com.example.foodorderingapp.user.activity.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class AdminSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_settings)

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logoutAdmin()
        }
    }

    private fun logoutAdmin() {
        // ✅ Sign out the admin from Firebase
        FirebaseAuth.getInstance().signOut()

        // ✅ Redirect to LoginActivity & clear back stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Close the current activity
    }
}
