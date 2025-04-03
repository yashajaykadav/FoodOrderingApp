package com.example.foodorderingapp.user.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.foodorderingapp.R

class OrderSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_success)

        findViewById<TextView>(R.id.successMessage)
        val lottieSuccess = findViewById<LottieAnimationView>(R.id.lottieSuccess)

        // ✅ Start Lottie Animation
        lottieSuccess.playAnimation()

        // ✅ Delay for 3 seconds then navigate to FeedbackActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, com.example.foodorderingapp.user.activity.FeedbackActivity::class.java))
            finish()
        }, 3000)
    }
}
