//package com.example.ads.activity.widgets
//
//import android.animation.Animator
//import android.animation.AnimatorSet
//import android.animation.ObjectAnimator
//import android.annotation.SuppressLint
//import android.content.Context
//import android.content.res.Resources
//import android.os.Build
//import android.util.AttributeSet
//import android.util.DisplayMetrics
//import android.util.Log
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.FrameLayout
//import android.widget.FrameLayout.LayoutParams
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.PopupWindow
//import androidx.annotation.LayoutRes
//import androidx.core.view.doOnAttach
//import androidx.core.view.doOnDetach
//import com.applovin.mediation.ads.MaxAdView
//import com.google.android.gms.ads.AdView
//import com.google.android.gms.ads.admanager.AdManagerAdView
//import com.google.android.gms.ads.nativead.NativeAd
//import com.google.android.gms.ads.nativead.NativeAdView
//import com.example.ads.R
//import com.example.ads.activity.data.db.IKDataRepository
//import com.example.ads.activity.data.dto.pub.IKAdError
//import com.example.ads.activity.data.dto.pub.IKAdFormat
//import com.example.ads.activity.data.dto.pub.IKNativeTemplate
//import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
//import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
//import com.example.ads.activity.data.dto.sdk.data.IKSdkProdWidgetDetailDto
//import com.example.ads.activity.format.banner.IKBannerCollapseController
//import com.example.ads.activity.format.banner.IKBannerController
//import com.example.ads.activity.format.banner.IKBannerInlineController
//import com.example.ads.activity.format.custom.IKBannerCollapseBannerController
//import com.example.ads.activity.format.custom.IKBannerCollapseInlineController
//import com.example.ads.activity.format.custom.IKNativeCollapseNativeController
//import com.example.ads.activity.format.native_ads.IKNativeController
//import com.example.ads.activity.format.native_ads.IKNativeFullScreenController
//import com.example.ads.activity.format.native_ads.NativeBackUpLatest
//import com.example.ads.activity.listener.pub.IKShowWidgetAdListener
//import com.example.ads.activity.listener.sdk.IKSdkBaseTrackingListener
//import com.example.ads.activity.listener.sdk.IKSdkShowWidgetAdListener
//import com.example.ads.activity.mediation.applovin.IKApplovinHelper
//import com.example.ads.activity.mediation.applovin.IkObjectNativeMax
//import com.example.ads.activity.tracking.CoreTracking
//import com.example.ads.activity.tracking.IKSdkTrackingHelper
//import com.example.ads.activity.utils.IKLogs
//import com.example.ads.activity.utils.IKSdkDefConst
//import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import com.example.ads.activity.utils.IKSdkUtilsCore
//import com.example.ads.activity.utils.IKTrackingConst
//import com.example.ads.activity.utils.IkmSdkCoreFunc
//import kotlinx.coroutines.CompletableDeferred
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.async
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.withContext
//
//class IKSdkWidgetAdLoader {
//    companion object {
//        const val TAG_LOG = "IkmWidgetAdView"
//        const val BACKUP_BANNER_INLINE = "backup_banner_inline"
//        const val BACKUP_BANNER = "backup_banner"
//        const val BACKUP_BANNER_COLLAPSE = "backup_banner_cl"
//        const val SMALL_BANNER_INLINE_HEIGHT = 100
//    }
//
//    var mIsAdLoaded: Boolean = false
//        private set
//    var mIsRecall: Boolean = false
//        private set
//    var mIsAdLoading: Boolean = false
//        private set
//    var mEnableShimmer: Boolean = true
//    private var currentAdView: IKSdkBaseLoadedAd<*>? = null
//    var isParentDestroy: Boolean = false
//
//    @LayoutRes
//    var mLoadingAdLayout: Int = -1
//
//    var mAdLayout: IkmWidgetAdLayout? = null
//    private var mScreen = ""
//    var nativeTemplate: IKNativeTemplate = IKNativeTemplate.NORMAL_LAYOUT
//    var mEnableFullView: Boolean = false
//    var uiScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
//    var adViewLayout: FrameLayout? = null
//        private set
//    private var mAdViewLoading: FrameLayout? = null
//
//    private var mCurrentListener: IKShowWidgetAdListener? = null
//    private var mCurrentRecallListener: IKShowWidgetAdListener? = null
//    private var mCurrentAdFormat = IKSdkDefConst.AdFormat.WIDGET
//    private var mAutoReload = false
//    private var mFromAutoReload = false
//    private var mFrameAdView: IkmWidgetAdView? = null
//    private var mContext: Context? = null
//    private var mCollapseCount = 0
//    private var reloadJob: Job? = null
//    private var startTime: Long = 0L
//    var showCollapseWhenRecal = true
//    private var mCurrentAddUuid: String? = null
//
//    val mRepository: IKDataRepository? by lazy {
//        IKDataRepository.getInstance()
//    }
//
//    val mTrackingListener: IKSdkBaseTrackingListener = object : IKSdkBaseTrackingListener(
//        "", IKSdkDefConst.AdFormat.BANNER,
//        "", ""
//    ) {}
//
//    fun initViews(context: Context, attrs: AttributeSet?, frameLayout: IkmWidgetAdView) {
//        mFrameAdView = frameLayout
//        mContext = context
//        if (adViewLayout != null)
//            return
////        adViewLayout = FrameLayout(context).apply {
////            id = R.id.ikWidgetAdLayoutView
////        }
////        mAdViewLoading = FrameLayout(context).apply {
////            id = R.id.ikWidgetAdLoadingLayoutView
////        }
//        mFrameAdView?.addView(
//            adViewLayout,
//            centerLayoutParam()
//        )
////        val loadingView = LayoutInflater.from(context).inflate(
////            attrs?.let {
////                val typedArray = context.obtainStyledAttributes(it, R.styleable.IKWidgetAdViewCore)
////                val layoutPreview =
////                    if (typedArray.hasValue(R.styleable.IKWidgetAdViewCore_ikwad_preview_layout)) {
////                        typedArray.getResourceId(
////                            R.styleable.IKWidgetAdViewCore_ikwad_preview_layout,
////                            -1
////                        )
////                    } else {
////                        -1
////                    }
////                typedArray.recycle()
////                if (layoutPreview != -1) layoutPreview else R.layout.shimmer_loading_banner
////            } ?: R.layout.shimmer_loading_banner,
////            mAdViewLoading, false
////        )
//        mFrameAdView?.addView(
//            mAdViewLoading,
//            centerLayoutParam()
//        )
//        kotlin.runCatching {
////            mAdViewLoading?.addView(loadingView)
//        }
//        if (mFrameAdView?.background == null) {
//            mFrameAdView?.setBackgroundResource(R.color.color_ads_bg)
//        }
//    }
//
//    private fun showShimmer(adFormat: IKAdFormat) {
//        showLogD("showShimmer") { "$adFormat - start show" }
//        uiScope.launchWithSupervisorJob {
//            if (mContext == null)
//                return@launchWithSupervisorJob
//            mAdViewLoading?.removeAllViewsInLayout()
//            val adLoading = if (mLoadingAdLayout != -1) {
//                showLogD("showShimmer") { "$adFormat - show custom" }
//                LayoutInflater.from(mContext).inflate(
//                    mLoadingAdLayout,
//                    mAdViewLoading, false
//                )
//            } else {
//                val layoutLoading = when (adFormat) {
//                    IKAdFormat.BANNER -> R.layout.shimmer_loading_banner
//                    IKAdFormat.NATIVE -> R.layout.shimmer_loading_native
//                    IKAdFormat.NATIVE_BANNER -> R.layout.shimmer_loading_native_banner
//                    IKAdFormat.MREC -> R.layout.shimmer_loading_native
//                    IKAdFormat.BANNER_INLINE -> R.layout.shimmer_loading_native
//                    IKAdFormat.BANNER_COLLAPSE -> R.layout.shimmer_loading_banner
//                    IKAdFormat.BANNER_COLLAPSE_C1 -> R.layout.shimmer_loading_banner
//                    IKAdFormat.BANNER_COLLAPSE_C2 -> R.layout.shimmer_loading_banner
//                    else -> R.layout.shimmer_loading_banner
//                }
//                showLogD("showShimmer") { "$adFormat - show default" }
//                LayoutInflater.from(mContext).inflate(
//                    layoutLoading,
//                    mAdViewLoading, false
//                )
//            }
//            kotlin.runCatching {
//                mAdViewLoading?.addView(adLoading)
//            }
//        }
//    }
//
//    fun hideShimmer() {
//        showLogD("hideShimmer") { "start hide" }
//        uiScope.launchWithSupervisorJob {
//            mAdViewLoading?.visibility = View.GONE
//        }
//    }
//
//    private fun removeAdLayout() {
//        uiScope.launchWithSupervisorJob {
//            adViewLayout?.removeAllViewsInLayout()
//        }
//    }
//
//    fun loadAdCore(
//        screen: String,
//        adListener: IKShowWidgetAdListener?
//    ) {
//        mScreen = screen
//        mIsAdLoaded = false
//        mIsAdLoading = true
//        mIsRecall = false
//        mCurrentListener = null
//        mCurrentListener = adListener
//        showLogD("loadAd") { "screen = $screen, startLoad" }
//
//        uiScope.launchWithSupervisorJob(Dispatchers.IO) {
//            if (!IKSdkUtilsCore.canShowAdAsync()) {
//                showLogD("loadAd") { "screen = $screen, error: ${IKSdkErrorCode.USER_PREMIUM}" }
//                uiScope.launchWithSupervisorJob {
//                    mCurrentListener?.onAdShowFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
//                }
//                return@launchWithSupervisorJob
//            }
//            if (mFrameAdView == null) {
//                uiScope.launchWithSupervisorJob {
//                    mCurrentListener?.onAdShowFail(IKAdError(IKSdkErrorCode.VIEW_GROUP_NULL))
//                }
//                return@launchWithSupervisorJob
//            }
//
//            trackingWidget(
//                adStatus = IKSdkDefConst.AdStatus.PRE_SHOW,
//                screen = screen
//            )
//
//            val sdkAdListener = object : IKShowWidgetAdListener {
//                override fun onAdShowed() {
//                    mIsAdLoaded = true
//                    mIsAdLoading = false
//                    uiScope.launchWithSupervisorJob {
//                        mCurrentListener?.onAdShowed()
//                    }
//                    trackingWidget(
//                        adStatus = IKSdkDefConst.AdStatus.SHOWED,
//                        screen = screen
//                    )
//                    showLogD("loadAd") { "screen = $screen, showed" }
//                }
//
//                override fun onAdShowFail(error: IKAdError) {
//                    if (!mIsRecall)
//                        mIsAdLoaded = false
//                    mIsAdLoading = false
//                    uiScope.launchWithSupervisorJob(Dispatchers.Main) {
//                        mCurrentListener?.onAdShowFail(error)
//                    }
//                    trackingWidget(
//                        adStatus = IKSdkDefConst.AdStatus.SHOW_FAIL,
//                        screen = screen,
//                        Pair(IKTrackingConst.ParamName.ERROR_CODE, "${error.code}"),
//                        Pair(IKTrackingConst.ParamName.MESSAGE, error.message)
//                    )
//                    showLogD("loadAd") { "screen = $screen, show fail: $error" }
//                }
//
//                override fun onAdClick() {
//                    uiScope.launchWithSupervisorJob {
//                        mCurrentListener?.onAdClick()
//                    }
//                    showLogD("loadAd") { "screen = $screen, onAdClick" }
//                }
//            }
//            if (mFrameAdView?.visibility == View.GONE) {
//                showLogD("loadAd") { "screen = $screen, show fail: ${IKSdkErrorCode.VIEW_GROUP_NOT_VISIBLE}" }
//                sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.VIEW_GROUP_NOT_VISIBLE))
//                return@launchWithSupervisorJob
//            }
//            val configDto = uiScope.async(Dispatchers.IO) {
//                mRepository?.getConfigWidget(screen)
//            }.await()
//
//            if (configDto == null) {
//                showLogD("loadAd") { "screen = $screen, show fail: ${IKSdkErrorCode.NO_SCREEN_ID_AD}" }
//                sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.NO_SCREEN_ID_AD))
//                return@launchWithSupervisorJob
//            }
//            if (configDto.enable != true) {
//                showLogD("loadAd") { "screen = $screen, show fail: ${IKSdkErrorCode.DISABLE_SHOW}" }
//                sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.DISABLE_SHOW))
//                return@launchWithSupervisorJob
//            }
//            removeAdLayout()
//            showLogD("loadAd") { "screen = $screen, start show adFormat: ${configDto.adFormat}" }
//            mTrackingListener.apply {
//                this.screen = screen
//                this.adFormat = configDto.adFormat
//                this.isRecall = false
//            }
//            if (configDto.adFormat != IKAdFormat.BANNER_COLLAPSE_C1.value ||
//                configDto.adFormat != IKAdFormat.BANNER_COLLAPSE_C2.value
//            )
//                mTrackingListener.onAdPreShow(screen = mScreen, "")
//
//            when (configDto.adFormat) {
//                IKAdFormat.BANNER.value -> {
//                    showShimmer(IKAdFormat.BANNER)
//                    showBannerNormal(screen, sdkAdListener, configDto)
//                }
//
//                IKAdFormat.BANNER_INLINE.value, IKAdFormat.MREC.value -> {
//                    showShimmer(IKAdFormat.BANNER_INLINE)
//                    showBannerNormalInline(screen, sdkAdListener, configDto, true)
//                }
//
//                IKAdFormat.BANNER_COLLAPSE.value -> {
//                    showShimmer(IKAdFormat.BANNER_COLLAPSE)
//                    showBannerCollapse(screen, sdkAdListener, configDto, false)
//                }
//
//                IKAdFormat.NATIVE.value -> {
//                    showShimmer(IKAdFormat.NATIVE)
//                    showNativeNormal(screen, sdkAdListener, configDto)
//                }
//
//                IKAdFormat.NATIVE_BANNER.value -> {
//                    showShimmer(IKAdFormat.NATIVE_BANNER)
//                    showNativeBannerNormal(screen, sdkAdListener, configDto)
//                }
//
//                IKAdFormat.BANNER_COLLAPSE_C1.value -> {
//                    showShimmer(IKAdFormat.BANNER_COLLAPSE_C1)
//                    mCollapseCount = configDto.collapseCount ?: 0
//                    showBannerCollapseC1(screen, sdkAdListener, configDto)
//                }
//
//                IKAdFormat.BANNER_COLLAPSE_C2.value -> {
//                    showShimmer(IKAdFormat.BANNER_COLLAPSE_C2)
//                    mCollapseCount = configDto.collapseCount ?: 0
//                    showBannerCollapseC2(screen, sdkAdListener, configDto)
//                }
//            }
//        }
//    }
//
//    private fun loadAdCoreB1(
//        currentScreen: String,
//        adFormat: IKAdFormat,
//        adListener: IKShowWidgetAdListener?
//    ) {
//        showLogD("loadAdCoreB1") { "format: $adFormat, screen: $currentScreen startLoad" }
//        mIsAdLoaded = false
//        mIsAdLoading = true
//        mIsRecall = false
//        uiScope.launchWithSupervisorJob(Dispatchers.IO) {
//            if (!IKSdkUtilsCore.canShowAdAsync()) {
//                showLogD("loadAdCoreB1") { "format: $adFormat, screen: $currentScreen error: ${IKSdkErrorCode.USER_PREMIUM}" }
//                adListener?.onAdShowFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
//                return@launchWithSupervisorJob
//            }
//            val screenNew: String
//            val configDto = when (adFormat) {
//
//                IKAdFormat.BANNER_INLINE, IKAdFormat.MREC -> {
//                    mRepository
//                        ?.getConfigWidget(currentScreen + IKSdkDefConst.AdScreen.PREFIX_BACKUP_BANNER_INLINE)
//                        ?: mRepository?.getConfigWidget(BACKUP_BANNER_INLINE)
//                }
//
//                IKAdFormat.BANNER_COLLAPSE -> {
//                    mRepository
//                        ?.getConfigWidget(currentScreen + IKSdkDefConst.AdScreen.PREFIX_BACKUP_BANNER_COLLAPSE)
//                        ?: mRepository?.getConfigWidget(BACKUP_BANNER_COLLAPSE)
//                }
//
//                else -> {
//                    null
//                }
//            }
//
//            screenNew = configDto?.screenName ?: BACKUP_BANNER
//            showLogD("loadAdCoreB1") { "format: $adFormat, screenNew: $screenNew" }
//            mScreen = screenNew
//
//            trackingWidget(
//                adStatus = IKSdkDefConst.AdStatus.PRE_SHOW,
//                screen = screenNew
//            )
//
//
//            val sdkAdListener = object : IKShowWidgetAdListener {
//                override fun onAdShowed() {
//                    mIsAdLoaded = true
//                    mIsAdLoading = false
//                    adListener?.onAdShowed()
//                    trackingWidget(
//                        adStatus = IKSdkDefConst.AdStatus.SHOWED,
//                        screen = screenNew
//                    )
//                    showLogD("loadAdCoreB1") { "format: $adFormat, screenNew: $screenNew onAdReady" }
//                }
//
//                override fun onAdShowFail(error: IKAdError) {
//                    if (!mIsRecall)
//                        mIsAdLoaded = false
//                    mIsAdLoading = false
//                    adListener?.onAdShowFail(error)
//                    trackingWidget(
//                        adStatus = IKSdkDefConst.AdStatus.SHOW_FAIL,
//                        screen = screenNew,
//                        Pair(IKTrackingConst.ParamName.ERROR_CODE, "${error.code}"),
//                        Pair(IKTrackingConst.ParamName.MESSAGE, error.message)
//                    )
//                    showLogD("loadAdCoreB1") { "format: $adFormat, screenNew: $screenNew onAdShowFail error: $error" }
//                }
//
//                override fun onAdClick() {
//                    adListener?.onAdClick()
//                    showLogD("loadAdCoreB1") { "format: $adFormat, screenNew: $screenNew onAdClick" }
//                }
//            }
//
//            if (mFrameAdView?.visibility == View.GONE) {
//                sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.VIEW_GROUP_NOT_VISIBLE))
//                return@launchWithSupervisorJob
//            }
//
//            if (configDto == null) {
//                sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.NO_SCREEN_ID_AD))
//                return@launchWithSupervisorJob
//            }
//            if (configDto.enable != true) {
//                sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.DISABLE_SHOW))
//                return@launchWithSupervisorJob
//            }
//
//            removeAdLayout()
//            showLogD("loadAdCoreB1") { "format: $adFormat, screenNew: $screenNew start show adFormat: ${configDto.adFormat}" }
//
//            mTrackingListener.apply {
//                this.screen = currentScreen
//                this.adFormat = configDto.adFormat
//                this.isRecall = false
//            }
//            if (configDto.adFormat != IKAdFormat.BANNER_COLLAPSE_C1.value ||
//                configDto.adFormat != IKAdFormat.BANNER_COLLAPSE_C2.value
//            )
//                mTrackingListener.onAdPreShow(screen = mScreen, "")
//
//            when (configDto.adFormat) {
//                IKAdFormat.BANNER.value -> {
//                    showBannerNormal(currentScreen, sdkAdListener, configDto)
//                }
//
//                IKAdFormat.BANNER_INLINE.value, IKAdFormat.MREC.value -> {
//                    showBannerNormalInline(currentScreen, sdkAdListener, configDto, false)
//                }
//
//                IKAdFormat.BANNER_COLLAPSE.value -> {
//                    showBannerCollapse(currentScreen, sdkAdListener, configDto, false)
//                }
//
//                IKAdFormat.NATIVE.value -> {
//                    showNativeNormal(screenNew, sdkAdListener, configDto)
//                }
//
//                IKAdFormat.NATIVE_BANNER.value -> {
//                    showNativeBannerNormal(screenNew, sdkAdListener, configDto)
//                }
//
//                IKAdFormat.BANNER_COLLAPSE_C1.value -> {
//                    showBannerCollapseC1(screenNew, sdkAdListener, configDto)
//                }
//
//                IKAdFormat.BANNER_COLLAPSE_C2.value -> {
//                    showBannerCollapseC2(screenNew, sdkAdListener, configDto)
//                }
//
//                else -> {
//                    showLogD("loadAdCoreB1") { "format: $adFormat, screenNew: $screenNew showFail: ${IKSdkErrorCode.NO_DATA_TO_LOAD_AD}" }
//                    sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
//                }
//            }
//        }
//
//    }
//
//    private fun showBannerCollapse(
//        screen: String,
//        sdkAdListener: IKShowWidgetAdListener,
//        configDto: IKSdkProdWidgetDetailDto,
//        enableLoadB1: Boolean
//    ) {
//        mCurrentAdFormat = IKSdkDefConst.AdFormat.BANNER_COLLAPSE
//
//        IKBannerCollapseController.showAd(
//            screen,
//            configDto,
//            object : IKSdkShowWidgetAdListener {
//                override fun onAdReady(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    hideShimmer()
//
//                    handleShowAdView(adData,
//                        onSuccess = {
//                            mTrackingListener.onAdShowed(
//                                adNetworkName,
//                                screen,
//                                scriptName,
//                                currentAdView?.adPriority ?: 0,
//                                currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                            )
//                            sdkAdListener.onAdShowed()
//                        }, onFail = { err ->
//                            onAdShowFail(err, scriptName, adNetworkName)
//                        })
//                }
//
//                override fun onAdShowFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    if (enableLoadB1) {
//                        loadAdCoreB1(
//                            screen,
//                            IKAdFormat.BANNER_COLLAPSE,
//                            sdkAdListener
//                        )
//                        return
//                    }
//                    mTrackingListener.onAdShowFailed(
//                        adNetworkName,
//                        screen,
//                        scriptName,
//                        error
//                    )
//
//                    if (NativeBackUpLatest.getBackupAd(IKAdFormat.BANNER_COLLAPSE) == null) {
//                        sdkAdListener.onAdShowFail(error)
//                        return
//                    }
//
//                    showNativeBackupLatest(
//                        adFormat = IKAdFormat.BANNER_COLLAPSE,
//                        scriptName = scriptName,
//                        screen = screen,
//                        configDto = configDto,
//                        sdkAdListener = sdkAdListener
//                    )
//                }
//
//                override fun onAdClick(scriptName: String, adNetworkName: String) {
//                    sdkAdListener.onAdClick()
//                    mTrackingListener.onAdClicked(
//                        adNetworkName,
//                        screen,
//                        scriptName,
//                        currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                    )
//                }
//
//                override fun onAdImpression(scriptName: String, adNetworkName: String) {
//                    mTrackingListener.onAdImpression(
//                        adNetworkName,
//                        screen,
//                        scriptName,
//                        currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                    )
//                }
//
//                override fun onAdReloaded(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    handleShowAdView(adData,
//                        onSuccess = {
//
//                        }, onFail = { _ ->
//
//                        })
//                }
//
//                override fun onAdReloadFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//
//                }
//
//            }
//        )
//    }
//
//    private fun showBannerNormalInline(
//        screen: String,
//        sdkAdListener: IKShowWidgetAdListener,
//        configDto: IKSdkProdWidgetDetailDto,
//        enableLoadB1: Boolean
//    ) {
//        mCurrentAdFormat = IKSdkDefConst.AdFormat.BANNER_INLINE
//        IKBannerInlineController.showAd(
//            screen,
//            configDto,
//            object : IKSdkShowWidgetAdListener {
//                override fun onAdReady(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    hideShimmer()
//                    handleShowAdView(adData,
//                        onSuccess = {
//                            mTrackingListener.onAdShowed(
//                                adNetworkName,
//                                screen,
//                                scriptName,
//                                currentAdView?.adPriority ?: 0,
//                                currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                            )
//                            sdkAdListener.onAdShowed()
//                            autoReload(configDto)
//                        }, onFail = { err ->
//                            onAdShowFail(err, scriptName, adNetworkName)
//                        })
//                }
//
//                override fun onAdShowFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    if (enableLoadB1)
//                        loadAdCoreB1(
//                            screen,
//                            IKAdFormat.BANNER_INLINE,
//                            object : IKShowWidgetAdListener {
//                                override fun onAdShowed() {
//                                    sdkAdListener.onAdShowed()
//                                }
//
//                                override fun onAdShowFail(error: IKAdError) {
//                                    if (NativeBackUpLatest.getBackupAd(IKAdFormat.BANNER_INLINE) == null) {
//                                        sdkAdListener.onAdShowFail(error)
//                                        return
//                                    }
//
//                                    showNativeBackupLatest(
//                                        adFormat = IKAdFormat.BANNER_INLINE,
//                                        scriptName = scriptName,
//                                        screen = screen,
//                                        configDto = configDto,
//                                        sdkAdListener = sdkAdListener,
//                                    )
//                                }
//
//                                override fun onAdClick() {
//                                    sdkAdListener.onAdClick()
//                                }
//                            }
//                        )
//                    else {
//                        mTrackingListener.onAdShowFailed(
//                            adNetworkName,
//                            screen,
//                            scriptName,
//                            error
//                        )
//
//                        if (NativeBackUpLatest.getBackupAd(IKAdFormat.BANNER_INLINE) == null) {
//                            sdkAdListener.onAdShowFail(error)
//                            return
//                        }
//
//                        showNativeBackupLatest(
//                            adFormat = IKAdFormat.BANNER_INLINE,
//                            scriptName = scriptName,
//                            screen = screen,
//                            configDto = configDto,
//                            sdkAdListener = sdkAdListener
//                        )
//                    }
//                }
//
//                override fun onAdClick(scriptName: String, adNetworkName: String) {
//                    sdkAdListener.onAdClick()
//                    mTrackingListener.onAdClicked(
//                        adNetworkName,
//                        screen,
//                        scriptName,
//                        currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                    )
//                }
//
//                override fun onAdImpression(scriptName: String, adNetworkName: String) {
//                    mTrackingListener.onAdImpression(
//                        adNetworkName,
//                        screen,
//                        scriptName,
//                        currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                    )
//                }
//
//                override fun onAdReloaded(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    handleShowAdView(adData,
//                        onSuccess = {
//
//                        }, onFail = { _ ->
//
//                        })
//                }
//
//                override fun onAdReloadFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                }
//
//            }
//        )
//    }
//
//    private fun showBannerNormal(
//        screen: String,
//        sdkAdListener: IKShowWidgetAdListener,
//        configDto: IKSdkProdWidgetDetailDto
//    ) {
//        mCurrentAdFormat = IKSdkDefConst.AdFormat.BANNER
//
//        IKBannerController.showAd(
//            screen,
//            configDto,
//            object : IKSdkShowWidgetAdListener {
//                override fun onAdReady(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    hideShimmer()
//
//                    handleShowAdView(adData,
//                        onSuccess = {
//                            mTrackingListener.onAdShowed(
//                                adNetworkName,
//                                screen,
//                                scriptName,
//                                currentAdView?.adPriority ?: 0,
//                                currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                            )
//                            sdkAdListener.onAdShowed()
//                            autoReload(configDto)
//                        }, onFail = { err ->
//                            onAdShowFail(err, scriptName, adNetworkName)
//                        })
//                }
//
//                override fun onAdShowFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    mTrackingListener.onAdShowFailed(
//                        adNetworkName,
//                        screen,
//                        scriptName,
//                        error
//                    )
//
//                    if (NativeBackUpLatest.getBackupAd(IKAdFormat.BANNER) == null) {
//                        sdkAdListener.onAdShowFail(error)
//                        return
//                    }
//
//                    showNativeBackupLatest(
//                        adFormat = IKAdFormat.BANNER,
//                        scriptName = scriptName,
//                        screen = screen,
//                        configDto = configDto,
//                        sdkAdListener = sdkAdListener
//                    )
//                }
//
//                override fun onAdClick(scriptName: String, adNetworkName: String) {
//                    sdkAdListener.onAdClick()
//                    mTrackingListener.onAdClicked(
//                        adNetworkName,
//                        screen,
//                        scriptName,
//                        currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                    )
//                }
//
//                override fun onAdImpression(scriptName: String, adNetworkName: String) {
//                    mTrackingListener.onAdImpression(
//                        adNetworkName,
//                        screen,
//                        scriptName,
//                        currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                    )
//                }
//
//                override fun onAdReloaded(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    handleShowAdView(adData,
//                        onSuccess = {
//
//                        }, onFail = { _ ->
//
//                        })
//                }
//
//                override fun onAdReloadFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                }
//
//            }
//        )
//    }
//
//    private fun showNativeBannerNormal(
//        screen: String,
//        sdkAdListener: IKShowWidgetAdListener,
//        configDto: IKSdkProdWidgetDetailDto
//    ) {
//        mCurrentAdFormat = IKSdkDefConst.AdFormat.NATIVE_BANNER
//        nativeTemplate = IKNativeTemplate.BANNER_LAYOUT
//        @SuppressLint("InflateParams")
//        val layoutView =
//            if (mAdLayout != null) mAdLayout else getTempAdLayout()
//        if (layoutView == null) {
//            val error = IKAdError(IKSdkErrorCode.VIEW_GROUP_NULL)
//            mTrackingListener.onAdShowFailed(
//                IKSdkDefConst.UNKNOWN,
//                screen,
//                "",
//                error
//            )
//            sdkAdListener.onAdShowFail(error)
//            return
//        }
//        mAdLayout = layoutView
//        IKNativeController.showAd(
//            screen,
//            configDto,
//            object : IKSdkShowWidgetAdListener {
//                override fun onAdReady(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    hideShimmer()
//                    handleShowNativeAdView(adData,
//                        IKAdFormat.NATIVE_BANNER,
//                        onSuccess = {
//                            mTrackingListener.onAdShowed(
//                                adNetworkName,
//                                screen,
//                                scriptName,
//                                currentAdView?.adPriority ?: 0,
//                                currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                            )
//                            sdkAdListener.onAdShowed()
//                            autoReload(configDto)
//                        }, onFail = { err ->
//                            onAdShowFail(err, scriptName, adNetworkName)
//                        })
//                }
//
//                override fun onAdShowFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    mTrackingListener.onAdShowFailed(
//                        adNetworkName,
//                        screen,
//                        scriptName,
//                        error
//                    )
//
//                    if (NativeBackUpLatest.getBackupAd(IKAdFormat.NATIVE_BANNER) == null) {
//                        sdkAdListener.onAdShowFail(error)
//                        return
//                    }
//
//                    showNativeBackupLatest(
//                        adFormat = IKAdFormat.NATIVE_BANNER,
//                        scriptName = scriptName,
//                        screen = screen,
//                        configDto = configDto,
//                        sdkAdListener = sdkAdListener
//                    )
//                }
//
//
//                override fun onAdClick(scriptName: String, adNetworkName: String) {
//                    sdkAdListener.onAdClick()
//                    mTrackingListener.onAdClicked(
//                        adNetworkName,
//                        screen,
//                        scriptName,
//                        currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                    )
//                }
//
//                override fun onAdImpression(scriptName: String, adNetworkName: String) {
//                    mTrackingListener.onAdImpression(
//                        adNetworkName,
//                        screen,
//                        scriptName,
//                        currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                    )
//                }
//
//                override fun onAdReloaded(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    handleShowNativeAdView(adData,
//                        IKAdFormat.NATIVE_BANNER,
//                        onSuccess = {
//
//                        }, onFail = { _ ->
//
//                        })
//                }
//
//                override fun onAdReloadFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//
//                }
//
//            }
//        )
//    }
//
//    private fun showNativeNormal(
//        screen: String,
//        sdkAdListener: IKShowWidgetAdListener,
//        configDto: IKSdkProdWidgetDetailDto
//    ) {
//        mCurrentAdFormat = IKSdkDefConst.AdFormat.NATIVE
//        @SuppressLint("InflateParams")
//        val layoutView =
//            if (mAdLayout != null) mAdLayout else getTempAdLayout()
//        if (layoutView == null) {
//            val error = IKAdError(IKSdkErrorCode.VIEW_GROUP_NULL)
//            mTrackingListener.onAdShowFailed(
//                IKSdkDefConst.UNKNOWN,
//                screen,
//                "",
//                error
//            )
//            sdkAdListener.onAdShowFail(error)
//            return
//        }
//        mAdLayout = layoutView
//        IKNativeController.showAd(
//            screen,
//            configDto,
//            object : IKSdkShowWidgetAdListener {
//                override fun onAdReady(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    hideShimmer()
//                    handleShowNativeAdView(adData,
//                        IKAdFormat.NATIVE,
//                        onSuccess = {
//                            mTrackingListener.onAdShowed(
//                                adNetworkName,
//                                screen,
//                                scriptName,
//                                currentAdView?.adPriority ?: 0,
//                                currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                            )
//                            sdkAdListener.onAdShowed()
//                            autoReload(configDto)
//                        }, onFail = { err ->
//                            onAdShowFail(err, scriptName, adNetworkName)
//                        })
//                }
//
//                override fun onAdShowFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    mTrackingListener.onAdShowFailed(
//                        adNetworkName,
//                        screen,
//                        scriptName,
//                        error
//                    )
//
//                    if (NativeBackUpLatest.getBackupAd(IKAdFormat.NATIVE) == null) {
//                        sdkAdListener.onAdShowFail(error)
//                        return
//                    }
//
//                    showNativeBackupLatest(
//                        adFormat = IKAdFormat.NATIVE,
//                        scriptName = scriptName,
//                        screen = screen,
//                        configDto = configDto,
//                        sdkAdListener = sdkAdListener
//                    )
//                }
//
//                override fun onAdClick(scriptName: String, adNetworkName: String) {
//                    sdkAdListener.onAdClick()
//                    mTrackingListener.onAdClicked(
//                        adNetworkName,
//                        screen,
//                        scriptName,
//                        currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                    )
//                }
//
//                override fun onAdImpression(scriptName: String, adNetworkName: String) {
//                    mTrackingListener.onAdImpression(
//                        adNetworkName,
//                        screen,
//                        scriptName,
//                        currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                    )
//                }
//
//                override fun onAdReloaded(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    handleShowNativeAdView(adData,
//                        IKAdFormat.NATIVE,
//                        onSuccess = {
//
//                        }, onFail = { _ ->
//
//                        })
//                }
//
//                override fun onAdReloadFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                }
//
//            }
//        )
//    }
//
//    private fun centerLayoutParam(): LayoutParams {
//        return LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
//            gravity = Gravity.CENTER
//        }
//    }
//
//    private fun centerFullLayoutParam(): LayoutParams {
//        return LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).apply {
//            gravity = Gravity.CENTER
//        }
//    }
//
//    fun reCallLoadAdCore(listener: IKShowWidgetAdListener?) {
//        showLogD("reCallLoadAd") { "start load" }
//        mCurrentRecallListener = null
//        mCurrentRecallListener = listener
//        mIsRecall = true
//        mIsAdLoading = true
//        if (showCollapseWhenRecal)
//            mFromAutoReload = false
//        uiScope.launchWithSupervisorJob(Dispatchers.IO) {
//            if (!IKSdkUtilsCore.canShowAdAsync()) {
//                showLogD("reCallLoadAd") { "loadFail: ${IKSdkErrorCode.USER_PREMIUM}" }
//                uiScope.launchWithSupervisorJob {
//                    mCurrentRecallListener?.onAdShowFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
//                }
//                return@launchWithSupervisorJob
//            }
//            trackingWidget(
//                adStatus = IKSdkDefConst.AdStatus.PRE_SHOW,
//                screen = mScreen,
//                Pair(IKTrackingConst.ParamName.RECALL_AD, IKTrackingConst.ParamName.YES)
//            )
//            val sdkAdListener = object : IKShowWidgetAdListener {
//                override fun onAdShowed() {
//                    Log.d(TAG_LOG, "onAdReady")
//                    mIsAdLoaded = true
//                    mIsAdLoading = false
//                    uiScope.launchWithSupervisorJob(Dispatchers.Main) {
//                        mCurrentRecallListener?.onAdShowed()
//                    }
//                    trackingWidget(
//                        adStatus = IKSdkDefConst.AdStatus.SHOWED,
//                        screen = mScreen,
//                        Pair(IKTrackingConst.ParamName.RECALL_AD, IKTrackingConst.ParamName.YES)
//                    )
//                }
//
//                override fun onAdShowFail(error: IKAdError) {
//                    showLogD("reCallLoadAd") { "onAdShowFail error: $error" }
//                    mIsAdLoading = false
//                    uiScope.launchWithSupervisorJob(Dispatchers.Main) {
//                        mCurrentRecallListener?.onAdShowFail(error)
//                    }
//                    trackingWidget(
//                        adStatus = IKSdkDefConst.AdStatus.SHOW_FAIL,
//                        screen = mScreen,
//                        Pair(IKTrackingConst.ParamName.RECALL_AD, IKTrackingConst.ParamName.YES),
//                        Pair(IKTrackingConst.ParamName.ERROR_CODE, "${error.code}"),
//                        Pair(IKTrackingConst.ParamName.MESSAGE, error.message)
//                    )
//                }
//
//                override fun onAdClick() {
//                    showLogD("reCallLoadAd") { "onAdClick" }
//                    uiScope.launchWithSupervisorJob {
//                        mCurrentRecallListener?.onAdClick()
//                        mCurrentListener?.onAdClick()
//                    }
//                }
//            }
//
//            uiScope.launchWithSupervisorJob(Dispatchers.IO) scope@{
//                if (mFrameAdView?.visibility == View.GONE) {
//                    sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.VIEW_GROUP_NOT_VISIBLE))
//                    return@scope
//                }
//                val configDto = mRepository?.getConfigWidget(mScreen)
//                if (configDto == null) {
//                    sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.NO_SCREEN_ID_AD))
//                    return@scope
//                }
//                if (configDto.enable != true) {
//                    sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.DISABLE_SHOW))
//                    return@scope
//                }
//                mTrackingListener.apply {
//                    this.screen = mScreen
//                    this.adFormat = configDto.adFormat
//                    this.isRecall = true
//                }
//                if (configDto.adFormat != IKAdFormat.BANNER_COLLAPSE_C1.value ||
//                    configDto.adFormat != IKAdFormat.BANNER_COLLAPSE_C2.value
//                )
//                    mTrackingListener.onAdPreShow(screen = mScreen, "")
//                when (configDto.adFormat) {
//                    IKAdFormat.BANNER.value -> {
//                        showBannerNormal(mScreen, sdkAdListener, configDto)
//                    }
//
//                    IKAdFormat.BANNER_INLINE.value, IKAdFormat.MREC.value -> {
//                        showBannerNormalInline(mScreen, sdkAdListener, configDto, false)
//                    }
//
//                    IKAdFormat.BANNER_COLLAPSE.value -> {
//                        showBannerCollapse(mScreen, sdkAdListener, configDto, false)
//                    }
//
//                    IKAdFormat.NATIVE.value -> {
//                        showNativeNormal(mScreen, sdkAdListener, configDto)
//                    }
//
//                    IKAdFormat.NATIVE_BANNER.value -> {
//                        showNativeBannerNormal(mScreen, sdkAdListener, configDto)
//                    }
//
//                    IKAdFormat.BANNER_COLLAPSE_C1.value -> {
//                        showBannerCollapseC1(mScreen, sdkAdListener, configDto)
//                    }
//
//                    IKAdFormat.BANNER_COLLAPSE_C2.value -> {
//                        showBannerCollapseC2(mScreen, sdkAdListener, configDto)
//                    }
//                }
//            }
//        }
//
//    }
//
//    fun loadSAWAdCore(
//        adListener: IKShowWidgetAdListener?
//    ) {
//        mScreen = IKSdkDefConst.AdScreen.BANNER_SPLASH
//        mIsAdLoaded = false
//        mIsAdLoading = true
//        mIsRecall = false
//        showLogD("loadSAWAd") { "start load" }
//        mCurrentListener = null
//        mCurrentListener = adListener
//        uiScope.launchWithSupervisorJob(Dispatchers.IO) {
//            if (!IKSdkUtilsCore.canShowAdAsync()) {
//                showLogD("loadSAWAd") { "showFail: ${IKSdkErrorCode.USER_PREMIUM}" }
//                uiScope.launchWithSupervisorJob {
//                    mCurrentListener?.onAdShowFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
//                }
//                return@launchWithSupervisorJob
//            }
//            if (mFrameAdView?.visibility == View.GONE) {
//                showLogD("loadSAWAd") { "showFail: ${IKSdkErrorCode.VIEW_GROUP_NOT_VISIBLE}" }
//                uiScope.launchWithSupervisorJob {
//                    mCurrentListener?.onAdShowFail(IKAdError(IKSdkErrorCode.VIEW_GROUP_NOT_VISIBLE))
//                }
//                return@launchWithSupervisorJob
//            }
//            trackingWidget(
//                adStatus = IKSdkDefConst.AdStatus.PRE_SHOW,
//                screen = mScreen
//            )
//
//            mTrackingListener.apply {
//                this.screen = mScreen
//                this.adFormat = IKSdkDefConst.AdFormat.BANNER
//                this.isRecall = false
//            }
//            mTrackingListener.onAdPreShow(screen = mScreen, "")
//
//            val sdkAdListener = object : IKShowWidgetAdListener {
//                override fun onAdShowed() {
//                    mIsAdLoaded = true
//                    mIsAdLoading = false
//                    uiScope.launchWithSupervisorJob {
//                        mCurrentListener?.onAdShowed()
//                    }
//                    trackingWidget(
//                        adStatus = IKSdkDefConst.AdStatus.SHOWED,
//                        screen = mScreen
//                    )
//                    showLogD("loadSAWAd") { "onAdReady" }
//                }
//
//                override fun onAdShowFail(error: IKAdError) {
//                    if (!mIsRecall)
//                        mIsAdLoaded = false
//                    mIsAdLoading = false
//                    uiScope.launchWithSupervisorJob(Dispatchers.Main) {
//                        mCurrentListener?.onAdShowFail(error)
//                    }
//                    trackingWidget(
//                        adStatus = IKSdkDefConst.AdStatus.SHOW_FAIL,
//                        screen = mScreen,
//                        Pair(IKTrackingConst.ParamName.ERROR_CODE, "${error.code}"),
//                        Pair(IKTrackingConst.ParamName.MESSAGE, error.message)
//                    )
//                    showLogD("loadSAWAd") { "onAdShowFail error: $error" }
//                }
//
//                override fun onAdClick() {
//                    uiScope.launchWithSupervisorJob {
//                        mCurrentListener?.onAdClick()
//                    }
//                    showLogD("loadSAWAd") { "onAdClick" }
//                }
//            }
//
//            val configDto =
//                mRepository?.getConfigWidget(mScreen, IKAdFormat.BANNER)
//                    ?: IKSdkProdWidgetDetailDto(
//                        mScreen, IKAdFormat.BANNER.value,
//                        null,
//                        false,
//                        enable = true
//                    )
//            removeAdLayout()
//            hideShimmer()
//            if (configDto.enable != true) {
//                sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.DISABLE_SHOW))
//                return@launchWithSupervisorJob
//            }
//            showBannerNormal(mScreen, sdkAdListener, configDto)
//        }
//    }
//
//    private fun getTempAdLayout(): IkmWidgetAdLayout? {
//        if (mContext == null)
//            return null
//        val adLayout = LayoutInflater.from(mContext).inflate(
//            when (nativeTemplate) {
//                IKNativeTemplate.NORMAL_LAYOUT -> R.layout.ik_temp_native_normal
//                IKNativeTemplate.EXIT_LAYOUT -> R.layout.ik_temp_native_exit
//                IKNativeTemplate.BANNER_LAYOUT -> R.layout.ik_temp_native_banner_normal
//            }, null, false
//        ) as? IkmWidgetAdLayout
//
//        adLayout?.apply {
//            when (nativeTemplate) {
//                IKNativeTemplate.NORMAL_LAYOUT -> {
//                    titleView = findViewById(R.id.tempNative_headline)
//                    bodyView = findViewById(R.id.tempNative_body)
//                    callToActionView = findViewById(R.id.tempNative_call_to_action)
//                    iconView = findViewById(R.id.tempNative_app_icon)
//                    mediaView = findViewById(R.id.tempNative_media)
//                }
//
//                IKNativeTemplate.EXIT_LAYOUT -> {
//                    titleView = findViewById(R.id.tempNative_headline)
//                    bodyView = findViewById(R.id.tempNative_body)
//                    callToActionView = findViewById(R.id.tempNative_call_to_action)
//                    iconView = findViewById(R.id.tempNative_app_icon)
//                    mediaView = findViewById(R.id.tempNative_media)
//                }
//
//                IKNativeTemplate.BANNER_LAYOUT -> {
//                    titleView = findViewById(R.id.tempNativeBanner_headline)
//                    bodyView = findViewById(R.id.tempNativeBanner_body)
//                    callToActionView = findViewById(R.id.tempNativeBanner_call_to_action)
//                    iconView = findViewById(R.id.tempNativeBanner_app_icon)
//                    mediaView = null  // Assume no media view for banner layout
//                }
//            }
//        }
//
//        return adLayout
//    }
//
//    private fun initLoadingView() {
//        uiScope.launchWithSupervisorJob {
//            if (mContext == null)
//                return@launchWithSupervisorJob
//            removeAdLayout()
//            if (mLoadingAdLayout != -1) {
//                hideShimmer()
//                LayoutInflater.from(mContext).inflate(
//                    mLoadingAdLayout,
//                    mAdViewLoading, true
//                )
//            }
//        }
//    }
//
//    fun showWithDisplayAdViewCore(
//        screen: String,
//        displayWidgetAdView: IkmDisplayWidgetAdView,
//        adListener: IKShowWidgetAdListener?
//    ) {
//        showLogD("showWithDisplayAdView") { "start load" }
//        mScreen = screen
//        mIsAdLoaded = false
//        mIsAdLoading = true
//        mIsRecall = false
//
//        mCurrentListener = null
//        mCurrentListener = adListener
//        uiScope.launchWithSupervisorJob(Dispatchers.IO) {
//            if (!IKSdkUtilsCore.canShowAdAsync()) {
//                uiScope.launchWithSupervisorJob {
//                    mCurrentListener?.onAdShowFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
//                }
//                showLogD("showWithDisplayAdView") { "showFail: ${IKSdkErrorCode.USER_PREMIUM}" }
//                return@launchWithSupervisorJob
//            }
//
//            mTrackingListener.apply {
//                this.screen = screen
//                this.adFormat = IKSdkDefConst.AdFormat.NATIVE
//            }
//            mTrackingListener.onAdPreShow(screen = screen, "")
//
//            val sdkAdListener = object : IKShowWidgetAdListener {
//                override fun onAdShowed() {
//                    mIsAdLoaded = true
//                    mIsAdLoading = false
//                    uiScope.launchWithSupervisorJob {
//                        mCurrentListener?.onAdShowed()
//                    }
//                    trackingWidget(
//                        adStatus = IKSdkDefConst.AdStatus.SHOWED,
//                        screen = mScreen
//                    )
//                    showLogD("showWithDisplayAdView") { "showed" }
//                }
//
//                override fun onAdShowFail(error: IKAdError) {
//                    if (!mIsRecall)
//                        mIsAdLoaded = false
//                    mIsAdLoading = false
//                    uiScope.launchWithSupervisorJob {
//                        mCurrentListener?.onAdShowFail(error)
//                    }
//                    trackingWidget(
//                        adStatus = IKSdkDefConst.AdStatus.SHOW_FAIL,
//                        screen = screen,
//                        Pair(IKTrackingConst.ParamName.ERROR_CODE, "${error.code}"),
//                        Pair(IKTrackingConst.ParamName.MESSAGE, error.message)
//                    )
//                    showLogD("showWithDisplayAdView") { "showFail error: $error" }
//                }
//
//                override fun onAdClick() {
//                    uiScope.launchWithSupervisorJob {
//                        mCurrentListener?.onAdClick()
//                    }
//                    showLogD("showWithDisplayAdView") { "onAdClick" }
//                }
//            }
//
//            if (mFrameAdView?.visibility == View.GONE) {
//                sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.VIEW_GROUP_NOT_VISIBLE))
//                return@launchWithSupervisorJob
//            }
//
//            val configDto = mRepository?.getConfigWidget(screen)
//            if (configDto == null) {
//                sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.NO_SCREEN_ID_AD))
//                return@launchWithSupervisorJob
//            }
//            if (configDto.enable != true) {
//                sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.DISABLE_SHOW))
//                return@launchWithSupervisorJob
//            }
//
//            initLoadingView()
//
//            @SuppressLint("InflateParams")
//            val layoutView =
//                if (mAdLayout != null) mAdLayout else getTempAdLayout()
//            if (layoutView == null) {
//                val error = IKAdError(IKSdkErrorCode.VIEW_GROUP_NULL)
//                mTrackingListener.onAdShowFailed(
//                    IKSdkDefConst.UNKNOWN,
//                    screen,
//                    IKSdkDefConst.UNKNOWN,
//                    error
//                )
//                sdkAdListener.onAdShowFail(error)
//                return@launchWithSupervisorJob
//            }
//            mAdLayout = layoutView
//
//            IKNativeController.showWithDisplayAdView(
//                screen,
//                displayWidgetAdView,
//                object : IKSdkShowWidgetAdListener {
//                    override fun onAdReady(
//                        adData: IKSdkBaseLoadedAd<*>,
//                        scriptName: String,
//                        adNetworkName: String
//                    ) {
//                        hideShimmer()
//                        handleShowNativeAdView(adData,
//                            IKAdFormat.NATIVE,
//                            onSuccess = {
//                                mTrackingListener.onAdShowed(
//                                    adNetworkName,
//                                    screen,
//                                    scriptName,
//                                    currentAdView?.adPriority ?: 0,
//                                    currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                                )
//                                sdkAdListener.onAdShowed()
//                                autoReload(configDto)
//                            }, onFail = { err ->
//                                onAdShowFail(err, scriptName, adNetworkName)
//                            })
//                    }
//
//                    override fun onAdShowFail(
//                        error: IKAdError,
//                        scriptName: String,
//                        adNetworkName: String
//                    ) {
//                        sdkAdListener.onAdShowFail(error)
//                        mTrackingListener.onAdShowFailed(
//                            adNetworkName,
//                            screen,
//                            scriptName,
//                            error
//                        )
//                    }
//
//                    override fun onAdClick(scriptName: String, adNetworkName: String) {
//                        sdkAdListener.onAdClick()
//                        mTrackingListener.onAdClicked(
//                            adNetworkName,
//                            screen,
//                            scriptName,
//                            currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                        )
//                    }
//
//                    override fun onAdImpression(scriptName: String, adNetworkName: String) {
//                        mTrackingListener.onAdImpression(
//                            adNetworkName,
//                            screen,
//                            scriptName,
//                            currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                        )
//                    }
//
//                    override fun onAdReloaded(
//                        adData: IKSdkBaseLoadedAd<*>,
//                        scriptName: String,
//                        adNetworkName: String
//                    ) {
//                        handleShowNativeAdView(adData,
//                            IKAdFormat.NATIVE,
//                            onSuccess = {
//
//                            }, onFail = { _ ->
//
//                            })
//                    }
//
//                    override fun onAdReloadFail(
//                        error: IKAdError,
//                        scriptName: String,
//                        adNetworkName: String
//                    ) {
//                    }
//
//                }
//            )
//        }
//    }
//
//
//    private suspend fun reloadAd(reloadTime: Long) {
//        if (!mAutoReload) {
//            showLogD("reCallLoadAd") { "reloadAd stop" }
//            return
//        }
//        if (mContext == null) {
//            stopAutoReload()
//            return
//        }
//        if (mFrameAdView?.isShown == true) {
//            mFromAutoReload = true
//            showLogD("reCallLoadAd") { "reloadAd start reCallLoadAdCore" }
//            reCallLoadAdCore(null)
//        } else {
//            showLogD("reCallLoadAd") { "reloadAd delay" }
//            delay(IKSdkDefConst.TIME_RECHECK_RELOAD_VIEW)
//            if (mAutoReload) {
//                reloadAd(reloadTime)
//            }
//        }
//    }
//
//    fun stopAutoReload() {
//        mAutoReload = false
//    }
//
//    private suspend fun checkReload(reloadTime: Long) {
//        if (!mAutoReload) {
//            showLogD("reCallLoadAd") { "checkReload stop" }
//            return
//        }
//        showLogD("reCallLoadAd") { "checkReload start delay $reloadTime" }
//        delay(reloadTime)
//        // Tính toán thời gian còn lại cần delay nếu một quảng cáo mới được hiển thị
//        val elapsedTime = System.currentTimeMillis() - startTime
//        val remainingDelay = reloadTime - elapsedTime
//        if (remainingDelay > 0) {
//            showLogD("reCallLoadAd") { "Need delaying for new ad display $remainingDelay ms" }
//            delay(remainingDelay)
//        } else {
//            showLogD("reCallLoadAd") { "No need to delay, elapsedTime: $elapsedTime ms" }
//        }
//        reloadAd(reloadTime)
//    }
//
//    private fun autoReload(configDto: IKSdkProdWidgetDetailDto) {
//        showLogD("autoReload") { "check can reload" }
//        val reloadTime = configDto.reloadTime ?: 0L
//        if (reloadTime >= IKSdkDefConst.MIN_AUTO_RELOAD) {
//            mAutoReload = true
//            startTime = System.currentTimeMillis()
//            showLogD("autoReload") { "start run" }
//            mFrameAdView?.doOnDetach {
//                showLogD("autoReload") { "doOnDetach" }
//                reloadJob?.cancel()
//                stopAutoReload()
//            }
//
//            mFrameAdView?.doOnAttach {
//                showLogD("autoReload") { "doOnAttach" }
//                if (reloadJob?.isActive != true) { // Chỉ khi không có job nào đang chạy
//                    showLogD("autoReload") { "Starting new reload job" }
//                    reloadJob = uiScope.launchWithSupervisorJob(Dispatchers.IO) {
//                        checkReload(reloadTime)
//                    }
//                } else {
//                    showLogD("autoReload") { "reloadJob is already running" }
//                }
//            }
//        }
//    }
//
//    fun loadAdFSCore(
//        layoutAd: IkmWidgetAdLayout,
//        screen: String,
//        callback: IKShowWidgetAdListener?
//    ) {
//        showLogD("loadAdFS") { "start load" }
////        mAdViewLoading = mContext?.let {
////            FrameLayout(it).apply {
////                id = R.id.ikWidgetAdLoadingLayoutView
////            }
////        }
//        mScreen = screen
//
//        mCurrentListener = null
//        mCurrentListener = callback
//        mAdLayout = layoutAd
//        mFrameAdView?.removeAllViewsInLayout()
//        mFrameAdView?.addView(
//            adViewLayout,
//            centerFullLayoutParam()
//        )
//
//        uiScope.launchWithSupervisorJob(Dispatchers.IO) {
//            if (!IKSdkUtilsCore.canShowAdAsync()) {
//                showLogD("loadAdFS") { "showFail: ${IKSdkErrorCode.USER_PREMIUM}" }
//                uiScope.launchWithSupervisorJob {
//                    mCurrentListener?.onAdShowFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
//                }
//                return@launchWithSupervisorJob
//            }
//
//            trackingWidget(
//                adStatus = IKSdkDefConst.AdStatus.AD_PLACEMENT,
//                screen = screen
//            )
//
//            val sdkAdListener = object : IKShowWidgetAdListener {
//                override fun onAdShowed() {
//                    mIsAdLoaded = true
//                    mIsAdLoading = false
//                    uiScope.launchWithSupervisorJob {
//                        mCurrentListener?.onAdShowed()
//                    }
//                    trackingWidget(
//                        adStatus = IKSdkDefConst.AdStatus.SHOWED,
//                        screen = screen
//                    )
//                    showLogD("loadAdFS") { "showed" }
//                }
//
//                override fun onAdShowFail(error: IKAdError) {
//                    if (!mIsRecall)
//                        mIsAdLoaded = false
//                    mIsAdLoading = false
//                    uiScope.launchWithSupervisorJob {
//                        mCurrentListener?.onAdShowFail(error)
//                    }
//                    trackingWidget(
//                        adStatus = IKSdkDefConst.AdStatus.SHOW_FAIL,
//                        screen = screen,
//                        Pair(IKTrackingConst.ParamName.ERROR_CODE, "${error.code}"),
//                        Pair(IKTrackingConst.ParamName.MESSAGE, error.message)
//                    )
//                    showLogD("loadAdFS") { "showFail: $error" }
//                }
//
//                override fun onAdClick() {
//                    uiScope.launchWithSupervisorJob {
//                        mCurrentListener?.onAdClick()
//                    }
//                    showLogD("loadAdFS") { "onAdClick" }
//                }
//            }
//
//            if (mFrameAdView?.visibility == View.GONE) {
//                sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.VIEW_GROUP_NOT_VISIBLE))
//                return@launchWithSupervisorJob
//            }
//
//            val configDto =
//                mRepository?.getConfigWidget(screen, IKAdFormat.NATIVE_FULL)
//                    ?: mRepository?.getConfigWidget(screen, IKAdFormat.NATIVE)
//
//            if (configDto == null) {
//                sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.NO_SCREEN_ID_AD))
//                return@launchWithSupervisorJob
//            }
//
//            if (configDto.enable != true) {
//                sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.DISABLE_SHOW))
//                return@launchWithSupervisorJob
//            }
//            initLoadingView()
//            mCurrentAdFormat = IKSdkDefConst.AdFormat.NATIVE_FULL
//
//            mTrackingListener.apply {
//                this.screen = screen
//                this.adFormat = IKSdkDefConst.AdFormat.NATIVE_FULL
//            }
//            mTrackingListener.onAdPreShow(screen = screen, "")
//            @SuppressLint("InflateParams")
//            val layoutView =
//                if (mAdLayout != null) mAdLayout else getTempAdLayout()
//            if (layoutView == null) {
//                val error = IKAdError(IKSdkErrorCode.VIEW_GROUP_NULL)
//                mTrackingListener.onAdShowFailed(
//                    IKSdkDefConst.UNKNOWN,
//                    screen,
//                    IKSdkDefConst.UNKNOWN,
//                    error
//                )
//                sdkAdListener.onAdShowFail(error)
//                return@launchWithSupervisorJob
//            }
//            mAdLayout = layoutView
//            IKNativeFullScreenController.showAd(
//                screen,
//                configDto,
//                object : IKSdkShowWidgetAdListener {
//                    override fun onAdReady(
//                        adData: IKSdkBaseLoadedAd<*>,
//                        scriptName: String,
//                        adNetworkName: String
//                    ) {
//                        hideShimmer()
//                        handleShowNativeAdView(adData,
//                            IKAdFormat.NATIVE_FULL,
//                            onSuccess = {
//                                mTrackingListener.onAdShowed(
//                                    adNetworkName,
//                                    screen,
//                                    scriptName,
//                                    currentAdView?.adPriority ?: 0,
//                                    currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                                )
//                                sdkAdListener.onAdShowed()
//                                autoReload(configDto)
//                            }, onFail = { err ->
//                                onAdShowFail(err, scriptName, adNetworkName)
//                            })
//                    }
//
//                    override fun onAdShowFail(
//                        error: IKAdError,
//                        scriptName: String,
//                        adNetworkName: String
//                    ) {
//                        mTrackingListener.onAdShowFailed(
//                            adNetworkName,
//                            screen,
//                            scriptName,
//                            error
//                        )
//
//                        if (NativeBackUpLatest.getBackupAd(IKAdFormat.NATIVE_FULL) == null) {
//                            sdkAdListener.onAdShowFail(error)
//                            return
//                        }
//
//                        showNativeBackupLatest(
//                            adFormat = IKAdFormat.NATIVE_FULL,
//                            scriptName = scriptName,
//                            screen = screen,
//                            configDto = configDto,
//                            sdkAdListener = sdkAdListener
//                        )
//                    }
//
//                    override fun onAdClick(scriptName: String, adNetworkName: String) {
//                        sdkAdListener.onAdClick()
//                        mTrackingListener.onAdClicked(
//                            adNetworkName,
//                            screen,
//                            scriptName,
//                            currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                        )
//                    }
//
//                    override fun onAdImpression(scriptName: String, adNetworkName: String) {
//                        mTrackingListener.onAdImpression(
//                            adNetworkName,
//                            screen,
//                            scriptName,
//                            currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                        )
//                    }
//
//                    override fun onAdReloaded(
//                        adData: IKSdkBaseLoadedAd<*>,
//                        scriptName: String,
//                        adNetworkName: String
//                    ) {
//                        handleShowNativeAdView(adData,
//                            IKAdFormat.NATIVE_FULL,
//                            onSuccess = {
//
//                            }, onFail = { _ ->
//
//                            })
//                    }
//
//                    override fun onAdReloadFail(
//                        error: IKAdError,
//                        scriptName: String,
//                        adNetworkName: String
//                    ) {
//                    }
//
//                }
//            )
//        }
//    }
//
//    private fun setCcAdView(value: IKSdkBaseLoadedAd<*>?) {
//        if (value?.loadedAd == null)
//            return
//        uiScope.launchWithSupervisorJob {
//            destroyAdObject(currentAdView)
//            delay(500)
//            currentAdView = value
//        }
//        uiScope.launchWithSupervisorJob {
//            if (isParentDestroy)
//                destroyCurrentAd()
//        }
//    }
//
//    private fun handleShowAdView(
//        value: IKSdkBaseLoadedAd<*>,
//        onSuccess: () -> Unit,
//        onFail: (err: IKAdError) -> Unit
//    ) {
//        val adLoaded = value.loadedAd as View?
//        if (adLoaded == null) {
//            onFail.invoke(IKAdError(IKSdkErrorCode.VIEW_GROUP_NULL))
//            return
//        }
//        mCurrentAddUuid = value?.uuid
//        kotlin.runCatching {
//            destroyOldAd()
//            adViewLayout?.removeAllViewsInLayout()
//            adViewLayout?.addView(adLoaded)
//        }.onFailure {
//            onFail.invoke(IKAdError(IKSdkErrorCode.VIEW_GROUP_NULL))
//        }.onSuccess {
//            onSuccess.invoke()
//            uiScope.launchWithSupervisorJob {
//                destroyAdObject(currentAdView)
//                delay(500)
//                currentAdView = value
//            }
//            uiScope.launchWithSupervisorJob {
//                if (isParentDestroy)
//                    destroyCurrentAd()
//            }
//        }
//    }
//
//    private fun updateAdViewWithFadeInOut(viewAd: View) {
//        val currentAdView = adViewLayout?.getChildAt(0)
//        currentAdView?.let {
//            viewAd.alpha = 0f
//            adViewLayout?.addView(viewAd)
//
//            val fadeOut = ObjectAnimator.ofFloat(it, View.ALPHA, 1f, 0f)
//            val fadeIn = ObjectAnimator.ofFloat(viewAd, View.ALPHA, 0f, 1f)
//            val animatorSet = AnimatorSet()
//            animatorSet.duration = 1000
//            animatorSet.playTogether(fadeOut, fadeIn)
//            animatorSet.start()
//
//            fadeOut.addListener(object : Animator.AnimatorListener {
//                override fun onAnimationStart(animation: Animator) {}
//                override fun onAnimationEnd(animation: Animator) {
//                    kotlin.runCatching {
//                        adViewLayout?.removeView(it)
//                    }
//                }
//
//                override fun onAnimationCancel(animation: Animator) {}
//                override fun onAnimationRepeat(animation: Animator) {}
//            })
//        } ?: run {
//            adViewLayout?.addView(viewAd)
//        }
//    }
//
//    private fun handleShowNativeAdView(
//        adLoaded: IKSdkBaseLoadedAd<*>?,
//        adFormat: IKAdFormat,
//        onSuccess: () -> Unit,
//        onFail: (err: IKAdError) -> Unit,
//        configDto: IKSdkProdWidgetDetailDto? = null,
//    ) {
////        uiScope.launchWithSupervisorJob {
////            if (mContext == null || adLoaded?.loadedAd == null) {
////                onFail.invoke(IKAdError(IKSdkErrorCode.VIEW_GROUP_NULL))
////                return@launchWithSupervisorJob
////            }
////            kotlin.runCatching {
////                mAdLayout?.let {
////                    (it.parent as? NativeAdView)?.removeView(it)
////                }
////            }
////            mCurrentAddUuid = adLoaded?.uuid
////            var viewAd: View? = null
////            when (adFormat) {
////                IKAdFormat.NATIVE -> {
////                    when (adLoaded.adNetwork) {
////                        AdNetwork.AD_MOB.value -> {
////                            viewAd = IKWidgetAdUtil.Admob.Native.setupNativeCustomAdView(
////                                mContext, adLoaded.loadedAd as? NativeAd?, mAdLayout
////                            )
////                        }
////
////                        AdNetwork.AD_MANAGER.value -> {
////                            viewAd = IKWidgetAdUtil.Gam.Native.setupNativeCustomAdView(
////                                mContext, adLoaded.loadedAd as? NativeAd?, mAdLayout
////                            )
////                        }
////
////                        AdNetwork.AD_MAX.value -> {
////                            viewAd = IKWidgetAdUtil.Applovin.Native.setupNativeCustomAdView(
////                                mContext, adLoaded.loadedAd as? IkObjectNativeMax?, mAdLayout
////                            )
////                        }
////                    }
////                }
////
////                IKAdFormat.NATIVE_FULL -> {
////                    when (adLoaded.adNetwork) {
////                        AdNetwork.AD_MOB.value -> {
////                            viewAd = IKWidgetAdUtil.Admob.NativeFull.setupNativeCustomAdView(
////                                mContext, adLoaded.loadedAd as? NativeAd?, mAdLayout
////                            )
////                        }
////
////                        AdNetwork.AD_MANAGER.value -> {
////                            viewAd = IKWidgetAdUtil.Admob.NativeFull.setupNativeCustomAdView(
////                                mContext, adLoaded.loadedAd as? NativeAd?, mAdLayout
////                            )
////                        }
////
////                        AdNetwork.AD_MAX.value -> {
////                            viewAd = IKWidgetAdUtil.Applovin.NativeFull.setupNativeCustomAdView(
////                                mContext, adLoaded.loadedAd as? IkObjectNativeMax?, mAdLayout
////                            )
////                        }
////                    }
////                }
////
////                IKAdFormat.NATIVE_BANNER -> {
////                    when (adLoaded.adNetwork) {
////                        AdNetwork.AD_MOB.value -> {
////                            viewAd = IKWidgetAdUtil.Admob.Native.setupNativeCustomAdView(
////                                mContext, adLoaded.loadedAd as? NativeAd?, mAdLayout
////                            )
////                        }
////
////                        AdNetwork.AD_MANAGER.value -> {
////                            viewAd = IKWidgetAdUtil.Gam.Native.setupNativeCustomAdView(
////                                mContext, adLoaded.loadedAd as? NativeAd?, mAdLayout
////                            )
////                        }
////
////                        AdNetwork.AD_MAX.value -> {
////                            viewAd = IKWidgetAdUtil.Applovin.Native.setupNativeCustomAdView(
////                                mContext, adLoaded.loadedAd as? IkObjectNativeMax?, mAdLayout
////                            )
////                        }
////                    }
////                }
////
////                IKAdFormat.BANNER -> {
////                    when (adLoaded.adNetwork) {
////                        AdNetwork.AD_MOB.value -> {
////                            viewAd = IKWidgetAdUtil.Admob.Native.setupNativeCustomAdView(
////                                mContext, adLoaded.loadedAd as? NativeAd?, getBannerNativeLayout()
////                            )
////                        }
////
////                        AdNetwork.AD_MANAGER.value -> {
////                            viewAd = IKWidgetAdUtil.Gam.Native.setupNativeCustomAdView(
////                                mContext, adLoaded.loadedAd as? NativeAd?, getBannerNativeLayout()
////                            )
////                        }
////
////                        AdNetwork.AD_MAX.value -> {
////                            viewAd = IKWidgetAdUtil.Applovin.Native.setupNativeCustomAdView(
////                                mContext,
////                                adLoaded.loadedAd as? IkObjectNativeMax?,
////                                getBannerNativeLayout()
////                            )
////                        }
////                    }
////                }
////
////                IKAdFormat.BANNER_INLINE -> {
////                    when (adLoaded.adNetwork) {
////                        AdNetwork.AD_MOB.value -> {
////                            viewAd = IKWidgetAdUtil.Admob.Native.setupNativeCustomAdView(
////                                mContext,
////                                adLoaded.loadedAd as? NativeAd?,
////                                getBannerInlineLayout(configDto)
////                            )
////                        }
////
////                        AdNetwork.AD_MANAGER.value -> {
////                            viewAd = IKWidgetAdUtil.Gam.Native.setupNativeCustomAdView(
////                                mContext,
////                                adLoaded.loadedAd as? NativeAd?,
////                                getBannerInlineLayout(configDto)
////                            )
////                        }
////
////                        AdNetwork.AD_MAX.value -> {
////                            viewAd = IKWidgetAdUtil.Applovin.Native.setupNativeCustomAdView(
////                                mContext,
////                                adLoaded.loadedAd as? IkObjectNativeMax?,
////                                getBannerInlineLayout(configDto)
////                            )
////                        }
////                    }
////                }
////
////                IKAdFormat.BANNER_COLLAPSE -> {
////                    when (adLoaded.adNetwork) {
////                        AdNetwork.AD_MOB.value -> {
////                            viewAd = IKWidgetAdUtil.Admob.Native.setupNativeCustomAdView(
////                                mContext, adLoaded.loadedAd as? NativeAd?, getBannerNativeLayout()
////                            )
////                        }
////
////                        AdNetwork.AD_MANAGER.value -> {
////                            viewAd = IKWidgetAdUtil.Gam.Native.setupNativeCustomAdView(
////                                mContext, adLoaded.loadedAd as? NativeAd?, getBannerNativeLayout()
////                            )
////                        }
////
////                        AdNetwork.AD_MAX.value -> {
////                            viewAd = IKWidgetAdUtil.Applovin.Native.setupNativeCustomAdView(
////                                mContext,
////                                adLoaded.loadedAd as? IkObjectNativeMax?,
////                                getBannerNativeLayout()
////                            )
////                        }
////                    }
////                }
////
////                IKAdFormat.BANNER_COLLAPSE_C1_BN -> {
////                    when (adLoaded.adNetwork) {
////                        AdNetwork.AD_MOB.value -> {
////                            viewAd = IKWidgetAdUtil.Admob.Native.setupNativeCustomAdView(
////                                mContext, adLoaded.loadedAd as? NativeAd?, getBannerNativeLayout()
////                            )
////                        }
////
////                        AdNetwork.AD_MANAGER.value -> {
////                            viewAd = IKWidgetAdUtil.Gam.Native.setupNativeCustomAdView(
////                                mContext, adLoaded.loadedAd as? NativeAd?, getBannerNativeLayout()
////                            )
////                        }
////
////                        AdNetwork.AD_MAX.value -> {
////                            viewAd = IKWidgetAdUtil.Applovin.Native.setupNativeCustomAdView(
////                                mContext,
////                                adLoaded.loadedAd as? IkObjectNativeMax?,
////                                getBannerNativeLayout()
////                            )
////                        }
////                    }
////                }
////
////                IKAdFormat.BANNER_COLLAPSE_C2 -> {
////                    when (adLoaded.adNetwork) {
////                        AdNetwork.AD_MOB.value -> {
////                            viewAd = IKWidgetAdUtil.Admob.Native.setupNativeCustomAdView(
////                                mContext,
////                                adLoaded.loadedAd as? NativeAd?,
////                                getNativeCollapseLayout()
////                            )
////                        }
////
////                        AdNetwork.AD_MANAGER.value -> {
////                            viewAd = IKWidgetAdUtil.Gam.Native.setupNativeCustomAdView(
////                                mContext,
////                                adLoaded.loadedAd as? NativeAd?,
////                                getNativeCollapseLayout()
////                            )
////                        }
////
////                        AdNetwork.AD_MAX.value -> {
////                            viewAd = IKWidgetAdUtil.Applovin.Native.setupNativeCustomAdView(
////                                mContext,
////                                adLoaded.loadedAd as? IkObjectNativeMax?,
////                                getNativeCollapseLayout()
////                            )
////                        }
////                    }
////                }
////
////                else -> {
////                    onFail.invoke(IKAdError(IKSdkErrorCode.VIEW_GROUP_NULL))
////                }
////            }
////            mCurrentAddUuid = adLoaded?.uuid
////
////            kotlin.runCatching {
////                destroyOldAd()
////                adViewLayout?.removeAllViewsInLayout()
////                adViewLayout?.addView(viewAd)
////            }.onFailure {
////                onFail.invoke(IKAdError(IKSdkErrorCode.VIEW_GROUP_NULL))
////            }.onSuccess {
////                onSuccess.invoke()
////                destroyAdObject(currentAdView)
////                delay(500)
////                currentAdView = adLoaded
////                if (isParentDestroy)
////                    destroyCurrentAd()
////            }
////        }
//    }
//
//    @SuppressLint("InflateParams")
//    private fun getBannerNativeLayout(): IkmWidgetAdLayout? {
//        if (mContext == null)
//            return null
//        val adLayout = LayoutInflater.from(mContext)
//            .inflate(R.layout.ik_temp_native_banner_normal, null, false) as? IkmWidgetAdLayout
//
//        adLayout?.apply {
//            titleView = findViewById(R.id.tempNativeBanner_headline)
//            bodyView = findViewById(R.id.tempNativeBanner_body)
//            callToActionView = findViewById(R.id.tempNativeBanner_call_to_action)
//            iconView = findViewById(R.id.tempNativeBanner_app_icon)
//            mediaView = findViewById(R.id.tempNative_media)
//        }
//
//        return adLayout
//    }
//
////    @SuppressLint("InflateParams")
////    private fun getNativeCollapseLayoutExpand(): IkmWidgetAdLayout? {
////        if (mContext == null)
////            return null
////        val adLayout = LayoutInflater.from(mContext)
////            .inflate(R.layout.ik_temp_native_collapse_expand, null, false) as? IkmWidgetAdLayout
////
////        adLayout?.apply {
////            iconView = findViewById(R.id.tempNativeBanner_app_icon)
////            titleView = findViewById(R.id.tempNativeBanner_headline)
////            bodyView = findViewById(R.id.tempNativeBanner_body)
////            mediaView = findViewById(R.id.tempNative_media)
////            mediaView?.setMediaScaleType(ImageView.ScaleType.CENTER_CROP)
////        }
////
////        return adLayout
////    }
//
////    @SuppressLint("InflateParams")
////    private fun getNativeCollapseLayout(): IkmWidgetAdLayout? {
////        if (mContext == null)
////            return null
////        val adLayout = LayoutInflater.from(mContext)
////            .inflate(R.layout.ik_temp_native_collapse, null, false) as? IkmWidgetAdLayout
////
////        adLayout?.apply {
////            callToActionView = findViewById(R.id.tempNative_call_to_action)
////            iconView = findViewById(R.id.tempNativeBanner_app_icon)
////            titleView = findViewById(R.id.tempNativeBanner_headline)
////            bodyView = findViewById(R.id.tempNativeBanner_body)
////        }
////
////        return adLayout
////    }
//
////    @SuppressLint("InflateParams")
////    private fun getBannerInlineLayout(configDto: IKSdkProdWidgetDetailDto? = null): IkmWidgetAdLayout? {
////        if (mContext == null)
////            return null
////        val adLayout = when {
////            configDto?.adSize == null -> LayoutInflater
////                .from(mContext)
////                .inflate(R.layout.ik_temp_native_large, null, false) as? IkmWidgetAdLayout
////
////            configDto.adSize.height != null && configDto.adSize.height < SMALL_BANNER_INLINE_HEIGHT -> LayoutInflater
////                .from(mContext)
////                .inflate(R.layout.ik_temp_native_80, null, false) as? IkmWidgetAdLayout
////
////            else -> LayoutInflater
////                .from(mContext)
////                .inflate(R.layout.ik_temp_native_normal, null, false) as? IkmWidgetAdLayout
////        }
////
////        adLayout?.apply {
////            titleView = findViewById(R.id.tempNative_headline)
////            bodyView = findViewById(R.id.tempNative_body)
////            callToActionView = findViewById(R.id.tempNative_call_to_action)
////            iconView = findViewById(R.id.tempNative_app_icon)
////            mediaView = findViewById(R.id.tempNative_media)
////        }
////
////        return adLayout
////    }
//
//    private var popupWindow: PopupWindow? = null
//
//    @SuppressLint("InflateParams")
//    @Suppress("DEPRECATION")
//    private fun showBannerCollapseC1(
//        screen: String,
//        sdkAdListener: IKShowWidgetAdListener,
//        configDto: IKSdkProdWidgetDetailDto
//    ) {
//        mCurrentAdFormat = IKSdkDefConst.AdFormat.BANNER_COLLAPSE_C1
//        val trackingScreenBn = screen + "_bnClC1Bn"
//        mTrackingListener.onAdPreShow(screen = screen + "_bnClC1Bn", "")
//        val bannerAd = CompletableDeferred<IKSdkBaseLoadedAd<*>?>()
//        val bannerAdInline = CompletableDeferred<IKSdkBaseLoadedAd<*>?>()
//        IKBannerCollapseBannerController.showAd(
//            screen,
//            configDto,
//            object : IKSdkShowWidgetAdListener {
//                override fun onAdReady(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    hideShimmer()
//                    handleShowAdView(adData,
//                        onSuccess = {
//                            bannerAd.complete(adData)
//                            mTrackingListener.onAdShowed(
//                                adNetworkName,
//                                trackingScreenBn,
//                                "bnClC1Bn_$scriptName",
//                                currentAdView?.adPriority ?: 0,
//                                currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                            )
//                            sdkAdListener.onAdShowed()
//                            autoReload(configDto)
//                        }, onFail = { err ->
//                            bannerAd.complete(null)
//                            onAdShowFail(err, scriptName, adNetworkName)
//                        })
//                }
//
//                override fun onAdShowFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    bannerAd.complete(null)
//                    mTrackingListener.onAdShowFailed(
//                        adNetworkName,
//                        trackingScreenBn,
//                        "bnClC1Bn_$scriptName",
//                        error
//                    )
//                }
//
//                override fun onAdClick(scriptName: String, adNetworkName: String) {
//                    sdkAdListener.onAdClick()
//                    mTrackingListener.onAdClicked(
//                        adNetworkName,
//                        trackingScreenBn,
//                        "bnClC1_bn_$scriptName",
//                        currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                    )
//                }
//
//                override fun onAdImpression(scriptName: String, adNetworkName: String) {
//                    mTrackingListener.onAdImpression(
//                        adNetworkName,
//                        trackingScreenBn,
//                        "bnClC1_bn_$scriptName",
//                        currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                    )
//                }
//
//                override fun onAdReloaded(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    handleShowAdView(adData,
//                        onSuccess = {
//
//                        }, onFail = { _ ->
//
//                        })
//                }
//
//                override fun onAdReloadFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                }
//
//            }
//        )
//        if (mFromAutoReload && mCollapseCount <= 0) {
//            bannerAdInline.complete(null)
//        } else {
//            if (mFromAutoReload)
//                mCollapseCount--
//            val trackingScreenBnCl = screen + "_bnClC1Il"
//            mTrackingListener.onAdPreShow(screen = trackingScreenBnCl, "")
//            IKBannerCollapseInlineController.showAd(
//                screen,
//                configDto,
//                object : IKSdkShowWidgetAdListener {
//                    override fun onAdReady(
//                        adData: IKSdkBaseLoadedAd<*>,
//                        scriptName: String,
//                        adNetworkName: String
//                    ) {
//                        bannerAdInline.complete(adData)
//                        mTrackingListener.onAdShowed(
//                            adNetworkName,
//                            trackingScreenBnCl,
//                            "bnClC1Il_$scriptName",
//                            currentAdView?.adPriority ?: 0,
//                            currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                        )
//                    }
//
//                    override fun onAdShowFail(
//                        error: IKAdError,
//                        scriptName: String,
//                        adNetworkName: String
//                    ) {
//                        bannerAdInline.complete(null)
//                        mTrackingListener.onAdShowFailed(
//                            adNetworkName,
//                            trackingScreenBnCl,
//                            "bnClC1Il_$scriptName",
//                            error
//                        )
//                    }
//
//                    override fun onAdClick(scriptName: String, adNetworkName: String) {
//                        sdkAdListener.onAdClick()
//                        mTrackingListener.onAdClicked(
//                            adNetworkName,
//                            trackingScreenBnCl,
//                            "bnClC1Il_$scriptName",
//                            currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                        )
//                    }
//
//                    override fun onAdImpression(scriptName: String, adNetworkName: String) {
//                        mTrackingListener.onAdImpression(
//                            adNetworkName,
//                            trackingScreenBnCl,
//                            "bnClC1Il_$scriptName",
//                            currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                        )
//                    }
//
//                    override fun onAdReloaded(
//                        adData: IKSdkBaseLoadedAd<*>,
//                        scriptName: String,
//                        adNetworkName: String
//                    ) {
//
//                    }
//
//                    override fun onAdReloadFail(
//                        error: IKAdError,
//                        scriptName: String,
//                        adNetworkName: String
//                    ) {
//
//                    }
//
//                }
//            )
//        }
//        uiScope.launchWithSupervisorJob {
//            val bn = bannerAd.await()
//            val bnIn = bannerAdInline.await()
//            if (bn == null) {
//                if (NativeBackUpLatest.getBackupAd(IKAdFormat.BANNER_COLLAPSE_C1_BN) != null)
//                    showNativeBackupLatest(
//                        adFormat = IKAdFormat.BANNER_COLLAPSE_C1_BN,
//                        scriptName = "",
//                        screen = screen,
//                        configDto = configDto,
//                        sdkAdListener = sdkAdListener
//                    ) else
//                    sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW))
//            }
//
//            if (bnIn == null)
//                return@launchWithSupervisorJob
//
//            var popupView: View? = null
//            runCatching {
//                popupWindow?.dismiss()
//                val activity =
//                    withContext(Dispatchers.Default) {
//                        IkmSdkCoreFunc.AppF.listActivity.filter { it.value != null }.values.lastOrNull()
//                    }
//                if (activity == null || activity.isDestroyed || activity.isFinishing) {
//                    return@runCatching
//                }
//                if (popupWindow == null) {
//                    val inflater =
//                        activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater?
//                    popupView = inflater?.inflate(R.layout.layout_banner_collapse, null)
//                    popupWindow = PopupWindow(
//                        popupView, LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT, false
//                    )
//
//                    popupWindow?.setOnDismissListener {
//                        destroyAdObject(bnIn)
//                    }
//                } else {
//                    popupView = popupWindow?.contentView
//                }
//
//                val viewParentPos = intArrayOf(0, 0)
//                adViewLayout?.getLocationOnScreen(viewParentPos)
//                if (configDto.collapsePosition == "top") {
//                    mFrameAdView?.post {
//                        if (activity.isDestroyed || activity.isFinishing) {
//                            return@post
//                        }
//                        runCatching {
//                            popupWindow?.showAtLocation(
//                                mFrameAdView,
//                                Gravity.TOP,
//                                0,
//                                viewParentPos[1]
//                            )
//                        }
//                    }
//                } else {
//                    val display = IkmSdkCoreFunc.AppF.getCurrentDisplay(activity!!)
//                    var outMetrics = DisplayMetrics()
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                        outMetrics = Resources.getSystem().displayMetrics
//                        if (outMetrics.density == 0f)
//                            display?.getMetrics(outMetrics)
//                    } else {
//                        display?.getMetrics(outMetrics)
//                    }
//                    val hy = outMetrics.heightPixels - viewParentPos[1]
//                    mFrameAdView?.post {
//                        if (activity.isDestroyed || activity.isFinishing) {
//                            return@post
//                        }
//                        runCatching {
//                            popupWindow?.showAtLocation(
//                                mFrameAdView,
//                                Gravity.BOTTOM,
//                                0,
//                                hy
//                            )
//                        }
//                    }
//                }
//            }.onFailure {
//                showLogD("cunnn") {
//                    "error:${it.message}"
//                }
//            }
//
//            mFrameAdView?.doOnDetach {
//                runCatching {
//                    popupWindow?.dismiss()
//                }
//            }
//
//            popupView?.findViewById<FrameLayout>(R.id.bannerAdCollapse_container)
//                ?.apply {
//                    removeAllViews()
//                    val adLoaded: IKSdkBaseLoadedAd<View>? = bnIn as? IKSdkBaseLoadedAd<View>?
//                    runCatching {
//                        addView(adLoaded?.loadedAd)
//                    }
//                }
//
//            if (configDto.collapsePosition == "top") {
//                popupView?.findViewById<ImageView>(R.id.bannerAdCollapseBottom_close)?.apply {
//                    setOnClickListener {
//                        popupWindow?.dismiss()
//                    }
//                    visibility = View.VISIBLE
//                }
//            } else {
//                popupView?.findViewById<ImageView>(R.id.bannerAdCollapseTop_close)?.apply {
//                    setOnClickListener {
//                        popupWindow?.dismiss()
//                    }
//                    visibility = View.VISIBLE
//                }
//            }
//        }
//    }
//
//    private fun showBannerCollapseC2(
//        screen: String,
//        sdkAdListener: IKShowWidgetAdListener,
//        configDto: IKSdkProdWidgetDetailDto
//    ) {
//        mCurrentAdFormat = IKSdkDefConst.AdFormat.BANNER_COLLAPSE_C2
//        val trackingScreenNt = screen + "_bnClC2Nt"
//        mTrackingListener.onAdPreShow(screen = trackingScreenNt, "")
//        val nativeAd = CompletableDeferred<IKSdkBaseLoadedAd<*>?>()
//
//        IKNativeCollapseNativeController.showAd(
//            screen = screen,
//            itemAds = configDto,
//            adListener = object : IKSdkShowWidgetAdListener {
//                override fun onAdReady(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    hideShimmer()
//                    handleShowNativeAdView(
//                        adData,
//                        IKAdFormat.BANNER_COLLAPSE_C2,
//                        onSuccess = {
//                            nativeAd.complete(adData)
//                            mTrackingListener.onAdShowed(
//                                adNetworkName,
//                                trackingScreenNt,
//                                "bnClC2Nt_$scriptName",
//                                currentAdView?.adPriority ?: 0,
//                                currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                            )
//                            sdkAdListener.onAdShowed()
//                            autoReload(configDto)
//                        }, onFail = { err ->
//                            nativeAd.complete(null)
//                            onAdShowFail(err, scriptName, adNetworkName)
//                        }
//                    )
//                }
//
//                override fun onAdReloaded(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    handleShowAdView(
//                        adData,
//                        onSuccess = {
//
//                        }, onFail = { _ ->
//
//                        }
//                    )
//                }
//
//                override fun onAdReloadFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    /* no-op */
//                }
//
//                override fun onAdShowFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    nativeAd.complete(null)
//                    mTrackingListener.onAdShowFailed(
//                        adNetworkName,
//                        trackingScreenNt,
//                        "bnClC2Nt_$scriptName",
//                        error
//                    )
//                }
//
//                override fun onAdClick(scriptName: String, adNetworkName: String) {
//                    sdkAdListener.onAdClick()
//                    mTrackingListener.onAdClicked(
//                        adNetworkName,
//                        trackingScreenNt,
//                        "bnClC2Nt_$scriptName",
//                        currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                    )
//                }
//
//                override fun onAdImpression(scriptName: String, adNetworkName: String) {
//                    mTrackingListener.onAdImpression(
//                        adNetworkName,
//                        trackingScreenNt,
//                        "bnClC2Nt_$scriptName",
//                        currentAdView?.uuid ?: mCurrentAddUuid ?: IKSdkDefConst.UNKNOWN
//                    )
//                }
//            },
//        )
//
//        uiScope.launchWithSupervisorJob {
//
//            val native = nativeAd.await()
//
//            if (native == null) {
//                if (NativeBackUpLatest.getBackupAd(IKAdFormat.BANNER_COLLAPSE_C2) != null)
//                    showNativeBackupLatest(
//                        adFormat = IKAdFormat.BANNER_COLLAPSE_C2,
//                        scriptName = "",
//                        screen = screen,
//                        configDto = configDto,
//                        sdkAdListener = sdkAdListener
//                    ) else
//                    sdkAdListener.onAdShowFail(IKAdError(IKSdkErrorCode.NOT_VALID_ADS_TO_SHOW))
//            }
//
//            var popupView: View? = null
//            runCatching {
//                popupWindow?.dismiss()
//                val activity =
//                    withContext(Dispatchers.Default) {
//                        IkmSdkCoreFunc.AppF.listActivity.filter { it.value != null }.values.lastOrNull()
//                    }
//                if (activity == null || activity.isDestroyed || activity.isFinishing) {
//                    return@runCatching
//                }
//                if (popupWindow == null) {
//                    val inflater =
//                        activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater?
//                    popupView = inflater?.inflate(R.layout.layout_banner_collapse, null)
//                    popupWindow = PopupWindow(
//                        popupView, LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT, false
//                    )
//
//                    popupWindow?.setOnDismissListener {
//                        destroyAdObject(native)
//                    }
//                } else {
//                    popupView = popupWindow?.contentView
//                }
//
//                val viewParentPos = intArrayOf(0, 0)
//                adViewLayout?.getLocationOnScreen(viewParentPos)
//                if (configDto.collapsePosition == "top") {
//                    mFrameAdView?.post {
//                        if (activity.isDestroyed || activity.isFinishing) {
//                            return@post
//                        }
//                        popupWindow?.showAtLocation(
//                            mFrameAdView,
//                            Gravity.TOP,
//                            0,
//                            viewParentPos[1]
//                        )
//                    }
//                } else {
//                    val display = IkmSdkCoreFunc.AppF.getCurrentDisplay(activity!!)
//                    var outMetrics = DisplayMetrics()
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                        outMetrics = Resources.getSystem().displayMetrics
//                        if (outMetrics.density == 0f)
//                            display?.getMetrics(outMetrics)
//                    } else {
//                        display?.getMetrics(outMetrics)
//                    }
//                    val hy = outMetrics.heightPixels - viewParentPos[1]
//                    mFrameAdView?.post {
//                        if (activity.isDestroyed || activity.isFinishing) {
//                            return@post
//                        }
//                        mFrameAdView?.post {
//                            popupWindow?.showAtLocation(
//                                mFrameAdView,
//                                Gravity.BOTTOM,
//                                0,
//                                hy
//                            )
//                        }
//                    }
//
//                }
//            }.onFailure {
//                it.printStackTrace()
//            }
//
//            mFrameAdView?.doOnDetach {
//                runCatching {
//                    popupWindow?.dismiss()
//                }
//            }
//
////            popupView?.findViewById<FrameLayout>(R.id.bannerAdCollapse_container)
////                ?.apply {
////                    removeAllViews()
////                    val adLoaded: IKSdkBaseLoadedAd<View>? = native as? IKSdkBaseLoadedAd<View>?
////                    runCatching {
////                        val layout = when (adLoaded?.adNetwork) {
////                            AdNetwork.AD_MOB.value -> {
////                                IKWidgetAdUtil.Admob.Native.setupNativeCustomAdView(
////                                    mContext,
////                                    adLoaded.loadedAd as? NativeAd?,
////                                    getNativeCollapseLayoutExpand()
////                                )
////                            }
////
////                            AdNetwork.AD_MANAGER.value -> {
////                                IKWidgetAdUtil.Gam.Native.setupNativeCustomAdView(
////                                    mContext,
////                                    adLoaded.loadedAd as? NativeAd?,
////                                    getNativeCollapseLayoutExpand(),
////                                )
////                            }
////
////                            AdNetwork.AD_MAX.value -> {
////                                IKWidgetAdUtil.Applovin.Native.setupNativeCustomAdView(
////                                    mContext,
////                                    adLoaded.loadedAd as? IkObjectNativeMax?,
////                                    getNativeCollapseLayoutExpand(),
////                                )
////                            }
////
////                            else -> null
////                        }
////                        addView(layout)
////                    }
////                }
//
//
//            popupView?.findViewById<ImageView>(R.id.bannerAdCollapseTop_close)?.apply {
//                setOnClickListener {
//                    popupWindow?.dismiss()
//                }
//                visibility = View.VISIBLE
//            }
//        }
//    }
//
//    private fun destroyOldAd() {
//        if (currentAdView?.isDisplayAdView == true || currentAdView?.isBackup == true) {
//            return
//        }
//        // destroyAd(currentAdView)
//    }
//
//    private fun destroyAd(value: Any?) {
//        if (NativeBackUpLatest.getCurrentBackupAd() == value) {
//            return
//        }
//        if (value == null)
//            return
//        val adData = (value as? IKSdkBaseLoadedAd<*>)
//        if (adData?.isBackup == true)
//            return
//        uiScope.launchWithSupervisorJob {
//
//            adData?.removeListener()
//            val loadedAd = adData?.loadedAd ?: value
//            when {
//                loadedAd is NativeAd -> {
//                    kotlin.runCatching {
//                        val adv = (mAdLayout?.parent as? NativeAdView)
//                        if (adv?.parent != null)
//                            (adv.parent as? ViewGroup)?.removeView(adv)
//                    }
//                    runCatching {
//                        (loadedAd as? NativeAd)?.destroy()
//                        ((mFrameAdView?.getChildAt(0) as? ViewGroup)?.getChildAt(0)
//                                as? NativeAdView)?.destroy()
//                        (mAdLayout?.parent as? NativeAdView)?.destroy()
//                        ((((mFrameAdView?.getChildAt(0) as? ViewGroup)?.getChildAt(0))
//                                as? ViewGroup)?.getChildAt(0) as? NativeAdView)?.destroy()
//                    }
//                }
//
//                loadedAd is AdView -> {
//                    kotlin.runCatching {
//                        if (loadedAd.parent != null)
//                            (loadedAd.parent as? ViewGroup)?.removeView(loadedAd)
//                    }
//                    runCatching {
//                        (loadedAd as? AdView)?.destroy()
//                        ((mFrameAdView?.getChildAt(0) as? ViewGroup)?.getChildAt(0)
//                                as? AdView)?.destroy()
//                        ((((mFrameAdView?.getChildAt(0) as? ViewGroup)?.getChildAt(0))
//                                as? ViewGroup)?.getChildAt(0) as? AdView)?.destroy()
//                    }
//                }
//
//                loadedAd is AdManagerAdView -> {
//                    kotlin.runCatching {
//                        if (loadedAd.parent != null)
//                            (loadedAd.parent as? ViewGroup)?.removeView(loadedAd)
//                    }
//                    runCatching {
//                        (loadedAd as? AdManagerAdView)?.destroy()
//                        ((mFrameAdView?.getChildAt(0) as? ViewGroup)?.getChildAt(0)
//                                as? AdManagerAdView)?.destroy()
//                        ((((mFrameAdView?.getChildAt(0) as? ViewGroup)?.getChildAt(0))
//                                as? ViewGroup)?.getChildAt(0) as? AdManagerAdView)?.destroy()
//                    }
//                }
//
//                IKApplovinHelper.isBannerAd(loadedAd) -> {
//                    (loadedAd as MaxAdView).let {
//                        kotlin.runCatching {
//                            if (loadedAd.parent != null)
//                                (loadedAd.parent as? ViewGroup)?.removeView(loadedAd)
//                        }
//                        runCatching {
//                            (loadedAd as? MaxAdView)?.destroy()
//                            ((mFrameAdView?.getChildAt(0) as? ViewGroup)?.getChildAt(0)
//                                    as? MaxAdView)?.destroy()
//                            ((((mFrameAdView?.getChildAt(0) as? ViewGroup)?.getChildAt(0))
//                                    as? ViewGroup)?.getChildAt(0) as? MaxAdView)?.destroy()
//                        }
//                    }
//                }
//
//                IKApplovinHelper.isNativeAd(loadedAd) -> {
//                    runCatching {
//                        (loadedAd as? IkObjectNativeMax)?.apply {
//                            this.loader.destroy()
//                        }
//                    }
//                }
//
//                else -> {
//                    kotlin.runCatching {
////                        if (loadedAd is BannerView) {
////                            kotlin.runCatching {
////                                if (loadedAd.parent != null)
////                                    (loadedAd.parent as? ViewGroup)?.removeView(loadedAd)
////                            }
////                            runCatching {
////                                (loadedAd as? BannerView)?.destroy()
////                                ((mFrameAdView?.getChildAt(0) as? ViewGroup)?.getChildAt(0)
////                                        as? BannerView)?.destroy()
////                                ((((mFrameAdView?.getChildAt(0) as? ViewGroup)?.getChildAt(0))
////                                        as? BannerView)?.getChildAt(0) as? BannerView)?.destroy()
////                            }
////                        }
//                    }
//                }
//            }
//            adData?.loadedAd = null
//            adData?.removeListener()
//        }
//    }
//
//    private fun destroyAdObject(value: Any?) {
//        if (NativeBackUpLatest.getCurrentBackupAd() == value) {
//            return
//        }
//        if (value == null)
//            return
//        val adData = (value as? IKSdkBaseLoadedAd<*>)
//        if (adData?.isBackup == true)
//            return
//        if (adData?.isDisplayAdView == true || adData?.isBackup == true) {
//            return
//        }
//        uiScope.launchWithSupervisorJob {
//
//            adData?.removeListener()
//            val loadedAd = adData?.loadedAd ?: value
//            when {
//                loadedAd is NativeAd -> {
//                    runCatching {
//                        (loadedAd as? NativeAd)?.destroy()
//                    }
//                }
//
//                loadedAd is AdView -> {
//                    runCatching {
//                        (loadedAd as? AdView)?.destroy()
//                    }
//                }
//
//                loadedAd is AdManagerAdView -> {
//                    runCatching {
//                        (loadedAd as? AdManagerAdView)?.destroy()
//                    }
//                }
//
//                IKApplovinHelper.isBannerAd(loadedAd) -> {
//                    runCatching {
//                        (loadedAd as? MaxAdView)?.destroy()
//                    }
//                }
//
//                IKApplovinHelper.isNativeAd(loadedAd) -> {
//                    runCatching {
//                        (loadedAd as? IkObjectNativeMax)?.apply {
//                            this.loader.destroy()
//                        }
//                    }
//                }
//
//                else -> {
//                    kotlin.runCatching {
////                        if (loadedAd is BannerView) {
////                            (loadedAd as? BannerView)?.destroy()
////                        }
//                    }
//                }
//            }
//            adData?.loadedAd = null
////            adData?.removeListener()
//        }
//    }
//
//    fun destroyCurrentAd() {
//        stopAutoReload()
//        isParentDestroy = true
//        mIsRecall = false
//        mIsAdLoaded = false
//        mIsAdLoading = false
//        destroyAd(currentAdView)
//        currentAdView?.removeListener()
//        currentAdView = null
//        mCurrentListener = null
//        mCurrentRecallListener = null
//        mAdLayout?.removeAllViews()
//        mAdViewLoading?.removeAllViews()
//        mFrameAdView?.removeAllViews()
//    }
//
//    fun dstrAdCheck() {
//        isParentDestroy = true
//        mIsRecall = false
//        mIsAdLoaded = false
//        mIsAdLoading = false
//        if (currentAdView?.isDisplayAdView != true && currentAdView?.isBackup != true) {
////            destroyAd(currentAdView)
//            destroyAdObject(currentAdView)
//        }
//
//        mCurrentListener = null
//        mCurrentRecallListener = null
//        mAdLayout?.removeAllViews()
//        mAdViewLoading?.removeAllViews()
//        mFrameAdView?.removeAllViews()
//    }
//
//    private fun trackingWidget(
//        adStatus: String,
//        screen: String,
//        vararg multiValue: Pair<String, String>
//    ) {
//        IKSdkTrackingHelper.trackingSdkShowAd(
//            adFormat = IKSdkDefConst.AdFormat.WIDGET,
//            adStatus = adStatus,
//            screen = screen,
//            *multiValue
//        )
//    }
//
//    fun destroyContext() {
//        mFrameAdView = null
//        mContext = null
//    }
//
//    fun showLogD(tag: String, message: () -> String) {
//        IKLogs.d("WidgetAdView") {
//            "${tag}:$mScreen:" + message.invoke()
//        }
//    }
//
//    fun showNativeBackupLatest(
//        adFormat: IKAdFormat,
//        scriptName: String,
//        screen: String,
//        configDto: IKSdkProdWidgetDetailDto,
//        sdkAdListener: IKShowWidgetAdListener
//    ) {
//        IKNativeController.showAdNativeBackupLatest(
//            scriptName,
//            screen,
//            object : IKSdkShowWidgetAdListener {
//                override fun onAdReady(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    hideShimmer()
//                    handleShowNativeAdView(
//                        adLoaded = NativeBackUpLatest.getCurrentBackupAd(),
//                        adFormat = adFormat,
//                        onSuccess = {
//                            sdkAdListener.onAdShowed()
//                            CoreTracking.trackingSdkBackupAd(
//                                adFormat = adFormat.value,
//                                adStatus = IKSdkDefConst.AdStatus.SHOWED,
//                                screen = screen,
//                                actionWithAds = IKSdkDefConst.AdAction.SHOW
//                            )
//                        },
//                        onFail = { err ->
//                            onAdShowFail(err, scriptName, adNetworkName)
//                        },
//                        configDto = configDto,
//                    )
//                }
//
//                override fun onAdReloaded(
//                    adData: IKSdkBaseLoadedAd<*>,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    /* no-op */
//                }
//
//                override fun onAdReloadFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    /* no-op */
//                }
//
//                override fun onAdShowFail(
//                    error: IKAdError,
//                    scriptName: String,
//                    adNetworkName: String
//                ) {
//                    sdkAdListener.onAdShowFail(error)
//                    CoreTracking.trackingSdkBackupAd(
//                        adFormat = adFormat.value,
//                        adStatus = IKSdkDefConst.AdStatus.SHOW_FAIL,
//                        screen = screen,
//                        actionWithAds = IKSdkDefConst.AdAction.SHOW,
//                        Pair(IKTrackingConst.ParamName.ERROR_CODE, "${error.code}"),
//                        Pair(IKTrackingConst.ParamName.MESSAGE, error.message)
//                    )
//                }
//
//                override fun onAdClick(scriptName: String, adNetworkName: String) {
//                    sdkAdListener.onAdClick()
//                }
//
//                override fun onAdImpression(scriptName: String, adNetworkName: String) {
//                    /* no-op */
//                }
//            }
//        )
//    }
//}