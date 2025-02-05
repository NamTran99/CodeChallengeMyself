package com.example.ads.activity.listener.pub

import com.example.ads.activity.data.dto.pub.IKAdError

interface IKShowWidgetAdListener {
    fun onAdShowed()
    fun onAdShowFail(error: IKAdError)
    fun onAdClick() {}
}