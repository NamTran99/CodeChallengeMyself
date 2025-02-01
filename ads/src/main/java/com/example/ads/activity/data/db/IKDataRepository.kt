package com.example.ads.activity.data.db

import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.data.db.IkmSdkCacheFunc.DB.verifyCache
import com.example.ads.activity.data.dto.pub.IKAdFormat
import com.example.ads.activity.data.dto.sdk.IKGkAdDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkAudioIconDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkBackupAdDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkBannerCollapseCustomDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkBannerCollapseDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkBannerDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkBannerInlineDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkCustomNCLDetailDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkCustomNCLDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkDataOpLocalDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkFirstAdDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkInterDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkMRECDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkNativeDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkNativeFullScreenDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkOpenDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdInterDetailDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdInterDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdOpenDetailDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdOpenDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdRewardDetailDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdRewardDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdWidgetDetailDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdWidgetDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkRewardDto
import com.example.ads.activity.data.dto.sdk.data.UserBillingDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object IKDataRepository {
    private var commonFileDao: IKSdkDbDAO? = null

    fun getInstance(): IKDataRepository = this

    private fun getDao(): IKSdkDbDAO? {
        if (commonFileDao == null) {
            val database = IKSdkApplicationProvider.getContext()?.let {
                IKSdkRoomDB.getInstance(it)
            }
            commonFileDao = database?.commonAdsDao()
        }
        return commonFileDao
    }

    suspend fun insertAllDefaultOpen(listDto: List<IKSdkDataOpLocalDto>) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                IkmSdkCacheFunc.DB.cacheDataLocalDefault = Pair(System.currentTimeMillis(), listDto)
            }
            deleteAllDefaultOpen()
            kotlin.runCatching {
                getDao()?.insertAllOpenDefault(listDto)
            }
        }
    }

    suspend fun insertAllDefaultOpenCache(listDto: List<IKSdkDataOpLocalDto>) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                if (IkmSdkCacheFunc.DB.cacheDataLocalDefault.second.isNullOrEmpty())
                    IkmSdkCacheFunc.DB.cacheDataLocalDefault =
                        Pair(0, listDto)
            }
        }
    }


    suspend fun insertAllUserBilling(listDto: List<UserBillingDetail>) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                deleteAllUserBilling()
                getDao()?.insertAllUserBilling(listDto)
            }
        }
    }


    suspend fun deleteAllDefaultOpen() {
        withContext(Dispatchers.IO) {
            IkmSdkCacheFunc.DB.cacheDataLocalDefault = Pair(0, listOf())
            kotlin.runCatching {
                getDao()?.deleteAllDefaultOpen()
            }
        }
    }

    suspend fun deleteAllOther() {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                getDao()?.deleteAllBackup()
            }
        }
    }


    suspend fun deleteAllUserBilling() {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                getDao()?.deleteAllUserBilling()
            }
        }
    }

    suspend fun getAllDataLocalDefault(): List<IKSdkDataOpLocalDto>? {
        val cache = IkmSdkCacheFunc.DB.cacheDataLocalDefault
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getAllOpenDefault()
            }.getOrNull()
        }

        return if (newData?.second.isNullOrEmpty()) {
            IkmSdkCacheFunc.DB.cacheDataLocalDefault.second
        } else {
            if (newData != null) {
                IkmSdkCacheFunc.DB.cacheDataLocalDefault = newData
            }
            IkmSdkCacheFunc.DB.cacheDataLocalDefault.second
        }
    }


    suspend fun getAllUserBilling(): List<UserBillingDetail> {
        return withContext(Dispatchers.IO) {
            runCatching {
                getDao()?.getAllUserBilling()
            }.getOrNull() ?: listOf()
        }
    }

    suspend fun insertBackup(dto: IKSdkBackupAdDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSdkBackupAdDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllBackup()
            }
            runCatching {
                getDao()?.insertBackup(dto)
            }
        }
    }

    suspend fun getBackupDto(): IKSdkBackupAdDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSdkBackupAdDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getBackupDto()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSdkBackupAdDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSdkBackupAdDto = newData
            IkmSdkCacheFunc.DB.cacheIKSdkBackupAdDto.second
        }
    }

    suspend fun insertIKGkAdDto(dto: IKGkAdDto) {
        withContext(Dispatchers.IO) {
            runCatching {
                getDao()?.deleteIKGkAdDto()
                getDao()?.insertIKGkAdDto(dto)
            }
        }
    }

    suspend fun getIKGkAdDto(): IKGkAdDto? {
        return withContext(Dispatchers.IO) {
            runCatching {
                getDao()?.getIKGkAdDto()
            }.getOrNull()
        }
    }

    //IKSDKInter
    suspend fun insertSDKInter(dto: IKSdkInterDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSDKInterDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKInter()
            }
            runCatching {
                getDao()?.insertSDKInter(dto)
            }
        }
    }

    suspend fun getSDKInter(): IKSdkInterDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSDKInterDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getSDKInter()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSDKInterDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSDKInterDto = newData
            IkmSdkCacheFunc.DB.cacheIKSDKInterDto.second
        }
    }

    //IKSDKOpen
    suspend fun insertSDKOpen(dto: IKSdkOpenDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSDKOpenDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKOpen()
            }
            runCatching {
                getDao()?.insertSDKOpen(dto)
            }
        }
    }

    suspend fun getSDKOpen(): IKSdkOpenDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSDKOpenDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getSDKOpen()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSDKOpenDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSDKOpenDto = newData
            IkmSdkCacheFunc.DB.cacheIKSDKOpenDto.second
        }
    }

    //IKSDKNative
    suspend fun insertSDKNative(dto: IKSdkNativeDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSDKNativeDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKNative()
            }
            runCatching {
                getDao()?.insertSDKNative(dto)
            }
        }
    }

    suspend fun getSDKNative(): IKSdkNativeDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSDKNativeDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getSDKNative()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSDKNativeDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSDKNativeDto = newData
            IkmSdkCacheFunc.DB.cacheIKSDKNativeDto.second
        }
    }

    //IKSDKOBanner
    suspend fun insertSDKBanner(dto: IKSdkBannerDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSDKBannerDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKBanner()
            }
            runCatching {
                getDao()?.insertSDKBanner(dto)
            }
        }
    }

    suspend fun getSDKBanner(): IKSdkBannerDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSDKBannerDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getSDKBanner()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSDKBannerDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSDKBannerDto = newData
            IkmSdkCacheFunc.DB.cacheIKSDKBannerDto.second
        }
    }

    //IKSDKReward
    suspend fun insertSDKReward(dto: IKSdkRewardDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSDKRewardDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKReward()
            }
            runCatching {
                getDao()?.insertSDKReward(dto)
            }
        }
    }

    suspend fun getSDKReward(): IKSdkRewardDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSDKRewardDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getSDKReward()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSDKRewardDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSDKRewardDto = newData
            IkmSdkCacheFunc.DB.cacheIKSDKRewardDto.second
        }
    }

    //IKSDK Fist ad
    suspend fun insertSDKFirstAd(dto: IKSdkFirstAdDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSDKFirstAdDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKFirstAd()
            }
            runCatching {
                getDao()?.insertSDKFirstAd(dto)
            }
        }
    }

    //IKSDK Fist ad
    suspend fun insertCacheSDKFirstAd(dto: IKSdkFirstAdDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                if (IkmSdkCacheFunc.DB.cacheIKSDKFirstAdDto.second == null)
                    IkmSdkCacheFunc.DB.cacheIKSDKFirstAdDto = Pair(current, dto)
            }
        }
    }

    suspend fun getSDKFirstAd(): IKSdkFirstAdDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSDKFirstAdDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getSDKFirstAd()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSDKFirstAdDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSDKFirstAdDto = newData
            IkmSdkCacheFunc.DB.cacheIKSDKFirstAdDto.second
        }
    }

    suspend fun insertConfigOpen(dto: IKSdkProdOpenDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSdkProdOpenDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKConfigOpen()
            }
            runCatching {
                getDao()?.insertConfigOpen(dto)
            }
        }
    }

    suspend fun getConfigOpen(): IKSdkProdOpenDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSdkProdOpenDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getConfigOpen()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSdkProdOpenDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSdkProdOpenDto = newData
            IkmSdkCacheFunc.DB.cacheIKSdkProdOpenDto.second
        }
    }

    suspend fun getConfigOpen(screenName: String): IKSdkProdOpenDetailDto? {
        return runCatching {
            getConfigOpen()?.data?.find { it.screenName == screenName }
        }.getOrNull()
    }

    suspend fun insertConfigInter(dto: IKSdkProdInterDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSdkProdInterDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKConfigInter()
            }
            runCatching {
                getDao()?.insertConfigInter(dto)
            }
        }
    }

    suspend fun getProductConfigInter(): IKSdkProdInterDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSdkProdInterDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getConfigInter()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSdkProdInterDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSdkProdInterDto = newData
            IkmSdkCacheFunc.DB.cacheIKSdkProdInterDto.second
        }
    }

    suspend fun getProductConfigInter(screenName: String): IKSdkProdInterDetailDto? {
        return runCatching {
            getProductConfigInter()?.data?.find { it.screenName == screenName }
        }.getOrNull()
    }

    suspend fun insertConfigReward(dto: IKSdkProdRewardDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSdkProdRewardDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKConfigReward()
            }
            runCatching {
                getDao()?.insertConfigReward(dto)
            }
        }
    }

    suspend fun getConfigReward(screenName: String): IKSdkProdRewardDetailDto? {
        return runCatching {
            getConfigReward()?.data?.find { it.screenName == screenName }
        }.getOrNull()
    }

    suspend fun getConfigReward(): IKSdkProdRewardDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSdkProdRewardDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getConfigReward()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSdkProdRewardDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSdkProdRewardDto = newData
            IkmSdkCacheFunc.DB.cacheIKSdkProdRewardDto.second
        }
    }

    suspend fun insertConfigWidget(dto: IKSdkProdWidgetDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSdkProdWidgetDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKConfigWidget()
            }
            runCatching {
                getDao()?.insertConfigWidget(dto)
            }
        }
    }

    suspend fun getConfigWidget(): IKSdkProdWidgetDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSdkProdWidgetDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getConfigWidget()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSdkProdWidgetDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSdkProdWidgetDto = newData
            IkmSdkCacheFunc.DB.cacheIKSdkProdWidgetDto.second
        }
    }

    suspend fun getConfigWidget(screenName: String, format: IKAdFormat): IKSdkProdWidgetDetailDto? {
        return runCatching {
            getConfigWidget()?.data?.find { it.screenName == screenName && it.adFormat == format.value }
        }.getOrNull()
    }

    suspend fun getConfigWidget(screenName: String): IKSdkProdWidgetDetailDto? {
        return runCatching {
            getConfigWidget()?.data?.find { it.screenName == screenName }
        }.getOrNull()
    }

    suspend fun insertConfigNCL(dto: IKSdkCustomNCLDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSdkProdNCLDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKConfigNCL()
            }
            runCatching {
                getDao()?.insertConfigNCL(dto)
            }
        }
    }

    suspend fun getConfigNCL(): IKSdkCustomNCLDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSdkProdNCLDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getConfigNCL()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSdkProdNCLDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSdkProdNCLDto = newData
            IkmSdkCacheFunc.DB.cacheIKSdkProdNCLDto.second
        }
    }

    suspend fun getConfigNCL(format: IKAdFormat): IKSdkCustomNCLDetailDto? {
        return runCatching {
            getConfigNCL()?.data?.find { it.adFormat == format.value }
        }.getOrNull()
    }

    suspend fun getConfigNCL(screenName: String): IKSdkCustomNCLDetailDto? {
        return runCatching {
            getConfigNCL()?.data?.find { it.screenName == screenName }
        }.getOrNull()
    }

    //IKSdkBannerInlineDto
    suspend fun insertSDKBannerInline(dto: IKSdkBannerInlineDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSDKBannerInlineDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKBannerInline()
            }
            runCatching {
                getDao()?.insertSDKBannerInline(dto)
            }
        }
    }

    suspend fun getSDKBannerInline(): IKSdkBannerInlineDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSDKBannerInlineDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getSDKBannerInline()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSDKBannerInlineDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSDKBannerInlineDto = newData
            IkmSdkCacheFunc.DB.cacheIKSDKBannerInlineDto.second
        }
    }

    //IKSdkBannerInlineDto
    suspend fun insertSDKMREC(dto: IKSdkMRECDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSDKMRECDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKMREC()
            }
            runCatching {
                getDao()?.insertSDKMREC(dto)
            }
        }
    }

    suspend fun getSDKMREC(): IKSdkMRECDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSDKMRECDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getSDKMREC()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSDKMRECDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSDKMRECDto = newData
            IkmSdkCacheFunc.DB.cacheIKSDKMRECDto.second
        }
    }

    //IKSDKOBannerCl
    suspend fun insertSDKBannerCollapse(dto: IKSdkBannerCollapseDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSDKBannerCollapseDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKBannerCollapse()
            }
            runCatching {
                getDao()?.insertSDKBannerCollapse(dto)
            }
        }
    }

    suspend fun getSDKBannerCollapse(): IKSdkBannerCollapseDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSDKBannerCollapseDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getSDKBannerCollapse()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSDKBannerCollapseDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSDKBannerCollapseDto = newData
            IkmSdkCacheFunc.DB.cacheIKSDKBannerCollapseDto.second
        }
    }

    //IKSDKOBannerCl
    suspend fun insertSDKNativeFullScreen(dto: IKSdkNativeFullScreenDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSDKNativeFullScreenDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKNativeFullScreen()
            }
            runCatching {
                getDao()?.insertSDKNativeFullScreen(dto)
            }
        }
    }

    suspend fun getSDKNativeFullScreen(): IKSdkNativeFullScreenDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSDKNativeFullScreenDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getSDKNativeFullScreen()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSDKNativeFullScreenDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSDKNativeFullScreenDto = newData
            IkmSdkCacheFunc.DB.cacheIKSDKNativeFullScreenDto.second
        }
    }

    //AudioIcon
    suspend fun insertSDKAudioIcon(dto: IKSdkAudioIconDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSDKAudioIconDto = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKAudioIcon()
            }
            runCatching {
                getDao()?.insertSDKAudioIcon(dto)
            }
        }
    }

    suspend fun getSDKAudioIcon(): IKSdkAudioIconDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSDKAudioIconDto
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getSDKAudioIcon()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSDKAudioIconDto.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSDKAudioIconDto = newData
            IkmSdkCacheFunc.DB.cacheIKSDKAudioIconDto.second
        }
    }

    //IKSDKOBannerCl
    suspend fun insertSDKBannerCollapseCustom(dto: IKSdkBannerCollapseCustomDto) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val current = System.currentTimeMillis()
                IkmSdkCacheFunc.DB.cacheIKSDKBannerCollapseCustom = Pair(current, dto)
            }
            runCatching {
                getDao()?.deleteAllSDKBannerCollapseCustom()
            }
            runCatching {
                getDao()?.insertSDKBannerCollapseCustom(dto)
            }
        }
    }

    suspend fun getSDKBannerCollapseCustom(): IKSdkBannerCollapseCustomDto? {
        val cache = IkmSdkCacheFunc.DB.cacheIKSDKBannerCollapseCustom
        val newData = cache.verifyCache {
            runCatching {
                getDao()?.getSDKBannerCollapseCustom()
            }.getOrNull()
        }

        return if (newData?.second == null) {
            IkmSdkCacheFunc.DB.cacheIKSDKBannerCollapseCustom.second
        } else {
            IkmSdkCacheFunc.DB.cacheIKSDKBannerCollapseCustom = newData
            IkmSdkCacheFunc.DB.cacheIKSDKBannerCollapseCustom.second
        }
    }

}