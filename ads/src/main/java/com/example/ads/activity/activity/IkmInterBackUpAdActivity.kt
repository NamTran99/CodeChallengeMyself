package com.example.ads.activity.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.nativead.NativeAd
import com.example.ads.R
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.pub.IKAdFormat
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.format.intertial.IKInterstitialAd
import com.example.ads.activity.format.native_ads.IKNativeController
import com.example.ads.activity.format.native_ads.NativeBackUpLatest
import com.example.ads.activity.listener.sdk.IKSdkShowAdListener
import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
import com.example.ads.activity.mediation.applovin.IkObjectNativeMax
//import com.example.ads.activity.tracking.CoreTracking
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKTrackingConst
import com.example.ads.activity.widgets.IKWidgetAdUtil
import com.example.ads.activity.widgets.IkmWALF
import com.example.ads.activity.widgets.IkmWidgetAdLayout
//import com.example.ads.activity.widgets.IkmWidgetAdView


class IkmInterBackUpAdActivity : AppCompatActivity() {
    private val interAd: IKInterstitialAd by lazy {
        IKInterstitialAd(lifecycle)
    }

    companion object {
        private var adEventCallback: IKSdkShowAdListener? = null
        private var screen = ""

        fun showAd(context: Context?, screen: String, eventCallback: IKSdkShowAdListener? = null) {
            this.screen = screen
            this.adEventCallback = eventCallback

            runCatching {
                if (context == null) {
                    adEventCallback?.onAdShowFail(IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID))
                    adEventCallback = null
                    return
                }
                context.startActivity(Intent(context, IkmInterBackUpAdActivity::class.java))
            }.onFailure {
                adEventCallback?.onAdShowFail(IKAdError(IKSdkErrorCode.RUNNING_EXCEPTION))
                adEventCallback = null
            }
        }

