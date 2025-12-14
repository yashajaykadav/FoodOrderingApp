package com.foodordering.krishnafoods.core.util

// Author: Yash Kadav
// Email: yashkadav52@gmail.com

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Module: Pagination Logic
class EndlessScrollListener(
    private val layoutManager: LinearLayoutManager,
    private val loadMore: () -> Unit
) : RecyclerView.OnScrollListener() {

    private var isLoading = false
    private var isLastPage = false

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        if (!isLoading && !isLastPage) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                loadMore()
            }
        }
    }

    fun setLoading(loading: Boolean) {
        isLoading = loading
    }

    fun setLastPage(last: Boolean) {
        isLastPage = last
    }
}