package com.foodordering.krishnafoods.user.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.foodordering.krishnafoods.R
import com.google.android.material.button.MaterialButton

object NetworkUtil {

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun showInternetDialog(context: Context, onRetry: () -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_no_internet, null)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<MaterialButton>(R.id.btnOpenSettings).setOnClickListener {
            try {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            } catch (_: Exception) {
                context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            }
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnRetry).setOnClickListener {
            dialog.dismiss()
            if (isInternetAvailable(context)) {
                onRetry()
            } else {
                showInternetDialog(context, onRetry)
            }
        }

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // rounded bg
            setLayout(
                (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            ) // set width = 90% screen
        }

        dialog.show()
    }
}
