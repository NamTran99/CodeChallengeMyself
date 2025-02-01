//package com.example.ads.activity.mediation.admob.custom.aps
//
//import android.content.res.Resources
//import android.util.DisplayMetrics
//import android.view.View
//import android.view.ViewGroup
////import com.amazon.aps.ads.ApsAd
////import com.amazon.aps.ads.ApsAdController
////import com.amazon.aps.ads.listeners.ApsAdListener
////import com.amazon.aps.shared.metrics.ApsMetricsPerfEventModelBuilder
////import com.amazon.device.ads.AdRegistration
////import com.amazon.device.ads.DTBAdUtil
////import com.amazon.device.ads.DTBExpectedSizeProvider
//import com.google.android.gms.ads.AdError
//import com.google.android.gms.ads.mediation.MediationAdLoadCallback
//import com.google.android.gms.ads.mediation.MediationBannerAd
//import com.google.android.gms.ads.mediation.MediationBannerAdCallback
//import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration
//import com.example.ads.activity.mediation.custom.utils.IKCustomEventError
//import com.example.ads.activity.mediation.custom.utils.IKCustomParamParser
//import com.example.ads.activity.utils.IKLogs
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlin.math.roundToInt
//
///** Banner custom event loader for the SampleSDK.  */
//class APSBannerCustomEventLoader(
//    /** Configuration for requesting the banner ad from the third party network.  */
//    private val mediationAdConfiguration: MediationBannerAdConfiguration,
//    /** Callback that fires on loading success or failure.  */
//    private val mediationAdLoadCallback: MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback>
//) : MediationBannerAd, DTBExpectedSizeProvider {
//    /** View to contain the sample banner ad.  */
//    private var viewToAd: ViewGroup? = null
//    private var expectedWidth = 0
//    private var expectedHeight = 0
//
//    /** Callback for banner ad events.  */
//    private var adCallback: MediationBannerAdCallback? = null
//    private var apsAdController: ApsAdController? = null
//    override fun setExpectedHeight(height: Int) {
//        expectedHeight = height
//    }
//
//    override fun setExpectedWidth(width: Int) {
//        expectedWidth = width
//    }
//
//    override fun getExpectedHeight(): Int {
//        return expectedHeight
//    }
//
//    override fun getExpectedWidth(): Int {
//        return expectedWidth
//    }
//
//    /** Loads a banner ad from the third party ad network.  */
//    fun loadAd(metricsBuilder: ApsMetricsPerfEventModelBuilder, correlationId: String?) {
//        // All custom events have a server parameter named "parameter" that returns back the parameter
//        // entered into the AdMob UI when defining the custom event.
//        CoroutineScope(Dispatchers.Main).launch {
//            showLogD("Begin loading banner ad.")
//
//            val adId = IKCustomParamParser.getAdUnit(mediationAdConfiguration.serverParameters)
//            val pricePoint =
//                IKCustomParamParser.getPricePoint(mediationAdConfiguration.serverParameters)
//            if (adId.isNullOrBlank() || pricePoint.isNullOrBlank()) {
//                mediationAdLoadCallback.onFailure(IKCustomEventError.createCustomEventNoAdIdError())
//                return@launch
//            }
//            showLogD("Received server parameter.")
//            val context = mediationAdConfiguration.context
//            val size = mediationAdConfiguration.adSize
//
//            val widthInPixels = size.getWidthInPixels(context)
//            val heightInPixels = size.getHeightInPixels(context)
//            val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics
//            val widthInDp = (widthInPixels / displayMetrics.density).roundToInt()
//            val heightInDp = (heightInPixels / displayMetrics.density).roundToInt()
//            val customEventExtras = DTBAdUtil.createAdMobBannerRequestBundle(
//                adId,
//                widthInDp,
//                heightInDp
//            )
//            // Assumes that the serverParameter is the AdUnit for the Sample Network.
//            if (apsAdController == null)
//                apsAdController = ApsAdController(context, object : ApsAdListener {
//
//                    override fun onAdLoaded(p0: ApsAd?) {
//                        showLogD("Received the banner ad.")
//                        adCallback =
//                            mediationAdLoadCallback.onSuccess(this@APSBannerCustomEventLoader)
//                        viewToAd = DTBAdUtil.getAdViewWrapper(
//                            p0?.adView,
//                            widthInPixels,
//                            heightInPixels,
//                            expectedWidth,
//                            expectedHeight
//                        )
//                    }
//
//                    override fun onAdError(apsAd: ApsAd?) {
//                        super.onAdError(apsAd)
//                    }
//
//                    override fun onAdOpen(apsAd: ApsAd?) {
//                        super.onAdOpen(apsAd)
//                        showLogD("The banner ad was shown fullscreen.")
//                        adCallback?.onAdOpened()
//                    }
//
//                    override fun onAdClosed(apsAd: ApsAd?) {
//                        super.onAdClosed(apsAd)
//                        showLogD("The banner ad was closed.")
//                        adCallback?.onAdClosed()
//                        adCallback = null
//                    }
//
//                    override fun onVideoCompleted(apsAd: ApsAd?) {
//                        super.onVideoCompleted(apsAd)
//                    }
//
//                    override fun onAdFailedToLoad(p0: ApsAd?) {
//                        showLogE("Failed to fetch the banner ad.")
//                        mediationAdLoadCallback.onFailure(
//                            AdError(
//                                3,
//                                "Custom banner ad failed to load",
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
//
//            showLogD("Start fetching banner ad.")
//
//            val requestId = customEventExtras.getString("amazon_custom_event_request_id")
//            val dtbCacheData = AdRegistration.getAdMobCache(requestId)
//            if (dtbCacheData != null) {
//                if (dtbCacheData.isBidRequestFailed) {
//
//                    mediationAdLoadCallback.onFailure(
//                        AdError(
//                            3,
//                            "Fail to load custom banner ad in loadAd because previous bid requests failure",
//                            "com.amazon.device.ads"
//                        )
//                    )
//                    return@launch
//                }
//                val apsAd = dtbCacheData.adResponse as? ApsAd
//                if (apsAd != null && apsAdController != null) {
//                    APSAdMobUtil.renderAPSBannerAds(
//                        apsAd, apsAdController!!,
//                        mediationAdLoadCallback,
//                        pricePoint,
//                        requestId
//                    )
//                    return@launch
//                }
//            }
//            if (apsAdController != null) {
//                APSAdMobUtil.loadBannerAd(
//                    apsAdController!!,
//                    mediationAdLoadCallback,
//                    customEventExtras,
//                    pricePoint,
//                    autoRefreshRequestIds,
//                    metricsBuilder,
//                    correlationId
//                )
//            } else mediationAdLoadCallback.onFailure(IKCustomEventError.createCustomEventAdNotAvailableError())
//
//        }
//    }
//
//    override fun getView(): View {
//        return viewToAd ?: View(mediationAdConfiguration.context)
//    }
//
//    companion object {
//        /** Tag used for log statements  */
//        private const val TAG = "APSBannerCustomEvent"
//        var autoRefreshRequestIds: MutableSet<String> = mutableSetOf()
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
