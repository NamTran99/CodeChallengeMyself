/*
 * Created by CuongNV on 2/3/23, 1:53 PM
 * Copyright (c) by Begamob.com 2023 . All rights reserved.
 * Last modified 12/30/22, 9:44 AM
 */

package com.example.ads.activity.billing.core

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.example.ads.activity.IKSdkConstants
import com.example.ads.activity.billing.dto.PurchaseInfo
import com.example.ads.activity.billing.dto.SdkProductDetails
import com.example.ads.activity.billing.listener.IKBillingPriceListener
import com.example.ads.activity.billing.listener.IKBillingValueListenerBase
import com.example.ads.activity.billing.listener.IKOnQueryHistoryListener
import com.example.ads.activity.billing.listener.IKPurchasesUpdatedListener
//import com.example.ads.activity.core.firebase.IKRemoteDataManager
import com.example.ads.activity.data.dto.pub.IKBillingError
import com.example.ads.activity.data.dto.sdk.IKSdkBillingErrorCode
import com.example.ads.activity.listener.keep.IKBillingProvider
import com.example.ads.activity.listener.keep.SDKIAPProductIDProvider
import com.example.ads.activity.listener.pub.IKBillingDetailListener
import com.example.ads.activity.listener.pub.IKBillingDetailsHandlerListener
import com.example.ads.activity.listener.pub.IKBillingHandlerListener
import com.example.ads.activity.listener.pub.IKBillingInitialListener
import com.example.ads.activity.listener.pub.IKBillingListener
import com.example.ads.activity.listener.pub.IKBillingPurchaseListener
import com.example.ads.activity.listener.pub.IKBillingValueListener
import com.example.ads.activity.listener.pub.IKNewBillingHandlerListener
import com.example.ads.activity.listener.pub.IKPurchaseListener
import com.example.ads.activity.listener.sdk.IKSdkBillingHandlerListener
import com.example.ads.activity.listener.sdk.IKSdkBillingPurchaseListener
import com.example.ads.activity.utils.IKLogs
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


object IKBillingCore : IKBillingBase() {
    const val TAG = "BillingHelper:"
    private var mIsDebugMode = false

    fun setDebugMode(value: Boolean) {
        mIsDebugMode = value
    }

    override fun isConnected() = mBillingProcess?.isConnected
    override fun updateBillingProvider(provider: SDKIAPProductIDProvider?) {
        setupBillingProvider(provider)
    }

