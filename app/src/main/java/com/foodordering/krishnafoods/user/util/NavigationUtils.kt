/*
 * Developed by: Yash Kadav
 * Email: yashkadav52@gmail.com
 */

package com.foodordering.krishnafoods.user.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.foodordering.krishnafoods.admin.activity.AdminMainActivity
import com.foodordering.krishnafoods.user.activity.LoginActivity
import com.foodordering.krishnafoods.user.activity.MainActivity
import com.foodordering.krishnafoods.user.activity.ProfileSetupActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

object NavigationUtils {

    fun navigateBasedOnRole(activity: Activity, role: String?) {
        val targetActivity = if (role == "admin") {
            AdminMainActivity::class.java
        } else {
            MainActivity::class.java
        }

        val intent = Intent(activity, targetActivity)
        // Clear back stack so user can't press "Back" to return to Login
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }

    fun verifyAdminAccess(context: Context, loadingDialog: LoadingDialog) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        loadingDialog.show("Verifying Access...")

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                loadingDialog.dismiss()
                val role = doc.getString("role")

                if (role == "admin") {
                    context.startActivity(Intent(context, AdminMainActivity::class.java))
                } else {
                    Toast.makeText(context, "Access Denied: Admins only.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Toast.makeText(context, "Error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    fun navigateToProfileSetup(activity: Activity, user: FirebaseUser) {
        val intent = Intent(activity, ProfileSetupActivity::class.java).apply {
            putExtra("USER_NAME", user.displayName)
            putExtra("USER_EMAIL", user.email)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        activity.startActivity(intent)
        activity.finish()
    }
    // Reusable: Perform Logout
    fun logoutUser(activity: Activity) {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }
}