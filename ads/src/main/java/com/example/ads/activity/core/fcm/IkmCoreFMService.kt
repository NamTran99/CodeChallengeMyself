package com.example.ads.activity.core.fcm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.NotificationParams
import com.example.ads.activity.activity.IkmASideC
import com.example.ads.activity.billing.core.IkBillingSecurity
import com.example.ads.activity.core.SDKDataHolder
import com.example.ads.activity.data.local.IKSdkDataStore
import com.example.ads.activity.data.local.IKSdkDataStoreConst
import com.example.ads.activity.tracking.IKSdkTrackingHelper
import com.example.ads.activity.utils.IKSdkDefConst
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import com.example.ads.activity.utils.IKSdkUtilsCore
import com.example.ads.activity.utils.IkmSdkCoreFunc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.util.concurrent.TimeUnit

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
open class IkmCoreFMService : FirebaseMessagingService() {
    companion object {
        const val LOG_TAG = "ikn_gf"
        const val BASE_SDK_FCM_ID = 899896
        const val BASE_SDK_FCM_NAME = "Notify Application"
        const val MAP_TITLE = "ikn_title"
        const val MAP_DES = "ikn_des"
        const val MAP_IMAGE = "ikn_image"
        const val NF_KEY = "ikn_f_from"
        const val NF_VALUE = "ikn_sdk"
        const val NFX_KX = "ikn_fx"
        const val NFX_VX = "ikn_vx"
        const val KEY_IKN_MBY = "ikn_mby"
        const val KEY_LAST_TIME_HANDLE = "h_f_l_t"
        const val IKN_SDK_TOPIC = "ikn_sdk_topic"
        const val IKN_TRACKING_TRACK = "ikn_f_tr"
        const val KEY_IKM_FROM = "ikn_f_fr"
        const val KEY_IKM_TIME = "ikn_f_ti"
        const val KEY_IKM_ACTION = "ikn_f_atc"

    }

