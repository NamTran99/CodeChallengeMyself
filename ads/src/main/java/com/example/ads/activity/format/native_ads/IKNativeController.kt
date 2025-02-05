package com.example.ads.activity.format.native_ads

import com.google.android.gms.ads.nativead.NativeAd
import com.example.ads.activity.core.IKDataCoreManager
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.data.db.IKDataRepository
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.pub.IKAdFormat
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.SDKAdPriorityDto
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkBaseDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdWidgetDetailDto
import com.example.ads.activity.format.base.IKSdkBaseAd
import com.example.ads.activity.format.base.IKSdkBaseAdController
import com.example.ads.activity.listener.pub.IKLoadDisplayAdViewListener
import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
import com.example.ads.activity.mediation.admob.NativeAdmob
import com.example.ads.activity.mediation.applovin.IKApplovinHelper
import com.example.ads.activity.mediation.applovin.IkObjectNativeMax
import com.example.ads.activity.mediation.applovin.NativeMAX
import com.example.ads.activity.mediation.gam.NativeGam
import com.example.ads.activity.utils.IKLogs
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import com.example.ads.activity.utils.IKSdkSerUtils.initMediation
import com.example.ads.activity.utils.IKSdkUtilsCore
import com.example.ads.activity.utils.IkmSdkCoreFunc
import com.example.ads.activity.widgets.IkmDisplayWidgetAdView
import com.example.ads.activity.widgets.SdkDPWAdViewCore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean


object IKNativeController : IKSdkBaseAdController() {
    private const val TAG_LOG = "IKNativeController_"
    override val adMax: NativeMAX? by lazy {
        if (IKApplovinHelper.isInitialized())
            NativeMAX()
        else null
    }

    override val admob: NativeAdmob by lazy {
        NativeAdmob()
    }

    override val adGam: NativeGam by lazy {
        NativeGam()
    }
    override val adIK: IKSdkBaseAd<*>?
        get() = null

    override val adFairBid: IKSdkBaseAd<*>?
        get() = null

    override suspend fun getBackupAd(): IKAdapterDto? = IKDataCoreManager.getOtherNativeAds()
    override val adFormat: IKAdFormat = IKAdFormat.NATIVE

    private var backupAdLatest: IKSdkBaseLoadedAd<*>? = null

    private suspend fun addAndGetAdObjectAd(targetPriority: Boolean): ArrayList<IKSdkBaseLoadedAd<*>> {
        val totalAdObject: ArrayList<IKSdkBaseLoadedAd<*>> = arrayListOf()
        suspend fun addAdsToList(adNetwork: AdNetwork, timeCheck: Int) {
            val ads = when (adNetwork) {
                AdNetwork.AD_MOB -> admob.getListAdReady(targetPriority, timeCheck)
                AdNetwork.AD_MANAGER -> adGam.getListAdReady(targetPriority, timeCheck)
                AdNetwork.AD_MAX -> adMax?.getListAdReady(targetPriority, timeCheck) ?: listOf()
                ?: listOf()

                else -> listOf()
            }

            val iterator = ads.iterator()
            while (iterator.hasNext()) {
                val adData = kotlin.runCatching {
                    iterator.next()
                }.getOrNull()
                if (adData?.loadedAd == null)
                    break
                totalAdObject.add(
                    adData
                )
            }
        }
        addAdsToList(AdNetwork.AD_MOB, IKSdkDefConst.TimeOutAd.NATIVE)
        addAdsToList(AdNetwork.AD_MANAGER, IKSdkDefConst.TimeOutAd.NATIVE)
        addAdsToList(AdNetwork.AD_MAX, IKSdkDefConst.TimeOutAd.NATIVE)
        return totalAdObject
    }

    private suspend fun addAndGetAdData(targetPriority: Boolean): ArrayList<SDKAdPriorityDto> {
        val totalAdObject: ArrayList<SDKAdPriorityDto> = arrayListOf()
        suspend fun addAdsToList(adNetwork: AdNetwork, timeCheck: Int) {
            val ads = when (adNetwork) {
                AdNetwork.AD_MOB -> admob.getListAdReady(targetPriority, timeCheck)
                AdNetwork.AD_MANAGER -> adGam.getListAdReady(targetPriority, timeCheck)
                AdNetwork.AD_MAX -> adMax?.getListAdReady(targetPriority, timeCheck) ?: listOf()
                else -> listOf()
            }

            val iterator = ads.iterator()
            while (iterator.hasNext()) {
                val adData = kotlin.runCatching {
                    iterator.next()
                }.getOrNull()
                if (adData?.loadedAd == null)
                    break
                totalAdObject.add(
                    SDKAdPriorityDto(
                        adNetwork.value,
                        adData.adPriority,
                        adData.showPriority
                    )
                )
            }
        }
        addAdsToList(AdNetwork.AD_MOB, IKSdkDefConst.TimeOutAd.NATIVE)
        addAdsToList(AdNetwork.AD_MANAGER, IKSdkDefConst.TimeOutAd.NATIVE)
        addAdsToList(AdNetwork.AD_MAX, IKSdkDefConst.TimeOutAd.NATIVE)
        return totalAdObject
    }

