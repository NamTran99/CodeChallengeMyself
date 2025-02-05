package com.example.ads.activity.listener.pub

import com.example.ads.activity.data.dto.pub.IKBillingError

interface IKBillingInitialListener {
    fun onInitError(error: IKBillingError)
    fun onInitialized()
}