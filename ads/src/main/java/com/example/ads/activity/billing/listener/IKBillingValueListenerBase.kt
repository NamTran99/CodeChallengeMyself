package com.example.ads.activity.billing.listener

import com.example.ads.activity.data.dto.pub.IKBillingError


interface IKBillingValueListenerBase {
    fun onError(error: IKBillingError)
}