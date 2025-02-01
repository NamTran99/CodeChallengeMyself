package com.example.ads.activity.data.db

//import com.example.ads.activity.core.firebase.IKRemoteDataManager
import com.example.ads.activity.data.dto.sdk.data.IKMapShowInterDto
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
import com.example.ads.activity.data.dto.sdk.data.IKSdkMRECDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkNativeDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkNativeFullScreenDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkOpenDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdInterDetailDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdInterDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdOpenDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdRewardDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdWidgetDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkRewardDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object IkmSdkCacheFunc {
    const val TIME_CACHE_EXPIRE = 300_000

    object AD {
        private var mapShowAdInterFrequency: MutableList<IKMapShowInterDto> = mutableListOf()

        fun resetInterAdFrequency() {
            mapShowAdInterFrequency = mutableListOf()
        }

        fun checkInterAdFrequency(configDto: IKSdkProdInterDetailDto): Boolean {
            var mapDto = mapShowAdInterFrequency.find { it.screenName == configDto.screenName }
            if (mapDto == null) {
                mapDto = IKMapShowInterDto(
                    configDto.screenName,
                    configDto.isFirstTime ?: true,
//                    configDto.timeShow ?: IKRemoteDataManager.BLOCK_TIME_SHOW_FULL_ADS,
                    configDto.timeShow ?:2,
                    1,
                    if ((configDto.showAdFrequency ?: 1) <= 0) 1
                    else configDto.showAdFrequency ?: 1
                )
                mapShowAdInterFrequency.add(mapDto)
            }

            if (mapDto.isFirstTime) {
                if (configDto.countFirstTime == true)
                    mapDto.countSinceLastAd = 2
                mapDto.isFirstTime = false
                return true
            }
            if (mapDto.countSinceLastAd <= 1) {
                mapDto.countSinceLastAd = 2
                return false
            }
            runCatching {
                if (mapDto.countSinceLastAd % mapDto.showAdFrequency == 0) {
                    mapDto.countSinceLastAd += 1
                    return true
                }
            }

            mapDto.countSinceLastAd++
            return false
        }
    }

    object DB {
        var cacheDataLocalDefault: Pair<Long, List<IKSdkDataOpLocalDto>?> = Pair(0, listOf())
        var cacheIKSdkProdWidgetDto: Pair<Long, IKSdkProdWidgetDto?> = Pair(0, null)
        var cacheIKSdkProdRewardDto: Pair<Long, IKSdkProdRewardDto?> = Pair(0, null)
        var cacheIKSdkProdInterDto: Pair<Long, IKSdkProdInterDto?> = Pair(0, null)
        var cacheIKSdkProdOpenDto: Pair<Long, IKSdkProdOpenDto?> = Pair(0, null)
        var cacheIKSDKFirstAdDto: Pair<Long, IKSdkFirstAdDto?> = Pair(0, null)
        var cacheIKSDKRewardDto: Pair<Long, IKSdkRewardDto?> = Pair(0, null)
        var cacheIKSDKBannerDto: Pair<Long, IKSdkBannerDto?> = Pair(0, null)
        var cacheIKSDKNativeDto: Pair<Long, IKSdkNativeDto?> = Pair(0, null)
        var cacheIKSDKOpenDto: Pair<Long, IKSdkOpenDto?> = Pair(0, null)
        var cacheIKSDKInterDto: Pair<Long, IKSdkInterDto?> = Pair(0, null)
        var cacheIKSdkProdNCLDto: Pair<Long, IKSdkCustomNCLDto?> = Pair(0, null)
        var cacheWidgetByScreen: HashMap<String, Pair<Long, IKSdkProdWidgetDto?>> = hashMapOf()
        var cacheIKSDKBannerInlineDto: Pair<Long, IKSdkBannerInlineDto?> = Pair(0, null)
        var cacheIKSDKMRECDto: Pair<Long, IKSdkMRECDto?> = Pair(0, null)
        var cacheIKSDKBannerCollapseDto: Pair<Long, IKSdkBannerCollapseDto?> = Pair(0, null)
        var cacheIKSDKNativeFullScreenDto: Pair<Long, IKSdkNativeFullScreenDto?> = Pair(0, null)
        var cacheIKSDKAudioIconDto: Pair<Long, IKSdkAudioIconDto?> = Pair(0, null)
        var cacheIKSdkBackupAdDto: Pair<Long, IKSdkBackupAdDto?> = Pair(0, null)
        var cacheIKSDKBannerCollapseCustom: Pair<Long, IKSdkBannerCollapseCustomDto?> =
            Pair(0, null)

        suspend fun <T> Pair<Long, T>.verifyCache(
            action: suspend () -> T?
        ): Pair<Long, T?>? {
            val current = System.currentTimeMillis()
            return if (current - this@verifyCache.first < TIME_CACHE_EXPIRE &&
                this@verifyCache.second != null
            ) {
                null
            } else {
                val result = withContext(Dispatchers.Default) {
                    runCatching {
                        action.invoke()
                    }.getOrNull()
                }
                Pair(current, result)
            }
        }

        fun <T> Pair<Long, T>.verifyCacheNor(
            action: () -> T?
        ): Pair<Long, T?>? {
            val current = System.currentTimeMillis()
            return if (current - this@verifyCacheNor.first < TIME_CACHE_EXPIRE &&
                this@verifyCacheNor.second != null
            ) {
                null
            } else {
                val result = runCatching {
                    action.invoke()
                }.getOrNull()

                Pair(current, result)
            }
        }
    }

    object Utils {
        var cacheCountryCode: String = ""
    }
}