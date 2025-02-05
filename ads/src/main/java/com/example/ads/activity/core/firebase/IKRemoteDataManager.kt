package com.example.ads.activity.core.firebase

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.internal.ConfigContainer
import com.google.firebase.remoteconfig.internal.DefaultsXmlParser
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.ads.activity.core.CoreController.Companion.BLOCK_TIME_FORGE_UPDATE
import com.example.ads.activity.core.IKDataCoreManager.setOtherConfig
import com.example.ads.activity.core.IKDataCoreManager.updateAppConfig
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.data.db.IKDataRepository
import com.example.ads.activity.data.dto.pub.IKAdFormat
import com.example.ads.activity.data.dto.pub.IKError
import com.example.ads.activity.data.dto.pub.IKRemoteConfigValue
import com.example.ads.activity.data.dto.sdk.data.CacheConfigDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkAudioIconDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkBackupAdDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkBannerCollapseCustomDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkBannerCollapseDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkBannerDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkBannerInlineDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkCustomNCLDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkDataOpLocalDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkFirstAdDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkInterDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkNativeDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkNativeFullScreenDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkOpenDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdInterDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdOpenDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdRewardDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdWidgetDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkRewardDto
import com.example.ads.activity.data.dto.sdk.data.UserBillingDetail
import com.example.ads.activity.data.local.IKSdkDataStore
import com.example.ads.activity.data.local.IKSdkDataStoreConst
import com.example.ads.activity.listener.pub.IKNewRemoteConfigCallback
import com.example.ads.activity.listener.sdk.IKSdkRemoteConfigCallback
import com.example.ads.activity.utils.IKLogs
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import com.example.ads.activity.utils.IKSdkExt.log
import com.example.ads.activity.utils.IKSdkUtilsCore
import com.example.ads.activity.utils.IKTrackingConst
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.util.concurrent.ConcurrentHashMap

object IKRemoteDataManager {
    private const val TAG = "IKRemoteDataManager_"

    var firebaseRemote: IKFirebaseRemote = IKSdkFirebaseModule.provideIKFirebaseRemote()
    private val mRemoteConfig: FirebaseRemoteConfig? by lazy {
        firebaseRemote.getRemoteConfig()
    }

    const val BLOCK_TIME_SHOW_FULL_ADS = 500L
    const val TIME_UPDATE_CONFIG = 60000L
    var mRemoteConfigData: ConcurrentHashMap<String, IKRemoteConfigValue> = ConcurrentHashMap()
        private set
    var lastTimeUpdateConfig = 0L
        private set
    private var mLastTimeGetConfig = 0L
    private var dataHasCache = false

    private var onFetchConfig = false
    var configCallback: IKSdkRemoteConfigCallback? = null

    fun setLastTimeUpdateConfig(value: Long) {
        lastTimeUpdateConfig = value
    }

    private val ikDataRepository: IKDataRepository? by lazy {
        IKDataRepository.getInstance()
    }

    private val mJob = SupervisorJob()
    private val mUiScope = CoroutineScope(Dispatchers.Main + mJob)
    private val mutexRemoteConfigData = Mutex()

    private val sdkCallback = object : IKFirebaseRemoteConfigListener {
        override fun onComplete(value: Task<Boolean>) {
            if (value.isSuccessful) {
                showLogSdk("FirebaseRemoteCallback") { "onComplete" }
                mUiScope.launchWithSupervisorJob {
                    withContext(Dispatchers.Default) {
                        mutexRemoteConfigData.withLock {
                            kotlin.runCatching {
                                mRemoteConfig?.all?.forEach {
                                    mRemoteConfigData[it.key] =
                                        IKRemoteConfigValue(it.value)
                                }
                            }
                        }
                        kotlin.runCatching {
                            IKSdkDataStore.putString(
                                IKSdkDataStoreConst.REMOTE_CONFIG_DATA,
                                Gson().toJson(getRemoteConfigData())
                            )
                        }
                    }
                    withContext(Dispatchers.Main) {
                        configCallback?.onSuccess()
                    }
                    updateAppConfig =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.UPDATE_APP_CONFIG)
                            ?: IKSdkDefConst.EMPTY
                    showLogSdk("FirebaseRemoteCallback") { "onComplete onFetchConfig=$onFetchConfig" }
                    if (onFetchConfig) {
                        return@launchWithSupervisorJob
                    }
                    onFetchConfig = true
                    getDataRemoteConfig()
                    onFetchConfig = false
                }
            } else {
                showLogSdk("FirebaseRemoteCallback") { "on Fail" }
                if (onFetchConfig)
                    configCallback?.onFail(IKError(value.exception))
            }
        }

