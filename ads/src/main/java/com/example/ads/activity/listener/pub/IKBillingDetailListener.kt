package com.example.ads.activity.listener.pub

import com.example.ads.activity.data.dto.pub.IKBillingError


interface IKBillingDetailListener<T> {
    fun onSuccess(value: T?)
    fun onError(error: IKBillingError)
}