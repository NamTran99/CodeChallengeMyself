package com.example.ads.activity.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.OnHierarchyChangeListener
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.gms.ads.MediaAspectRatio
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.example.ads.R
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.mediation.applovin.IkObjectNativeMax
import java.lang.ref.WeakReference

object IKWidgetAdUtil {
    object Applovin {
        object Native {
            fun setupNativeCustomAdView(
                context: Context?,
                adsObject: IkObjectNativeMax?,
                mapAdView: IkmWidgetAdLayout?
            ): MaxNativeAdView? {
                if (context == null || mapAdView == null || adsObject == null)
                    return null
                val ctc = WeakReference(context.applicationContext)
                return ctc.get()?.let {
                    val btnCall = if (mapAdView.callToActionView is Button)
                        mapAdView.callToActionView
                    else Button(context).apply {
//                        id = R.id.applovinBtnCallAction

                        mapAdView.callToActionView?.setOnClickListener {
                            this.performClick()
                        }
                        this.doOnTextChanged { text, _, _, _ ->
                            mapAdView.callToActionView?.text = text
                        }
                        visibility = View.INVISIBLE
                        mapAdView.addView(this, ViewGroup.LayoutParams(1, 1))
                    }
                    kotlin.runCatching {
                        mapAdView.parent?.let {
                            (it as? ViewGroup)?.removeView(mapAdView)
                        }
                    }
                    var binder: MaxNativeAdViewBinder = MaxNativeAdViewBinder.Builder(mapAdView)
                        .setTitleTextViewId(mapAdView.titleView?.id ?: 0)
                        .setBodyTextViewId(mapAdView.bodyView?.id ?: 0)
                        .setIconImageViewId(mapAdView.iconView?.id ?: 0)
                        .setMediaContentViewGroupId(mapAdView.mediaView?.id ?: 0)
                        .setCallToActionButtonId(btnCall?.id ?: 0)
                        .build()
                    mapAdView.mediaMixView?.visibility = View.GONE
                    mapAdView.mediaMixViewVideo?.visibility = View.GONE
                    if (mapAdView.isMixIconAndMediaView) {
                        if (adsObject.adData.nativeAd?.icon == null) {
                            mapAdView.iconView?.visibility = View.GONE
                            val mediaMixView = mapAdView.mediaMixViewVideo ?: mapAdView.mediaMixView
                            mediaMixView?.visibility = View.VISIBLE
                            binder = MaxNativeAdViewBinder.Builder(mapAdView)
                                .setTitleTextViewId(mapAdView.titleView?.id ?: 0)
                                .setBodyTextViewId(mapAdView.bodyView?.id ?: 0)
                                .setIconImageViewId(mapAdView.iconView?.id ?: 0)
                                .setMediaContentViewGroupId(mediaMixView?.id ?: 0)
                                .setCallToActionButtonId(btnCall?.id ?: 0)
                                .build()
                        }
                    }
                    val adView = MaxNativeAdView(binder, it)
                    adsObject.loader.render(adView, adsObject.adData)
                    return adView
                }
            }
        }

        object NativeFull {
             fun setupNativeCustomAdView(
                 context: Context?,
                 adsObject: IkObjectNativeMax?,
                 mapAdView: IkmWidgetAdLayout?
             ): MaxNativeAdView? {
                 if (context == null || mapAdView == null || adsObject == null)
                     return null
                val ctc = WeakReference(context.applicationContext)
                return ctc.get()?.let {
                    val btnCall =
                        if (mapAdView.callToActionView is Button) mapAdView.callToActionView
                        else Button(context).apply {
//                            id = R.id.applovinBtnCallAction

                            mapAdView.callToActionView?.setOnClickListener {
                                this.performClick()
                            }
                            this.doOnTextChanged { text, _, _, _ ->
                                mapAdView.callToActionView?.text = text
                            }
                            visibility = View.INVISIBLE
                            mapAdView.addView(this, ViewGroup.LayoutParams(1, 1))
                        }
                    kotlin.runCatching {
                        mapAdView.parent?.let {
                            (it as? ViewGroup)?.removeView(mapAdView)
                        }
                    }
                    val binder: MaxNativeAdViewBinder = MaxNativeAdViewBinder.Builder(mapAdView)
                        .setTitleTextViewId(mapAdView.titleView?.id ?: 0)
                        .setBodyTextViewId(mapAdView.bodyView?.id ?: 0)
                        .setIconImageViewId(mapAdView.iconView?.id ?: 0)
                        .setMediaContentViewGroupId(mapAdView.mediaView?.id ?: 0)
                        .setCallToActionButtonId(btnCall?.id ?: 0).build()
                    val adView = MaxNativeAdView(binder, it)
                    adsObject.loader.render(adView, adsObject.adData)
                    return adView
                }
            }