    fun showAd(
        screen: String,
        itemAds: IKSdkProdWidgetDetailDto,
        adListener: IKSdkShowWidgetAdListener,
        isRecall: Boolean = false,
        canRetry: Boolean = true
    ) {
        showAdNormal(screen, itemAds, adListener, canRetry)
    }

    fun showAdNormal(
        screen: String,
        itemAds: IKSdkProdWidgetDetailDto,
        adListener: IKSdkShowWidgetAdListener,
        canRetry: Boolean,
        targetPriority: Boolean = true
    ) {
        IKLogs.d(TAG_LOG) { "showAdNormal" }

        mUiScope.launchWithSupervisorJob(Dispatchers.Default) {
            val totalAdObject = addAndGetAdData(targetPriority)
            val maxAdObj = totalAdObject.maxWithOrNull(compareBy(
                { it.showPriority },
                { it.adPriority }
            ))
            if (maxAdObj == null) {
                loadAndShow(
                    targetPriority,
                    adListener,
                    screen,
                    canRetry,
                    itemAds
                )
                return@launchWithSupervisorJob
            }
            val delayJob: Job?

            val adLoader = when (maxAdObj.adNetwork) {
                AdNetwork.AD_MOB.value -> {
                    admob
                }

                AdNetwork.AD_MANAGER.value -> {
                    adGam
                }

                AdNetwork.AD_MAX.value -> {
                    adMax
                }

                else -> null
            }
            if (adLoader != null) {
                delayJob = launch {
                    delay(DELAY_CHECK_AD)
                }
                adLoader.showAdNetwork(
                    screen,
                    adListener,
                    itemAds,
                    delayJob
                )
                delayJob.join()
                loadAdBase(screen, object : IKSdkLoadAdCoreListener {
                    override fun onAdLoaded() {

                    }

                    override fun onAdLoadFail(error: IKAdError) {

                    }

                })
            } else {
                loadAndShow(
                    targetPriority,
                    adListener,
                    screen,
                    canRetry,
                    itemAds
                )
            }
        }
    }

    override suspend fun getAdDto(): IKSdkBaseDto? {
        repeat(4) {
            IKDataRepository.getInstance().getSDKNative()?.let {
                mAdDto = it
                return it
            }
            delay(500)
        }
        return mAdDto
    }

