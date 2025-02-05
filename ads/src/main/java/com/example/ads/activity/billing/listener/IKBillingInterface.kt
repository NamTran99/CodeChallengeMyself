package com.example.ads.activity.billing.listener

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.example.ads.activity.billing.dto.IKSdkBillingDataDto
import com.example.ads.activity.billing.dto.SdkProductDetails
import com.example.ads.activity.listener.keep.SDKIAPProductIDProvider
import com.example.ads.activity.listener.pub.IKBillingDetailListener
import com.example.ads.activity.listener.pub.IKBillingHandlerListener
import com.example.ads.activity.listener.pub.IKBillingInitialListener
import com.example.ads.activity.listener.pub.IKBillingListener
import com.example.ads.activity.listener.pub.IKBillingPurchaseListener
import com.example.ads.activity.listener.pub.IKNewBillingHandlerListener

interface IKBillingInterface {
    fun initBilling(
        context: Context,
        provider: SDKIAPProductIDProvider?,
        callback: IKBillingInitialListener?
    )

    fun initBilling(
        context: Context,
        provider: SDKIAPProductIDProvider?
    )

    fun updateBillingProvider(provider: SDKIAPProductIDProvider?)
    fun release()
    fun isConnected(): Boolean?
    fun getBillingClient(): BillingClient?
    fun setPurchasesUpdatedListener(callback: IKPurchasesUpdatedListener?)
    fun reCheckIAP(
        listener: IKBillingListener?,
        hasDelay: Boolean = false
    )

    fun reCheckIAP(
        listener: IKBillingListener?
    )

    fun setBillingInitialListener(listener: IKBillingInitialListener?)

    fun getPurchaseDetail(
        productId: String,
        callback: IKBillingDetailListener<SdkProductDetails>?
    )

    fun getPricePurchase(
        productId: String,
        sale: Int = 0,
        callback: IKBillingValueListenerBase?
    )

    suspend fun getPricePurchaseAsync(
        productId: String,
        sale: Int = 0,
        callback: IKBillingValueListenerBase?
    )

    fun getPricePurchase(
        productId: String,
        callback: IKBillingValueListenerBase?
    )

    suspend fun getPricePurchaseAsync(
        productId: String,
        callback: IKBillingValueListenerBase?
    )

    fun getSubscriptionDetail(
        productId: String,
        callback: IKBillingDetailListener<SdkProductDetails>?
    )

    fun getPriceSubscribe(
        productId: String,
        sale: Int = 0,
        callback: IKBillingValueListenerBase?
    )

    suspend fun getPriceSubscribeAsync(
        productId: String,
        sale: Int = 0,
        callback: IKBillingValueListenerBase?
    )

    fun getPriceSubscribe(
        productId: String,
        callback: IKBillingValueListenerBase?
    )

    suspend fun getPriceSubscribeAsync(
        productId: String,
        callback: IKBillingValueListenerBase?
    )

    fun subscribe(
        activity: Activity?,
        productId: String,
        listener: IKBillingPurchaseListener? = null
    )

    fun removeHandlerListener()
    fun setBillingListener(listener: IKBillingHandlerListener)
    fun setBillingListener(listener: IKNewBillingHandlerListener)
    fun purchase(
        activity: Activity?,
        productId: String,
        listener: IKBillingPurchaseListener? = null
    )

    fun handlePurchase(
        activity: Activity,
        productId: String,
        listener: IKBillingPurchaseListener? = null
    )

    fun purchase(
        activity: Activity?,
        productId: String,
        listener: IKBillingPurchaseListener? = null,
        hasBuyMultipleTime: Boolean
    )

    fun purchaseMultipleTime(
        activity: Activity?,
        productId: String,
        listener: IKBillingPurchaseListener? = null
    )

    fun checkInitialized(): Boolean
    fun isIabServiceAvailable(context: Context?): Boolean
    fun isFeatureSupported(summary: String): BillingResult?
    suspend fun isProductSubscribed(productId: String): Boolean
    suspend fun isProductPurchased(productId: String): Boolean
    fun updateSubscription(
        activity: Activity?,
        oldProductId: String,
        productId: String,
        listener: IKBillingPurchaseListener? = null
    )

    fun setFirstIapStatusListeners(listener: IKBillingListener?)
    fun queryPurchaseHistoryAsync(listener: IKOnQueryHistoryListener)
    fun querySubHistoryAsync(listener: IKOnQueryHistoryListener)
    suspend fun getListConfigData(screen: String): ArrayList<IKSdkBillingDataDto>
    fun setDebugMode(value: Boolean)
}