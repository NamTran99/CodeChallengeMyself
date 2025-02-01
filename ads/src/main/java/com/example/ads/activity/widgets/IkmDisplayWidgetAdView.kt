package com.example.ads.activity.widgets

import com.example.ads.activity.utils.IKSdkDefConst

class IkmDisplayWidgetAdView(
    private val aj: Any?, private val adDt: Any?,
    private val adF: String = IKSdkDefConst.AdFormat.NATIVE
) : SdkDPWAdViewCore(aj, adDt, adF)