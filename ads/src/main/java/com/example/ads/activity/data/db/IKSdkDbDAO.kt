package com.example.ads.activity.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

@Dao
interface IKSdkDbDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllUserBilling(listDto: List<UserBillingDetail>)

    @Query("DELETE FROM ik_sdk_default_config")
    fun deleteAllBackup()

    @Query("SELECT * FROM ik_sdk_default_config")
    fun getBackupDto(): IKSdkBackupAdDto?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBackup(dto: IKSdkBackupAdDto)

    @Query("DELETE FROM ik_sdk_gk")
    fun deleteIKGkAdDto()

    @Query("SELECT * FROM ik_sdk_gk")
    fun getIKGkAdDto(): IKGkAdDto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIKGkAdDto(dto: IKGkAdDto)

    @Query("DELETE FROM ik_sdk_user_billing_config")
    fun deleteAllUserBilling()

    @Query("SELECT * FROM ik_sdk_user_billing_config")
    fun getAllUserBilling(): List<UserBillingDetail>?

    @Query("SELECT * FROM ik_sdk_user_billing_config WHERE orderId = :orderId")
    fun getUserBillingDto(orderId: String): UserBillingDetail?


    //sdk local
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllOpenDefault(listDto: List<IKSdkDataOpLocalDto>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOpenDefault(dto: IKSdkDataOpLocalDto)

    @Query("SELECT * FROM ik_sdk_data_o_lc")
    fun getOpenDefaultDto(): IKSdkDataOpLocalDto?

    @Query("SELECT * FROM ik_sdk_data_o_lc")
    fun getAllOpenDefault(): List<IKSdkDataOpLocalDto>

    @Query("DELETE FROM ik_sdk_data_o_lc")
    fun deleteAllDefaultOpen()

    //inter
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSDKInter(dto: IKSdkInterDto)

    @Query("SELECT * FROM ik_sdk_inter_config")
    fun getSDKInter(): IKSdkInterDto?

    @Query("DELETE FROM ik_sdk_inter_config")
    fun deleteAllSDKInter()

    //open
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSDKOpen(dto: IKSdkOpenDto)

    @Query("SELECT * FROM ik_sdk_open_config")
    fun getSDKOpen(): IKSdkOpenDto?

    @Query("DELETE FROM ik_sdk_open_config")
    fun deleteAllSDKOpen()

    //native
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSDKNative(dto: IKSdkNativeDto)

    @Query("SELECT * FROM ik_sdk_native_config")
    fun getSDKNative(): IKSdkNativeDto?

    @Query("DELETE FROM ik_sdk_native_config")
    fun deleteAllSDKNative()

    //banner
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSDKBanner(dto: IKSdkBannerDto)

    @Query("SELECT * FROM ik_sdk_banner_config")
    fun getSDKBanner(): IKSdkBannerDto?

    @Query("DELETE FROM ik_sdk_banner_config")
    fun deleteAllSDKBanner()

    //reward
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSDKReward(dto: IKSdkRewardDto)

    @Query("SELECT * FROM ik_sdk_reward_config")
    fun getSDKReward(): IKSdkRewardDto?

    @Query("DELETE FROM ik_sdk_reward_config")
    fun deleteAllSDKReward()

    //first ad
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSDKFirstAd(dto: IKSdkFirstAdDto)

    @Query("SELECT * FROM ik_prod_first_config")
    fun getSDKFirstAd(): IKSdkFirstAdDto?

    @Query("DELETE FROM ik_prod_first_config")
    fun deleteAllSDKFirstAd()

    //ik_prod_open_config
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConfigOpen(dto: IKSdkProdOpenDto)

    @Query("SELECT * FROM ik_prod_open_config")
    fun getConfigOpen(): IKSdkProdOpenDto?

    @Query("DELETE FROM ik_prod_open_config")
    fun deleteAllSDKConfigOpen()

    //ik_prod_reward_config
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConfigReward(dto: IKSdkProdRewardDto)

    @Query("SELECT * FROM ik_prod_reward_config")
    fun getConfigReward(): IKSdkProdRewardDto?

    @Query("DELETE FROM ik_prod_reward_config")
    fun deleteAllSDKConfigReward()

    //ik_prod_inter_config
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConfigInter(dto: IKSdkProdInterDto)

    @Query("SELECT * FROM ik_prod_inter_config")
    fun getConfigInter(): IKSdkProdInterDto?

    @Query("DELETE FROM ik_prod_inter_config")
    fun deleteAllSDKConfigInter()

    //ik_prod_widget_config

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConfigWidget(dto: IKSdkProdWidgetDto)

    @Query("SELECT * FROM ik_prod_widget_config")
    fun getConfigWidget(): IKSdkProdWidgetDto?

    @Query("DELETE FROM ik_prod_widget_config")
    fun deleteAllSDKConfigWidget()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConfigNCL(dto: IKSdkCustomNCLDto)

    @Query("SELECT * FROM ik_sdk_custom_ncl_config")
    fun getConfigNCL(): IKSdkCustomNCLDto?

    @Query("DELETE FROM ik_sdk_custom_ncl_config")
    fun deleteAllSDKConfigNCL()

    //bannerInline
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSDKBannerInline(dto: IKSdkBannerInlineDto)

    @Query("SELECT * FROM ik_sdk_banner_inline_config")
    fun getSDKBannerInline(): IKSdkBannerInlineDto?

    @Query("DELETE FROM ik_sdk_banner_inline_config")
    fun deleteAllSDKBannerInline()

    //MREC
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSDKMREC(dto: IKSdkMRECDto)

    @Query("SELECT * FROM ik_sdk_mrec_config")
    fun getSDKMREC(): IKSdkMRECDto?

    @Query("DELETE FROM ik_sdk_mrec_config")
    fun deleteAllSDKMREC()

    //banner collapse
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSDKBannerCollapse(dto: IKSdkBannerCollapseDto)

    @Query("SELECT * FROM ik_sdk_banner_cl_config")
    fun getSDKBannerCollapse(): IKSdkBannerCollapseDto?

    @Query("DELETE FROM ik_sdk_banner_cl_config")
    fun deleteAllSDKBannerCollapse()

    //native full screen
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSDKNativeFullScreen(dto: IKSdkNativeFullScreenDto)

    @Query("SELECT * FROM ik_sdk_native_fs_config")
    fun getSDKNativeFullScreen(): IKSdkNativeFullScreenDto?

    @Query("DELETE FROM ik_sdk_native_fs_config")
    fun deleteAllSDKNativeFullScreen()

    //AudioIconD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSDKAudioIcon(dto: IKSdkAudioIconDto)

    @Query("SELECT * FROM ik_sdk_audio_icon")
    fun getSDKAudioIcon(): IKSdkAudioIconDto?

    @Query("DELETE FROM ik_sdk_audio_icon")
    fun deleteAllSDKAudioIcon()

    //banner collapse
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSDKBannerCollapseCustom(dto: IKSdkBannerCollapseCustomDto)

    @Query("SELECT * FROM ik_sdk_banner_cl_custom")
    fun getSDKBannerCollapseCustom(): IKSdkBannerCollapseCustomDto?

    @Query("DELETE FROM ik_sdk_banner_cl_custom")
    fun deleteAllSDKBannerCollapseCustom()

}
