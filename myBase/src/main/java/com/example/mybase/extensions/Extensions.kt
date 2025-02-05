package com.example.mybase.extensions

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.navOptions
import java.math.BigDecimal

fun String?.convertStringToNumber(): Float {
    return try {
        this?.replace("[^\\d.-]".toRegex(), "")?.toFloat() ?: 0f
    } catch (e: NumberFormatException) {
        0f
    }
}

fun String?.convertStringToDouble(): Double {
    return try {
        this?.replace("[^\\d.-]".toRegex(), "")?.toDouble() ?: "0".toDouble()
    } catch (e: NumberFormatException) {
        "0".toDouble()
    }
}

fun String?.convertStringToNumberToLong(): Long {
    return try {
        this?.replace("[^\\d.-]".toRegex(), "")?.toLong() ?: 0
    } catch (e: NumberFormatException) {
        0
    }
}

fun String?.convertStringToBigDecimalNumber(scale: Int? = null): BigDecimal {
    val result = try {
        this?.replace("[^\\d.-]".toRegex(), "")?.toBigDecimal() ?: "0".toBigDecimal()
    } catch (e: NumberFormatException) {
        "0".toBigDecimal()
    }
    if (scale != null && scale >= result.scale()) {
        return result.setScale(scale)
    }
    return result
}

fun NavController.navigateWithAnim(
    idDestination: Int,
    bundle: Bundle? = null,
    popupToId: Int? = null
) {
    val anim = navOptions {
//        anim {
//            popEnter = R.anim.slide_in_left
//            popExit = R.anim.slide_out_right
//            enter = R.anim.slide_in_right
//            exit = R.anim.slide_out_left
//        }
        popupToId?.let {
            popUpTo(popupToId) {
                inclusive = true
            }
        }
    }
    // TungVu - STABI-265 - 17/01/2023
    try {
        this.navigate(idDestination, bundle, anim)
    } catch (exception: Exception) {
    }
}