    override fun initBilling(
        context: Context,
        provider: SDKIAPProductIDProvider?,
        callback: IKBillingInitialListener?
    ) {
        setupBillingProvider(provider)
        showLogD("initBilling") { "start" }
        if (mBillingProcess?.isConnected == true) {
            showLogD("initBilling") { "isConnected break" }
            callback?.onInitialized()
            mBillingInitialListener?.onInitialized()
            return
        }
        if (mIsInitializing) {
            callback?.onInitError(IKBillingError(0, "Is Initializing"))
            showLogD("initBilling") { "IsInitializing break" }
            return
        }

        var licenseKey = IKSdkConstants.LICENSE_KEY
        if (provider is IKBillingProvider) {
            licenseKey = (provider as? IKBillingProvider)?.licenseKey()?.ifBlank {
                IKSdkConstants.LICENSE_KEY
            } ?: IKSdkConstants.LICENSE_KEY
        }
        if (!mIsDebugMode) {
            if (licenseKey.isBlank()) {
                callback?.onInitError(IKBillingError(0, "initBilling: error licenseKey is Blank"))
                IKLogs.dNone("BillingHelper") {
                    "initBilling: error licenseKey is Blank"
                }
                return
            }
        }
        mBillingProcess =
            BillingProcessor(context, licenseKey, object : IKSdkBillingHandlerListener {
                override fun onProductPurchased(
                    productId: String, details: PurchaseInfo?
                ) {
                    showLogD("initBilling") { "onProductPurchased, productId=$productId" }
                    mBillingUiScope.launchWithSupervisorJob(Dispatchers.IO) {
                        showLogD("whenProductPurchased") { "trackingIAP start run" }
                        trackingIAP(productId, details)
                        IkBillingSecurity.setTempCacheIapPackage(productId)
                        kotlin.runCatching {
                            mListener?.onProductPurchased(productId, details)
                        }
                    }

                    whenProductPurchased(productId)
                }

                override fun onPurchaseHistoryRestored() {
                    mBillingUiScope.launchWithSupervisorJob {
                        kotlin.runCatching {
                            mListener?.onPurchaseHistoryRestored()
                        }
                    }
                    reCheckIAP(mFirstIapStatusListener, true)
                    showLogD("initBilling") { "onPurchaseHistoryRestored" }
                }

                override fun onBillingError(error: IKSdkBillingErrorCode, thr: Throwable?) {
                    mBillingUiScope.launchWithSupervisorJob {
                        kotlin.runCatching {
                            mListener?.onBillingError(error, thr)
                        }
                    }
                    reCheckIAP(mFirstIapStatusListener, true)
                    showLogD("initBilling") { "onBillingError $error" }
                }

                override fun onBillingInitialized() {
                    reCheckIAP(mFirstIapStatusListener, true)
                    kotlin.runCatching {
                        mListener?.onBillingInitialized()
                    }
                    showLogD("initBilling") { "onBillingInitialized" }
                }
            })
        showLogD("initBilling") { "start initBillingProcess" }
        kotlin.runCatching {
            if (mBillingProcess?.isConnected == false) {
                mIsInitializing = true
                mBillingProcess?.initialize(object : IKBillingInitialListener {
                    override fun onInitError(error: IKBillingError) {
                        mIsInitializing = false
                        callback?.onInitError(error)
                        mBillingInitialListener?.onInitError(error)
                        showLogD("initBilling") { "onInitError $error" }
                    }

                    override fun onInitialized() {
                        mIsInitializing = false
                        callback?.onInitialized()
                        mBillingInitialListener?.onInitialized()
                        showLogD("initBilling") { "onInitialized" }
                    }
                })
                mBillingUiScope.launchWithSupervisorJob {
                    delay(20000)
                    mIsInitializing = false
                }
            } else {
                callback?.onInitialized()
                mBillingInitialListener?.onInitialized()
            }
        }
    }

    /**
     * Rechecks in-app purchases for billing success or failure.
     *
     * @param listener a lambda function that will be executed on successful billing.
     * @param hasDelay a flag indicating whether there should be a delay in the billing check, with a default value of `false`.
     */
    override fun reCheckIAP(
        listener: IKBillingListener?, hasDelay: Boolean
    ) {
        showLogD("reCheckIAP") { "start run" }
        kotlin.runCatching {
            mBillingProcess?.loadOwnedPurchasesFromGoogleAsync(object :
                BillingProcessor.IPurchasesResponseListener {
                override fun onPurchasesSuccess(purchaseInfo: PurchaseInfo?) {
                    showLogD("reCheckIAP") { "onPurchasesSuccess" }
                    checkIAP(purchaseInfo, listener, hasDelay)
                }

                override fun onPurchasesError(error: IKSdkBillingErrorCode) {
                    showLogD("reCheckIAP") { "onPurchasesError $error" }
                    checkIAP(listener, hasDelay)
                }
            })
        }
    }

    override fun reCheckIAP(listener: IKBillingListener?) {
        reCheckIAP(listener, false)
    }

    override fun getPurchaseDetail(
        productId: String, callback: IKBillingDetailListener<SdkProductDetails>?
    ) {
        if (mBillingProcess == null) {
            callback?.onError(IKBillingError(IKSdkBillingErrorCode.BILLING_ERROR_INITIALIZE_PURCHASE))
            return
        }
        mBillingUiScope.launchWithSupervisorJob {
            mBillingProcess?.getPurchaseListingDetailsAsync(
                productId,
                object : BillingProcessor.IProductDetailsResponseListener {
                    override fun onProductDetailsResponse(products: List<SdkProductDetails>?) {
                        postResult {
                            callback?.onSuccess(products?.firstOrNull())
                        }
                    }

                    override fun onProductDetailsError(error: IKBillingError) {
                        postResult {
                            callback?.onError(error)
                        }
                    }

                })
        }
    }


