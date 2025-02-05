package com.example.ads.activity.format.banner

import android.content.Context
import android.os.Build
import android.view.WindowManager
import com.google.android.gms.ads.AdSize
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.SDKAdPriorityDto
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdWidgetDetailDto
import com.example.ads.activity.format.base.IKSdkBaseAd
import com.example.ads.activity.listener.sdk.IKSdkAdLoadCoreCallback
import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import com.example.ads.activity.widgets.IkmDisplayWidgetAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


abstract class IKSdkBaseBannerAd<T : Any>(adNetwork: AdNetwork) :
    IKSdkBaseAd<T>(adNetwork) {
    override var adFormatName: String = IKSdkDefConst.AdFormat.BANNER
    override val logTag: String
        get() = "${adFormatName}_$adNetworkName"


    protected fun calculatorAdSize(context: Context): Pair<Int, Float> {
        val displayMetrics = context.resources.displayMetrics
        val adWidthPixels =
            kotlin.runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val windowManager =
                        context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                    val windowMetrics = windowManager?.currentWindowMetrics
                    windowMetrics?.bounds?.width() ?: displayMetrics.widthPixels
                } else {
                    displayMetrics.widthPixels
                }
            }.getOrNull() ?: displayMetrics.widthPixels
        val density = displayMetrics.density
        return Pair(adWidthPixels, density)
    }

    open suspend fun getAdmobAdSize(
        context: Context,
        screen: String?,
    ): AdSize {
        return withContext(Dispatchers.Default) {
            try {
                val (adWidthPixels, density) = calculatorAdSize(context)
                if (adWidthPixels == 0 || density == 0f)
                    AdSize.BANNER
                val adWidth = (adWidthPixels / density).toInt()
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
            } catch (e: Exception) {
                AdSize.BANNER
            }
        }
    }

    abstract fun showAvailableAd(
        coroutineScope: CoroutineScope,
        screen: String,
        scriptName: String,
        showAdListener: IKSdkShowWidgetAdListener
    )

    abstract fun showAdWithAdObject(
        adReady: IKSdkBaseLoadedAd<T>,
        screen: String,
        scriptName: String,
        showAdListener: IKSdkShowWidgetAdListener
    )

    private fun handleAdLoadFail(
        scope: CoroutineScope,
        error: IKAdError,
        showAdListener: IKSdkShowWidgetAdListener,
        scriptName: String,
        screen: String
    ) {
        scope.launchWithSupervisorJob {
            val adReady = getReadyAd(IKSdkDefConst.TimeOutAd.BANNER)
            if (adReady?.loadedAd != null) {

                showLogD("showAd cache1")
                showAdWithAdObject(
                    adReady,
                    scriptName,
                    screen,
                    showAdListener
                )
            } else {
                showAdListener.onAdReloadFail(
                    error,
                    scriptName,
                    adNetworkName
                )
                showAdListener.onAdShowFail(error, scriptName, adNetworkName)
                showLogD("showAd not valid Ad1")
            }
        }
    }


    private fun handleAdLoaded(
        adsResult: IKSdkBaseLoadedAd<T>?,
        showAdListener: IKSdkShowWidgetAdListener,
        scriptName: String,
        screen: String
    ) {

        if (adsResult != null) {
            showLogD("showAd pre start show $screen")
            showAdWithAdObject(
                adsResult,
                screen,
                scriptName,
                showAdListener
            )
        } else {
            showLogD("showAd not valid Ad2")
            showAdListener.onAdShowFail(
                IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW),
                scriptName,
                adNetworkName
            )
        }
    }

    suspend fun loadAndShowAd(
        coroutineScope: CoroutineScope,
        screen: String,
        scriptName: String,
        unitDto: IKAdUnitDto?,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
        showLogD("showAd load 1")
        val callback = object : IKSdkAdLoadCoreCallback<IKSdkBaseLoadedAd<T>, IKAdError> {
            override fun onAdLoadFail(adNetwork: String, error: IKAdError) {
                handleAdLoadFail(
                    coroutineScope,
                    error,
                    showAdListener,
                    scriptName,
                    screen
                )
            }

            override fun onAdLoaded(adNetwork: String, adsResult: IKSdkBaseLoadedAd<T>?) {
                handleAdLoaded(
                    adsResult,
                    showAdListener,
                    scriptName,
                    screen
                )
            }
        }
        loadSingleAdSdk(
            coroutineScope,
            0,
            scriptName,
            screen,
            true,
            unitDto,
            callback
        )
    }

    suspend fun getReadyDisplayAd(): IkmDisplayWidgetAdView? {
        val mAdsObject = getReadyAd(IKSdkDefConst.TimeOutAd.NATIVE)
        return if (mAdsObject == null) return null
        else IkmDisplayWidgetAdView(
            mAdsObject, SDKAdPriorityDto(
                adNetwork.value,
                mAdsObject.adPriority,
                mAdsObject.showPriority
            ),
            adF = IKSdkDefConst.AdFormat.NATIVE
        )
    }

    open fun showAdWithAdView(
        coroutineScope: CoroutineScope,
        screen: String,
        scriptName: String,
        adObject: IkmDisplayWidgetAdView,
        showAdListener: IKSdkShowWidgetAdListener
    ) {
    }

    open suspend fun loadAndShowCollapsibleAd(
        idAds: IKAdUnitDto,
        adData: IKSdkProdWidgetDetailDto,
        screen: String,
        showAdListener: IKSdkShowWidgetAdListener
    ) {

    }
}