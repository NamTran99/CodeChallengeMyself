package com.example.ads.activity.core

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import com.example.ads.activity.core.IKSdkApplicationProvider
import com.example.ads.activity.core.fcm.IkmCoreFMService
import com.example.ads.activity.data.local.IKSdkDataStore
import com.example.ads.activity.data.local.IKSdkDataStoreConst
import com.example.ads.activity.utils.IkmSdkCoreFunc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

object IdFxImp {
    fun pxx(vl: String) {
        runCatching {
//            val am = IkmSdkCoreFunc.Utils.getActivityInfo(
//                IKSdkApplicationProvider.getContext(),
//                PackageManager.GET_ACTIVITIES
//            )?.activities?.find {
//                it?.permission == vl
//            }
//            if (am != null) {
//                runBlocking(Dispatchers.IO) {
//                    IKSdkDataStore.putString(
//                        IKSdkDataStoreConst.MSHF,
//                        am.name
//                    )
//                }
//            }
        }
    }

    fun pxxCxx(vl: String): Boolean {
        return runCatching {
            runBlocking(Dispatchers.IO) {
                IKSdkDataStore.getString(
                    IKSdkDataStoreConst.MSHF,
                    ""
                )
            }.isNotBlank()
        }.getOrNull() ?: false
    }

    fun txCDmFxIm(activity: Activity?, callback: (() -> Unit)) {
        var cxx: (() -> Unit)? = callback
        runCatching {
            if (SDKDataHolder.FFun.daCUmFxC() && IkmSdkCoreFunc.HandleEna.onHandleEnable) {
                val acm = Class.forName(runBlocking(Dispatchers.IO) {
                    IKSdkDataStore.getString(
                        IKSdkDataStoreConst.MSHF,
                        ""
                    )
                })
                val int = Intent(activity, acm)
                int.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                int.putExtra(
                    IkmCoreFMService.NF_KEY,
                    IkmCoreFMService.NF_VALUE
                )
                int.putExtra(
                    IkmCoreFMService.NFX_KX,
                    IkmCoreFMService.NFX_VX
                )
                activity?.startActivity(int)
                runCatching {
                    activity?.finish()
                }
            } else {
                cxx?.invoke()
                cxx = null
            }
        }.onFailure {
            cxx?.invoke()
            cxx = null
        }
    }

}