        override fun onError(error: java.lang.Exception) {
            showLogSdk("FirebaseRemoteCallback") { "onError $error" }
            configCallback?.onFail(IKError(error))
        }
    }

    private suspend fun getDefaultXml(context: Context) {
        showLogSdk("getDefaultXml") { "start run" }
        getDefaultXmlCustom()
        val configMap = withContext(Dispatchers.IO) {
            val xmlDefaults =
                kotlin.runCatching {
                    DefaultsXmlParser.getDefaultsFromXml(
                        context,
                        IKDataSourceHelper.getSdkConfigDataFile()
                    )
                }.getOrElse {
                    runCatching {
                        IKDataSourceHelper.getSdkConfigData(context)
                    }.getOrNull()
                }
            val defaultConfigs: ConfigContainer? = try {
                ConfigContainer.newBuilder().replaceConfigsWith(xmlDefaults).build()
            } catch (e: JSONException) {
                null
            }
            defaultConfigs?.configs
        }
        withContext(Dispatchers.Default) {
            val sdkOpen = async {
                kotlin.runCatching {
                    val value =
                        configMap?.getString(IKSdkDefConst.Config.SDK_DATA_OPEN)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkOpenDto::class.java)
                    showLogSdk("getDefaultXml") { "sdkOpen=${(result != null).log()}" }
                    if (result != null)
                        ikDataRepository?.insertSDKOpen(result)
                }
            }

            val sdkInter = async {
                kotlin.runCatching {
                    val value = configMap?.getString(IKSdkDefConst.Config.SDK_DATA_INTER)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkInterDto::class.java)
                    showLogSdk("getDefaultXml") { "sdkInter=${(result != null).log()}" }
                    if (result != null)
                        ikDataRepository?.insertSDKInter(result)
                }
            }
            val sdkReward = async {
                kotlin.runCatching {
                    val value = configMap?.getString(IKSdkDefConst.Config.SDK_DATA_REWARD)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkRewardDto::class.java)
                    showLogSdk("getDefaultXml") { "sdkReward=${(result != null).log()}" }
                    if (result != null)
                        ikDataRepository?.insertSDKReward(result)
                }
            }

            val sdkNative = async {
                kotlin.runCatching {
                    val value = configMap?.getString(IKSdkDefConst.Config.SDK_DATA_NATIVE)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkNativeDto::class.java)
                    showLogSdk("getDefaultXml") { "sdkNative=${(result != null).log()}" }
                    if (result != null)
                        ikDataRepository?.insertSDKNative(result)
                }
            }

            val sdkBanner = async {
                kotlin.runCatching {
                    val value = configMap?.getString(IKSdkDefConst.Config.SDK_DATA_BANNER)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkBannerDto::class.java)
                    showLogSdk("getDefaultXml") { "sdkBanner=${(result != null).log()}" }
                    if (result != null)
                        ikDataRepository?.insertSDKBanner(result)
                }
            }
            val getOther = async {
                kotlin.runCatching {
                    val value = configMap?.getString(IKSdkDefConst.Config.BACK_UP_AD_CONFIG)

                    val result = try {
                        SDKDataHolder.getObjectSdk(value, IKSdkBackupAdDto::class.java)
                    } catch (e: Exception) {
                        null
                    }
                    showLogSdk("getDefaultXml") { "getOther=${(result != null).log()}" }
                    if (result != null) {
                        ikDataRepository?.insertBackup(result)
                    }
                }
            }
            val sdkFirstAd = async {
                kotlin.runCatching {
                    val value = configMap?.getString(IKSdkDefConst.Config.FIRST_AD_CONFIG)
                    val result = SDKDataHolder.getObject(value, IKSdkFirstAdDto::class.java)
                    showLogSdk("getDefaultXml") { "sdkFirstAd=${(result != null).log()}" }
                    if (result != null) {
                        val interSplash =
                            IKSdkUtilsCore.checkInterSplashCountryCode(IKSdkApplicationProvider.getContext())
                        if (interSplash)
                            result.adsFormat = IKAdFormat.INTER.value
                        ikDataRepository?.insertSDKFirstAd(result)
                    }
                }
            }
            val configInter = async {
                kotlin.runCatching {
                    val userProperty =
                        IKSdkDataStore.getString(IKSdkDefConst.Config.SDK_USER_PROPERTY_TYPE, "")
                    val keyConfig = IKSdkDefConst.Config.CONFIG_DATA_INTER
                    if (userProperty.isNotBlank()) {
                        val value = configMap?.getString(keyConfig + "_$userProperty")
                        val result = kotlin.runCatching {
                            SDKDataHolder.getObject(
                                value,
                                IKSdkProdInterDto::class.java
                            )
                        }.getOrNull()
                        showLogSdk("getDefaultXml") { "configInter and _$userProperty =${(!result?.data.isNullOrEmpty()).log()}" }
                        if (result != null && !result.data.isNullOrEmpty()) {
                            ikDataRepository?.insertConfigInter(result)
                            return@async
                        }
                    }
                    val value = configMap?.getString(keyConfig)
                    val result = SDKDataHolder.getObject(
                        value,
                        IKSdkProdInterDto::class.java
                    )
                    showLogSdk("getDefaultXml") { "configInter=${(!result?.data.isNullOrEmpty()).log()}" }
                    if (!result?.data.isNullOrEmpty())
                        result?.let {
                            ikDataRepository?.insertConfigInter(it)
                        }
                }
            }

            val configReward = async {
                kotlin.runCatching {
                    val value = configMap?.getString(IKSdkDefConst.Config.CONFIG_DATA_REWARD)
                    val result = SDKDataHolder.getObject(
                        value,
                        IKSdkProdRewardDto::class.java
                    )
                    showLogSdk("getDefaultXml") { "configReward=${(!result?.data.isNullOrEmpty()).log()}" }
                    if (!result?.data.isNullOrEmpty())
                        result?.let {
                            ikDataRepository?.insertConfigReward(it)
                        }
                }
            }

            val configOpen = async {
                kotlin.runCatching {
                    val value = configMap?.getString(IKSdkDefConst.Config.CONFIG_DATA_OPEN)
                    val result = SDKDataHolder.getObject(
                        value,
                        IKSdkProdOpenDto::class.java
                    )
                    showLogSdk("getDefaultXml") { "configOpen=${(!result?.data.isNullOrEmpty()).log()}" }
                    if (!result?.data.isNullOrEmpty())
                        result?.let {
                            ikDataRepository?.insertConfigOpen(it)
                        }
                }
            }

            val configWidget = async {
                kotlin.runCatching {
                    val value = configMap?.getString(IKSdkDefConst.Config.CONFIG_DATA_WIDGET)
                    val result = SDKDataHolder.getObject(
                        value,
                        IKSdkProdWidgetDto::class.java
                    )
                    showLogSdk("getDefaultXml") { "configWidget=${(!result?.data.isNullOrEmpty()).log()}" }
                    if (!result?.data.isNullOrEmpty())
                        result?.let {
                            ikDataRepository?.insertConfigWidget(it)
                        }
                }
            }

            val getUserBilling = async(Dispatchers.Default) {
                kotlin.runCatching {
                    val userBillingConfig =
                        configMap?.getString(IKSdkDefConst.Config.PURCHASE_USER_IGNORE)
                            ?: IKSdkDefConst.EMPTY
                    val listUser = try {
                        Gson().fromJson<ArrayList<UserBillingDetail>>(
                            userBillingConfig,
                            object : TypeToken<ArrayList<UserBillingDetail>>() {}.type
                        ) ?: arrayListOf()
                    } catch (e: Exception) {
                        arrayListOf()
                    }
                    showLogSdk("getDefaultXml") { "getUserBilling=${listUser.isNotEmpty().log()}" }
                    if (listUser.isNotEmpty()) ikDataRepository
                        ?.insertAllUserBilling(listUser)

                }
            }
            val configNCL = async {
                kotlin.runCatching {
                    val value = configMap?.getString(IKSdkDefConst.Config.SDK_CUSTOM_NCL)
                    val result = SDKDataHolder.getObject(
                        value,
                        IKSdkCustomNCLDto::class.java
                    )
                    showLogSdk("getDefaultXml") { "configNCL=${(!result?.data.isNullOrEmpty()).log()}" }
                    if (!result?.data.isNullOrEmpty())
                        result?.let {
                            ikDataRepository?.insertConfigNCL(it)
                        }
                }
            }

            kotlin.runCatching {
                val value =
                    configMap?.getBoolean(IKSdkDefConst.Config.ENABLE_LOADING_NCL) ?: false
                IKSdkDataStore.putBoolean(IKSdkDataStoreConst.ENABLE_LOADING_NCL, value)
            }
            val getOtherConfig = async(Dispatchers.Default) {
                kotlin.runCatching {
                    val otherConfigData =
                        configMap?.getString(IKSdkDefConst.Config.OTHER_CONFIG_DATA)
                            ?: IKSdkDefConst.EMPTY
                    IKLogs.d(TAG) {
                        "getDefaultXml_ otherConfigData=${
                            otherConfigData.isNotEmpty().log()
                        }"
                    }
                    kotlin.runCatching {
                        setOtherConfig(
                            Gson().fromJson<HashMap<String, Any>>(
                                otherConfigData,
                                HashMap::class.java
                            )
                        )

                        IKSdkDataStore.putString(
                            IKSdkDataStoreConst.KEY_OTHER_CONFIG_DATA,
                            otherConfigData
                        )
                    }
                }
            }


            val getCache = async(Dispatchers.Default) {
                kotlin.runCatching {
                    val cacheConfig =
                        configMap?.getString(IKSdkDefConst.Config.CACHE_CONFIG)
                            ?: IKSdkDefConst.EMPTY
                    showLogSdk("getDefaultXml") { "getCache=${cacheConfig.isNotEmpty().log()}" }
                    if (cacheConfig.isNotBlank()) {
                        IKSdkDataStore.putString(IKSdkDataStoreConst.CACHE_ADS_DTO, cacheConfig)
                        val ctTime = System.currentTimeMillis()
                        IKSdkDataStore.putLong(IKSdkDataStoreConst.CACHE_ADS_TIME, ctTime)
                    }
                }
            }

            kotlin.runCatching {
                val value =
                    configMap?.getString(IKSdkDefConst.Config.AWS_MEDIATION_CONFIG)
                        ?: IKSdkDefConst.EMPTY
                showLogSdk("getDefaultXml") { "configMap=${value.isNotEmpty().log()}" }
                if (value.isNotBlank())
                    IKSdkDataStore.putString(IKSdkDataStoreConst.AWS_MEDIATION_CONFIG, value)
            }
            val sdkBannerInline = async {
                kotlin.runCatching {
                    val value = configMap?.getString(IKSdkDefConst.Config.SDK_DATA_BANNER_INLINE)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkBannerInlineDto::class.java)
                    showLogSdk("getDefaultXml") { "sdkBannerInline=${(result != null).log()}" }
                    if (result != null)
                        ikDataRepository?.insertSDKBannerInline(result)
                }
            }

            val sdkBannerCl = async {
                kotlin.runCatching {
                    val value = configMap?.getString(IKSdkDefConst.Config.SDK_DATA_BANNER_COLLAPSE)
                    val result =
                        SDKDataHolder.getObjectSdk(value, IKSdkBannerCollapseDto::class.java)
                    showLogSdk("getDefaultXml") { "sdkBannerCl=${(result != null).log()}" }
                    if (result != null)
                        ikDataRepository?.insertSDKBannerCollapse(result)
                }
            }

            val sdkNativeFs = async {
                kotlin.runCatching {
                    val value =
                        configMap?.getString(IKSdkDefConst.Config.SDK_DATA_NATIVE_FULL_SCREEN)
                    val result =
                        SDKDataHolder.getObjectSdk(value, IKSdkNativeFullScreenDto::class.java)
                    showLogSdk("getDefaultXml") { "sdkNativeFs=${(result != null).log()}" }
                    if (result != null)
                        ikDataRepository?.insertSDKNativeFullScreen(result)
                }
            }
            val sdkAudioIcon = async {
                kotlin.runCatching {
                    val value =
                        configMap?.getString(IKSdkDefConst.Config.SDK_DATA_AUDIO_ICON)
                    val result =
                        SDKDataHolder.getObjectSdk(value, IKSdkAudioIconDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKAudioIcon(result)
                }
            }

            val sdkBannerClCustom = async {
                kotlin.runCatching {
                    val value =
                        configMap?.getString(IKSdkDefConst.Config.SDK_DATA_BANNER_COLLAPSE_CUSTOM)
                    val result =
                        SDKDataHolder.getObjectSdk(value, IKSdkBannerCollapseCustomDto::class.java)
                    showLogSdk("getDefaultXml") { "sdkBannerCl=${(result != null).log()}" }
                    if (result != null)
                        ikDataRepository?.insertSDKBannerCollapseCustom(result)
                }
            }
            getOther.await()
            getUserBilling.await()
            getOtherConfig.await()
            getCache.await()
            sdkOpen.await()
            sdkInter.await()
            sdkReward.await()
            sdkNative.await()
            sdkBanner.await()
            sdkFirstAd.await()
            configInter.await()
            configOpen.await()
            configReward.await()
            configWidget.await()
            configNCL.await()
            sdkBannerInline.await()
//            sdkMrec.await()
            sdkBannerCl.await()
            sdkNativeFs.await()
//            sdkAudioIcon.await()
            sdkBannerClCustom.await()
            showLogSdk("getDefaultXml") { "on dome" }
        }
    }

    private suspend fun getDefaultXmlCustom() {
        showLogSdk("getDefaultXmlCustom") { "start" }
        withContext(Dispatchers.Default) {
            if (IKDataSourceHelper.getDataSdkLocal().trim() == IKSdkDefConst.STRING_EMPTY) {
                showLogSdk("getDefaultXmlCustom") { "deleteAllDefaultOpen" }
                ikDataRepository?.deleteAllDefaultOpen()
            } else {
                var listOpenDefaultDto = ArrayList<IKSdkDataOpLocalDto>()
                kotlin.runCatching {
                    listOpenDefaultDto =
                        SDKDataHolder.getObjectSdk<ArrayList<IKSdkDataOpLocalDto>>(
                            IKDataSourceHelper.getDataSdkLocal().trim(),
                            object : TypeToken<ArrayList<IKSdkDataOpLocalDto>>() {}.type
                        ) ?: arrayListOf()
                }
                showLogSdk("getDefaultXmlCustom") {
                    "listOpenDefaultDto ${
                        listOpenDefaultDto.isNotEmpty().log()
                    }"
                }
                if (listOpenDefaultDto.isNotEmpty()) {
                    ikDataRepository?.insertAllDefaultOpen(listOpenDefaultDto)
                    IKSdkDataStore.putLong(
                        IKSdkDataStoreConst.KEY_TIME_CALL_OPEN_DEFAULT,
                        System.currentTimeMillis()
                    )
                } else {
                }
            }
        }
    }

    private suspend fun getDefaultXmlCustomCache() {
        showLogSdk("getDefaultXmlCustomCache") { "start" }
        withContext(Dispatchers.Default) {
            var listOpenDefaultDto = ArrayList<IKSdkDataOpLocalDto>()
            kotlin.runCatching {
                listOpenDefaultDto =
                    SDKDataHolder.getObjectSdk<ArrayList<IKSdkDataOpLocalDto>>(
                        IKDataSourceHelper.getDataSdkLocal().trim(),
                        object : TypeToken<ArrayList<IKSdkDataOpLocalDto>>() {}.type
                    ) ?: arrayListOf()
            }
            showLogSdk("getDefaultXmlCustomCache") {
                "listOpenDefaultDto ${
                    listOpenDefaultDto.isNotEmpty().log()
                }"
            }
            if (listOpenDefaultDto.isNotEmpty()) {
                ikDataRepository?.insertAllDefaultOpenCache(listOpenDefaultDto)
            }
        }
    }

    fun getDataRemoteConfig(callbackDone: (() -> Unit)? = null) {
        showLogSdk("getDataRemoteConfig") { "start" }
        mUiScope.launchWithSupervisorJob(Dispatchers.Default) {

            kotlin.runCatching {
                updateAppConfig = mRemoteConfig?.getString(IKSdkDefConst.Config.UPDATE_APP_CONFIG)
                    ?: IKSdkDefConst.EMPTY
            }
            val getCmpRequest = async(Dispatchers.Default) {
                kotlin.runCatching {
                    val value =
                        mRemoteConfig?.getBoolean(IKSdkDefConst.Config.CMP_CONFIG_REQUEST_ENABLE)
                            ?: false
                    IKSdkDataStore.putBoolean(IKSdkDataStoreConst.CMP_CONFIG_REQUEST_ENABLE, value)
                }
            }
            val current = System.currentTimeMillis()
            if (current - mLastTimeGetConfig < TIME_UPDATE_CONFIG / 2) {
                showLogSdk("getDataRemoteConfig") { "LastTimeGetConfig block" }
                delay(500)
                callbackDone?.invoke()
                return@launchWithSupervisorJob
            }

            mLastTimeGetConfig = System.currentTimeMillis()

            val sdkOpen = async {
                kotlin.runCatching {
                    val value =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_OPEN)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkOpenDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKOpen(result)

                }
            }
            val allRemoteConfig = async(Dispatchers.Default) {
                kotlin.runCatching {
                    mRemoteConfig?.all?.forEach {
                        getRemoteConfigData()[it.key] = IKRemoteConfigValue(it.value)
                    }
                }
                kotlin.runCatching {
                    IKSdkDataStore.putString(
                        IKSdkDataStoreConst.REMOTE_CONFIG_DATA,
                        Gson().toJson(getRemoteConfigData())
                    )
                }
            }

            val sdkInter = async {
                kotlin.runCatching {
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_INTER)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkInterDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKInter(result)
                }
            }
            val sdkReward = async {
                kotlin.runCatching {
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_REWARD)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkRewardDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKReward(result)

                }
            }

            val sdkNative = async {
                kotlin.runCatching {
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_NATIVE)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkNativeDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKNative(result)

                }
            }

            val sdkBanner = async {
                kotlin.runCatching {
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_BANNER)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkBannerDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKBanner(result)

                }
            }
            val getOther = async {
                kotlin.runCatching {
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.BACK_UP_AD_CONFIG)

                    val result = try {
                        SDKDataHolder.getObjectSdk(value, IKSdkBackupAdDto::class.java)
                    } catch (e: Exception) {
                        null
                    }
                    if (result != null) {
                        ikDataRepository?.insertBackup(result)
                    }
                }
            }
            val sdkFirstAd = async {
                kotlin.runCatching {
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.FIRST_AD_CONFIG)
                    val result = SDKDataHolder.getObject(value, IKSdkFirstAdDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKFirstAd(result)
                }
            }
            val configInter = async {
                kotlin.runCatching {
                    val userProperty =
                        IKSdkDataStore.getString(IKSdkDefConst.Config.SDK_USER_PROPERTY_TYPE, "")
                    val keyConfig = IKSdkDefConst.Config.CONFIG_DATA_INTER
                    if (userProperty.isNotBlank()) {
                        val value = mRemoteConfig?.getString(keyConfig + "_$userProperty")
                        val result = kotlin.runCatching {
                            SDKDataHolder.getObject(
                                value,
                                IKSdkProdInterDto::class.java
                            )
                        }.getOrNull()
                        showLogSdk("getDataRemoteConfig") { "configInter and _$userProperty =${(!result?.data.isNullOrEmpty()).log()}" }
                        if (result != null && !result.data.isNullOrEmpty()) {
                            ikDataRepository?.insertConfigInter(result)
                            return@async
                        }
                    }
                    val value = mRemoteConfig?.getString(keyConfig)
                    val result = SDKDataHolder.getObject(
                        value,
                        IKSdkProdInterDto::class.java
                    )
                    showLogSdk("getDataRemoteConfig") { "configInter=${(!result?.data.isNullOrEmpty()).log()}" }
                    if (!result?.data.isNullOrEmpty())
                        result?.let {
                            ikDataRepository?.insertConfigInter(it)
                        }
                }
            }

            val configReward = async {
                kotlin.runCatching {
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.CONFIG_DATA_REWARD)
                    val result = SDKDataHolder.getObject(
                        value,
                        IKSdkProdRewardDto::class.java
                    )
                    if (!result?.data.isNullOrEmpty())
                        result?.let {
                            ikDataRepository?.insertConfigReward(it)
                        }
                }
            }

            val configOpen = async {
                kotlin.runCatching {
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.CONFIG_DATA_OPEN)
                    val result = SDKDataHolder.getObject(
                        value,
                        IKSdkProdOpenDto::class.java
                    )
                    if (!result?.data.isNullOrEmpty())
                        result?.let {
                            ikDataRepository?.insertConfigOpen(it)
                        }
                }
            }

            val configWidget = async {
                kotlin.runCatching {
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.CONFIG_DATA_WIDGET)
                    val result = SDKDataHolder.getObject(
                        value,
                        IKSdkProdWidgetDto::class.java
                    )
                    if (!result?.data.isNullOrEmpty())
                        result?.let {
                            ikDataRepository?.insertConfigWidget(it)
                        }
                }
            }

            val getUserBilling = async(Dispatchers.Default) {
                kotlin.runCatching {
                    val userBillingConfig =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.PURCHASE_USER_IGNORE)
                            ?: IKSdkDefConst.EMPTY
                    val listUser = try {
                        Gson().fromJson<java.util.ArrayList<UserBillingDetail>>(
                            userBillingConfig,
                            object : TypeToken<java.util.ArrayList<UserBillingDetail>>() {}.type
                        ) ?: arrayListOf()
                    } catch (e: Exception) {
                        arrayListOf()
                    }
                    if (listUser.isNotEmpty()) ikDataRepository
                        ?.insertAllUserBilling(listUser)

                }
            }

            val configNCL = async {
                kotlin.runCatching {
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_CUSTOM_NCL)
                    val result = SDKDataHolder.getObject(
                        value,
                        IKSdkCustomNCLDto::class.java
                    )
                    if (!result?.data.isNullOrEmpty())
                        result?.let {
                            ikDataRepository?.insertConfigNCL(it)
                        }
                }
            }
            kotlin.runCatching {
                val value =
                    mRemoteConfig?.getBoolean(IKSdkDefConst.Config.ENABLE_LOADING_NCL) ?: false
                IKSdkDataStore.putBoolean(IKSdkDataStoreConst.ENABLE_LOADING_NCL, value)
            }
            val getOtherConfig = async(Dispatchers.Default) {
                kotlin.runCatching {
                    val otherConfigData =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.OTHER_CONFIG_DATA)
                            ?: IKSdkDefConst.EMPTY
                    kotlin.runCatching {
                        setOtherConfig(
                            Gson().fromJson<java.util.HashMap<String, Any>>(
                                otherConfigData,
                                java.util.HashMap::class.java
                            )
                        )
                        IKSdkDataStore.putString(
                            IKSdkDataStoreConst.KEY_OTHER_CONFIG_DATA,
                            otherConfigData
                        )
                    }
                }
            }


            val getCache = async(Dispatchers.Default) {
                kotlin.runCatching {
                    val cacheConfig =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.CACHE_CONFIG)
                            ?: IKSdkDefConst.EMPTY
                    if (cacheConfig.isNotBlank()) {
                        IKSdkDataStore.putString(IKSdkDataStoreConst.CACHE_ADS_DTO, cacheConfig)
                        val ctTime = System.currentTimeMillis()
                        IKSdkDataStore.putLong(IKSdkDataStoreConst.CACHE_ADS_TIME, ctTime)
                    }
                }
            }

            val getRemoteVersion = async(Dispatchers.Default) {
                kotlin.runCatching {
                    val remoteVersion =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.REMOTE_VERSION)
                            ?: IKSdkDefConst.EMPTY
                    if (remoteVersion.isNotBlank()) {
                        IKSdkDataStore.putString(IKSdkDataStoreConst.REMOTE_VERSION, remoteVersion)
                        IKSdkApplicationProvider.getContext()?.let {
                            FirebaseAnalytics.getInstance(it)
                                .setUserProperty(
                                    IKTrackingConst.ParamName.REMOTE_VERSION,
                                    remoteVersion
                                )
                        }
                    }
                }
            }

            kotlin.runCatching {
                val value =
                    mRemoteConfig?.getString(IKSdkDefConst.Config.AWS_MEDIATION_CONFIG)
                        ?: IKSdkDefConst.EMPTY
                if (value.isNotBlank())
                    IKSdkDataStore.putString(IKSdkDataStoreConst.AWS_MEDIATION_CONFIG, value)
            }
            val getOenDataLocal = async(Dispatchers.Default) {
                runCatching {
                    val dataOpenLC = mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_OPEN_LC)
                        ?: IKSdkDefConst.EMPTY
                    if (dataOpenLC.isBlank())
                        return@async
                    if (dataOpenLC.trim() == IKSdkDefConst.STRING_EMPTY) {
                        ikDataRepository?.deleteAllDefaultOpen()
                    } else {
                        var listOpenDefaultDto = ArrayList<IKSdkDataOpLocalDto>()
                        kotlin.runCatching {
                            listOpenDefaultDto =
                                SDKDataHolder.getObjectSdk<ArrayList<IKSdkDataOpLocalDto>>(
                                    dataOpenLC.trim(),
                                    object : TypeToken<ArrayList<IKSdkDataOpLocalDto>>() {}.type
                                ) ?: arrayListOf()
                        }
                        if (listOpenDefaultDto.isNotEmpty()) {
                            ikDataRepository?.insertAllDefaultOpen(listOpenDefaultDto)
                            IKSdkDataStore.putLong(
                                IKSdkDataStoreConst.KEY_TIME_CALL_OPEN_DEFAULT,
                                System.currentTimeMillis()
                            )
                        } else {
                        }
                    }
                }
            }
            val sdkBannerInline = async {
                kotlin.runCatching {
                    val value =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_BANNER_INLINE)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkBannerInlineDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKBannerInline(result)
                }
            }
            val sdkBannerCl = async {
                kotlin.runCatching {
                    val value =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_BANNER_COLLAPSE)
                    val result =
                        SDKDataHolder.getObjectSdk(value, IKSdkBannerCollapseDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKBannerCollapse(result)
                }
            }
            val sdkNativeFs = async {
                kotlin.runCatching {
                    val value =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_NATIVE_FULL_SCREEN)
                    val result =
                        SDKDataHolder.getObjectSdk(value, IKSdkNativeFullScreenDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKNativeFullScreen(result)
                }
            }
            val sdkAudioIcon = async {
                kotlin.runCatching {
                    val value =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_AUDIO_ICON)
                    val result =
                        SDKDataHolder.getObjectSdk(value, IKSdkAudioIconDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKAudioIcon(result)
                }
            }
            val sdkBannerClCustom = async {
                kotlin.runCatching {
                    val value =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_BANNER_COLLAPSE_CUSTOM)
                    val result =
                        SDKDataHolder.getObjectSdk(value, IKSdkBannerCollapseCustomDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKBannerCollapseCustom(result)
                }
            }
            getCmpRequest.await()
            getRemoteVersion.await()
            allRemoteConfig.await()
            getOther.await()
            getUserBilling.await()
            getOtherConfig.await()
            callbackDone?.invoke()
            getCache.await()
            sdkOpen.await()
            sdkInter.await()
            sdkReward.await()
            sdkNative.await()
            sdkBanner.await()
            sdkFirstAd.await()
            configInter.await()
            configOpen.await()
            configReward.await()
            configWidget.await()
            configNCL.await()
