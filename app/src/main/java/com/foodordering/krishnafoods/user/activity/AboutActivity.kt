package com.foodordering.krishnafoods.user.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import com.foodordering.krishnafoods.R
import com.foodordering.krishnafoods.user.util.LoadingDialog
import com.foodordering.krishnafoods.user.util.NetworkUtil
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class AboutActivity : AppCompatActivity() {

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_about)

        val rootLayout = findViewById<View>(R.id.rootLayout)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        // --- START OF FIX --
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
        val actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())

            // Set the toolbar's height to be its original height + the status bar's height
            toolbar.layoutParams.height = actionBarHeight + systemBars.top

            // Also add top padding to correctly position the title and nav icon
            toolbar.setPadding(toolbar.paddingLeft, systemBars.top, toolbar.paddingRight, toolbar.paddingBottom)

            // Apply bottom inset as padding to the root layout
            view.updatePadding(bottom = systemBars.bottom)

            insets
        }

        initViews()
    }

    private fun initViews() {
        // DELETED: The call to setupStatusBar() has been removed
        setupToolbar()
        setAppInfoContent()
        setupButtonListeners()
    }

    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setAppInfoContent() {
        findViewById<ImageView>(R.id.appLogo).setImageResource(R.drawable.krishna_logo)

        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (_: Exception) {
            "1.0.0"
        }

        val versionCode = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, 0).longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0).versionCode.toString()
            }
        } catch (_: Exception) {
            "N/A"
        }

        findViewById<TextView>(R.id.appDescription).text = getString(R.string.about_app)
        findViewById<TextView>(R.id.appVersion).text = getString(R.string.about_app_version, versionName)
        findViewById<TextView>(R.id.appBuildCode).text = "Build: $versionCode"
        findViewById<TextView>(R.id.developerInfo).text = getString(R.string.about_developer_info)
    }

    private fun setupButtonListeners() {
        loadingDialog = LoadingDialog(this)

        findViewById<MaterialButton>(R.id.btnPrivacyPolicy).setOnClickListener {
            openExternalLink(getString(R.string.privacy_policy_url))
        }

        findViewById<MaterialButton>(R.id.btnTermsOfService).setOnClickListener {
            openExternalLink(getString(R.string.terms_of_service_url))
        }

        findViewById<MaterialButton>(R.id.btnRateApp).setOnClickListener {
            openPlayStorePage()
        }
    }

    private fun openExternalLink(url: String) {
        if (NetworkUtil.isInternetAvailable(this)) {
            loadingDialog.show(getString(R.string.loading_link))
            try {
                startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
            } catch (_: Exception) {
                showToast(getString(R.string.error_opening_link))
            } finally {
                loadingDialog.dismiss()
            }
        } else {
            NetworkUtil.showInternetDialog(this) { openExternalLink(url) }
        }
    }

    private fun openPlayStorePage() {
        val uri = "market://details?id=$packageName".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri()))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}