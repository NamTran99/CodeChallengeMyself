package com.example.ads.activity.tracking

import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustEvent
import com.example.ads.activity.IKSdkConstants
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkUtilsCore
import com.example.ads.activity.utils.IKTrackingConst
import com.example.ads.activity.utils.IKTrackingConst.mapNewAdFormat
import com.example.ads.activity.utils.IkmSdkCoreFunc

object IKSdkTrackingHelper {


    fun logEventRetention(context: Context?) {
//        CoreTracking.logEventRetention(context)
    }


    fun trackingSdkShowAd(
        adFormat: String,
        adStatus: String,
        screen: String,
        vararg multiValue: Pair<String, String>
    ) {
//        CoreTracking.trackingSdkAd(
//            adFormat = adFormat,
//            adStatus = adStatus,
//            screen = screen,
//            actionWithAds = IKSdkDefConst.AdAction.SHOW,
//            multiValue = multiValue
//        )
    }

    fun measureAdRevenue(
        platform: String,
        adUnitName: String,
        adFormat: String,
        adSource: String,
        revenue: Double
    ) {
//        CoreTracking.measureAdRevenue(
//            platform = platform, adUnitName = adUnitName, adFormat = adFormat,
//            adSource = adSource, revenue = revenue
//        )
    }

    fun customPaidAd(
        adNetwork: String,
        revMicros: Double,
        currency: String?,
        adUnitId: String?,
        responseAdNetwork: String?,
        adFormat: String,
        adId: String = "",
        screen: String
    ) {
//        var enableTrackingThreshold = true
//        val eventName = if (IkmSdkCoreFunc.HandleEna.onHandleEnable) {
//            enableTrackingThreshold = false
//            IKSdkConstants.PAID_AD_IMPRESSION_EVENT_CUSTOM
//        } else IKSdkConstants.PAID_AD_IMPRESSION_EVENT
//        CoreTracking.trackingCustomPaidAd(
//            eventName = eventName,
//            valueMicros = revMicros,
//            currency = currency,
//            adUnitId = adUnitId,
//            network = responseAdNetwork,
//            adPlatform = adNetwork,
//            adFormat = adFormat,
//            enableTrackingThreshold = enableTrackingThreshold,
//            adId,
//            screen
//        )
//        CoreTracking.trackingCustomPaidAd(
//            IKSdkConstants.PAID_AD_IMPRESSION_EVENT_ALL,
//            revMicros,
//            currency,
//            adUnitId,
//            responseAdNetwork,
//            adNetwork,
//            adFormat,
//            false,
//            adId,
//            screen
//        )
//        kotlin.runCatching {
//            CoreTracking.trackingAdjustPartnerWithRevenue(
//                IKSdkConstants.ADJUST_AD_IMPRESSION_TOKEN, revMicros,
//                currency ?: IKSdkDefConst.CURRENCY_CODE_USD,
//                hashMapOf(
//                    Pair(
//                        IKTrackingConst.ParamName.AD_TYPE, when (adFormat) {
//                            IKSdkDefConst.AdFormat.BANNER,
//                            IKSdkDefConst.AdFormat.BANNER_INLINE,
//                            IKSdkDefConst.AdFormat.BANNER_COLLAPSE,
//                            -> IKTrackingConst.ParamName.BANNER
//
//                            IKSdkDefConst.AdFormat.NATIVE,
//                            IKSdkDefConst.AdFormat.NATIVE_BANNER -> IKTrackingConst.ParamName.NATIVE
//
//                            IKSdkDefConst.AdFormat.REWARD,
//                            IKSdkDefConst.AdFormat.REWARDED_INTER -> IKTrackingConst.ParamName.REWARDED_VIDEO
//
//                            IKSdkDefConst.AdFormat.INTER,
//                            IKSdkDefConst.AdFormat.OPEN -> IKTrackingConst.ParamName.INTERSTITIAL
//
//                            else -> IKSdkDefConst.UNKNOWN
//                        }
//                    )
//                )
//            )
//        }
    }


    fun customizeTracking(
        eventName: String,
        vararg param: Pair<String, String?>
    ) {
//        CoreTracking.customizeTracking(eventName, false, *param)
    }


