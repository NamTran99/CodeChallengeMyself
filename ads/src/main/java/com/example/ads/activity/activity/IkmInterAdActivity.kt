package com.example.ads.activity.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ads.R
import com.example.ads.activity.core.fcm.IkmCoreFMService
import com.example.ads.activity.data.db.IKDataRepository
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKSdkInterDto
//import com.example.ads.activity.format.intertial.IKInterController
//import com.example.ads.activity.format.intertial.IKInterController.adPlayGap
import com.example.ads.activity.format.intertial.IKInterstitialAd
import com.example.ads.activity.listener.pub.IKShowAdListener
import com.example.ads.activity.listener.pub.IKShowWidgetAdListener
import com.example.ads.activity.listener.sdk.IKSdkBaseListener
import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
import com.example.ads.activity.listener.sdk.IKSdkShowAdListener
//import com.example.ads.activity.mediation.playgap.IKPlayGapHelper
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import com.example.ads.activity.utils.IkmSdkCoreFunc
import com.example.ads.activity.widgets.IkmWALF
//import com.example.ads.activity.widgets.IkmWidgetAdView
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull


class IkmInterAdActivity : AppCompatActivity() {
    private var isBlockClick = true
    private var countDownTimer: CountDownTimer? = null
    private var forgeClose = false
    private val interAd : IKInterstitialAd by lazy {
        IKInterstitialAd(lifecycle)
    }

