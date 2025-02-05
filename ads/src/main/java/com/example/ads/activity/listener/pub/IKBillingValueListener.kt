package com.example.ads.activity.listener.pub

import com.example.ads.activity.billing.listener.IKBillingValueListenerBase


interface IKBillingValueListener : IKBillingValueListenerBase {
    /**
     *
     * @param price The regular price of an item.
     * @param salePrice The sale price of an item.
     *
     */
    fun onResult(price: String, salePrice: String)

}