package com.example.ads.activity.mediation.gam

import android.content.Context
import com.google.android.gms.ads.AdSize
import com.example.ads.activity.data.db.IKDataRepository
import com.example.ads.activity.data.dto.pub.IKAdFormat
import com.example.ads.activity.data.dto.sdk.data.IKAdSizeDto
import com.example.ads.activity.utils.IKSdkDefConst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BannerInlineGam : BannerGam() {
    override var adFormatName: String = IKSdkDefConst.AdFormat.BANNER_INLINE

    override suspend fun getAdmobAdSize(context: Context, screen: String?): AdSize {
        val adSize = getAdSize(screen, context)
        return adSize
    }

    private suspend fun getAdSize(
        screen: String?,
        context: Context
    ): AdSize {
        val adSizeConfig: IKAdSizeDto =
            getBannerInlineAdSize(screen) ?: return adaptiveSize(context)

        try {
            when (adSizeConfig.adType) {
                IKSdkDefConst.AdSize.NORMAL -> {
                    return normalSize(context)
                }

                IKSdkDefConst.AdSize.ADAPTIVE -> {
                    return adaptiveSize(context)
                }


                IKSdkDefConst.AdSize.MANUAL -> {
                    val width = adSizeConfig.width ?: 0
                    val height = adSizeConfig.height ?: 0
                    if (width == 0 || height == 0)
                        return this.adaptiveSize(context)
                    return AdSize.getInlineAdaptiveBannerAdSize(width, height)
                }

                IKSdkDefConst.AdSize.MANUAL_HEIGHT -> {
                    val height = adSizeConfig.height ?: 0
                    if (height == 0)
                        return this.adaptiveSize(context)
                    return kotlin.runCatching {
                        val (adWidthPixels, density) = calculatorAdSize(context)
                        if (density == 0f) return@runCatching AdSize.BANNER
                        val adWidth = (adWidthPixels / density).toInt()
                        AdSize.getInlineAdaptiveBannerAdSize(adWidth, height)
                    }.getOrNull() ?: this.adaptiveSize(context)
                }

                IKSdkDefConst.AdSize.MANUAL_WIDTH -> {
                    val width = adSizeConfig.width ?: 0
                    if (width == 0)
                        return this.adaptiveSize(context)
                    return kotlin.runCatching {
                        val (adWidthPixels, density) = calculatorAdSize(context)
                        if (density == 0f) return@runCatching AdSize.BANNER
                        val effectiveWidth = if (width > 10) width else adWidthPixels
                        val adWidth = (effectiveWidth / density).toInt()
                        AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, adWidth)
                    }.getOrNull() ?: this.adaptiveSize(context)
                }
            }
        } catch (e: Exception) {
            return this.adaptiveSize(context)
        }
        return this.adaptiveSize(context)
    }

    private suspend fun adaptiveSize(context: Context, width: Int = 0): AdSize =
        withContext(Dispatchers.Default) {
            try {
                val (adWidthPixels, density) = calculatorAdSize(context)
                if (density == 0f) return@withContext AdSize.MEDIUM_RECTANGLE
                val effectiveWidth = if (width > 10) width else adWidthPixels
                val adWidth = (effectiveWidth / density).toInt()
                AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, adWidth)
            } catch (e: Exception) {
                AdSize.MEDIUM_RECTANGLE
            }
        }

    private suspend fun normalSize(context: Context, width: Int = 0): AdSize =
        withContext(Dispatchers.Default) {
            try {
                val (adWidthPixels, density) = calculatorAdSize(context)
                if (density == 0f) return@withContext AdSize.MEDIUM_RECTANGLE
                val effectiveWidth = if (width > 10) width else adWidthPixels
                var adWidth = (effectiveWidth / density).toInt()
                if (adWidth < 300)
                    adWidth = 300
                AdSize.getInlineAdaptiveBannerAdSize(adWidth, 250)
            } catch (e: Exception) {
                AdSize.MEDIUM_RECTANGLE
            }
        }

    private suspend fun getBannerInlineAdSize(screen: String?): IKAdSizeDto? {
        return if (screen == null) {
            null
        } else {
            IKDataRepository.getConfigWidget(screen)?.adSize
        }
    }
}