    fun trackingAdjustPurchase(
        revenue: Double,
        currencyCode: String,
        token: String
    ) {
        if (token.isBlank())
            return
        kotlin.runCatching {
            val adjustEvent = AdjustEvent(token)
            adjustEvent.setRevenue(revenue / 1_000_000, currencyCode)
            Adjust.trackEvent(adjustEvent)
        }
    }


    fun customizeTrackingAdjustPartner(
        token: String,
        param: HashMap<String, String>
    ) {
        kotlin.runCatching {
//            CoreTracking.customizeTrackingAdjustPartner(token, param)
        }
    }


    fun customizeTrackingAdjustCallback(
        token: String,
        param: HashMap<String, String>
    ) {
        kotlin.runCatching {
//            CoreTracking.customizeTrackingAdjustCallback(token, param)
        }
    }


    fun trackingAdjustPurchaseNew(
        token: String,
        productId: String,
        orderId: String,
        purchaseTime: String,
        productType: String,
        purchaseToken: List<String>
    ) {
        if (token.isBlank())
            return
        kotlin.runCatching {
            val param: HashMap<String, String> = hashMapOf()
            param[IKTrackingConst.ParamName.PRODUCT_ID] = productId
            param[IKTrackingConst.ParamName.ORDER_ID] = orderId
            param[IKTrackingConst.ParamName.PURCHASE_TIME] = purchaseTime
            param[IKTrackingConst.ParamName.PRODUCT_TYPE] = productType
            if (purchaseToken.size <= 1) {
                kotlin.runCatching {
                    param[IKTrackingConst.ParamName.PURCHASE_TOKEN] = purchaseToken.first()
                }
            }
            purchaseToken.forEachIndexed { index, s ->
                param[IKTrackingConst.ParamName.PURCHASE_TOKEN + "_$index"] = s
            }

//            CoreTracking.customizeTrackingAdjustPartner(token, param)
        }
    }

    fun trackingAdjustInAppPurchase(
        token: String,
        productId: String,
        orderId: String,
        purchaseTime: String,
        productType: String,
        revenue: Double,
        currencyCode: String,
        purchaseToken: String,
    ) {
        if (token.isBlank())
            return
        kotlin.runCatching {
            val param: HashMap<String, String> = hashMapOf()
            param[IKTrackingConst.ParamName.PRODUCT_ID] = productId
            param[IKTrackingConst.ParamName.ORDER_ID] = orderId
            param[IKTrackingConst.ParamName.PURCHASE_TIME] = purchaseTime
            param[IKTrackingConst.ParamName.PRODUCT_TYPE] = productType
            param[IKTrackingConst.ParamName.PURCHASE_TOKEN] = purchaseToken
//            CoreTracking.trackingAdjustInAppPurchase(token, revenue, currencyCode, param)
        }

    }

    fun trackingSdkLoadAd(
        startTime: Long,
        priority: Int,
        adFormat: String,
        adUnit: String,
        adNetwork: String,
        adStatus: String,
        adUUID: String,
        vararg multiValue: Pair<String, String>
    ) {
//        CoreTracking.customizeTracking(
//            IKTrackingConst.EventName.AD_TRACK, false,
//            Pair(IKTrackingConst.ParamName.TIME, "${IKSdkUtilsCore.minusLoadTime(startTime)}"),
//            Pair(IKTrackingConst.ParamName.PRIORITY, "$priority"),
//            Pair(IKTrackingConst.ParamName.AD_NETWORK, adNetwork),
//            Pair(IKTrackingConst.ParamName.AD_UNIT, adUnit),
//            Pair(IKTrackingConst.ParamName.SUB_AD_FORMAT, adFormat),
//            Pair(IKTrackingConst.ParamName.AD_FORMAT, mapNewAdFormat(adFormat)),
//            Pair(IKTrackingConst.ParamName.AD_STATUS, adStatus),
//            Pair(IKTrackingConst.ParamName.AD_ACTION, IKSdkDefConst.AdAction.LOAD),
//            Pair(IKTrackingConst.ParamName.AD_UUID, adUUID),
//            *multiValue
//        )
    }
}
