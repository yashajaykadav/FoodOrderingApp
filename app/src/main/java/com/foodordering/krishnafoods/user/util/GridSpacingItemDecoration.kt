/*
 * Author: Yash Kadav
 * Email: yashkadav52@gmail.com
 * ADCET CSE 2026
 */

package com.foodordering.krishnafoods.user.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val pos = parent.getChildAdapterPosition(view)
        if (pos == RecyclerView.NO_POSITION) return
        val col = pos % spanCount

        if (includeEdge) {
            outRect.left = spacing - col * spacing / spanCount
            outRect.right = (col + 1) * spacing / spanCount
            if (pos < spanCount) outRect.top = spacing
            outRect.bottom = spacing
        } else {
            outRect.left = col * spacing / spanCount
            outRect.right = spacing - (col + 1) * spacing / spanCount
            if (pos >= spanCount) outRect.top = spacing
        }
    }
}