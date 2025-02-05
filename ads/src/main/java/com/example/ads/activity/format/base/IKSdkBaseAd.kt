package com.example.ads.activity.format.base

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.data.dto.AdNetwork
import com.example.ads.activity.data.dto.IKSdkViewSize
import com.example.ads.activity.data.dto.pub.IKAdError
import com.example.ads.activity.data.dto.sdk.IKSdkBaseLoadedAd
import com.example.ads.activity.data.dto.sdk.IKSdkErrorCode
import com.example.ads.activity.data.dto.sdk.data.IKAdUnitDto
import com.example.ads.activity.data.dto.sdk.data.IKAdapterDto
import com.example.ads.activity.data.dto_secur.IKAdLoadMode
import com.example.ads.activity.listener.sdk.IKSdkAdCallback
import com.example.ads.activity.listener.sdk.IKSdkAdLoadCoreCallback
import com.example.ads.activity.listener.sdk.IKSdkLoadAdCoreListener
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKLogs
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import com.example.ads.activity.utils.IKSdkSerUtils.initMediation
import com.example.ads.activity.utils.IKTrackingConst
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.Date
import java.util.UUID

abstract class IKSdkBaseAd<T : Any>(val adNetwork: AdNetwork) {
    abstract val logTag: String
    protected val adNetworkName = adNetwork.value
    open var adFormatName: String = ""

    companion object {
        const val TIME_DELAY_CHECK_SHOWN = 1000L
        const val SAME_SIZE = 1
        const val SAME_SIZE_BACKUP = 1
    }

    private val getAdRedMutex = Mutex()

    private val mAdsHandler: Handler? by lazy {
        Handler(Looper.getMainLooper())
    }
    private var mAdShowingRunner = kotlinx.coroutines.Runnable {
        isAdShowing = false
    }
    var isAdShowing = false
        protected set(value) {
            showLogD("set isAdsShowing=$value")
            if (value) {
                mAdsHandler?.removeCallbacks(mAdShowingRunner)
                mAdsHandler?.postDelayed(mAdShowingRunner, 60000)
            } else {
                mAdsHandler?.removeCallbacks(mAdShowingRunner)
            }
            field = value
        }

    protected var mListAd: MutableList<IKSdkBaseLoadedAd<T>> =
        mutableListOf()
    protected val mutexListAd = Mutex()
    protected val mutexListAdBackup = Mutex()
    private var mAdLoadingRunner = kotlinx.coroutines.Runnable {
        mIsAdLoading = false
    }
    var mIsAdLoading = false
        set(value) {
            showLogD("set isLoading=$value")
            if (value) {
                mAdsHandler?.removeCallbacks(mAdLoadingRunner)
                mAdsHandler?.postDelayed(mAdLoadingRunner, 60000)
            } else {
                mAdsHandler?.removeCallbacks(mAdLoadingRunner)
            }
            field = value
        }

    private var mBackupAdLoadingRunner = kotlinx.coroutines.Runnable {
        mIsBackupAdLoading = false
    }
    var mCurrentAdSize = 0

    protected var mIsBackupAdLoading = false
        set(value) {
            showLogD("set isLoading=$value")
            if (value) {
                mAdsHandler?.removeCallbacks(mBackupAdLoadingRunner)
                mAdsHandler?.postDelayed(mBackupAdLoadingRunner, 30000)
            } else {
                mAdsHandler?.removeCallbacks(mBackupAdLoadingRunner)
            }
            field = value
        }

    var mListBackupAd: MutableList<IKSdkBaseLoadedAd<T>> =
        mutableListOf()

    private var enablePreloadAd = false

    open fun loadAd(
        coroutineScope: CoroutineScope,
        adData: IKAdapterDto?,
        callback: IKSdkLoadAdCoreListener?
    ) {
        IKLogs.d("cnvttt") { "$adFormatName mlist=${mListAd.size}, backup=${mListBackupAd.size}" }
        var callbackNew: IKSdkLoadAdCoreListener? =
            callback
        showLogD("loadAd start")
        val context = IKSdkApplicationProvider.getContext()
        if (context == null) {
            showLogD("loadAd context null")
            callbackNew?.onAdLoadFail(IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID))
            return
        }
        if (adData == null) {
            callbackNew?.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_SCREEN_ID_AD))
            showLogD("loadAd adData null")
            return
        }
        enablePreloadAd = adData.enablePreload == true
        if (adData.enablePreload != true) {
            callbackNew?.onAdLoadFail(IKAdError(IKSdkErrorCode.DISABLE_PRELOAD))
            showLogD("loadAd disable preload")
            return
        }
        if (mIsAdLoading) {
            callbackNew?.onAdLoadFail(IKAdError(IKSdkErrorCode.CURRENT_AD_LOADING))
            showLogD("loadAd loading")
            return
        }
        coroutineScope.launchWithSupervisorJob {
            mIsAdLoading = true
            showLogD("loadAd run")
            loadWFAd(coroutineScope, context, adData, object : IKSdkLoadAdCoreListener {
                override fun onAdLoaded() {
                    callbackNew?.onAdLoaded()
                    mIsAdLoading = false
                    showLogD("loadAd loaded")
                    callbackNew = null
                }

                override fun onAdLoadFail(error: IKAdError) {
                    mIsAdLoading = false
                    callbackNew?.onAdLoadFail(error)
                    showLogD("loadAd fail")
                    callbackNew = null
                }
            })
        }
    }

    open fun loadAndShowAd(
        coroutineScope: CoroutineScope,
        adData: IKAdapterDto?,
        callback: IKSdkLoadAdCoreListener?
    ) {

        IKLogs.d("cnvttt") { "$adFormatName mlist=${mListAd.size}, backup=${mListBackupAd.size}" }
        var callbackNew: IKSdkLoadAdCoreListener? =
            callback
        showLogD("loadAd start")
        val context = IKSdkApplicationProvider.getContext()
        if (context == null) {
            showLogD("loadAd context null")
            callbackNew?.onAdLoadFail(IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID))
            return
        }
        if (adData == null) {
            callbackNew?.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_SCREEN_ID_AD))
            showLogD("loadAd adData null")
            return
        }
