package com.example.ads.activity.format.base

import com.example.ads.activity.data.db.IKDataRepository
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.AdsScriptName
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.pub.IKAdFormat
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkBaseDto
import com.example.ads.activity.data.dto_secur.IKAdLoadMode
import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
import com.example.ads.activity.utils.IKLogs
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import com.example.ads.activity.utils.IKSdkSerUtils.initMediation
import com.example.ads.activity.utils.IKSdkUtilsCore
import com.example.ads.activity.utils.IkmSdkCoreFunc
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

abstract class IKSdkBaseAdController {
    companion object {
        private const val TAG_LOG = "BaseAdController"
        const val DELAY_CHECK_AD = 1000L
        const val DELAY_SHOWING_AD = 30000L
        const val DELAY_LOADING_AD = 30000L
    }

    private val mJob = SupervisorJob()
    protected val mUiScope = CoroutineScope(Dispatchers.Main + mJob)

    val mRepository: IKDataRepository? by lazy {
        IKDataRepository.getInstance()
    }

    private var adShowingJob: Job? = null
    var isAdShowing = false
        protected set(value) {
            if (value) {
                cancelJob(adShowingJob)
                adShowingJob = mUiScope.launchWithSupervisorJob {
                    delay(DELAY_SHOWING_AD)
                    isAdShowing = false
                }
            } else {
                cancelJob(adShowingJob)
            }
            field = value
        }
    private var adFirstLoadingJob: Job? = null
    var adFirstLoading = false
        protected set(value) {
            if (value) {
                cancelJob(adFirstLoadingJob)
                adFirstLoadingJob = mUiScope.launchWithSupervisorJob {
                    delay(DELAY_LOADING_AD)
                    adFirstLoading = false
                }
            } else {
                cancelJob(adFirstLoadingJob)
            }
            field = value
        }

    protected var mAdDto: IKSdkBaseDto? = null

    protected abstract val adMax: IKSdkBaseAd<*>?

    protected abstract val admob: IKSdkBaseAd<*>?

    protected abstract val adGam: IKSdkBaseAd<*>?

    protected abstract val adFairBid: IKSdkBaseAd<*>?

    protected abstract val adIK: IKSdkBaseAd<*>?

    protected abstract val adFormat: IKAdFormat

    fun cancelJob(job: Job?) {
        kotlin.runCatching {
            job?.cancel()
        }
    }

    protected abstract suspend fun getAdDto(): IKSdkBaseDto?
    protected abstract suspend fun getBackupAd(): IKAdapterDto?

