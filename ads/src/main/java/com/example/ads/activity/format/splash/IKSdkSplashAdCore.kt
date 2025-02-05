//package com.example.ads.activity.format.splash
//
//import android.app.Activity
//import com.google.android.ump.ConsentRequestParameters
//import com.google.android.ump.UserMessagingPlatform
//import com.example.ads.activity.IKSdkConstants
//import com.example.ads.activity.IKSdkController.disableFirstAd
////import com.example.ads.activity.IKSdkController.firstAdsType
//import com.example.ads.activity.IKSdkController.getFirstAdType
//import com.example.ads.activity.IKSdkController.requestCMPForm
//import com.example.ads.activity.IKSdkController.splashInit
//import com.example.ads.activity.IKSdkOptions
////import com.example.ads.activity.core.firebase.IKRemoteDataManager
//import com.example.ads.activity.data.db.IKDataRepository
//import com.example.ads.activity.data.db.IkmSdkCacheFunc
//import com.example.ads.activity.data.dto.pub.IKAdError
//import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
//import com.example.ads.activity.data.dto.sdk.data.IKSdkFirstAdDto
//import com.example.ads.activity.data.local.IKSdkDataStore
//import com.example.ads.activity.data.local.IKSdkDataStoreConst
//import com.example.ads.activity.format.intertial.IKInterController
//import com.example.ads.activity.format.open_ads.IKAppOpenController
//import com.example.ads.activity.listener.pub.IKLoadAdListener
//import com.example.ads.activity.listener.pub.IKNoneSplashAdListener
//import com.example.ads.activity.listener.pub.IKShowAdListener
//import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
//import com.example.ads.activity.listener.sdk.IKSdkShowFirstAdListener
//import com.example.ads.activity.tracking.CoreTracking
//import com.example.ads.activity.tracking.IKSdkTrackingHelper
//import com.example.ads.activity.utils.IKLogs
//import com.example.ads.activity.utils.IKSdkDefConst
//import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import com.example.ads.activity.utils.IKSdkSerUtils
//import com.example.ads.activity.utils.IKSdkUtilsCore
//import com.example.ads.activity.utils.IKTrackingConst
//import com.example.ads.activity.utils.IKTrackingConst.mapNewAdFormat
//import com.example.ads.activity.utils.IkmSdkCoreFunc
//import kotlinx.coroutines.CompletableDeferred
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.cancel
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.joinAll
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.lang.ref.WeakReference
//import java.util.concurrent.atomic.AtomicBoolean
//
//object IKSdkSplashAdCore {
//    private val mConfigJob = SupervisorJob()
//    private val mUiScope = CoroutineScope(Dispatchers.Main + mConfigJob)
//    private var disableDataLocal = false
//    private var mIsFirstAdLoading = false
//    private var mIsFirstBackupAdLoading = false
//
//    private val mRepository: IKDataRepository? by lazy {
//        IKDataRepository.getInstance()
//    }
//
//    private fun runningShowFirstAd(
//        activity: Activity,
//        callback: IKSdkShowFirstAdListener,
//        foreShow: Boolean = false
//    ) {
//        showLogSdk("runningShowFirstAd") { "start run" }
//        trackAdEventStart(
//            IKTrackingConst.EventName.APP_START_TRACK, IKSdkDefConst.StartAppStatus.START_SHOW
//        )
//
//        mUiScope.launchWithSupervisorJob(Dispatchers.Default) {
//            val timeoutJob = launch {
////                delay(IKSdkConstants.TIME_OUT_CHECK_SHOW_AD)
//                showLogSdk("runningShowFirstAd") { "start run START_SHOW_FAIL" }
//                trackAdEventStart(
//                    IKTrackingConst.EventName.APP_START_TRACK,
//                    IKSdkDefConst.StartAppStatus.START_SHOW_TIMEOUT
//                )
////                delay(IKSdkConstants.TIME_OUT_CHECK_SHOW_AD)
//                trackAdEventStart(
//                    IKTrackingConst.EventName.APP_START_TRACK,
//                    IKSdkDefConst.StartAppStatus.FORGE_DISMISS_AD
//                )
//                callback.onAdsDismiss()
//                cancel()
//            }
//
//            val adsListener = object : IKSdkShowFirstAdListener {
//                override fun onAdsShowed(priority: Int, format: String) {
//                    callback.onAdsShowed(priority, format)
//                    timeoutJob.cancel()
//                }
//
//                override fun onAdsDismiss() {
//                    callback.onAdsDismiss()
//                    trackAdEventStart(
//                        IKTrackingConst.EventName.APP_START_TRACK,
//                        IKSdkDefConst.StartAppStatus.DISMISS_AD
//                    )
//                    timeoutJob.cancel()
//                }
//
//                override fun onAdsShowFail(error: IKAdError) {
//                    callback.onAdsShowFail(error)
//                    timeoutJob.cancel()
//                }
//            }
//            //sau 5s không nhận dc callback thì call dissmiss
////            if (firstAdsType != IKSdkDefConst.FirstAdType.OPEN &&
////                (!foreShow && IKInterController.isAdReady())
////            ) {
////                IKInterController.showAd(
////                    activity,
////                    IKSdkDefConst.AdScreen.START,
////                    adsListener,
////                    true
////                )
////                showLogSdk("runningShowFirstAd") { "showInterstitialAdCore run" }
////                return@launchWithSupervisorJob
////            }
//
//            val config = mRepository?.getConfigOpen(IKSdkDefConst.AdLabel.START)
//            if (config == null) {
//                adsListener.onAdsShowFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
//                showLogSdk("runningShowFirstAd") { "error NO_DATA_TO_LOAD_AD" }
//                return@launchWithSupervisorJob
//            }
//            if (config.enable == true) {
//                IKAppOpenController.showAd(activity, IKSdkDefConst.AdScreen.START, adsListener)
//                showLogSdk("runningShowFirstAd") { "showAppOpenAdCore run" }
//                return@launchWithSupervisorJob
//            } else {
//                adsListener.onAdsShowFail(IKAdError(IKSdkErrorCode.DISABLE_SHOW))
//                showLogSdk("runningShowFirstAd") { "error DISABLE_SHOW" }
//                return@launchWithSupervisorJob
//            }
//        }
//
//    }
//
//    fun loadAndShowSplashScreenAdNonAsync(activity: Activity?, listener: IKShowAdListener?) {
//        showLogD("loadAndShowSplashScreenAdNonAsync") { "start show" }
//        mUiScope.launchWithSupervisorJob(Dispatchers.Main) {
//            loadSplashScreenAd(activity, null)
//            showSplashScreenAd(activity, listener)
//        }
//    }
//
//    suspend fun loadAndShowSplashScreenAd(
//        activity: Activity?,
//        listener: IKShowAdListener?
//    ): Job? {
//        showLogD("loadAndShowSplashScreenAd") { "start show" }
//        loadSplashScreenAd(activity, null)
//        return showSplashScreenAd(activity, listener)
//    }
//
//    fun loadSplashScreenAd(activity: Activity?, listener: IKLoadAdListener?) {
//        val weakReference = WeakReference(activity)
//        val context = weakReference.get()?.applicationContext
//        val act = weakReference.get()
//        showLogSdk("loadSplashScreenAd") { "start run" }
//        mUiScope.launchWithSupervisorJob(Dispatchers.IO) {
//            this.launch {
//                CoreTracking.setUserRetention(activity)
//            }
//            trackAdEventStart(
//                IKTrackingConst.EventName.APP_START_TRACK,
//                IKSdkDefConst.StartAppStatus.PRE_START_LOAD
//            )
//            val canRequestAd = IKSdkDataStore.getBoolean(IKSdkDataStoreConst.KEY_CMP_STATUS, false)
//            val checkCountry = IKSdkUtilsCore.checkCmpCountryCode(context)
//            val needRequest =
//                IKSdkDataStore.getBoolean(
//                    IKSdkDataStoreConst.CMP_CONFIG_REQUEST_ENABLE,
//                    false
//                ) || checkCountry
//            showLogSdk("loadSplashScreenAd") { "cmp start needRequest=$needRequest,canRequestAd=$canRequestAd" }
//            if (!needRequest) {
//                showLogSdk("loadSplashScreenAd") { "cmp not need request" }
//                loadSplashScreenAdCore(listener)
//                return@launchWithSupervisorJob
//            }
//            if (!IKSdkUtilsCore.isConnectionAvailableAsync()) {
//                showLogSdk("loadSplashScreenAd") { "cmp not connect internet" }
//                loadSplashScreenAdCore(listener)
//                return@launchWithSupervisorJob
//            }
//            if (canRequestAd) {
//                loadSplashScreenAdCore(listener)
//                val params = ConsentRequestParameters
//                    .Builder()
//                    .setTagForUnderAgeOfConsent(false)
//                    .build()
//                showLogSdk("loadSplashScreenAd") { "cmp start request form" }
//                if (act != null) {
//                    val consentInformation =
//                        UserMessagingPlatform.getConsentInformation(act.applicationContext)
//                    consentInformation.requestConsentInfoUpdate(
//                        act,
//                        params,
//                        {
//                            showLogSdk("loadSplashScreenAd") { "cmp recheck onUpdate" }
//                            this.launchWithSupervisorJob {
//                                if (consentInformation.canRequestAds())
//                                    IKSdkDataStore.putBoolean(
//                                        IKSdkDataStoreConst.KEY_CMP_STATUS,
//                                        true
//                                    )
//                                else IKSdkDataStore.putBoolean(
//                                    IKSdkDataStoreConst.KEY_CMP_STATUS,
//                                    false
//                                )
//                            }
//                        },
//                        {
//                            showLogSdk("loadSplashScreenAd") { "cmp  recheck onFail" }
//                        })
//                }
//                return@launchWithSupervisorJob
//            }
//
//            requestCMPForm(act)
//            loadSplashScreenAdCore(listener)
//        }
//    }
//
//    @Deprecated(
//        message = "This function may return null unexpectedly due to the asynchronous nature of coroutines. Use showSplashScreenAd directly within your coroutine context instead.",
//        replaceWith = ReplaceWith(
//            expression = "CoroutineScope(Dispatchers.Main).launch { showSplashScreenAd(activity, listener) }",
//            imports = arrayOf("kotlinx.coroutines.launch", "kotlinx.coroutines.Dispatchers")
//        )
//    )
//    fun showSplashScreenAdNor(
//        activity: Activity?, listener: IKShowAdListener?
//    ): Job? {
//        showLogSdk("showSplashScreenAdNor") { "start run" }
//        var job: Job? = null
//        mUiScope.launchWithSupervisorJob {
//            job = showSplashScreenAd(activity, listener)
//        }
//        return job
//    }
//
//    suspend fun showSplashScreenAd(
//        activity: Activity?, listener: IKShowAdListener?
//    ): Job? {
//        showLogSdk("showSplashScreenAd") { "start run" }
//        kotlin.runCatching {
//            activity?.javaClass?.name?.let {
//                IKSdkDataStore.putString(
//                    IKSdkDataStoreConst.SPLASH_NAME_SHF,
//                    it
//                )
//            }
//        }
//        trackAdEvent(
//            IKTrackingConst.EventName.APP_START_TRACK, 0,
//            0, IKSdkDefConst.StartAppStatus.PRE_SHOW
//        )
//
//        val startTime = System.currentTimeMillis()
//        var completableJob: Job? = null
//        var onAdsShowFail: ((error: IKAdError) -> Unit)? = { error ->
//            completableJob?.cancel()
//
//            mUiScope.launchWithSupervisorJob(Dispatchers.Main) {
//                listener?.onAdsShowFail(error)
//            }
//
//            mUiScope.launchWithSupervisorJob(Dispatchers.IO) {
//                val timeElapsed = logAdEvent("First Ads fail", startTime)
//                trackAdEvent(
//                    IKTrackingConst.EventName.APP_START_TRACK, timeElapsed,
//                    0, IKSdkDefConst.StartAppStatus.SHOW_FAIL, error = error
//                )
//                showLogSdk("showSplashScreenAd") { "FirstOpenApp onAdsShowFail_ $error" }
////                IKRemoteDataManager.checkUpdateRemoteConfig()
//                IkmSdkCacheFunc.AD.resetInterAdFrequency()
//            }
//
//        }
//        val weakActivity = WeakReference(activity).get()
//        if (weakActivity == null) {
//            onAdsShowFail?.invoke(IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID))
//            onAdsShowFail = null
//            return null
//        }
//        return withContext(Dispatchers.Default) {
//            if (disableFirstAd) {
//                onAdsShowFail?.invoke(IKAdError(IKSdkErrorCode.DISABLE_SHOW))
//                onAdsShowFail = null
//                return@withContext null
//            }
//            IKSdkOptions.reloadNetworkState(weakActivity)
//
//            if (!IKSdkUtilsCore.canShowAdAsync()) {
//                onAdsShowFail?.invoke(IKAdError(IKSdkErrorCode.USER_PREMIUM))
//                onAdsShowFail = null
//                return@withContext null
//            }
//            getFirstAdType()
//
//            IkmSdkCoreFunc.SdkF.onHandleFirstAd = true
//
//            showLogSdk("showSplashScreenAd") { "FirstOpenApp time First Ads startTime" }
//
//            var onAdsShowed: ((priority: Int, format: String) -> Unit)? = { priority, format ->
//                completableJob?.cancel()
//                mUiScope.launchWithSupervisorJob(Dispatchers.Main) {
//                    listener?.onAdsShowed()
//                }
//                mUiScope.launchWithSupervisorJob(Dispatchers.IO) {
//                    val timeElapsed = logAdEvent("First Ads showed", startTime)
//                    trackAdEvent(
//                        IKTrackingConst.EventName.APP_START_TRACK, timeElapsed,
//                        priority, IKSdkDefConst.StartAppStatus.SHOWED, format
//                    )
////                    IKRemoteDataManager.checkUpdateRemoteConfig()
//                    IkmSdkCacheFunc.AD.resetInterAdFrequency()
//                }
//            }
//
//            var timeOutShowAd: (() -> Unit)? = null
//            if (IKSdkOptions.mEnableTimeOutShowOpenAd) {
//                timeOutShowAd = {
//                    mUiScope.launchWithSupervisorJob(Dispatchers.Main) {
//                        listener?.onAdsShowTimeout()
//                    }
//                    showLogSdk("showSplashScreenAd") { "FirstOpenApp time out" }
//                }
//            }
//            val customCallback = object : IKSdkShowFirstAdListener {
//                override fun onAdsShowFail(error: IKAdError) {
//                    onAdsShowFail?.invoke(error)
//                    onAdsShowed = null
//                    onAdsShowFail = null
//                    IkmSdkCoreFunc.SdkF.onHandleFirstAd = false
//                }
//
//                override fun onAdsShowed(priority: Int, format: String) {
//                    onAdsShowed?.invoke(priority, format)
//                    onAdsShowed = null
//                    onAdsShowFail = null
//                    if (IKSdkOptions.mEnableTimeOutShowOpenAd) {
//                        mUiScope.launchWithSupervisorJob {
//                            kotlin.runCatching {
//                                delay(IKSdkOptions.mTimeOutShowOpenAd)
//                                timeOutShowAd?.invoke()
//                                timeOutShowAd = null
//                            }
//                        }
//                    }
//                }
//
//                override fun onAdsDismiss() {
//                    IkmSdkCoreFunc.SdkF.onHandleFirstAd = false
//                    onAdsShowFail = null
//                    timeOutShowAd = null
//                    mUiScope.launchWithSupervisorJob(Dispatchers.Main) {
//                        listener?.onAdsDismiss()
//                    }
//                    completableJob?.cancel()
//                }
//            }
//            val firstAdDto = mRepository?.getSDKFirstAd()
////            var timeOut = getTimeOut(
////                firstAdDto?.timeOut ?: 0L,
////                IKSdkConstants.DEFAULT_TIME_OUT_START
////            )
////            val timeOutFirstTimeExtend = getTimeOut(
////                firstAdDto?.timeExtend ?: 0L,
////                IKSdkConstants.DEFAULT_TIME_OUT_EXTEND
////            )
//
//            val isFirstCallInit =
//                IKSdkDataStore.getBoolean(IKSdkDataStoreConst.FIRST_INIT_ADS_SDK, true)
//            if (isFirstCallInit) {
////                timeOut = maxOf(timeOut, timeOutFirstTimeExtend)
//                IKSdkDataStore.putBoolean(IKSdkDataStoreConst.FIRST_INIT_ADS_SDK, false)
//            }
//
////            if (checkAvailableFirstAds()) {
////                if (IkmSdkCoreFunc.HandleEna.onHandleFx) {
////                    runningShowFirstAd(weakActivity, customCallback, true)
////                    return@withContext null
////                } else if (!checkAvailableFirstAds()) {
//////                    val reducedTimeOut = (timeOut * 0.7).toLong()
//////                    timeOut = getTimeOut(firstAdDto?.timeReload ?: 0L, reducedTimeOut)
////                }
////                //loadFirstAd()
////            } else {
////                //loadFirstAd()
////            }
//
//            val splitTime = 400L
//            val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
//            val enableBid = firstAdDto?.enableBid == true
//
////            val timeOutWaitLoading: Long = getTimeOut(
////                firstAdDto?.timeOutWaitLoading ?: 0L,
////                (timeOut * 0.7).toLong()
////            )
//
//            val hasBeenCalled = AtomicBoolean(false)
//            var checkAdLoaded = false
//            var adFirstLoading = false
////            completableJob = IKSdkSerUtils.startTimer(
////                timeOutStart = timeOut,
////                period = splitTime,
////                scope = scope,
////                onTick = { timePassed ->
////                    val isConnected = IKSdkUtilsCore.isConnectionAvailableAsync()
////                    showLogSdk("showSplashScreenAd") { "onTick timePassed=$timePassed" }
////
////                    if (!IKSdkUtilsCore.canShowAdAsync() || disableFirstAd) {
////                        onAdsShowFail?.invoke(IKAdError(IKSdkErrorCode.DISABLE_SHOW))
////                        onAdsShowFail = null
////                        showLogSdk("showSplashScreenAd") { "FirstOpenApp run check4 DISABLE_SHOW" }
////                        completableJob?.cancel()
////                        return@startTimer
////                    }
////
////                    if (!isConnected) {
////                        onAdsShowFail?.invoke(IKAdError(IKSdkErrorCode.NETWORK_AD_NOT_VALID_TO_LOAD))
////                        onAdsShowFail = null
////                        showLogSdk("showSplashScreenAd") { "FirstOpenApp run check5 internet check" }
////                        runningShowFirstAd(weakActivity, customCallback)
////                        return@startTimer
////                    }
////
////                    adFirstLoading = if (firstAdsType != IKSdkDefConst.FirstAdType.OPEN)
////                        IKInterController.adFirstLoading else IKAppOpenController.adFirstLoading
////
////                    checkAdLoaded = checkAvailableFirstAds(
////                        targetPriority = !enableBid && !adFirstLoading,
////                        foreCheck = !adFirstLoading
////                    )
////
////                    if (checkAdLoaded) {
////                        showLogSdk("showSplashScreenAd") { "FirstOpenApp run check1" }
////                        if (hasBeenCalled.compareAndSet(false, true)) {
////                            showLogSdk("showSplashScreenAd") { "FirstOpenApp run check1 true" }
////                            runningShowFirstAd(weakActivity, customCallback)
////                            completableJob?.cancel()
////                            return@startTimer
////                        }
////                    }
////
////                    if (firstAdDto?.enableTimeOutWaitLoading == true && !adFirstLoading) {
////                        val checkAdLoadedEnd = checkAvailableFirstAds(
////                            targetPriority = false,
////                            foreCheck = true
////                        ) && timePassed > timeOutWaitLoading
////
////                        if (checkAdLoadedEnd && hasBeenCalled.compareAndSet(false, true)) {
////                            showLogSdk("showSplashScreenAd") { "FirstOpenApp run check2 true" }
////                            runningShowFirstAd(weakActivity, customCallback)
////                            completableJob?.cancel()
////                            return@startTimer
////                        }
////                    }
////
////                },
////                onFinish = {
////                    showLogSdk("showSplashScreenAd") { "onFinish" }
////                    if (hasBeenCalled.compareAndSet(false, true))
////                        runningShowFirstAd(weakActivity, customCallback)
////                }
////            ).apply {
////                invokeOnCompletion {
////                    showLogSdk("showSplashScreenAd") { "on other" }
////                    if (hasBeenCalled.compareAndSet(false, true))
////                        runningShowFirstAd(weakActivity, customCallback)
////                }
////            }
//
//            return@withContext completableJob
//        }
//    }
//
//    private fun showLogSdk(tag: String, message: () -> String) {
//        IKLogs.dSdk("IKSdkController") {
//            "${tag}:" + message.invoke()
//        }
//    }
//
//    private fun showLogD(tag: String, message: () -> String) {
//        IKLogs.d("IKSdkController") {
//            "${tag}:" + message.invoke()
//        }
//    }
//
//
//    private fun logAdEvent(event: String, startTime: Long): Long {
//        val timeElapsed = System.currentTimeMillis() - startTime
//        showLogSdk("logAdEvent") { "FirstOpenApp time $event = $timeElapsed" }
//        return timeElapsed
//    }
//
//    private suspend fun getTimeOut(
//        configTime: Long,
//        defaultTimeout: Long
//    ): Long {
//        return withContext(Dispatchers.Default) {
//            val timeout = if (configTime == 0L) defaultTimeout else configTime
//            timeout
//        }
//    }
//
//    private fun loadBackupFirstAd(dto: IKSdkFirstAdDto?, callback: IKSdkLoadAdCoreListener) {
//        showLogD("loadBackupFirstAd") { "start" }
//        if (dto == null) {
//            callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
//            showLogD("loadBackupFirstAd") { "error ${IKSdkErrorCode.NO_DATA_TO_LOAD_AD}" }
//            return
//        }
//        if (dto.backupAdEnable != true) {
//            callback.onAdLoadFail(IKAdError(IKSdkErrorCode.DISABLE_PRELOAD))
//            showLogD("loadBackupFirstAd") { "error ${IKSdkErrorCode.DISABLE_PRELOAD}" }
//            return
//        }
//        showLogD("loadBackupFirstAd") { "load ad backup format=${dto.backupAdFormat}" }
//        when (dto.backupAdFormat) {
//            IKSdkDefConst.AdFormat.OPEN -> {
//                IKAppOpenController.loadBackupAd(
//                    mUiScope,
//                    object : IKSdkLoadAdCoreListener {
//                        override fun onAdLoaded() {
//                            callback.onAdLoaded()
//                        }
//
//                        override fun onAdLoadFail(error: IKAdError) {
//                            callback.onAdLoadFail(error)
//                        }
//                    })
//            }
//
//            IKSdkDefConst.AdFormat.INTER -> {
//                IKInterController.loadBackupAd(
//                    mUiScope,
//                    object : IKSdkLoadAdCoreListener {
//                        override fun onAdLoaded() {
//                            callback.onAdLoaded()
//                        }
//
//                        override fun onAdLoadFail(error: IKAdError) {
//                            callback.onAdLoadFail(error)
//                        }
//                    })
//            }
//
//            else -> {
//                IKAppOpenController.loadBackupAd(
//                    mUiScope,
//                    object : IKSdkLoadAdCoreListener {
//                        override fun onAdLoaded() {
//                            callback.onAdLoaded()
//                        }
//
//                        override fun onAdLoadFail(error: IKAdError) {
//                            callback.onAdLoadFail(error)
//                        }
//                    })
//            }
//        }
//    }
//
//    private suspend fun <T> getDataWithTimeout(
//        timeoutMillis: Long,
//        intervalMillis: Long,
//        dataFetcher: suspend () -> T?
//    ): T? {
//        val startTime = System.currentTimeMillis()
//
//        while (System.currentTimeMillis() - startTime < timeoutMillis) {
//            val data = dataFetcher()
//            if (data != null) {
//                return data
//            }
//            delay(intervalMillis)
//        }
//
//        return null
//    }
//
//    private suspend fun <T> getDataWithTimeoutList(
//        timeoutMillis: Long,
//        intervalMillis: Long,
//        dataFetcher: suspend () -> List<T>?
//    ): List<T>? {
//        val startTime = System.currentTimeMillis()
//
//        while (System.currentTimeMillis() - startTime < timeoutMillis) {
//            val data = dataFetcher()
//            if (!data.isNullOrEmpty()) {
//                return data
//            }
//            delay(intervalMillis)
//        }
//
//        return null
//    }
//
//    private suspend fun loadSplashScreenAdCore(listener: IKLoadAdListener?) {
//        showLogD("loadSplashScreenAdCore") { "start" }
//        var firstAdDto = withContext(Dispatchers.IO) {
//            mRepository?.getSDKFirstAd()
//        }
//        trackAdEventStart(
//            IKTrackingConst.EventName.APP_START_TRACK, IKSdkDefConst.StartAppStatus.START_LOAD
//        )
//        withContext(Dispatchers.IO) {
//            firstAdDto = getDataWithTimeout(1000, 200) {
//                mRepository?.getSDKFirstAd()
//            }
////            disableDataLocal = firstAdDto?.disableDataLocal == true
//            showLogD("loadSplashScreenAdCore") { "start with delay 1" }
//            getFirstAdType()
//        }
//
////        disableDataLocal = firstAdDto?.disableDataLocal == true
//
//        val loadJobs = mutableListOf<Job>()
//        disableDataLocal = true
//        fun loadInter() {
//            if (!disableDataLocal) {
//                loadJobs += mUiScope.launchWithSupervisorJob {
//                    val localDto = getDataWithTimeoutList(3000, 300) {
//                        mRepository?.getAllDataLocalDefault()
//                    }
//                    mIsFirstAdLoading = true
//                    IKInterController.loadFirstAds(
//                        localDto,
//                        object : IKSdkLoadAdCoreListener {
//                            override fun onAdLoaded() {
//                                mIsFirstAdLoading = false
//                                listener?.onAdLoaded()
//                            }
//
//                            override fun onAdLoadFail(error: IKAdError) {
//                                mIsFirstAdLoading = false
//                                listener?.onAdLoadFail(error)
//                            }
//                        })
//                }
//            } else {
//                loadJobs += mUiScope.launchWithSupervisorJob {
//                    val fullAdsDto = getDataWithTimeout(3000, 300) {
//                        mRepository?.getSDKInter()
//                    }
//                    if (fullAdsDto != null) {
//                        IKInterController.loadFirstAds(
//                            null,
//                            object : IKSdkLoadAdCoreListener {
//                                override fun onAdLoaded() {
//                                    mIsFirstAdLoading = false
//                                    listener?.onAdLoaded()
//                                }
//
//                                override fun onAdLoadFail(error: IKAdError) {
//                                    mIsFirstAdLoading = false
//                                    listener?.onAdLoadFail(error)
//                                }
//                            })
//                    }
//                }
//            }
//        }
//
//        fun loadOpen() {
//            if (!disableDataLocal) {
//                loadJobs += mUiScope.launchWithSupervisorJob {
//                    val localDto = getDataWithTimeoutList(3000, 300) {
//                        mRepository?.getAllDataLocalDefault()
//                    }
//                    mIsFirstAdLoading = true
//                    IKAppOpenController.loadFirstAds(
//                        localDto,
//                        object : IKSdkLoadAdCoreListener {
//                            override fun onAdLoaded() {
//                                mIsFirstAdLoading = false
//                                listener?.onAdLoaded()
//                            }
//
//                            override fun onAdLoadFail(error: IKAdError) {
//                                mIsFirstAdLoading = false
//                                listener?.onAdLoadFail(error)
//                            }
//                        })
//                }
//            } else {
//                loadJobs += mUiScope.launchWithSupervisorJob {
//                    getDataWithTimeout(3000, 300) {
//                        mRepository?.getSDKOpen()
//                    }
//                    mIsFirstAdLoading = true
//                    IKAppOpenController.loadFirstAds(null, object : IKSdkLoadAdCoreListener {
//                        override fun onAdLoaded() {
//                            mIsFirstAdLoading = false
//                            listener?.onAdLoaded()
//                        }
//
//                        override fun onAdLoadFail(error: IKAdError) {
//                            mIsFirstAdLoading = false
//                            listener?.onAdLoadFail(error)
//                        }
//                    })
//                }
//            }
//        }
//
//        fun loadBackup() {
//            loadJobs += mUiScope.launchWithSupervisorJob {
//                delay(2000)
//                mIsFirstBackupAdLoading = true
//                loadBackupFirstAd(firstAdDto, object : IKSdkLoadAdCoreListener {
//                    override fun onAdLoaded() {
//                        mIsFirstBackupAdLoading = false
//                    }
//
//                    override fun onAdLoadFail(error: IKAdError) {
//                        mIsFirstBackupAdLoading = false
//                    }
//                })
//            }
//        }
////        showLogD("loadSplashScreenAdCore") { "start load first ads type=$firstAdsType" }
////        if (firstAdDto?.customLabel == "mode_1") {
////            loadInter()
////            loadOpen()
////        } else if (firstAdDto?.customLabel == "mode_2") {
////            if (firstAdsType == IKSdkDefConst.AdFormat.INTER) {
////                loadInter()
////                delay(3000)
////                loadOpen()
////            } else {
////                loadOpen()
////                delay(3000)
////                loadInter()
////            }
////        } else {
////            if (firstAdsType == IKSdkDefConst.AdFormat.INTER) {
////                loadInter()
////            } else {
////                loadOpen()
////                mUiScope.launch {
////                    delay(5000)
////                    IKInterController.loadAd("inapp",null)
////                }
////            }
////        }
//        loadBackup()
//        loadJobs.joinAll()
//
//    }
//
////    private suspend fun checkAvailableFirstAds(
////        targetPriority: Boolean = false,
////        foreCheck: Boolean = false
////    ): Boolean {
////        return if (firstAdsType != IKSdkDefConst.FirstAdType.OPEN) {
////            var hasAds = IKInterController.isAdReady(
////                targetPriority
////            )
////
////            if (!hasAds && foreCheck) {
////                hasAds = IKAppOpenController.isAdReady() == true
////            }
////            hasAds
////        } else {
////            return IKAppOpenController.isAdReady(
////                targetPriority
////            )
////        }
////    }
//
//    fun noneShowSplashAd(activity: Activity?, listener: IKNoneSplashAdListener) {
//        val weakReference = WeakReference(activity)
//        val context = weakReference.get()?.applicationContext
//        val act = weakReference.get()
//        showLogSdk("noneShowSplashAd") { "start run" }
//        splashInit()
//        mUiScope.launchWithSupervisorJob(Dispatchers.IO) {
//            val canRequestAd = IKSdkDataStore.getBoolean(IKSdkDataStoreConst.KEY_CMP_STATUS, false)
//            val checkCountry = IKSdkUtilsCore.checkCmpCountryCode(context)
//            val needRequest =
//                IKSdkDataStore.getBoolean(
//                    IKSdkDataStoreConst.CMP_CONFIG_REQUEST_ENABLE,
//                    false
//                ) || checkCountry
//            showLogSdk("noneShowSplashAd") { "cmp start needRequest=$needRequest,canRequestAd=$canRequestAd" }
//            if (!needRequest) {
//                mUiScope.launchWithSupervisorJob {
//                    listener.onMove()
//                }
//                showLogSdk("noneShowSplashAd") { "cmp not need request" }
//                return@launchWithSupervisorJob
//            }
//            if (!IKSdkUtilsCore.isConnectionAvailableAsync()) {
//                mUiScope.launchWithSupervisorJob {
//                    listener.onMove()
//                }
//                showLogSdk("noneShowSplashAd") { "cmp not connect internet" }
//                return@launchWithSupervisorJob
//            }
//            if (canRequestAd) {
//                mUiScope.launchWithSupervisorJob {
//                    listener.onMove()
//                }
//                val params = ConsentRequestParameters
//                    .Builder()
//                    .setTagForUnderAgeOfConsent(false)
//                    .build()
//                showLogSdk("noneShowSplashAd") { "cmp start request form" }
//                if (act != null) {
//                    val consentInformation =
//                        UserMessagingPlatform.getConsentInformation(act.applicationContext)
//                    consentInformation.requestConsentInfoUpdate(
//                        act,
//                        params,
//                        {
//                            showLogSdk("noneShowSplashAd") { "cmp recheck onUpdate" }
//                            this.launchWithSupervisorJob {
//                                if (consentInformation.canRequestAds())
//                                    IKSdkDataStore.putBoolean(
//                                        IKSdkDataStoreConst.KEY_CMP_STATUS,
//                                        true
//                                    )
//                                else IKSdkDataStore.putBoolean(
//                                    IKSdkDataStoreConst.KEY_CMP_STATUS,
//                                    false
//                                )
//                            }
//                        },
//                        {
//                            showLogSdk("noneShowSplashAd") { "cmp  recheck onFail" }
//                        })
//                }
//                return@launchWithSupervisorJob
//            }
//
//            requestCMPForm(act)
//            mUiScope.launchWithSupervisorJob {
//                listener.onMove()
//            }
//        }
//    }
//
//    private suspend fun trackAdEvent(
//        eventName: String, timeElapsed: Long, priority: Int,
//        status: String, format: String = "", error: IKAdError? = null
//    ) {
//        val networkType = IkmSdkCoreFunc.AppF?.mSDKNetworkType?.networkType ?: IKSdkDefConst.EMPTY
//        val lastTimeRemoteConfig = kotlin.runCatching {
//            IKSdkDataStore.getString(IKSdkDataStoreConst.LAST_TIME_GET_REMOTE_CONFIG, "")
//        }.getOrDefault("")
//        val trackingParams = mutableListOf<Pair<String, String>>().apply {
//            add(Pair(IKTrackingConst.ParamName.TIME, "$timeElapsed"))
//            add(Pair(IKTrackingConst.ParamName.NETWORK_TYPE, networkType))
//            add(Pair(IKTrackingConst.ParamName.AD_STATUS, status))
//            add(Pair(IKTrackingConst.ParamName.SUB_AD_FORMAT, format))
//            add(Pair(IKTrackingConst.ParamName.AD_FORMAT, mapNewAdFormat(format)))
//            add(Pair(IKTrackingConst.ParamName.LT_R, lastTimeRemoteConfig))
////            add(Pair(IKTrackingConst.ParamName.FA_R, "$mDataFromRemote"))
//            add(Pair(IKTrackingConst.ParamName.PRIORITY, "$priority"))
//            error?.let {
//                add(Pair(IKTrackingConst.ParamName.ERROR_CODE, "${it.code}"))
//                add(Pair(IKTrackingConst.ParamName.MESSAGE, it.message))
//            }
//        }
//        IKSdkTrackingHelper.customizeTracking(eventName, *trackingParams.toTypedArray())
//    }
//
//    private fun trackAdEventStart(
//        eventName: String, status: String
//    ) {
//        val networkType = IkmSdkCoreFunc.AppF?.mSDKNetworkType?.networkType ?: IKSdkDefConst.EMPTY
//
//        val trackingParams = mutableListOf<Pair<String, String>>().apply {
//            add(Pair(IKTrackingConst.ParamName.NETWORK_TYPE, networkType))
//            add(Pair(IKTrackingConst.ParamName.AD_STATUS, status))
//        }
//        IKSdkTrackingHelper.customizeTracking(eventName, *trackingParams.toTypedArray())
//    }
//}