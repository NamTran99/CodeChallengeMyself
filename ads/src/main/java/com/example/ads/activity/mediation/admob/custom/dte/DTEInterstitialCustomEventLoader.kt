package com.example.ads.activity.mediation.admob.custom.dte

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.android.gms.ads.mediation.MediationAdLoadCallback
import com.google.android.gms.ads.mediation.MediationInterstitialAd
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKCustomEventData
import com.example.ads.activity.listener.sdk.IKSdkCustomEventAdListener
import com.example.ads.activity.listener.sdk.IKSdkShowAdListener
//import com.example.ads.activity.mediation.custom.fairbid.IKFairBidController
import com.example.ads.activity.mediation.custom.utils.IKCustomEventError
import com.example.ads.activity.mediation.custom.utils.IKCustomParamParser
import com.example.ads.activity.utils.IKLogs
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IkmSdkCoreFunc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DTEInterstitialCustomEventLoader(
    private val mediationAdConfiguration: MediationInterstitialAdConfiguration,
    private val mediationAdLoadCallback: MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback>
) : MediationInterstitialAd {
//    private var controller: IKFairBidController? = IKFairBidController()
    private var adCallback: MediationInterstitialAdCallback? = null

    fun loadAd() {
        CoroutineScope(Dispatchers.Main).launch {
            showLogD("Begin loading interstitial ad.")
//            controller?.loadInterstitialAd(
//                listOf(
//                    IKCustomEventData(
//                        0,
//                        IKCustomParamParser.getAdUnit(mediationAdConfiguration.serverParameters)
//                            ?: IKSdkDefConst.EMPTY,
//                        15000L
//                    )
//                ), object : IKSdkCustomEventAdListener {
//                    override fun onAdLoaded() {
//                        adCallback =
//                            mediationAdLoadCallback.onSuccess(this@DTEInterstitialCustomEventLoader)
//                    }

//                    override fun onAdLoadFail(error: IKAdError) {
//                        mediationAdLoadCallback.onFailure(IKCustomEventError.createCustomEventAdNotAvailableError())
//                    }
//
//                    override fun onAdClicked() {
//                        adCallback?.reportAdClicked()
//                    }

//                    override fun onAdImpression() {
//
//                    }
//
//                    override fun onAdShowed(bundle: Bundle?) {
//                        adCallback?.onAdOpened()
//                    }
//
//                    override fun onAdShowFailed(error: IKAdError) {
//                        adCallback?.onAdFailedToShow(
//                            IKCustomEventError.createSdkError(
//                                "Ad Display Failed",
//                                9000
//                            )
//                        )
//                    }
//
//                    override fun onAdDismissed() {
//                        adCallback?.onAdClosed()
//                    }
//
//                })
//            showLogD("start fetching interstitial ad.")
        }
    }

    override fun showAd(context: Context) {
        showLogD("The interstitial ad was shown.")
//        if (controller == null) {
//            adCallback?.onAdFailedToShow(
//                IKCustomEventError.createSdkError(
//                    "Interstitial ad not ready",
//                    9001
//                )
//            )
//            return
//        }
        var act = context as? Activity
        if (act == null)
            act =
                IkmSdkCoreFunc.AppF.listActivity.filter { it.value != null }.values.firstOrNull()
        if (act == null) {
            adCallback?.onAdFailedToShow(
                IKCustomEventError.createSdkError(
                    "Ad Display Failed, context activity null",
                    9001
                )
            )
            return
        }
//        controller?.showAd(act, object : IKSdkShowAdListener {
//            override fun onAdShowed(priority: Int) {
//            }
//
//            override suspend fun onAdReady(priority: Int) {
//            }
//
//            override fun onAdDismiss() {
//            }
//
//            override fun onAdShowFail(error: IKAdError) {
//                adCallback?.onAdFailedToShow(
//                    IKCustomEventError.createSdkError(
//                        "Ad Display Failed",
//                        9001
//                    )
//                )
//            }
//        })
    }

    companion object {
        /** Tag used for log statements  */
        private const val TAG = "DTEInterCustomEvent"
    }

    fun showLogD(message: String) {
        IKLogs.d(TAG) { message }
    }

    fun showLogE(message: String) {
        IKLogs.e(TAG) { message }
    }
}
