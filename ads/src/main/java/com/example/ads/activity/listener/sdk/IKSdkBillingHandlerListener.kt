package com.example.ads.activity.listener.sdk

import com.example.ads.activity.billing.dto.PurchaseInfo
import com.example.ads.activity.data.dto.sdk.IKSdkBillingErrorCode

/**
 * Callback methods where billing events are reported.
 * Apps must implement one of these to construct a BillingProcessor.
 */

interface IKSdkBillingHandlerListener {
    fun onProductPurchased(productId: String, details: PurchaseInfo?)
    fun onPurchaseHistoryRestored()
    fun onBillingError(error: IKSdkBillingErrorCode, thr: Throwable?)
    fun onBillingInitialized()
    fun onBillingDataSave(isPaySuccess: Boolean) {

    }
}