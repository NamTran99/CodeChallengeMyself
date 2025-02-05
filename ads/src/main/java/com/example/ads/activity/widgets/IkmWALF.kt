package com.example.ads.activity.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.MediaAspectRatio

class IkmWALF : IkmWidgetAdLayout {
    companion object {
        const val TAG_LOG = "IkmWALF"

    }

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

    var containerNor: View? = null
    var containerSquare: View? = null
    var containerPor: View? = null

    //portal
    var titleViewPor: TextView? = null
    var bodyViewPor: TextView? = null
    var callToActionViewPor: TextView? = null
    var iconViewPor: ImageView? = null
    var mediaViewPor: IkmWidgetMediaView? = null
    var priceViewPor: TextView? = null
    var storeViewPor: TextView? = null
    var advertiserViewPor: TextView? = null
    var starRatingViewPor: TextView? = null
    var customActionViewPor: View? = null

    var titleViewPor2: TextView? = null
    var bodyViewPor2: TextView? = null
    var callToActionViewPor2: TextView? = null
    var iconViewPor2: ImageView? = null
    var storeViewPor2: TextView? = null
    var starRatingViewPor2: TextView? = null
    var customAnimateView: View? = null
    var customAnimateView2: View? = null

    //square
    var titleViewSquare: TextView? = null
    var bodyViewSquare: TextView? = null
    var callToActionViewSquare: TextView? = null
    var iconViewSquare: ImageView? = null
    var mediaViewSquare: IkmWidgetMediaView? = null
    var priceViewSquare: TextView? = null
    var storeViewSquare: TextView? = null
    var advertiserViewSquare: TextView? = null
    var starRatingViewSquare: TextView? = null
    var customActionViewSquare: View? = null

    var adAspectRatio: Int = MediaAspectRatio.LANDSCAPE

    var ikmWALFType : IkmWALFType = IkmWALFType.DEFAULT

    fun displayContainerPor() {
        containerNor?.visibility = View.GONE
        containerSquare?.visibility = View.GONE
        containerPor?.visibility = View.VISIBLE
    }

    fun displayContainerSquare() {
        containerNor?.visibility = View.GONE
        containerSquare?.visibility = View.VISIBLE
        containerPor?.visibility = View.GONE
    }

    fun displayContainerNor() {
        containerNor?.visibility = View.VISIBLE
        containerSquare?.visibility = View.GONE
        containerPor?.visibility = View.GONE
    }

    override fun enableClickOutside(value: Boolean) {
        if (value) {
            customActionView = this
            customActionViewPor = this
            customActionViewSquare = this
            customActionView?.setOnClickListener {
                callToActionView?.performClick()
            }
            customActionViewPor?.setOnClickListener {
                callToActionView?.performClick()
            }
            customActionViewSquare?.setOnClickListener {
                callToActionView?.performClick()
            }
        } else {
            customActionView = null
            customActionViewSquare = null
            customActionViewPor = null
        }

    }
}