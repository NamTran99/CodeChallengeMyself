package com.example.ads.activity.format.banner

import com.example.ads.activity.core.IKDataCoreManager
import com.example.ads.activity.data.db.IKDataRepository
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.pub.IKAdFormat
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkBaseDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdWidgetDetailDto
import com.example.ads.activity.format.base.IKSdkBaseAd
import com.example.ads.activity.format.base.IKSdkBaseAdController
import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
import com.example.ads.activity.mediation.admob.BannerCollapseAdmob
import com.example.ads.activity.mediation.gam.BannerCollapseGam
import com.example.ads.activity.utils.IKLogs
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import com.example.ads.activity.utils.IKSdkUtilsCore
import com.example.ads.activity.utils.IkmSdkCoreFunc
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


object IKBannerCollapseController : IKSdkBaseAdController() {
    private const val TAG_LOG = "IKBannerCollapseC"
    override val adMax: IKSdkBaseAd<*>?
        get() = null

    override val admob: BannerCollapseAdmob by lazy {
        BannerCollapseAdmob()
    }

    override val adGam: BannerCollapseGam by lazy {
        BannerCollapseGam()
    }

    override val adFairBid: IKSdkBaseAd<*>?
        get() = null

    override val adIK: IKSdkBaseAd<*>?
        get() = null

    override suspend fun getBackupAd(): IKAdapterDto? = IKDataCoreManager.getOtherBannerCollapseAds()
    override val adFormat: IKAdFormat = IKAdFormat.BANNER_COLLAPSE

