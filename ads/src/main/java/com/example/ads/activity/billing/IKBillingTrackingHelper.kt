package com.example.ads.activity.billing

import android.os.Bundle
import com.example.ads.activity.billing.listener.IKBillingTrackingHelperInterFace
//import com.example.ads.activity.tracking.CoreTracking

object IKBillingTrackingHelper : IKBillingTrackingHelperInterFace {

    override fun trackLoadRemoteConfig(loadStatus: String, loadTime: String) {
        val bundle = Bundle()
        bundle.putString("load_status", loadStatus)
        bundle.putString("load_time", loadTime)
//        CoreTracking.customizeTracking("sdk_load_remote_config", true, bundle)
    }

    override fun trackLoadPriceStart() {
        val bundle = Bundle()
        bundle.putString("action_name", "start_load")
//        CoreTracking.customizeTracking("sdk_load_price", true, bundle)
    }

    override fun trackLoadPriceLoaded(
        loadStatus: String,
        loadTime: String,
        errorDetail: String?
    ) {
        val bundle = Bundle()
        bundle.putString("action_name", "loaded")
        bundle.putString("load_status", loadStatus)
        bundle.putString("load_time", loadTime)
        errorDetail?.let { bundle.putString("error_detail", it) }
//        CoreTracking.customizeTracking("sdk_load_price", true, bundle)
    }

    override fun trackPremiumScreenOpen(premiumScreenName: String, screenFrom: String) {
        val bundle = Bundle()
        bundle.putString("action_name", "open")
        bundle.putString("premium_screen_name", premiumScreenName)
        bundle.putString("screen_from", screenFrom)
//        CoreTracking.customizeTracking("sdk_premium_track", true, bundle)
    }

    override fun trackPremiumProductSelect(
        premiumScreenName: String,
        productId: String,
        productType: String,
        price: String,
        currency: String
    ) {
        val bundle = Bundle()
        bundle.putString("action_name", "select_product")
        bundle.putString("premium_screen_name", premiumScreenName)
        bundle.putString("product_id", productId)
        bundle.putString("product_type", productType)
        bundle.putString("price", price)
        bundle.putString("currency", currency)
//        CoreTracking.customizeTracking("sdk_premium_track", true, bundle)
    }

    override fun trackPremiumPurchaseFinish(
        premiumScreenName: String,
        productId: String,
        productType: String,
        price: String,
        currency: String,
        purchaseStatus: String,
        errorDetail: String?
    ) {
        val bundle = Bundle()
        bundle.putString("action_name", "finish_purchase")
        bundle.putString("premium_screen_name", premiumScreenName)
        bundle.putString("product_id", productId)
        bundle.putString("product_type", productType)
        bundle.putString("price", price)
        bundle.putString("currency", currency)
        bundle.putString("purchase_status", purchaseStatus)
        errorDetail?.let { bundle.putString("error_detail", it) }
//        CoreTracking.customizeTracking("sdk_premium_track", true, bundle)
    }

    override fun trackPremiumRestore(
        premiumScreenName: String,
        productId: String,
        productType: String,
        restoreStatus: String,
        price: String,
        currency: String
    ) {
        val bundle = Bundle()
        bundle.putString("action_name", "restore")
        bundle.putString("premium_screen_name", premiumScreenName)
        bundle.putString("product_id", productId)
        bundle.putString("product_type", productType)
        bundle.putString("restore_status", restoreStatus)
        bundle.putString("price", price)
        bundle.putString("currency", currency)
//        CoreTracking.customizeTracking("sdk_premium_track", true, bundle)
    }
}