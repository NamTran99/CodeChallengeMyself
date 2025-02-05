package com.example.ads.activity.mediation.applovin

import android.app.Activity
import android.content.Context
//import com.amazon.device.ads.AdError
//import com.amazon.device.ads.AdRegistration
//import com.amazon.device.ads.DTBAdCallback
//import com.amazon.device.ads.DTBAdNetwork
//import com.amazon.device.ads.DTBAdNetworkInfo
//import com.amazon.device.ads.DTBAdRequest
//import com.amazon.device.ads.DTBAdResponse
//import com.amazon.device.ads.DTBAdSize
import com.applovin.mediation.ads.MaxAdView
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.applovin.sdk.AppLovinSdkUtils
//import com.fyber.FairBid
import com.google.gson.reflect.TypeToken
//import com.ikame.android.datasourceadapter.IKDataSourceHelper
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.data.dto.sdk.IKMAXConfig
import com.example.ads.activity.data.local.IKSdkDataStore
import com.example.ads.activity.data.local.IKSdkDataStoreConst
import com.example.ads.activity.utils.IKLogs
import com.example.ads.activity.utils.IKSdkDefConst
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import sg.bigo.ads.BigoAdSdk
//import sg.bigo.ads.api.AdConfig
import java.util.concurrent.atomic.AtomicBoolean

