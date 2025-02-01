package com.example.ads.activity.listener.pub

import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.widgets.IkmDisplayWidgetAdView

interface IKLoadDisplayAdViewListener {
    fun onAdLoaded(adObject: IkmDisplayWidgetAdView?)
    fun onAdLoadFail(error: IKAdError)
}