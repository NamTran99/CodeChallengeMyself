package com.example.ads.activity.listener.pub

import com.example.ads.activity.billing.dto.PurchaseInfo


interface IKPurchaseListener : IKBillingPurchaseListener{
    fun onPurchaseSuccess(purchaseInfo: PurchaseInfo?)

}