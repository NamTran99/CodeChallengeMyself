package com.example.ads.activity.listener.pub

import com.example.ads.activity.data.dto.pub.IKAdError

interface IKLoadAdListener {
    fun onAdLoaded()
    fun onAdLoadFail(error: IKAdError)
}