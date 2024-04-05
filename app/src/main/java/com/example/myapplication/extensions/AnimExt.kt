package com.example.myapplication.extensions

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.util.DisplayMetrics
import android.view.View

fun View.moveViewToRight(duration: Long = 1000) {
    val displayMetrics = DisplayMetrics()
    val (screenWidth, screenHeight) = context.getScreenSize()
    animate().translationX(screenWidth.toFloat()).setDuration(duration).start()
}

fun View.moveViewFromBottom(duration: Long = 1000) {
    val displayMetrics = DisplayMetrics()
    val (screenWidth, screenHeight) = context.getScreenSize()
    translationY = screenHeight
    animate().translationY(0f).setDuration(duration).start()
}
