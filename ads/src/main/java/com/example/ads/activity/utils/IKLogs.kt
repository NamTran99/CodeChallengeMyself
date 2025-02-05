package com.example.ads.activity.utils

import android.util.Log
//import com.example.ads.activity.BuildConfig
import com.example.ads.activity.core.IKDataCoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object IKLogs {

    private const val TAG = "ikLogs"
    private val supervisor = SupervisorJob()
    private val logScope = CoroutineScope(Dispatchers.IO + supervisor)
    private fun getMethodNames(sElements: Array<StackTraceElement>): Triple<String, String, String> {
        val element = sElements.getOrNull(4) ?: sElements.getOrNull(3) ?: sElements.getOrNull(2)?: sElements.getOrNull(1)
        val className = element?.fileName.orEmpty()
        val methodName = element?.methodName.orEmpty()
        val lineNumber = (element?.lineNumber ?: 0).toString()
        return Triple(className, methodName, lineNumber)
    }

    private fun createLog(methodName: String, lineNumber: String, log: String): String {
        return "[$methodName:$lineNumber] $log"
    }

    private fun log(
        level: Int,
        subTag: String,
        tag: String,
        shouldLog: Boolean,
        message: () -> String
    ) {
        if (!shouldLog) return
        val (className, methodName, lineNumber) = getMethodNames(Throwable().stackTrace)
        logScope.launch(supervisor) {
            runCatching {
                val logMessage =
                    createLog(
                        methodName,
                        lineNumber,
                        kotlin.runCatching { message() }.getOrNull() ?: ""
                    )
                when (level) {
                    Log.DEBUG -> Log.d("$TAG$subTag", "$tag: $className $logMessage")
                    Log.ERROR -> Log.e("$TAG$subTag", "$tag: $className $logMessage")
                    else -> {

                    }
                }
            }
        }
    }

    private fun logS(
        level: Int,
        subTag: String,
        tag: String,
        shouldLog: Boolean,
        message: () -> String
    ) {
        if (!shouldLog) return
        logScope.launch(supervisor) {
            runCatching {
                val logMessage = kotlin.runCatching { message() }.getOrNull() ?: ""
                when (level) {
                    Log.DEBUG -> Log.d("$TAG$subTag", "$tag: $logMessage")
                    Log.ERROR -> Log.e("$TAG$subTag", "$tag: $logMessage")
                    else -> {

                    }
                }
            }
        }
    }

    fun d(tag: String, level: Int = Log.DEBUG, message: () -> String) {
        if (IKDataCoreManager.ikDmLv == IKSdkDefConst.Logs.Level.LEVEL_TRACKING)
            return
//        log(level, "_d", tag, IKDataCoreManager.isEnableDebug(), message)
    }

    fun trackingLogSdk(tag: String, level: Int = Log.DEBUG, message: () -> String) {
//        log(level, "_tracking", tag, IKDataCoreManager.ikDmLvSdk || BuildConfig.DEBUG, message)
    }

    fun trackingLog(tag: String, level: Int = Log.DEBUG, message: () -> String) {
//        logS(level, "_tracking", tag, IKDataCoreManager.isEnableTracking(), message)
    }

    fun dSdk(tag: String, level: Int = Log.DEBUG, message: () -> String) {
//        log(level, "_sdk", tag, IKDataCoreManager.ikDmLvSdk || BuildConfig.DEBUG, message)
    }

    fun dNone(tag: String, message: () -> String) {
        log(Log.DEBUG, "_dn", tag, true, message)
    }

    fun dNoneSdk(tag: String, message: () -> String) {
        logS(Log.DEBUG, "", tag, true, message)
    }

    fun e(tag: String, message: () -> String) {
        if (IKDataCoreManager.ikDmLv == IKSdkDefConst.Logs.Level.LEVEL_TRACKING)
            return
//        log(Log.ERROR, "_e", tag, IKDataCoreManager.isEnableDebug(), message)
    }
}
