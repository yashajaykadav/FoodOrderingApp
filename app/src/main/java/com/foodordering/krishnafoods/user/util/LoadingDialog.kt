package com.foodordering.krishnafoods.user.util

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.foodordering.krishnafoods.R

class LoadingDialog(private val activity: Activity) {

    private var dialog: AlertDialog? = null

    fun show(message: String = "Please wait...") {
        // If already showing, dismiss first
        if (dialog?.isShowing == true) {
            dismiss()
        }

        val builder = AlertDialog.Builder(activity)
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_loading, null)

        val loadingText = view.findViewById<TextView>(R.id.loadingText)
        val lottieView = view.findViewById<LottieAnimationView>(R.id.loadingAnimation)

        loadingText.text = message
        lottieView.playAnimation()

        builder.setView(view)
        builder.setCancelable(false)

        dialog = builder.create().apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }
    }

    fun dismiss() {
        if (!activity.isFinishing && !activity.isDestroyed) {
            dialog?.findViewById<LottieAnimationView>(R.id.loadingAnimation)?.cancelAnimation()
            dialog?.dismiss()
        }
        dialog = null // Release reference
    }

    fun isShowing(): Boolean {
        return dialog?.isShowing ?: false
    }
}