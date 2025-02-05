package com.example.ads.activity.billing.dto

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

/**
 * Data class representing billing configuration data for a specific position on the screen.
 *
 * @property productId Product ID for purchase or subscription.
 * @property type Type of billing configuration, which can be 'sub' for subscription or 'inapp' for purchase.
 * @property position Position where the data will be displayed.
 * @property title Title of the specified position.
 * @property description Description of the specified position.
 * @property buttonTitle Button title for the specified position.
 */
@Keep
@Parcelize
data class IKSdkBillingDataDto(
    var productId: String? = null,
    var type: String? = null,
    var position: String? = null,
    var title: String? = null,
    var description: String? = null,
    var buttonTitle: String? = null
) : Parcelable
