package com.example.ads.activity.listener.pub

import com.example.ads.activity.billing.dto.PurchaseInfo


interface IKBillingDetailsPurchaseListener :IKBillingPurchaseListener {
    /**
     * @param productId The product purchase ID
     */
    fun onDetailBillingSuccess(detail: PurchaseInfo?)

}