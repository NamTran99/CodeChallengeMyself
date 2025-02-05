package com.tdt.pmobile3.ewallet.base.recyclerview.helper

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)

        // Add space to the right of each item except the last one
        if (position != parent.adapter?.itemCount?.minus(1)) {
            outRect.right = space
        }
    }
}