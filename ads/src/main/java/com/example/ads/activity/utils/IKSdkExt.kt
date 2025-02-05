package com.example.ads.activity.utils

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.ads.activity.core.SDKDataHolder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


object IKSdkExt {
    fun String.getStringEncrypt(): String {
        if (this.isBlank())
            return ""
        return try {
            var result = ""
            kotlin.runCatching {
                result = SDKDataHolder.getDefaultString(this) ?: IKSdkDefConst.EMPTY
            }
            result
        } catch (e: Exception) {
            ""
        }
    }

    fun Context.addViewLifecycleCallback(onPause: (() -> Unit), onResume: (() -> Unit)) {
        (this as? LifecycleOwner)?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                onPause.invoke()
                kotlin.runCatching {
                    (this@addViewLifecycleCallback as? LifecycleOwner)?.lifecycle?.removeObserver(
                        this
                    )
                }
            }

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                onResume.invoke()
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                onPause.invoke()
            }
        })
    }


    fun String.subStringEvent(): MutableList<String> {
        return if (this.length > 99) {
            val newSub = this.filterNot { it.isWhitespace() }
            newSub.chunked(99).toMutableList()
        } else mutableListOf(this)
    }

    fun Boolean?.log(): String {
        return if (this == true) "filled" else "empty"
    }

    fun CoroutineScope.launchWithSupervisorJob(
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return this.launch(SupervisorJob()) {
            block()
        }
    }

    fun CoroutineScope.launchWithSupervisorJob(
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return this.launch(SupervisorJob() + dispatcher) {
            block()
        }
    }
}