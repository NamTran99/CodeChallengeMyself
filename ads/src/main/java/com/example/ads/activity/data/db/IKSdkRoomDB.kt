package com.example.ads.activity.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ads.activity.IKSdkConstants
import com.example.ads.activity.data.converter.IKAdUnitDtoConverter
import com.example.ads.activity.data.converter.IKAdapterDtoConverter
import com.example.ads.activity.data.converter.IKListStringConverter
import com.example.ads.activity.data.converter.IKSdkAdDefaultConfigConverter
import com.example.ads.activity.data.converter.IKSdkBaseDtoConverter
import com.example.ads.activity.data.converter.IKSdkCustomNCLConverter
import com.example.ads.activity.data.converter.IKSdkOpenDtoConverter
import com.example.ads.activity.data.converter.IKSdkProdInterDetailDtoConverter
import com.example.ads.activity.data.converter.IKSdkProdOpenDetailDtoConverter
import com.example.ads.activity.data.converter.IKSdkProdRewardDetailDtoConverter
import com.example.ads.activity.data.converter.IKSdkProdWidgetDetailDtoConverter
import com.example.ads.activity.data.dto.sdk.IKGkAdDto
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
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdInterDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdOpenDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdRewardDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkProdWidgetDto
import com.example.ads.activity.data.dto.sdk.data.IKSdkRewardDto
import com.example.ads.activity.data.dto.sdk.data.UserBillingDetail

@Database(
    entities = [
        UserBillingDetail::class,
        IKSdkDataOpLocalDto::class, IKSdkFirstAdDto::class,
        IKSdkOpenDto::class, IKSdkBannerDto::class,
        IKSdkNativeDto::class, IKSdkBackupAdDto::class,
        IKSdkRewardDto::class, IKSdkInterDto::class,
        IKSdkProdOpenDto::class, IKSdkProdWidgetDto::class,
        IKSdkProdInterDto::class, IKSdkProdRewardDto::class,
        IKGkAdDto::class, IKSdkCustomNCLDto::class,
        IKSdkMRECDto::class, IKSdkBannerInlineDto::class,
        IKSdkBannerCollapseDto::class,
        IKSdkNativeFullScreenDto::class,
        IKSdkAudioIconDto::class,
        IKSdkBannerCollapseCustomDto::class,
    ],
    version = IKSdkConstants.DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(
    IKAdUnitDtoConverter::class,
    IKAdapterDtoConverter::class,
    IKSdkOpenDtoConverter::class,
    IKSdkProdOpenDetailDtoConverter::class,
    IKSdkProdRewardDetailDtoConverter::class,
    IKSdkProdWidgetDetailDtoConverter::class,
    IKSdkProdInterDetailDtoConverter::class,
    IKSdkCustomNCLConverter::class,
    IKListStringConverter::class,
    IKSdkAdDefaultConfigConverter::class,
    IKSdkBaseDtoConverter::class,
)
abstract class IKSdkRoomDB : RoomDatabase() {
    abstract fun commonAdsDao(): IKSdkDbDAO

    companion object {
        @Volatile
        private var instance: IKSdkRoomDB? = null

        fun getInstance(context: Context): IKSdkRoomDB {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context, IKSdkRoomDB::class.java,
                    "ikm_sdk_database.db"
                ).fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
        }
    }
}