object IKApplovinHelper {
    private var awsConfig: List<IKMAXConfig>? = null
    private var APS_CONFIG = "aps"
    private var DTE_CONFIG = "dte"
    private var BIGO_CONFIG = "bigo"
    var initStatus = false
        private set
    private var isFairBidInit = false
    private val isInitializing = AtomicBoolean()
    private const val TAG = "IKApplovinHelper"
    fun initialize(context: Context) {
        if (initStatus)
            return
        if (!isInitializing.compareAndSet(false, true))
            return
        if (!hasLib())
            return
//        kotlin.runCatching {
//            IKLogs.dNone(TAG) { "Applovin start init" }
//            val initConfig = AppLovinSdkInitializationConfiguration.builder(
//                IKDataSourceHelper.getApplovinKey(),
//                context.applicationContext
//            )
//                .setMediationProvider(AppLovinMediationProvider.MAX)
//                .build()
//            AppLovinSdk.getInstance(context.applicationContext)
//                .initialize(initConfig) {
//                    initStatus = true
//                    isInitializing.set(false)
//                    IKLogs.dNone(TAG) { "Applovin initialized" }
//                }
//            intMAXConfig(context.applicationContext)
//        }.onFailure {
//            IKLogs.dNone(TAG) { "Applovin init fail:${it.message}" }
//        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(30000)
            isInitializing.set(false)
        }
    }

    fun hasLib(): Boolean {
        return kotlin.runCatching {
            Class.forName(com.applovin.sdk.AppLovinSdk::class.java.name)
            IKLogs.d(TAG) { "Applovin has available" }
            true
        }.onFailure {
            IKLogs.d(TAG) { "Applovin not available" }
        }.getOrDefault(false)
    }

    fun isInitialized(): Boolean {
        if (initStatus)
            return true
        var isInit = false
        kotlin.runCatching {
            Class.forName(com.applovin.sdk.AppLovinSdk::class.java.name)
            IKLogs.d(TAG) { "Applovin has Init" }
            isInit = true
        }.onFailure {
            IKLogs.d(TAG) { "Applovin not Init" }
            isInit = false
        }
        return isInit
    }

    fun isInitAws(): Boolean {
        var isInit = false
        kotlin.runCatching {
//            Class.forName(AdRegistration::class.java.name)
            IKLogs.d(TAG) { "AdRegistration has Init" }
            isInit = true
        }.onFailure {
            IKLogs.d(TAG) { "AdRegistration not Init" }
            isInit = false
        }
        return isInit
    }

    private fun intMAXConfig(context: Context) {
        if (!isInitAws())
            return
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                runCatching {
                    awsConfig = SDKDataHolder.getObjectSdk<List<IKMAXConfig>>(
                        IKSdkDataStore.getString(IKSdkDataStoreConst.AWS_MEDIATION_CONFIG, ""),
                        object : TypeToken<List<IKMAXConfig>>() {}.type
                    )
                }
            }
            runCatching {
                awsConfig?.forEach {
                    when (it.name) {
                        APS_CONFIG -> {
                            kotlin.runCatching {
                                it.appKey?.let { it1 ->
//                                    AdRegistration.getInstance(it1, context)
                                }
//                                AdRegistration.setAdNetworkInfo(DTBAdNetworkInfo(DTBAdNetwork.MAX))
                            }
                        }

                        BIGO_CONFIG -> {
                            kotlin.runCatching {
//                                val adConfigBuilder: AdConfig.Builder = AdConfig.Builder()

                                it.appKey?.let { it1 ->
//                                    adConfigBuilder.setAppId(it1.trim()) // APP ID(required)
                                }

//                                val adConfig: AdConfig = adConfigBuilder.build()

//                                val initListener: BigoAdSdk.InitListener = BigoAdSdk.InitListener {
//                                     callback of initialization
//                                }
//                                BigoAdSdk.initialize(
//                                    context,
//                                    adConfig,
//                                    initListener
//                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun initDte(activity: Activity) {
        if (!initStatus)
            return
        if (isFairBidInit)
            return
        CoroutineScope(Dispatchers.Main).launch {
            if (awsConfig == null)
                withContext(Dispatchers.Default) {
                    runCatching {
                        awsConfig = SDKDataHolder.getObjectSdk<List<IKMAXConfig>>(
                            IKSdkDataStore.getString(IKSdkDataStoreConst.AWS_MEDIATION_CONFIG, ""),
                            object : TypeToken<List<IKMAXConfig>>() {}.type
                        )
                    }
                }
            runCatching {
                awsConfig?.find { it.name == DTE_CONFIG }?.let {
                    if (!it.appKey.isNullOrBlank()) {
                        isFairBidInit = true
//                        FairBid.start(it.appKey, activity)
                    }
                }
            }
        }
    }

//    private var bannerAwsResponse: DTBAdResponse? = null
//    var bannerAwsError: AdError? = null
//        private set
//    private var interAwsResponse: DTBAdResponse? = null
//    var interAwsError: AdError? = null
//        private set
//    private var rewardAwsResponse: DTBAdResponse? = null
//    var rewardAwsError: AdError? = null
//        private set
//
//    suspend fun getBannerAws(rawSize: AppLovinSdkUtils.Size): DTBAdResponse? {
//        if (bannerAwsResponse != null) {
//            coroutineScope {
//                launch {
//                    initBannerAws(rawSize)
//                }
//            }
//            return bannerAwsResponse
//        } else initBannerAws(rawSize)
//        return null
//    }

//    suspend fun getInterAws(): DTBAdResponse? {
//        if (interAwsResponse != null) {
//            coroutineScope {
//                launch {
//                    initInterAws()
//                }
//            }
//            return interAwsResponse
//        } else initInterAws()
//        return null
//    }

//    suspend fun getRewardAws(): DTBAdResponse? {
//        if (rewardAwsResponse != null) {
//            coroutineScope {
//                launch {
//                    initRewardAws()
//                }
//            }
//            return rewardAwsResponse
//        } else initRewardAws()
//        return null
//    }
//
//    private suspend fun initBannerAws(rawSize: AppLovinSdkUtils.Size) {
//        withContext(Dispatchers.IO) {
//            if (!isInitAws())
//                return@withContext
//            if (awsConfig?.find { it.name == APS_CONFIG }?.banner.isNullOrBlank())
//                return@withContext
//            val adLoader = DTBAdRequest()
//            val size = DTBAdSize(
//                rawSize.width,
//                rawSize.height,
//                awsConfig?.find { it.name == APS_CONFIG }?.banner ?: IKSdkDefConst.EMPTY
//            )
//            adLoader.setSizes(size)
//            adLoader.loadAd(object : DTBAdCallback {
//                override fun onSuccess(dtbAdResponse: DTBAdResponse) {
//                    bannerAwsResponse = dtbAdResponse
//
//                }
//
//                override fun onFailure(adError: AdError) {
//                    bannerAwsError = adError
//                }
//            })
//        }
//    }
//
//    private suspend fun initInterAws() {
//        withContext(Dispatchers.IO) {
//            if (!isInitAws())
//                return@withContext
//            if (awsConfig?.find { it.name == APS_CONFIG }?.inter.isNullOrBlank())
//                return@withContext
//            val adLoader = DTBAdRequest()
//            adLoader.setSizes(
//                DTBAdSize.DTBInterstitialAdSize(
//                    awsConfig?.find { it.name == APS_CONFIG }?.inter ?: IKSdkDefConst.EMPTY
//                )
//            )
//            adLoader.loadAd(object : DTBAdCallback {
//                override fun onSuccess(dtbAdResponse: DTBAdResponse) {
//                    interAwsResponse = dtbAdResponse
//
//                }
//
//                override fun onFailure(adError: AdError) {
//                    interAwsError = adError
//                }
//            })
//        }
//    }
//
//    private suspend fun initRewardAws() {
//        withContext(Dispatchers.IO) {
//            if (!isInitAws())
//                return@withContext
//            if (awsConfig?.find { it.name == APS_CONFIG }?.reward.isNullOrBlank())
//                return@withContext
//            val adLoader = DTBAdRequest()
//            adLoader.setSizes(
//                DTBAdSize.DTBVideo(
//                    320,
//                    480,
//                    awsConfig?.find { it.name == APS_CONFIG }?.reward ?: IKSdkDefConst.EMPTY
//                )
//            )
//            adLoader.loadAd(object : DTBAdCallback {
//                override fun onSuccess(dtbAdResponse: DTBAdResponse) {
//                    rewardAwsResponse = dtbAdResponse
//
//                }
//
//                override fun onFailure(adError: AdError) {
//                    rewardAwsError = adError
//                }
//            })
//        }
//    }
//
//    fun showDebug(activity: Activity){
//        AppLovinSdk.getInstance(activity).showMediationDebugger()
//    }
//
//
//    fun isBannerAd(loadedAd: Any?): Boolean {
//        return kotlin.runCatching { loadedAd is MaxAdView }.getOrNull() == true
//    }
//
//    fun isNativeAd(loadedAd: Any?): Boolean {
//        return kotlin.runCatching { loadedAd is IkObjectNativeMax }.getOrNull() == true
//    }
}