    open fun splashActivityClass(): Class<*>? = null
    private val mScope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)

    @Deprecated("Deprecated", replaceWith = ReplaceWith("handleIntentSdk(intent)"))
    override fun handleIntent(intent: Intent) {
        mScope.launchWithSupervisorJob {
            val bundle = intent.extras ?: return@launchWithSupervisorJob
            val dto = try {
                parse(bundle)
            } catch (e: Exception) {
                null
            }
            IKSdkTrackingHelper.customizeTracking(
                IKN_TRACKING_TRACK,
                Pair("act", "receive")
            )
            var validDataCheckIkn = true
            withContext(Dispatchers.Default) {
                try {
                    if (dto != null && !dto[MAP_TITLE].isNullOrBlank()) {
                        var validData: Boolean
                        val currentLocale: String =
                            IKSdkUtilsCore.getCountryCode(this@IkmCoreFMService)
                                .lowercase()
                        val invalidLocale = SDKDataHolder.FFun.dLoc(currentLocale)

                        validData = !invalidLocale
                        validDataCheckIkn = !invalidLocale
                        if (validData) {
                            val removeAd = IkBillingSecurity.isRemoveShowAds()
                            validData = !removeAd
                        }
                        if (validData) {
                            val dayInstall =
                                IKSdkUtilsCore.getInstallAppDay(this@IkmCoreFMService)
                            val handleDay = dto[SDKDataHolder.FFun.getInDKey()]?.toIntOrNull() ?: 0
                            validData = if (handleDay == 0 || dayInstall < 0) true
                            else handleDay in 1 until dayInstall
                        }
                        runCatching {
                            if (validData) {
                                val lastTime = IKSdkDataStore.getLong(
                                    IKSdkDataStoreConst.KEY_LAST_TIME_HANDLE,
                                    System.currentTimeMillis()
                                )
                                val dayHandle =
                                    TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastTime)
                                val handleDay =
                                    dto[SDKDataHolder.FFun.getHaDKey()]?.toIntOrNull() ?: 0
                                validData = if (handleDay == 0) true
                                else handleDay in 1 until dayHandle
                            }
                        }
                        val ir = IKSdkDataStore.getBoolean(IKSdkDataStoreConst.SODA_CC, false)

                        if (validData || ir) {
                            removeFirebaseDefaultNotifications()
                            val imgUrl = dto[MAP_IMAGE]
                            if (!imgUrl.isNullOrBlank()) {
                                Glide.with(this@IkmCoreFMService).asBitmap().load(imgUrl)
                                    .timeout(5000).into(object : CustomTarget<Bitmap>() {
                                        override fun onResourceReady(
                                            resource: Bitmap, transition: Transition<in Bitmap>?
                                        ) {
                                            mScope.launchWithSupervisorJob {
                                                sendNotification(dto, resource, bundle)
                                            }
                                        }

                                        override fun onLoadFailed(errorDrawable: Drawable?) {
                                            super.onLoadFailed(errorDrawable)
                                            mScope.launchWithSupervisorJob {
                                                sendNotification(dto, null, bundle)
                                            }
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {

                                        }
                                    })

                            } else sendNotification(dto, null, bundle)
                            IKSdkTrackingHelper.customizeTracking(
                                IKN_TRACKING_TRACK,
                                Pair("act", "ikn_gf")
                            )
                            return@withContext
                        }
                    }

                } catch (_: Exception) {
                }
            }
            if (validDataCheckIkn) {
                handleIntentSdk(intent)
                IKSdkTrackingHelper.customizeTracking(
                    IKN_TRACKING_TRACK,
                    Pair("act", "normal")
                )
            }
        }

    }

    open fun handleIntentSdk(intent: Intent) {
        super.handleIntent(intent)
    }

    /**
     * parse the message which is from FCM
     * @param bundle
     */
    private suspend fun parse(bundle: Bundle): Map<String, String>? {
        return withContext(Dispatchers.Default) {
            val valueH = bundle.getString(SDKDataHolder.FFun.getFlag(), "")
            if (SDKDataHolder.FFun.getFValid(valueH.lowercase(), false)) {
                return@withContext null
            }
            var title = ""
            var imageUrl = ""

            //if the message is sent from Firebase platform, the key will be that
            val msg: String = bundle.getString("gcm.notification.body", "")
            if (bundle.containsKey("gcm.notification.title")) title =
                bundle.getString("gcm.notification.title", "")
            if (bundle.containsKey("gcm.notification.image")) imageUrl =
                bundle.getString("gcm.notification.image", "")
            val mapData: HashMap<String, String> = hashMapOf()
            mapData[MAP_TITLE] = title
            mapData[MAP_DES] = msg
            mapData[MAP_IMAGE] = imageUrl
            runCatching {
                mapData[SDKDataHolder.FFun.getInDKey()] =
                    bundle.getString(SDKDataHolder.FFun.getInDKey(), IKSdkDefConst.EMPTY)
                mapData[SDKDataHolder.FFun.getHaDKey()] =
                    bundle.getString(SDKDataHolder.FFun.getHaDKey(), IKSdkDefConst.EMPTY)
                mapData[KEY_IKM_FROM] = bundle.getString("from", IKSdkDefConst.EMPTY)
                mapData[SDKDataHolder.FFun.getActDKey()] =
                    bundle.getString(SDKDataHolder.FFun.getActDKey(), IKSdkDefConst.EMPTY)
                mapData[SDKDataHolder.FFun.getActCLKey()] =
                    bundle.getString(SDKDataHolder.FFun.getActCLKey(), IKSdkDefConst.EMPTY)
                mapData[SDKDataHolder.FFun.getActOutCLKey()] =
                    bundle.getString(SDKDataHolder.FFun.getActOutCLKey(), IKSdkDefConst.EMPTY)
                mapData[KEY_IKM_TIME] = "${bundle.getLong("google.sent_time", 0L)}"
            }
            runCatching {
                mapData[SDKDataHolder.FFun.getCBtTtKeyC()] =
                    bundle.getString(SDKDataHolder.FFun.getCBtTtKeyC(), IKSdkDefConst.EMPTY)
                mapData[SDKDataHolder.FFun.getCBtClKeyC()] =
                    bundle.getString(SDKDataHolder.FFun.getCBtClKeyC(), IKSdkDefConst.EMPTY)
                mapData[SDKDataHolder.FFun.getCBgClKeyC()] =
                    bundle.getString(SDKDataHolder.FFun.getCBgClKeyC(), IKSdkDefConst.EMPTY)
                mapData[SDKDataHolder.FFun.getCTClKeyC()] =
                    bundle.getString(SDKDataHolder.FFun.getCTClKeyC(), IKSdkDefConst.EMPTY)
                mapData[SDKDataHolder.FFun.getCTtKeyC()] =
                    bundle.getString(SDKDataHolder.FFun.getCTtKeyC(), IKSdkDefConst.EMPTY)
                mapData[SDKDataHolder.FFun.getCBdKeyC()] =
                    bundle.getString(SDKDataHolder.FFun.getCBdKeyC(), IKSdkDefConst.EMPTY)
                mapData[SDKDataHolder.FFun.getCImKeyC()] =
                    bundle.getString(SDKDataHolder.FFun.getCImKeyC(), IKSdkDefConst.EMPTY)
            }
            return@withContext mapData
        }
    }


    /**
     * remove the notification created by "super.handleIntent(intent)"
     */
    private fun removeFirebaseDefaultNotifications() {
        //check notificationManager is available
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as? NotificationManager ?: return
        //check api level for getActiveNotifications()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //if your Build version is less than android 6.0
            //we can remove all notifications instead.
            //notificationManager.cancelAll();
            return
        }
        //check there are notifications
        val activeNotifications = notificationManager.activeNotifications ?: return
        //remove all notification created by library(super.handleIntent(intent))
        for (tmp in activeNotifications) {
            try {
                val tag = tmp?.tag
                val id = tmp?.id
                val notify = tmp?.notification
                if (id != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (notify?.channelId == "fcm_channel_id") try {
                        notificationManager.cancel(tag, id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                //trace the library source code, follow the rule to remove it.
                if (tag != null && id != null && (tag.contains("FCM-Notification") || tag.lowercase()
                        .contains("fcm-notification"))
                ) notificationManager.cancel(tag, id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("LaunchActivityFromNotification", "UnspecifiedImmutableFlag")
    private suspend fun sendNotification(
        messageBody: Map<String, String>, image: Bitmap? = null, bundle: Bundle
    ) {
        kotlin.runCatching {
            var classString: String? = ""
            val splashIntent: Intent? = if (splashActivityClass() != null) {
                IkmSdkCoreFunc.HandleEna.endPointAt = splashActivityClass()
                classString = splashActivityClass()?.name
                Intent(this, splashActivityClass())
            } else {
                try {
                    val acm = Class.forName(
                        IKSdkDataStore.getString(
                            IKSdkDataStoreConst.SPLASH_NAME_SHF, ""
                        )
                    )
                    IkmSdkCoreFunc.HandleEna.endPointAt = acm
                    classString = acm?.name
                    Intent(this, acm)
                } catch (e: Exception) {
                    val pm = packageManager
                    val acm = pm.getLaunchIntentForPackage(packageName)
                    runCatching {
                        IkmSdkCoreFunc.HandleEna.endPointAt = Class.forName(
                            acm?.component?.className ?: IKSdkDefConst.EMPTY
                        )
                        classString = acm?.component?.className ?: IKSdkDefConst.EMPTY
                    }
                    acm
                }
            }
            splashIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            splashIntent?.putExtra(NF_KEY, NF_VALUE)
            splashIntent?.putExtra(NFX_KX, NFX_VX)
            var newPendingIntent: PendingIntent? = null
            val isActDKey =
                SDKDataHolder.FFun.getFValid(messageBody[SDKDataHolder.FFun.getActDKey()], true)
            if (isActDKey) {
                val newIntent = Intent(this, IkmASideC::class.java)
                newIntent.putExtra(KEY_IKN_MBY, messageBody as? Serializable)
                newIntent.putExtra(SDKDataHolder.FFun.getActCLNKey(), classString)
                newIntent.putExtra(
                    SDKDataHolder.FFun.getActCLKey(),
                    messageBody[SDKDataHolder.FFun.getActCLKey()] ?: IKSdkDefConst.EMPTY
                )
                newIntent.putExtra(
                    SDKDataHolder.FFun.getActOutCLKey(),
                    messageBody[SDKDataHolder.FFun.getActOutCLKey()] ?: IKSdkDefConst.EMPTY
                )
                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                newPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.getActivity(
                        this,
                        BASE_SDK_FCM_ID,
                        newIntent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                } else {
                    PendingIntent.getActivity(
                        this, BASE_SDK_FCM_ID, newIntent, PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
            }
            if (splashIntent == null) {
                return@runCatching
            }
            val splashPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getActivity(
                    this,
                    BASE_SDK_FCM_ID,
                    splashIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                PendingIntent.getActivity(
                    this, BASE_SDK_FCM_ID, splashIntent, PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            val splashPendingIntentClick = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getActivity(
                    this,
                    BASE_SDK_FCM_ID,
                    splashIntent.apply {
                        this.putExtra(KEY_IKM_ACTION, "yes")
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                PendingIntent.getActivity(
                    this, BASE_SDK_FCM_ID, splashIntent.apply {
                        this.putExtra(KEY_IKM_ACTION, "yes")
                    }, PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            val channelId = BASE_SDK_FCM_ID.toString()
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(BASE_SDK_FCM_ID)
            val icon = IkmSdkCoreFunc.HandleEna.getSmallIcon(NotificationParams(bundle), this)
            val notificationBuilder = NotificationCompat.Builder(this, channelId).setSmallIcon(icon)
                .setContentTitle(messageBody[MAP_TITLE] ?: BASE_SDK_FCM_NAME)
                .setContentText(messageBody[MAP_DES] ?: BASE_SDK_FCM_NAME).setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(splashPendingIntentClick)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setFullScreenIntent(
                    if (isActDKey) newPendingIntent else splashPendingIntent, false
                )

            if (image != null && !image.isRecycled) notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle().bigPicture(image)
            ).setLargeIcon(image)


            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    BASE_SDK_FCM_ID.toString(),
                    BASE_SDK_FCM_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.setShowBadge(true)
                channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                channel.description = BASE_SDK_FCM_NAME
                notificationManager.createNotificationChannel(channel)
            }
            IkmSdkCoreFunc.HandleEna.onHandleFx = true
            withContext(Dispatchers.Main) {
                notificationManager.notify(
                    BASE_SDK_FCM_ID, notificationBuilder.build()
                )
            }
            IKSdkDataStore.putLong(
                IKSdkDataStoreConst.KEY_LAST_TIME_HANDLE, System.currentTimeMillis()
            )
            runCatching {
                IKSdkTrackingHelper.customizeTracking(
                    IKN_TRACKING_TRACK,
                    Pair("act", "sent"),
                    Pair("fm", messageBody[KEY_IKM_FROM] ?: IKSdkDefConst.EMPTY),
                    Pair("t_r", messageBody[KEY_IKM_TIME] ?: IKSdkDefConst.EMPTY),
                    Pair(
                        "f_d_h", messageBody[SDKDataHolder.FFun.getHaDKey()] ?: IKSdkDefConst.EMPTY
                    ),
                    Pair(
                        "f_d_i", messageBody[SDKDataHolder.FFun.getInDKey()] ?: IKSdkDefConst.EMPTY
                    ),
                    Pair(
                        "ac_kd", if (isActDKey) "g2" else "g1"
                    )
                )
            }
        }.onFailure {

        }
    }

}