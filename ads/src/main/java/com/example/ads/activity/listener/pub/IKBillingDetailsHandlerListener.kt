package com.example.ads.activity.listener.pub

import com.example.ads.activity.billing.dto.PurchaseInfo

/**
 * Callback methods where billing events are reported.
 * Apps must implement one of these to construct a BillingProcessor.
 */

interface IKBillingDetailsHandlerListener : IKBillingHandlerListener {
    fun onDetailProductPurchased(details: PurchaseInfo?)
}