    override fun getPricePurchase(
        productId: String, sale: Int, callback: IKBillingValueListenerBase?
    ) {
        mBillingUiScope.launchWithSupervisorJob {
            getPurchaseDetail(productId, object : IKBillingDetailListener<SdkProductDetails> {
                override fun onSuccess(value: SdkProductDetails?) {
                    val price = value?.priceValue ?: 0.0
                    if (callback is IKBillingValueListener) {
                        val salePrice = price + price * (sale / 100f)
                        postResult {
                            callback.onResult(
                                value?.priceText
                                    ?: (formatValue(price) + " ${value?.currency ?: IKSdkDefConst.EMPTY}"),
                                formatValue(salePrice)
                            )
                        }
                    } else if (callback is IKBillingPriceListener) {
                        postResult {
                            callback.onResult(
                                value?.priceText
                                    ?: (formatValue(price) + " ${value?.currency ?: IKSdkDefConst.EMPTY}")
                            )
                        }
                    }
                }

                override fun onError(error: IKBillingError) {
                    postResult {
                        callback?.onError(error)
                    }
                }

            })
        }
    }


    override suspend fun getPricePurchaseAsync(
        productId: String, sale: Int, callback: IKBillingValueListenerBase?
    ) {
        getPurchaseDetailAsync(productId, object : IKBillingDetailListener<SdkProductDetails> {
            override fun onSuccess(value: SdkProductDetails?) {
                val price = value?.priceValue ?: 0.0
                if (callback is IKBillingValueListener) {
                    val salePrice = price + price * (sale / 100f)
                    postResult {
                        callback.onResult(
                            value?.priceText
                                ?: (formatValue(price) + " ${value?.currency ?: IKSdkDefConst.EMPTY}"),
                            formatValue(salePrice)
                        )
                    }
                } else if (callback is IKBillingPriceListener) {
                    postResult {
                        callback.onResult(
                            value?.priceText
                                ?: (formatValue(price) + " ${value?.currency ?: IKSdkDefConst.EMPTY}")
                        )
                    }
                }
            }

            override fun onError(error: IKBillingError) {
                postResult {
                    callback?.onError(error)
                }
            }

        })
    }


    override fun getPricePurchase(
        productId: String, callback: IKBillingValueListenerBase?
    ) {
        getPricePurchase(productId, 0, callback)
    }


    override suspend fun getPricePurchaseAsync(
        productId: String, callback: IKBillingValueListenerBase?
    ) {
        getPricePurchaseAsync(productId, 0, callback)
    }


    override fun getSubscriptionDetail(
        productId: String, callback: IKBillingDetailListener<SdkProductDetails>?
    ) {
        if (mBillingProcess == null) {
            callback?.onError(IKBillingError(IKSdkBillingErrorCode.BILLING_ERROR_INITIALIZE_PURCHASE))
            return
        }
        mBillingUiScope.launchWithSupervisorJob {
            mBillingProcess?.getSubscriptionListingDetailsAsync(
                productId,
                object : BillingProcessor.IProductDetailsResponseListener {
                    override fun onProductDetailsResponse(products: List<SdkProductDetails>?) {
                        postResult {
                            callback?.onSuccess(products?.firstOrNull())
                        }
                    }

                    override fun onProductDetailsError(error: IKBillingError) {
                        postResult {
                            callback?.onError(error)
                        }
                    }
                })
        }
    }

    override fun getPriceSubscribe(
        productId: String, sale: Int, callback: IKBillingValueListenerBase?
    ) {
        getSubscriptionDetail(productId, object : IKBillingDetailListener<SdkProductDetails> {
            override fun onSuccess(value: SdkProductDetails?) {
                val price = value?.priceValue ?: 0.0
                if (callback is IKBillingValueListener) {
                    val salePrice = price + price * (sale / 100f)
                    postResult {
                        callback.onResult(
                            value?.priceText
                                ?: (formatValue(price) + " ${value?.currency ?: IKSdkDefConst.EMPTY}"),
                            formatValue(salePrice)
                        )
                    }
                } else if (callback is IKBillingPriceListener) {
                    postResult {
                        callback.onResult(
                            value?.priceText
                                ?: (formatValue(price) + " ${value?.currency ?: IKSdkDefConst.EMPTY}")
                        )
                    }
                }
            }

            override fun onError(error: IKBillingError) {
                postResult {
                    callback?.onError(error)
                }
            }
        })

    }


