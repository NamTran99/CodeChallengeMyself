package com.example.ads.activity.core.service//package com.example.ads.activity.core.service
//
//import android.app.Service
//import android.content.Intent
//import android.os.IBinder
//import android.util.Log
//import com.example.ads.activity.control.IKSdkConnectInterface
//import com.example.ads.activity.core.IKDataCoreManager
//import com.example.ads.activity.data.local.IKSdkDataStore
//import com.example.ads.activity.data.local.IKSdkDataStoreConst
//import com.example.ads.activity.utils.IKSdkExt.launchWithSupervisorJob
//import com.example.ads.activity.utils.IkmSdkCoreFunc.SdkF.checkLogLevel
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//
//class IKSdkConnectService : Service() {
//
//    private var logLevel: Int = -1
//
//    private val binder = object : IKSdkConnectInterface.Stub() {
//        override fun ikLogLevel(): Int {
//            return logLevel
//        }
//
//        override fun setIkLogLevel(value: Int) {
//            Log.d("adadja",value.toString())
//            logLevel = value
//            checkLogLevel(value)
//            CoroutineScope(Dispatchers.IO).launchWithSupervisorJob {
//                IKSdkDataStore.putBoolean(
//                    IKSdkDataStoreConst.IK_SDK_DM,
//                    IKDataCoreManager.ikDmLv1
//                )
//            }
//        }
//    }
//
//    override fun onBind(intent: Intent?): IBinder {
//        return binder
//    }
//}