package com.example.ads.activity.billing.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.ads.activity.billing.core.BillingCache.Companion.verifyCache
import com.example.ads.activity.billing.listener.IKOnQueryHistoryListener
import com.example.ads.activity.billing.dto.IkSdkBillingConst
import com.example.ads.activity.billing.dto.PurchaseInfo
import com.example.ads.activity.billing.dto.SdkProductDetails
import com.example.ads.activity.data.dto.pub.IKBillingError
import com.example.ads.activity.data.dto.sdk.IKSdkBillingErrorCode
import com.example.ads.activity.data.local.IKSdkDataStoreBilling
import com.example.ads.activity.data.local.IKSdkDataStoreConst
import com.example.ads.activity.listener.pub.IKBillingInitialListener
import com.example.ads.activity.listener.sdk.IKSdkBillingHandlerListener
import com.example.ads.activity.listener.sdk.IKSdkBillingPurchaseListener
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


class BillingProcessor private constructor(
    context: Context,
    private val signatureBase64: String?,
    merchantId: String?,
    var billingListeneraaaa: IKSdkBillingHandlerListener?,
    bindImmediately: Boolean
) : BillingCacheBase(context) {
    private val purchaseHistoryRestoredKey: String
        get() = IKSdkDataStoreConst.Billing.createBooleanRef(preferencesBaseKey + RESTORE_KEY)
    private val purPurchasePayloadKey: String
        get() = IKSdkDataStoreConst.Billing.createStringRef(preferencesBaseKey + PURCHASE_PAYLOAD_CACHE_KEY)

    /**
     * Callback methods for notifying about success or failure attempt to fetch purchases from the server.
     */
    interface IPurchasesResponseListener {
        fun onPurchasesSuccess(purchaseInfo: PurchaseInfo?)
        fun onPurchasesError(error: IKSdkBillingErrorCode)
    }

    /**
     * Callback methods where result of SkuDetails fetch returned or error message on failure.
     */
    interface IProductDetailsResponseListener {
        fun onProductDetailsResponse(products: List<SdkProductDetails>?)
        fun onProductDetailsError(error: IKBillingError)
    }

    private var reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS
    private var billingService: BillingClient? = null
    private val cachedProducts: BillingCache = BillingCache(context, MANAGED_PRODUCTS_CACHE_KEY)
    private val cachedSubscriptions: BillingCache = BillingCache(context, SUBSCRIPTIONS_CACHE_KEY)
    private val developerMerchantId: String? = merchantId
    private var isSubsUpdateSupported = false
    private var isHistoryTaskExecuted = false
    private val mBillingJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + mBillingJob)
    private val sdkBillingListener: HashMap<String, IKSdkBillingPurchaseListener> = hashMapOf()
    private var mLastProductId = ""
    private var mPurchasesUpdatedListener: PurchasesUpdatedListener? = null

    constructor(
        context: Context,
        licenseKey: String?,
        handler: IKSdkBillingHandlerListener?
    ) : this(
        context,
        licenseKey,
        null,
        handler
    )

    constructor(
        context: Context, licenseKey: String?, merchantId: String?,
        handler: IKSdkBillingHandlerListener?
    ) : this(context, licenseKey, merchantId, handler, true)

    private var dateMerchantLimit1 //5th December 2012
            : Date? = null
    private var dateMerchantLimit2 //21st July 2015
            : Date? = null


    init {
        init(context)
        if (bindImmediately) {
//            initialize(null)
        }
        val calendar = Calendar.getInstance()
        calendar.set(2012, Calendar.DECEMBER, 5)
        dateMerchantLimit1 = calendar.time
        calendar.set(2015, Calendar.JULY, 21)
        dateMerchantLimit2 = calendar.time
    }

    private fun init(context: Context) {
        if (billingService != null)
            return
        val calendar = Calendar.getInstance()
        calendar.set(2012, Calendar.DECEMBER, 5)
        dateMerchantLimit1 = calendar.time
        calendar.set(2015, Calendar.JULY, 21)
        dateMerchantLimit2 = calendar.time
        val listener =
            PurchasesUpdatedListener { billingResult, purchases ->
                uiScope.launchWithSupervisorJob(Dispatchers.Default) {
                    when (val responseCode = billingResult.responseCode) {
                        BillingClient.BillingResponseCode.OK -> {
                            purchases?.forEach {
                                handlePurchase(it)
                            }
                        }

                        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                            val purchasePayload: String = purchasePayload()
                            if (TextUtils.isEmpty(purchasePayload)) {
                                loadOwnedPurchasesFromGoogleAsync(null)
                            } else {
                                handleItemAlreadyOwned(
                                    purchasePayload.split(":".toRegex())
                                        .dropLastWhile { it.isEmpty() }
                                        .toTypedArray().getOrNull(1) ?: IKSdkDefConst.EMPTY, null)
                                savePurchasePayload(null)
                            }
                            reportBillingError(
                                mapResponseCode(responseCode),
                                Throwable(billingResult.debugMessage)
                            )
                        }

                        BillingClient.BillingResponseCode.USER_CANCELED, BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE, BillingClient.BillingResponseCode.BILLING_UNAVAILABLE, BillingClient.BillingResponseCode.ITEM_UNAVAILABLE, BillingClient.BillingResponseCode.DEVELOPER_ERROR, BillingClient.BillingResponseCode.ERROR, BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                            reportBillingError(
                                mapResponseCode(responseCode),
                                Throwable(billingResult.debugMessage)
                            )
                        }

                        else -> {
                            reportBillingError(
                                mapResponseCode(responseCode),
                                Throwable(billingResult.debugMessage)
                            )
                        }
                    }
                }
                mPurchasesUpdatedListener?.onPurchasesUpdated(billingResult, purchases)
            }
        billingService = BillingClient.newBuilder(context)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .setListener(listener)
            .build()
    }

    /**
     * Establishing Connection to Google Play
     * you should call this method if used [.newBillingProcessor] method or called constructor
     * with bindImmediately = false
     */
    fun initialize(callback: IKBillingInitialListener?) {
        var retryCount = 5
        if (billingService == null)
            init(context)
        if (billingService != null && billingService?.isReady == true) {
            callback?.onInitialized()
            return
        }
        if (billingService?.isReady != true) {
            billingService?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS
                        // The BillingClient is ready. You can query purchases here.

                        //Initialize history of purchases if any exist.
                        if (!isHistoryTaskExecuted) {
                            uiScope.launchWithSupervisorJob(Dispatchers.Default) {
                                var restored = false
                                if (!isPurchaseHistoryRestored()) {
                                    loadOwnedPurchasesFromGoogleAsync(null)
                                    restored = true
                                } else restored = false

                                isHistoryTaskExecuted = true
                                if (restored) {
                                    setPurchaseHistoryRestored()
                                    if (billingListeneraaaa != null) {
                                        billingListeneraaaa?.onPurchaseHistoryRestored()
                                    }
                                }
                                if (billingListeneraaaa != null) {
                                    billingListeneraaaa?.onBillingInitialized()
                                }
                            }
                        }
                        callback?.onInitialized()
                    } else {
                        if (retryCount < 5) {
                            retryCount++
                            retryBillingClientConnection(callback)
                        }
                        reportBillingError(
                            mapResponseCode(billingResult.responseCode),
                            Exception(billingResult.debugMessage)
                        )
                        callback?.onInitError(IKBillingError(mapResponseCode(billingResult.responseCode)))
                    }
                }

                override fun onBillingServiceDisconnected() {

                    //retrying connection to GooglePlay
                    if (!isConnected) {
                        retryBillingClientConnection(callback)
                    }
                }
            })
        } else {
            callback?.onInitialized()
        }
    }

    /**
     * Retries the billing client connection with exponential backoff
     * Max out at the time specified by RECONNECT_TIMER_MAX_TIME_MILLISECONDS (15 minutes)
     */
    private fun retryBillingClientConnection(callback: IKBillingInitialListener?) {
        uiScope.launchWithSupervisorJob {
            delay(reconnectMilliseconds)
            initialize(callback)
            reconnectMilliseconds =
                (reconnectMilliseconds * 2).coerceAtMost(RECONNECT_TIMER_MAX_TIME_MILLISECONDS)
        }
    }

    /**
     * Check for billingClient is initialized and connected, if true then its ready for use.
     * @return true or false
     */
    val isConnected: Boolean
        get() = this.isInitialized && billingService?.isReady ?: false

    /**
     * This method should be called when you are done with BillingProcessor.
     * BillingClient object holds a binding to the in-app billing service and the manager to handle
     * broadcast events, which will leak unless you dispose it correctly.
     */
    fun release() {
        if (isConnected) {
            billingService?.endConnection()
        }
        mPurchasesUpdatedListener = null
    }

    val isInitialized: Boolean
        get() = billingService != null

    suspend fun isPurchased(productId: String?): Boolean {
        return cachedProducts.includesProduct(productId)
    }

    suspend fun isSubscribed(productId: String?): Boolean {
        return cachedSubscriptions.includesProduct(productId)
    }

    fun listOwnedProducts(): List<String> {
        return cachedProducts.contents
    }

    fun listOwnedSubscriptions(): List<String> {
        return cachedSubscriptions.contents
    }

    private fun loadPurchasesByTypeAsync(
        type: String, cacheStorage: BillingCache,
        listener: IPurchasesResponseListener
    ) {
        if (!isConnected) {
            reportPurchasesError(IKSdkBillingErrorCode.BILLING_SERVICE_UNAVAILABLE, listener)
            retryBillingClientConnection(null)
            return
        }
        billingService?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(type)
                .build()
        ) { billingResult, list ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                cacheStorage.clear()
                if (list.isEmpty()) {
                    reportPurchasesError(
                        IKSdkBillingErrorCode.BILLING_NO_HAVE_READY_PURCHASES_PRODUCT,
                        listener
                    )
                    return@queryPurchasesAsync
                }
                for (purchaseItem in list) {
                    val jsonData = purchaseItem.originalJson
                    if (!TextUtils.isEmpty(jsonData)) {
                        try {
                            val purchase = JSONObject(jsonData)
                            val purchaseDto = PurchaseInfo(
                                jsonData,
                                purchaseItem.signature,
                                type
                            )
                            cacheStorage.put(
                                purchase.getString(IkSdkBillingConst.RESPONSE_PRODUCT_ID),
                                purchaseDto
                            )
                            reportPurchasesSuccess(purchaseDto, listener)
                        } catch (e: Exception) {
                            reportBillingError(
                                IKSdkBillingErrorCode.BILLING_ERROR_FAILED_LOAD_PURCHASES, e
                            )
                            reportPurchasesError(
                                IKSdkBillingErrorCode.BILLING_ERROR_FAILED_LOAD_PURCHASES,
                                listener
                            )
                        }
                    } else
                        reportPurchasesSuccess(null, listener)
                }
            } else {
                reportPurchasesError(mapResponseCode(billingResult.responseCode), listener)
            }
        }
    }

    /**
     * Attempt to fetch purchases from the server and update our cache if successful
     *
     * @param listener invokes method onPurchasesError if all retrievals are failure,
     * onPurchasesSuccess if even one retrieval succeeded
     */
    fun loadOwnedPurchasesFromGoogleAsync(listener: IPurchasesResponseListener?) {
        val successListener: IPurchasesResponseListener = object : IPurchasesResponseListener {
            override fun onPurchasesSuccess(purchaseInfo: PurchaseInfo?) {
                reportPurchasesSuccess(purchaseInfo, listener)
            }

            override fun onPurchasesError(error: IKSdkBillingErrorCode) {
                reportPurchasesError(error, listener)
            }
        }
        val errorListener: IPurchasesResponseListener = object : IPurchasesResponseListener {
            override fun onPurchasesSuccess(purchaseInfo: PurchaseInfo?) {
                reportPurchasesSuccess(purchaseInfo, listener)
            }

            override fun onPurchasesError(error: IKSdkBillingErrorCode) {
                reportPurchasesError(error, listener)
            }
        }
        loadPurchasesByTypeAsync(
            ProductType.INAPP,
            cachedProducts,
            object : IPurchasesResponseListener {
                override fun onPurchasesSuccess(purchaseInfo: PurchaseInfo?) {
                    loadPurchasesByTypeAsync(
                        ProductType.SUBS,
                        cachedSubscriptions,
                        successListener
                    )
                }

                override fun onPurchasesError(error: IKSdkBillingErrorCode) {
                    loadPurchasesByTypeAsync(
                        ProductType.SUBS,
                        cachedSubscriptions,
                        errorListener
                    )
                }
            })
    }

    /***
     * Purchase a product
     *
     * @param activity the activity calling this method
     * @param productId the product id to purchase
     * @return `false` if the billing system is not initialized, `productId` is empty
     * or if an exception occurs. Will return `true` otherwise.
     */
    suspend fun purchase(
        activity: Activity,
        productId: String,
        listener: IKSdkBillingPurchaseListener?
    ): Boolean {
        return purchase(activity, productId, ProductType.INAPP, listener)
    }

    /***
     * Subscribe for a product
     *
     * @param activity the activity calling this method
     * @param productId the product id to subscribe
     * @return `false` if the billing system is not initialized, `productId` is empty
     * or if an exception occurs. Will return `true` otherwise.
     */
    suspend fun subscribe(
        activity: Activity,
        productId: String,
        listener: IKSdkBillingPurchaseListener?
    ): Boolean {
        return purchase(activity, productId, ProductType.SUBS, listener)
    }

    // Avoid calling the service again if this value is true
    private val isSubscriptionUpdateSupported: Boolean
        get() {
            // Avoid calling the service again if this value is true
            if (isSubsUpdateSupported) {
                return true
            }
            if (!isConnected) {
                return false
            }
            val result =
                billingService?.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS_UPDATE)
            isSubsUpdateSupported = result?.responseCode == BillingClient.BillingResponseCode.OK
            return isSubsUpdateSupported
        }

    /**
     * Change subscription i.e. upgrade or downgrade
     *
     * @param activity         the activity calling this method
     * @param oldProductId     passing null or empty string will act the same as [.subscribe]
     * @param productId        the new subscription id
     * @return `false` if `oldProductId` is not `null` AND change subscription
     * is not supported.
     */
    suspend fun updateSubscription(
        activity: Activity,
        oldProductId: String,
        productId: String,
        listener: IKSdkBillingPurchaseListener?
    ): Boolean {
        mLastProductId = productId
        return if (oldProductId.isBlank() && !isSubscriptionUpdateSupported) {
            listener?.onBillingFail(productId, IKSdkBillingErrorCode.BILLING_UPGRADE_NOT_VALID)
            false
        } else updatePurchase(
            activity,
            productId,
            oldProductId,
            ProductType.SUBS,
            BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE,
            listener
        )
    }

    /**
     * Change subscription i.e. upgrade or downgrade
     *
     * @param activity         the activity calling this method
     * @param oldProductId     passing null or empty string will act the same as [.subscribe]
     * @param productId        the new subscription id
     * @return `false` if `oldProductId` is not `null` AND change subscription
     * is not supported.
     */
    suspend fun updateSubscription(
        activity: Activity,
        oldProductId: String,
        productId: String,
        subscriptionReplacementMode: Int = IkSdkBillingConst.ReplacementMode.CHARGE_FULL_PRICE,
        listener: IKSdkBillingPurchaseListener?
    ): Boolean {
        mLastProductId = productId
        return if (oldProductId.isBlank() && !isSubscriptionUpdateSupported) {
            listener?.onBillingFail(productId, IKSdkBillingErrorCode.BILLING_UPGRADE_NOT_VALID)
            false
        } else updatePurchase(
            activity,
            productId,
            oldProductId,
            ProductType.SUBS,
            subscriptionReplacementMode,
            listener
        )
    }

    private suspend fun purchase(
        activity: Activity,
        productId: String,
        purchaseType: String,
        listener: IKSdkBillingPurchaseListener?
    ): Boolean {
        return withContext(Dispatchers.Default) {
            mLastProductId = productId
            if (!isConnected || TextUtils.isEmpty(productId) || TextUtils.isEmpty(purchaseType)) {
                if (!isConnected) {
                    retryBillingClientConnection(null)
                }
                listener?.onBillingFail(productId, IKSdkBillingErrorCode.BILLING_PRODUCT_NOT_FOUND)
                return@withContext false
            }

            try {
                kotlin.runCatching {
                    listener?.let {
                        sdkBillingListener.put(productId, it)
                    }
                }
                var purchasePayload = "$purchaseType:$productId"
                if (purchaseType != ProductType.SUBS) {
                    purchasePayload += ":" + UUID.randomUUID().toString()
                }
                savePurchasePayload(purchasePayload)

                val productList =
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(purchaseType)
                            .build()
                    )

                val params = QueryProductDetailsParams.newBuilder().setProductList(productList)

                billingService?.queryProductDetailsAsync(params.build()) { _,
                                                                           productDetailsList ->
                    if (productDetailsList.isNotEmpty()) {
                        startPurchaseFlow(activity, purchaseType, productDetailsList, listener)
                    } else {
                        uiScope.launchWithSupervisorJob {
                            kotlin.runCatching {
                                sdkBillingListener[productId]?.onBillingFail(
                                    productId,
                                    IKSdkBillingErrorCode.BILLING_ERROR_INITIALIZE_PURCHASE
                                )
                                sdkBillingListener.remove(productId)
                            }
                        }
                        // This will occur if product id does not match with the product type
                        reportBillingError(
                            IKSdkBillingErrorCode.BILLING_ERROR_INITIALIZE_PURCHASE,
                            null
                        )
                    }
                }

                return@withContext true
            } catch (e: Exception) {
                reportBillingError(IKSdkBillingErrorCode.BILLING_ERROR_OTHER_ERROR, e)
            }
            return@withContext false
        }
    }

    private fun startPurchaseFlow(
        activity: Activity,
        purchaseType: String,
        productDetails: List<ProductDetails>,
        listener: IKSdkBillingPurchaseListener?
    ) {
        uiScope.launchWithSupervisorJob {
            val productDetail = productDetails.first()
            val productId = productDetail.productId
            val offerToken = productDetail.subscriptionOfferDetails?.getOrNull(0)?.offerToken
                ?: IKSdkDefConst.EMPTY

            val productDetailsParamsList =
                if (purchaseType == ProductType.SUBS && offerToken.isNotEmpty())
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetail)
                            .setOfferToken(offerToken)
                            .build()
                    ) else listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetail)
                        .build()
                )
            val billingFlowParams =
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()
            val responseCode =
                billingService?.launchBillingFlow(activity, billingFlowParams)?.responseCode
            if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                handleItemAlreadyOwned(productId, listener)
            } else {
                //listener?.onBillingFail(productId)
            }
        }

    }

    private suspend fun handleItemAlreadyOwned(
        productId: String,
        listener: IKSdkBillingPurchaseListener?
    ) {
        coroutineScope {
            if (!isPurchased(productId) && !isSubscribed(productId)) {
                loadOwnedPurchasesFromGoogleAsync(object : IPurchasesResponseListener {
                    override fun onPurchasesSuccess(purchaseInfo: PurchaseInfo?) {
                        launchWithSupervisorJob {
                            handleOwnedPurchaseTransaction(productId, listener)
                        }
                    }

                    override fun onPurchasesError(error: IKSdkBillingErrorCode) {
                        launchWithSupervisorJob {
                            handleOwnedPurchaseTransaction(productId, listener)
                        }
                    }
                })
            } else {
                handleOwnedPurchaseTransaction(productId, listener)
            }
        }
    }

    private suspend fun handleOwnedPurchaseTransaction(
        productId: String,
        listener: IKSdkBillingPurchaseListener?
    ) {
        var details = getPurchaseInfo(productId)
        if (!checkMerchant(details)) {
            reportBillingError(
                IKSdkBillingErrorCode.BILLING_ERROR_INVALID_MERCHANT_ID,
                null
            )
            kotlin.runCatching {
                sdkBillingListener[productId]?.onBillingFail(
                    productId,
                    IKSdkBillingErrorCode.BILLING_ERROR_INVALID_MERCHANT_ID
                )
                sdkBillingListener.remove(productId)
            }
        } else
            if (billingListeneraaaa != null) {
                if (details == null) {
                    details = getSubscriptionPurchaseInfo(productId)
                }
                reportProductPurchased(productId, details, listener)
            } else {
                kotlin.runCatching {
                    sdkBillingListener[productId]?.onBillingFail(
                        productId,
                        IKSdkBillingErrorCode.BILLING_PRODUCT_PURCHASE_TRANSACTION_ERROR
                    )
                    sdkBillingListener.remove(productId)
                }
            }
    }

    /**
     * Checks merchant's id validity. If purchase was generated by Freedom alike program it doesn't know
     * real merchant id, unless publisher GoogleId was hacked
     * If merchantId was not supplied function checks nothing
     *
     * @param details PurchaseInfo
     * @return boolean
     */
    private fun checkMerchant(details: PurchaseInfo?): Boolean {
        if (developerMerchantId == null) //omit merchant id checking
        {
            return true
        }
        if (details?.purchaseData?.purchaseTime?.before(dateMerchantLimit1) == true) //newest format applied
        {
            return true
        }
        if (details?.purchaseData?.purchaseTime?.after(dateMerchantLimit2) == true) //newest format applied
        {
            return true
        }
        if (details?.purchaseData?.orderId == null ||
            details.purchaseData?.orderId?.trim { it <= ' ' }.isNullOrBlank()
        ) {
            return false
        }
        val index = details.purchaseData?.orderId?.indexOf('.') ?: 0
        if (index <= 0) {
            return false //protect on missing merchant id
        }
        //extract merchant id
        val merchantId = details.purchaseData?.orderId?.substring(0, index) ?: IKSdkDefConst.EMPTY
        return merchantId.compareTo(developerMerchantId) == 0
    }

    private suspend fun getPurchaseInfo(productId: String?, cache: BillingCache): PurchaseInfo? {
        val details = cache.getDetails(productId)
        return if (details != null && !TextUtils.isEmpty(details.responseData)) {
            details
        } else null
    }

    suspend fun consumePurchaseAsync(productId: String, listener: IPurchasesResponseListener?) {
        if (!isConnected) {
            reportPurchasesError(IKSdkBillingErrorCode.BILLING_SERVICE_UNAVAILABLE, listener)
        }
        try {
            val purchaseInfo = getPurchaseInfo(productId, cachedProducts)
            if (purchaseInfo != null && !TextUtils.isEmpty(purchaseInfo.purchaseData?.purchaseToken)) {
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(
                        purchaseInfo.purchaseData?.purchaseToken ?: IKSdkDefConst.EMPTY
                    )
                    .build()
                billingService?.consumeAsync(consumeParams) { billingResult, _ ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        cachedProducts.remove(productId)
                        reportPurchasesSuccess(purchaseInfo, listener)
                    } else {
                        reportBillingError(
                            IKSdkBillingErrorCode.BILLING_ERROR_CONSUME_FAILED,
                            Exception(billingResult.debugMessage)
                        )
                        reportPurchasesError(
                            IKSdkBillingErrorCode.BILLING_ERROR_CONSUME_FAILED,
                            listener
                        )
                    }
                }
            } else {
                reportPurchasesError(IKSdkBillingErrorCode.BILLING_PRODUCT_NOT_VALID, listener)
            }
        } catch (e: Exception) {
            reportBillingError(
                IKSdkBillingErrorCode.BILLING_ERROR_CONSUME_FAILED,
                e
            )
            reportPurchasesError(IKSdkBillingErrorCode.BILLING_ERROR_CONSUME_FAILED, listener)
        }
    }

    private suspend fun getSkuDetailsAsync(
        productId: String, purchaseType: String,
        listener: IProductDetailsResponseListener?
    ) {
        withContext(Dispatchers.IO) {
            val cachedData =
                if (purchaseType == ProductType.INAPP)
                    cachedProducts
                else
                    cachedSubscriptions
            val cachedItem = kotlin.runCatching {
                cachedData.productDetailsListCache[productId]
            }.getOrNull()
            if (cachedItem != null && cachedItem.first.verifyCache()) {
                val findValid = cachedItem.second?.find { it.priceValue > 0 }
                if (findValid != null) {
                    reportSkuDetailsResponseCaller(cachedItem.second, listener)
                    return@withContext
                }
            }

            getProductDetailsAsync(
                productId,
                purchaseType,
                object : IProductDetailsResponseListener {
                    override fun onProductDetailsResponse(products: List<SdkProductDetails>?) {
                        if (products != null) {
                            kotlin.runCatching {
                                cachedData.productDetailsListCache[productId] =
                                    System.currentTimeMillis() to products
                            }
                            listener?.let { reportSkuDetailsResponseCaller(products, it) }
                        }
                    }

                    override fun onProductDetailsError(error: IKBillingError) {
                        reportSkuDetailsErrorCaller(error, listener)
                    }
                })
        }
    }

    private fun getProductDetailsAsync(
        productId: String, purchaseType: String,
        listener: IProductDetailsResponseListener
    ) {
        if (billingService == null || billingService?.isReady != true) {
            reportSkuDetailsErrorCaller(
                IKBillingError(IKSdkBillingErrorCode.BILLING_ERROR_INITIALIZE_PURCHASE),
                listener
            )
            return
        }
        if (productId.isBlank()) {
            reportSkuDetailsErrorCaller(
                IKBillingError(IKSdkBillingErrorCode.BILLING_PRODUCT_NOT_FOUND),
                listener
            )
            return
        }
        try {
            val productList =
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(purchaseType)
                        .build()
                )

            val params = QueryProductDetailsParams.newBuilder().setProductList(productList)

            billingService?.queryProductDetailsAsync(params.build()) { billingResult,
                                                                       productDetailsList ->
                val response = billingResult.responseCode
                if (response == BillingClient.BillingResponseCode.OK) {
                    val result: ArrayList<SdkProductDetails> = arrayListOf()
                    productDetailsList.forEach {
                        result.add(SdkProductDetails(it))
                    }
                    reportSkuDetailsResponseCaller(result, listener)
                } else {
                    reportBillingError(mapResponseCode(response), null)
                    val errorMessage = String.format(
                        Locale.US,
                        "Failed to retrieve info for %d products, %d",
                        productDetailsList.size, response
                    )
                    reportSkuDetailsErrorCaller(
                        IKBillingError(
                            IKSdkBillingErrorCode.BILLING_PURCHASE_FAIL.code,
                            errorMessage
                        ), listener
                    )
                }
            }

        } catch (e: Exception) {
            reportBillingError(
                IKSdkBillingErrorCode.BILLING_ERROR_SKUDETAILS_FAILED,
                e
            )
            reportSkuDetailsErrorCaller(
                IKBillingError(
                    IKSdkBillingErrorCode.BILLING_ERROR_SKUDETAILS_FAILED
                ), listener
            )
        }
    }

    suspend fun getPurchaseListingDetailsAsync(
        productId: String,
        listener: IProductDetailsResponseListener
    ) {
        getSkuDetailsAsync(productId, ProductType.INAPP, listener)
    }

    suspend fun getSubscriptionListingDetailsAsync(
        productId: String,
        listener: IProductDetailsResponseListener?
    ) {
        getSkuDetailsAsync(productId, ProductType.SUBS, listener)
    }

    suspend fun getPurchaseInfo(productId: String?): PurchaseInfo? {
        return getPurchaseInfo(productId, cachedProducts)
    }

    suspend fun getSubscriptionPurchaseInfo(productId: String?): PurchaseInfo? {
        return getPurchaseInfo(productId, cachedSubscriptions)
    }

    private suspend fun detectPurchaseTypeFromPurchaseResponseData(purchase: JSONObject?): String {
        return withContext(Dispatchers.Default) {
            val purchasePayload = purchasePayload()
            // regular flow, based on developer payload
            if (!TextUtils.isEmpty(purchasePayload) && purchasePayload.startsWith(ProductType.SUBS)) {
                return@withContext ProductType.SUBS
            }
            // backup check for the promo codes (no payload available)
            return@withContext if (purchase != null && purchase.has(IkSdkBillingConst.RESPONSE_AUTO_RENEWING)) {
                ProductType.SUBS
            } else ProductType.INAPP
        }
    }

    private fun verifyAndCachePurchase(purchase: Purchase) {
        uiScope.launchWithSupervisorJob(Dispatchers.Default) {
            val purchaseData = purchase.originalJson
            val dataSignature = purchase.signature
            try {
                val purchaseJsonObject = JSONObject(purchaseData)
                val productId =
                    purchaseJsonObject.getString(IkSdkBillingConst.RESPONSE_PRODUCT_ID)
                if (verifyPurchaseSignature(productId, purchaseData, dataSignature)) {
                    val purchaseType =
                        detectPurchaseTypeFromPurchaseResponseData(purchaseJsonObject)
                    val cache =
                        if (purchaseType == ProductType.SUBS) cachedSubscriptions else cachedProducts
                    val purchaseInfo =
                        PurchaseInfo(
                            purchaseData,
                            dataSignature,
                            purchaseType
                        )
                    cache.put(productId, purchaseInfo)
                    if (billingListeneraaaa != null) {
                        reportProductPurchased(productId, purchaseInfo, null)
                    }
                } else {
                    reportBillingError(
                        IKSdkBillingErrorCode.BILLING_ERROR_INVALID_SIGNATURE,
                        null
                    )
                }
            } catch (e: Exception) {
                reportBillingError(IKSdkBillingErrorCode.BILLING_ERROR_OTHER_ERROR, e)
            }
            savePurchasePayload(null)
        }
    }

    private fun verifyPurchaseSignature(
        productId: String,
        purchaseData: String,
        dataSignature: String
    ): Boolean {
        return try {
            /*
             * Skip the signature check if the provided License Key is NULL and return true in order to
             * continue the purchase flow
             */
            TextUtils.isEmpty(signatureBase64) ||
                    IkBillingSecurity.verifyPurchase(
                        productId,
                        signatureBase64,
                        purchaseData,
                        dataSignature
                    )
        } catch (e: Exception) {
            false
        }
    }

    fun isValidPurchaseInfo(purchaseInfo: PurchaseInfo): Boolean {
        return verifyPurchaseSignature(
            purchaseInfo.purchaseData?.productId ?: IKSdkDefConst.EMPTY,
            purchaseInfo.responseData ?: IKSdkDefConst.EMPTY,
            purchaseInfo.signature ?: IKSdkDefConst.EMPTY
        ) &&
                checkMerchant(purchaseInfo)
    }


    private suspend fun isPurchaseHistoryRestored(): Boolean =
        IKSdkDataStoreBilling.getBoolean(purchaseHistoryRestoredKey, false)

    private suspend fun setPurchaseHistoryRestored() {
        IKSdkDataStoreBilling.putBoolean(purchaseHistoryRestoredKey, true)
    }

    private suspend fun savePurchasePayload(value: String?) {
        IKSdkDataStoreBilling.putString(purPurchasePayloadKey, value ?: IKSdkDefConst.EMPTY)
    }

    private suspend fun purchasePayload(): String =
        IKSdkDataStoreBilling.getString(purPurchasePayloadKey, "")

    private fun reportBillingError(error: IKSdkBillingErrorCode, thr: Throwable?) {
        if (billingListeneraaaa != null) {
            billingListeneraaaa?.onBillingError(error, thr)
        }
        kotlin.runCatching {
            sdkBillingListener[mLastProductId]?.onBillingFail(
                mLastProductId,
                error
            )
            sdkBillingListener.remove(mLastProductId)
            mLastProductId = ""
        }
    }

    private fun reportPurchasesSuccess(
        purchaseInfo: PurchaseInfo?,
        listener: IPurchasesResponseListener?
    ) {
        if (listener != null && billingListeneraaaa != null) {
            uiScope.launchWithSupervisorJob {
                listener.onPurchasesSuccess(purchaseInfo)
            }
        }
        kotlin.runCatching {
            sdkBillingListener[mLastProductId]?.onBillingSuccess(
                purchaseInfo,
                mLastProductId
            )
            sdkBillingListener.remove(mLastProductId)
            mLastProductId = ""
        }
    }

    private fun reportPurchasesError(
        error: IKSdkBillingErrorCode,
        listener: IPurchasesResponseListener?
    ) {
        if (listener != null && billingListeneraaaa != null) {
            uiScope.launchWithSupervisorJob {
                listener.onPurchasesError(error)
            }
        }
    }

    private fun reportSkuDetailsErrorCaller(
        error: IKBillingError,
        listener: IProductDetailsResponseListener?
    ) {
        listener?.onProductDetailsError(error)
    }

    private fun reportSkuDetailsResponseCaller(
        products: List<SdkProductDetails>?,
        listener: IProductDetailsResponseListener?
    ) {
        listener?.onProductDetailsResponse(products)
    }

    private fun reportProductPurchased(
        productId: String,
        details: PurchaseInfo?,
        listener: IKSdkBillingPurchaseListener?
    ) {
        uiScope.launchWithSupervisorJob(Dispatchers.Default) {
            if (billingListeneraaaa != null) {
                if (IkBillingSecurity.provider?.listProductIDsCanPurchaseMultiTime()?.contains(productId) != true
                ) {
                    billingListeneraaaa?.onProductPurchased(
                        productId,
                        details
                    )
                    listener?.onBillingSuccess(details, productId)
                    kotlin.runCatching {
                        sdkBillingListener[productId]?.onBillingSuccess(details, productId)
                        sdkBillingListener.remove(productId)
                    }
                    return@launchWithSupervisorJob
                }
                consumePurchaseAsync(productId, object : IPurchasesResponseListener {
                    override fun onPurchasesSuccess(purchaseInfo: PurchaseInfo?) {
                        billingListeneraaaa?.onProductPurchased(
                            productId,
                            details
                        )
                        listener?.onBillingSuccess(purchaseInfo, productId)
                        kotlin.runCatching {
                            sdkBillingListener[productId]?.onBillingSuccess(purchaseInfo, productId)
                            sdkBillingListener.remove(productId)
                        }
                    }

                    override fun onPurchasesError(error: IKSdkBillingErrorCode) {
                        billingListeneraaaa?.onProductPurchased(
                            productId,
                            details
                        )
                        listener?.onBillingFail(productId, error)
                        kotlin.runCatching {
                            sdkBillingListener[productId]?.onBillingSuccess(details, productId)
                            sdkBillingListener.remove(productId)
                        }
                    }
                })
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (purchase.isAcknowledged) {
                verifyAndCachePurchase(purchase)
            } else {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingService?.acknowledgePurchase(
                    acknowledgePurchaseParams
                ) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        verifyAndCachePurchase(purchase)
                    } else {
                        reportBillingError(
                            IKSdkBillingErrorCode.BILLING_ERROR_FAILED_TO_ACKNOWLEDGE_PURCHASE,
                            null
                        )
                    }
                }
            }
        }

    }

    private suspend fun updatePurchase(
        activity: Activity,
        productId: String,
        oldProductId: String,
        purchaseType: String,
        subscriptionReplacementMode: Int = BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE,
        listener: IKSdkBillingPurchaseListener?
    ): Boolean {
        return withContext(Dispatchers.Default) {
            if (!isConnected || TextUtils.isEmpty(productId) || TextUtils.isEmpty(purchaseType)) {
                if (!isConnected) {
                    retryBillingClientConnection(null)
                }
                listener?.onBillingFail(productId, IKSdkBillingErrorCode.BILLING_PRODUCT_NOT_FOUND)
                return@withContext false
            }

            try {
                kotlin.runCatching {
                    listener?.let {
                        sdkBillingListener.put(productId, it)
                    }
                }
                var purchasePayload = "$purchaseType:$productId"
                if (purchaseType != ProductType.SUBS) {
                    purchasePayload += ":" + UUID.randomUUID().toString()
                }
                savePurchasePayload(purchasePayload)

                val productList =
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(purchaseType)
                            .build()
                    )

                val params = QueryProductDetailsParams.newBuilder().setProductList(productList)

                billingService?.queryProductDetailsAsync(params.build()) { _,
                                                                           productDetailsList ->
                    if (productDetailsList.isNotEmpty()) {
                        startPurchaseFlow(
                            activity,
                            purchaseType,
                            productDetailsList,
                            oldProductId,
                            subscriptionReplacementMode,
                            listener
                        )
                    } else {
                        uiScope.launchWithSupervisorJob {
                            kotlin.runCatching {
                                sdkBillingListener[productId]?.onBillingFail(
                                    productId,
                                    IKSdkBillingErrorCode.BILLING_ERROR_INITIALIZE_PURCHASE
                                )
                                sdkBillingListener.remove(productId)
                            }
                        }
                        // This will occur if product id does not match with the product type
                        reportBillingError(
                            IKSdkBillingErrorCode.BILLING_ERROR_INITIALIZE_PURCHASE,
                            null
                        )
                    }
                }

                return@withContext true
            } catch (e: Exception) {
                reportBillingError(IKSdkBillingErrorCode.BILLING_ERROR_OTHER_ERROR, e)
            }
            return@withContext false
        }
    }

    private fun startPurchaseFlow(
        activity: Activity,
        purchaseType: String,
        productDetails: List<ProductDetails>,
        oldProductId: String,
        subscriptionReplacementMode: Int = BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE,
        listener: IKSdkBillingPurchaseListener?
    ) {

        uiScope.launchWithSupervisorJob {
            val productDetail = productDetails.first()
            val oldProductDetails = getSubscriptionPurchaseInfo(oldProductId)
            val productId = productDetail.productId

            val offerToken = productDetail.subscriptionOfferDetails?.getOrNull(0)?.offerToken
                ?: IKSdkDefConst.EMPTY

            val productDetailsParamsList =
                if (purchaseType == ProductType.SUBS && offerToken.isNotEmpty())
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetail)
                            .setOfferToken(offerToken)
                            .build()
                    ) else listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetail)
                        .build()
                )
            val billingFlowParamsBuilder = BillingFlowParams.newBuilder()


            if (oldProductDetails != null && oldProductDetails.purchaseData?.purchaseToken != null) {
                billingFlowParamsBuilder.setSubscriptionUpdateParams(
                    BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                        .setOldPurchaseToken(
                            oldProductDetails.purchaseData?.purchaseToken ?: IKSdkDefConst.EMPTY
                        )
                        .setSubscriptionReplacementMode(
                            subscriptionReplacementMode
                        ).build()
                )
            }
            val billingFlowParams =
                billingFlowParamsBuilder.setProductDetailsParamsList(productDetailsParamsList)
                    .build()
            val responseCode =
                billingService?.launchBillingFlow(activity, billingFlowParams)?.responseCode
            if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                handleItemAlreadyOwned(productId, listener)
            } else {
                //listener?.onBillingFail(productId)
            }
        }
    }

    fun isFeatureSupported(summary: String): BillingResult? {
        return billingService?.isFeatureSupported(summary)
    }

    companion object {

        private const val LOG_TAG = "BillingProcessor"
        private const val SETTINGS_VERSION = ".v2_6"
        private const val RESTORE_KEY = ".products.restored$SETTINGS_VERSION"
        private const val MANAGED_PRODUCTS_CACHE_KEY = ".products.cache$SETTINGS_VERSION"
        private const val SUBSCRIPTIONS_CACHE_KEY = ".subscriptions.cache$SETTINGS_VERSION"
        private const val PURCHASE_PAYLOAD_CACHE_KEY = ".purchase.last$SETTINGS_VERSION"
        private const val RECONNECT_TIMER_START_MILLISECONDS = 2000L
        private const val RECONNECT_TIMER_MAX_TIME_MILLISECONDS = 1000L * 60L * 15L

        /**
         * Returns a new [BillingProcessor], without immediately binding to Play Services. If you use
         * this factory, then you must call [.initialize] afterwards.
         * @param context Context object
         * @param licenseKey Licence key from Play Console
         * @param handler callback instance
         * @return BillingProcessor instance
         */
        fun newBillingProcessor(
            context: Context,
            licenseKey: String?,
            handler: IKSdkBillingHandlerListener?
        ): BillingProcessor {
            return newBillingProcessor(context, licenseKey, null, handler)
        }

        /**
         * Returns a new [BillingProcessor], without immediately binding to Play Services. If you use
         * this factory, then you must call [.initialize] afterwards.
         * @param context Context object
         * @param licenseKey Licence key from Play Console
         * @param merchantId Google merchant ID
         * @param handler callback instance
         * @return BillingProcessor instance
         */
        private fun newBillingProcessor(
            context: Context, licenseKey: String?, merchantId: String?,
            handler: IKSdkBillingHandlerListener?
        ): BillingProcessor {
            return BillingProcessor(context, licenseKey, merchantId, handler, false)
        }

        private val bindServiceIntent: Intent
            get() {
                val intent = Intent("com.android.vending.billing.InAppBillingService.BIND")
                intent.setPackage("com.android.vending")
                return intent
            }

        fun isIabServiceAvailable(context: Context): Boolean {
            val packageManager = context.packageManager
            val list = try {
                packageManager.queryIntentServices(
                    bindServiceIntent, 0
                )
            } catch (e: Exception) {
                listOf()
            }
            return list.isNotEmpty()
        }
    }

    fun mapResponseCode(code: Int): IKSdkBillingErrorCode {
        return when (code) {
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> IKSdkBillingErrorCode.BILLING_SERVICE_TIMEOUT
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> IKSdkBillingErrorCode.BILLING_FEATURE_NOT_SUPPORTED
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> IKSdkBillingErrorCode.BILLING_SERVICE_DISCONNECTED
            BillingClient.BillingResponseCode.OK -> IKSdkBillingErrorCode.BILLING_OK
            BillingClient.BillingResponseCode.USER_CANCELED -> IKSdkBillingErrorCode.BILLING_USER_CANCELED
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> IKSdkBillingErrorCode.BILLING_SERVICE_UNAVAILABLE
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> IKSdkBillingErrorCode.BILLING_BILLING_UNAVAILABLE
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> IKSdkBillingErrorCode.BILLING_ITEM_UNAVAILABLE
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> IKSdkBillingErrorCode.BILLING_DEVELOPER_ERROR
            BillingClient.BillingResponseCode.ERROR -> IKSdkBillingErrorCode.BILLING_ERROR
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> IKSdkBillingErrorCode.BILLING_ITEM_ALREADY_OWNED
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> IKSdkBillingErrorCode.BILLING_ITEM_NOT_OWNED
            BillingClient.BillingResponseCode.NETWORK_ERROR -> IKSdkBillingErrorCode.BILLING_NETWORK_ERROR
            else -> IKSdkBillingErrorCode.BILLING_UNKNOWN_ERROR
        }
    }

    fun querySubHistoryAsync(listener: IKOnQueryHistoryListener, type: String) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(type)
        // uses queryPurchaseHistory Kotlin extension function
        billingService?.queryPurchasesAsync(
            params.build()
        ) { billingResult, listProducts ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                listener.onSuccess(listProducts)
            } else {
                listener.onFailure(
                    IKBillingError(
                        billingResult.responseCode,
                        billingResult.debugMessage
                    )
                )
            }
        }
    }

    fun getBillingClient(): BillingClient? = billingService

    fun setPurchasesUpdatedListener(callback: PurchasesUpdatedListener?) {
        mPurchasesUpdatedListener = callback
    }
}