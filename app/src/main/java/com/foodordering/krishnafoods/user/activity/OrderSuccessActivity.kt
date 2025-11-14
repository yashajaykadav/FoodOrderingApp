package com.foodordering.krishnafoods.user.activity

import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.foodordering.krishnafoods.R
import com.google.android.material.button.MaterialButton

class OrderSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_order_success)
        supportActionBar?.hide()

        // Handle window insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }

        // Get data from intent
        val orderId = intent.getStringExtra("orderId") ?: "N/A"
        val totalAmount = intent.getDoubleExtra("totalAmount", 0.0)

        // Set text views
        findViewById<TextView>(R.id.orderIdText).text = orderId
        findViewById<TextView>(R.id.totalAmountText).text = "₹${String.format("%.2f", totalAmount)}"

        // Play feedback
        playSound()
        vibratePhone()

        // Setup button clicks
        findViewById<MaterialButton>(R.id.btnTrackOrder).setOnClickListener {
            // Navigate to an order tracking or order history screen
            // Example: startActivity(Intent(this, OrderHistoryActivity::class.java))
            finish() // Close this activity
        }

        findViewById<MaterialButton>(R.id.btnProvideFeedback).setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
            finish() // Close this activity
        }
    }

    private fun playSound() {
        MediaPlayer.create(this, R.raw.order_confirmation).apply {
            start()
            setOnCompletionListener { it.release() }
        }
    }

    private fun vibratePhone() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12 and higher
            val vibratorManager =
                getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            // For older versions
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    android.os.VibrationEffect.createOneShot(
                        500,
                        android.os.VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }
    }
}