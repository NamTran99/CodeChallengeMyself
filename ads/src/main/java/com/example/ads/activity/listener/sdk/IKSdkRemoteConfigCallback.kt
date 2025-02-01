package com.example.ads.activity.listener.sdk

import com.example.ads.activity.data.dto.pub.IKError
import com.example.ads.activity.data.dto.pub.IKRemoteConfigValue

interface IKSdkRemoteConfigCallback {
    fun onSuccess()

    fun onFail(error: IKError?)
}