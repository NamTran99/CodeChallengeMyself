//package com.example.ads.activity.mediation.admob.custom.dte
//
//import android.app.Activity
//import android.content.Context
//import androidx.annotation.Keep
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
//import com.example.ads.activity.data.dto.pub.IKAdError
//import com.example.ads.activity.mediation.custom.IKCustomEventInitListener
//import com.example.ads.activity.mediation.custom.fairbid.IKFairBid
//import com.example.ads.activity.mediation.custom.utils.IKCustomParamParser
//import com.example.ads.activity.mediation.custom.utils.IKInitializationStatus
//import com.example.ads.activity.utils.IkmSdkCoreFunc
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//@Keep
//class DTECustomEvent : Adapter() {
//    private var bannerLoader: DTEBannerCustomEventLoader? = null
//    private var interstitialLoader: DTEInterstitialCustomEventLoader? = null
//    override fun loadBannerAd(
//        adConfiguration: MediationBannerAdConfiguration,
//        callback: MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback>
//    ) {
//        CoroutineScope(Dispatchers.Main).launch {
//            if (status != IKInitializationStatus.INITIALIZED_SUCCESS) {
//                status = IKInitializationStatus.INITIALIZING
//
//                var act = adConfiguration.context as? Activity
//                if (act == null) {
//                    act =
//                        IkmSdkCoreFunc.AppF.listActivity.filter { it.value != null }.values.firstOrNull()
//                    if (act == null) {
//                        status = IKInitializationStatus.INITIALIZED_FAILURE
//                        return@launch
//                    }
//                }
//
//                IKCustomParamParser.getAppKey(adConfiguration)?.let {
//                    runCatching {
//                        IKFairBid.initialize(
//                            act,
//                            it,
//                            null,
//                            object : IKCustomEventInitListener {
//                                override fun onSuccess() {
//                                }
//
//                                override fun onFail(error: IKAdError) {
//                                }
//                            })
//
//                    }
//                }
//            }
//            bannerLoader = DTEBannerCustomEventLoader(adConfiguration, callback)
//            bannerLoader?.loadAd()
//        }
//    }
//
//    override fun loadInterstitialAd(
//        adConfiguration: MediationInterstitialAdConfiguration,
//        callback: MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback>
//    ) {
//        CoroutineScope(Dispatchers.Main).launch {
//            if (status != IKInitializationStatus.INITIALIZED_SUCCESS) {
//                status = IKInitializationStatus.INITIALIZING
//
//                var act = adConfiguration.context as? Activity
//                if (act == null) {
//                    act =
//                        IkmSdkCoreFunc.AppF.listActivity.filter { it.value != null }.values.firstOrNull()
//                    if (act == null) {
//                        status = IKInitializationStatus.INITIALIZED_FAILURE
//                        return@launch
//                    }
//                }
//                IKCustomParamParser.getAppKey(adConfiguration)?.let {
//                    runCatching {
//                        IKFairBid.initialize(
//                            act,
//                            it,
//                            null,
//                            object : IKCustomEventInitListener {
//                                override fun onSuccess() {
//                                }
//
//                                override fun onFail(error: IKAdError) {
//                                }
//                            })
//
//                    }
//                }
//            }
//
//            interstitialLoader = DTEInterstitialCustomEventLoader(adConfiguration, callback)
//            interstitialLoader?.loadAd()
//        }
//    }
//
//    override fun initialize(
//        context: Context,
//        initializationCompleteCallback: InitializationCompleteCallback,
//        list: List<MediationConfiguration>
//    ) {
//        if (status == IKInitializationStatus.INITIALIZED_SUCCESS) {
//            initializationCompleteCallback.onInitializationSucceeded()
//            return
//        }
//        status = IKInitializationStatus.INITIALIZING
//        CoroutineScope(Dispatchers.Main).launch {
//            var act = context as? Activity
//            if (act == null) {
//                act =
//                    IkmSdkCoreFunc.AppF.listActivity.filter { it.value != null }.values.firstOrNull()
//                if (act == null) {
//                    status = IKInitializationStatus.INITIALIZED_FAILURE
//                    initializationCompleteCallback.onInitializationFailed("not have activity")
//                    return@launch
//                }
//            }
//            IKCustomParamParser.parseParam(list)
//                .forEach {
//                    IKFairBid.initialize(
//                        act,
//                        it,
//                        null,
//                        object : IKCustomEventInitListener {
//                            override fun onSuccess() {
//                            }
//
//                            override fun onFail(error: IKAdError) {
//                            }
//                        })
//                }
//        }
//    }
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
//        protected val TAG = DTECustomEvent::class.java.simpleName
//        private var status: IKInitializationStatus? = null
//    }
//}
