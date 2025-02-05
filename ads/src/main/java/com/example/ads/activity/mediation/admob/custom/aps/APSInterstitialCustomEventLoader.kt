//package com.example.ads.activity.mediation.admob.custom.aps
//
//import android.content.Context
//import com.amazon.aps.ads.ApsAd
//import com.amazon.aps.ads.ApsAdController
//import com.amazon.aps.ads.listeners.ApsAdListener
//import com.amazon.aps.shared.metrics.ApsMetricsPerfEventModelBuilder
//import com.amazon.device.ads.AdRegistration
//import com.amazon.device.ads.DTBAdUtil
//import com.google.android.gms.ads.mediation.MediationAdLoadCallback
//import com.google.android.gms.ads.mediation.MediationInterstitialAd
//import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback
//import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration
//import com.example.ads.activity.mediation.custom.utils.IKCustomEventError
//import com.example.ads.activity.mediation.custom.utils.IKCustomParamParser
//import com.example.ads.activity.utils.IKLogs
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
///** Interstitial custom event loader for the SampleSDK.  */
//class APSInterstitialCustomEventLoader(
//    /** Configuration for requesting the interstitial ad from the third party network.  */
//    private val mediationAdConfiguration: MediationInterstitialAdConfiguration,
//    /** Callback that fires on loading success or failure.  */
//    private val mediationAdLoadCallback: MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback>
//) : MediationInterstitialAd {
//    /** A sample third party SDK interstitial ad.  */
//    private var apsAdController: ApsAdController? = null
//
//    /** Callback for interstitial ad events.  */
//    private var adCallback: MediationInterstitialAdCallback? = null
//
//    /** Loads the interstitial ad from the third party ad network.  */
//    fun loadAd(metricsBuilder: ApsMetricsPerfEventModelBuilder, correlationId: String?) {
//        // All custom events have a server parameter named "parameter" that returns back the parameter
//        // entered into the AdMob UI when defining the custom event.
//        CoroutineScope(Dispatchers.Main).launch {
//            showLogD("Begin loading interstitial ad.")
//
//            val adId = IKCustomParamParser.getAdUnit(mediationAdConfiguration.serverParameters)
//            val pricePoint =
//                IKCustomParamParser.getPricePoint(mediationAdConfiguration.serverParameters)
//            if (adId.isNullOrBlank() || pricePoint.isNullOrBlank()) {
//                mediationAdLoadCallback.onFailure(IKCustomEventError.createCustomEventNoAdIdError())
//                return@launch
//            }
//            val customEventExtras = DTBAdUtil.createAdMobInterstitialRequestBundle(adId)
//            val context = mediationAdConfiguration.context
//            showLogD("start fetching interstitial ad.")
//            if (apsAdController == null)
//                apsAdController = ApsAdController(context, object : ApsAdListener {
//
//                    override fun onAdLoaded(p0: ApsAd?) {
//                        showLogD("Received the interstitial ad.")
//                        adCallback =
//                            mediationAdLoadCallback.onSuccess(this@APSInterstitialCustomEventLoader)
//                    }
//
//                    override fun onAdError(apsAd: ApsAd?) {
//                        super.onAdError(apsAd)
//                    }
//
//                    override fun onAdOpen(apsAd: ApsAd?) {
//                        super.onAdOpen(apsAd)
//                        showLogD("The interstitial ad was shown fullscreen.")
//                        adCallback?.onAdOpened()
//                    }
//
//                    override fun onAdClosed(apsAd: ApsAd?) {
//                        super.onAdClosed(apsAd)
//                        showLogD("The interstitial ad was closed.")
//                        adCallback?.onAdClosed()
//                        adCallback = null
//                    }
//
//                    override fun onVideoCompleted(apsAd: ApsAd?) {
//                        super.onVideoCompleted(apsAd)
//                    }
//
//                    override fun onAdFailedToLoad(p0: ApsAd?) {
//                        showLogE("Failed to fetch the interstitial ad.")
//                        mediationAdLoadCallback.onFailure(
//                            com.google.android.gms.ads.AdError(
//                                3,
//                                "Custom interstitial ad failed to load",
//                                "com.amazon.device.ads"
//                            )
//                        )
//                    }
//
//                    override fun onAdClicked(p0: ApsAd?) {
//                        adCallback?.reportAdClicked()
//                    }
//
//                    override fun onImpressionFired(p0: ApsAd?) {
//                        adCallback?.reportAdImpression()
//                    }
//                })
//            val requestId = customEventExtras.getString("amazon_custom_event_request_id")
//            val dtbCacheData = AdRegistration.getAdMobCache(requestId)
//            if (dtbCacheData != null) {
//                if (dtbCacheData.isBidRequestFailed) {
//
//                    mediationAdLoadCallback.onFailure(
//                        com.google.android.gms.ads.AdError(
//                            3,
//                            "Fail to load custom interstitial ad in loadAd because previous bid requests failure",
//                            "com.amazon.device.ads"
//                        )
//                    )
//                    return@launch
//                }
//                val apsAd: ApsAd? = dtbCacheData.adResponse as? ApsAd
//                if (apsAd != null && apsAdController != null) {
//                    APSAdMobUtil.renderAPSInterstitialAds(
//                        apsAd,
//                        apsAdController!!,
//                        mediationAdLoadCallback,
//                        pricePoint,
//                        requestId
//                    )
//                    return@launch
//                }
//            }
//            if (apsAdController != null) {
//                APSAdMobUtil.loadInterstitialAd(
//                    apsAdController!!,
//                    mediationAdLoadCallback,
//                    customEventExtras,
//                    pricePoint,
//                    metricsBuilder,
//                    correlationId
//                )
//            } else mediationAdLoadCallback.onFailure(IKCustomEventError.createCustomEventAdNotAvailableError())
//
//        }
//    }
//
//    override fun showAd(context: Context) {
//        showLogD("The interstitial ad was shown.")
//        if (apsAdController != null && apsAdController?.isAdAvailable == true) {
//            apsAdController?.show()
//        } else {
//            adCallback?.onAdFailedToShow(IKCustomEventError.createCustomEventAdNotAvailableError())
//        }
//    }
//
//    companion object {
//        /** Tag used for log statements  */
//        private const val TAG = "APSInterCustomEvent"
//    }
//
//    fun showLogD(message: String) {
//        IKLogs.d(TAG) { message }
//    }
//
//    fun showLogE(message: String) {
//        IKLogs.e(TAG) { message }
//    }
//}
