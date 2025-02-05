package com.example.ads.activity.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView.ScaleType
import com.google.android.gms.ads.nativead.MediaView
import com.example.ads.activity.data.dto.AdNetwork

class IkmWidgetMediaView : FrameLayout {
    companion object {
        const val TAG_LOG = "IkmWidgetMediaView"

    }

    var mediaScaleType: ScaleType? = null
        private set
    var mediaAdjustViewBounds: Boolean? = null
        private set

    constructor(context: Context) : this(context, null) {
        initViews(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        initViews(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initViews(context, attrs)
    }

    private fun initViews(context: Context, attrs: AttributeSet?) {

    }

    fun setupMediaView(adsName: AdNetwork): View? {
        return when (adsName) {
            AdNetwork.AD_MOB -> {
                val view = MediaView(this.context)
                removeAllViews()
                view.tag = TAG_LOG
                addView(view)
                view
            }

            AdNetwork.AD_MANAGER -> {
                val view = MediaView(this.context)
                removeAllViews()
                view.tag = TAG_LOG
                addView(view)
                view
            }

            AdNetwork.AD_MAX -> this

            else -> {
                null
            }
        }
    }

    fun setMediaScaleType(scaleType: ScaleType) {
        mediaScaleType = scaleType
    }

    fun setMediaAdjustViewBounds(value: Boolean) {
        mediaAdjustViewBounds = value
    }
}