package com.example.ads.activity.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ads.R
import com.example.ads.activity.core.fcm.IkmCoreFMService
import com.example.ads.activity.data.db.IKDataRepository
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.listener.pub.IKShowWidgetAdListener
import com.example.ads.activity.listener.sdk.IKSdkShowAdListener
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.widgets.IkmWALF
//import com.example.ads.activity.widgets.IkmWidgetAdView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class IkmOpenAdActivity : AppCompatActivity() {
    private var isBlockClick = true

    companion object {
        var adEventCallback: IKSdkShowAdListener? = null
        const val TIME_OUT = 4000L
        fun showAd(context: Context?, eventCallback: IKSdkShowAdListener? = null) {
            adEventCallback = eventCallback
            runCatching {
                if (context == null) {
                    adEventCallback?.onAdShowFail(IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID))
                    adEventCallback = null
                    return
                }
                context.startActivity(Intent(context, IkmOpenAdActivity::class.java))
            }.onFailure {
                adEventCallback?.onAdShowFail(IKAdError(IKSdkErrorCode.RUNNING_EXCEPTION))
                adEventCallback = null
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adEventCallback?.onAdDismiss()
        adEventCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.ikml_open_activity)
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
            Pair("act", "ct_opc"),
            Pair("ac_kd", "g4")
        )
        val appText = findViewById<TextView>(R.id.custom_appText)
        val appIcon = findViewById<ImageView>(R.id.custom_appIcon)
        val customCloseBtn = findViewById<View>(R.id.custom_close_btn)
//        val adsLoadingView = findViewById<View>(R.id.custom_adsLoadingView)
        runCatching {
            appText?.text = getApplicationName()
        }
        kotlin.runCatching {
            appIcon?.setImageDrawable(getAppIcon())
        }
        adEventCallback?.onAdShowed(0)

        findViewById<View>(R.id.custom_close_container)?.setOnClickListener {
            if (isBlockClick)
                return@setOnClickListener
            adEventCallback?.onAdDismiss()
            adEventCallback = null
            finish()
        }
        lifecycleScope.launch {
            var delayTime =
                IKDataRepository.getInstance()
                    ?.getConfigNCL(IKSdkDefConst.AdScreen.NATIVE_OPEN_CUSTOM)?.timeOutClose
                    ?: TIME_OUT
            if (delayTime < TIME_OUT)
                delayTime = TIME_OUT
            delay(delayTime)
            isBlockClick = false
//            adsLoadingView?.visibility = View.GONE
            customCloseBtn?.visibility = View.VISIBLE
        }
        loadAd()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        if (!isBlockClick)
            finish()
    }

    private fun loadAd() {
//        val adView = findViewById<IkmWidgetAdView>(R.id.openAd_containerAds)
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
//        adView?.setEnableShimmer(false)
        adLayout?.enableClickOutside()
        adLayout?.setRoundIconValue(resources?.getDimensionPixelOffset(R.dimen.sdk_icon_round) ?: 0)
        findViewById<View>(R.id.openAd_loadingAds)?.visibility =
            View.VISIBLE
        val customCloseBtn = findViewById<View>(R.id.custom_close_btn)
        if (adLayout != null) {
//            adView?.loadAdFS(
//                adLayout,
//                IKSdkDefConst.AdScreen.NATIVE_OPEN_CUSTOM,
//                object : IKShowWidgetAdListener {
//
//                    override fun onAdShowed() {
//                        findViewById<View>(R.id.openAd_loadingAds)?.visibility =
//                            View.GONE
//                    }
//
//                    override fun onAdShowFail(error: IKAdError) {
//                        isBlockClick = false
//                        customCloseBtn?.visibility = View.VISIBLE
//                    }
//                }
//            )
        }
    }

    private fun getApplicationName(): String {
        return runCatching {
            val applicationInfo: ApplicationInfo = applicationInfo
            val stringId: Int = applicationInfo.labelRes
            return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else getString(
                stringId
            )
        }.getOrNull() ?: IKSdkDefConst.EMPTY
    }

    private fun getAppIcon(): Drawable? {
        return runCatching {
            packageManager.getApplicationIcon(packageName)
        }.getOrNull()
    }
}