    private suspend fun loadAndShow(
        targetPriority: Boolean,
        adListener: IKSdkShowWidgetAdListener,
        screen: String,
        canRetry: Boolean,
        itemAds: IKSdkProdWidgetDetailDto
    ) {
        val scriptName = ""
        if (!targetPriority) {
            adListener.onAdShowFail(
                IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW),
                scriptName,
                IKSdkDefConst.UNKNOWN
            )
            return
        }
        IKLogs.d(TAG_LOG) { "showAdNormal_ loadAndShow" }
        var countRetry = 1
        loadAndShowAd(
            screen,
            object : IKSdkShowWidgetAdListener {
                override fun onAdReady(
                    adData: IKSdkBaseLoadedAd<*>,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    adListener.onAdReady(adData, scriptName, adNetworkName)
                    loadAdBase(screen, object : IKSdkLoadAdCoreListener {
                        override fun onAdLoaded() {

                        }

                        override fun onAdLoadFail(error: IKAdError) {

                        }

                    })
                }

                override fun onAdReloaded(
                    adData: IKSdkBaseLoadedAd<*>,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    adListener.onAdReloaded(adData, scriptName, adNetworkName)
                }

                override fun onAdReloadFail(
                    error: IKAdError,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    adListener.onAdReloadFail(error, scriptName, adNetworkName)
                }

                override fun onAdShowFail(
                    error: IKAdError,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    if (canRetry && countRetry > 0 && error.code == IKSdkErrorCode.READY_CURRENT_AD.code) {
                        showAd(
                            screen,
                            itemAds,
                            adListener,
                            false,
                            canRetry = false
                        )
                        countRetry--
                    } else {
                        showAdNormal(
                            screen,
                            itemAds,
                            adListener,
                            false,
                            targetPriority = false
                        )
                    }
                    loadAdBase(screen, object : IKSdkLoadAdCoreListener {
                        override fun onAdLoaded() {

                        }

                        override fun onAdLoadFail(error: IKAdError) {

                        }

                    })
                }

                override fun onAdClick(scriptName: String, adNetworkName: String) {
                    adListener.onAdClick(scriptName, adNetworkName)
                }

                override fun onAdImpression(scriptName: String, adNetworkName: String) {
                    adListener.onAdImpression(scriptName, adNetworkName)
                }
            },
            itemAds,
            IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW)
        )
    }


    private suspend fun loadAndShowAd(
        screen: String,
        showAdListener: IKSdkShowWidgetAdListener,
        itemAds: IKSdkProdWidgetDetailDto,
        error: IKAdError
    ) {
        val scriptName = ""
        IKLogs.d(TAG_LOG) { "loadAndShowAd" }
        if (!IKSdkUtilsCore.canLoadAdAsync()) {
            showAdListener.onAdShowFail(error, scriptName, IKSdkDefConst.UNKNOWN)
            IKLogs.d(TAG_LOG) { "loadAndShowAd_ onAdShowFail_ $error" }
            return
        }

        val adDto = getAdDto()
        if (adDto?.adapters.isNullOrEmpty()) {
            loadAndShowBackupAd(screen, itemAds, error, showAdListener)
            IKLogs.d(TAG_LOG) { "loadAndShowAd_ onAdShowFail_ ${IKSdkErrorCode.NO_SCREEN_ID_AD}" }
            return
        }
        val filterData = getAdDataInApp(screen)?.filter { it.disableLoadAndShow != true }
        if (filterData.isNullOrEmpty()) {
            loadAndShowBackupAd(screen, itemAds, error, showAdListener)
            return
        }
        filterData.forEach {
//            it.initMediation()
        }
        val sortedArray = withContext(Dispatchers.Default) {
            val arrayList = ArrayList<Triple<String, Int, IKAdUnitDto>>()
            filterData.forEach { s ->
                s.adData?.forEach {
                    arrayList.add(Triple(s.adNetwork ?: "", s.showPriority ?: 0, it))
                }
            }
            val comparator =
                compareByDescending<Triple<String, Int, IKAdUnitDto>> { it.second }
                    .thenByDescending { it.third.adPriority ?: 0 }
            arrayList.sortedWith(comparator)
        }
        if (sortedArray.isEmpty()) {
            loadAndShowBackupAd(screen, itemAds, error, showAdListener)
            IKLogs.d(TAG_LOG) { "showAdsCollapse_ onAdShowFail_ ${IKSdkErrorCode.NO_AD_FROM_SERVER}" }
            return
        }
        var adLoaded = false
        for (adapter in sortedArray) {
            val success =
                loadAndShowForNetwork(screen, adapter.first, adapter.third,
                    object : IKSdkShowWidgetAdListener {
                        override fun onAdReady(
                            adData: IKSdkBaseLoadedAd<*>,
                            scriptName: String,
                            adNetworkName: String
                        ) {
                            showAdListener.onAdReady(adData, scriptName, adNetworkName)
                        }

                        override fun onAdShowFail(
                            error: IKAdError,
                            scriptName: String,
                            adNetworkName: String
                        ) {
                        }

                        override fun onAdClick(scriptName: String, adNetworkName: String) {
                            showAdListener.onAdClick(scriptName, adNetworkName)
                        }

                        override fun onAdImpression(scriptName: String, adNetworkName: String) {
                            showAdListener.onAdImpression(scriptName, adNetworkName)
                        }

                        override fun onAdReloaded(
                            adData: IKSdkBaseLoadedAd<*>,
                            scriptName: String,
                            adNetworkName: String
                        ) {
                            showAdListener.onAdReloaded(adData, scriptName, adNetworkName)
                        }

                        override fun onAdReloadFail(
                            error: IKAdError,
                            scriptName: String,
                            adNetworkName: String
                        ) {
                            showAdListener.onAdReloadFail(error, scriptName, adNetworkName)
                        }
                    })
            if (success) {
                adLoaded = true
                break
            }
        }
        if (!adLoaded) {
            loadAndShowBackupAd(screen, itemAds, error, showAdListener)
            IKLogs.d(TAG_LOG) { "showAdsCollapse_ onAdShowFail_ ${IKSdkErrorCode.NO_AD_FROM_SERVER}" }
        }
    }


    private fun IKSdkBaseNativeAds<*>.showAdNetwork(
        screen: String,
        showAdListener: IKSdkShowWidgetAdListener?,
        itemAds: IKSdkProdWidgetDetailDto,
        delayJob: Job?
    ) {
        val scriptName = ""
        IKLogs.d(TAG_LOG) { "showAdNetwork" }
        showAvailableAd(
            mUiScope,
            screen,
            scriptName,
            object : IKSdkShowWidgetAdListener {
                override fun onAdReloaded(
                    adData: IKSdkBaseLoadedAd<*>,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    showAdListener?.onAdReloaded(adData, scriptName, adNetworkName)
                }

                override fun onAdReloadFail(
                    error: IKAdError,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    showAdListener?.onAdReloadFail(error, scriptName, adNetworkName)
                }

                override fun onAdImpression(scriptName: String, adNetworkName: String) {
                    showAdListener?.onAdImpression(scriptName, adNetworkName)
                }

                override fun onAdReady(
                    adData: IKSdkBaseLoadedAd<*>,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    IKLogs.d(TAG_LOG) { "showAdNetwork_  onAdReady" }
                    cancelJob(delayJob)
                    showAdListener?.onAdReady(adData, scriptName, adNetworkName)
                }

                override fun onAdShowFail(
                    error: IKAdError,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    IKLogs.d(TAG_LOG) { "showAdNetwork_  onAdShowFail_ $error" }
                    cancelJob(delayJob)
                    loadAndShowBackupAd(
                        screen,
                        itemAds,
                        error,
                        showAdListener
                    )
                }

                override fun onAdClick(scriptName: String, adNetworkName: String) {
                    showAdListener?.onAdClick(scriptName, adNetworkName)
                }
            }
        )
    }

    private suspend fun loadAndShowForNetwork(
        screen: String,
        adNetwork: String,
        dto: IKAdUnitDto,
        showAdListener: IKSdkShowWidgetAdListener
    ): Boolean {
        IKLogs.d(TAG_LOG) { "loadAndShowAdNetwork" }
        val scriptName = ""
        return when (adNetwork) {
            AdNetwork.AD_MOB.value -> {
                admob.loadAndShowUnitForNetwork(
                    screen,
                    scriptName,
                    dto,
                    showAdListener
                )
            }

            AdNetwork.AD_MANAGER.value -> {
                adGam.loadAndShowUnitForNetwork(
                    screen,
                    scriptName,
                    dto,
                    showAdListener
                )
            }

            AdNetwork.AD_MAX.value -> {
                adMax?.loadAndShowUnitForNetwork(
                    screen,
                    scriptName,
                    dto,
                    showAdListener
                ) ?: false
            }


            else -> {
                showAdListener.onAdShowFail(
                    IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD),
                    scriptName,
                    IKSdkDefConst.UNKNOWN
                )
                IKLogs.d(TAG_LOG) { "loadAdForNetwork_ onAdShowFail_ ${IKSdkErrorCode.NO_DATA_TO_LOAD_AD}" }
                false
            }
        }

    }

    private suspend fun IKSdkBaseNativeAds<*>.loadAndShowUnitForNetwork(
        screen: String,
        scriptName: String,
        dto: IKAdUnitDto,
        callback: IKSdkShowWidgetAdListener
    ): Boolean {
        val result = CompletableDeferred<Boolean>()
        loadAndShowAd(
            mUiScope,
            screen,
            scriptName,
            dto, object : IKSdkShowWidgetAdListener {

                override fun onAdReady(
                    adData: IKSdkBaseLoadedAd<*>,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    result.complete(true)
                    callback.onAdReady(adData, scriptName, adNetworkName)
                }

                override fun onAdShowFail(
                    error: IKAdError,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    result.complete(false)
                    callback.onAdShowFail(error, scriptName, adNetworkName)
                }

                override fun onAdClick(scriptName: String, adNetworkName: String) {
                    callback.onAdClick(scriptName, adNetworkName)
                }

                override fun onAdImpression(scriptName: String, adNetworkName: String) {
                    callback.onAdImpression(scriptName, adNetworkName)
                }

                override fun onAdReloaded(
                    adData: IKSdkBaseLoadedAd<*>,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    callback.onAdReloaded(adData, scriptName, adNetworkName)
                }

                override fun onAdReloadFail(
                    error: IKAdError,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    callback.onAdReloadFail(error, scriptName, adNetworkName)
                }
            }
        )
        return result.await()
    }

    private fun loadAndShowBackupAd(
        screen: String,
        itemAds: IKSdkProdWidgetDetailDto,
        error: IKAdError,
        showAdListener: IKSdkShowWidgetAdListener?
    ) {
        val scriptName = ""

        IKLogs.d(TAG_LOG) { "loadAndShowBackupAd" }
        if (itemAds.enableBackup == false) {
            showAdListener?.onAdShowFail(error, scriptName, IKSdkDefConst.UNKNOWN)
            IKLogs.d(TAG_LOG) { "loadAndShowBackupAd_ onAdShowFail_ disable backup" }
            return
        }

        mUiScope.launchWithSupervisorJob {
            val otherObject = getBackupAd()
            if (otherObject == null || !otherObject.enable) {
                IKLogs.d(TAG_LOG) { "loadAndShowBackupAd_ onAdShowFail" }
                showAdListener?.onAdShowFail(error, scriptName, IKSdkDefConst.UNKNOWN)
                return@launchWithSupervisorJob
            }

            var otherAdsDto =
                otherObject.adData?.firstOrNull { it.label != IKSdkDefConst.AdLabel.NFX }

            if (IkmSdkCoreFunc.HandleEna.onHandleFx) {
                otherAdsDto = otherObject.adData?.find { it.label == IKSdkDefConst.AdLabel.NFX }
            }

            if (otherAdsDto == null) {
                showAdListener?.onAdShowFail(error, scriptName, IKSdkDefConst.UNKNOWN)
                IKLogs.d(TAG_LOG) { "loadAndShowBackupAd_ onAdShowFail_ other ad invalid" }
                return@launchWithSupervisorJob
            }
            val adLoader = when (otherObject.adNetwork) {
                AdNetwork.AD_MOB.value -> {
                    admob
                }

                AdNetwork.AD_MANAGER.value -> {
                    adGam
                }

                AdNetwork.AD_MAX.value -> {
                    adMax
                }

                else -> null
            }
            if (adLoader == null) {
                IKLogs.d(TAG_LOG) { "loadAndShowBackupAd_ onAdShowFail_ network invalid" }
                showAdListener?.onAdShowFail(error, scriptName, IKSdkDefConst.UNKNOWN)
                return@launchWithSupervisorJob
            }
            IKLogs.d(TAG_LOG) { "loadAndShowBackupAd_ loadSingleAdTimeout" }
            adLoader.loadSingleAd(
                mUiScope,
                screen,
                otherAdsDto,
                0,
                scriptName,
                null,
                true,
                object : IKSdkLoadAdCoreListener {
                    override fun onAdLoadFail(error: IKAdError) {
                        IKLogs.d(TAG_LOG) { "loadAndShowBackupAd_ onAdLoadFail_ $error" }
                        showAdListener?.onAdShowFail(error, scriptName, IKSdkDefConst.UNKNOWN)

                    }

                    override fun onAdLoaded() {
                        IKLogs.d(TAG_LOG) { "loadAndShowBackupAd_ onAdLoaded" }
                        adLoader.showAdNetworkBackup(
                            screen,
                            showAdListener,
                            null
                        )
                    }

                })
        }
    }

    private fun IKSdkBaseNativeAds<*>.showAdNetworkBackup(
        screen: String,
        showAdListener: IKSdkShowWidgetAdListener?,
        delayJob: Job?
    ) {

        val scriptName = ""
        IKLogs.d(TAG_LOG) { "showAdNetworkBackup_ showAvailableAd" }
        showAvailableAd(
            mUiScope,
            screen,
            scriptName,
            object : IKSdkShowWidgetAdListener {

                override fun onAdReady(
                    adData: IKSdkBaseLoadedAd<*>,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    IKLogs.d(TAG_LOG) { "showAdNetworkBackup_ onAdReady" }
                    showAdListener?.onAdReady(adData, scriptName, adNetworkName)
                    cancelJob(delayJob)
                }

                override fun onAdReloaded(
                    adData: IKSdkBaseLoadedAd<*>,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    showAdListener?.onAdReloaded(adData, scriptName, adNetworkName)
                }

                override fun onAdReloadFail(
                    error: IKAdError,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    showAdListener?.onAdReloadFail(error, scriptName, adNetworkName)
                }

                override fun onAdShowFail(
                    error: IKAdError,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    IKLogs.d(TAG_LOG) { "showAdNetworkBackup_ onAdShowFail_ $error" }
                    showAdListener?.onAdShowFail(error, scriptName, adNetworkName)
                    cancelJob(delayJob)
                }

                override fun onAdClick(scriptName: String, adNetworkName: String) {
                    showAdListener?.onAdClick(scriptName, adNetworkName)
                }

                override fun onAdImpression(scriptName: String, adNetworkName: String) {
                    showAdListener?.onAdImpression(scriptName, adNetworkName)
                }
            }
        )
    }

    suspend fun isAdReady(
        targetPriority: Boolean = false
    ): Boolean {
        return when {
            admob.isAdReady(targetPriority, IKSdkDefConst.TimeOutAd.NATIVE) -> {
                true
            }

            adGam.isAdReady(targetPriority, IKSdkDefConst.TimeOutAd.NATIVE) -> {
                true
            }

            adMax?.isAdReady(targetPriority, IKSdkDefConst.TimeOutAd.NATIVE) == true -> {
                true
            }

            else -> {
                false
            }
        }
    }

    fun preloadAd(screen: String, callback: IKSdkLoadAdCoreListener) {
        mUiScope.launchWithSupervisorJob {
            if (!IKSdkUtilsCore.canLoadAdAsync()) {
                IKLogs.d(TAG_LOG) { "preloadAd_ onAdLoadFail_ ${IKSdkErrorCode.USER_PREMIUM}" }
                callback.onAdLoadFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
                return@launchWithSupervisorJob
            }
//            val weakReference = WeakReference(IKSdkApplicationProvider.getContext())
//            val context = weakReference.get()
//            if (context == null) {
//                IKLogs.d(TAG_LOG) { "preloadAd_ onAdLoadFail_ ${IKSdkErrorCode.CONTEXT_NOT_VALID}" }
//                callback.onAdLoadFail(IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID))
//                return@launchWithSupervisorJob
//            }

            getAdDto()
            if (mAdDto?.adapters.isNullOrEmpty()) {
                IKLogs.d(TAG_LOG) { "preloadAd_ onAdLoadFail_ ${IKSdkErrorCode.NO_DATA_TO_LOAD_AD}" }
                callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
                return@launchWithSupervisorJob
            }

            withContext(Dispatchers.IO) {
                val filterData = getAdDataInApp(screen)
                if (filterData.isNullOrEmpty()) {
                    IKLogs.d(TAG_LOG) { "preloadAd_ onAdLoadFail_ ${IKSdkErrorCode.NO_DATA_TO_LOAD_AD}" }
                    callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
                    return@withContext
                }

                val latch = CountDownLatch(filterData.size)
                val adLoadedSuccessfully = AtomicBoolean(false)
                filterData.forEach { ad ->
                    when (ad.adNetwork) {
                        AdNetwork.AD_MOB.value,
                        AdNetwork.AD_MAX.value,
                        AdNetwork.AD_MANAGER.value -> {
                            val adLoader = when (ad.adNetwork) {
                                AdNetwork.AD_MOB.value -> admob
                                AdNetwork.AD_MAX.value -> adMax
                                AdNetwork.AD_MANAGER.value -> adGam
                                else -> null
                            }

//                            adLoader?.loadPreloadAd(
//                                this,
//                                context,
//                                ad,
//                                "",
//                                object : IKSdkLoadAdCoreListener {
//                                    override fun onAdLoaded() {
//                                        adLoadedSuccessfully.set(true)
//                                        latch.countDown()
//                                    }
//
//                                    override fun onAdLoadFail(error: IKAdError) {
//                                        adLoadedSuccessfully.set(false)
//                                        latch.countDown()
//                                    }
//                                })
                        }

                        else -> {
                            adLoadedSuccessfully.set(false)
                            latch.countDown()
                        }
                    }
                }
                latch.await()
                if (adLoadedSuccessfully.get()) {
                    callback.onAdLoaded()

                } else launch {
                    loadBackupAd(mUiScope, object : IKSdkLoadAdCoreListener {
                        override fun onAdLoaded() {
                            IKLogs.d(TAG_LOG) { "preloadAd_ loadBackupAd_ onAdLoaded" }
                            callback.onAdLoaded()
                        }

                        override fun onAdLoadFail(error: IKAdError) {
                            IKLogs.d(TAG_LOG) { "preloadAd_ onAdLoadFail_ $error" }
                            callback.onAdLoadFail(error)
                        }
                    })
                }
            }
        }
    }

    fun loadDisplayAd(screen: String, callback: IKLoadDisplayAdViewListener) {
        IKLogs.d(TAG_LOG) { "loadDisplayAd start screen = $screen" }
        mUiScope.launchWithSupervisorJob {
            if (!IKSdkUtilsCore.canLoadAdAsync()) {
                IKLogs.d(TAG_LOG) { "loadDisplayAd onAdLoadFail_ ${IKSdkErrorCode.USER_PREMIUM}" }
                callback.onAdLoadFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
                return@launchWithSupervisorJob
            }

            getAdDto()
            if (mAdDto?.adapters.isNullOrEmpty()) {
                IKLogs.d(TAG_LOG) { "loadDisplayAd onAdLoadFail_ ${IKSdkErrorCode.NO_DATA_TO_LOAD_AD}" }
                callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
                return@launchWithSupervisorJob
            }

            val totalAdObject = addAndGetAdObjectAd(false)
            val maxAdObj = totalAdObject.maxWithOrNull(compareBy(
                { it.showPriority },
                { it.adPriority }
            ))
            val waitAdLoad = CompletableDeferred<Boolean>()
            suspend fun checkAdLoaded(adObj: IKSdkBaseLoadedAd<*>) {
                when (adObj.adNetwork) {
                    AdNetwork.AD_MOB.value -> admob.removeAdAny(adObj)
                    AdNetwork.AD_MANAGER.value -> adGam.removeAdAny(adObj)
                    AdNetwork.AD_MAX.value -> adMax?.removeAdAny(adObj)
                    else -> {
                    }
                }
                val adsViewObj = IkmDisplayWidgetAdView(
                    adObj,
                    SDKAdPriorityDto(
                        adObj.adNetwork,
                        adObj.adPriority,
                        adObj.showPriority
                    ),
                    adF = IKSdkDefConst.AdFormat.NATIVE
                )
                callback.onAdLoaded(adsViewObj)
            }
            if (maxAdObj == null) {
                loadDisplayAdView(screen, object : IKSdkLoadAdCoreListener {
                    override fun onAdLoaded() {
                        waitAdLoad.complete(true)
                        IKLogs.d(TAG_LOG) { "loadDisplayAd loadDisplayAdView onAdLoaded" }
                    }

                    override fun onAdLoadFail(error: IKAdError) {
                        waitAdLoad.complete(false)
                        callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER))
                        IKLogs.d(TAG_LOG) { "loadDisplayAd onAdLoadFail ${error.message}" }
                    }
                })
                if (waitAdLoad.await()) {
                    val totalAdObjectInner = addAndGetAdObjectAd(false)
                    val maxAdObjInner = totalAdObjectInner.maxWithOrNull(compareBy(
                        { it.showPriority },
                        { it.adPriority }
                    ))
                    if (maxAdObjInner != null)
                        checkAdLoaded(maxAdObjInner)
                    else callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER))
                }
                return@launchWithSupervisorJob
            }
            checkAdLoaded(maxAdObj)
        }
    }

    fun showWithDisplayAdView(
        screen: String,
        displayWidgetAdView: IkmDisplayWidgetAdView,
        adListener: IKSdkShowWidgetAdListener?
    ) {
        IKLogs.d(TAG_LOG) { "showWithDisplayAdView" }

        val showAdListener = object : IKSdkShowWidgetAdListener {
            override fun onAdReady(
                adData: IKSdkBaseLoadedAd<*>,
                scriptName: String,
                adNetworkName: String
            ) {
                adListener?.onAdReady(adData, scriptName, adNetworkName)
                IKLogs.d(TAG_LOG) { "showWithDisplayAdView_ onAdReady" }
            }

            override fun onAdReloaded(
                adData: IKSdkBaseLoadedAd<*>,
                scriptName: String,
                adNetworkName: String
            ) {
                adListener?.onAdReloaded(adData, scriptName, adNetworkName)
            }

            override fun onAdReloadFail(
                error: IKAdError,
                scriptName: String,
                adNetworkName: String
            ) {
                adListener?.onAdReloadFail(error, scriptName, adNetworkName)
            }

            override fun onAdShowFail(error: IKAdError, scriptName: String, adNetworkName: String) {
                adListener?.onAdShowFail(error, scriptName, adNetworkName)
                IKLogs.d(TAG_LOG) { "showWithDisplayAdView_ onAdShowFail_ $error" }
            }

            override fun onAdClick(scriptName: String, adNetworkName: String) {
                adListener?.onAdClick(scriptName, adNetworkName)
            }

            override fun onAdImpression(scriptName: String, adNetworkName: String) {
                adListener?.onAdImpression(scriptName, adNetworkName)
            }
        }

        mUiScope.launchWithSupervisorJob(Dispatchers.Default) {
            val adsViewObj = displayWidgetAdView as? SdkDPWAdViewCore
            val adDetail = adsViewObj?.clX(adsViewObj) as? SDKAdPriorityDto

            val scriptName = ""
            val adNetwork = adDetail?.adNetwork

            val adLoader = when (adNetwork) {
                AdNetwork.AD_MOB.value -> admob
                AdNetwork.AD_MANAGER.value -> adGam
                AdNetwork.AD_MAX.value -> adMax
                else -> null
            }
            adLoader?.showAdWithAdView(
                mUiScope,
                screen,
                scriptName,
                displayWidgetAdView,
                showAdListener
            ) ?: adListener?.onAdShowFail(
                IKAdError(IKSdkErrorCode.AD_NETWORK_NULL),
                scriptName,
                IKSdkDefConst.UNKNOWN
            )
        }
    }


    suspend fun getDisplayAd(): IkmDisplayWidgetAdView? {
        return withContext(Dispatchers.Default) {
            val totalAdObject = addAndGetAdObjectAd(false)
            val adObj = totalAdObject.maxWithOrNull(compareBy(
                { it.showPriority },
                { it.adPriority }
            ))

            if (adObj == null) {
                return@withContext null
            }

            when (adObj.adNetwork) {
                AdNetwork.AD_MOB.value -> admob.removeAdAny(adObj)
                AdNetwork.AD_MANAGER.value -> adGam.removeAdAny(adObj)
                AdNetwork.AD_MAX.value -> adMax?.removeAdAny(adObj)
                else -> {
                }
            }
            val adsViewObj = IkmDisplayWidgetAdView(
                adObj,
                SDKAdPriorityDto(
                    adObj.adNetwork,
                    adObj.adPriority,
                    adObj.showPriority
                ),
                adF = IKSdkDefConst.AdFormat.NATIVE
            )
            adsViewObj
        }
    }

    suspend fun getReadyAd(): IKSdkBaseLoadedAd<*>? {
        return withContext(Dispatchers.Default) {
            val totalAdObject = addAndGetAdObjectAd(false)
            val maxAdObj = totalAdObject.maxWithOrNull(compareBy(
                { it.showPriority },
                { it.adPriority }
            ))
            if (maxAdObj == null) {
                return@withContext null
            }
            when (maxAdObj.adNetwork) {
                AdNetwork.AD_MOB.value -> {
                    admob.removeAdAny(maxAdObj)
                    return@withContext maxAdObj
                }

                AdNetwork.AD_MANAGER.value -> {
                    adGam.removeAdAny(maxAdObj)
                    return@withContext maxAdObj
                }

                AdNetwork.AD_MAX.value -> {
                    adMax?.removeAdAny(maxAdObj)
                    return@withContext maxAdObj
                }

                else -> return@withContext null
            }
        }
    }

    private fun loadDisplayAdView(
        screen: String,
        callback: IKSdkLoadAdCoreListener
    ) {
        mUiScope.launchWithSupervisorJob {
            withContext(Dispatchers.IO) {
                val filterData = getAdDataInApp(screen)
                if (filterData.isNullOrEmpty()) {
                    IKLogs.d(TAG_LOG) { "preloadAd_ onAdLoadFail_ ${IKSdkErrorCode.NO_DATA_TO_LOAD_AD}" }
                    callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
                    return@withContext
                }

                val latch = CountDownLatch(filterData.size)
                val adLoadedSuccessfully = AtomicBoolean(false)
                filterData.forEach { ad ->
                    when (ad.adNetwork) {
                        AdNetwork.AD_MOB.value,
                        AdNetwork.AD_MAX.value,
                        AdNetwork.AD_MANAGER.value -> {
                            val adLoader = when (ad.adNetwork) {
                                AdNetwork.AD_MOB.value -> admob
                                AdNetwork.AD_MAX.value -> adMax
                                AdNetwork.AD_MANAGER.value -> adGam
                                else -> null
                            }

                            adLoader?.loadAndShowAd(
                                this,
                                ad,
                                object : IKSdkLoadAdCoreListener {
                                    override fun onAdLoaded() {
                                        adLoadedSuccessfully.set(true)
                                        latch.countDown()
                                    }

                                    override fun onAdLoadFail(error: IKAdError) {
                                        adLoadedSuccessfully.set(false)
                                        latch.countDown()
                                    }
                                })
                        }

                        else -> {
                            adLoadedSuccessfully.set(false)
                            latch.countDown()
                        }
                    }
                }
                latch.await()
                if (adLoadedSuccessfully.get()) {
                    callback.onAdLoaded()

                } else launch {
                    loadBackupAd(mUiScope, object : IKSdkLoadAdCoreListener {
                        override fun onAdLoaded() {
                            IKLogs.d(TAG_LOG) { "preloadAd_ loadBackupAd_ onAdLoaded" }
                            callback.onAdLoaded()
                        }

                        override fun onAdLoadFail(error: IKAdError) {
                            IKLogs.d(TAG_LOG) { "preloadAd_ onAdLoadFail_ $error" }
                            callback.onAdLoadFail(error)
                        }
                    })
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun showAdNativeBackupLatest(
        scriptName: String,
        screen: String,
        showAdListener: IKSdkShowWidgetAdListener,
    ) {

        NativeBackUpLatest.getCurrentBackupAd()?.let {
            when (it.adNetwork) {
                AdNetwork.AD_MOB.value -> admob.showAdWithAdObject(
                    it as IKSdkBaseLoadedAd<NativeAd>,
                    scriptName,
                    screen,
                    showAdListener
                )

                AdNetwork.AD_MANAGER.value -> adGam.showAdWithAdObject(
                    it as IKSdkBaseLoadedAd<NativeAd>,
                    scriptName,
                    screen,
                    showAdListener
                )

                AdNetwork.AD_MAX.value -> adMax?.showAdWithAdObject(
                    it as IKSdkBaseLoadedAd<IkObjectNativeMax>,
                    scriptName,
                    screen,
                    showAdListener
                )

                else -> {
                    showAdListener.onAdShowFail(
                        IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER),
                        scriptName,
                        ""
                    )
                }
            }
        }
    }
}