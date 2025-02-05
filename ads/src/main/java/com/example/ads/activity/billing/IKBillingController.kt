package com.example.ads.activity.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.google.gson.reflect.TypeToken
import com.example.ads.activity.billing.core.IKBillingCore
import com.example.ads.activity.billing.listener.IKBillingInterface
import com.example.ads.activity.billing.listener.IKOnQueryHistoryListener
import com.example.ads.activity.billing.dto.IKSdkBillingDataDto
import com.example.ads.activity.billing.dto.IKSdkBillingDto
import com.example.ads.activity.billing.dto.SdkProductDetails
import com.example.ads.activity.billing.listener.IKBillingValueListenerBase
import com.example.ads.activity.billing.listener.IKPurchasesUpdatedListener
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.listener.keep.SDKIAPProductIDProvider
import com.example.ads.activity.listener.pub.IKBillingDetailListener
import com.example.ads.activity.listener.pub.IKBillingHandlerListener
import com.example.ads.activity.listener.pub.IKBillingInitialListener
import com.example.ads.activity.listener.pub.IKBillingListener
import com.example.ads.activity.listener.pub.IKBillingPurchaseListener
import com.example.ads.activity.listener.pub.IKNewBillingHandlerListener

@Deprecated(
    "Deprecated",
    replaceWith = ReplaceWith("IKBillingHelper", "com.ikame.android.sdk.billing.IKBillingHelper")
)
object IKBillingController : IKBillingInterface {

    override fun updateBillingProvider(provider: SDKIAPProductIDProvider?) {
        IKBillingCore.updateBillingProvider(provider)
    }

    override fun initBilling(
        context: Context,
        provider: SDKIAPProductIDProvider?,
        callback: IKBillingInitialListener?
    ) {
        IKBillingCore.initBilling(context, provider, callback)
    }

    override fun initBilling(context: Context, provider: SDKIAPProductIDProvider?) {
        IKBillingCore.initBilling(context, provider, null)
    }

    override fun release() {
        IKBillingCore.release()
    }

    override fun isConnected(): Boolean? {
        return IKBillingCore.isConnected()
    }

    override fun getBillingClient(): BillingClient? {
        return IKBillingCore.getBillingClient()
    }

    override fun setPurchasesUpdatedListener(callback: IKPurchasesUpdatedListener?) {
        IKBillingCore.setPurchasesUpdatedListener(callback)
    }

    override fun reCheckIAP(listener: IKBillingListener?, hasDelay: Boolean) {
        IKBillingCore.reCheckIAP(listener, hasDelay)
    }

    override fun reCheckIAP(listener: IKBillingListener?) {
        IKBillingCore.reCheckIAP(listener)
    }

    override fun setBillingInitialListener(listener: IKBillingInitialListener?) {
        IKBillingCore.setBillingInitialListener(listener)
    }

    override fun getPurchaseDetail(
        productId: String,
        callback: IKBillingDetailListener<SdkProductDetails>?
    ) {
        IKBillingCore.getPurchaseDetail(productId, callback)
    }

    override fun getPricePurchase(productId: String, sale: Int, callback: IKBillingValueListenerBase?) {
        IKBillingCore.getPricePurchase(productId, sale, callback)
    }

    override fun getPricePurchase(productId: String, callback: IKBillingValueListenerBase?) {
        IKBillingCore.getPricePurchase(productId, callback)
    }

    override suspend fun getPricePurchaseAsync(
        productId: String,
        sale: Int,
        callback: IKBillingValueListenerBase?
    ) {
        IKBillingCore.getPricePurchaseAsync(productId, sale, callback)
    }

    override suspend fun getPricePurchaseAsync(
        productId: String,
        callback: IKBillingValueListenerBase?
    ) {
        IKBillingCore.getPricePurchaseAsync(productId, callback)
    }

    override fun getSubscriptionDetail(
        productId: String,
        callback: IKBillingDetailListener<SdkProductDetails>?
    ) {
        IKBillingCore.getSubscriptionDetail(productId, callback)
    }

    override fun getPriceSubscribe(
        productId: String,
        sale: Int,
        callback: IKBillingValueListenerBase?
    ) {
        IKBillingCore.getPriceSubscribe(productId, sale, callback)
    }