//            getOenDataLocal.await()
            sdkBannerInline.await()
//            sdkMrec.await()
            sdkBannerCl.await()
            sdkNativeFs.await()
            sdkBannerClCustom.await()
//            sdkAudioIcon.await()
            showLogSdk("getDataRemoteConfig") { "on done" }
        }
    }

    private suspend fun validNewDataUpdate(): Boolean {
        var newDataUpdate = false
        val currentVersion: String =
            IKSdkUtilsCore.getVersionApp(IKSdkApplicationProvider.getContext()).toString()
        val saveVersionCode = IKSdkDataStore.getString(
            IKSdkDataStoreConst.KEY_CURRENT_VERSION_CODE,
            IKSdkDefConst.EMPTY
        )
        if (saveVersionCode != currentVersion) {
            newDataUpdate = true
            IKSdkDataStore.putString(IKSdkDataStoreConst.KEY_CURRENT_VERSION_CODE, currentVersion)
        }
        return newDataUpdate
    }

    private suspend fun getRemoteData() {
        showLogSdk("getRemoteData") { "start" }
        kotlin.runCatching {
            lastTimeUpdateConfig = System.currentTimeMillis()
            firebaseRemote.addCompleteListener(sdkCallback)
            firebaseRemote.fetchRemoteData()
        }.onFailure {
            showLogSdk("getRemoteData") { "onFailure" }
            configCallback?.onFail(IKError(it))
        }
        addUpdateConfigCallback()
    }

    fun getDataCache(context: Context?) {
        showLogSdk("getRemoteData") { "getDataCache start" }
        mUiScope.launchWithSupervisorJob(Dispatchers.IO) {
            getDefaultXmlCustomCache()
        }
        mUiScope.launchWithSupervisorJob(Dispatchers.IO) {
            try {
                val cacheConfigDto = try {
                    Gson().fromJson(
                        IKSdkDataStore.getString(
                            IKSdkDataStoreConst.CACHE_ADS_DTO,
                            IKSdkDefConst.EMPTY
                        ), CacheConfigDto::class.java
                    )
                } catch (e: Exception) {
                    null
                }
                if (cacheConfigDto != null) {
                    val cacheOpenAds = ikDataRepository?.getConfigOpen()
                    if (cacheOpenAds != null) {

                        val currentTime = System.currentTimeMillis()
                        val currentTimeCache =
                            IKSdkDataStore.getLong(IKSdkDataStoreConst.CACHE_ADS_TIME, -1)
                        if (currentTime - currentTimeCache < (cacheConfigDto.cacheTime * 1000)) {
                            dataHasCache = true
                        }
                    }
                }

                if (dataHasCache) {
                    val newDataUpdate = validNewDataUpdate()
                    showLogSdk("getRemoteData") { "newDataUpdate=${newDataUpdate}" }
//                    if (newDataUpdate) getDefaultXmlCustom()
                } else {
                    if (context != null) {
                        getDefaultXml(context)
                    }
                }

                kotlin.runCatching {
                    val type =
                        object :
                            TypeToken<java.util.HashMap<String, IKRemoteConfigValue>>() {}.type
                    val remoteDataCache = IKSdkDataStore.getString(
                        IKSdkDataStoreConst.REMOTE_CONFIG_DATA,
                        IKSdkDefConst.EMPTY
                    )
                    if (remoteDataCache.isNotBlank())
                        mutexRemoteConfigData.withLock {
                            mRemoteConfigData = Gson().fromJson(remoteDataCache, type)
                        }
                }
                showLogSdk("getRemoteData") {
                    "remoteConfigData=${
                        mRemoteConfigData.isNotEmpty().log()
                    }"
                }
                kotlin.runCatching {
                    val remoteVersion = IKSdkDataStore.getString(
                        IKSdkDataStoreConst.REMOTE_VERSION,
                        IKSdkDefConst.EMPTY
                    )
                    showLogSdk("getRemoteData") { "remoteVersion=${remoteVersion}" }
                    if (context != null) {
                        FirebaseAnalytics.getInstance(context)
                            .setUserProperty("remote_version", remoteVersion)
                    }
                }
                showLogSdk("getRemoteData") { "dataHasCache=$dataHasCache" }
                if (dataHasCache)
                    withContext(Dispatchers.Main) {
                        configCallback?.onSuccess()
                    }
                else {
                    if (configCallback == null)
                        delay(2000)

                    getRemoteData()

                }
            } catch (e: Exception) {
                showLogSdk("getRemoteData") { "Exception=${e.message}" }
                configCallback?.onFail(IKError(e))
            }
        }
    }

    private fun addUpdateConfigCallback() {
        mRemoteConfig?.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                showLogSdk("addUpdateConfigCallback") { "onUpdate" }
                onUpdateRemoteConfig(configUpdate)
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                showLogSdk("addUpdateConfigCallback") { "onError ${error.message}" }
            }
        })
    }

    private fun onUpdateRemoteConfig(
        configUpdate: ConfigUpdate?
    ) {
        showLogSdk("onUpdateRemoteConfig") { "start run" }
        mUiScope.launchWithSupervisorJob(Dispatchers.Default) {
            if (configUpdate == null || mRemoteConfig?.all.isNullOrEmpty()) return@launchWithSupervisorJob
            if (System.currentTimeMillis() - lastTimeUpdateConfig < TIME_UPDATE_CONFIG) return@launchWithSupervisorJob

            kotlin.runCatching {
                if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.UPDATE_APP_CONFIG))
                    return@runCatching
                updateAppConfig = mRemoteConfig?.getString(IKSdkDefConst.Config.UPDATE_APP_CONFIG)
                    ?: IKSdkDefConst.EMPTY
            }
            val getCmpRequest = async(Dispatchers.Default) {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.CMP_CONFIG_REQUEST_ENABLE))
                        return@runCatching
                    val value =
                        mRemoteConfig?.getBoolean(IKSdkDefConst.Config.CMP_CONFIG_REQUEST_ENABLE)
                            ?: false
                    IKSdkDataStore.putBoolean(IKSdkDataStoreConst.CMP_CONFIG_REQUEST_ENABLE, value)
                }
            }
            val current = System.currentTimeMillis()
            if (current - mLastTimeGetConfig < TIME_UPDATE_CONFIG / 2) {
                delay(500)
                return@launchWithSupervisorJob
            }

            mLastTimeGetConfig = System.currentTimeMillis()

            val sdkOpen = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.SDK_DATA_OPEN))
                        return@runCatching
                    val value =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_OPEN)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkOpenDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKOpen(result)

                }
            }
            val allRemoteConfig = async(Dispatchers.Default) {
                mutexRemoteConfigData.withLock {
                    kotlin.runCatching {
                        mRemoteConfig?.all?.forEach {
                            mRemoteConfigData[it.key] = IKRemoteConfigValue(it.value)
                        }
                    }
                }
                kotlin.runCatching {
                    IKSdkDataStore.putString(
                        IKSdkDataStoreConst.REMOTE_CONFIG_DATA,
                        Gson().toJson(getRemoteConfigData())
                    )
                }
            }

            val sdkInter = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.SDK_DATA_INTER))
                        return@runCatching
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_INTER)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkInterDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKInter(result)
                }
            }
            val sdkReward = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.SDK_DATA_REWARD))
                        return@runCatching
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_REWARD)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkRewardDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKReward(result)

                }
            }

            val sdkNative = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.SDK_DATA_NATIVE))
                        return@runCatching
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_NATIVE)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkNativeDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKNative(result)

                }
            }

            val sdkBanner = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.SDK_DATA_BANNER))
                        return@runCatching
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_BANNER)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkBannerDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKBanner(result)

                }
            }
            val getOther = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.BACK_UP_AD_CONFIG))
                        return@runCatching
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.BACK_UP_AD_CONFIG)

                    val result = try {
                        SDKDataHolder.getObjectSdk(value, IKSdkBackupAdDto::class.java)
                    } catch (e: Exception) {
                        null
                    }
                    if (result != null) {
                        ikDataRepository?.insertBackup(result)
                    }
                }
            }
            val sdkFirstAd = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.FIRST_AD_CONFIG))
                        return@runCatching
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.FIRST_AD_CONFIG)
                    val result = SDKDataHolder.getObject(value, IKSdkFirstAdDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKFirstAd(result)
                }
            }
            val configInter = async {
                kotlin.runCatching {
                    val userProperty =
                        IKSdkDataStore.getString(IKSdkDefConst.Config.SDK_USER_PROPERTY_TYPE, "")
                    val keyConfig = IKSdkDefConst.Config.CONFIG_DATA_INTER
                    if (userProperty.isNotBlank() && configUpdate.updatedKeys.contains(keyConfig + "_$userProperty")) {
                        val value = mRemoteConfig?.getString(keyConfig + "_$userProperty")
                        val result = kotlin.runCatching {
                            SDKDataHolder.getObject(
                                value,
                                IKSdkProdInterDto::class.java
                            )
                        }.getOrNull()
                        showLogSdk("getDataRemoteConfig") { "configInter and _$userProperty =${(!result?.data.isNullOrEmpty()).log()}" }
                        if (result != null && !result.data.isNullOrEmpty()) {
                            ikDataRepository?.insertConfigInter(result)
                            return@async
                        }
                    }

                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.CONFIG_DATA_INTER))
                        return@runCatching
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.CONFIG_DATA_INTER)
                    val result = SDKDataHolder.getObject(
                        value,
                        IKSdkProdInterDto::class.java
                    )
                    if (!result?.data.isNullOrEmpty())
                        result?.let {
                            ikDataRepository?.insertConfigInter(it)
                        }
                }
            }

            val configReward = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.CONFIG_DATA_REWARD))
                        return@runCatching
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.CONFIG_DATA_REWARD)
                    val result = SDKDataHolder.getObject(
                        value,
                        IKSdkProdRewardDto::class.java
                    )
                    if (!result?.data.isNullOrEmpty())
                        result?.let {
                            ikDataRepository?.insertConfigReward(it)
                        }
                }
            }

            val configOpen = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.CONFIG_DATA_OPEN))
                        return@runCatching
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.CONFIG_DATA_OPEN)
                    val result = SDKDataHolder.getObject(
                        value,
                        IKSdkProdOpenDto::class.java
                    )
                    if (!result?.data.isNullOrEmpty())
                        result?.let {
                            ikDataRepository?.insertConfigOpen(it)
                        }
                }
            }

            val configWidget = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.CONFIG_DATA_WIDGET))
                        return@runCatching
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.CONFIG_DATA_WIDGET)
                    val result = SDKDataHolder.getObject(
                        value,
                        IKSdkProdWidgetDto::class.java
                    )
                    if (!result?.data.isNullOrEmpty())
                        result?.let {
                            ikDataRepository?.insertConfigWidget(it)
                        }
                }
            }

            val getUserBilling = async(Dispatchers.Default) {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.PURCHASE_USER_IGNORE))
                        return@runCatching
                    val userBillingConfig =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.PURCHASE_USER_IGNORE)
                            ?: IKSdkDefConst.EMPTY
                    val listUser = try {
                        Gson().fromJson<java.util.ArrayList<UserBillingDetail>>(
                            userBillingConfig,
                            object : TypeToken<java.util.ArrayList<UserBillingDetail>>() {}.type
                        ) ?: arrayListOf()
                    } catch (e: Exception) {
                        arrayListOf()
                    }
                    if (listUser.isNotEmpty()) ikDataRepository
                        ?.insertAllUserBilling(listUser)

                }
            }

            val configNCL = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.SDK_CUSTOM_NCL))
                        return@runCatching
                    val value = mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_CUSTOM_NCL)
                    val result = SDKDataHolder.getObject(
                        value,
                        IKSdkCustomNCLDto::class.java
                    )
                    if (!result?.data.isNullOrEmpty())
                        result?.let {
                            ikDataRepository?.insertConfigNCL(it)
                        }
                }
            }
            kotlin.runCatching {
                if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.ENABLE_LOADING_NCL))
                    return@runCatching
                val value =
                    mRemoteConfig?.getBoolean(IKSdkDefConst.Config.ENABLE_LOADING_NCL) ?: false
                IKSdkDataStore.putBoolean(IKSdkDataStoreConst.ENABLE_LOADING_NCL, value)
            }
            val getOtherConfig = async(Dispatchers.Default) {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.OTHER_CONFIG_DATA))
                        return@runCatching
                    val otherConfigData =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.OTHER_CONFIG_DATA)
                            ?: IKSdkDefConst.EMPTY
                    kotlin.runCatching {
                        setOtherConfig(
                            Gson().fromJson<java.util.HashMap<String, Any>>(
                                otherConfigData,
                                java.util.HashMap::class.java
                            )
                        )
                        IKSdkDataStore.putString(
                            IKSdkDataStoreConst.KEY_OTHER_CONFIG_DATA,
                            otherConfigData
                        )
                    }
                }
            }


            val getCache = async(Dispatchers.Default) {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.CACHE_CONFIG))
                        return@runCatching
                    val cacheConfig =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.CACHE_CONFIG)
                            ?: IKSdkDefConst.EMPTY
                    if (cacheConfig.isNotBlank()) {
                        IKSdkDataStore.putString(IKSdkDataStoreConst.CACHE_ADS_DTO, cacheConfig)
                        val ctTime = System.currentTimeMillis()
                        IKSdkDataStore.putLong(IKSdkDataStoreConst.CACHE_ADS_TIME, ctTime)
                    }
                }
            }

            val getRemoteVersion = async(Dispatchers.Default) {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.REMOTE_VERSION))
                        return@runCatching
                    val remoteVersion =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.REMOTE_VERSION)
                            ?: IKSdkDefConst.EMPTY
                    if (remoteVersion.isNotBlank()) {
                        IKSdkDataStore.putString(IKSdkDataStoreConst.REMOTE_VERSION, remoteVersion)
                        IKSdkApplicationProvider.getContext()?.let {
                            FirebaseAnalytics.getInstance(it)
                                .setUserProperty(
                                    IKTrackingConst.ParamName.REMOTE_VERSION,
                                    remoteVersion
                                )
                        }
                    }
                }
            }

            kotlin.runCatching {
                if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.AWS_MEDIATION_CONFIG))
                    return@runCatching
                val value =
                    mRemoteConfig?.getString(IKSdkDefConst.Config.AWS_MEDIATION_CONFIG)
                        ?: IKSdkDefConst.EMPTY
                if (value.isNotBlank())
                    IKSdkDataStore.putString(IKSdkDataStoreConst.AWS_MEDIATION_CONFIG, value)
            }
            val getOenDataLocal = async(Dispatchers.Default) {
                runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.SDK_DATA_OPEN_LC))
                        return@runCatching
                    val dataOpenLC = mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_OPEN_LC)
                        ?: IKSdkDefConst.EMPTY
                    if (dataOpenLC.isBlank())
                        return@async
                    if (dataOpenLC.trim() == IKSdkDefConst.STRING_EMPTY) {
                        ikDataRepository?.deleteAllDefaultOpen()
                    } else {
                        var listOpenDefaultDto = ArrayList<IKSdkDataOpLocalDto>()
                        kotlin.runCatching {
                            listOpenDefaultDto =
                                SDKDataHolder.getObjectSdk<ArrayList<IKSdkDataOpLocalDto>>(
                                    dataOpenLC.trim(),
                                    object : TypeToken<ArrayList<IKSdkDataOpLocalDto>>() {}.type
                                ) ?: arrayListOf()
                        }
                        if (listOpenDefaultDto.isNotEmpty()) {
                            ikDataRepository?.insertAllDefaultOpen(listOpenDefaultDto)
                            IKSdkDataStore.putLong(
                                IKSdkDataStoreConst.KEY_TIME_CALL_OPEN_DEFAULT,
                                System.currentTimeMillis()
                            )
                        }
                    }
                }
            }
            val sdkBannerInline = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.SDK_DATA_BANNER_INLINE))
                        return@runCatching
                    val value =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_BANNER_INLINE)
                    val result = SDKDataHolder.getObjectSdk(value, IKSdkBannerInlineDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKBannerInline(result)
                }
            }

            val sdkBannerCl = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.SDK_DATA_BANNER_COLLAPSE))
                        return@runCatching
                    val value =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_BANNER_COLLAPSE)
                    val result =
                        SDKDataHolder.getObjectSdk(value, IKSdkBannerCollapseDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKBannerCollapse(result)
                }
            }

            val sdkNativeFs = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.SDK_DATA_NATIVE_FULL_SCREEN))
                        return@runCatching
                    val value =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_NATIVE_FULL_SCREEN)
                    val result =
                        SDKDataHolder.getObjectSdk(value, IKSdkNativeFullScreenDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKNativeFullScreen(result)
                }
            }

            val sdkAudioIcon = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.SDK_DATA_AUDIO_ICON))
                        return@runCatching
                    val value =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_AUDIO_ICON)
                    val result =
                        SDKDataHolder.getObjectSdk(value, IKSdkAudioIconDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKAudioIcon(result)
                }
            }
            val sdkBannerClCustom = async {
                kotlin.runCatching {
                    if (!configUpdate.updatedKeys.contains(IKSdkDefConst.Config.SDK_DATA_BANNER_COLLAPSE_CUSTOM))
                        return@runCatching
                    val value =
                        mRemoteConfig?.getString(IKSdkDefConst.Config.SDK_DATA_BANNER_COLLAPSE_CUSTOM)
                    val result =
                        SDKDataHolder.getObjectSdk(value, IKSdkBannerCollapseCustomDto::class.java)
                    if (result != null)
                        ikDataRepository?.insertSDKBannerCollapseCustom(result)
                }
            }

            getCmpRequest.await()
            getRemoteVersion.await()
            allRemoteConfig.await()
            getOther.await()
            getUserBilling.await()
            getOtherConfig.await()
            getCache.await()
            sdkOpen.await()
            sdkInter.await()
            sdkReward.await()
            sdkNative.await()
            sdkBanner.await()
            sdkFirstAd.await()
            configInter.await()
            configOpen.await()
            configReward.await()
            configWidget.await()
            configNCL.await()
