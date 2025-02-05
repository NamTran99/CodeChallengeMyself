package com.example.ads.activity.mediation.admob.custom.dte

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
//import com.fyber.fairbid.ads.Banner
//import com.fyber.fairbid.ads.banner.BannerError
//import com.fyber.fairbid.ads.banner.BannerListener
//import com.fyber.fairbid.ads.banner.BannerOptions
//import com.fyber.fairbid.ads.banner.BannerSize
import com.google.android.gms.ads.mediation.MediationAdLoadCallback
import com.google.android.gms.ads.mediation.MediationBannerAd
import com.google.android.gms.ads.mediation.MediationBannerAdCallback
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration
import com.example.ads.activity.mediation.custom.utils.IKCustomEventError
import com.example.ads.activity.mediation.custom.utils.IKCustomParamParser
import com.example.ads.activity.utils.IKLogs
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IkmSdkCoreFunc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Banner custom event loader for the SampleSDK.  */
class DTEBannerCustomEventLoader(
    /** Configuration for requesting the banner ad from the third party network.  */
    private val mediationAdConfiguration: MediationBannerAdConfiguration,
    /** Callback that fires on loading success or failure.  */
    private val mediationAdLoadCallback: MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback>
) : MediationBannerAd {
    /** View to contain the sample banner ad.  */
    private var adUnit = ""
    private var adViewGroup: ViewGroup? = null
    private var isAdLoading = false

    /** Callback for banner ad events.  */
    private var adCallback: MediationBannerAdCallback? = null

    /** Loads a banner ad from the third party ad network.  */
    fun loadAd() {
        // All custom events have a server parameter named "parameter" that returns back the parameter
        // entered into the AdMob UI when defining the custom event.
        CoroutineScope(Dispatchers.Main).launch {
            showLogD("Begin loading banner ad.")
            adUnit = IKCustomParamParser.getAdUnit(mediationAdConfiguration.serverParameters)
                ?: IKSdkDefConst.EMPTY
            if (adUnit.isBlank()) {
                mediationAdLoadCallback.onFailure(
                    IKCustomEventError.createSdkError(
                        "ad unit empty",
                        8900
                    )
                )
                return@launch
            }
            if (isAdLoading) {
                mediationAdLoadCallback.onFailure(
                    IKCustomEventError.createSdkError(
                        "Other Ad Loading",
                        8901
                    )
                )
                return@launch
            }
            if (adViewGroup != null && adCallback != null) {
                return@launch
            }
            showLogD("Received server parameter.")
            val context = mediationAdConfiguration.context
            var act = context as? Activity
            if (act == null) {
                act =
                    IkmSdkCoreFunc.AppF.listActivity.filter { it.value != null }.values.firstOrNull()
                if (act == null) {
                    mediationAdLoadCallback.onFailure(
                        IKCustomEventError.createSdkError(
                            "Ad Display Failed, context activity null",
                            9001
                        )
                    )
                    return@launch
                }
            }
            // Assumes that the serverParameter is the AdUnit for the Sample Network.
//            val bannerSize = BannerSize.SMART
            adViewGroup = RelativeLayout(context)
            isAdLoading = true
            showLogD("Start fetching banner ad.")

//            Banner.setBannerListener(object : BannerListener {
//                override fun onError(placementId: String, error: BannerError) {
//                    mediationAdLoadCallback.onFailure(IKCustomEventError.createCustomEventAdNotAvailableError())
//                    isAdLoading = false
//                }
//
//                override fun onLoad(placementId: String) {
//                    isAdLoading = false
//                    adCallback =
//                        mediationAdLoadCallback.onSuccess(this@DTEBannerCustomEventLoader)
//                }
//
//                override fun onShow(
//                    placementId: String,
//                    impressionData: com.fyber.fairbid.ads.ImpressionData
//                ) {
//                    // Passing extra info such as creative id supported in 9.15.0+
//                    val creativeId = impressionData.creativeId
//                    adCallback?.onAdOpened()
//                }
//
//                override fun onClick(placementId: String) {
//                    adCallback?.reportAdClicked()
//                }
//
//                override fun onRequestStart(placementId: String, requestId: String) {
//                    // Called when the banner from placement 'placementId' is going to be requested
//                    // 'requestId' identifies the request across the whole request/show flow
//                }
//            })
//
//            Banner.show(
//                adUnit, BannerOptions()
//                    .placeInContainer(adViewGroup!!)
//                    .withSize(bannerSize), act
//            )
        }

    }

    override fun getView(): View {
        return adViewGroup ?: View(mediationAdConfiguration.context)
    }

    companion object {
        /** Tag used for log statements  */
        private const val TAG = "APSBannerCustomEvent"
    }

    fun showLogD(message: String) {
        IKLogs.d(TAG) { message }
    }

    fun showLogE(message: String) {
        IKLogs.e(TAG) { message }
    }
}
