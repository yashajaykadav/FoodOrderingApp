package com.foodordering.krishnafoods.user.util

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.foodordering.krishnafoods.R

fun Activity.setupEdgeToEdgeUI(rootView: View, topView: View? = null, bottomView: View? = null) {
    WindowCompat.setDecorFitsSystemWindows(window, false)

    ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val density = resources.displayMetrics.density

        topView?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = systemBars.top + (8 * density).toInt()
        }

        bottomView?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = systemBars.bottom + (16 * density).toInt()
        }

        insets
    }
}
fun View.visible() { this.visibility = View.VISIBLE }
fun View.gone() { this.visibility = View.GONE }

fun View.fadeIn(duration: Long = 200L) {
    this.alpha = 0f
    this.animate().alpha(1f).setDuration(duration).start()
}
// Slides a view up and shows it
fun View.slideUpAndShow() {
    if (this.isVisible) return
    startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_up))
    visibility = View.VISIBLE
}

// Slides a view down and hides it safely
fun View.slideDownAndHide(onEnd: () -> Unit = {}) {
    if (!this.isVisible) return
    val anim = AnimationUtils.loadAnimation(context, R.anim.slide_down)
    anim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(a: Animation?) {}
        override fun onAnimationRepeat(a: Animation?) {}
        override fun onAnimationEnd(a: Animation?) {
            visibility = View.GONE
            onEnd()
        }
    })
    startAnimation(anim)
}