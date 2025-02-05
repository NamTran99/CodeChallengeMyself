package com.example.ads.activity.billing.core

import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustPlayStoreSubscription
import com.android.billingclient.api.BillingClient.ProductType
import com.google.gson.Gson
import com.example.ads.activity.IKSdkConstants
import com.example.ads.activity.billing.dto.PurchaseInfo
import com.example.ads.activity.billing.dto.SdkProductDetails
import com.example.ads.activity.core.IKDataCoreManager
//import com.example.ads.activity.core.firebase.IKRemoteDataManager
import com.example.ads.activity.data.db.IKDataRepository
import com.example.ads.activity.data.dto.pub.IKBillingError
import com.example.ads.activity.data.dto.pub.IKProductType
import com.example.ads.activity.data.dto.pub.SdkIapPackageDto
import com.example.ads.activity.listener.keep.SDKIAPProductIDProvider
import com.example.ads.activity.listener.pub.IKBillingDetailListener
import com.example.ads.activity.listener.pub.IKBillingInitialListener
import com.example.ads.activity.listener.pub.IKBillingListener
import com.example.ads.activity.listener.sdk.IKSdkBillingHandlerListener
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKLogs
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import com.example.ads.activity.utils.IKSdkUtilsCore
import com.example.ads.activity.utils.IKTrackingConst
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

abstract class IKBillingBase : IKBillingInterfaceCore {
    var mListener: IKSdkBillingHandlerListener? = null
        protected set

    protected var mBillingProcess: BillingProcessor? = null

    private val mBillingJob = SupervisorJob()
    protected val mBillingUiScope = CoroutineScope(Dispatchers.Main + mBillingJob)

    protected var mFirstIapStatusListener: IKBillingListener? = null
    protected var mBillingInitialListener: IKBillingInitialListener? = null
    protected var mIsInitializing = false

    protected val mRepository: IKDataRepository? by lazy {
        IKDataRepository.getInstance()
    }

    protected fun showLogD(tag: String, message: () -> String) {
        IKLogs.d("BillingHelper") {
            "${tag}:" + message.invoke()
        }
    }

    private fun splitToken(token: String?): List<String> {
        if (token.isNullOrBlank())
            return arrayListOf()
        return kotlin.runCatching {
            token.chunked(90)
        }.getOrNull() ?: arrayListOf()
    }

    private fun trackingFirebase(
        productId: String,
        details: PurchaseInfo, isPurchase: Boolean
    ) {
        showLogD("trackingIAP") { "start trackingFirebase" }
        kotlin.runCatching {
            val listToken: ArrayList<Pair<String, String>> = arrayListOf()
            val splitToken =
                splitToken(details.purchaseData?.purchaseToken ?: IKSdkDefConst.EMPTY)
            if (splitToken.size <= 1) {
                kotlin.runCatching {
                    listToken.add(
                        Pair(
                            IKTrackingConst.ParamName.PURCHASE_TOKEN,
                            splitToken.first()
                        )
                    )
                }
            } else {
                splitToken.forEachIndexed { index, s ->
                    kotlin.runCatching {
                        listToken.add(
                            Pair(
                                IKTrackingConst.ParamName.PURCHASE_TOKEN + "_$index",
                                s
                            )
                        )
                    }
                }
            }
            IKSdkTrackingHelper.customizeTracking(
                IKTrackingConst.EventName.PURCHASE_SDK_EVENT,
                Pair(IKTrackingConst.ParamName.PRODUCT_ID, productId),
                Pair(
                    IKTrackingConst.ParamName.ORDER_ID,
                    details.purchaseData?.orderId ?: IKSdkDefConst.EMPTY
                ),
                Pair(
                    IKTrackingConst.ParamName.PURCHASE_TIME,
                    (details.purchaseData?.purchaseTime?.time ?: 0L).toString()
                ),
                Pair(
                    IKTrackingConst.ParamName.PRODUCT_TYPE,
                    if (isPurchase) IKSdkDefConst.TXT_PURCHASE else IKSdkDefConst.TXT_SUBSCRIPTION
                ),
                *(listToken.toTypedArray())
            )
            showLogD("trackingIAP") { "done trackingFirebase" }
        }
    }

