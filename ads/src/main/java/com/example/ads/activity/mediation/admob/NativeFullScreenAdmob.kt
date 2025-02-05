package com.example.ads.activity.mediation.admob

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MediaAspectRatio
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.format.base.IKSdkHandleTimeoutAd
import com.example.ads.activity.format.native_ads.IKSdkBaseNativeFullScreenAdGoogle
import com.example.ads.activity.listener.sdk.IKSdkAdCallback
import com.example.ads.activity.listener.sdk.IKSdkLoadCoreAdCallback
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

open class NativeFullScreenAdmob : IKSdkBaseNativeFullScreenAdGoogle(AdNetwork.AD_MOB) {

    override suspend fun loadCoreAd(
        coroutineScope: CoroutineScope,
        idAds: IKAdUnitDto,
        scriptName: String,
        screen: String?,
        showPriority: Int,
        isLoadAndShow: Boolean,
        callback: IKSdkAdCallback<NativeAd>
    ) {
        showLogD("loadCoreAd pre start")
        val context = IKSdkApplicationProvider.getContext()
        if (context == null) {
            callback.onAdFailedToLoad(
                adNetworkName, IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID)
            )
            showLogD("loadCoreAd ${IKSdkErrorCode.CONTEXT_NOT_VALID}")
            return
        }
        coroutineScope.launchWithSupervisorJob(Dispatchers.IO) {
            val unitId = idAds.adUnitId?.trim()
            if (unitId.isNullOrBlank()) {
                callback.onAdFailedToLoad(
                    adNetworkName,
                    IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD)
                )
                showLogD("loadCoreAd unit empty")
                return@launchWithSupervisorJob
            }
            if (!isLoadAndShow && checkLoadSameAd(
                    idAds.adPriority ?: 0,
                    idAds.cacheSize ?: 0,
                    IKSdkDefConst.TimeOutAd.NATIVE
                )
            ) {
                callback.onAdFailedToLoad(adNetworkName, IKAdError(IKSdkErrorCode.READY_CURRENT_AD))
                showLogD("loadCoreAd an ad ready")
                return@launchWithSupervisorJob
            }
            showLogD("loadCoreAd start")

            var handleTimeout: IKSdkHandleTimeoutAd<NativeAd>? =
                IKSdkHandleTimeoutAd(adNetworkName, idAds, callback)
            var objectAd: IKSdkBaseLoadedAd<NativeAd>? = null

            var loadCoreCallback: IKSdkLoadCoreAdCallback<NativeAd>? =
                object : IKSdkLoadCoreAdCallback<NativeAd> {
                    override fun onLoaded(result: NativeAd) {
                        objectAd = createDto(showPriority, result, idAds)
                        //callback.onAdLoaded(adNetworkName, objectAd)
                        handleTimeout?.onLoaded(
                            this@NativeFullScreenAdmob,
                            coroutineScope,
                            objectAd,
                            scriptName
                        )
                        handleTimeout = null
                    }

                    override fun onLoadFail(error: IKAdError) {
                        handleTimeout?.onLoadFail(this@NativeFullScreenAdmob, error, scriptName)
                        handleTimeout = null
                    }
                }

//            val builder = AdLoader.Builder(context, unitId)
//            builder.forNativeAd { unifiedNativeAd ->
//                loadCoreCallback?.onLoaded(unifiedNativeAd)
//                loadCoreCallback = null
//            }

            val adOptions =
                NativeAdOptions.Builder().setMediaAspectRatio(MediaAspectRatio.PORTRAIT).build()
//            builder.withNativeAdOptions(adOptions)

//            val adLoader = builder.withAdListener(object : AdListener() {
//                override fun onAdClosed() {
//
//                }
//
//                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                    showLogD("loadCoreAd onAdFailedToLoad, $loadAdError")
//                    loadCoreCallback?.onLoadFail(IKAdError(loadAdError))
//                    loadCoreCallback = null
//                }
//
//                override fun onAdOpened() {
//
//                }
//
//                override fun onAdLoaded() {
//                    showLogD("loadCoreAd onAdLoaded")
//                }

//                override fun onAdClicked() {
//                    showLogD("loadCoreAd onAdClicked")
//                    objectAd?.listener?.onAdClicked(adNetworkName)
//                }
//
//                override fun onAdImpression() {
//                    showLogD("loadCoreAd onAdImpression")
//                    objectAd?.listener?.onAdImpression(adNetworkName)
//                }
//            }).build()

//            adLoader.loadAd(AdRequest.Builder().build())
            handleTimeout?.startHandle(this@NativeFullScreenAdmob, scriptName)
        }
    }
}