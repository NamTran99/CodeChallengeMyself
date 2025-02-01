package com.example.ads.activity.listener.pub

import com.example.ads.activity.data.dto.pub.IKBillingError

/**
 * Callback methods where billing events are reported.
 * Apps must implement one of these to construct a BillingProcessor.
 */

interface IKBillingHandlerListener {
    fun onProductPurchased(productId: String, orderId: String?)
    fun onPurchaseHistoryRestored()
    fun onBillingError(error: IKBillingError)
    fun onBillingInitialized()
    fun onBillingDataSave(isPaySuccess: Boolean) {

    }
}