package com.example.ads.activity.listener.sdk

import com.example.ads.activity.billing.dto.PurchaseInfo
import com.example.ads.activity.data.dto.sdk.IKSdkBillingErrorCode


interface IKSdkBillingPurchaseListener {
    /**
     * @param productId The product purchase ID
     */
    fun onProductAlreadyPurchased(productId: String)

    /**
     * @param productId The product purchase ID
     */
    fun onBillingSuccess(purchaseInfo: PurchaseInfo?, productId: String)

    /**
     * @param productId The product purchase ID
     */
    fun onBillingFail(productId: String, error: IKSdkBillingErrorCode)
}