    protected suspend fun trackingIAP(
        productId: String,
        details: PurchaseInfo?
    ) {
        showLogD("trackingIAP") { "start run $productId" }
        coroutineScope {
            if (details?.purchaseData == null) {
                showLogD("trackingIAP") { "purchaseData empty" }
                return@coroutineScope
            }
            if (details.purchaseData?.orderId?.uppercase()?.startsWith("GPA") != true) {
                showLogD("trackingIAP") { "purchaseData not start with GPA" }
                return@coroutineScope
            }
            var isPurchase = details.productType == ProductType.INAPP
            if (!isPurchase)
                isPurchase =
                    IkBillingSecurity.provider?.listProductIDsPurchase()?.contains(productId)
                        ?: false

            kotlin.runCatching {
                if (!isPurchase)
                    isPurchase = IKSdkUtilsCore.parseSubObject(
                        IKSdkDefConst.CONFIG_OTHER_IN_APP
                    ).contains(productId)
            }

            showLogD("trackingIAP") { "start process isPurchase=$isPurchase" }
            launchWithSupervisorJob(Dispatchers.IO) {
                trackingFirebase(productId, details, isPurchase)
            }
            showLogD("trackingIAP") { "start adjust" }
            kotlin.runCatching {
                if (!isPurchase) {
                    getSubscriptionDetailAsync(
                        productId,
                        object : IKBillingDetailListener<SdkProductDetails> {
                            override fun onSuccess(value: SdkProductDetails?) {
                                showLogD("trackingIAP") { "tracking adjust onSuccess" }
                                val subscription = AdjustPlayStoreSubscription(
                                    value?.priceLong ?: 0L,
                                    (value?.currency
                                        ?: IKSdkDefConst.CURRENCY_CODE_USD).uppercase(),
                                    productId,
                                    details.purchaseData?.orderId
                                        ?: IKSdkDefConst.EMPTY,
                                    details.signature ?: IKSdkDefConst.EMPTY,
                                    details.purchaseData?.purchaseToken
                                        ?: IKSdkDefConst.EMPTY
                                )
                                subscription.purchaseTime =
                                    details.purchaseData?.purchaseTime?.time ?: 0L
                                Adjust.trackPlayStoreSubscription(subscription)
                            }

                            override fun onError(error: IKBillingError) {
                                showLogD("trackingIAP") { "tracking adjust onError:${error.message}" }
                            }
                        })
                } else {
                    getPurchaseDetailAsync(
                        productId,
                        object : IKBillingDetailListener<SdkProductDetails> {
                            override fun onSuccess(value: SdkProductDetails?) {
                                showLogD("trackingIAP") { "tracking adjust on callback" }
                                val valueDouble: Double = (value?.priceLong ?: 0).toDouble()
//                                if (IKSdkConstants.ADJUST_IN_APP_PURCHASE.isNotBlank()) {
//                                    showLogD("trackingIAP") { "tracking adjust onSuccess" }
//                                    IKSdkTrackingHelper.trackingAdjustInAppPurchase(
//                                        IKSdkConstants.ADJUST_IN_APP_PURCHASE,
//                                        productId,
//                                        details.purchaseData?.orderId
//                                            ?: IKSdkDefConst.EMPTY,
//                                        (details.purchaseData?.purchaseTime?.time
//                                            ?: 0L).toString(),
//                                        IKProductType.INAPP.value,
//                                        valueDouble,
//                                        (value?.currency
//                                            ?: IKSdkDefConst.CURRENCY_CODE_USD).uppercase(),
//                                        details.purchaseData?.purchaseToken
//                                            ?: IKSdkDefConst.EMPTY
//                                    )
//                                }
                            }

                            override fun onError(error: IKBillingError) {
                                showLogD("trackingIAP") { "tracking adjust onError:${error.message}" }
                            }
                        })
                }
            }
            showLogD("trackingIAP") { "done $productId" }
        }
    }

