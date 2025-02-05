package com.example.ads.activity.widgets

import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.utils.IKSdkDefConst

open class
SdkDPWAdViewCore {
    val adObject: Any?
    private val adDetail: Any?
    private val adFormat: String

    constructor(
        adObject: Any?,
        adDetail: Any? = null,
        adFormat: String = IKSdkDefConst.AdFormat.NATIVE
    ) {
        this.adObject = adObject
        (adObject as? IKSdkBaseLoadedAd<*>)?.isDisplayAdView = true
        this.adDetail = adDetail
        this.adFormat = adFormat
    }

    fun clX(x: Any?): Any? {
        return if (x is SdkDPWAdViewCore)
            adDetail
        else null
    }
}