            private fun setupNativeCustomAdViewSquare(
                context: Context, adsObject: IkObjectNativeMax, mapAdView: IkmWALF
            ): MaxNativeAdView? {
                val ctc = WeakReference(context.applicationContext)
                return ctc.get()?.let {
                    val btnCall =
                        if (mapAdView.callToActionViewSquare is Button) mapAdView.callToActionViewSquare
                        else Button(context).apply {
//                            id = R.id.applovinBtnCallAction

                            mapAdView.callToActionViewSquare?.setOnClickListener {
                                this.performClick()
                            }
                            this.doOnTextChanged { text, _, _, _ ->
                                mapAdView.callToActionViewSquare?.text = text
                            }
                            visibility = View.INVISIBLE
                            mapAdView.addView(this, ViewGroup.LayoutParams(1, 1))
                        }
                    kotlin.runCatching {
                        mapAdView.parent?.let {
                            (it as? ViewGroup)?.removeView(mapAdView)
                        }
                    }
                    val binder: MaxNativeAdViewBinder = MaxNativeAdViewBinder.Builder(mapAdView)
                        .setTitleTextViewId(mapAdView.titleViewSquare?.id ?: 0)
                        .setBodyTextViewId(mapAdView.bodyViewSquare?.id ?: 0)
                        .setIconImageViewId(mapAdView.iconViewSquare?.id ?: 0)
                        .setMediaContentViewGroupId(mapAdView.mediaViewSquare?.id ?: 0)
                        .setCallToActionButtonId(btnCall?.id ?: 0).build()
                    val adView = MaxNativeAdView(binder, it)
                    adsObject.loader.render(adView, adsObject.adData)
                    return adView
                }
            }

            private fun setupNativeCustomAdViewPor(
                context: Context, adsObject: IkObjectNativeMax, mapAdView: IkmWALF
            ): MaxNativeAdView? {
                val ctc = WeakReference(context.applicationContext)
                return ctc.get()?.let {
                    val btnCall =
                        if (mapAdView.callToActionViewPor2 is Button) mapAdView.callToActionViewPor2
                        else Button(context).apply {
//                            id = R.id.applovinBtnCallAction

                            mapAdView.callToActionViewPor2?.setOnClickListener {
                                this.performClick()
                            }
                            this.doOnTextChanged { text, _, _, _ ->
                                mapAdView.callToActionViewPor2?.text = text
                            }
                            visibility = View.INVISIBLE
                            mapAdView.addView(this, ViewGroup.LayoutParams(1, 1))
                        }
                    kotlin.runCatching {
                        mapAdView.parent?.let {
                            (it as? ViewGroup)?.removeView(mapAdView)
                        }
                    }
                    mapAdView.customAnimateView2?.visibility = View.VISIBLE
                    val binder: MaxNativeAdViewBinder = MaxNativeAdViewBinder.Builder(mapAdView)
                        .setTitleTextViewId(mapAdView.titleViewPor2?.id ?: 0)
                        .setBodyTextViewId(mapAdView.bodyViewPor2?.id ?: 0)
                        .setIconImageViewId(mapAdView.iconViewPor2?.id ?: 0)
                        .setMediaContentViewGroupId(mapAdView.mediaViewPor?.id ?: 0)
                        .setCallToActionButtonId(btnCall?.id ?: 0).build()
                    val adView = MaxNativeAdView(binder, it)
                    adsObject.loader.render(adView, adsObject.adData)
                    return adView
                }
            }

            private fun IkmWALF.handleFullLayout(
                context: Context, adsObject: IkObjectNativeMax?
            ): MaxNativeAdView? {
                if (adsObject == null) return null
                fun setDefaultNativeAdView(): MaxNativeAdView? {
                    val ratio = adsObject.adData.nativeAd?.mediaContentAspectRatio ?: 0f
                    return when {
                        ratio == 1f -> {
                            this.adAspectRatio = MediaAspectRatio.SQUARE
                            this.displayContainerSquare()
                            setupNativeCustomAdViewSquare(
                                context, adsObject, this
                            )
                        }

                        ratio < 1f -> {
                            this.adAspectRatio = MediaAspectRatio.PORTRAIT
                            this.displayContainerPor()
                            setupNativeCustomAdViewPor(
                                context, adsObject, this
                            )
                        }

                        else -> {
                            this.adAspectRatio = MediaAspectRatio.LANDSCAPE
                            this.displayContainerNor()
                            setupNativeCustomAdView(
                                context, adsObject, this
                            )
                        }
                    }
                }
                return when (this.ikmWALFType) {
                    IkmWALFType.DEFAULT -> {
                        setDefaultNativeAdView()
                    }

                    IkmWALFType.NORMAL -> {
                        this.adAspectRatio = MediaAspectRatio.LANDSCAPE
                        this.displayContainerNor()
                        setupNativeCustomAdView(
                            context, adsObject, this
                        )

                    }

                    IkmWALFType.SQUARE -> {
                        this.adAspectRatio = MediaAspectRatio.SQUARE
                        this.displayContainerSquare()
                        setupNativeCustomAdViewSquare(
                            context, adsObject, this
                        )
                    }

                    IkmWALFType.PORTRAIT -> {
                        this.adAspectRatio = MediaAspectRatio.PORTRAIT
                        this.displayContainerPor()
                        setupNativeCustomAdViewPor(
                            context, adsObject, this
                        )
                    }
                }
            }

        }
    }