//        adData.initMediation()
        enablePreloadAd = adData.enablePreload == true
        if (adData.enablePreload != true) {
            callbackNew?.onAdLoadFail(IKAdError(IKSdkErrorCode.DISABLE_PRELOAD))
            showLogD("loadAd disable preload")
            return
        }

        coroutineScope.launchWithSupervisorJob {
            showLogD("loadAd run")
            loadWFAd(coroutineScope, context, adData, object : IKSdkLoadAdCoreListener {
                override fun onAdLoaded() {
                    callbackNew?.onAdLoaded()
                    showLogD("loadAd loaded")
                    callbackNew = null
                }

                override fun onAdLoadFail(error: IKAdError) {
                    callbackNew?.onAdLoadFail(error)
                    showLogD("loadAd fail")
                    callbackNew = null
                }
            })
        }
    }

    private suspend fun loadWFAd(
        coroutineScope: CoroutineScope,
        context: Context,
        dataDto: IKAdapterDto,
        loadAdsListener: IKSdkLoadAdCoreListener
    ) {
        if (dataDto.adData.isNullOrEmpty()) {
            loadAdsListener.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
            showLogD("loadWFAd empty")
            return
        }

        showLogD("loadWFAd s=${dataDto.adData?.size}")
        if (dataDto.adData?.size == 1) {
            val adObject = withContext(Dispatchers.Default) {
                dataDto.adData?.firstOrNull()
            }
            if (adObject == null) {
                loadAdsListener.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_DATA_TO_LOAD_AD))
                return
            }
            loadSingleAd(
                coroutineScope,
                context,
                adObject,
                dataDto.showPriority ?: 0,
                IKSdkDefConst.TXT_SCRIPT_LOAD + (dataDto.adNetwork ?: IKSdkDefConst.UNKNOWN),
                callback = object : IKSdkAdLoadCoreCallback<IKSdkBaseLoadedAd<T>, IKAdError> {
                    override fun onAdLoadFail(adNetwork: String, error: IKAdError) {
                        loadAdsListener.onAdLoadFail(error)
                    }

                    override fun onAdLoaded(adNetwork: String, adsResult: IKSdkBaseLoadedAd<T>?) {
                        loadAdsListener.onAdLoaded()
                    }
                }
            )
            return
        }
        when (dataDto.loadMode) {
            IKAdLoadMode.PARALLEL.value -> {
                fetchAdParallel(
                    coroutineScope,
                    dataDto.maxQueue ?: 0,
                    IKSdkDefConst.TXT_SCRIPT_LOAD + (dataDto.adNetwork
                        ?: IKSdkDefConst.UNKNOWN) + "_" + (dataDto.loadMode
                        ?: IKSdkDefConst.UNKNOWN),
                    dataDto.adData ?: listOf(),
                    dataDto.showPriority ?: 0,
                    loadAdsListener
                )
            }

            IKAdLoadMode.SEQUENTIALLY.value -> {
                fetchAdsSequent(
                    coroutineScope,
                    IKSdkDefConst.TXT_SCRIPT_LOAD + (dataDto.adNetwork
                        ?: IKSdkDefConst.UNKNOWN) + "_" + (dataDto.loadMode
                        ?: IKSdkDefConst.UNKNOWN),
                    dataDto.adData ?: listOf(),
                    dataDto.showPriority ?: 0,
                    loadAdsListener
                )
            }

        }
    }

    open suspend fun loadSingleAd(
        coroutineScope: CoroutineScope,
        context: Context,
        adData: IKAdUnitDto,
        showPriority: Int,
        scriptName: String,
        callback: IKSdkLoadAdCoreListener
    ) {
        loadSingleAd(
            coroutineScope,
            context,
            adData,
            showPriority,
            scriptName,
            object : IKSdkAdLoadCoreCallback<IKSdkBaseLoadedAd<T>, IKAdError> {
                override fun onAdLoadFail(adNetwork: String, error: IKAdError) {
                    callback.onAdLoadFail(error)
                }

                override fun onAdLoaded(adNetwork: String, adsResult: IKSdkBaseLoadedAd<T>?) {
                    callback.onAdLoaded()
                }

            })
    }

    open suspend fun loadSingleAd(
        coroutineScope: CoroutineScope,
        context: Context,
        adData: IKAdUnitDto,
        showPriority: Int,
        scriptName: String,
        callback: IKSdkAdLoadCoreCallback<IKSdkBaseLoadedAd<T>, IKAdError>? = null
    ) {
        if (adData.adUnitId.isNullOrBlank()) {
            callback?.onAdLoadFail(adNetworkName, IKAdError(IKSdkErrorCode.UNIT_AD_NOT_VALID))
            return
        }

        showLogD("loadSingleAd start")
        loadCoreAd(
            coroutineScope,
            adData,
            scriptName,
            showPriority,
            false,
            object : IKSdkAdCallback<T> {
                override fun onAdFailedToLoad(adNetwork: String, error: IKAdError) {
                    showLogE("loadSingleAd onAdFailedToLoad $error")
                    if (error.code == IKSdkErrorCode.READY_CURRENT_AD.code)
                        callback?.onAdLoaded(adNetwork, null)
                    else
                        callback?.onAdLoadFail(adNetwork, error)
                }

                override fun onAdLoaded(adNetwork: String, adsResult: IKSdkBaseLoadedAd<T>?) {
                    showLogD("loadSingleAd onAdLoaded")
                    coroutineScope.launchWithSupervisorJob {
                        addItemAds(
                            adsResult
                        )
                    }
                    callback?.onAdLoaded(adNetwork, null)
                }
            })
    }

    suspend fun addItemAds(item: IKSdkBaseLoadedAd<T>?) {
        if (item == null)
            return

        showLogD("add item 396,$adFormatName,$adNetworkName,${item.unitId}")
        mutexListAd.withLock {
            val check = kotlin.runCatching {
                !mListAd.contains(item) && mListAd.find { it.loadedAd == item.loadedAd } == null
            }.getOrNull() ?: true
            runCatching {
                if (check)
                    mListAd.add(item)
            }
        }
    }

    fun createDto(showPriority: Int, loadedAd: T?, idAds: IKAdUnitDto): IKSdkBaseLoadedAd<T> {
        return IKSdkBaseLoadedAd(
            idAds.adUnitId,
            loadedAd = loadedAd,
            adPriority = idAds.adPriority ?: 0,
            showPriority = showPriority
        ).apply {
            this.adNetwork = adNetworkName
            this.adFormat = adFormatName
            kotlin.runCatching {
                this.lastTimeLoaded = System.currentTimeMillis()
            }
            kotlin.runCatching {
                this.uuid = UUID.randomUUID().toString()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun addItemAdsData(item: IKSdkBaseLoadedAd<*>?) {
        if (item == null)
            return
        showLogD("cnvvmmo addItemAdsData item 410,$adFormatName,$adNetworkName,${item.unitId}")
        mutexListAd.withLock {
            val check = kotlin.runCatching {
                !mListAd.contains(item) && mListAd.find { it.loadedAd == item.loadedAd } == null
            }.getOrNull() ?: true
            runCatching {
                if (check)
                    (item as? IKSdkBaseLoadedAd<T>)?.let {
                        mListAd.add(it)
                        showLogD("cnvvmmo addItemAdsData item 410,$adFormatName,$adNetworkName,${mListAd.size}")
                    }
            }
        }
    }

    protected suspend fun removeItemAds(item: IKSdkBaseLoadedAd<T>) {
        item.isRemove = true
        showLogD("add item 423,$adFormatName,$adNetworkName,${item.unitId}")
        mutexListAd.withLock {
            mListAd.removeAll { it == item }
        }
    }

    suspend fun addItemAdBackup(item: IKSdkBaseLoadedAd<T>?) {
        if (item == null)
            return

        item.lastTimeLoaded = System.currentTimeMillis()
        item.adFormat = adFormatName
        item.adNetwork = adNetworkName
        item.des = "backup"
        mutexListAdBackup.withLock {
            runCatching {
                mListBackupAd.add(item)
            }
        }
    }

    protected suspend fun removeItemBackup(item: IKSdkBaseLoadedAd<T>) {
        mutexListAdBackup.withLock {
            mListBackupAd.removeAll { it == item }
        }
    }

    abstract suspend fun loadCoreAd(
        coroutineScope: CoroutineScope,
        idAds: IKAdUnitDto,
        scriptName: String,
        screen: String?,
        showPriority: Int,
        isLoadAndShow: Boolean,
        callback: IKSdkAdCallback<T>
    )

    suspend fun loadCoreAd(
        coroutineScope: CoroutineScope,
        idAds: IKAdUnitDto,
        scriptName: String,
        showPriority: Int,
        isLoadAndShow: Boolean,
        callback: IKSdkAdCallback<T>
    ) {
        loadCoreAd(
            coroutineScope,
            idAds,
            scriptName,
            null,
            showPriority,
            isLoadAndShow = isLoadAndShow,
            callback = callback
        )
    }

    private suspend fun fetchAdParallel(
        coroutineScope: CoroutineScope,
        maxQueue: Int,
        scriptName: String,
        listBidID: List<IKAdUnitDto>,
        showPriority: Int,
        loadAdsListener: IKSdkLoadAdCoreListener
    ) {
        var countCallback = 0
        showLogD("FP start")
        withContext(Dispatchers.Default) {
            val listAd =
                listBidID.filter { it.adPriority != null }.sortedByDescending { it.adPriority }
                    .filter { dto -> mListAd.findAd(mutexListAd) { it.adPriority == dto.adPriority } == null }
            val itemAdSize = listAd.size
            if (listAd.isEmpty()) {
                showLogD("FP empty")
                loadAdsListener.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_SCREEN_ID_AD))
                return@withContext
            }

            val semaphore =
                Semaphore(
                    if (maxQueue <= 0)
                        IKSdkDefConst.MAX_QUE_LOAD
                    else maxQueue
                )

            listAd.forEach { itemAds ->
                semaphore.withPermit {
                    loadCoreAd(coroutineScope, itemAds,
                        scriptName,
                        showPriority,
                        false,
                        object : IKSdkAdCallback<T> {
                            override fun onAdFailedToLoad(adNetwork: String, error: IKAdError) {
                                countCallback++

                                if (countCallback == itemAdSize) {
                                    val otherAdsLoaded = mListAd.find { it.loadedAd != null }
                                    if (otherAdsLoaded != null) {
                                        loadAdsListener.onAdLoaded()
                                    } else loadAdsListener.onAdLoadFail(error)
                                } else {
                                    var isHigherAdLoadSuccess = false
                                    val indexAds =
                                        mListAd.indexOfFirst { it.unitId == itemAds.adUnitId }
                                    loop@ for (i in 0 until indexAds) {
                                        val item = mListAd.getOrNull(i)
                                        if (item?.loadedAd != null) {
                                            isHigherAdLoadSuccess = true
                                            break@loop
                                        }
                                    }
                                    if (isHigherAdLoadSuccess) {
                                        loadAdsListener.onAdLoaded()
                                    }
                                }
                                kotlin.runCatching {
                                    showLogD("FP Fail p=${itemAds.adPriority},${error}")
                                }
                            }

                            override fun onAdLoaded(
                                adNetwork: String,
                                adsResult: IKSdkBaseLoadedAd<T>?
                            ) {
                                coroutineScope.launchWithSupervisorJob {
                                    addItemAds(
                                        adsResult
                                    )
                                }
                                showLogD("FP Loaded p=${itemAds.adPriority}")
                                countCallback++
                                if (countCallback == itemAdSize) {
                                    loadAdsListener.onAdLoaded()
                                } else {
                                    var isHigherAdLoadSuccess = false
                                    val indexAds =
                                        mListAd.indexOfFirst { it.unitId == itemAds.adUnitId }
                                    loop@ for (i in 0 until indexAds) {
                                        val item = mListAd.getOrNull(i)
                                        if (item?.loadedAd != null) {
                                            isHigherAdLoadSuccess = true
                                            break@loop
                                        }
                                    }
                                    if (isHigherAdLoadSuccess) {
                                        loadAdsListener.onAdLoaded()
                                    }
                                }
                            }
                        })
                }
            }
        }
    }

    private suspend fun loadAdAndWait(
        scope: CoroutineScope,
        scriptName: String, itemAds: IKAdUnitDto,
        showPriority: Int
    ): Boolean {
        val loadAdJob = CompletableDeferred<Boolean>()

        loadAdsNext(scope, scriptName, itemAds, showPriority, object : IKSdkLoadAdCoreListener {
            override fun onAdLoaded() {
                loadAdJob.complete(true)
            }

            override fun onAdLoadFail(error: IKAdError) {
                if (error.code == IKSdkErrorCode.READY_CURRENT_AD.code)
                    loadAdJob.complete(true)
                else
                    loadAdJob.complete(false)
            }
        })

        return loadAdJob.await()
    }

    private suspend fun loadAdsNext(
        coroutineScope: CoroutineScope,
        scriptName: String,
        itemAds: IKAdUnitDto,
        showPriority: Int,
        loadCallback: IKSdkLoadAdCoreListener,
    ) {
        loadCoreAd(coroutineScope, itemAds,
            scriptName,
            showPriority,
            false,
            object : IKSdkAdCallback<T> {
                override fun onAdFailedToLoad(adNetwork: String, error: IKAdError) {
                    kotlin.runCatching {
                        showLogD(
                            "loadNext fail p=${itemAds.adPriority},${error}"
                        )
                    }
                    loadCallback.onAdLoadFail(error)
                }

                override fun onAdLoaded(adNetwork: String, adsResult: IKSdkBaseLoadedAd<T>?) {
                    coroutineScope.launchWithSupervisorJob {
                        addItemAds(
                            adsResult
                        )
                    }
                    showLogD("loadNext loaded p=${itemAds.adPriority}")
                    loadCallback.onAdLoaded()
                }

            })

    }

    private suspend fun fetchAdsSequent(
        scope: CoroutineScope,
        scriptName: String, listBidID: List<IKAdUnitDto>,
        showPriority: Int,
        loadAdsListener: IKSdkLoadAdCoreListener
    ) {
        showLogD("FS: start")
        withContext(Dispatchers.Default) {
            val listAd =
                listBidID.filter { it.adPriority != null }.sortedByDescending { it.adPriority }
                    .filter { dto -> mListAd.findAd(mutexListAd) { it.adPriority == dto.adPriority } == null }
            if (listAd.isEmpty()) {
                showLogD("FS: empty")
                loadAdsListener.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_SCREEN_ID_AD))
                return@withContext
            }
            val listDto =
                listAd.iterator()
            while (listDto.hasNext()) {
                val itemAds = listDto.next()
                val adLoaded = loadAdAndWait(scope, scriptName, itemAds, showPriority)

                if (adLoaded) {
                    loadAdsListener.onAdLoaded()
                    return@withContext
                }
            }

            loadAdsListener.onAdLoadFail(IKAdError(IKSdkErrorCode.LOAD_ADS_ERROR))
        }
    }

    open fun loadPreloadAd(
        coroutineScope: CoroutineScope,
        context: Context,
        adData: IKAdapterDto,
        scriptName: String,
        callback: IKSdkLoadAdCoreListener
    ) {
        var callbackNew: IKSdkLoadAdCoreListener? = callback
        coroutineScope.launchWithSupervisorJob {
            if (adData.enablePreload == true) {
                loadAd(coroutineScope, adData, object : IKSdkLoadAdCoreListener {
                    override fun onAdLoaded() {
                        callbackNew?.onAdLoaded()
                        callbackNew = null
                    }

                    override fun onAdLoadFail(error: IKAdError) {
                        callbackNew?.onAdLoadFail(error)
                        callbackNew = null
                    }
                })
            } else {
                if (adData.adData.isNullOrEmpty()) {
                    callbackNew
                        ?.onAdLoadFail(IKAdError(IKSdkErrorCode.NO_SCREEN_ID_AD))
                    callbackNew = null
                    return@launchWithSupervisorJob
                }
                showLogD("loadWFAd s=${adData.adData?.size}")
                val adPreload = withContext(Dispatchers.Default) {
                    val list = adData.adData?.filter { it.label == IKSdkDefConst.AdLabel.PRELOAD }
                    if (list.isNullOrEmpty()) {
                        adData.adData?.maxByOrNull { it.adPriority ?: 0 }
                    } else list.maxByOrNull { it.adPriority ?: 0 }
                }

                if (adPreload != null) {
                    loadSingleAd(
                        coroutineScope,
                        context,
                        adPreload,
                        adData.showPriority ?: 0,
                        IKSdkDefConst.TXT_SCRIPT_LOAD + (adData.adNetwork ?: IKSdkDefConst.UNKNOWN),
                        callback = object :
                            IKSdkAdLoadCoreCallback<IKSdkBaseLoadedAd<T>, IKAdError> {
                            override fun onAdLoadFail(adNetwork: String, error: IKAdError) {
                                callbackNew?.onAdLoadFail(error)
                                callbackNew = null
                            }

                            override fun onAdLoaded(
                                adNetwork: String,
                                adsResult: IKSdkBaseLoadedAd<T>?
                            ) {
                                callbackNew?.onAdLoaded()
                                callbackNew = null
                            }
                        }
                    )
                }
            }
        }
    }

    suspend fun loadBackupAd(
        coroutineScope: CoroutineScope,
        adData: IKAdUnitDto, scriptName: String, showPriority: Int,
        callback: IKSdkLoadAdCoreListener
    ) {
        if (adData.adUnitId.isNullOrBlank()) {
            callback.onAdLoadFail(IKAdError(IKSdkErrorCode.UNIT_AD_NOT_VALID))
            return
        }
        if (mIsBackupAdLoading) {
            callback.onAdLoadFail(IKAdError(IKSdkErrorCode.CURRENT_AD_LOADING))
            showLogD("loadBackupAd IsBackupAdLoading")
            return
        }
        var callbackNew: IKSdkLoadAdCoreListener? = callback
        withContext(Dispatchers.Default) {
            val timeout = when (adFormatName) {
                IKSdkDefConst.AdFormat.BANNER, IKSdkDefConst.AdFormat.BANNER_INLINE ->
                    IKSdkDefConst.TimeOutAd.BANNER

                IKSdkDefConst.AdFormat.NATIVE, IKSdkDefConst.AdFormat.NATIVE_BANNER ->
                    IKSdkDefConst.TimeOutAd.NATIVE

                IKSdkDefConst.AdFormat.OPEN ->
                    IKSdkDefConst.TimeOutAd.OPEN

                IKSdkDefConst.AdFormat.INTER ->
                    IKSdkDefConst.TimeOutAd.INTER

                IKSdkDefConst.AdFormat.REWARD ->
                    IKSdkDefConst.TimeOutAd.REWARD

                else -> IKSdkDefConst.TimeOutAd.INTER
            }

            if (isBackupAdReady(timeout, adData.cacheSize ?: 0)) {
                callbackNew
                    ?.onAdLoadFail(IKAdError(IKSdkErrorCode.READY_CURRENT_AD))
                callbackNew = null
                showLogD("loadBackupAd: exist AD")
                return@withContext
            }
            mIsBackupAdLoading = true
            showLogD("loadBackupAd start")
            loadCoreAd(
                coroutineScope,
                adData,
                scriptName,
                showPriority,
                false,
                object : IKSdkAdCallback<T> {
                    override fun onAdFailedToLoad(adNetwork: String, error: IKAdError) {
                        showLogE("loadBackupAd onAdFailedToLoad $error")
                        callbackNew?.onAdLoadFail(error)
                        mIsBackupAdLoading = false
                        callbackNew = null
                    }

                    override fun onAdLoaded(adNetwork: String, adsResult: IKSdkBaseLoadedAd<T>?) {
                        showLogD("loadBackupAd onAdLoaded")
                        callbackNew?.onAdLoaded()
                        coroutineScope.launchWithSupervisorJob {
                            addItemAdBackup(adsResult)
                        }
                        mIsBackupAdLoading = false
                        callbackNew = null
                    }
                })
        }
    }

    open suspend fun loadSingleAd(
        coroutineScope: CoroutineScope,
        screen: String,
        adData: IKAdUnitDto,
        showPriority: Int,
        scriptName: String,
        viewSize: IKSdkViewSize? = null,
        isLoadAndShow: Boolean,
        callback: IKSdkLoadAdCoreListener? = null
    ) {
        if (adData.adUnitId.isNullOrBlank()) {
            callback?.onAdLoadFail(IKAdError(IKSdkErrorCode.UNIT_AD_NOT_VALID))
            return
        }
        var callbackNew: IKSdkLoadAdCoreListener? = callback
        showLogD("loadSingleAd start")
        loadCoreAd(
            coroutineScope,
            adData,
            scriptName,
            screen,
            showPriority,
            isLoadAndShow,
            object : IKSdkAdCallback<T> {
                override fun onAdFailedToLoad(adNetwork: String, error: IKAdError) {
                    showLogE("loadSingleAd onAdFailedToLoad $error")
                    if (error.code == IKSdkErrorCode.READY_CURRENT_AD.code)
                        callbackNew?.onAdLoaded()
                    else
                        callbackNew?.onAdLoadFail(error)
                    callbackNew = null
                }

                override fun onAdLoaded(adNetwork: String, adsResult: IKSdkBaseLoadedAd<T>?) {
                    showLogD("loadSingleAd onAdLoaded")
                    coroutineScope.launchWithSupervisorJob {
                        addItemAds(
                            adsResult
                        )
                    }
                    callbackNew?.onAdLoaded()
                    callbackNew = null
                }

            })
    }

    open suspend fun loadSingleAdSdk(
        coroutineScope: CoroutineScope,
        showPriority: Int,
        scriptName: String,
        screen: String? = null,
        isLoadAndShow: Boolean,
        unitData: IKAdUnitDto?,
        callback: IKSdkAdLoadCoreCallback<IKSdkBaseLoadedAd<T>, IKAdError>? = null
    ) {
        showLogE("loadSingleAd2 start")
//        val context = WeakReference(IKSdkApplicationProvider.getContext()).get()
        var callbackNew: IKSdkAdLoadCoreCallback<IKSdkBaseLoadedAd<T>, IKAdError>? = callback
//        if (context == null) {
//            callbackNew
//                ?.onAdLoadFail(adNetworkName, IKAdError(IKSdkErrorCode.CONTEXT_NOT_VALID))
//            showLogD("loadSingleAd2 context null")
//            return
//        }

        if (unitData == null) {
            showLogE("loadSingleAd2 onAdFailedToLoad")
            callbackNew
                ?.onAdLoadFail(adNetworkName, IKAdError(IKSdkErrorCode.UNIT_AD_NOT_VALID))
            return
        }

        showLogD("loadSingleAd start")
        loadCoreAd(
            coroutineScope,
            unitData,
            IKSdkDefConst.TXT_SCRIPT_LOAD + adNetworkName,
            screen,
            showPriority,
            true,
            callback = object : IKSdkAdCallback<T> {
                override fun onAdFailedToLoad(adNetwork: String, error: IKAdError) {
                    showLogE("loadSingleAd onAdFailedToLoad $error")
                    callbackNew?.onAdLoadFail(adNetwork, error)
                    callbackNew = null
                }

                override fun onAdLoaded(adNetwork: String, adsResult: IKSdkBaseLoadedAd<T>?) {
                    showLogD("loadSingleAd onAdLoaded")
                    callbackNew?.onAdLoaded(adNetwork, adsResult)
                    callbackNew = null
                }
            })

    }

    open suspend fun isAdReady(targetPriority: Boolean, timeCheck: Int): Boolean {
        return withContext(Dispatchers.Default) {
            mListAd.checkAdExpired(timeCheck, mutexListAd)
            if (targetPriority)
                return@withContext mListAd.maxAdByOrNull(mutexListAd) { it.adPriority } != null

            val adsObject = mListAd.firstAdOrNull(mutexListAd)
            if (adsObject == null) {
                mListBackupAd.checkAdExpired(timeCheck, mutexListAdBackup)
                return@withContext mListBackupAd.maxAdByOrNull(mutexListAdBackup) { it.adPriority } != null
            }

            return@withContext true
        }
    }

    private val getListAdReadyMutex = Mutex()
    open suspend fun getListAdReady(
        targetPriority: Boolean,
        timeCheck: Int
    ): List<IKSdkBaseLoadedAd<T>> {
        showLogD("cnvvmmo getListAdReady 1,$adFormatName,$adNetworkName, ${mListAd.size}")
        val result = withContext(Dispatchers.Default) {
            mListAd.checkAdExpired(timeCheck, mutexListAd)
            if (targetPriority) {
                val result = mListAd.filterAds(mutexListAd) { it.adPriority > 0 }
                if (result.isNotEmpty())
                    return@withContext result
            }

            if (mListAd.getList(mutexListAd).isEmpty()) {
                mListBackupAd.checkAdExpired(timeCheck, mutexListAdBackup)
                return@withContext mListBackupAd.getList(mutexListAdBackup).ifEmpty { listOf() }
            }
            showLogD("cnvvmmo getListAdReady item 2,$adFormatName,$adNetworkName,${mListAd.size}")
            return@withContext mListAd.getList(mutexListAd)
        }
        showLogD("cnvvmmo getListAdReady item 3,$adFormatName,$adNetworkName,${mListAd.size}")
        return result
    }

    suspend fun isAdReady(adPriority: Int, timeCheck: Int): Boolean {
        return withContext(Dispatchers.Default) {
            mListAd.checkAdExpired(timeCheck, mutexListAd)
            return@withContext mListAd.findAd(mutexListAd) {
                it.adPriority >= adPriority
            } != null
        }
    }

    suspend fun checkLoadSameAd(adPriority: Int, cacheSize: Int, timeCheck: Int): Boolean {
        return withContext(Dispatchers.Default) {
            mListAd.checkAdExpired(timeCheck, mutexListAd)
            val currentSize = mListAd.filterAds(mutexListAd) {
                it.adPriority >= adPriority
            }.size
            val cacheS = if (cacheSize <= 0) SAME_SIZE else cacheSize
            return@withContext currentSize >= cacheS
        }
    }

    open suspend fun isBackupAdReady(timeCheck: Int, cacheSize: Int): Boolean {
        return withContext(Dispatchers.Default) {
            mListBackupAd.checkAdExpired(timeCheck, mutexListAdBackup)
            val currentSize = mListBackupAd.filterAds(mutexListAdBackup) {
                it.adPriority >= 0
            }.size
            val cacheS = if (cacheSize <= 0) SAME_SIZE_BACKUP else cacheSize
            return@withContext currentSize >= cacheS
        }
    }

    open suspend fun getReadyAd(timeCheck: Int): IKSdkBaseLoadedAd<T>? {
        return withContext(Dispatchers.Default) {
            mListAd.checkAdExpired(timeCheck, mutexListAd)
            val adsObject = mListAd.maxAdByOrNull(mutexListAd) { it.adPriority }
            if (adsObject == null) {
                mListAd.withList(mutexListAd) {
                    it.clear()
                }

                showLogD("GF,no ads to next show")
                mListBackupAd.checkAdExpired(timeCheck, mutexListAdBackup)
                val objetAd = mListBackupAd.maxAdByOrNull(mutexListAdBackup) { it.adPriority }
                if (objetAd != null) {
                    removeItemBackup(objetAd)
                    return@withContext objetAd
                }
                return@withContext null
            }
            removeItemAds(adsObject)
            showLogD("GF,store p=${adsObject.adPriority}, currentSize=${mListAd.size}")
            return@withContext adsObject
        }
    }

    open fun isAdLoading(): Boolean {
        return mIsAdLoading
    }


    protected fun showLogD(message: String) {
        runCatching {
            IKLogs.d(logTag) { "$adNetworkName $message" }
        }
    }

    protected fun showLogE(message: String) {
        runCatching {
            IKLogs.e(logTag) { "$adNetworkName $message" }
        }
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Int, timeLoad: Long): Boolean {
        val dateDifference = Date().time - timeLoad
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }


    fun trackAdLoaded(
        startLoadTime: Long,
        priority: Int,
        adUnit: String,
        scriptName: String,
        adUUID: String = ""
    ) {
        IKSdkTrackingHelper.trackingSdkLoadAd(
            startLoadTime,
            priority,
            adFormatName,
            adUnit,
            adNetworkName,
            IKSdkDefConst.AdStatus.LOADED,
            adUUID,
            Pair(IKTrackingConst.ParamName.SCRIPT_NAME, scriptName)
        )
    }


    fun trackAdLoaded(
        startLoadTime: Long,
        priority: Int,
        adUnit: String,
        scriptName: String,
        adUUID: String = "",
        isTimeout: Boolean = false
    ) {
        IKSdkTrackingHelper.trackingSdkLoadAd(
            startLoadTime,
            priority,
            adFormatName,
            adUnit,
            adNetworkName,
            IKSdkDefConst.AdStatus.LOADED,
            adUUID,
            Pair(IKTrackingConst.ParamName.SCRIPT_NAME, scriptName),
            Pair(IKTrackingConst.ParamName.IS_TIME_OUT, isTimeout.toString()),
        )
    }

    fun trackAdLoadFail(
        startLoadTime: Long,
        priority: Int,
        adUnit: String,
        scriptName: String,
        message: String,
        errorCode: String,
    ) {
        IKSdkTrackingHelper.trackingSdkLoadAd(
            startLoadTime,
            priority,
            adFormatName,
            adUnit,
            adNetworkName,
            IKSdkDefConst.AdStatus.LOAD_FAIL,
            "",
            Pair(IKTrackingConst.ParamName.MESSAGE, message),
            Pair(IKTrackingConst.ParamName.ERROR_CODE, errorCode),
            Pair(IKTrackingConst.ParamName.SCRIPT_NAME, scriptName)
        )
    }

    fun trackAdLoadFail(
        startLoadTime: Long,
        priority: Int,
        adUnit: String,
        scriptName: String,
        message: String,
        errorCode: String,
        isTimeout: Boolean = false
    ) {
        IKSdkTrackingHelper.trackingSdkLoadAd(
            startLoadTime,
            priority,
            adFormatName,
            adUnit,
            adNetworkName,
            IKSdkDefConst.AdStatus.LOAD_FAIL,
            "",
            Pair(IKTrackingConst.ParamName.MESSAGE, message),
            Pair(IKTrackingConst.ParamName.ERROR_CODE, errorCode),
            Pair(IKTrackingConst.ParamName.SCRIPT_NAME, scriptName),
            Pair(IKTrackingConst.ParamName.IS_TIME_OUT, isTimeout.toString()),
        )
    }

    protected suspend fun MutableList<IKSdkBaseLoadedAd<T>>.checkAdExpired(
        timeCheck: Int,
        mutex: Mutex
    ) {
        withContext(Dispatchers.Default) {
            if (timeCheck <= 0)
                this@checkAdExpired.filterAds(mutex) { it.loadedAd == null || it.isRemove }.let {
                    runCatching {
                        if (it.isNotEmpty())
                            this@checkAdExpired.removeAllAd(it.toSet(), mutex)
                    }
                }
            else
                this@checkAdExpired.filterAds(mutex) {
                    it.loadedAd == null || it.isRemove ||
                            !wasLoadTimeLessThanNHoursAgo(
                                timeCheck,
                                it.lastTimeLoaded
                            )
                }.let {
                    runCatching {
                        if (it.isNotEmpty())
                            this@checkAdExpired.removeAllAd(it.toSet(), mutex)
                    }
                }
            withList(mutex) {
                kotlin.runCatching {
                    it.sortByDescending { it.adPriority }
                }
            }

        }
    }

    private suspend fun MutableList<IKSdkBaseLoadedAd<T>>.removeAllAd(
        data: Set<IKSdkBaseLoadedAd<T>>,
        mutex: Mutex
    ) {
        mutex.withLock {
            removeAll(data)
        }
    }

    suspend fun clearAllCache() {
        mutexListAd.withLock {
            mListAd.clear()
        }
        mutexListAdBackup.withLock {
            mListBackupAd.clear()
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun removeAdAny(ad: IKSdkBaseLoadedAd<*>) {
        ad.isRemove = true
        IKLogs.d("cnnnnad") { "3: start remove" }
        kotlin.runCatching {
            (ad as? IKSdkBaseLoadedAd<T>)?.let { removeItemAds(it) }
        }
        kotlin.runCatching {
            (ad as? IKSdkBaseLoadedAd<T>)?.let { removeItemBackup(it) }
        }
        IKLogs.d("cnnnnad") { "3: end remove" }
    }

    suspend fun forEachAd(action: (IKSdkBaseLoadedAd<T>) -> Unit) {
        mutexListAd.withLock {
            mListAd.forEach(action)
        }
    }

    private suspend fun MutableList<IKSdkBaseLoadedAd<T>>.filterAds(
        mutex: Mutex,
        predicate: (IKSdkBaseLoadedAd<T>) -> Boolean
    ): List<IKSdkBaseLoadedAd<T>> {
        return mutex.withLock {
            runCatching {
                this.filter(predicate)
            }.getOrNull() ?: listOf()
        }
    }

    private suspend fun MutableList<IKSdkBaseLoadedAd<T>>.findAd(
        mutex: Mutex,
        predicate: (IKSdkBaseLoadedAd<T>) -> Boolean
    ): IKSdkBaseLoadedAd<T>? {
        return mutex.withLock {
            find(predicate)
        }
    }

    suspend fun MutableList<IKSdkBaseLoadedAd<T>>.firstAdOrNull(mutex: Mutex): IKSdkBaseLoadedAd<T>? {
        return mutex.withLock {
            firstOrNull()
        }
    }

    protected suspend fun MutableList<IKSdkBaseLoadedAd<T>>.maxAdByOrNull(
        mutex: Mutex,
        selector: (IKSdkBaseLoadedAd<T>) -> Int
    ): IKSdkBaseLoadedAd<T>? {
        return mutex.withLock {
            maxByOrNull(selector)
        }
    }

    protected suspend fun <R> MutableList<IKSdkBaseLoadedAd<T>>.withList(
        mutex: Mutex,
        action: (MutableList<IKSdkBaseLoadedAd<T>>) -> R
    ): R {
        return mutex.withLock {
            action(this)
        }
    }

    private suspend fun MutableList<IKSdkBaseLoadedAd<T>>.getList(mutex: Mutex): MutableList<IKSdkBaseLoadedAd<T>> {
        return mutex.withLock {
            this
        }
    }

    suspend fun preLoadAd(
        idAds: IKAdUnitDto,
        callback: IKSdkAdCallback<T>
    ): Boolean {
        showLogD("loadCoreAd pre start")
        val unitId = idAds.adUnitId?.trim()
        if (unitId.isNullOrBlank()) {
            callback.onAdFailedToLoad(
                adNetworkName,
                IKAdError(IKSdkErrorCode.UNIT_AD_NOT_VALID)
            )
            showLogD("loadCoreAd unit empty")
            return false
        }
        if (checkLoadSameAd(
                idAds.adPriority ?: 0,
                idAds.cacheSize ?: 0,
                IKSdkDefConst.TimeOutAd.INTER
            )
        ) {
            callback.onAdFailedToLoad(
                adNetworkName,
                IKAdError(IKSdkErrorCode.READY_CURRENT_AD)
            )
            showLogD("loadCoreAd an ad ready")
            return false
        }
        return true
    }
}