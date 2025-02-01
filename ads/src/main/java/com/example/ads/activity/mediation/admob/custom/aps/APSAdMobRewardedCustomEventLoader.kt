package com.example.ads.activity.mediation.admob.custom.aps//package com.bmik.android.sdk.mediation.admob.custom.aps
//
//import android.content.Context
//import android.util.Log
//import com.amazon.aps.ads.ApsAd
//import com.amazon.aps.ads.ApsLog
//import com.amazon.aps.ads.listeners.ApsAdListener
//import com.amazon.aps.shared.APSAnalytics
//import com.amazon.aps.shared.analytics.APSEventSeverity
//import com.amazon.aps.shared.analytics.APSEventType
//import com.amazon.aps.shared.metrics.ApsMetricsPerfEventModelBuilder
//import com.amazon.device.ads.AdRegistration
//import com.google.android.gms.ads.AdError
//import com.google.android.gms.ads.mediation.MediationAdLoadCallback
//import com.google.android.gms.ads.mediation.MediationRewardedAd
//import com.google.android.gms.ads.mediation.MediationRewardedAdCallback
//import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration
//import com.google.android.gms.ads.rewarded.RewardItem
//
//internal class APSAdMobRewardedCustomEventLoader /* JADX INFO: Access modifiers changed from: package-private */(
//    private val mediationRewardedAdConfiguration: MediationRewardedAdConfiguration,
//    private val mediationAdLoadCallback: MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback>?
//) : MediationRewardedAd, ApsAdListener {
//    private val apsAdMobUtil = APSAdMobUtil()
//    private var mediationRewardedAdCallback: MediationRewardedAdCallback? = null
//
//    /* JADX INFO: Access modifiers changed from: package-private */
//    fun loadAd(metricsBuilder: ApsMetricsPerfEventModelBuilder, correlationId: String?) {
//        try {
//            val customEventListenerAdapter = CustomEventListenerAdapter(mediationAdLoadCallback)
//            val customEventExtras = mediationRewardedAdConfiguration.mediationExtras
//            val context = mediationRewardedAdConfiguration.context
//            val serverParameter =
//                mediationRewardedAdConfiguration.serverParameters.getString("parameter")
//            if (customEventExtras.containsKey("amazon_custom_event_adapter_version") && customEventExtras.getString(
//                    "amazon_custom_event_adapter_version",
//                    "1.0"
//                ) == "2.0"
//            ) {
//                val requestId = customEventExtras.getString("amazon_custom_event_request_id")
//                val dtbCacheData = AdRegistration.getAdMobCache(requestId)
//                if (dtbCacheData != null) {
//                    if (dtbCacheData.isBidRequestFailed) {
//                        ApsLog.e(
//                            LOGTAG,
//                            "Fail to load custom interstitial ad in loadAd because previous bid requests failure"
//                        )
//                        mediationAdLoadCallback!!.onFailure(
//                            AdError(
//                                3,
//                                "Fail to load custom banner ad in loadAd because previous bid requests failure",
//                                "com.amazon.device.ads"
//                            )
//                        )
//                        return
//                    }
//                    val apsAd: ApsAd? = dtbCacheData.adResponse
//                    if (apsAd != null) {
//                        apsAdMobUtil.renderAPSInterstitialAds(
//                            apsAd,
//                            context,
//                            customEventListenerAdapter,
//                            serverParameter,
//                            requestId,
//                            this,
//                            metricsBuilder,
//                            correlationId
//                        )
//                        return
//                    }
//                }
//                apsAdMobUtil.loadInterstitialAd(
//                    context,
//                    customEventListenerAdapter,
//                    customEventExtras,
//                    serverParameter,
//                    this,
//                    metricsBuilder,
//                    correlationId
//                )
//            } else {
//                Log.d(
//                    LOGTAG,
//                    "Please upgrade to APS API since we don't support Rewarded video through DTB API!"
//                )
//                mediationAdLoadCallback!!.onFailure(
//                    AdError(
//                        3,
//                        "Please upgrade to APS API since we don't support Rewarded video through DTB API!",
//                        "com.amazon.device.ads"
//                    )
//                )
//            }
//        } catch (e: RuntimeException) {
//            APSAnalytics.logEvent(
//                APSEventSeverity.FATAL,
//                APSEventType.EXCEPTION,
//                "Fail to execute loadAd method during runtime in APSAdMobRewardedCustomEventLoader class",
//                e
//            )
//            mediationAdLoadCallback!!.onFailure(
//                AdError(
//                    3,
//                    "Fail to load custom interstitial ad in loadAd method",
//                    "com.amazon.device.ads"
//                )
//            )
//        }
//    }
//
//    override fun showAd(context: Context) {
//        try {
//            if (apsAdMobUtil.getApsAdController() != null) {
//                apsAdMobUtil.getApsAdController().show()
//            }
//        } catch (e: RuntimeException) {
//            APSAnalytics.logEvent(
//                APSEventSeverity.FATAL,
//                APSEventType.EXCEPTION,
//                "Fail to execute show Ad method during runtime in APSAdMobRewardedCustomEventLoader class",
//                e
//            )
//            mediationAdLoadCallback!!.onFailure(
//                AdError(
//                    3,
//                    "Fail to show custom interstitial ad in APSAdMobRewardedCustomEventLoader class",
//                    "com.amazon.device.ads"
//                )
//            )
//        }
//    }
//
//    override fun onAdLoaded(apsAd: ApsAd) {
//        try {
//            if (mediationAdLoadCallback != null) {
//                mediationRewardedAdCallback = mediationAdLoadCallback.onSuccess(this)
//            }
//        } catch (e: RuntimeException) {
//            APSAnalytics.logEvent(
//                APSEventSeverity.FATAL,
//                APSEventType.EXCEPTION,
//                "Fail to execute onAdLoaded method during runtime in APSAdMobRewardedCustomEventLoader class",
//                e
//            )
//        }
//    }
//
//    override fun onAdFailedToLoad(apsAd: ApsAd) {
//        val adError = AdError(3, "Custom interstitial ad failed to load", "com.amazon.device.ads")
//        try {
//            if (mediationAdLoadCallback != null) {
//                mediationAdLoadCallback.onFailure(adError)
//            }
//            mediationRewardedAdCallback!!.onAdFailedToShow(adError)
//        } catch (e: RuntimeException) {
//            APSAnalytics.logEvent(
//                APSEventSeverity.FATAL,
//                APSEventType.EXCEPTION,
//                "Fail to execute onAdFailedToShow method during runtime in APSAdMobRewardedCustomEventLoader class",
//                e
//            )
//        }
//    }
//
//    override fun onAdClicked(apsAd: ApsAd) {
//        try {
//            if (mediationRewardedAdCallback != null) {
//                mediationRewardedAdCallback!!.reportAdClicked()
//            }
//        } catch (e: RuntimeException) {
//            APSAnalytics.logEvent(
//                APSEventSeverity.FATAL,
//                APSEventType.EXCEPTION,
//                "Fail to execute reportAdClicked method during runtime in APSAdMobRewardedCustomEventLoader class",
//                e
//            )
//        }
//    }
//
//    override fun onImpressionFired(apsAd: ApsAd) {}
//    override fun onAdError(apsAd: ApsAd) {
//        super.onAdError(apsAd)
//    }
//
//    override fun onAdOpen(apsAd: ApsAd) {
//        try {
//            if (mediationRewardedAdCallback != null) {
//                mediationRewardedAdCallback!!.onAdOpened()
//            }
//        } catch (e: RuntimeException) {
//            APSAnalytics.logEvent(
//                APSEventSeverity.FATAL,
//                APSEventType.EXCEPTION,
//                "Fail to execute onAdOpen method during runtime in APSAdMobRewardedCustomEventLoader class",
//                e
//            )
//        }
//    }
//
//    override fun onAdClosed(apsAd: ApsAd) {
//        try {
//            if (mediationRewardedAdCallback != null) {
//                mediationRewardedAdCallback!!.onAdClosed()
//            }
//        } catch (e: RuntimeException) {
//            APSAnalytics.logEvent(
//                APSEventSeverity.FATAL,
//                APSEventType.EXCEPTION,
//                "Fail to execute onAdClosed method during runtime in APSAdMobRewardedCustomEventLoader class",
//                e
//            )
//        }
//    }
//
//    override fun onVideoCompleted(apsAd: ApsAd) {
//        try {
//            val reward = APSReward()
//            if (mediationRewardedAdCallback != null) {
//                APSAdMobAdapterUtil.executeOnMainThread {
//                    mediationRewardedAdCallback!!.onVideoComplete()
//                    mediationRewardedAdCallback!!.onUserEarnedReward(reward)
//                }
//            }
//        } catch (e: RuntimeException) {
//            APSAnalytics.logEvent(
//                APSEventSeverity.FATAL,
//                APSEventType.EXCEPTION,
//                "Fail to execute onVideoComplete method during runtime in APSAdMobRewardedCustomEventLoader class",
//                e
//            )
//        }
//    }
//
//    /* loaded from: aps-admob-adapter.aar:classes.jar:com/amazon/admob_adapter/APSAdMobRewardedCustomEventLoader$APSReward.class */
//    internal class APSReward : RewardItem {
//        override fun getType(): String {
//            return ""
//        }
//
//        override fun getAmount(): Int {
//            return 1
//        }
//    }
//
//    companion object {
//        private val LOGTAG = APSAdMobRewardedCustomEventLoader::class.java.simpleName
//    }
//}
