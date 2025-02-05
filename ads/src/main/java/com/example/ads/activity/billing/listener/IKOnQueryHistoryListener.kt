package com.example.ads.activity.billing.listener

import com.android.billingclient.api.Purchase
import com.example.ads.activity.data.dto.pub.IKBillingError

interface IKOnQueryHistoryListener {
    fun onSuccess(products: MutableList<Purchase>)
    fun onFailure(error: IKBillingError)
}