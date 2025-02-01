package com.example.ads.activity.listener.pub

import com.example.ads.activity.data.dto.pub.IKBillingError

interface IKNewBillingHandlerListener  {
    fun onProductPurchased(productId: String, orderId: String?, purchaseToken : String?)
    fun onPurchaseHistoryRestored()
    fun onBillingError(error: IKBillingError)
    fun onBillingInitialized()
    fun onBillingDataSave(isPaySuccess: Boolean) {
    }
}