    object Admob {
        object Native {

            fun setupNativeCustomAdView(
                context: Context?, nativeAd: NativeAd?, mapAdView: IkmWidgetAdLayout?
            ): NativeAdView? {
                if (context == null || mapAdView == null || nativeAd == null || (nativeAd.headline == null && nativeAd.body == null))
                    return null
                val ctc = WeakReference(context.applicationContext)
                return ctc.get()?.let { contextRe ->
                    val adView = NativeAdView(contextRe)

                    adView.mediaView =
                        mapAdView.mediaView?.setupMediaView(AdNetwork.AD_MOB) as? MediaView
                    adView.headlineView =
                        mapAdView.titleView
                    adView.bodyView = mapAdView.bodyView
                    adView.callToActionView =
                        mapAdView.callToActionView
                    adView.iconView = mapAdView.iconView

                    (adView.headlineView as? TextView)?.text = nativeAd.headline
                    if (nativeAd.body == null) {
                        adView.bodyView?.visibility = View.GONE
                    } else {
                        adView.bodyView?.visibility = View.VISIBLE
                        (adView.bodyView as? TextView)?.text = nativeAd.body
                    }
                    if (nativeAd.callToAction == null) {
                        adView.callToActionView?.visibility = View.GONE
                    } else {
                        adView.callToActionView?.visibility = View.VISIBLE
                        (adView.callToActionView as? TextView)?.text = nativeAd.callToAction
                    }
                    var canShowIcon = true
                    if (nativeAd.icon == null) {
                        canShowIcon = false
                        adView.iconView?.visibility = View.INVISIBLE
                    } else {
                        if (mapAdView.roundIcon > 0)
                            runCatching {
                                (adView.iconView as? ImageView)?.let {
                                    Glide.with(it)
                                        .load(nativeAd.icon?.drawable)
                                        .transform(
                                            RoundedCorners(
                                                mapAdView.roundIcon
                                            )
                                        )
                                        .skipMemoryCache(true)
                                        .downsample(DownsampleStrategy.AT_MOST)
                                        .into(it)
                                }
                            }.onFailure {
                                if (nativeAd.icon?.drawable == null)
                                    canShowIcon = false
                                else
                                    (adView.iconView as? ImageView)?.setImageDrawable(
                                        nativeAd.icon?.drawable
                                    )
                            }
                        else {
                            if (nativeAd.icon?.drawable == null)
                                canShowIcon = false
                            else
                                (adView.iconView as? ImageView)?.setImageDrawable(
                                    nativeAd.icon?.drawable
                                )
                        }
                        if (canShowIcon)
                            adView.iconView?.visibility = View.VISIBLE
                    }

                    adView.starRatingView = mapAdView.starRatingView
                    adView.priceView = mapAdView.priceView
                    adView.storeView = mapAdView.storeView
                    adView.advertiserView = mapAdView.advertiserView

                    if (nativeAd.price == null) {
                        adView.priceView?.visibility = View.GONE
                    } else {
                        adView.priceView?.visibility = View.VISIBLE
                        (adView.priceView as? TextView)?.text = nativeAd.price
                    }
                    if (nativeAd.store == null) {
                        adView.storeView?.visibility = View.GONE
                    } else {
                        adView.storeView?.visibility = View.VISIBLE
                        (adView.storeView as? TextView)?.text = nativeAd.store
                    }
                    if (nativeAd.starRating == null) {
                        adView.starRatingView?.visibility = View.GONE
                    } else {
                        val startRate = nativeAd.starRating?.toFloat() ?: 0f
                        if (startRate > 0f) {
                            (adView.starRatingView as? TextView)?.text = startRate.toString()
                            adView.starRatingView?.visibility = View.VISIBLE
                        } else {
                            adView.starRatingView?.visibility = View.GONE
                        }
                    }

                    val vc = nativeAd.mediaContent?.videoController
                    if (vc?.hasVideoContent() == true) {
                        runCatching {
                            vc.mute(mapAdView.isMute)
                        }
//                vc.videoLifecycleCallbacks = object : VideoLifecycleCallbacks() {}
                    }
                    mapAdView.mediaMixView?.visibility = View.GONE
                    mapAdView.mediaMixViewVideo?.visibility = View.GONE
                    if (mapAdView.isMixIconAndMediaView) {
                        if (!canShowIcon) {
                            adView.iconView?.visibility = View.GONE
                            if (nativeAd.mediaContent != null) {
                                val mediaMixView =
                                    if (vc?.hasVideoContent() == true)
                                        mapAdView.mediaMixViewVideo
                                    else
                                        mapAdView.mediaMixView
                                adView.mediaView =
                                    mediaMixView?.setupMediaView(AdNetwork.AD_MOB) as? MediaView
                                adView.mediaView?.visibility = View.VISIBLE
                                mediaMixView?.visibility = View.VISIBLE
                                customSetupMediaView(adView, mediaMixView)
                            } else {
                                mapAdView.iconView?.visibility = View.INVISIBLE
                            }
                        }
                    } else customSetupMediaView(adView, mapAdView.mediaView)

                    adView.setNativeAd(nativeAd)
                    if (adView.callToActionView != null) adView.setClickConfirmingView(adView.callToActionView)

                    kotlin.runCatching {
                        mapAdView.parent?.let {
                            (it as? ViewGroup)?.removeView(mapAdView)
                        }
                    }
                    adView.addView(mapAdView)
                    return adView
                }
            }