//            getOenDataLocal.await()
            sdkBannerInline.await()
            sdkBannerCl.await()
            sdkNativeFs.await()
//            sdkMrec.await()
//            sdkAudioIcon.await()
            sdkBannerClCustom.await()
        }
    }

    fun checkUpdateRemoteConfig(time: Long = TIME_UPDATE_CONFIG) {
        showLogSdk("checkUpdateRemoteConfig") { "start" }
        mUiScope.launchWithSupervisorJob(Dispatchers.Default) {
            runCatching {
                val current = System.currentTimeMillis()
                if (current - lastTimeUpdateConfig < time) return@launchWithSupervisorJob
                val cacheConfigDto = try {
                    Gson().fromJson(
                        IKSdkDataStore.getString(
                            IKSdkDataStoreConst.CACHE_ADS_DTO,
                            IKSdkDefConst.EMPTY
                        ), CacheConfigDto::class.java
                    )
                } catch (e: Exception) {
                    null
                }

                val currentTimeCache =
                    IKSdkDataStore.getLong(IKSdkDataStoreConst.CACHE_ADS_TIME, -1)
                if (cacheConfigDto != null && (cacheConfigDto.forgeUpdate || (current - currentTimeCache) > (cacheConfigDto.cacheTime * 1000))) {
                    dataHasCache = false
                    lastTimeUpdateConfig = System.currentTimeMillis()
                    val sdkCallback = object : IKFirebaseRemoteConfigListener {
                        override fun onComplete(value: Task<Boolean>) {
                            kotlin.runCatching {
                                firebaseRemote.removeCompleteListener(this)
                            }
                            showLogSdk("checkUpdateRemoteConfig") { "onComplete = ${value.isSuccessful}" }
                            if (value.isSuccessful) {
                                mUiScope.launchWithSupervisorJob {
                                    IKSdkDataStore.putLong(IKSdkDataStoreConst.CACHE_ADS_TIME, -1)
                                }
                                getDataRemoteConfig()
                            }
                        }

                        override fun onError(error: java.lang.Exception) {
                            showLogSdk("checkUpdateRemoteConfig") { "onError = ${error.message}" }
                            kotlin.runCatching {
                                firebaseRemote.removeCompleteListener(this)
                            }
                        }
                    }
                    firebaseRemote.addCompleteListener(sdkCallback)
                    firebaseRemote.fetchRemoteData()
                }
            }
        }

    }

    fun initFirebaseRemoteConfig() {
        firebaseRemote.getRemoteConfig()?.setDefaultsAsync(IKDataSourceHelper.getDefaultsDataFile())
        firebaseRemote.getRemoteConfig()?.setConfigSettingsAsync(remoteConfigSettings {
            minimumFetchIntervalInSeconds = BLOCK_TIME_FORGE_UPDATE
            fetchTimeoutInSeconds = 30
        })
    }

    suspend fun getRemoteConfigData(): HashMap<String, IKRemoteConfigValue> {
        return mutexRemoteConfigData.withLock {
            if (mRemoteConfigData.isEmpty()) {
                firebaseRemote.getRemoteConfigData().forEach {
                    mRemoteConfigData[it.key] = IKRemoteConfigValue(it.value)
                }
            }
            HashMap(mRemoteConfigData)
        }
    }

    suspend fun getRemoteConfigDataProduct(): HashMap<String, IKRemoteConfigValue> {
        return withContext(Dispatchers.Default) {
            val remoteData = getRemoteConfigData()
            HashMap(remoteData.filter { !IKSdkDefConst.Config.listK.contains(it.key) })
        }
    }

    fun fetchNewRemoteConfigData(callback: IKNewRemoteConfigCallback) {
        val sdkCallback = object : IKFirebaseRemoteConfigListener {
            override fun onComplete(value: Task<Boolean>) {
                kotlin.runCatching {
                    firebaseRemote.removeCompleteListener(this)
                }
                if (value.isSuccessful) {
                    getDataRemoteConfig {
                        mUiScope.launchWithSupervisorJob {
                            callback.onSuccess(getRemoteConfigData())
                        }
                    }
                } else {
                    mUiScope.launchWithSupervisorJob {
                        callback.onFail(IKError(value.exception))
                    }
                }
            }

            override fun onError(error: java.lang.Exception) {
                kotlin.runCatching {
                    firebaseRemote.removeCompleteListener(this)
                }
                mUiScope.launchWithSupervisorJob {
                    callback.onFail(IKError(error))
                }
            }
        }
        firebaseRemote.addCompleteListener(sdkCallback)
        mUiScope.launchWithSupervisorJob {
            firebaseRemote.fetchRemoteData()
        }
    }

    private fun showLogSdk(tag: String, message: () -> String) {
        IKLogs.dSdk("remoteData") {
            "${tag}:" + message.invoke()
        }
    }
}