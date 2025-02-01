package com.example.mybase.extensions

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.fragment.app.Fragment

fun Context.getWindowManager(): WindowManager = getSystemService(WindowManager::class.java)
fun Context.getScreenSize(): Pair<Float, Float> {
    val displayMetrics = DisplayMetrics()
    this.getWindowManager().defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels.toFloat() to displayMetrics.heightPixels.toFloat()
}