    private suspend fun loadAndShowForNetwork(
        itemAds: IKSdkProdWidgetDetailDto,
        screen: String,
        adNetwork: String?,
        dto: IKAdUnitDto,
        callback: IKSdkShowWidgetAdListener
    ): Boolean {
        IKLogs.d(TAG_LOG) { "loadAndShowForNetwork" }
        val scriptName = ""
        return when (adNetwork) {
            AdNetwork.AD_MOB.value -> {
                admob.loadAndShowUnitForNetwork(
                    itemAds,
                    screen,
                    dto,
                    callback
                )
            }

            AdNetwork.AD_MANAGER.value -> {
                adGam.loadAndShowUnitForNetwork(
                    itemAds,
                    screen,
                    dto,
                    callback
                )
            }

            else -> {
                callback.onAdShowFail(
                    IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD),
                    scriptName,
                    IKSdkDefConst.UNKNOWN
                )
                IKLogs.d(TAG_LOG) { "loadAdForNetwork_ onAdShowFail_ ${IKSdkErrorCode.NO_DATA_TO_LOAD_AD}" }
                false
            }
        }
    }

    private suspend fun IKSdkBaseBannerAd<*>.loadAndShowUnitForNetwork(
        itemAds: IKSdkProdWidgetDetailDto,
        screen: String,
        dto: IKAdUnitDto,
        callback: IKSdkShowWidgetAdListener
    ): Boolean {
        val result = CompletableDeferred<Boolean>()
        loadAndShowCollapsibleAd(
            dto,
            itemAds,
            screen,
            object : IKSdkShowWidgetAdListener {
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

    fun showAd(
        screen: String,
        itemAds: IKSdkProdWidgetDetailDto,
        adListener: IKSdkShowWidgetAdListener,
        isRecall: Boolean = false,
        canRetry: Boolean = true
    ) {
        IKLogs.d(TAG_LOG) { "showAdsCollapse" }
        val scriptName = ""
        mUiScope.launchWithSupervisorJob(Dispatchers.IO) {
            if (!IKSdkUtilsCore.canLoadAdAsync()) {
                adListener.onAdShowFail(
                    IKAdError(IKSdkErrorCode.USER_PREMIUM),
                    scriptName,
                    IKSdkDefConst.UNKNOWN
                )
                IKLogs.d(TAG_LOG) { "showAdsCollapse_ onAdShowFail_ ${IKSdkErrorCode.USER_PREMIUM}" }
                return@launchWithSupervisorJob
            }

            IKLogs.d(TAG_LOG) { "showAdsCollapse_ enable=${itemAds.enable} " }

            val showAdListener = object : IKSdkShowWidgetAdListener {
                override fun onAdReady(
                    adData: IKSdkBaseLoadedAd<*>,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    IKLogs.d(TAG_LOG) { "showAdsCollapse_ onAdReady" }
                    adListener.onAdReady(adData, scriptName, adNetworkName)
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
                    IKLogs.d(TAG_LOG) { "showAdsCollapse_ onAdShowFail_ $error" }
                    loadAndShowBackupAd(
                        screen,
                        itemAds,
                        error,
                        adListener
                    )
                }

                override fun onAdClick(scriptName: String, adNetworkName: String) {
                    adListener.onAdClick(scriptName, adNetworkName)
                }

                override fun onAdImpression(scriptName: String, adNetworkName: String) {
                    adListener.onAdImpression(scriptName, adNetworkName)
                }
            }

            val adDto = getAdDto()

            val sortedAdapters = adDto?.adapters
                ?.filter {
                    it.adNetwork in listOf(
                        AdNetwork.AD_MANAGER.value,
                        AdNetwork.AD_MOB.value
                    )
                }

            if (sortedAdapters.isNullOrEmpty()) {
                showAdListener.onAdShowFail(
                    IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER),
                    scriptName,
                    IKSdkDefConst.UNKNOWN
                )
                IKLogs.d(TAG_LOG) { "showAdsCollapse_ onAdShowFail_ ${IKSdkErrorCode.NO_AD_FROM_SERVER}" }
                return@launchWithSupervisorJob
            }
            val sortedArray = withContext(Dispatchers.Default) {
                val arrayList = ArrayList<Triple<String, Int, IKAdUnitDto>>()
                sortedAdapters.forEach { s ->
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
                showAdListener.onAdShowFail(
                    IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER),
                    scriptName,
                    IKSdkDefConst.UNKNOWN
                )
                IKLogs.d(TAG_LOG) { "showAdsCollapse_ onAdShowFail_ ${IKSdkErrorCode.NO_AD_FROM_SERVER}" }
                return@launchWithSupervisorJob
            }
            var adLoaded = false
            for (adapter in sortedArray) {
                val success =
                    loadAndShowForNetwork(itemAds,
                        screen,
                        adapter.first,
                        adapter.third,
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
                                showAdListener.onAdShowFail(error, scriptName, adNetworkName)
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
                        }
                    )
                if (success) {
                    adLoaded = true
                    break
                }
            }
            if (!adLoaded) {
                showAdListener.onAdShowFail(
                    IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER),
                    scriptName,
                    IKSdkDefConst.UNKNOWN
                )
                IKLogs.d(TAG_LOG) { "showAdsCollapse_ onAdShowFail_ ${IKSdkErrorCode.NO_AD_FROM_SERVER}" }
            }
        }

    }

    override suspend fun getAdDto(): IKSdkBaseDto? {
        repeat(4) {
            IKDataRepository.getInstance().getSDKBannerCollapse()?.let {
                mAdDto = it
                return it
            }
            delay(500)
        }
        return mAdDto
    }

    private fun loadAndShowBackupAd(
        screen: String,
        itemAds: IKSdkProdWidgetDetailDto,
        error: IKAdError,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
        IKLogs.d(TAG_LOG) { "loadAndShowBackupAd" }
        val scriptName = ""
        if (itemAds.enableBackup == false) {
            showAdListener.onAdShowFail(error, scriptName, IKSdkDefConst.UNKNOWN)
            IKLogs.d(TAG_LOG) { "loadAndShowBackupAd_ onAdShowFail_ disable backup" }
            return
        }

        mUiScope.launchWithSupervisorJob {
            val otherObject = getBackupAd()
            if (otherObject == null || !otherObject.enable) {
                showAdListener.onAdShowFail(error, scriptName, IKSdkDefConst.UNKNOWN)
                IKLogs.d(TAG_LOG) { "loadAndShowBackupAd_ onAdShowFail " }
                return@launchWithSupervisorJob
            }

            var otherAdsDto =
                otherObject.adData?.firstOrNull { it.label != IKSdkDefConst.AdLabel.NFX }

            if (IkmSdkCoreFunc.HandleEna.onHandleFx) {
                 otherAdsDto = otherObject.adData?.find { it.label == IKSdkDefConst.AdLabel.NFX }
            }

            if (otherAdsDto == null) {
                showAdListener.onAdShowFail(error, scriptName, IKSdkDefConst.UNKNOWN)
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

                else -> null
            }

            if (adLoader == null) {
                showAdListener.onAdShowFail(error, scriptName, IKSdkDefConst.UNKNOWN)
                IKLogs.d(TAG_LOG) { "loadAndShowBackupAd_ onAdShowFail_ network invalid" }
                return@launchWithSupervisorJob
            }
            adLoader.loadAndShowCollapsibleAd(
                otherAdsDto,
                itemAds,
                screen,
                showAdListener
            )
        }
    }

    private fun showLogSdk(tag: String, message: () -> String) {
        IKLogs.dSdk(TAG_LOG) {
            "${tag}:" + message.invoke()
        }
    }

    private fun showLogD(tag: String, message: () -> String) {
        IKLogs.dSdk(TAG_LOG) {
            "${tag}:" + message.invoke()
        }
    }
}