        const val TXT_CLOSE = " (X) "
    }

    override fun onDestroy() {
        super.onDestroy()
        interAd.destroy()
        adEventCallback?.onAdDismiss()
        adEventCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.ikml_inter_activity)
        supportActionBar?.hide()

        val textCountDown = findViewById<TextView>(R.id.interAd_timeAdText)
        textCountDown.text = TXT_CLOSE

        findViewById<View>(R.id.interAd_closeContainer)?.setOnClickListener {
            adEventCallback?.onAdDismiss()
            adEventCallback = null
            finish()
        }

        loadAd()
    }

    private fun loadAd() {
//        val adView = findViewById<IkmWidgetAdView>(R.id.interAd_containerAds)
//        adView?.setEnableShimmer(false)
        findViewById<View>(R.id.interAd_loadingAds)?.visibility = View.VISIBLE

        IKNativeController.showAdNativeBackupLatest(
            "",
            screen,
            object : IKSdkShowWidgetAdListener {
                override fun onAdReady(
                    adData: IKSdkBaseLoadedAd<*>,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    findViewById<View>(R.id.interAd_loadingAds)?.visibility = View.GONE
                    handleShowNativeFullAdView(
                        adLoaded = NativeBackUpLatest.getBackupAd(IKAdFormat.INTER),
                        onSuccess = {
//                            CoreTracking.trackingSdkBackupAd(
//                                adFormat = IKAdFormat.NATIVE_FULL.value,
//                                adStatus = IKSdkDefConst.AdStatus.SHOWED,
//                                screen = screen,
//                                actionWithAds = IKSdkDefConst.AdAction.SHOW
//                            )
                        },
                        onFail = { err ->
                            onAdShowFail(err, scriptName, adNetworkName)
                        },
                    )
                }

                override fun onAdReloaded(
                    adData: IKSdkBaseLoadedAd<*>,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    /* no-op */
                }

                override fun onAdReloadFail(
                    error: IKAdError,
                    scriptName: String,
                    adNetworkName: String
                ) {
                    /* no-op */
                }

                override fun onAdShowFail(
                    error: IKAdError,
                    scriptName: String,
                    adNetworkName: String
                ) {
//                    CoreTracking.trackingSdkBackupAd(
//                        adFormat = IKAdFormat.NATIVE_FULL.value,
//                        adStatus = IKSdkDefConst.AdStatus.SHOW_FAIL,
//                        screen = screen,
//                        actionWithAds = IKSdkDefConst.AdAction.SHOW,
//                        Pair(IKTrackingConst.ParamName.ERROR_CODE, "${error.code}"),
//                        Pair(IKTrackingConst.ParamName.MESSAGE, error.message)
//                    )
                }

                override fun onAdClick(scriptName: String, adNetworkName: String) {
                    /* no-op */
                }

                override fun onAdImpression(scriptName: String, adNetworkName: String) {
                    /* no-op */
                }
            }
        )
    }

    private fun handleShowNativeFullAdView(
        adLoaded: IKSdkBaseLoadedAd<*>?,
        onSuccess: () -> Unit,
        onFail: (err: IKAdError) -> Unit,
    ) {
        if (adLoaded?.loadedAd == null) {
            onFail.invoke(IKAdError(IKSdkErrorCode.VIEW_GROUP_NULL))
            return
        }
        var viewAd: View? = null
        when (adLoaded.adNetwork) {
            AdNetwork.AD_MOB.value -> {
                viewAd = IKWidgetAdUtil.Admob.NativeFull.setupNativeCustomAdView(
                    this, adLoaded.loadedAd as? NativeAd?, getNativeFullLayout()
                )
            }

            AdNetwork.AD_MANAGER.value -> {
                viewAd = IKWidgetAdUtil.Admob.NativeFull.setupNativeCustomAdView(
                    this, adLoaded.loadedAd as? NativeAd?, getNativeFullLayout()
                )
            }

            AdNetwork.AD_MAX.value -> {
                viewAd = IKWidgetAdUtil.Applovin.NativeFull.setupNativeCustomAdView(
                    this, adLoaded.loadedAd as? IkObjectNativeMax?, getNativeFullLayout()
                )
            }
        }
//        val adView = findViewById<IkmWidgetAdView>(R.id.interAd_containerAds)
//        adView.addView(viewAd)
        onSuccess.invoke()
    }

    private fun getNativeFullLayout(): IkmWidgetAdLayout? {
        val adLayout = LayoutInflater.from(applicationContext).inflate(
            R.layout.layout_native_full,
            null, false
        ) as? IkmWALF
        adLayout?.titleView = adLayout?.findViewById(R.id.custom_headline)
        adLayout?.bodyView = adLayout?.findViewById(R.id.custom_body)
        adLayout?.callToActionView = adLayout?.findViewById(R.id.custom_call_to_action)
        adLayout?.iconView = adLayout?.findViewById(R.id.custom_app_icon)
        adLayout?.mediaView = adLayout?.findViewById(R.id.custom_media)
        adLayout?.storeView = adLayout?.findViewById(R.id.custom_store)
        adLayout?.starRatingView = adLayout?.findViewById(R.id.custom_rate)

        adLayout?.titleViewPor = adLayout?.findViewById(R.id.custom_headlinePor)
        adLayout?.bodyViewPor = adLayout?.findViewById(R.id.custom_bodyPor)
        adLayout?.callToActionViewPor = adLayout?.findViewById(R.id.custom_call_to_actionPor)
        adLayout?.iconViewPor = adLayout?.findViewById(R.id.custom_app_iconPor)
        adLayout?.mediaViewPor = adLayout?.findViewById(R.id.custom_mediaPor)

        adLayout?.titleViewPor2 = adLayout?.findViewById(R.id.custom_headlinePor2)
        adLayout?.bodyViewPor2 = adLayout?.findViewById(R.id.custom_bodyPor2)
        adLayout?.callToActionViewPor2 = adLayout?.findViewById(R.id.custom_call_to_actionPor2)
        adLayout?.iconViewPor2 = adLayout?.findViewById(R.id.custom_app_iconPor2)
        adLayout?.storeViewPor2 = adLayout?.findViewById(R.id.custom_storePor2)
        adLayout?.starRatingViewPor2 = adLayout?.findViewById(R.id.custom_ratePor2)

        adLayout?.titleViewSquare = adLayout?.findViewById(R.id.custom_headlineSquare)
        adLayout?.bodyViewSquare = adLayout?.findViewById(R.id.custom_bodySquare)
        adLayout?.callToActionViewSquare = adLayout?.findViewById(R.id.custom_call_to_actionSquare)
        adLayout?.iconViewSquare = adLayout?.findViewById(R.id.custom_app_iconSquare)
        adLayout?.mediaViewSquare = adLayout?.findViewById(R.id.custom_mediaSquare)
        adLayout?.storeViewSquare = adLayout?.findViewById(R.id.custom_storeSquare)
        adLayout?.starRatingViewSquare = adLayout?.findViewById(R.id.custom_rateSquare)

        adLayout?.containerNor = adLayout?.findViewById(R.id.custom_containerAds)
        adLayout?.containerPor = adLayout?.findViewById(R.id.custom_containerAdsPor)
        adLayout?.containerSquare = adLayout?.findViewById(R.id.custom_containerAdsSquare)

        adLayout?.customAnimateView = adLayout?.findViewById(R.id.custom_actionContainerPor)
        adLayout?.customAnimateView2 = adLayout?.findViewById(R.id.custom_actionContainerPor2)
        adLayout?.isMute = false
        adLayout?.mediaView?.setMediaAdjustViewBounds(true)
        adLayout?.enableClickOutside()
        adLayout?.setRoundIconValue(resources?.getDimensionPixelOffset(R.dimen.sdk_icon_round) ?: 0)

        return adLayout
    }
}