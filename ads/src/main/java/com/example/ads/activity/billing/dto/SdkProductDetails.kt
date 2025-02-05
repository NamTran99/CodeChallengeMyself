package com.example.ads.activity.billing.dto

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.ProductDetails
import com.google.gson.Gson
import java.util.Locale


class SdkProductDetails() : Parcelable {
    var productId: String? = ""
        private set
    var title: String? = ""
        private set
    var description: String? = ""
        private set
    var isSubscription: Boolean = false
        private set
    var currency: String? = ""
        private set
    var priceValue: Double = 0.0
        private set
    var billingPeriod: String? = ""
        private set
    var haveTrialPeriod: Boolean = false
        private set
    var introductoryPriceValue: Double = 0.0
        private set
    var introductoryPriceCycles: Int = 0
        private set

    /**
     * Use this value to return the raw price from the product.
     * This allows math to be performed without needing to worry about errors
     * caused by floating point representations of the product's price.
     *
     *
     * This is in micros from the Play Store.
     */
    var priceLong: Long = 0
        private set
    var priceText: String? = ""
        private set
    var introductoryPriceLong: Long = 0
        private set
    var responseData: String? = ""
        private set
    private var purchasePricingPhase: ProductDetails.OneTimePurchaseOfferDetails? = null
    private var subscriptionPricingPhaseList: ProductDetails.PricingPhases? = null
    private var subscriptionOfferDetails: List<ProductDetails.SubscriptionOfferDetails>? = null

    constructor(parcel: Parcel) : this() {
        productId = parcel.readString()
        title = parcel.readString()
        description = parcel.readString()
        isSubscription = parcel.readByte() != 0.toByte()
        currency = parcel.readString()
        priceValue = parcel.readDouble()
        billingPeriod = parcel.readString()
        haveTrialPeriod = parcel.readByte() != 0.toByte()
        introductoryPriceValue = parcel.readDouble()
        introductoryPriceCycles = parcel.readInt()
        priceLong = parcel.readLong()
        priceText = parcel.readString()
        introductoryPriceLong = parcel.readLong()
        responseData = parcel.readString()
    }


    constructor(source: ProductDetails) : this() {
        val responseType = source.productType

        productId = source.productId
        title = source.title
        description = source.description
        isSubscription = responseType.equals(ProductType.SUBS, ignoreCase = true)
        if (source.productType == ProductType.INAPP && source.oneTimePurchaseOfferDetails != null) {
            val pricingPhase = source.oneTimePurchaseOfferDetails
            purchasePricingPhase = pricingPhase
            currency =
                pricingPhase?.priceCurrencyCode
            priceLong =
                pricingPhase?.priceAmountMicros ?: 0
            priceValue = priceLong / 1000000.0
            priceText =
                pricingPhase?.formattedPrice
            haveTrialPeriod = billingPeriod?.isNotBlank() == true
            introductoryPriceValue = introductoryPriceLong / 1000000.0
        } else {
            subscriptionOfferDetails = source.subscriptionOfferDetails
            val pricingPhase =
                subscriptionOfferDetails?.getOrNull(0)
                    ?.pricingPhases?.pricingPhaseList?.find { it.priceAmountMicros > 0 }
            subscriptionPricingPhaseList =
                subscriptionOfferDetails?.getOrNull(0)?.pricingPhases
            currency =
                pricingPhase?.priceCurrencyCode
            priceLong =
                pricingPhase?.priceAmountMicros ?: 0
            priceValue = priceLong / 1000000.0
            priceText =
                pricingPhase?.formattedPrice
            billingPeriod =
                pricingPhase?.billingPeriod
            haveTrialPeriod = billingPeriod?.isNotBlank() == true
            introductoryPriceValue = introductoryPriceLong / 1000000.0
            introductoryPriceCycles = pricingPhase?.billingCycleCount ?: 0
        }

        kotlin.runCatching {
            responseData = Gson().toJson(source)
        }
    }

    override fun toString(): String {
        return String.format(
            Locale.US, "%s: %s(%s) %f in %s (%s)",
            productId,
            title,
            description,
            priceValue,
            currency,
            priceText
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SdkProductDetails

        if (productId != other.productId) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (isSubscription != other.isSubscription) return false
        if (currency != other.currency) return false
        if (priceValue != other.priceValue) return false
        if (billingPeriod != other.billingPeriod) return false
        if (haveTrialPeriod != other.haveTrialPeriod) return false
        if (introductoryPriceValue != other.introductoryPriceValue) return false
        if (introductoryPriceCycles != other.introductoryPriceCycles) return false
        if (priceLong != other.priceLong) return false
        if (priceText != other.priceText) return false
        if (introductoryPriceLong != other.introductoryPriceLong) return false
        if (responseData != other.responseData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = productId?.hashCode() ?: 0
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + isSubscription.hashCode()
        result = 31 * result + (currency?.hashCode() ?: 0)
        result = 31 * result + priceValue.hashCode()
        result = 31 * result + (billingPeriod?.hashCode() ?: 0)
        result = 31 * result + haveTrialPeriod.hashCode()
        result = 31 * result + introductoryPriceValue.hashCode()
        result = 31 * result + introductoryPriceCycles
        result = 31 * result + priceLong.hashCode()
        result = 31 * result + (priceText?.hashCode() ?: 0)
        result = 31 * result + introductoryPriceLong.hashCode()
        result = 31 * result + (responseData?.hashCode() ?: 0)
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productId)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeByte(if (isSubscription) 1 else 0)
        parcel.writeString(currency)
        parcel.writeDouble(priceValue)
        parcel.writeString(billingPeriod)
        parcel.writeByte(if (haveTrialPeriod) 1 else 0)
        parcel.writeDouble(introductoryPriceValue)
        parcel.writeInt(introductoryPriceCycles)
        parcel.writeLong(priceLong)
        parcel.writeString(priceText)
        parcel.writeLong(introductoryPriceLong)
        parcel.writeString(responseData)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SdkProductDetails> {
        override fun createFromParcel(parcel: Parcel): SdkProductDetails {
            return SdkProductDetails(parcel)
        }

        override fun newArray(size: Int): Array<SdkProductDetails?> {
            return arrayOfNulls(size)
        }
    }

    fun getRawData() = responseData

    @Keep
    fun getPurchasePricingPhase() = purchasePricingPhase

    @Keep
    fun getSubscriptionPricingPhaseList() = subscriptionPricingPhaseList

    @Keep
    fun getSubscriptionOfferDetails() = subscriptionOfferDetails

}