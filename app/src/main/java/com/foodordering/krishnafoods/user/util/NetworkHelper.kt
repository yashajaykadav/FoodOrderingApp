package com.foodordering.krishnafoods.user.util

import android.content.Context

object NetworkHelper {
    fun requireInternet(context: Context, onProceed: () -> Unit) {
        if (NetworkUtil.isInternetAvailable(context)) {
            onProceed()
        } else {
            NetworkUtil.showInternetDialog(context) { onProceed() }
        }
    }
}
