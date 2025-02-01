package com.example.ads.activity.core

import android.app.Application
import android.content.Context
import com.example.ads.activity.core.firebase.IKSdkFirebaseModule
import com.example.ads.activity.data.local.IKSdkDataStoreBillingCore
import com.example.ads.activity.data.local.IKSdkDataStoreCore
import com.example.ads.activity.utils.IKLogs
import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
import com.example.ads.activity.utils.IKSdkUtilsCore.getCountryCode
import com.example.ads.activity.utils.IkmSdkCoreFunc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object IKSdkApplicationProvider {
    private var appInstance: Application? = null
    private val mConfigJob = SupervisorJob()
    private val mConfigUiScope = CoroutineScope(Dispatchers.Main + mConfigJob)
//
//    private var firebaseRemote: IKFirebaseRemote = IKSdkFirebaseModule.provideIKFirebaseRemote()
//
    private fun onCreate(application: Application) {
        mConfigUiScope.launchWithSupervisorJob(Dispatchers.IO) {
//            firebaseRemote.initFirebase(application.applicationContext)
        }

        IKSdkDataStoreCore.create(application.applicationContext)
        IKSdkDataStoreBillingCore.create(application.applicationContext)
        mConfigUiScope.launchWithSupervisorJob {
            IkmSdkCoreFunc.AppF.initFirebase(application.applicationContext)
        }
        mConfigUiScope.launchWithSupervisorJob(Dispatchers.IO) {
            IkmSdkCoreFunc.AppF.initAdsApplicationSdk(
                application
            )
        }
        mConfigUiScope.launchWithSupervisorJob(Dispatchers.IO) {
            getCountryCode(application.applicationContext)
        }


        mConfigUiScope.launchWithSupervisorJob(Dispatchers.Default) {
//            showLog()
        }
    }

//    private fun showLog() {
//        val iKame = """
//            .
//             ___    _  __    ___    __  __   _____      ____    ____    _  __
//            |_ _|  | |/ /   / _ \  |  \/  | | ____|    / ___|  |  _ \  | |/ /
//             | |   | ' /   / /_\ \ | |\/| | |  _|      \___ \  | | | | | ' /
//             | |   | . \   |  _  | | |  | | | |___      ___) | | |_| | | . \
//            |___|  |_|\_\  |_| |_| |_|  |_| |_____|    |____/  |____/  |_|\_\
//            .
//        """.trimIndent()
//        IKLogs.dNoneSdk("IKSdk") { iKame }
//        IKLogs.dNoneSdk("IKSdk") { " :: Android SDK Version ::           ${BuildConfig.versionNameSdk}" }
//        IKLogs.dNoneSdk("IKSdk") { " :: SDK Data Source Version ::           ${IKDataSourceHelper.getArtifactIdSdk()}" }
//    }

    fun init(application: Application) {
        IKLogs.dNoneSdk("IKSdk") { "init sdk" }
        appInstance = application
        onCreate(application)
    }

    fun getApplication(): Application? {
        return appInstance
    }

    fun getContext(): Context? {
        return appInstance?.applicationContext
    }
//
//    fun getPackageName(): String {
//        return appInstance?.packageName ?: ""
//    }

}