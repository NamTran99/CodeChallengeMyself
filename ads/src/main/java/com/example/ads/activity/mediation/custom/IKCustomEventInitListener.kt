package com.example.ads.activity.mediation.custom

import com.example.ads.activity.data.dto.pub.IKAdError

interface IKCustomEventInitListener {

    fun onSuccess()
    fun onFail(error: IKAdError)
}