    companion object {
        var adEventCallback: IKSdkShowAdListener? = null
        const val TIME_OUT = 11000L
        fun showAd(context: Context?, eventCallback: IKSdkShowAdListener? = null) {
            adEventCallback = eventCallback
            runCatching {
                if (context == null) {
                    adEventCallback?.onAdShowFail(IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID))
                    adEventCallback = null
                    return
                }
                context.startActivity(Intent(context, IkmInterAdActivity::class.java))
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
        if (forgeClose)
            return
        adEventCallback?.onAdDismiss()
        adEventCallback = null
        countDownTimer?.cancel()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        if (!isBlockClick)
            finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.ikml_inter_activity)
        supportActionBar?.hide()
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                if (!isBlockClick)
                    finish()
            }
            onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (!isBlockClick)
                            finish()
                    }
                })
        } else {
            onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (!isBlockClick)
                            finish()
                    }
                })
        }
        IKSdkTrackingHelper.customizeTracking(
            IkmCoreFMService.IKN_TRACKING_TRACK,
            Pair("act", "ct_itc"),
            Pair("ac_kd", "g4")
        )
        adEventCallback?.onAdShowed(0)
        val textCountDown = findViewById<TextView>(R.id.interAd_timeAdText)
        findViewById<View>(R.id.interAd_closeContainer)?.setOnClickListener {
            if (isBlockClick)
                return@setOnClickListener
            adEventCallback?.onAdDismiss()
            adEventCallback = null
            finish()
        }
        lifecycleScope.launchWithSupervisorJob(Dispatchers.Main) {
            var delayTime =
                IKDataRepository.getInstance()
                    ?.getConfigNCL(IKSdkDefConst.AdScreen.NATIVE_INTER_CUSTOM)?.timeOutClose
                    ?: TIME_OUT
            if (delayTime < TIME_OUT)
                delayTime = TIME_OUT
            val delayJob = launch {
                delay(delayTime)
                textCountDown?.text = TXT_CLOSE
                isBlockClick = false
            }
            val text = delayTime / 1000 - 1
            textCountDown?.text = "$text"
            countDownTimer = object : CountDownTimer(delayTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    textCountDown?.text = (millisUntilFinished / 1000).toString()
                }

                override fun onFinish() {
                    textCountDown?.text = TXT_CLOSE
                    isBlockClick = false
                }
            }

            countDownTimer?.start()
            loadAd()
            delayJob.join()
        }
    }

    private suspend fun loadAd() {
//        val adView = findViewById<IkmWidgetAdView>(R.id.interAd_containerAds)
        val adLayout = LayoutInflater.from(applicationContext).inflate(
            R.layout.layout_native_full,
            null, false
        ) as? IkmWALF
//        adView?.enableFullView()
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
//        adView?.setEnableShimmer(false)
        findViewById<View>(R.id.interAd_loadingAds)?.visibility =
            View.VISIBLE
        val textCountDown = findViewById<TextView>(R.id.interAd_timeAdText)
        lifecycleScope.launch {
            val deferred = CompletableDeferred<Boolean>()
            val timeout = IKDataRepository.getConfigNCL(IKSdkDefConst.AdScreen.NATIVE_OPEN_CUSTOM)?.timeOutLoad
                ?: 4000
            val result = withTimeoutOrNull(timeout) {
//                IKInterController.loadAdLabel("inter_custom", object : IKSdkLoadAdCoreListener {
//                    override fun onAdLoaded() {
//                        deferred.complete(true)
//                    }
//
//                    override fun onAdLoadFail(error: IKAdError) {
//                        deferred.complete(false)
//                    }
//                })
                deferred.await()
            } ?: false
            if (result) {
                interAd.showAdCustom(
                    IkmSdkCoreFunc.AppF.listActivity.values.firstOrNull(),
                    IKSdkDefConst.AdScreen.NATIVE_INTER_CUSTOM,
                    object :
                        IKShowAdListener {
                        override fun onAdsShowed() {
                            super.onAdsShowed()
                            forgeClose = true
                            this@IkmInterAdActivity.finish()
                        }

                        override fun onAdsDismiss() {
                            forgeClose = false
                            this@IkmInterAdActivity.finish()
                            adEventCallback?.onAdDismiss()
                            adEventCallback = null
                            countDownTimer?.cancel()
                        }

                        override fun onAdsShowFail(error: IKAdError) {
//                            showCustomNative(adLayout, adView, textCountDown)
                        }
                    }
                )
            }
//            } else
//                showCustomNative(adLayout, adView, textCountDown)
        }
    }

    private fun showCustomNative(
        adLayout: IkmWALF?,
//        adView: IkmWidgetAdView?,
        textCountDown: TextView
    ) {
        if (adLayout != null) {
//            adView?.loadAdFS(
//                adLayout,
//                IKSdkDefConst.AdScreen.NATIVE_INTER_CUSTOM,
//                object : IKShowWidgetAdListener {
//
//                    override fun onAdShowed() {
//                        findViewById<View>(R.id.interAd_loadingAds)?.visibility =
//                            View.GONE
//                    }
//
//                    override fun onAdShowFail(error: IKAdError) {
//                        textCountDown.text = TXT_CLOSE
//                        showPlayGap(null)
//                    }
//                }
//            )
        }
    }

    private fun showPlayGap(callback: IKSdkShowAdListener?) {
        lifecycleScope.launchWithSupervisorJob {
            var mAdDto: IKSdkInterDto? = null
            withContext(Dispatchers.Default) {
                IKDataRepository.getInstance()?.getSDKInter()?.let {
                    mAdDto = it
                }
                val playGapDto =
                    mAdDto?.adapters?.find { it.enable && it.adNetwork == AdNetwork.PLAYGAP.value }
                if (playGapDto != null && playGapDto.appKey?.isNotBlank() == true) {
//                    IKPlayGapHelper.initialize(playGapDto.appKey ?: "")
                } else {
                    runCatching {
                        countDownTimer?.cancel()
                    }
                    isBlockClick = false
                }
            }
            val playGapDto =
                mAdDto?.adapters?.find { it.enable && it.adNetwork == AdNetwork.PLAYGAP.value }
            if (playGapDto != null && playGapDto.appKey?.isNotBlank() == true) {
//                adPlayGap?.showAd(
//                    lifecycleScope,
//                    this@IkmInterAdActivity,
//                    IKSdkDefConst.AdScreen.NATIVE_INTER_CUSTOM_PG,
//                    "",
//                    object : IKSdkBaseListener() {
//                        override fun onAdClicked(
//                            adNetworkName: String,
//                            screen: String,
//                            scriptName: String,
//                            adUUID: String
//                        ) {
//
//                        }
//
//                        override fun onAdImpression(
//                            adNetworkName: String,
//                            screen: String,
//                            scriptName: String,
//                            adUUID: String
//                        ) {
//                        }
//
//                        override fun onAdShowed(
//                            adNetworkName: String,
//                            screen: String,
//                            scriptName: String,
//                            priority: Int,
//                            adUUID: String
//                        ) {
//                            callback?.onAdShowed(priority)
//                            forgeClose = true
//                            this@IkmInterAdActivity.finish()
//                        }
//
//                        override fun onAdShowFailed(
//                            adNetworkName: String,
//                            screen: String,
//                            scriptName: String,
//                            error: IKAdError
//                        ) {
//                            callback?.onAdShowFail(error)
//                        }
//
//                        override fun onAdDismissed(
//                            adNetworkName: String,
//                            screen: String,
//                            scriptName: String,
//                            adUUID: String
//                        ) {
//                            callback?.onAdDismiss()
//                            forgeClose = false
//                            this@IkmInterAdActivity.finish()
//                            adEventCallback?.onAdDismiss()
//                            adEventCallback = null
//                            countDownTimer?.cancel()
//                        }
//
//                    }) ?: callback?.onAdShowFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
            } else {
                runCatching {
                    countDownTimer?.cancel()
                }
                isBlockClick = false
            }
        }

    }


}