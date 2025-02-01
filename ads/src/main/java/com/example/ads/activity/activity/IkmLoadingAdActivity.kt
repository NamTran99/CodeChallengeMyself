package com.example.ads.activity.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ads.R
import com.example.ads.activity.core.fcm.IkmCoreFMService
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.listener.pub.IKShowAdListener
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


open class IkmLoadingAdActivity : AppCompatActivity() {

    companion object {

        fun showActivity(context: Context?, eventCallback: IKShowAdListener? = null) {
            runCatching {
                context?.startActivity(Intent(context, IkmLoadingAdActivity::class.java))
            }.onFailure {
                eventCallback?.onAdsShowFail(IKAdError(IKSdkErrorCode.RUNNING_EXCEPTION))
            }
        }
    }

    private var mLoadingRunner = kotlinx.coroutines.Runnable {
        mLoadingDone = true
    }
    private var mLoadingDone = false
        set(value) {
            if (value) {
                mAdsHandler?.removeCallbacks(mLoadingRunner)
                mAdsHandler?.postDelayed(mLoadingRunner, 2000)
            } else {
                mAdsHandler?.removeCallbacks(mLoadingRunner)
            }
            field = value
        }

    private val mAdsHandler: Handler? by lazy {
        Handler(Looper.getMainLooper())
    }
    private var onResume = false
    private var onPause = false
    private fun reOpenActivity() {
        val pm: PackageManager? = packageManager
        val a = pm?.getLaunchIntentForPackage(packageName)
//        finishAffinity()
        a?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(a)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.layout_activity_loading_ad)
        IKSdkTrackingHelper.customizeTracking(
            IkmCoreFMService.IKN_TRACKING_TRACK,
            Pair("act", "lding_mid"),
            Pair("ac_kd", "g3")
        )
        supportActionBar?.hide()
        findViewById<View>(R.id.adLoading_Action)?.setOnClickListener {
            reOpenActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        onResume = true
        onPause = false
        lifecycleScope.launch {
            delay(500)
            if (onPause)
                onResume = false
            delay(500)
            if (onPause)
                onResume = false
            if (onResume)
                reOpenActivity()
        }

    }

    override fun onPause() {
        super.onPause()
        onPause = true
    }

}