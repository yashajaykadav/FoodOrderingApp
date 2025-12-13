/*
 * Developed by: Yash Kadav
 * Email: yashkadav52@gmail.com
 * Project: Krishna Foods (ADCET CSE 2026)
 */

package com.foodordering.krishnafoods.user.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.ActivityOrderSuccessBinding
import com.foodordering.krishnafoods.user.util.playSound
import com.foodordering.krishnafoods.user.util.vibrateDevice

class OrderSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityOrderSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        playSound(R.raw.order_confirmation)
        vibrateDevice(500)

        setupWindowInsets()
        displayOrderDetails()
        setupButtons()
        setupBackPressHandler()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }
    }

    private fun displayOrderDetails() {
        val orderId = intent.getStringExtra("orderId") ?: "Unknown"

        val totalAmountDouble = intent.getDoubleExtra("totalAmount", 0.0)
        val totalAmountInt = totalAmountDouble.toInt()

        binding.orderIdText.text = getString(R.string.order_id_format, orderId)

        // Now passing an Int, which matches your %d in strings.xml
        binding.totalAmountText.text = getString(R.string.currency_format, totalAmountInt)
    }

    private fun setupButtons() {
        binding.btnTrackOrder.setOnClickListener {
            navigateToHome(openOrdersTab = true)
        }

        binding.btnProvideFeedback.setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
            finish()
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToHome(openOrdersTab = false)
            }
        })
    }

    private fun navigateToHome(openOrdersTab: Boolean) {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        if (openOrdersTab) {
            intent.putExtra("NAVIGATE_TO", "ORDERS")
        }
        startActivity(intent)
        finish()
    }
}