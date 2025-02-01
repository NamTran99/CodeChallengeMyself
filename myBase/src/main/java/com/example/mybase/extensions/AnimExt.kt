package com.example.mybase.extensions

import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AnimationUtils
import com.example.mybase.R

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


fun View.fadeInFromRight(){
    val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in_from_right)
    startAnimation(animation)
}