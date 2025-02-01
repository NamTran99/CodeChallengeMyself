package com.example.ads.activity.listener.pub

import com.example.ads.activity.data.dto.pub.IKRemoteConfigValue

@Deprecated(
    "Deprecated",
    ReplaceWith(
        "IKNewRemoteConfigCallback",
        "com.ikame.android.sdk.listener.pub.IKNewRemoteConfigCallback"
    )
)
interface IKRemoteConfigCallback {
    fun onSuccess(data: HashMap<String, IKRemoteConfigValue>)

    fun onFail()
}