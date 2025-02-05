package com.example.ads.activity.billing.listener


interface IKBillingPriceListener : IKBillingValueListenerBase {

    fun onResult(price: String)
}