package com.example.mybase.helper

import android.os.Build
import android.os.Looper
import androidx.annotation.ChecksSdkIntAtLeast

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun isUpsideDownCakePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE