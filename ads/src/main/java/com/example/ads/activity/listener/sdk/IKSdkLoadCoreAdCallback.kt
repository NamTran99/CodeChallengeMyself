package com.example.ads.activity.listener.sdk

import com.example.ads.activity.data.dto.pub.IKAdError

interface IKSdkLoadCoreAdCallback<T> {
    fun onLoaded(result: T)
    fun onLoadFail(error: IKAdError)
}