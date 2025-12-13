package com.foodordering.krishnafoods.user.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.databinding.ActivityAboutBinding
import com.foodordering.krishnafoods.user.util.getAppVersionCode
import com.foodordering.krishnafoods.user.util.getAppVersionName
import com.foodordering.krishnafoods.user.util.openExternalUrl

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Setup ViewBinding
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Edge-to-Edge UI handling
        setupWindowInsets()

        // 3. Initialize UI
        initViews()
    }

    private fun setupWindowInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Adjust Toolbar height to include status bar
            binding.toolbar.updatePadding(top = systemBars.top)

            // Adjust bottom padding for navigation bar
            binding.root.updatePadding(bottom = systemBars.bottom)

            insets
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        // Toolbar
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Set Dynamic App Info
        binding.appLogo.setImageResource(R.drawable.krishna_logo)
        binding.appVersion.text = getString(R.string.about_app_version, getAppVersionName())
        binding.appBuildCode.text = "Build: ${getAppVersionCode()}"

        // Set Developer Info (Hardcoded strings should be in strings.xml)
        binding.appDescription.text = getString(R.string.about_app)
        binding.developerInfo.text = getString(R.string.about_developer_info)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnPrivacyPolicy.setOnClickListener {
            openExternalUrl(getString(R.string.privacy_policy_url))
        }

        binding.btnTermsOfService.setOnClickListener {
            openExternalUrl(getString(R.string.terms_of_service_url))
        }

        binding.btnRateApp.setOnClickListener {
            openPlayStorePage()
        }
    }

    private fun openPlayStorePage() {
        val appPackageName = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$appPackageName".toUri()))
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$appPackageName".toUri()))
        }
    }
}