    private fun startLoadAdsSequentially(
        scope: CoroutineScope,
        dto: List<IKAdapterDto>,
        callback: IKSdkLoadAdCoreListener
    ) {
        showLogSdk("startLoadAdsSequentially") { "start run" }
        val listDto = dto.iterator()

        scope.launchWithSupervisorJob {
            val loadedAd = async(Dispatchers.Default) {
                while (listDto.hasNext()) {
                    val itemAds = kotlin.runCatching {
                        listDto.next()
                    }.getOrNull()
                    val adLoaded = if (itemAds == null)
                        false
                    else
                        if (itemAds.enablePreload != true)
                            false
                        else
                            loadAdAndWait(scope, itemAds)

                    if (adLoaded) {
                        return@async true
                    }
                }
                return@async false
            }
            val loadedBackup = CompletableDeferred<Boolean>()
            loadBackupAd(scope, object : IKSdkLoadAdCoreListener {
                override fun onAdLoaded() {
                    loadedBackup.complete(true)
                }

                override fun onAdLoadFail(error: IKAdError) {
                    if (error.code == IKSdkErrorCode.READY_CURRENT_AD.code) {
                        loadedBackup.complete(true)
                    } else {
                        loadedBackup.complete(false)
                    }
                }
            })
            if (loadedAd.await() || loadedBackup.await()) {
                callback.onAdLoaded()
            } else {
                callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER))
            }
        }
    }

    private suspend fun loadAdAndWait(
        scope: CoroutineScope,
        dto: IKAdapterDto
    ): Boolean {
        showLogSdk("loadAdAndWait") { "start run" }
        val loadAdJob = CompletableDeferred<Boolean>()

        val adLoader = when (dto.adNetwork) {
            AdNetwork.AD_MOB.value -> admob
            AdNetwork.AD_MAX.value -> adMax
            AdNetwork.AD_MANAGER.value -> adGam
            AdNetwork.AD_FAIR_BID.value -> adFairBid
            AdNetwork.AD_IK.value -> adIK
            else -> null
        }
        adLoader?.let {
            adLoader.loadAd(
                scope,
                dto,
                object : IKSdkLoadAdCoreListener {
                    override fun onAdLoaded() {
                        loadAdJob.complete(true)
                    }

                    override fun onAdLoadFail(error: IKAdError) {
                        if (error.code == IKSdkErrorCode.READY_CURRENT_AD.code)
                            loadAdJob.complete(true)
                        else
                            loadAdJob.complete(false)
                    }
                }
            )
        } ?: loadAdJob.complete(false)

        return loadAdJob.await()
    }


    private suspend fun loadAdParallel(
        scope: CoroutineScope,
        maxQueue: Int,
        dto: List<IKAdapterDto>,
        callback: IKSdkLoadAdCoreListener
    ) {
        showLogSdk("loadAdParallel") { "start run" }
        val latch = CountDownLatch(dto.size)
        val adLoadedSuccessfully = AtomicBoolean(false)
        val adBackupLoadedSuccessfully = AtomicBoolean(false)

        val semaphore =
            Semaphore(
                if (maxQueue <= 0)
                    IKSdkDefConst.MAX_QUE_LOAD
                else maxQueue
            )

        val adLoadingJobs = dto.map { ad ->
            scope.launchWithSupervisorJob {
                when (ad.adNetwork) {
                    AdNetwork.AD_MOB.value,
                    AdNetwork.AD_MAX.value,
                    AdNetwork.AD_MANAGER.value,
                    AdNetwork.AD_IK.value,
                    AdNetwork.AD_FAIR_BID.value -> {
                        if (ad.enablePreload == true) {
                            val adLoader = when (ad.adNetwork) {
                                AdNetwork.AD_MOB.value -> admob
                                AdNetwork.AD_MAX.value -> adMax
                                AdNetwork.AD_MANAGER.value -> adGam
                                AdNetwork.AD_FAIR_BID.value -> adFairBid
                                AdNetwork.AD_IK.value -> adIK
                                else -> null
                            }

                            adLoader?.let {
                                semaphore.withPermit {
                                    it.loadAd(
                                        scope,
                                        ad,
                                        object : IKSdkLoadAdCoreListener {
                                            override fun onAdLoaded() {
                                                adLoadedSuccessfully.set(true)
                                                latch.countDown()
                                            }

                                            override fun onAdLoadFail(error: IKAdError) {
                                                if (error.code == IKSdkErrorCode.READY_CURRENT_AD.code) {
                                                    adLoadedSuccessfully.set(true)
                                                }
                                                latch.countDown()
                                            }
                                        })
                                }
                            } ?: run {
                                latch.countDown()
                            }
                        } else {
                            latch.countDown()
                        }
                    }

                    else -> {
                        latch.countDown()
                    }
                }
            }
        }

        val backupAdJob = scope.launchWithSupervisorJob {
            loadBackupAd(scope, object : IKSdkLoadAdCoreListener {
                override fun onAdLoaded() {
                    adBackupLoadedSuccessfully.set(true)
                }

                override fun onAdLoadFail(error: IKAdError) {
                    if (error.code == IKSdkErrorCode.READY_CURRENT_AD.code) {
                        adBackupLoadedSuccessfully.set(true)
                    } else {
                        adBackupLoadedSuccessfully.set(false)
                    }
                }
            })
        }

        adLoadingJobs.forEach { it.join() }
        withContext(Dispatchers.IO) {
            latch.await()
        }
        backupAdJob.join()

        if (adLoadedSuccessfully.get()) {
            callback.onAdLoaded()
        } else {
            if (adBackupLoadedSuccessfully.get())
                callback.onAdLoaded()
            else
                callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_AD_FROM_SERVER))
        }
    }


    fun loadAdBase(screen: String, callback: IKSdkLoadAdCoreListener) {
        showLogSdk("loadAd") { "start run" }
        if (IKSdkDefConst.Config.exitParamWidget.contains(screen)) {
            callback.onAdLoadFail(IKAdError(IKSdkErrorCode.USER_EXIT_APP))
            showLogSdk("loadAd") { "fail USER_EXIT_APP" }
            return
        }
        mUiScope.launchWithSupervisorJob {
            if (!IKSdkUtilsCore.canLoadAdAsync()) {
                callback.onAdLoadFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
                showLogSdk("loadAd") { "fail USER_PREMIUM" }
                return@launchWithSupervisorJob
            }
            getAdDto()
            if (mAdDto?.adapters.isNullOrEmpty()) {
                callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_SCREEN_ID_AD))
                showLogSdk("loadAd") { "fail NO_SCREEN_ID_AD" }
                return@launchWithSupervisorJob
            }

            withContext(Dispatchers.IO) {
                val filterData = getAdDataInApp(screen)

                if (filterData.isNullOrEmpty()) {
                    callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_SCREEN_ID_AD))
                    showLogSdk("loadAd") { "fail NO_SCREEN_ID_AD 2" }
                    return@withContext
                }
                showLogSdk("loadAd") { "initMediation" }
                filterData.forEach {
//                    it.initMediation()
                }
                if (mAdDto?.loadMode == IKAdLoadMode.SEQUENTIALLY.value) {
                    startLoadAdsSequentially(this@launchWithSupervisorJob, filterData, callback)
                } else {
                    loadAdParallel(
                        this@launchWithSupervisorJob,
                        mAdDto?.maxQueue ?: 0,
                        filterData,
                        callback
                    )
                }
            }
        }
    }

    fun loadBackupAd(coroutineScope: CoroutineScope, callbackLoad: IKSdkLoadAdCoreListener?) {
        //load default ads
        showLogSdk("loadBackupAd") { "start run" }
        mUiScope.launchWithSupervisorJob {
            if (!IKSdkUtilsCore.canLoadAdAsync()) {
                callbackLoad?.onAdLoadFail(IKAdError(IKSdkErrorCode.USER_PREMIUM))
                showLogSdk("loadBackupAd") { "fail USER_PREMIUM" }
                return@launchWithSupervisorJob
            }

            val otherObject = getBackupAd()
            if (otherObject == null || !otherObject.enable) {
                callbackLoad?.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
                showLogSdk("loadBackupAd") { "fail NO_DATA_TO_LOAD_AD" }
                return@launchWithSupervisorJob
            }
            val callback = object : IKSdkLoadAdCoreListener {
                override fun onAdLoaded() {
                    callbackLoad?.onAdLoaded()
                    showLogSdk("loadBackupAd") { "onAdLoaded" }
                }

                override fun onAdLoadFail(error: IKAdError) {
                    if (error.code == IKSdkErrorCode.READY_CURRENT_AD.code) {
                        callbackLoad?.onAdLoaded()
                        showLogSdk("loadBackupAd") { "onAdLoadFail READY_CURRENT_AD loaded" }
                    } else {
                        callbackLoad?.onAdLoadFail(error)
                        showLogSdk("loadBackupAd") { "onAdLoadFail $error" }
                    }
                }
            }
            var otherAdsDto =
                otherObject.adData?.firstOrNull { it.label != IKSdkDefConst.AdLabel.NFX }

            if (IkmSdkCoreFunc.HandleEna.onHandleFx) {
                otherAdsDto = otherObject.adData?.find { it.label == IKSdkDefConst.AdLabel.NFX }
            }
            if (otherAdsDto == null) {
                callbackLoad?.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
                return@launchWithSupervisorJob
            }
            if (otherObject.enablePreload != true) {
                callback.onAdLoadFail(IKAdError(IKSdkErrorCode.DISABLE_PRELOAD))
                return@launchWithSupervisorJob
            }
            showLogSdk("loadBackupAd") { "start load ${otherObject.adNetwork}" }
            when (otherObject.adNetwork) {
                AdNetwork.AD_MOB.value -> {
                    admob?.loadBackupAd(
                        coroutineScope,
                        IKAdUnitDto(otherAdsDto.adUnitId, 0),
                        AdsScriptName.OPEN_ADMOB_BACKUP.value,
                        otherObject.showPriority ?: 0,
                        callback
                    )
                }

                AdNetwork.AD_MANAGER.value -> {
                    adGam?.loadBackupAd(
                        coroutineScope,
                        IKAdUnitDto(otherAdsDto.adUnitId, 0),
                        AdsScriptName.OPEN_ADMANAGER_BACKUP.value,
                        otherObject.showPriority ?: 0,
                        callback
                    )
                }

                AdNetwork.AD_MAX.value -> {
                    adMax?.loadBackupAd(
                        coroutineScope,
                        IKAdUnitDto(otherAdsDto.adUnitId, 0),
                        AdsScriptName.OPEN_MAX_BACKUP.value,
                        otherObject.showPriority ?: 0,
                        callback
                    )
                }

                AdNetwork.AD_FAIR_BID.value -> {
                    adFairBid?.loadBackupAd(
                        coroutineScope,
                        IKAdUnitDto(otherAdsDto.adUnitId, 0),
                        AdsScriptName.OPEN_MAX_BACKUP.value,
                        otherObject.showPriority ?: 0,
                        callback
                    )
                }

                AdNetwork.AD_IK.value -> {
                    adIK?.loadBackupAd(
                        coroutineScope,
                        IKAdUnitDto(otherAdsDto.adUnitId, 0),
                        AdsScriptName.OPEN_MAX_BACKUP.value,
                        otherObject.showPriority ?: 0,
                        callback
                    )
                }

                else -> {
                    callback.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
                }
            }
        }
    }

    protected suspend fun getAdDataInApp(screen: String): List<IKAdapterDto>? {
        val screenLabel = when (adFormat) {
            IKAdFormat.BANNER, IKAdFormat.NATIVE_FULL, IKAdFormat.BANNER_COLLAPSE_C1,
            IKAdFormat.NATIVE, IKAdFormat.MREC, IKAdFormat.NATIVE_BANNER,
            IKAdFormat.BANNER_COLLAPSE, IKAdFormat.BANNER_INLINE -> mRepository?.getConfigWidget(
                screen
            )?.adLabel

            else -> ""
        }

        var filterData =
            mAdDto?.adapters?.filter { it.enable && it.label == screenLabel }
                ?.sortedByDescending { it.showPriority }

        if (IkmSdkCoreFunc.HandleEna.onHandleFx) {
            if (mAdDto != null) {
                val listNFX = mAdDto?.adapters
                    ?.filter { it.enable && it.label == IKSdkDefConst.AdLabel.NFX }
                    ?.sortedByDescending { it.showPriority }
                if (!listNFX.isNullOrEmpty()) {
                    filterData = listNFX
                }
            }
        }

        if (filterData.isNullOrEmpty())
            filterData =
                mAdDto?.adapters?.filter { it.enable && it.label == IKSdkDefConst.AdLabel.IN_APP }
                    ?.sortedByDescending { it.showPriority }
        return filterData
    }

    private fun showLogSdk(tag: String, message: () -> String) {
        IKLogs.dSdk(TAG_LOG) {
            "${tag}:" + message.invoke()
        }
    }

    private fun showLogD(tag: String, message: () -> String) {
        IKLogs.dSdk(TAG_LOG) {
            "${tag}:" + message.invoke()
        }
    }
}