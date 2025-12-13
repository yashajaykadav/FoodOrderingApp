package com.foodordering.krishnafoods.user.util

import android.content.Context
/**
 * Simple wrapper to centralize internet checks.
 * Uses your existing NetworkUtil.
 */
object NetworkHelper {
    fun requireInternet(context: Context, onProceed: () -> Unit) {
        if (NetworkUtil.isInternetAvailable(context)) {
            onProceed()
        } else {
            NetworkUtil.showInternetDialog(context) { onProceed() }
        }
    }
}