    private suspend fun checkIAPSuspend(
        listener: IKBillingListener?,
        delay: Boolean = false
    ) {
        showLogD("checkIAPSuspend") { "start run" }
        var isSubscribedAds = false
        var isPurScribedAds = false
        val listIap: ArrayList<SdkIapPackageDto> = arrayListOf()
        val listConfigPur = ArrayList<String>()
        val listConfigSub = ArrayList<String>()
        withContext(Dispatchers.Default) {
            if (delay) {
//                if (IKRemoteDataManager.getRemoteConfigData().isEmpty())
//                    delay(1000)
//                if (IKRemoteDataManager.getRemoteConfigData().isEmpty())
//                    delay(1000)
//                if (IKRemoteDataManager.getRemoteConfigData().isEmpty())
//                    delay(1000)
            }
            listConfigSub.addAll(
                IkBillingSecurity.provider?.listProductIDsSubscription()
                    ?: listOf()
            )
            kotlin.runCatching {
                listConfigSub.addAll(
                    IKSdkUtilsCore.parseSubObject(
                        IKSdkDefConst.CONFIG_OTHER_SUB
                    )
                )
                kotlin.runCatching {
                    val listConfigSubData =
                        IKDataCoreManager.otherConfig[IKSdkDefConst.Config.OTHER_SUB_ID]?.toString()
                            ?: IKSdkDefConst.EMPTY
                    Gson().fromJson<List<String>>(
                        listConfigSubData,
                        List::class.java
                    )?.also {
                        listConfigSub.addAll(it)
                    }
                }
                val listSub = listConfigSub.distinct().filter {
                    mBillingProcess?.isSubscribed(it) == true
                }
                listSub.forEach {
                    listIap.add(SdkIapPackageDto(it, IKSdkDefConst.TXT_SUBSCRIPTION))
                }
                isSubscribedAds = listSub.isNotEmpty()
            }

            listConfigPur.addAll(
                IkBillingSecurity.provider?.listProductIDsPurchase()
                    ?: arrayListOf()
            )
            kotlin.runCatching {
                listConfigPur.addAll(
                    IKSdkUtilsCore.parseSubObject(
                        IKSdkDefConst.CONFIG_OTHER_IN_APP
                    )
                )
                kotlin.runCatching {
                    val listConfigPurData =
                        IKDataCoreManager.otherConfig[IKSdkDefConst.Config.OTHER_PUR_ID]?.toString()
                            ?: IKSdkDefConst.EMPTY
                    Gson().fromJson<List<String>>(
                        listConfigPurData,
                        List::class.java
                    )?.also {
                        listConfigPur.addAll(it)
                    }
                }
                val listPur = listConfigPur.distinct().filter {
                    mBillingProcess?.isPurchased(it) == true
                }
                listPur.forEach {
                    listIap.add(SdkIapPackageDto(it, IKSdkDefConst.TXT_PURCHASE))
                }
                isPurScribedAds = listPur.isNotEmpty()
            }
        }
        showLogD("checkIAPSuspend") { "listConfigPur=$listConfigPur" }
        showLogD("checkIAPSuspend") { "listConfigSub=$listConfigSub" }
        val checkFailList = checkFailedPurchase(listConfigSub, listConfigPur)
        listIap.addAll(checkFailList)
        val resultList = listIap.distinct()
        IkBillingSecurity.setIapPackage(listIap)

        if (isSubscribedAds || isPurScribedAds || resultList.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                listener?.onBillingSuccess()
            }
            showLogD("checkIAPSuspend") { " onBillingSuccess" }
        } else {
            IkBillingSecurity.clearIapPackage()
            withContext(Dispatchers.Main) {
                listener?.onBillingFail()
            }
            showLogD("checkIAPSuspend") {
                " onBillingFail_ isSubscribedAds=$isSubscribedAds, " +
                        "isPurScribedAds=$isPurScribedAds, resultList=${resultList.isNotEmpty()}"
            }
        }

    }

    private suspend fun checkIAPSuspend(
        purchaseInfo: PurchaseInfo?,
        listener: IKBillingListener?,
        delay: Boolean = false
    ) {
        showLogD("checkIAPSuspend 2") { "start run" }
        var isSubscribedAds = false
        var isPurScribedAds = false
        val listIap: ArrayList<SdkIapPackageDto> = arrayListOf()
        val listConfigPur = ArrayList<String>()
        val listConfigSub = ArrayList<String>()
        withContext(Dispatchers.Default) {
            if (delay) {
//                if (IKRemoteDataManager.getRemoteConfigData().isEmpty())
//                    delay(1000)
//                if (IKRemoteDataManager.getRemoteConfigData().isEmpty())
//                    delay(1000)
//                if (IKRemoteDataManager.getRemoteConfigData().isEmpty())
//                    delay(1000)
            }
            val productId = purchaseInfo?.purchaseData?.productId ?: IKSdkDefConst.EMPTY
            listConfigSub.addAll(
                IkBillingSecurity.provider?.listProductIDsSubscription()
                    ?: listOf()
            )
            kotlin.runCatching {
                listConfigSub.addAll(
                    IKSdkUtilsCore.parseSubObject(
                        IKSdkDefConst.CONFIG_OTHER_SUB
                    )
                )
                kotlin.runCatching {
                    val listConfigSubData =
                        IKDataCoreManager.otherConfig[IKSdkDefConst.Config.OTHER_SUB_ID]?.toString()
                            ?: IKSdkDefConst.EMPTY
                    Gson().fromJson<List<String>>(
                        listConfigSubData,
                        List::class.java
                    )?.also {
                        listConfigSub.addAll(it)
                    }
                }
                val listSub: ArrayList<String> = arrayListOf()
                listConfigSub.distinct().filter {
                    mBillingProcess?.isSubscribed(it) == true
                }.let {
                    listSub.addAll(it)
                }
                listSub.forEach {
                    listIap.add(SdkIapPackageDto(it, IKSdkDefConst.TXT_SUBSCRIPTION))
                }
//                if (purchaseInfo?.productType == ProductType.SUBS && purchaseInfo.purchaseData?.orderId?.uppercase()
//                        ?.startsWith("GPA") == true && productId.isNotBlank()
//                ) {
//                    showLogD("checkIAPSuspend 2") { "$productId is sub" }
//                    listSub.add(productId)
//                    listIap.add(SdkIapPackageDto(productId, IKSdkDefConst.TXT_SUBSCRIPTION))
//                }
                isSubscribedAds = listSub.isNotEmpty()
            }

            listConfigPur.addAll(
                IkBillingSecurity.provider?.listProductIDsPurchase()
                    ?: arrayListOf()
            )
            kotlin.runCatching {
                listConfigPur.addAll(
                    IKSdkUtilsCore.parseSubObject(
                        IKSdkDefConst.CONFIG_OTHER_IN_APP
                    )
                )
                kotlin.runCatching {
                    val listConfigPurData =
                        IKDataCoreManager.otherConfig[IKSdkDefConst.Config.OTHER_PUR_ID]?.toString()
                            ?: IKSdkDefConst.EMPTY
                    Gson().fromJson<List<String>>(
                        listConfigPurData,
                        List::class.java
                    )?.also {
                        listConfigPur.addAll(it)
                    }
                }
                val listPur = arrayListOf<String>()
                listConfigPur.distinct().filter {
                    mBillingProcess?.isPurchased(it) == true
                }.let {
                    listPur.addAll(it)
                }
                listPur.forEach {
                    listIap.add(SdkIapPackageDto(it, IKSdkDefConst.TXT_PURCHASE))
                }
//                if (purchaseInfo?.productType == ProductType.INAPP && purchaseInfo.purchaseData?.orderId?.uppercase()
//                        ?.startsWith("GPA") == true && productId.isNotBlank()
//                ) {
//                    showLogD("checkIAPSuspend 2") { "$productId is purchase" }
//                    listPur.add(productId)
//                    listIap.add(SdkIapPackageDto(productId, IKSdkDefConst.TXT_PURCHASE))
//                }
                isPurScribedAds = listPur.isNotEmpty()
            }
        }
        showLogD("checkIAPSuspend") { "listConfigPur=$listConfigPur" }
        showLogD("checkIAPSuspend") { "listConfigSub=$listConfigSub" }
        val checkFailList = checkFailedPurchase(listConfigSub, listConfigPur)
        listIap.addAll(checkFailList)
        val resultList = listIap.distinct()
        IkBillingSecurity.setIapPackage(listIap)

        if (isSubscribedAds || isPurScribedAds || resultList.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                listener?.onBillingSuccess()
            }
            showLogD("checkIAPSuspend") { " onBillingSuccess" }
        } else {
            IkBillingSecurity.clearIapPackage()
            withContext(Dispatchers.Main) {
                listener?.onBillingFail()
            }
            showLogD("checkIAPSuspend") {
                " onBillingFail_ isSubscribedAds=$isSubscribedAds, " +
                        "isPurScribedAds=$isPurScribedAds, resultList=${resultList.isNotEmpty()}"
            }
        }

    }

    private suspend fun checkFailedPurchase(
        subIdList: ArrayList<String>,
        purchaseIdList: ArrayList<String>
    ): ArrayList<SdkIapPackageDto> {
        val format = SimpleDateFormat(IKSdkDefConst.FORMAT_DATE_SERVER, Locale.US)
        val listIap: ArrayList<SdkIapPackageDto> = arrayListOf()

        val listCheck = mRepository?.getAllUserBilling() ?: arrayListOf()
        if (listCheck.isEmpty())
            return arrayListOf()
        return withContext(Dispatchers.Default) {
            listCheck.forEach { billingDetail ->
                try {
                    val endDate = format.parse(billingDetail.endDate)
                    endDate?.let {
                        if (!it.after(Date())) { // if date is today or before
                            purchaseIdList.forEach { idString ->
                                if (mBillingProcess?.getPurchaseInfo(idString)
                                        ?.purchaseData?.orderId == billingDetail.orderId
                                ) {
                                    listIap.add(
                                        SdkIapPackageDto(
                                            idString,
                                            IKSdkDefConst.TXT_PURCHASE
                                        )
                                    )
                                }
                            }

                            subIdList.forEach { idString ->
                                if (mBillingProcess?.getSubscriptionPurchaseInfo(idString)
                                        ?.purchaseData?.orderId == billingDetail.orderId
                                ) {
                                    listIap.add(
                                        SdkIapPackageDto(
                                            idString,
                                            IKSdkDefConst.TXT_SUBSCRIPTION
                                        )
                                    )
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }
            listIap
        }
    }

    protected fun whenProductPurchased(
        productId: String
    ) {
        showLogD("whenProductPurchased") { "start run" }
        mBillingUiScope.launchWithSupervisorJob {


            showLogD("whenProductPurchased") { "checkIAPSuspend start run" }
            checkIAPSuspend(object : IKBillingListener {
                override fun onBillingSuccess() {
                    showLogD("whenProductPurchased") { "onBillingSuccess" }
                    mBillingUiScope.launchWithSupervisorJob {
                        kotlin.runCatching {
                            showLogD("whenProductPurchased") { "onBillingSuccess send" }
                            mListener?.onBillingDataSave(true)
                        }
                    }
                }

                override fun onBillingFail() {
                    showLogD("whenProductPurchased") { "onBillingFail" }
                    mBillingUiScope.launchWithSupervisorJob {
                        kotlin.runCatching {
                            showLogD("whenProductPurchased") { "onBillingFail send" }
                            mListener?.onBillingDataSave(false)
                        }
                    }
                }
            })
        }
    }

    internal fun checkIAP(
        listener: IKBillingListener?,
        delay: Boolean = false
    ) {
        showLogD("checkIAP") { "start run" }
        mBillingUiScope.launchWithSupervisorJob {
            checkIAPSuspend(listener, delay)
        }
    }

    internal fun checkIAP(
        purchaseInfo: PurchaseInfo?,
        listener: IKBillingListener?,
        delay: Boolean = false
    ) {
        showLogD("checkIAP") { "start run" }
        mBillingUiScope.launchWithSupervisorJob {
            checkIAPSuspend(purchaseInfo, listener, delay)
        }
    }

    suspend fun getSubscriptionDetailAsync(
        productId: String,
        callback: IKBillingDetailListener<SdkProductDetails>?
    ) {
        mBillingProcess?.getSubscriptionListingDetailsAsync(productId, object :
            BillingProcessor.IProductDetailsResponseListener {
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

    suspend fun getPurchaseDetailAsync(
        productId: String,
        callback: IKBillingDetailListener<SdkProductDetails>?
    ) {
        withContext(Dispatchers.IO) {
            mBillingProcess?.getPurchaseListingDetailsAsync(productId, object :
                BillingProcessor.IProductDetailsResponseListener {
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

    protected fun postResult(action: () -> Unit) {
        mBillingUiScope.launchWithSupervisorJob(Dispatchers.Main) {
            action()
        }
    }

    fun setupBillingProvider(provider: SDKIAPProductIDProvider?) {
        IkBillingSecurity.provider = provider
    }
}