    override suspend fun getPriceSubscribeAsync(
        productId: String, sale: Int, callback: IKBillingValueListenerBase?
    ) {
        getSubscriptionDetailAsync(productId, object : IKBillingDetailListener<SdkProductDetails> {
            override fun onSuccess(value: SdkProductDetails?) {
                val price = value?.priceValue ?: 0.0
                if (callback is IKBillingValueListener) {
                    val salePrice = price + price * (sale / 100f)
                    postResult {
                        callback.onResult(
                            value?.priceText
                                ?: (formatValue(price) + " ${value?.currency ?: IKSdkDefConst.EMPTY}"),
                            formatValue(salePrice)
                        )
                    }
                } else if (callback is IKBillingPriceListener) {
                    postResult {
                        callback.onResult(
                            value?.priceText
                                ?: (formatValue(price) + " ${value?.currency ?: IKSdkDefConst.EMPTY}")
                        )
                    }
                }
            }

            override fun onError(error: IKBillingError) {
                postResult {
                    callback?.onError(error)
                }
            }
        })

    }


    override fun getPriceSubscribe(
        productId: String, callback: IKBillingValueListenerBase?
    ) {
        getPriceSubscribe(productId, 0, callback)
    }


    override suspend fun getPriceSubscribeAsync(
        productId: String, callback: IKBillingValueListenerBase?
    ) {
        getPriceSubscribeAsync(productId, 0, callback)
    }

