package com.example.ads.activity.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.ads.R
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.core.fcm.IkmCoreFMService
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IkmSdkCoreFunc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable


class IkmASideC : IkmSBaseAct() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setGravityDialog(Gravity.BOTTOM)
        setContentView(R.layout.ikm_at_nf)
        initView()
    }

    private fun initView() {
        var endpointC: Class<*>? = null
        IKSdkTrackingHelper.customizeTracking(
            IkmCoreFMService.IKN_TRACKING_TRACK,
            Pair("act", "op_mid"),
            Pair("ac_kd", "g2")
        )
        val enClOut = SDKDataHolder.FFun.getFValid(
            intent.getStringExtra(SDKDataHolder.FFun.getActOutCLKey()),
            true
        )
        if (enClOut) {
            findViewById<View>(R.id.ikAsTransContainer)?.visibility = View.VISIBLE
        } else {
            findViewById<View>(R.id.ikAsTransContainer)?.visibility = View.GONE
        }

        lifecycleScope.launch {
            val messageBody = withContext(Dispatchers.Default) {
                intent?.serializable(IkmCoreFMService.KEY_IKN_MBY) as? HashMap<String, String>
            }
            val iconApp = withContext(Dispatchers.Default) {
                packageManager?.getApplicationIcon(packageName)
            }
            runCatching {
                val imgIcon = findViewById<ImageView>(R.id.ikAsIcon)
                Glide.with(this@IkmASideC)
                    .load(iconApp)
                    .circleCrop()
                    .downsample(DownsampleStrategy.AT_MOST)
                    .into(imgIcon)
            }
            val endpoint = intent?.getStringExtra(SDKDataHolder.FFun.getActCLNKey())
            runCatching {
                endpointC = Class.forName(
                    endpoint ?: IKSdkDefConst.EMPTY
                )
            }
            if (endpointC == null)
                endpointC = IkmSdkCoreFunc.HandleEna.endPointAt
            findViewById<TextView>(R.id.ikAsTitle)?.text = withContext(Dispatchers.Default) {
                runCatching {
                    (messageBody?.get(SDKDataHolder.FFun.getCTtKeyC())
                        ?: IKSdkDefConst.EMPTY).ifBlank {
                        messageBody?.get("ikn_title")
                    }
                }.getOrNull()
            } ?: IKSdkDefConst.EMPTY
            findViewById<TextView>(R.id.ikAsDescription)?.text = withContext(Dispatchers.Default) {
                runCatching {
                    (messageBody?.get(SDKDataHolder.FFun.getCBdKeyC())
                        ?: IKSdkDefConst.EMPTY).ifBlank {
                        messageBody?.get("ikn_des")
                    }
                }.getOrNull()
            } ?: IKSdkDefConst.EMPTY

            val imgIcon = findViewById<ImageView>(R.id.ikAsImage)
            val image = withContext(Dispatchers.Default) {
                runCatching {
                    (messageBody?.get(SDKDataHolder.FFun.getCImKeyC())
                        ?: IKSdkDefConst.EMPTY).ifBlank {
                        messageBody?.get("ikn_image")
                    }
                }.getOrNull()
            } ?: IKSdkDefConst.EMPTY
            if (image.isBlank())
                runCatching {
                    Glide.with(this@IkmASideC)
                        .load(R.drawable.img_ikm_nf_default)
                        .downsample(DownsampleStrategy.AT_MOST)
                        .into(imgIcon)
                }
            else {
                runCatching {
                    Glide.with(this@IkmASideC)
                        .load(image)
                        .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.ads_margin_gone)))
                        .downsample(DownsampleStrategy.AT_MOST)
                        .into(imgIcon)
                }
            }
            findViewById<TextView>(R.id.ikAsTvAction)?.text = (withContext(Dispatchers.Default) {
                runCatching {
                    messageBody?.get(SDKDataHolder.FFun.getCBtTtKeyC())
                }.getOrNull()
            } ?: IKSdkDefConst.EMPTY).ifBlank { "Open" }

        }
        val enCl = runCatching {
            SDKDataHolder.FFun.getFValid(
                intent.getStringExtra(SDKDataHolder.FFun.getActCLKey()),
                true
            )
        }.getOrNull() ?: true

        findViewById<View>(R.id.ikAsContainerContent)?.setOnClickListener {
            runEndPoint(endpointC)
        }
        findViewById<View>(R.id.ikAsClose)?.setOnClickListener {
            if (enCl)
                finish()
            else
                runEndPoint(endpointC)
        }
        findViewById<View>(R.id.ikAsFullScreenContainer)?.setOnClickListener {
            if (enClOut) {
                runEndPoint(endpointC)
            }

        }
    }

    private fun runEndPoint(endpointC: Class<*>?) {
        runCatching {
            val intentN = Intent(this, endpointC)
            intentN.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            intentN.putExtra(
                IkmCoreFMService.NF_KEY,
                IkmCoreFMService.NF_VALUE
            )
            intentN.putExtra(
                IkmCoreFMService.NFX_KX,
                IkmCoreFMService.NFX_VX
            )
            startActivity(intentN)
            IKSdkTrackingHelper.customizeTracking(
                IkmCoreFMService.IKN_TRACKING_TRACK,
                Pair("act", "mid_cl"),
                Pair("ac_kd", "g2")
            )
            finish()

        }.onFailure {
            finish()
        }
    }

    private inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> runCatching {
            getSerializableExtra(
                key,
                T::class.java
            )
        }.getOrNull()

        else -> @Suppress("DEPRECATION") runCatching {
            getSerializableExtra(key) as? T
        }.getOrNull()
    }

}