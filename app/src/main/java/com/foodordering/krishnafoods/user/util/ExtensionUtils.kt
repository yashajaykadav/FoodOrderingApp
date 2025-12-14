package com.foodordering.krishnafoods.user.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.foodordering.krishnafoods.R

// Opens a URL in the browser safely
fun Context.openExternalUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show()
    }
}
fun ImageView.loadUrl(url: String?) {
    if (url.isNullOrEmpty()) {
        this.setImageResource(R.drawable.ic_profile_placeholder)
        return
    }

    Glide.with(this.context)
        .load(url)
        .placeholder(R.drawable.ic_profile_placeholder)
        .error(R.drawable.ic_profile_placeholder)
        .circleCrop()
        .into(this)
}

// Reusable: Load User Image into a Toolbar Icon
fun Context.loadToolbarIcon(url: String?, onLoaded: (Drawable) -> Unit) {
    Glide.with(this)
        .asBitmap() // Load as Bitmap first
        .load(url)
        .centerCrop()
        .placeholder(R.drawable.user)
        .error(R.drawable.user)
        .into(object : CustomTarget<Bitmap>(75, 75) {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val circularDrawable = RoundedBitmapDrawableFactory.create(resources, resource)
                circularDrawable.isCircular = true
                onLoaded(circularDrawable)
            }
            override fun onLoadCleared(placeholder: Drawable?) {
                placeholder?.let { onLoaded(it) }
            }
        })
}

// Gets the App Version Name safely
fun Context.getAppVersionName(): String? {
    return try {
        packageManager.getPackageInfo(packageName, 0).versionName
    } catch (_: PackageManager.NameNotFoundException) {
        "Unknown"
    }
}
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

// Reusable: Load User Avatar (Circular + Placeholder)
fun ImageView.loadUserAvatar(url: String?) {
    Glide.with(this.context)
        .load(url)
        .circleCrop()
        .placeholder(R.drawable.user) // Ensure 'user' drawable exists
        .error(R.drawable.user)
        .into(this)
}

fun Context.showLogoutDialog(onConfirm: () -> Unit) {
    AlertDialog.Builder(this)
        .setTitle(getString(R.string.logout)) // Ensure strings exist
        .setMessage(getString(R.string.logout_confirm))
        .setPositiveButton(getString(R.string.yes)) { _, _ -> onConfirm() }
        .setNegativeButton(getString(R.string.cancel), null)
        .show()
}

// Gets the App Version Code safely (Compatible with all Android versions)
fun Context.getAppVersionCode(): String {
    return try {
        val pInfo = packageManager.getPackageInfo(packageName, 0)
        PackageInfoCompat.getLongVersionCode(pInfo).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        "0"
    }
}

// Reusable: Vibrate Phone (Haptic Feedback)
fun Context.vibrateDevice(durationMs: Long = 200) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }
}

fun Context.playSound(@RawRes soundResId: Int) {
    try {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .build()
            )
            setDataSource(
                resources.openRawResourceFd(soundResId)
                    .also { fd ->
                        setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                        fd.close()
                    }
            )
            prepare()
            setOnCompletionListener {
                it.release()
            }
            start()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
