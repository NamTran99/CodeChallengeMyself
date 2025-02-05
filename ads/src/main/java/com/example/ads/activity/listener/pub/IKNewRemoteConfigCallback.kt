package com.example.ads.activity.listener.pub

import com.example.ads.activity.data.dto.pub.IKError
import com.example.ads.activity.data.dto.pub.IKRemoteConfigValue

interface IKNewRemoteConfigCallback {
    fun onSuccess(data: HashMap<String, IKRemoteConfigValue>)

    fun onFail(error: IKError?)
}