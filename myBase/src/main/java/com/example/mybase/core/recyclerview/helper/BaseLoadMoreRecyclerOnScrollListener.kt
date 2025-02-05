package com.tdt.pmobile3.ewallet.base.recyclerview.helper

import androidx.recyclerview.widget.RecyclerView

open class BaseLoadMoreRecyclerOnScrollListener(
) : RecyclerView.OnScrollListener() {
    var currentPage = 0
    protected var loading = false
    var onLoadMore: ((currentPage: Int) -> Unit)?= null
    var isLastPage = false

    fun setLoadingFinished() {
        loading = false
    }

    fun resetPage(){
        isLastPage = false
        currentPage = 0
        setLoadingFinished()
    }
}
