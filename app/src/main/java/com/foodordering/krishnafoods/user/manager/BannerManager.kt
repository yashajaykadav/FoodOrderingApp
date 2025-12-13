/*
 * Author: Yash Kadav
 * Email: yashkadav52@gmail.com
 * ADCET CSE 2026
 */

package com.foodordering.krishnafoods.user.manager

import android.util.TypedValue
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class BannerManager(
    private val viewPager: ViewPager2,
    private val scope: LifecycleCoroutineScope
) {
    private var autoScrollJob: Job? = null

    fun setupTransformer() {
        val transformer = CompositePageTransformer()
        val marginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 16f, viewPager.resources.displayMetrics
        ).toInt()

        transformer.addTransformer(MarginPageTransformer(marginPx))
        transformer.addTransformer { page, position ->
            val v = 1 - abs(position)
            page.scaleY = 0.85f + v * 0.13f
            page.scaleX = 0.9f + v * 0.10f
            page.alpha = 0.6f + v * 0.4f
        }
        viewPager.setPageTransformer(transformer)

        // Attach snap helper safely
        val inner = viewPager.getChildAt(0) as? RecyclerView
        if (inner?.onFlingListener == null) {
            LinearSnapHelper().attachToRecyclerView(inner)
        }
    }

    fun startAutoScroll(interval: Long = 3000L) {
        stopAutoScroll()
        autoScrollJob = scope.launch {
            while (isActive) {
                delay(interval)
                val adapter = viewPager.adapter ?: break
                if (adapter.itemCount > 1) {
                    val next = (viewPager.currentItem + 1) % adapter.itemCount
                    viewPager.setCurrentItem(next, true)
                }
            }
        }
    }

    fun stopAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = null
    }

    fun attachPageCallback() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) stopAutoScroll()
                else if (state == ViewPager2.SCROLL_STATE_IDLE) startAutoScroll()
            }
        })
    }
}