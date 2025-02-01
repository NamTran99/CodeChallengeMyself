package com.example.ads.activity.billing.listener

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.example.ads.activity.data.dto.pub.IKBillingError

interface IKPurchasesUpdatedListener {
    fun onSuccess(billingResult: BillingResult?, products: MutableList<Purchase>?)
    fun onFailure(error: IKBillingError)
}