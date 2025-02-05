//package com.example.ads.activity.mediation.admob.custom.aps
//
//import android.content.Context
//import androidx.annotation.Keep
//import com.amazon.aps.shared.APSAnalytics
//import com.amazon.aps.shared.analytics.APSEventSeverity
//import com.amazon.aps.shared.analytics.APSEventType
//import com.amazon.aps.shared.metrics.ApsMetricsPerfEventModelBuilder
//import com.amazon.aps.shared.metrics.model.ApsMetricsResult
//import com.amazon.device.ads.AdRegistration
//import com.amazon.device.ads.DTBAdNetwork
//import com.amazon.device.ads.DTBAdNetworkInfo
//import com.amazon.device.ads.DTBAdRequest
//import com.amazon.device.ads.DTBAdSize
//import com.google.android.gms.ads.VersionInfo
//import com.google.android.gms.ads.mediation.Adapter
//import com.google.android.gms.ads.mediation.InitializationCompleteCallback
//import com.google.android.gms.ads.mediation.MediationAdLoadCallback
//import com.google.android.gms.ads.mediation.MediationBannerAd
//import com.google.android.gms.ads.mediation.MediationBannerAdCallback
//import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration
//import com.google.android.gms.ads.mediation.MediationConfiguration
//import com.google.android.gms.ads.mediation.MediationInterstitialAd
//import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback
//import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration
//import com.example.ads.activity.mediation.custom.utils.IKCustomParamParser
//import com.example.ads.activity.mediation.custom.utils.IKInitializationStatus
//import com.example.ads.activity.utils.IKLogs
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import java.util.UUID
//
//@Keep
//open class APSCustomEvent : Adapter() {
//    private var bannerLoader: APSBannerCustomEventLoader? = null
//    private var interstitialLoader: APSInterstitialCustomEventLoader? = null
//    override fun loadBannerAd(
//        adConfiguration: MediationBannerAdConfiguration,
//        callback: MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback>
//    ) {
//        CoroutineScope(Dispatchers.Main).launch {
//            if (status != IKInitializationStatus.INITIALIZED_SUCCESS) {
//                runCatching {
//                    IKCustomParamParser.getAppKey(adConfiguration)?.let {
//                        status = IKInitializationStatus.INITIALIZED_SUCCESS
//                        AdRegistration.getInstance(it, adConfiguration.context)
//                    }
//                }
//            }
//
//            IKLogs.d(TAG) { "start loadBannerAd" }
//            var metricsResult = ApsMetricsResult.Success
//            val metricsBuilder = ApsMetricsPerfEventModelBuilder()
//            val correlationId = UUID.randomUUID().toString()
//
//            runCatching {
//                bannerLoader = APSBannerCustomEventLoader(adConfiguration, callback)
//                metricsBuilder.withAdapterStartTime(System.currentTimeMillis())
//                bannerLoader?.loadAd(metricsBuilder, correlationId)
//            }.onFailure {
//                metricsResult = ApsMetricsResult.Failure
//                APSAnalytics.logEvent(
//                    APSEventSeverity.FATAL,
//                    APSEventType.EXCEPTION,
//                    "Fail to execute loadBannerAd method during runtime in APSAdMobCustomEvent class",
//                    it as? Exception?
//                )
//            }
//            APSAdMobUtil.captureAdapterEndEvent(metricsResult, metricsBuilder, correlationId)
//        }
//    }
//
//    override fun loadInterstitialAd(
//        adConfiguration: MediationInterstitialAdConfiguration,
//        callback: MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback>
//    ) {
//        IKLogs.d(TAG) { "start loadInterstitialAd" }
//        CoroutineScope(Dispatchers.Main).launch {
//            if (status != IKInitializationStatus.INITIALIZED_SUCCESS) {
//                runCatching {
//                    IKCustomParamParser.getAppKey(adConfiguration)?.let {
//                        status = IKInitializationStatus.INITIALIZED_SUCCESS
//                        AdRegistration.getInstance(it, adConfiguration.context)
//                        AdRegistration.setAdNetworkInfo(DTBAdNetworkInfo(DTBAdNetwork.ADMOB))
//                    }
//                }
//            }
//
//            var metricsResult = ApsMetricsResult.Success
//            val metricsBuilder = ApsMetricsPerfEventModelBuilder()
//            var correlationId = UUID.randomUUID().toString()
//            try {
//                metricsBuilder.withAdapterStartTime(System.currentTimeMillis())
//                correlationId = UUID.randomUUID().toString()
//                interstitialLoader = APSInterstitialCustomEventLoader(adConfiguration, callback)
//                interstitialLoader?.loadAd(metricsBuilder, correlationId)
//            } catch (e: RuntimeException) {
//                metricsResult = ApsMetricsResult.Failure
//                APSAnalytics.logEvent(
//                    APSEventSeverity.FATAL,
//                    APSEventType.EXCEPTION,
//                    "Fail to execute loadInterstitialAd method during runtime in APSAdMobCustomEvent class",
//                    e
//                )
//            }
//            APSAdMobUtil.captureAdapterEndEvent(metricsResult, metricsBuilder, correlationId)
//
//        }
//    }
//
//    // This method won't be called for custom events.
//    override fun initialize(
//        context: Context,
//        initializationCompleteCallback: InitializationCompleteCallback,
//        list: List<MediationConfiguration>
//    ) {
//        try {
//            initializationCompleteCallback.onInitializationSucceeded()
//            APSAdMobAdapterUtil.setupMetricsAndRemoteLogs()
//        } catch (e: RuntimeException) {
//            APSAnalytics.logEvent(
//                APSEventSeverity.FATAL,
//                APSEventType.EXCEPTION,
//                "Fail to execute initialize method during runtime in APSAdMobCustomEvent class",
//                e
//            )
//        }
//        if (status == IKInitializationStatus.INITIALIZED_SUCCESS) {
//            initializationCompleteCallback.onInitializationSucceeded()
//            return
//        }
//        status = IKInitializationStatus.INITIALIZING
//        CoroutineScope(Dispatchers.Main).launch {
//            IKCustomParamParser.parseParam(list)
//                .forEach {
//                    AdRegistration.getInstance(it, context)
//                }
//        }
//
//    }
//
//
//    override fun getVersionInfo(): VersionInfo {
////        val versionString: String = BuildConfig.ADAPTER_VERSION
////        val splits =
////            versionString.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
////        if (splits.size >= 4) {
////            val major = splits[0].toInt()
////            val minor = splits[1].toInt()
////            val micro = splits[2].toInt() * 100 + splits[3].toInt()
////            return VersionInfo(major, minor, micro)
////        }
//        return VersionInfo(1, 0, 0)
//    }
//
//    override fun getSDKVersionInfo(): VersionInfo {
////        val versionString: String = SampleAdRequest.getSDKVersion()
////        val splits =
////            versionString.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
////        if (splits.size >= 3) {
////            val major = splits[0].toInt()
////            val minor = splits[1].toInt()
////            val micro = splits[2].toInt()
////            return VersionInfo(major, minor, micro)
////        }
//        return VersionInfo(1, 0, 0)
//    }
//
//    companion object {
//        protected val TAG = "APSCustomEvent"
//
//        private var status: IKInitializationStatus? = null
//
//        fun createSampleRequest(
//            adSize: DTBAdSize
//        ): DTBAdRequest {
//            val request = DTBAdRequest()
//            request.setSizes(adSize)
//            return request
//        }
//    }
//}