            private fun customSetupMediaView(
                adView: NativeAdView,
                mediaView: IkmWidgetMediaView?
            ) {
                runCatching {
                    mediaView?.mediaAdjustViewBounds?.let { value ->
                        mediaView.setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
                            override fun onChildViewAdded(parent: View, child: View) {
                                if (child is ImageView) {
                                    kotlin.runCatching {
                                        child.adjustViewBounds = value
                                    }
                                }
                                mediaView.setOnHierarchyChangeListener(null)
                            }

                            override fun onChildViewRemoved(parent: View, child: View) {}
                        })
                    }
                    mediaView?.mediaScaleType?.let { value ->
                        adView.mediaView?.setImageScaleType(value)
                    }
                }
            }
        }

        object NativeFull {

             fun setupNativeCustomAdView(
                context: Context?, nativeAd: NativeAd?, mapAdView: IkmWidgetAdLayout?
            ): NativeAdView? {
                if (context == null || mapAdView == null || nativeAd == null || (nativeAd.headline == null && nativeAd.body == null))
                    return null
                val ctc = WeakReference(context.applicationContext)
                return ctc.get()?.let { contextRe ->
                    val adView = NativeAdView(contextRe)

                    adView.mediaView =
                        mapAdView.mediaView?.setupMediaView(AdNetwork.AD_MOB) as? MediaView
                    adView.headlineView = mapAdView.titleView
                    adView.bodyView = mapAdView.bodyView
                    adView.callToActionView = mapAdView.callToActionView
                    adView.iconView = mapAdView.iconView

                    (adView.headlineView as? TextView)?.text = nativeAd.headline
                    if (nativeAd.body == null) {
                        adView.bodyView?.visibility = View.GONE
                    } else {
                        adView.bodyView?.visibility = View.VISIBLE
                        (adView.bodyView as? TextView)?.text = nativeAd.body
                    }
                    if (nativeAd.callToAction == null) {
                        adView.callToActionView?.visibility = View.GONE
                    } else {
                        adView.callToActionView?.visibility = View.VISIBLE
                        (adView.callToActionView as? TextView)?.text = nativeAd.callToAction
                    }

                    if (nativeAd.icon == null) {
                        adView.iconView?.visibility = View.INVISIBLE
                    } else {
                        if (mapAdView.roundIcon > 0) runCatching {
                            (adView.iconView as? ImageView)?.let {
                                Glide.with(it).load(nativeAd.icon?.drawable).transform(
                                    RoundedCorners(
                                        mapAdView.roundIcon
                                    )
                                ).skipMemoryCache(true)
                                    .downsample(DownsampleStrategy.AT_MOST).into(it)
                            }
                        }.onFailure {
                            (adView.iconView as? ImageView)?.setImageDrawable(
                                nativeAd.icon?.drawable
                            )
                        }
                        else (adView.iconView as? ImageView)?.setImageDrawable(
                            nativeAd.icon?.drawable
                        )

                        adView.iconView?.visibility = View.VISIBLE
                    }

                    adView.starRatingView = mapAdView.starRatingView
                    adView.priceView = mapAdView.priceView
                    adView.storeView = mapAdView.storeView
                    adView.advertiserView = mapAdView.advertiserView

                    if (nativeAd.price == null) {
                        adView.priceView?.visibility = View.GONE
                    } else {
                        adView.priceView?.visibility = View.VISIBLE
                        (adView.priceView as? TextView)?.text = nativeAd.price
                    }
                    if (nativeAd.store == null) {
                        adView.storeView?.visibility = View.GONE
                    } else {
                        adView.storeView?.visibility = View.VISIBLE
                        (adView.storeView as? TextView)?.text = nativeAd.store
                    }
                    if (nativeAd.starRating == null) {
                        adView.starRatingView?.visibility = View.GONE
                    } else {
                        val startRate = nativeAd.starRating?.toFloat() ?: 0f
                        if (startRate > 0f) {
                            (adView.starRatingView as? TextView)?.text = startRate.toString()
                            adView.starRatingView?.visibility = View.VISIBLE
                        } else {
                            adView.starRatingView?.visibility = View.GONE
                        }
                    }

                    adView.setNativeAd(nativeAd)

                    if (adView.callToActionView != null) adView.setClickConfirmingView(adView.callToActionView)
                    val vc = nativeAd.mediaContent?.videoController
                    if (vc?.hasVideoContent() == true) {
                        runCatching {
                            vc.mute(mapAdView.isMute)
                        }
//                vc.videoLifecycleCallbacks = object : VideoLifecycleCallbacks() {}
                    }
                    customSetupMediaView(adView, mapAdView)
                    kotlin.runCatching {
                        mapAdView.parent?.let {
                            (it as? ViewGroup)?.removeView(mapAdView)
                        }
                    }
                    adView.addView(mapAdView)
                    return adView
                }
            }

            private fun customSetupMediaView(
                adView: NativeAdView, mapAdView: IkmWidgetAdLayout
            ) {
                runCatching {
                    adView.mediaView?.children?.forEach { view ->
                        if (view is ImageView) {
                            mapAdView.mediaView?.mediaAdjustViewBounds?.let { value ->
                                (view as? ImageView)?.adjustViewBounds = value
                            }
                            mapAdView.mediaView?.mediaScaleType?.let { value ->
                                (view as? ImageView)?.scaleType = value
                            }
                        }
                    }
                }
            }

            private fun setupNativeCustomAdViewSquare(
                context: Context, nativeAd: NativeAd?, mapAdView: IkmWALF
            ): NativeAdView? {
                if (nativeAd == null || (nativeAd.headline == null && nativeAd.body == null)) return null
                val ctc = WeakReference(context.applicationContext)
                return ctc.get()?.let { contextRe ->
                    val adView = NativeAdView(contextRe)

                    adView.mediaView =
                        mapAdView.mediaViewSquare?.setupMediaView(AdNetwork.AD_MOB) as? MediaView
                    adView.headlineView = mapAdView.titleViewSquare
                    adView.bodyView = mapAdView.bodyViewSquare
                    adView.callToActionView = mapAdView.callToActionViewSquare
                    adView.iconView = mapAdView.iconViewSquare

                    (adView.headlineView as? TextView)?.text = nativeAd.headline
                    if (nativeAd.body == null) {
                        adView.bodyView?.visibility = View.GONE
                    } else {
                        adView.bodyView?.visibility = View.VISIBLE
                        (adView.bodyView as? TextView)?.text = nativeAd.body
                    }
                    if (nativeAd.callToAction == null) {
                        adView.callToActionView?.visibility = View.GONE
                    } else {
                        adView.callToActionView?.visibility = View.VISIBLE
                        (adView.callToActionView as? TextView)?.text = nativeAd.callToAction
                    }

                    if (nativeAd.icon == null) {
                        adView.iconView?.visibility = View.GONE
                    } else {
                        if (mapAdView.roundIcon > 0) runCatching {
                            (adView.iconView as? ImageView)?.let {
                                Glide.with(it).load(nativeAd.icon?.drawable).transform(
                                    RoundedCorners(
                                        mapAdView.roundIcon
                                    )
                                ).skipMemoryCache(true)
                                    .downsample(DownsampleStrategy.AT_MOST).into(it)
                            }
                        }.onFailure {
                            (adView.iconView as? ImageView)?.setImageDrawable(
                                nativeAd.icon?.drawable
                            )
                        }
                        else (adView.iconView as? ImageView)?.setImageDrawable(
                            nativeAd.icon?.drawable
                        )

                        adView.iconView?.visibility = View.VISIBLE
                    }

                    adView.starRatingView = mapAdView.starRatingViewSquare
                    adView.priceView = mapAdView.priceViewSquare
                    adView.storeView = mapAdView.storeViewSquare
                    adView.advertiserView = mapAdView.advertiserViewSquare

                    if (nativeAd.price == null) {
                        adView.priceView?.visibility = View.GONE
                    } else {
                        adView.priceView?.visibility = View.VISIBLE
                        (adView.priceView as? TextView)?.text = nativeAd.price
                    }
                    if (nativeAd.store == null) {
                        adView.storeView?.visibility = View.GONE
                    } else {
                        adView.storeView?.visibility = View.VISIBLE
                        (adView.storeView as? TextView)?.text = nativeAd.store
                    }
                    if (nativeAd.starRating == null) {
                        adView.starRatingView?.visibility = View.GONE
                    } else {
                        val startRate = nativeAd.starRating?.toFloat() ?: 0f
                        if (startRate > 0f) {
                            (adView.starRatingView as? TextView)?.text = startRate.toString()
                            adView.starRatingView?.visibility = View.VISIBLE
                        } else {
                            adView.starRatingView?.visibility = View.GONE
                        }
                    }

                    adView.setNativeAd(nativeAd)

                    if (adView.callToActionView != null) adView.setClickConfirmingView(adView.callToActionView)
                    val vc = nativeAd.mediaContent?.videoController
                    if (vc?.hasVideoContent() == true) {
                        runCatching {
                            vc.mute(mapAdView.isMute)
                        }
//                vc.videoLifecycleCallbacks = object : VideoLifecycleCallbacks() {}
                    }
                    customSetupMediaView(adView, mapAdView)
                    kotlin.runCatching {
                        mapAdView.parent?.let {
                            (it as? ViewGroup)?.removeView(mapAdView)
                        }
                    }
                    adView.addView(mapAdView)
                    return adView
                }
            }

            private fun setupNativeCustomAdViewPor(
                context: Context, nativeAd: NativeAd?, mapAdView: IkmWALF
            ): NativeAdView? {
                if (nativeAd == null || (nativeAd.headline == null && nativeAd.body == null)) return null
                val ctc = WeakReference(context.applicationContext)
                return ctc.get()?.let { contextRe ->
                    val adView = NativeAdView(contextRe)
                    adView.mediaView =
                        mapAdView.mediaViewPor?.setupMediaView(AdNetwork.AD_MOB) as? MediaView

                    val vc = nativeAd.mediaContent?.videoController
                    if (vc?.hasVideoContent() == true) {
                        adView.headlineView = mapAdView.titleViewPor
                        adView.bodyView = mapAdView.bodyViewPor
                        adView.callToActionView = mapAdView.callToActionViewPor
                        adView.iconView = mapAdView.iconViewPor
                        adView.starRatingView = mapAdView.starRatingViewPor
                        adView.storeView = mapAdView.storeViewPor
                    } else {
                        adView.headlineView = mapAdView.titleViewPor2
                        adView.bodyView = mapAdView.bodyViewPor2
                        adView.callToActionView = mapAdView.callToActionViewPor2
                        adView.iconView = mapAdView.iconViewPor2
                        adView.starRatingView = mapAdView.starRatingViewPor2
                        adView.storeView = mapAdView.storeViewPor2
                    }
                    (adView.headlineView as? TextView)?.text = nativeAd.headline
                    if (nativeAd.body == null) {
                        adView.bodyView?.visibility = View.GONE
                    } else {
                        adView.bodyView?.visibility = View.VISIBLE
                        (adView.bodyView as? TextView)?.text = nativeAd.body
                    }
                    if (nativeAd.callToAction == null) {
                        adView.callToActionView?.visibility = View.GONE
                    } else {
                        adView.callToActionView?.visibility = View.VISIBLE
                        (adView.callToActionView as? TextView)?.text = nativeAd.callToAction
                    }

                    if (nativeAd.icon == null) {
                        adView.iconView?.visibility = View.GONE
                    } else {
                        if (mapAdView.roundIcon > 0) runCatching {
                            (adView.iconView as? ImageView)?.let {
                                Glide.with(it).load(nativeAd.icon?.drawable).transform(
                                    RoundedCorners(
                                        mapAdView.roundIcon
                                    )
                                ).skipMemoryCache(true)
                                    .downsample(DownsampleStrategy.AT_MOST).into(it)
                            }
                        }.onFailure {
                            (adView.iconView as? ImageView)?.setImageDrawable(
                                nativeAd.icon?.drawable
                            )
                        }
                        else (adView.iconView as? ImageView)?.setImageDrawable(
                            nativeAd.icon?.drawable
                        )

                        adView.iconView?.visibility = View.VISIBLE
                    }


                    adView.priceView = mapAdView.priceViewPor
                    adView.advertiserView = mapAdView.advertiserViewPor

                    if (nativeAd.price == null) {
                        adView.priceView?.visibility = View.GONE
                    } else {
                        adView.priceView?.visibility = View.VISIBLE
                        (adView.priceView as? TextView)?.text = nativeAd.price
                    }
                    if (nativeAd.store == null) {
                        adView.storeView?.visibility = View.GONE
                    } else {
                        adView.storeView?.visibility = View.VISIBLE
                        (adView.storeView as? TextView)?.text = nativeAd.store
                    }
                    if (nativeAd.starRating == null) {
                        adView.starRatingView?.visibility = View.GONE
                    } else {
                        val startRate = nativeAd.starRating?.toFloat() ?: 0f
                        if (startRate > 0f) {
                            (adView.starRatingView as? TextView)?.text = startRate.toString()
                            adView.starRatingView?.visibility = View.VISIBLE
                        } else {
                            adView.starRatingView?.visibility = View.GONE
                        }
                    }

                    adView.setNativeAd(nativeAd)

                    if (adView.callToActionView != null) adView.setClickConfirmingView(adView.callToActionView)
                    if (vc?.hasVideoContent() == true) {
                        runCatching {
                            vc.mute(mapAdView.isMute)
                        }
                        if (mapAdView.customAnimateView != null) vc.videoLifecycleCallbacks =
                            object : VideoController.VideoLifecycleCallbacks() {
                                override fun onVideoEnd() {
                                    super.onVideoEnd()
                                    runCatching {
                                        mapAdView.customAnimateView?.visibility = View.VISIBLE
                                    }
                                }
                            }
//                vc.videoLifecycleCallbacks = object : VideoLifecycleCallbacks() {}
                    } else {
                        mapAdView.customAnimateView2?.visibility = View.VISIBLE
                    }
                    customSetupMediaView(adView, mapAdView)
                    kotlin.runCatching {
                        mapAdView.parent?.let {
                            (it as? ViewGroup)?.removeView(mapAdView)
                        }
                    }
                    adView.addView(mapAdView)
                    return adView
                }
            }

            private fun IkmWALF.handleFullLayout(
                context: Context, nativeAd: NativeAd?
            ): NativeAdView? {
                if (nativeAd == null || (nativeAd.headline == null && nativeAd.body == null)) return null
                fun setDefaultNativeAdView(): NativeAdView? {
                    val ratio = nativeAd.mediaContent?.aspectRatio ?: 0f
                    return when {
                        ratio == 1f -> {
                            this.adAspectRatio = MediaAspectRatio.SQUARE
                            this.displayContainerSquare()

                            setupNativeCustomAdViewSquare(
                                context, nativeAd, this
                            )
                        }

                        ratio < 1f -> {
                            this.adAspectRatio = MediaAspectRatio.PORTRAIT
                            this.displayContainerPor()

                            setupNativeCustomAdViewPor(
                                context, nativeAd, this
                            )
                        }

                        else -> {
                            this.adAspectRatio = MediaAspectRatio.LANDSCAPE
                            this.displayContainerNor()
                            setupNativeCustomAdView(
                                context, nativeAd, this
                            )
                        }
                    }
                }
                return when (this.ikmWALFType) {
                    IkmWALFType.DEFAULT -> {
                        setDefaultNativeAdView()
                    }

                    IkmWALFType.NORMAL -> {
                        this.adAspectRatio = MediaAspectRatio.LANDSCAPE
                        this.displayContainerNor()
                        setupNativeCustomAdView(
                            context, nativeAd, this
                        )

                    }

                    IkmWALFType.SQUARE -> {
                        this.adAspectRatio = MediaAspectRatio.SQUARE
                        this.displayContainerSquare()
                        setupNativeCustomAdViewSquare(
                            context, nativeAd, this
                        )

                    }

                    IkmWALFType.PORTRAIT -> {
                        this.adAspectRatio = MediaAspectRatio.PORTRAIT
                        this.displayContainerPor()
                        setupNativeCustomAdViewPor(
                            context, nativeAd, this
                        )
                    }
                }
            }
        }
    }

    object Gam {
        object Native {

            @SuppressLint("SetTextI18n")
             fun setupNativeCustomAdView(
                context: Context?, nativeAd: NativeAd?, mapAdView: IkmWidgetAdLayout?
            ): NativeAdView? {
                if (context == null || mapAdView == null || nativeAd == null || (nativeAd.headline == null && nativeAd.body == null))
                    return null
                val ctc = WeakReference(context.applicationContext)
                return ctc.get()?.let { contextRe ->
                    val adView = NativeAdView(contextRe)

                    adView.mediaView =
                        mapAdView.mediaView?.setupMediaView(AdNetwork.AD_MOB) as? MediaView
                    adView.headlineView =
                        mapAdView.titleView
                    adView.bodyView = mapAdView.bodyView
                    adView.callToActionView =
                        mapAdView.callToActionView
                    adView.iconView = mapAdView.iconView

                    (adView.headlineView as? TextView)?.text = nativeAd.headline
                    if (nativeAd.body == null) {
                        adView.bodyView?.visibility = View.GONE
                    } else {
                        adView.bodyView?.visibility = View.VISIBLE
                        (adView.bodyView as? TextView)?.text = nativeAd.body
                    }
                    if (nativeAd.callToAction == null) {
                        adView.callToActionView?.visibility = View.GONE
                    } else {
                        adView.callToActionView?.visibility = View.VISIBLE
                        (adView.callToActionView as? TextView)?.text = nativeAd.callToAction
                    }
                    var canShowIcon = true
                    if (nativeAd.icon == null) {
                        canShowIcon = false
                        adView.iconView?.visibility = View.INVISIBLE
                    } else {
                        if (mapAdView.roundIcon > 0)
                            runCatching {
                                (adView.iconView as? ImageView)?.let {
                                    Glide.with(it)
                                        .load(nativeAd.icon?.drawable)
                                        .transform(
                                            RoundedCorners(
                                                mapAdView.roundIcon
                                            )
                                        )
                                        .skipMemoryCache(true)
                                        .downsample(DownsampleStrategy.AT_MOST)
                                        .into(it)
                                }
                            }.onFailure {
                                if (nativeAd.icon?.drawable == null)
                                    canShowIcon = false
                                else
                                    (adView.iconView as? ImageView)?.setImageDrawable(
                                        nativeAd.icon?.drawable
                                    )
                            }
                        else {
                            if (nativeAd.icon?.drawable == null)
                                canShowIcon = false
                            else
                                (adView.iconView as? ImageView)?.setImageDrawable(
                                    nativeAd.icon?.drawable
                                )
                        }
                        if (canShowIcon)
                            adView.iconView?.visibility = View.VISIBLE
                    }

                    adView.starRatingView = mapAdView.starRatingView
                    adView.priceView = mapAdView.priceView
                    adView.storeView = mapAdView.storeView
                    adView.advertiserView = mapAdView.advertiserView

                    if (nativeAd.price == null) {
                        adView.priceView?.visibility = View.GONE
                    } else {
                        adView.priceView?.visibility = View.VISIBLE
                        (adView.priceView as? TextView)?.text = nativeAd.price
                    }
                    if (nativeAd.store == null) {
                        adView.storeView?.visibility = View.GONE
                    } else {
                        adView.storeView?.visibility = View.VISIBLE
                        (adView.storeView as? TextView)?.text = nativeAd.store
                    }
                    if (nativeAd.starRating == null) {
                        adView.starRatingView?.visibility = View.GONE
                    } else {
                        val startRate = nativeAd.starRating?.toFloat() ?: 0f
                        if (startRate > 0f) {
                            (adView.starRatingView as? TextView)?.text = startRate.toString()
                            adView.starRatingView?.visibility = View.VISIBLE
                        } else {
                            adView.starRatingView?.visibility = View.GONE
                        }
                    }

                    val vc = nativeAd.mediaContent?.videoController
                    if (vc?.hasVideoContent() == true) {
                        runCatching {
                            vc.mute(mapAdView.isMute)
                        }
//                vc.videoLifecycleCallbacks = object : VideoLifecycleCallbacks() {}
                    }
                    mapAdView.mediaMixView?.visibility = View.GONE
                    mapAdView.mediaMixViewVideo?.visibility = View.GONE
                    if (mapAdView.isMixIconAndMediaView) {
                        if (!canShowIcon) {
                            adView.iconView?.visibility = View.GONE
                            if (nativeAd.mediaContent != null) {
                                val mediaMixView =
                                    if (vc?.hasVideoContent() == true)
                                        mapAdView.mediaMixViewVideo
                                    else
                                        mapAdView.mediaMixView
                                adView.mediaView =
                                    mediaMixView?.setupMediaView(AdNetwork.AD_MOB) as? MediaView
                                adView.mediaView?.visibility = View.VISIBLE
                                mediaMixView?.visibility = View.VISIBLE
                                customSetupMediaView(adView, mediaMixView)
                            } else {
                                mapAdView.iconView?.visibility = View.INVISIBLE
                            }
                        }
                    } else customSetupMediaView(adView, mapAdView.mediaView)

                    adView.setNativeAd(nativeAd)
                    if (adView.callToActionView != null) adView.setClickConfirmingView(adView.callToActionView)
                    kotlin.runCatching {
                        mapAdView.parent?.let {
                            (it as? ViewGroup)?.removeView(mapAdView)
                        }
                    }
                    adView.addView(mapAdView)
                    return adView
                }
            }

            private fun customSetupMediaView(
                adView: NativeAdView,
                mediaView: IkmWidgetMediaView?
            ) {
                runCatching {
                    mediaView?.mediaAdjustViewBounds?.let { value ->
                        mediaView.setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
                            override fun onChildViewAdded(parent: View, child: View) {
                                if (child is ImageView) {
                                    kotlin.runCatching {
                                        child.adjustViewBounds = value
                                    }
                                }
                                mediaView.setOnHierarchyChangeListener(null)
                            }

                            override fun onChildViewRemoved(parent: View, child: View) {}
                        })
                    }
                    mediaView?.mediaScaleType?.let { value ->
                        adView.mediaView?.setImageScaleType(value)
                    }
                }
            }
        }
    }
}