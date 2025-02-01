//package com.example.ads.activity.format.intertial
//
//import android.app.Activity
//import android.os.Handler
//import android.os.Looper
//import com.example.ads.activity.IKSdkOptions
//import com.example.ads.activity.activity.IkmInterAdActivity
//import com.example.ads.activity.activity.IkmInterBackUpAdActivity
//import com.example.ads.activity.core.IKDataCoreManager
////import com.example.ads.activity.core.firebase.IKRemoteDataManager
//import com.example.ads.activity.data.db.IKDataRepository
//import com.example.ads.activity.data.db.IkmSdkCacheFunc
//import com.example.ads.activity.data.dto.AdNetwork
//import com.example.ads.activity.data.dto.AdsScriptName
//import com.example.ads.activity.data.dto.pub.IKAdError
//import com.example.ads.activity.data.dto.pub.IKAdFormat
//import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
//import com.example.ads.activity.data.dto.sdk.SDKAdPriorityDto
//import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
//import com.example.ads.activity.data.dto.sdk.data.IKSdkBaseDto
//import com.example.ads.activity.data.dto.sdk.data.IKSdkDataOpLocalDto
//import com.example.ads.activity.data.dto.sdk.data.IKSdkInterDto
//import com.example.ads.activity.format.base.IKSdkBaseAdController
//import com.example.ads.activity.format.native_ads.NativeBackUpLatest
//import com.example.ads.activity.listener.pub.IKLoadingsAdListener
//import com.example.ads.activity.listener.pub.IKShowAdListener
//import com.example.ads.activity.listener.sdk.IKSdkBaseListener
//import com.example.ads.activity.listener.sdk.IKSdkBaseTrackingListener
//import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
//import com.example.ads.activity.listener.sdk.IKSdkShowAdListener
//import com.example.ads.activity.listener.sdk.IKSdkShowFirstAdListener
//import com.example.ads.activity.mediation.admob.InterstitialAdmob
//import com.example.ads.activity.mediation.applovin.IKApplovinHelper
//import com.example.ads.activity.mediation.applovin.InterstitialMax
//import com.example.ads.activity.mediation.fairbid.IKFairBidHelper
//import com.example.ads.activity.mediation.fairbid.InterstitialFairBid
//import com.example.ads.activity.mediation.gam.InterstitialGam
//import com.example.ads.activity.mediation.ikad.InterstitialIkAd
//import com.example.ads.activity.mediation.playgap.IKPlayGapHelper
//import com.example.ads.activity.mediation.playgap.InterstitialPlayGap
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
//import kotlinx.coroutines.withContext
//import java.lang.ref.WeakReference
//import java.text.SimpleDateFormat
//import java.util.Locale
//import java.util.concurrent.atomic.AtomicInteger
//
//
//object IKInterController : IKSdkBaseAdController() {
//    private const val TAG_LOG = "IKInterC"
//    private var lastTimeShowInterAd = 0L
//
//    override val admob: InterstitialAdmob by lazy {
//        InterstitialAdmob()
//    }
//
//    override val adMax: InterstitialMax? by lazy {
//        if (IKApplovinHelper.isInitialized())
//            InterstitialMax()
//        else null
//    }
//
//    override val adGam: InterstitialGam by lazy {
//        InterstitialGam()
//    }
//
//    override val adFairBid: InterstitialFairBid? by lazy {
//        if (IKFairBidHelper.hasLib())
//            InterstitialFairBid()
//        else null
//    }
//    val adPlayGap: InterstitialPlayGap? by lazy {
//        if (IKPlayGapHelper.isInitialized())
//            InterstitialPlayGap()
//        else null
//    }
//    override val adIK: InterstitialIkAd? by lazy {
//        InterstitialIkAd()
//    }
//
//    override suspend fun getBackupAd(): IKAdapterDto? = IKDataCoreManager.getOtherInterAds()
//    override val adFormat: IKAdFormat = IKAdFormat.INTER
//    private val mTrackingListener: IKSdkBaseTrackingListener
//        get() = object : IKSdkBaseTrackingListener(
//            "", IKSdkDefConst.AdFormat.INTER,
//            "", ""
//        ) {}
//
//    private fun showAdsBase(
//        activity: Activity,
//        screen: String,
//        enableReloadAd: Boolean,
//        canShowCustom: Boolean,
//        adsListener: IKSdkShowAdListener?
//    ) {
//        showLogSdk("showAds") { "$screen, start run" }
//        var delayJob: Job? = null
//
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
//                showLogD("showAds") { "$screen, onAdImpression" }
//                mTrackingListener.onAdImpression(adNetworkName, screen, scriptName, adUUID)
//            }
//
//            override fun onAdShowed(
//                adNetworkName: String,
//                screen: String,
//                scriptName: String,
//                priority: Int,
//                adUUID: String
//            ) {
//                showLogD("showAds") { "$screen, onAdReady" }
//                isAdShowing = true
//                cancelJob(delayJob)
//                adsListener?.onAdShowed(priority)
//                mTrackingListener.onAdShowed(
//                    adNetworkName,
//                    screen,
//                    scriptName,
//                    priority, adUUID
//                )
//                if (IKSdkOptions.enablePreloadInterAd && enableReloadAd)
//                    loadAdBase(screen, object : IKSdkLoadAdCoreListener {
//                        override fun onAdLoaded() {
//
//                        }
//
//                        override fun onAdLoadFail(error: IKAdError) {
//
//                        }
//
//                    })
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
//                mTrackingListener.onAdShowFailed(
//                    adNetworkName, screen, scriptName,
//                    error
//                )
//
//                if (!canShowCustom) {
//                    adsListener?.onAdShowFail(error)
//                    return
//                }
//                showLogD("showAds") { "$screen, onAdShowFailed start showCustom" }
//                mUiScope.launchWithSupervisorJob(Dispatchers.Main) {
//                    showCustom(activity, screen, error, adsListener)
//                }
//                if (IKSdkOptions.enablePreloadInterAd && enableReloadAd)
//                    loadAdBase(screen, object : IKSdkLoadAdCoreListener {
//                        override fun onAdLoaded() {
//
//                        }
//
//                        override fun onAdLoadFail(error: IKAdError) {
//
//                        }
//
//                    })
//            }
//
//            override fun onAdDismissed(
//                adNetworkName: String,
//                screen: String,
//                scriptName: String,
//                adUUID: String
//            ) {
//                showLogD("showAds") { "$screen, onAdDismissed" }
//                isAdShowing = false
//                cancelJob(delayJob)
//                mTrackingListener.onAdDismissed(adNetworkName, screen, scriptName, adUUID)
//                adsListener?.onAdDismiss()
//            }
//
//        }
//
//        val totalAdObject: ArrayList<SDKAdPriorityDto> = arrayListOf()
//        suspend fun addAdsToList(adNetwork: AdNetwork, timeCheck: Int) {
////            val ads = when (adNetwork) {
////                AdNetwork.AD_MOB -> admob.getListAdReady(false, timeCheck)
////                AdNetwork.AD_MANAGER -> adGam.getListAdReady(false, timeCheck)
////                AdNetwork.AD_MAX -> adMax?.getListAdReady(false, timeCheck) ?: listOf()
////                AdNetwork.AD_FAIR_BID -> adFairBid?.getListAdReady(false, timeCheck) ?: listOf()
////                AdNetwork.AD_IK -> adIK?.getListAdReady(false, timeCheck) ?: listOf()
////                else -> {
////                    listOf()
////                }
////            }
////            AdNetwork.entries.find { it == adNetwork }
////            ads.forEach { adData ->
////                totalAdObject.add(
////                    SDKAdPriorityDto(
////                        adNetwork.value,
////                        adData.adPriority,
////                        adData.showPriority
////                    )
////                )
////            }
//        }
//        mUiScope.launchWithSupervisorJob(Dispatchers.Default) {
//            addAdsToList(AdNetwork.AD_MOB, IKSdkDefConst.TimeOutAd.INTER)
//            addAdsToList(AdNetwork.AD_MANAGER, IKSdkDefConst.TimeOutAd.INTER)
//            addAdsToList(AdNetwork.AD_MAX, IKSdkDefConst.TimeOutAd.INTER)
//            addAdsToList(AdNetwork.AD_FAIR_BID, IKSdkDefConst.TimeOutAd.INTER)
//            addAdsToList(AdNetwork.AD_IK, IKSdkDefConst.TimeOutAd.INTER)
//
//            val maxAdObj = totalAdObject.maxWithOrNull(compareBy(
//                { it.showPriority },
//                { it.adPriority }
//            ))
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
//                    admob.showAd(
//                        this,
//                        activity,
//                        screen,
//                        scriptName = AdsScriptName.INTERSTITIAL_ADMOB_INAPP_NORMAL.value,
//                        customAdsListener
//                    )
//                }
//
//                AdNetwork.AD_MANAGER.value -> {
//                    adsListener?.onAdReady(adsDto.showPriority)
//                    adGam.showAd(
//                        this,
//                        activity,
//                        screen,
//                        scriptName = AdsScriptName.INTERSTITIAL_ADMOB_INAPP_NORMAL.value,
//                        customAdsListener
//                    )
//                }
//
//                AdNetwork.AD_MAX.value -> {
//                    adsListener?.onAdReady(0)
//                    adMax?.showAd(
//                        this,
//                        activity,
//                        screen,
//                        scriptName = AdsScriptName.INTERSTITIAL_ADMOB_INAPP_NORMAL.value,
//                        customAdsListener
//                    )
//                }
//
//                AdNetwork.AD_FAIR_BID.value -> {
//                    adsListener?.onAdReady(0)
//                    adFairBid?.showAd(
//                        this,
//                        activity,
//                        screen,
//                        scriptName = AdsScriptName.INTERSTITIAL_ADMOB_INAPP_NORMAL.value,
//                        customAdsListener
//                    )
//                }
//
//                AdNetwork.AD_IK.value -> {
//                    adsListener?.onAdReady(0)
//                    adIK?.showAd(
//                        this,
//                        activity,
//                        screen,
//                        scriptName = AdsScriptName.INTERSTITIAL_IKAD_INAPP_NORMAL.value,
//                        customAdsListener
//                    )
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
//    suspend fun isAdReady(
//        targetPriority: Boolean = false
//    ): Boolean {
//        return when {
//            admob.isAdReady(targetPriority, IKSdkDefConst.TimeOutAd.INTER) -> {
//                true
//            }
//
//            adGam.isAdReady(targetPriority, IKSdkDefConst.TimeOutAd.INTER) -> {
//                true
//            }
//
//            adMax?.isAdReady(
//                targetPriority,
//                IKSdkDefConst.TimeOutAd.INTER
//            ) == true -> {
//                true
//            }
//
//            adFairBid?.isAdReady(
//                targetPriority,
//                IKSdkDefConst.TimeOutAd.INTER
//            ) == true -> {
//                true
//            }
//
//            adIK?.isAdReady(
//                targetPriority,
//                IKSdkDefConst.TimeOutAd.INTER
//            ) == true -> {
//                true
//            }
//
//            else -> {
//                false
//            }
//        }
//    }
//
//    suspend fun loadFirstAds(
//        sdkLocalOpen: List<IKSdkDataOpLocalDto>?,
//        callbackLoad: IKSdkLoadAdCoreListener
//    ) {
//        val callback = object : IKSdkLoadAdCoreListener {
//            override fun onAdLoaded() {
//                callbackLoad.onAdLoaded()
//                showLogD("loadFirstAds") { "onAdLoaded" }
//                adFirstLoading = false
//            }
//
//            override fun onAdLoadFail(error: IKAdError) {
//                callbackLoad.onAdLoadFail(error)
//                showLogD("loadFirstAds") { "onAdLoadFail $error" }
//                adFirstLoading = false
//            }
//        }
//
//        adFirstLoading = true
//        showLogD("loadFirstAds") { "start load" }
//        return withContext(Dispatchers.Default) {
//            if (!IKSdkUtilsCore.canLoadAdAsync()) {
//                callback.onAdLoadFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
//                return@withContext
//            }
//
//            val dateFormat = SimpleDateFormat(IKSdkDefConst.FORMAT_DATE_SERVER, Locale.US)
//                .format(System.currentTimeMillis()) ?: IKSdkDefConst.EMPTY
//
//            val adsLocalDto =
//                sdkLocalOpen?.find {
//                    dateFormat == it.validDate && !it.adapters.isNullOrEmpty() &&
//                            it.label == IKSdkDefConst.AdFormat.INTER
//                }?.adapters
//
//            if (IkmSdkCoreFunc.HandleEna.onHandleFx) {
//                showLogSdk("loadFirstAds") { "handleFx start" }
//                getAdDto()
//                val list = mAdDto?.adapters
//                    ?.filter { it.enable && it.label == IKSdkDefConst.AdLabel.NFX }
//                    ?.sortedByDescending { it.showPriority }
//                if (!list.isNullOrEmpty()) {
//                    showLogSdk("loadFirstAds") { "handleFx loadAd" }
//                    loadFirstAdInner(list, callback)
//                    return@withContext
//                }
//            }
//            if (adsLocalDto.isNullOrEmpty()) {
//                showLogSdk("loadFirstAds") { "loadAd none ika01" }
//                getAdDto()
//                if (mAdDto != null) {
//                    val list = mAdDto?.adapters
//                        ?.filter { it.enable && it.label == IKSdkDefConst.AdLabel.START }
//                        ?.sortedByDescending { it.showPriority }
//                    if (list.isNullOrEmpty()) {
//                        showLogSdk("loadFirstAds") { "loadAd none ika01 no data" }
//                        callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
//                        return@withContext
//                    }
//
//                    showLogSdk("loadFirstAds") { "loadAd none ika01 running" }
//                    loadFirstAdInner(list, callback)
//                }
//            } else {
//                showLogSdk("loadFirstAds") { "loadAd ika01" }
//                loadFirstAdInner(adsLocalDto, callback)
//            }
//            return@withContext
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
//                            AdNetwork.AD_MAX.value -> adMax?.loadAd(
//                                this@coroutineScope,
//                                dto,
//                                innerCallback
//                            )
//
//                            AdNetwork.AD_FAIR_BID.value -> adFairBid?.loadAd(
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
//
//    override suspend fun getAdDto(): IKSdkBaseDto? {
//        repeat(4) {
//            IKDataRepository.getInstance().getSDKInter()?.let {
//                mAdDto = it
//                return it
//            }
//            delay(500)
//        }
//        return mAdDto
//    }
//
//    fun setInterDto(adDto: IKSdkInterDto) {
//        mAdDto = adDto
//    }
//
//    suspend fun showCustom(
//        context: Activity,
//        screen: String,
//        error: IKAdError,
//        callback: IKSdkShowAdListener?
//    ) {
//        if (IkmSdkCoreFunc.SdkF.isInterNCL(mRepository) && IkmSdkCoreFunc.AppF?.isInternetAvailable == true) {
//            showLogSdk("loadFirstAds") { "showCustom start" }
//            IkmInterAdActivity.showAd(context, callback)
//        } else {
//            showLogSdk("loadFirstAds") { "showCustom cant show" }
//
//            val enable =
//                mAdDto?.adapters?.find { it.enable && it.adNetwork == AdNetwork.PLAYGAP.value } != null
//            if (enable && adPlayGap != null) {
//                adPlayGap?.showAd(mUiScope, context, screen, "", object : IKSdkBaseListener() {
//                    override fun onAdClicked(
//                        adNetworkName: String,
//                        screen: String,
//                        scriptName: String,
//                        adUUID: String
//                    ) {
//
//                    }
//
//                    override fun onAdImpression(
//                        adNetworkName: String,
//                        screen: String,
//                        scriptName: String,
//                        adUUID: String
//                    ) {
//                    }
//
//                    override fun onAdShowed(
//                        adNetworkName: String,
//                        screen: String,
//                        scriptName: String,
//                        priority: Int,
//                        adUUID: String
//                    ) {
//                        callback?.onAdShowed(priority)
//                    }
//
//                    override fun onAdShowFailed(
//                        adNetworkName: String,
//                        screen: String,
//                        scriptName: String,
//                        error: IKAdError
//                    ) {
//                        callback?.onAdShowFail(error)
//                    }
//
//                    override fun onAdDismissed(
//                        adNetworkName: String,
//                        screen: String,
//                        scriptName: String,
//                        adUUID: String
//                    ) {
//                        callback?.onAdDismiss()
//                    }
//
//                }) ?: callback?.onAdShowFail(error)
//            } else {
//                if (NativeBackUpLatest.getBackupAd(IKAdFormat.INTER) == null) {
//                    callback?.onAdShowFail(error)
//                    return
//                }
//
//                IkmInterBackUpAdActivity.showAd(context, screen, callback)
//            }
//        }
//    }
//
//    fun showLogD(message: String) {
//        runCatching {
//            IKLogs.d("IKInterCtl") { message }
//        }
//    }
//
//    suspend fun loadAdLabel(
//        label: String,
//        callbackLoad: IKSdkLoadAdCoreListener
//    ) {
//        val callback = object : IKSdkLoadAdCoreListener {
//            override fun onAdLoaded() {
//                callbackLoad.onAdLoaded()
//                showLogSdk("loadAdLabel") { "$label, onAdLoaded" }
//                adFirstLoading = false
//            }
//
//            override fun onAdLoadFail(error: IKAdError) {
//                callbackLoad.onAdLoadFail(error)
//                showLogSdk("loadAdLabel") { "$label, onAdLoadFail $error" }
//                adFirstLoading = false
//            }
//        }
//        adFirstLoading = true
//        showLogSdk("loadAdLabel") { "$label, start" }
//        coroutineScope {
//            if (!IKSdkUtilsCore.canLoadAdAsync()) {
//                callback.onAdLoadFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
//                return@coroutineScope
//            }
//            getAdDto()
//
//            if (mAdDto != null) {
//                val list = mAdDto?.adapters
//                    ?.filter { it.enable && it.label == label }
//                    ?.sortedByDescending { it.showPriority }
//                if (list.isNullOrEmpty()) {
//                    callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
//                    return@coroutineScope
//                }
//
//                val loadSuccessCount = AtomicInteger(0)
//
//                val deferredList = list.map { dto ->
//                    async {
//                        showLogSdk("loadAdLabel") { "$label, process ${dto.adNetwork}" }
//                        suspendCancellableCoroutine<Unit> { cont ->
//                            val innerCallback = object : IKSdkLoadAdCoreListener {
//                                override fun onAdLoaded() {
//                                    showLogSdk("loadAdLabel") { "$label, process ${dto.adNetwork} onAdLoaded" }
//                                    loadSuccessCount.incrementAndGet()
//                                    if (cont.isActive) {
//                                        cont.resumeWith(Result.success(Unit))
//                                    }
//                                }
//
//                                override fun onAdLoadFail(error: IKAdError) {
//                                    showLogSdk("loadAdLabel") { "$label, process ${dto.adNetwork} onAdLoadFail $error" }
//                                    if (error.code == IKSdkErrorCode.READY_CURRENT_AD.code)
//                                        loadSuccessCount.incrementAndGet()
//
//                                    if (cont.isActive) {
//                                        cont.resumeWith(Result.success(Unit))
//                                    }
//                                }
//                            }
//
//                            when (dto.adNetwork) {
//                                AdNetwork.AD_MOB.value -> admob.loadAd(
//                                    this@coroutineScope,
//                                    dto,
//                                    innerCallback
//                                )
//
//                                AdNetwork.AD_MANAGER.value -> adGam.loadAd(
//                                    this@coroutineScope,
//                                    dto,
//                                    innerCallback
//                                )
//
//                                AdNetwork.AD_MAX.value -> adMax?.loadAd(
//                                    this@coroutineScope,
//                                    dto,
//                                    innerCallback
//                                )
//
//                                AdNetwork.AD_FAIR_BID.value -> adFairBid?.loadAd(
//                                    this@coroutineScope,
//                                    dto,
//                                    innerCallback
//                                )
//
//                                AdNetwork.AD_IK.value -> adIK?.loadAd(
//                                    this@coroutineScope,
//                                    dto,
//                                    innerCallback
//                                )
//                            }
//                        }
//                    }
//                }
//                deferredList.awaitAll()
//                if (loadSuccessCount.get() > 0) {
//                    callback.onAdLoaded()
//                    showLogSdk("loadAdLabel") { "$label, loaded" }
//                } else {
//                    callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER))
//                    showLogSdk("loadAdLabel") { "$label, load fail" }
//                }
//            }
//        }
//    }
//
//    suspend fun clearAdCache() {
//        admob.clearAllCache()
//        adGam.clearAllCache()
//        adMax?.clearAllCache()
//    }
//
//    private fun showLogSdk(tag: String, message: () -> String) {
//        IKLogs.dSdk(TAG_LOG) {
//            "${tag}:" + message.invoke()
//        }
//    }
//
//    private fun showLogD(tag: String, message: () -> String) {
//        IKLogs.dSdk(TAG_LOG) {
//            "${tag}:" + message.invoke()
//        }
//    }
//
//    fun canAutoShowClaimAdPlayGap(): Boolean {
//        return mAdDto?.adapters?.find {
//            it.adNetwork == AdNetwork.PLAYGAP.value && it.autoClm == true
//        } != null
//    }
//
//
//    fun loadAd(
//        screenAd: String,
//        callback: IKSdkLoadAdCoreListener?
//    ) {
//        loadAdBase(screenAd, object : IKSdkLoadAdCoreListener {
//            override fun onAdLoaded() {
//                mUiScope.launchWithSupervisorJob {
//                    callback?.onAdLoaded()
//                }
//            }
//
//            override fun onAdLoadFail(error: IKAdError) {
//                mUiScope.launchWithSupervisorJob {
//                    callback?.onAdLoadFail(error)
//                }
//            }
//        })
//    }
//
//    fun showAd(
//        activity: Activity?,
//        screen: String,
//        adListener: IKShowAdListener?,
//        enableReloadAd: Boolean = false,
//        loadingCallback: IKLoadingsAdListener? = null
//    ) {
//        showAd(activity = activity, screen = screen, adListener = object : IKSdkShowAdListener {
//            override fun onAdShowed(priority: Int) {
//                mUiScope.launchWithSupervisorJob {
//                    adListener?.onAdsShowed()
//                }
//            }
//
//            override suspend fun onAdReady(priority: Int) {
//            }
//
//            override fun onAdDismiss() {
//                mUiScope.launchWithSupervisorJob {
//                    adListener?.onAdsDismiss()
//                }
//            }
//
//            override fun onAdShowFail(error: IKAdError) {
//                mUiScope.launchWithSupervisorJob {
//                    adListener?.onAdsShowFail(error)
//                }
//            }
//
//            override fun onAdShowTimeout() {
//                mUiScope.launchWithSupervisorJob {
//                    adListener?.onAdsShowTimeout()
//                }
//            }
//
//        }, enableReloadAd = enableReloadAd, loadingCallback = loadingCallback)
//    }
//
//    fun showAd(
//        activity: Activity?,
//        screen: String,
//        adListener: IKSdkShowFirstAdListener?,
//        enableReloadAd: Boolean = false,
//    ) {
//        showAd(activity = activity, screen = screen, adListener = object : IKSdkShowAdListener {
//            override fun onAdShowed(priority: Int) {
//                mUiScope.launchWithSupervisorJob {
//                    adListener?.onAdsShowed(priority, IKSdkDefConst.AdFormat.INTER)
//                }
//            }
//
//            override suspend fun onAdReady(priority: Int) {
//            }
//
//            override fun onAdDismiss() {
//                mUiScope.launchWithSupervisorJob {
//                    adListener?.onAdsDismiss()
//                }
//            }
//
//            override fun onAdShowFail(error: IKAdError) {
//                mUiScope.launchWithSupervisorJob {
//                    adListener?.onAdsShowFail(error)
//                }
//            }
//        }, enableReloadAd = enableReloadAd)
//    }
//
//    private fun showAd(
//        activity: Activity?,
//        screen: String,
//        adListener: IKSdkShowAdListener?,
//        enableReloadAd: Boolean,
//        loadingCallback: IKLoadingsAdListener? = null
//    ) {
//        mUiScope.launchWithSupervisorJob {
//            showLogD("showInterstitialAd") { "screen=$screen, start run" }
//            val context = WeakReference(activity).get()
//            if (context == null) {
//                showLogD("showInterstitialAd") { "screen=$screen error context null" }
//                adListener?.onAdShowFail(IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID))
//                return@launchWithSupervisorJob
//            }
//            if (!IKSdkUtilsCore.canShowAdAsync()) {
//                adListener?.onAdShowFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
//                showLogD("showInterstitialAd") { "screen=$screen error user premium" }
//                return@launchWithSupervisorJob
//            }
//            IKSdkTrackingHelper.trackingSdkShowAd(
//                adFormat = IKSdkDefConst.AdFormat.INTER,
//                adStatus = IKSdkDefConst.AdStatus.PRE_SHOW,
//                screen = screen
//            )
//            val sdkAdListener = object : IKSdkShowAdListener {
//                override fun onAdShowed(priority: Int) {
//                    adListener?.onAdShowed(priority)
//                    showLogD("showInterstitialAd") { "screen=$screen onAdsShowed" }
//                }
//
//                override suspend fun onAdReady(priority: Int) {
//
//                }
//
//                override fun onAdDismiss() {
//                    adListener?.onAdDismiss()
//                    showLogD("showInterstitialAd") { "screen=$screen onAdsDismiss" }
//                }
//
//                override fun onAdShowFail(error: IKAdError) {
//                    adListener?.onAdShowFail(error)
//                    IKSdkTrackingHelper.trackingSdkShowAd(
//                        adFormat = IKSdkDefConst.AdFormat.INTER,
//                        adStatus = IKSdkDefConst.AdStatus.SHOW_FAIL,
//                        screen = screen,
//                        Pair(IKTrackingConst.ParamName.ERROR_CODE, "${error.code}"),
//                        Pair(IKTrackingConst.ParamName.MESSAGE, error.message)
//                    )
//                    showLogD("showInterstitialAd") { "screen=$screen onAdsShowFail $error" }
//                }
//            }
//
//            if (IKSdkUtilsCore.isFullScreenAdShowing {
//                    showLogD("showInterstitialAd") { it }
//                }) {
//                sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.OTHER_ADS_SHOWING))
//                return@launchWithSupervisorJob
//            }
//
//            val configDto = mRepository?.getProductConfigInter(screen)
//
//            if (configDto == null || configDto.enable != true) {
//                val errorCode = if (configDto == null)
//                    IKSdkErrorCode.NO_SCREEN_ID_AD
//                else IKSdkErrorCode.DISABLE_SHOW
//                sdkAdListener.onAdShowFail(IKAdError(errorCode))
//                return@launchWithSupervisorJob
//            }
//
//            val currentTime = System.currentTimeMillis()
//            if (screen != IKSdkDefConst.AdScreen.START) {
//
//                if (currentTime - lastTimeShowInterAd < (configDto.timeShow
//                        ?: IKRemoteDataManager.BLOCK_TIME_SHOW_FULL_ADS)
//                ) {
//                    sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.SHOW_ADS_FAST))
//                    showLogD("showInterstitialAd") { "screen=$screen block time: ${currentTime - lastTimeShowInterAd}" }
//                    return@launchWithSupervisorJob
//                }
//            }
//            if (!IkmSdkCacheFunc.AD.checkInterAdFrequency(configDto)) {
//                sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.ITEM_AD_BLOCK_SHOW))
//                return@launchWithSupervisorJob
//            }
//
//            if (loadingCallback != null && loadingCallback.timeLoading >= 500) {
//                showLogD("showInterstitialAd") { "showLoading start" }
//                var timeOutShowAd: (() -> Unit)? = null
//                if (IKSdkOptions.mEnableTimeOutShowInterstitialAd) {
//                    timeOutShowAd = {
//                        sdkAdListener.onAdShowTimeout()
//                    }
//                }
//                showAdsBase(
//                    context,
//                    screen,
//                    enableReloadAd,
//                    true,
//                    object : IKSdkShowAdListener {
//                        override fun onAdShowed(priority: Int) {
//                            mUiScope.launchWithSupervisorJob {
//                                loadingCallback.onClose()
//                            }
//                            sdkAdListener.onAdShowed(priority)
//                            if (IKSdkOptions.mEnableTimeOutShowInterstitialAd) {
//                                mUiScope.launchWithSupervisorJob {
//                                    kotlin.runCatching {
//                                        delay(IKSdkOptions.mTimeOutShowInterstitialAd)
//                                        timeOutShowAd?.invoke()
//                                        timeOutShowAd = null
//                                    }
//                                }
//                            }
//                        }
//
//                        override suspend fun onAdReady(priority: Int) {
//                            mUiScope.launchWithSupervisorJob {
//                                loadingCallback.onShow()
//                            }
//                            showLogD("showInterstitialAd") { "showLoading loading show" }
//                            delay(loadingCallback.timeLoading)
//                        }
//
//                        override fun onAdDismiss() {
//                            mUiScope.launchWithSupervisorJob {
//                                loadingCallback.onClose()
//                            }
//                            timeOutShowAd = null
//                            sdkAdListener.onAdDismiss()
//                            kotlin.runCatching {
//                                lastTimeShowInterAd = System.currentTimeMillis()
//                            }
//                        }
//
//                        override fun onAdShowFail(error: IKAdError) {
//                            mUiScope.launchWithSupervisorJob {
//                                loadingCallback.onClose()
//                            }
//                            timeOutShowAd = null
//                            sdkAdListener.onAdShowFail(error)
//                        }
//                    }
//                )
//                return@launchWithSupervisorJob
//            }
//            showLogD("showInterstitialAd") { "none Loading start" }
//            showAdsBase(
//                context, screen, enableReloadAd, true, object : IKSdkShowAdListener {
//                    override fun onAdShowed(priority: Int) {
//                        sdkAdListener.onAdShowed(priority)
//                    }
//
//                    override suspend fun onAdReady(priority: Int) {
//
//                    }
//
//                    override fun onAdDismiss() {
//                        sdkAdListener.onAdDismiss()
//                        kotlin.runCatching {
//                            lastTimeShowInterAd = System.currentTimeMillis()
//                        }
//                    }
//
//                    override fun onAdShowFail(error: IKAdError) {
//                        sdkAdListener.onAdShowFail(error)
//                    }
//                }
//            )
//        }
//    }
//
//    fun showAdCustom(
//        activity: Activity?,
//        screen: String,
//        adListener: IKShowAdListener?,
//        enableReloadAd: Boolean
//    ) {
//        showLogD("showInterstitialAdCustom") { "screen=$screen start run" }
//        if (activity == null) {
//            showLogD("showInterstitialAdCustom") { "screen=$screen error context null" }
//            adListener?.onAdsShowFail(IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID))
//            return
//        }
//        mUiScope.launchWithSupervisorJob(Dispatchers.Main) {
//            if (!IKSdkUtilsCore.canShowAdAsync()) {
//                adListener?.onAdsShowFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
//                showLogD("showInterstitialAdCustom") { "screen=$screen error user premium" }
//                return@launchWithSupervisorJob
//            }
//            IKSdkTrackingHelper.trackingSdkShowAd(
//                adFormat = IKSdkDefConst.AdFormat.INTER,
//                adStatus = IKSdkDefConst.AdStatus.PRE_SHOW,
//                screen = screen
//            )
//            val sdkAdListener = object : IKShowAdListener {
//                override fun onAdsShowed() {
//                    super.onAdsShowed()
//                    mUiScope.launchWithSupervisorJob {
//                        adListener?.onAdsShowed()
//                    }
//                    showLogD("showInterstitialAdCustom") { "screen=$screen onAdsShowed" }
//                }
//
//                override fun onAdsShowTimeout() {
//                    super.onAdsShowTimeout()
//                    mUiScope.launchWithSupervisorJob {
//                        adListener?.onAdsShowTimeout()
//                    }
//                    showLogD("showInterstitialAdCustom") { "screen=$screen onAdsShowTimeout" }
//                }
//
//                override fun onAdsDismiss() {
//                    mUiScope.launchWithSupervisorJob {
//                        adListener?.onAdsDismiss()
//                    }
//                    showLogD("showInterstitialAdCustom") { "screen=$screen onAdsDismiss" }
//                }
//
//                override fun onAdsShowFail(error: IKAdError) {
//                    mUiScope.launchWithSupervisorJob {
//                        adListener?.onAdsShowFail(error)
//                    }
//                    IKSdkTrackingHelper.trackingSdkShowAd(
//                        adFormat = IKSdkDefConst.AdFormat.INTER,
//                        adStatus = IKSdkDefConst.AdStatus.SHOW_FAIL,
//                        screen = screen,
//                        Pair(IKTrackingConst.ParamName.ERROR_CODE, "${error.code}"),
//                        Pair(IKTrackingConst.ParamName.MESSAGE, error.message)
//                    )
//                    showLogD("showInterstitialAdCustom") { "screen=$screen onAdsShowFail" }
//                }
//            }
//
//            if (IKSdkUtilsCore.isFullScreenAdShowing {
//                    showLogD("showInterstitialAdCustom") { it }
//                }) {
//                sdkAdListener.onAdsShowFail(IKAdError(IKSdkErrorCode.OTHER_ADS_SHOWING))
//                return@launchWithSupervisorJob
//            }
//
//            val currentTime = System.currentTimeMillis()
//            if (screen != IKSdkDefConst.AdScreen.START) {
//                if (currentTime - lastTimeShowInterAd < IKRemoteDataManager.BLOCK_TIME_SHOW_FULL_ADS) {
//                    sdkAdListener.onAdsShowFail(IKAdError(IKSdkErrorCode.SHOW_ADS_FAST))
//                    showLogD("showInterstitialAdCustom") { "screen=$screen block time: ${currentTime - lastTimeShowInterAd}" }
//                    return@launchWithSupervisorJob
//                }
//            }
//
//            val mainHandler = Handler(Looper.getMainLooper())
//            showLogD("showInterstitialAdCustom") { "screen=$screen start show" }
//            showAdsBase(
//                activity, screen, enableReloadAd, false, object : IKSdkShowAdListener {
//                    override fun onAdShowed(priority: Int) {
//                        mainHandler.post {
//                            sdkAdListener.onAdsShowed()
//                        }
//                    }
//
//                    override suspend fun onAdReady(priority: Int) {
//
//                    }
//
//                    override fun onAdDismiss() {
//                        mainHandler.post {
//                            sdkAdListener.onAdsDismiss()
//                        }
//                        kotlin.runCatching {
//                            lastTimeShowInterAd = System.currentTimeMillis()
//                        }
//                    }
//
//                    override fun onAdShowFail(error: IKAdError) {
//                        mainHandler.post {
//                            sdkAdListener.onAdsShowFail(error)
//                        }
//                    }
//                }
//            )
//
//        }
//    }
//}