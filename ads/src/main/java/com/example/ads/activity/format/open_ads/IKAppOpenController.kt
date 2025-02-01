//package com.example.ads.activity.format.open_ads
//
//import android.app.Activity
//import android.content.Context
//import com.example.ads.activity.activity.IkmOpenAdActivity
//import com.example.ads.activity.activity.IkmOpenBackUpAdActivity
//import com.example.ads.activity.core.IKDataCoreManager
//import com.example.ads.activity.data.db.IKDataRepository
//import com.example.ads.activity.data.dto.AdNetwork
//import com.example.ads.activity.data.dto.pub.IKAdError
//import com.example.ads.activity.data.dto.pub.IKAdFormat
//import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
//import com.example.ads.activity.data.dto.sdk.SDKAdPriorityDto
//import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
//import com.example.ads.activity.data.dto.sdk.data.IKSdkBaseDto
//import com.example.ads.activity.data.dto.sdk.data.IKSdkDataOpLocalDto
//import com.example.ads.activity.format.base.IKSdkBaseAd
//import com.example.ads.activity.format.base.IKSdkBaseAdController
//import com.example.ads.activity.format.native_ads.NativeBackUpLatest
//import com.example.ads.activity.listener.sdk.IKSdkBaseListener
//import com.example.ads.activity.listener.sdk.IKSdkBaseTrackingListener
//import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
//import com.example.ads.activity.listener.sdk.IKSdkShowAdListener
//import com.example.ads.activity.listener.sdk.IKSdkShowFirstAdListener
//import com.example.ads.activity.mediation.admob.AppOpenAdmob
//import com.example.ads.activity.mediation.applovin.AppOpenMax
//import com.example.ads.activity.mediation.applovin.IKApplovinHelper
//import com.example.ads.activity.mediation.gam.AppOpenGam
//import com.example.ads.activity.mediation.ikad.OpenIkAd
//import com.example.ads.activity.tracking.IKSdkTrackingHelper
//import com.example.ads.activity.utils.IKLogs
//import com.example.ads.activity.utils.IKSdkDefConst
//import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import com.example.ads.activity.utils.IKSdkUtilsCore
//import com.example.ads.activity.utils.IKTrackingConst
//import com.example.ads.activity.utils.IkmSdkCoreFunc
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.async
//import kotlinx.coroutines.awaitAll
//import kotlinx.coroutines.coroutineScope
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.suspendCancellableCoroutine
//import java.lang.ref.WeakReference
//import java.text.SimpleDateFormat
//import java.util.Locale
//import java.util.concurrent.atomic.AtomicInteger
//
//
//object IKAppOpenController : IKSdkBaseAdController() {
//    private const val TAG_LOG = "IKOpenAdC"
//
//    override val admob: AppOpenAdmob by lazy {
//        AppOpenAdmob()
//    }
//
//    override val adMax: AppOpenMax? by lazy {
//        if (IKApplovinHelper.isInitialized())
//            AppOpenMax()
//        else null
//    }
//
//    override val adGam: AppOpenGam by lazy {
//        AppOpenGam()
//    }
//
//    override val adIK: OpenIkAd? by lazy {
//        OpenIkAd()
//    }
//
//    override val adFairBid: IKSdkBaseAd<*>?
//        get() = null
//
//    override suspend fun getBackupAd(): IKAdapterDto? = IKDataCoreManager.getOtherOpenAds()
//    override val adFormat: IKAdFormat = IKAdFormat.OPEN
//    private val mTrackingListener: IKSdkBaseTrackingListener
//        get() = object : IKSdkBaseTrackingListener(
//            "", IKSdkDefConst.AdFormat.OPEN,
//            "", ""
//        ) {}
//
//    private fun showAdsBase(
//        activity: Activity,
//        screen: String,
//        adsListener: IKSdkShowAdListener?
//    ) {
//        showLogSdk("showAds") { "$screen, start run" }
//        var delayJob: Job? = null
//        val customAdsListener = object : IKSdkBaseListener() {
//            override fun onAdClicked(
//                adNetworkName: String,
//                screen: String,
//                scriptName: String,
//                adUUID: String
//            ) {
//                mTrackingListener.onAdClicked(adNetworkName, screen, scriptName, adUUID)
//            }
//
//            override fun onAdImpression(
//                adNetworkName: String,
//                screen: String,
//                scriptName: String,
//                adUUID: String
//            ) {
//                mTrackingListener.onAdImpression(adNetworkName, screen, scriptName, adUUID)
//                showLogD("showAds") { "$screen, onAdImpression" }
//            }
//
//            override fun onAdShowed(
//                adNetworkName: String,
//                screen: String,
//                scriptName: String,
//                priority: Int,
//                adUUID: String
//            ) {
//                isAdShowing = true
//                showLogD("showAds") { "$screen, onAdReady" }
//                cancelJob(delayJob)
//                adsListener?.onAdShowed(priority)
//                mTrackingListener.onAdShowed(
//                    adNetworkName,
//                    screen,
//                    scriptName,
//                    priority, adUUID
//                )
//                loadAdBase(screen, object : IKSdkLoadAdCoreListener {
//                    override fun onAdLoaded() {
//
//                    }
//
//                    override fun onAdLoadFail(error: IKAdError) {
//
//                    }
//                })
//            }
//
//            override fun onAdShowFailed(
//                adNetworkName: String,
//                screen: String,
//                scriptName: String,
//                error: IKAdError
//            ) {
//                isAdShowing = false
//                showLogD("showAds") { "$screen, onAdShowFailed $error" }
//                cancelJob(delayJob)
//
//                mTrackingListener.onAdShowFailed(
//                    adNetworkName, screen, scriptName,
//                    error
//                )
//
//                mUiScope.launchWithSupervisorJob(Dispatchers.Main) {
//                    showCustom(activity, screen, error, adsListener)
//                }
//                showLogD("showAds") { "$screen, onAdShowFailed start showCustom" }
//                loadAdBase(screen, object : IKSdkLoadAdCoreListener {
//                    override fun onAdLoaded() {
//
//                    }
//
//                    override fun onAdLoadFail(error: IKAdError) {
//
//                    }
//                })
//            }
//
//            override fun onAdDismissed(
//                adNetworkName: String,
//                screen: String,
//                scriptName: String,
//                adUUID: String
//            ) {
//                isAdShowing = false
//                cancelJob(delayJob)
//                mTrackingListener.onAdDismissed(adNetworkName, screen, scriptName, adUUID)
//                adsListener?.onAdDismiss()
//                showLogD("showAds") { "$screen, onAdDismissed" }
//            }
//
//        }
//
//        val totalAdObject: ArrayList<SDKAdPriorityDto> = arrayListOf()
//        suspend fun addAdsToList(adNetwork: AdNetwork, timeCheck: Int) {
//            val ads = when (adNetwork) {
//                AdNetwork.AD_MOB -> admob.getListAdReady(false, timeCheck)
//                AdNetwork.AD_MANAGER -> adGam.getListAdReady(false, timeCheck)
//                AdNetwork.AD_IK -> this.adIK?.getListAdReady(false, timeCheck)
//                AdNetwork.AD_MAX -> adMax?.getListAdReady(false, timeCheck) ?: listOf()
//                else -> listOf()
//            }
//            ads?.forEach { adData ->
//                totalAdObject.add(
//                    SDKAdPriorityDto(
//                        adNetwork.value,
//                        adData.adPriority,
//                        adData.showPriority
//                    )
//                )
//            }
//        }
//        mUiScope.launchWithSupervisorJob(Dispatchers.Default) {
//            addAdsToList(AdNetwork.AD_MOB, IKSdkDefConst.TimeOutAd.OPEN)
//            addAdsToList(AdNetwork.AD_MANAGER, IKSdkDefConst.TimeOutAd.OPEN)
//            addAdsToList(AdNetwork.AD_MAX, IKSdkDefConst.TimeOutAd.OPEN)
//            addAdsToList(AdNetwork.AD_IK, IKSdkDefConst.TimeOutAd.OPEN)
//
//            val maxAdPri = totalAdObject.maxByOrNull { it.adPriority }
//            val maxAdObj =
//                totalAdObject.filter {
//                    it.adPriority >= (maxAdPri?.adPriority ?: 0)
//                }.maxByOrNull { it.showPriority }
//
//            val adsDto = maxAdObj ?: totalAdObject.firstOrNull()
//            val adNetworkValue = adsDto?.adNetwork
//
//            delayJob = launch {
//                delay(DELAY_CHECK_AD)
//            }
//
//            showLogSdk("showAds") { "$screen, process show adNetwork=$adNetworkValue" }
//            when (adNetworkValue) {
//                AdNetwork.AD_MOB.value -> {
//                    adsListener?.onAdReady(adsDto.showPriority)
//                    admob.showAd(this, activity, screen, customAdsListener)
//                }
//
//                AdNetwork.AD_MANAGER.value -> {
//                    adsListener?.onAdReady(adsDto.showPriority)
//                    adGam.showAd(this, activity, screen, customAdsListener)
//                }
//
//                AdNetwork.AD_IK.value -> {
//                    adsListener?.onAdReady(adsDto.showPriority)
//                    adIK?.showAd(
//                        this,
//                        activity,
//                        screen,
//                        customAdsListener
//                    )
//                }
//
//                AdNetwork.AD_MAX.value -> {
//                    adsListener?.onAdReady(0)
//                    adMax?.showAd(this, activity, screen, customAdsListener)
//                }
//
//                else -> {
//                    customAdsListener.onAdShowFailed(
//                        "",
//                        screen,
//                        "",
//                        IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER)
//                    )
//                    showLogSdk("showAds") { "$screen, process show no adNetwork valid!" }
//                }
//            }
//
//            delayJob?.join()
//        }
//    }
//
//    fun loadFirstAds(
//        sdkLocalOpen: List<IKSdkDataOpLocalDto>?,
//        callbackLoad: IKSdkLoadAdCoreListener
//    ) {
//        val callback = object : IKSdkLoadAdCoreListener {
//            override fun onAdLoaded() {
//                callbackLoad.onAdLoaded()
//                adFirstLoading = false
//                showLogD("loadFirstAds") { "onAdLoaded" }
//            }
//
//            override fun onAdLoadFail(error: IKAdError) {
//                callbackLoad.onAdLoadFail(error)
//                adFirstLoading = false
//                showLogD("loadFirstAds") { "onAdLoadFail $error" }
//            }
//        }
//        adFirstLoading = true
//        showLogD("loadFirstAds") { "start load" }
//        mUiScope.launchWithSupervisorJob {
//            if (!IKSdkUtilsCore.canLoadAdAsync()) {
//                callback.onAdLoadFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
//                return@launchWithSupervisorJob
//            }
//
//            val dateFormat = SimpleDateFormat(IKSdkDefConst.FORMAT_DATE_SERVER, Locale.US)
//                .format(System.currentTimeMillis()) ?: IKSdkDefConst.EMPTY
//
//            val adsLocalDto =
//                sdkLocalOpen?.find {
//                    dateFormat == it.validDate && !it.adapters.isNullOrEmpty() &&
//                            it.label == IKSdkDefConst.AdFormat.OPEN
//                }?.adapters
//
//            if (IkmSdkCoreFunc.HandleEna.onHandleFx) {
//                showLogSdk("loadFirstAds") { "handleFx start" }
//                getAdDto()
//                if (mAdDto != null) {
//                    val list = mAdDto?.adapters
//                        ?.filter { it.enable && it.label == IKSdkDefConst.AdLabel.NFX }
//                        ?.sortedByDescending { it.showPriority }
//                    if (!list.isNullOrEmpty()) {
//                        showLogSdk("loadFirstAds") { "handleFx loadAd" }
//                        loadFirstAdInner(list, callback)
//                        return@launchWithSupervisorJob
//                    }
//                }
//            }
//            if (adsLocalDto.isNullOrEmpty()) {
//                showLogSdk("loadFirstAds") { "loadAd none ika01" }
//                getAdDto()
//                val list = mAdDto?.adapters
//                    ?.filter { it.enable && it.label == IKSdkDefConst.AdLabel.START }
//                    ?.sortedByDescending { it.showPriority }
//                if (list.isNullOrEmpty()) {
//                    callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
//                    showLogSdk("loadFirstAds") { "loadAd none ika01 no data" }
//                    return@launchWithSupervisorJob
//                }
//
//                showLogSdk("loadFirstAds") { "loadAd none ika01 running" }
//                loadFirstAdInner(list, callback)
//            } else {
//                showLogSdk("loadFirstAds") { "loadAd ika01" }
//                loadFirstAdInner(adsLocalDto, callback)
//            }
//        }
//    }
//
//    private suspend fun loadFirstAdInner(
//        adsList: List<IKAdapterDto>,
//        callback: IKSdkLoadAdCoreListener
//    ) {
//        showLogSdk("loadFirstAds") { "loadFirstAdInner start" }
//        coroutineScope {
//            val loadSuccessCount = AtomicInteger(0)
//            val loadFailCount = AtomicInteger(0)
//            val deferredList = adsList.map { dto ->
//                async {
//                    showLogSdk("loadFirstAds") { "loadFirstAdInner process ${dto.adNetwork}" }
//                    suspendCancellableCoroutine<Unit> { cont ->
//                        val innerCallback = object : IKSdkLoadAdCoreListener {
//                            override fun onAdLoaded() {
//                                showLogSdk("loadFirstAds") { "loadFirstAdInner process ${dto.adNetwork} onAdLoaded" }
//                                loadSuccessCount.incrementAndGet()
//                                if (cont.isActive) {
//                                    cont.resumeWith(Result.success(Unit))
//                                }
//                            }
//
//                            override fun onAdLoadFail(error: IKAdError) {
//                                showLogSdk("loadFirstAds") { "loadFirstAdInner process ${dto.adNetwork} onAdLoadFail $error" }
//                                loadFailCount.incrementAndGet()
//                                if (cont.isActive) {
//                                    cont.resumeWith(Result.success(Unit))
//                                }
//                            }
//                        }
//
//                        when (dto.adNetwork) {
//                            AdNetwork.AD_MOB.value -> admob.loadAd(
//                                this@coroutineScope,
//                                dto,
//                                innerCallback
//                            )
//
//                            AdNetwork.AD_MANAGER.value -> adGam.loadAd(
//                                this@coroutineScope,
//                                dto,
//                                innerCallback
//                            )
//
//                            AdNetwork.AD_IK.value -> adIK?.loadAd(
//                                this@coroutineScope,
//                                dto,
//                                innerCallback
//                            )
//
//                            AdNetwork.AD_MAX.value -> adMax?.loadAd(
//                                this@coroutineScope,
//                                dto,
//                                innerCallback
//                            )
//                        }
//                    }
//                }
//            }
//            deferredList.awaitAll()
//            if (loadSuccessCount.get() > 0) {
//                callback.onAdLoaded()
//                showLogSdk("loadFirstAds") { "loadFirstAdInner loaded" }
//            } else {
//                callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER))
//                showLogSdk("loadFirstAds") { "loadFirstAdInner loadFail" }
//            }
//        }
//    }
//
//    suspend fun isAdReady(
//        targetPriority: Boolean = false
//    ): Boolean = coroutineScope {
//        val admobDeferred = async { admob.isAdReady(targetPriority, IKSdkDefConst.TimeOutAd.OPEN) }
//        val adGamDeferred = async { adGam.isAdReady(targetPriority, IKSdkDefConst.TimeOutAd.OPEN) }
//        val adIKDeferred =
//            async { adIK?.isAdReady(targetPriority, IKSdkDefConst.TimeOutAd.OPEN) ?: false }
//        val adMaxDeferred =
//            async { adMax?.isAdReady(targetPriority, IKSdkDefConst.TimeOutAd.OPEN) ?: false }
//
//        return@coroutineScope admobDeferred.await() || adGamDeferred.await() || adIKDeferred.await() || adMaxDeferred.await()
//    }
//
//    private suspend fun showCustom(
//        context: Context,
//        screen: String,
//        error: IKAdError,
//        callback: IKSdkShowAdListener?
//    ) {
//        if (IkmSdkCoreFunc.SdkF.isOpenNCL(mRepository) && IkmSdkCoreFunc.AppF?.isInternetAvailable == true) {
//            showLogSdk("loadFirstAds") { "showCustom start" }
//            IkmOpenAdActivity.showAd(context, callback)
//        } else {
//            showLogSdk("loadFirstAds") { "showCustom cant show" }
//            if (NativeBackUpLatest.getBackupAd(IKAdFormat.OPEN) == null) {
//                callback?.onAdShowFail(error)
//                return
//            }
//
//            IkmOpenBackUpAdActivity.showAd(context, screen, callback)
//        }
//    }
//
//    override suspend fun getAdDto(): IKSdkBaseDto? {
//        repeat(4) {
//            IKDataRepository.getInstance().getSDKOpen()?.let {
//                mAdDto = it
//                return it
//            }
//            delay(500)
//        }
//        return mAdDto
//    }
//
//    suspend fun clearAdCache() {
//        admob.clearAllCache()
//        adGam.clearAllCache()
//    }
//
//    private fun showLogSdk(tag: String, message: () -> String) {
//        IKLogs.dSdk(TAG_LOG) {
//            "${tag}:" + message.invoke()
//        }
//    }
//
//    suspend fun showAd(
//        activity: Activity?,
//        screen: String,
//        adListener: IKSdkShowFirstAdListener?
//    ) {
//        showAd(activity = activity, screen = screen, adListener = object : IKSdkShowAdListener {
//            override fun onAdShowed(priority: Int) {
//                adListener?.onAdsShowed(priority, IKSdkDefConst.AdFormat.INTER)
//            }
//
//            override suspend fun onAdReady(priority: Int) {
//            }
//
//            override fun onAdDismiss() {
//                adListener?.onAdsDismiss()
//            }
//
//            override fun onAdShowFail(error: IKAdError) {
//                adListener?.onAdsShowFail(error)
//            }
//        })
//    }
//
//    suspend fun showAd(
//        activity: Activity?,
//        screen: String,
//        adListener: IKSdkShowAdListener?
//    ) {
//        showLogD("showAppOpenAd_2") { "screen=$screen start run" }
//        val context = WeakReference(activity).get()
//        if (context == null) {
//            showLogD("showAppOpenAd_2") { "screen=$screen error context null" }
//            adListener?.onAdShowFail(IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID))
//            return
//        }
//        if (!IKSdkUtilsCore.canShowAdAsync()) {
//            adListener?.onAdShowFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
//            showLogD("showAppOpenAd_2") { "screen=$screen error user premium" }
//            return
//        }
//        IKSdkTrackingHelper.trackingSdkShowAd(
//            adFormat = IKSdkDefConst.AdFormat.OPEN,
//            adStatus = IKSdkDefConst.AdStatus.PRE_SHOW,
//            screen = screen
//        )
//        val sdkAdListener = object : IKSdkShowAdListener {
//            override fun onAdShowed(priority: Int) {
//                adListener?.onAdShowed(priority)
//                showLogD("showAppOpenAd_2") { "screen=$screen onAdReady" }
//            }
//
//            override suspend fun onAdReady(priority: Int) {
//                showLogD("showAppOpenAd_2") { "screen=$screen onAdReady" }
//            }
//
//            override fun onAdDismiss() {
//                adListener?.onAdDismiss()
//                showLogD("showAppOpenAd_2") { "screen=$screen onAdDismiss" }
//            }
//
//            override fun onAdShowFail(error: IKAdError) {
//                adListener?.onAdShowFail(error)
//                IKSdkTrackingHelper.trackingSdkShowAd(
//                    adFormat = IKSdkDefConst.AdFormat.OPEN,
//                    adStatus = IKSdkDefConst.AdStatus.SHOW_FAIL,
//                    screen = screen,
//                    Pair(IKTrackingConst.ParamName.ERROR_CODE, "${error.code}"),
//                    Pair(IKTrackingConst.ParamName.MESSAGE, error.message)
//                )
//                showLogD("showAppOpenAd_2") { "screen=$screen onAdShowFail" }
//            }
//        }
//        if (IKSdkUtilsCore.isFullScreenAdShowing {
//                showLogD("showAppOpenAd_2") { it }
//            }) {
//            sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.OTHER_ADS_SHOWING))
//            return
//        }
//
//        val configDto = mRepository?.getConfigOpen(screen)
//
//        if (configDto == null || configDto.enable != true) {
//            val errorCode = if (configDto == null)
//                IKSdkErrorCode.NO_SCREEN_ID_AD
//            else IKSdkErrorCode.DISABLE_SHOW
//            sdkAdListener.onAdShowFail(IKAdError(errorCode))
//            return
//        }
//        showLogD("showAppOpenAd_2") { "screen=$screen start show" }
//        showAdsBase(
//            context, screen, object : IKSdkShowAdListener {
//                override fun onAdShowed(priority: Int) {
//                    sdkAdListener.onAdShowed(priority)
//                }
//
//                override suspend fun onAdReady(priority: Int) {
//                    sdkAdListener.onAdReady(priority)
//                }
//
//                override fun onAdDismiss() {
//                    sdkAdListener.onAdDismiss()
//                }
//
//                override fun onAdShowFail(error: IKAdError) {
//                    sdkAdListener.onAdShowFail(error)
//                }
//            }
//        )
//
//    }
//
//    private fun showLogD(tag: String, message: () -> String) {
//        IKLogs.dSdk(TAG_LOG) {
//            "${tag}:" + message.invoke()
//        }
//    }
//}