    private fun formatValue(value: Double): String {
        return try {
            val format = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))
            format.format(value)
        } catch (e: Exception) {
            value.toString()
        }
    }

    override fun subscribe(
        activity: Activity?, productId: String, listener: IKBillingPurchaseListener?
    ) {
        showLogD("subscribe") { "productId=$productId" }
        if (mBillingProcess == null || activity == null) {
            showLogD("subscribe") { " onBillingFail_ ${IKSdkBillingErrorCode.BILLING_CONTEXT_NOT_VALID}" }
            listener?.onBillingFail(
                productId, IKBillingError(IKSdkBillingErrorCode.BILLING_CONTEXT_NOT_VALID)
            )
            return
        }
        mBillingUiScope.launchWithSupervisorJob {
            val isSub = mBillingProcess?.isSubscribed(productId) ?: false
            if (isSub) {
                showLogD("subscribe") { " onProductAlreadyPurchased" }
                listener?.onProductAlreadyPurchased(
                    productId
                )
                return@launchWithSupervisorJob
            }

            showLogD("subscribe") { "start process" }
            mBillingProcess?.subscribe(activity, productId, object : IKSdkBillingPurchaseListener {
                override fun onProductAlreadyPurchased(
                    productId: String
                ) {
                    showLogD("subscribe") { " onProductAlreadyPurchased_ productId=$productId" }
                    postResult { listener?.onProductAlreadyPurchased(productId) }
                }

                override fun onBillingSuccess(purchaseInfo: PurchaseInfo?, productId: String) {
                    showLogD("subscribe") { " onBillingSuccess_ productId=$productId" }
                    postResult { listener?.onBillingSuccess(productId) }
                    postResult {
                        if (listener is IKPurchaseListener) listener.onPurchaseSuccess(
                            purchaseInfo
                        )
                    }
                }

                override fun onBillingFail(productId: String, error: IKSdkBillingErrorCode) {
                    showLogD("subscribe") { " onBillingFail_ $error" }
                    postResult { listener?.onBillingFail(productId, IKBillingError(error)) }
                }
            })
        }
    }


    override fun removeHandlerListener() {
        mListener = null
    }

    override fun setBillingListener(listener: IKBillingHandlerListener) {
        mListener = object : IKSdkBillingHandlerListener {
            override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
                postResult {
                    listener.onProductPurchased(productId, details?.purchaseData?.orderId)
                    if (listener is IKBillingDetailsHandlerListener) {
                        listener.onDetailProductPurchased(details)
                    }
                }
            }

            override fun onPurchaseHistoryRestored() {
                postResult {
                    listener.onPurchaseHistoryRestored()
                }
            }

            override fun onBillingError(error: IKSdkBillingErrorCode, thr: Throwable?) {
                postResult {
                    listener.onBillingError(IKBillingError(error))
                }
            }

            override fun onBillingInitialized() {
                postResult {
                    listener.onBillingInitialized()
                }
            }

            override fun onBillingDataSave(isPaySuccess: Boolean) {
                super.onBillingDataSave(isPaySuccess)
                postResult {
                    listener.onBillingDataSave(isPaySuccess)
                }
            }
        }
    }


    override fun setBillingListener(listener: IKNewBillingHandlerListener) {
        mListener = object : IKSdkBillingHandlerListener {
            override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
                postResult {
                    listener.onProductPurchased(
                        productId,
                        details?.purchaseData?.orderId,
                        details?.purchaseData?.purchaseToken
                    )
                }
            }

            override fun onPurchaseHistoryRestored() {
                postResult {
                    listener.onPurchaseHistoryRestored()
                }
            }

            override fun onBillingError(error: IKSdkBillingErrorCode, thr: Throwable?) {
                postResult {
                    listener.onBillingError(IKBillingError(error))
                }
            }

            override fun onBillingInitialized() {
                postResult {
                    listener.onBillingInitialized()
                }
            }

            override fun onBillingDataSave(isPaySuccess: Boolean) {
                super.onBillingDataSave(isPaySuccess)
                postResult {
                    listener.onBillingDataSave(isPaySuccess)
                }
            }
        }
    }

    override fun setBillingInitialListener(listener: IKBillingInitialListener?) {
        mBillingInitialListener = listener
    }


    override fun purchase(
        activity: Activity?, productId: String, listener: IKBillingPurchaseListener?
    ) {
        showLogD("purchase") { "productId=$productId" }
        if (mBillingProcess == null || activity == null) {
            showLogD("purchase") { "productId=$productId onBillingFail_ ${IKSdkBillingErrorCode.BILLING_CONTEXT_NOT_VALID}" }
            listener?.onBillingFail(
                productId, IKBillingError(IKSdkBillingErrorCode.BILLING_CONTEXT_NOT_VALID)
            )
        } else {
            mBillingUiScope.launchWithSupervisorJob {
                val isPur = mBillingProcess?.isPurchased(productId) ?: false
                if (isPur) {
                    showLogD("purchase") { "productId=$productId onProductAlreadyPurchased" }
                    listener?.onProductAlreadyPurchased(
                        productId
                    )
                } else {
                    showLogD("purchase") { "productId=$productId handlePurchase" }
                    handlePurchase(activity, productId, listener)
                }
            }
        }
    }

    override fun handlePurchase(
        activity: Activity, productId: String, listener: IKBillingPurchaseListener?
    ) {
        showLogD("purchase") { "handlePurchase start run $productId" }
        mBillingUiScope.launchWithSupervisorJob {
            mBillingProcess?.purchase(activity, productId, object : IKSdkBillingPurchaseListener {
                override fun onProductAlreadyPurchased(
                    productId: String
                ) {
                    showLogD("purchase") { "handlePurchase onProductAlreadyPurchased productId=$productId" }
                    postResult {
                        listener?.onProductAlreadyPurchased(productId)
                    }
                }

                override fun onBillingSuccess(purchaseInfo: PurchaseInfo?, productId: String) {
                    showLogD("purchase") { "handlePurchase onBillingSuccess_ productId=$productId" }
                    postResult {
                        listener?.onBillingSuccess(productId)
                    }
                    postResult {
                        if (listener is IKPurchaseListener) listener.onPurchaseSuccess(
                            purchaseInfo
                        )
                    }
                }

                override fun onBillingFail(
                    productId: String, error: IKSdkBillingErrorCode
                ) {
                    showLogD("purchase") { "handlePurchase onBillingFail_ $error" }
                    postResult {
                        listener?.onBillingFail(productId, IKBillingError(error))
                    }
                }
            })
        }
    }

    override fun purchase(
        activity: Activity?,
        productId: String,
        listener: IKBillingPurchaseListener?,
        hasBuyMultipleTime: Boolean
    ) {
        showLogD("purchase") { "productId=$productId, BuyMultipleTime=$hasBuyMultipleTime" }
        if (mBillingProcess == null || activity == null) {
            showLogD("purchase") {
                "productId=$productId, BuyMultipleTime=$hasBuyMultipleTime " + "onBillingFail_ ${IKSdkBillingErrorCode.BILLING_CONTEXT_NOT_VALID}"
            }
            listener?.onBillingFail(
                productId, IKBillingError(IKSdkBillingErrorCode.BILLING_CONTEXT_NOT_VALID)
            )
            return
        }
        mBillingUiScope.launchWithSupervisorJob {
            val isPurchased = mBillingProcess?.isPurchased(productId) ?: false
            if (isPurchased) {
                showLogD("purchase") {
                    "productId=$productId, BuyMultipleTime=$hasBuyMultipleTime isPurchased true"
                }
                if (hasBuyMultipleTime) {
                    mBillingProcess?.consumePurchaseAsync(productId,
                        object : BillingProcessor.IPurchasesResponseListener {
                            override fun onPurchasesSuccess(purchaseInfo: PurchaseInfo?) {
                                handlePurchase(activity, productId, listener)
                                showLogD("purchase") {
                                    "productId=$productId, BuyMultipleTime=$hasBuyMultipleTime onPurchasesSuccess"
                                }
                            }

                            override fun onPurchasesError(error: IKSdkBillingErrorCode) {
                                handlePurchase(activity, productId, listener)
                                showLogD("purchase") {
                                    "productId=$productId, BuyMultipleTime=$hasBuyMultipleTime onPurchasesError $error"
                                }
                            }
                        })
                } else {
                    listener?.onProductAlreadyPurchased(productId)
                }
            } else {
                showLogD("purchase") {
                    "productId=$productId, BuyMultipleTime=$hasBuyMultipleTime isPurchased false"
                }
                handlePurchase(activity, productId, listener)
            }
        }
    }


    override fun purchaseMultipleTime(
        activity: Activity?, productId: String, listener: IKBillingPurchaseListener?
    ) {
        showLogD("purchaseMultipleTime") { "productId=$productId" }
        if (mBillingProcess == null || activity == null) {
            showLogD("purchaseMultipleTime") {
                "productId=$productId, purchaseMultipleTime " + "onBillingFail_ ${IKSdkBillingErrorCode.BILLING_CONTEXT_NOT_VALID}"
            }
            listener?.onBillingFail(
                productId, IKBillingError(IKSdkBillingErrorCode.BILLING_CONTEXT_NOT_VALID)
            )
            return
        }
        mBillingUiScope.launchWithSupervisorJob {
            mBillingProcess?.consumePurchaseAsync(productId,
                object : BillingProcessor.IPurchasesResponseListener {
                    override fun onPurchasesSuccess(purchaseInfo: PurchaseInfo?) {
                        handlePurchase(activity, productId, listener)
                        showLogD("purchaseMultipleTime") {
                            "productId=$productId, purchaseMultipleTime onPurchasesSuccess"
                        }
                    }

                    override fun onPurchasesError(error: IKSdkBillingErrorCode) {
                        handlePurchase(activity, productId, listener)
                        showLogD("purchaseMultipleTime") {
                            "productId=$productId, purchaseMultipleTime onPurchasesError $error"
                        }
                    }
                })
        }
    }


    override fun checkInitialized(): Boolean {
        return mBillingProcess?.isInitialized ?: true
    }


    override fun isIabServiceAvailable(context: Context?): Boolean {
        if (context == null) return false
        return BillingProcessor.isIabServiceAvailable(context)
    }


    override fun isFeatureSupported(summary: String): BillingResult? {
        return mBillingProcess?.isFeatureSupported(summary)
    }


    override suspend fun isProductPurchased(productId: String): Boolean {
        return mBillingProcess?.isPurchased(productId) ?: false
    }


    override suspend fun isProductSubscribed(productId: String): Boolean {
        return mBillingProcess?.isSubscribed(productId) ?: false
    }


    override fun updateSubscription(
        activity: Activity?,
        oldProductId: String,
        productId: String,
        listener: IKBillingPurchaseListener?
    ) {
        if (mBillingProcess == null || activity == null) {
            listener?.onBillingFail(
                productId, IKBillingError(IKSdkBillingErrorCode.BILLING_CONTEXT_NOT_VALID)
            )
            return
        }
        mBillingUiScope.launchWithSupervisorJob {
            val resultBilling = mBillingProcess?.isSubscribed(productId) ?: false
            if (resultBilling) {
                listener?.onProductAlreadyPurchased(
                    productId
                )
                return@launchWithSupervisorJob
            }
            mBillingUiScope.launchWithSupervisorJob {
                mBillingProcess?.updateSubscription(activity,
                    oldProductId,
                    productId,
                    object : IKSdkBillingPurchaseListener {
                        override fun onProductAlreadyPurchased(
                            productId: String
                        ) {
                            postResult { listener?.onProductAlreadyPurchased(productId) }
                        }

                        override fun onBillingSuccess(
                            purchaseInfo: PurchaseInfo?,
                            productId: String
                        ) {
                            postResult { listener?.onBillingSuccess(productId) }
                            postResult {
                                if (listener is IKPurchaseListener) listener.onPurchaseSuccess(
                                    purchaseInfo
                                )
                            }
                        }

                        override fun onBillingFail(
                            productId: String,
                            error: IKSdkBillingErrorCode
                        ) {
                            postResult { listener?.onBillingFail(productId, IKBillingError(error)) }
                        }
                    })
            }
        }
    }


    override fun setFirstIapStatusListeners(listener: IKBillingListener?) {
        mFirstIapStatusListener = listener
    }


    override fun queryPurchaseHistoryAsync(listener: IKOnQueryHistoryListener) {
        if (mBillingProcess == null) {
            listener.onFailure(IKBillingError(IKSdkBillingErrorCode.BILLING_ERROR_INITIALIZE_PURCHASE))
            return
        }
        mBillingProcess?.querySubHistoryAsync(listener, BillingClient.ProductType.INAPP)
    }


    override fun querySubHistoryAsync(listener: IKOnQueryHistoryListener) {
        if (mBillingProcess == null) {
            listener.onFailure(IKBillingError(IKSdkBillingErrorCode.BILLING_ERROR_INITIALIZE_PURCHASE))
            return
        }
        mBillingProcess?.querySubHistoryAsync(listener, BillingClient.ProductType.SUBS)
    }

    override suspend fun getListConfigData(screen: String): String? {
        return ""
//        return kotlin.runCatching {
//            IKRemoteDataManager.getRemoteConfigData()[IKSdkDefConst.Config.SDK_IAP_CONFIG]?.getString()
//        }.getOrNull()
    }

    private fun showLogSdk(tag: String, message: () -> String) {
        IKLogs.dSdk("BillingHelper") {
            "${tag}:" + message.invoke()
        }
    }

    override fun release() {
        mBillingProcess?.release()
    }

    override fun getBillingClient(): BillingClient? {
        return mBillingProcess?.getBillingClient()
    }

    override fun setPurchasesUpdatedListener(callback: IKPurchasesUpdatedListener?) {
        if (mBillingProcess == null) {
            callback?.onFailure(IKBillingError(IKSdkBillingErrorCode.BILLING_ERROR_INITIALIZE_PURCHASE))
            return
        }
        mBillingProcess?.setPurchasesUpdatedListener { p0, p1 ->
            callback?.onSuccess(p0, p1)
        }
    }
}