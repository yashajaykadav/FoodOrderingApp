package com.foodordering.krishnafoods.user.util

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import com.foodordering.krishnafoods.R

object AnimHelper {
    fun click(context: Context, v: View) {
        v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.button_click))
    }

    fun slideTop(context: Context, v: View) {
        v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_top))
    }
}
