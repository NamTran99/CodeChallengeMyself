package com.example.ads.activity.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

open class IkmWidgetAdCollapseLayout : RelativeLayout {
    companion object {
        const val TAG_LOG = "IkmWidgetAdCollapseLayout"

    }

    constructor(context: Context) : this(context, null) {
        initViews()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        initViews()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initViews()
    }

    private fun initViews() {

    }

}