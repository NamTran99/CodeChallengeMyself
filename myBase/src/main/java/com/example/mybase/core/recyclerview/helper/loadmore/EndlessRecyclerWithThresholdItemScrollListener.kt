package com.tdt.pmobile3.ewallet.base.recyclerview.helper.loadmore

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tdt.pmobile3.ewallet.base.recyclerview.helper.BaseLoadMoreRecyclerOnScrollListener

abstract class EndlessRecyclerWithThresholdItemScrollListener(
    private val layoutManager: RecyclerView.LayoutManager?
) : BaseLoadMoreRecyclerOnScrollListener() {

    private var previousTotal = 0 // Tổng số item sau lần tải cuối cùng
    private val visibleThreshold = 5 // Số item tối thiểu cần trước khi tải thêm

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if(layoutManager == null) return
        val visibleItemCount = recyclerView.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItem = when (layoutManager) {
            is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
            else -> throw IllegalArgumentException("Unsupported LayoutManager")
        }

        if (loading && totalItemCount > previousTotal) {
            loading = false
            previousTotal = totalItemCount
        }

        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // Đã cuộn đến cuối danh sách
            currentPage++
            onLoadMore?.invoke(currentPage)
            loading = true
        }
    }
}
