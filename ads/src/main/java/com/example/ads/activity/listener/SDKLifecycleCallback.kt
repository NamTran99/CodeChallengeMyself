package com.example.ads.activity.listener

import androidx.lifecycle.LifecycleOwner

interface SDKLifecycleCallback {
    fun onCreate(owner: LifecycleOwner?)

    fun onStart(owner: LifecycleOwner?)

    fun onResume(owner: LifecycleOwner?)

    fun onPause(owner: LifecycleOwner?)

    fun onStop(owner: LifecycleOwner?)

    fun onDestroy(owner: LifecycleOwner?)
}