    override fun getPriceSubscribe(productId: String, callback: IKBillingValueListenerBase?) {
        IKBillingCore.getPriceSubscribe(productId, callback)
    }

    override suspend fun getPriceSubscribeAsync(
        productId: String,
        sale: Int,
        callback: IKBillingValueListenerBase?
    ) {
        IKBillingCore.getPriceSubscribeAsync(productId, sale, callback)
    }

    override suspend fun getPriceSubscribeAsync(
        productId: String,
        callback: IKBillingValueListenerBase?
    ) {
        IKBillingCore.getPriceSubscribeAsync(productId, callback)
    }

    override fun subscribe(
        activity: Activity?,
        productId: String,
        listener: IKBillingPurchaseListener?
    ) {
        IKBillingCore.subscribe(activity, productId, listener)
    }

    override fun removeHandlerListener() {
        IKBillingCore.removeHandlerListener()
    }

    override fun setBillingListener(listener: IKBillingHandlerListener) {
        IKBillingCore.setBillingListener(listener)
    }

    override fun setBillingListener(listener: IKNewBillingHandlerListener) {
        IKBillingCore.setBillingListener(listener)
    }

    override fun purchase(
        activity: Activity?,
        productId: String,
        listener: IKBillingPurchaseListener?
    ) {
        IKBillingCore.purchase(activity, productId, listener)
    }

    override fun purchase(
        activity: Activity?,
        productId: String,
        listener: IKBillingPurchaseListener?,
        hasBuyMultipleTime: Boolean
    ) {
        IKBillingCore.purchase(activity, productId, listener, hasBuyMultipleTime)
    }

    override fun handlePurchase(
        activity: Activity,
        productId: String,
        listener: IKBillingPurchaseListener?
    ) {
        IKBillingCore.handlePurchase(activity, productId, listener)
    }

    override fun purchaseMultipleTime(
        activity: Activity?,
        productId: String,
        listener: IKBillingPurchaseListener?
    ) {
        IKBillingCore.purchaseMultipleTime(activity, productId, listener)
    }

    override fun checkInitialized(): Boolean {
        return IKBillingCore.checkInitialized()
    }

    override fun isIabServiceAvailable(context: Context?): Boolean {
        return IKBillingCore.isIabServiceAvailable(context)
    }

    override fun isFeatureSupported(summary: String): BillingResult? {
        return IKBillingCore.isFeatureSupported(summary)
    }

    override suspend fun isProductSubscribed(productId: String): Boolean {
        return IKBillingCore.isProductSubscribed(productId)
    }

    override suspend fun isProductPurchased(productId: String): Boolean {
        return IKBillingCore.isProductPurchased(productId)
    }

    override fun updateSubscription(
        activity: Activity?,
        oldProductId: String,
        productId: String,
        listener: IKBillingPurchaseListener?
    ) {
        IKBillingCore.updateSubscription(activity, oldProductId, productId, listener)
    }

    override fun setFirstIapStatusListeners(listener: IKBillingListener?) {
        IKBillingCore.setFirstIapStatusListeners(listener)
    }

    override fun queryPurchaseHistoryAsync(listener: IKOnQueryHistoryListener) {
        IKBillingCore.queryPurchaseHistoryAsync(listener)
    }

    override fun querySubHistoryAsync(listener: IKOnQueryHistoryListener) {
        IKBillingCore.querySubHistoryAsync(listener)
    }

    override suspend fun getListConfigData(screen: String): ArrayList<IKSdkBillingDataDto> {
        val data = IKBillingCore.getListConfigData(screen)
        if (data.isNullOrBlank()) return arrayListOf()

        val dataList = kotlin.runCatching {
            SDKDataHolder.getObject<ArrayList<IKSdkBillingDto>>(
                data, object : TypeToken<ArrayList<IKSdkBillingDto>>() {}.type
            ) ?: arrayListOf()
        }.getOrNull() ?: arrayListOf()
        return dataList.find { it.screen == screen }?.data ?: arrayListOf()
    }

    override fun setDebugMode(value: Boolean) {
        IKBillingCore.setDebugMode(value)
    }
}
