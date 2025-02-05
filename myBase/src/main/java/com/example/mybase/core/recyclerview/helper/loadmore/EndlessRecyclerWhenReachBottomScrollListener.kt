package com.tdt.pmobile3.ewallet.base.recyclerview.helper.loadmore

import androidx.recyclerview.widget.RecyclerView
import com.tdt.pmobile3.ewallet.base.recyclerview.helper.BaseLoadMoreRecyclerOnScrollListener

abstract class EndlessRecyclerWhenReachBottomScrollListener(
    private val layoutManager: RecyclerView.LayoutManager?
) : BaseLoadMoreRecyclerOnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        // Chỉ xử lý khi người dùng kéo xuống (dy > 0)
        if (dy > 0 && !recyclerView.canScrollVertically(1)) {
            // Kiểm tra nếu không còn dữ liệu để cuộn
            if (!loading && !isLastPage) {
                loading = true
                onLoadMore?.invoke(currentPage + 1)
            }
        }
    }
}
