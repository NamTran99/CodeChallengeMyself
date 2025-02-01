package com.example.ads.activity.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

open class IkmWidgetAdLayout : FrameLayout {
    companion object {
        const val TAG_LOG = "IkmWidgetAdLayout"

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

    var titleView: TextView? = null
    var bodyView: TextView? = null
    var callToActionView: TextView? = null
    var iconView: ImageView? = null
    var mediaView: IkmWidgetMediaView? = null
    var mediaMixView: IkmWidgetMediaView? = null
    var mediaMixViewVideo: IkmWidgetMediaView? = null
    var priceView: TextView? = null
    var storeView: TextView? = null
    var advertiserView: TextView? = null
    var starRatingView: TextView? = null
    var customActionView: View? = null
    var roundIcon: Int = 0
    var isMute: Boolean = true
    var isMixIconAndMediaView: Boolean = false

    open fun enableClickOutside(value: Boolean = true) {
        if (value) {
            customActionView = this
            customActionView?.setOnClickListener {
                callToActionView?.performClick()
            }
        } else customActionView = null
    }

    fun setRoundIconValue(value: Int) {
        roundIcon = if (value > 0) value else 0
    }
}