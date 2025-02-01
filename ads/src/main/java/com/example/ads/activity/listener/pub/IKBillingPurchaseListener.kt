package com.example.ads.activity.listener.pub

import com.example.ads.activity.data.dto.pub.IKBillingError


interface IKBillingPurchaseListener {
    /**
     * @param productId The product purchase ID
     */
    fun onProductAlreadyPurchased(productId: String)

    /**
     * @param productId The product purchase ID
     */
    fun onBillingSuccess(productId: String)

    /**
     * @param productId The product purchase ID
     */
    fun onBillingFail(productId: String, error: IKBillingError)
}