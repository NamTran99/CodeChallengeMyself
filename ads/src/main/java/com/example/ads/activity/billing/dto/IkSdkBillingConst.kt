package com.example.ads.activity.billing.dto

object IkSdkBillingConst {

    const val PRODUCT_TYPE_MANAGED = "inapp"
    const val PRODUCT_TYPE_SUBSCRIPTION = "subs"
    const val RESPONSE_ORDER_ID = "orderId"
    const val RESPONSE_PRODUCT_ID = "productId"
    const val RESPONSE_PACKAGE_NAME = "packageName"
    const val RESPONSE_PURCHASE_TIME = "purchaseTime"
    const val RESPONSE_PURCHASE_STATE = "purchaseState"
    const val RESPONSE_PURCHASE_TOKEN = "purchaseToken"
    const val OBFUSCATED_EXTERNAL_ACCOUNT_ID = "obfuscatedExternalAccountId"
    const val OBFUSCATED_EXTERNAL_PROFILE_ID = "obfuscatedExternalProfileId"
    const val RESPONSE_TYPE = "type"
    const val RESPONSE_TITLE = "title"
    const val RESPONSE_DESCRIPTION = "description"
    const val RESPONSE_PRICE = "price"
    const val RESPONSE_PRICE_CURRENCY = "price_currency_code"
    const val RESPONSE_PRICE_MICROS = "price_amount_micros"
    const val RESPONSE_SUBSCRIPTION_PERIOD = "subscriptionPeriod"
    const val RESPONSE_AUTO_RENEWING = "autoRenewing"
    const val RESPONSE_FREE_TRIAL_PERIOD = "freeTrialPeriod"
    const val RESPONSE_INTRODUCTORY_PRICE = "introductoryPrice"
    const val RESPONSE_INTRODUCTORY_PRICE_MICROS = "introductoryPriceAmountMicros"
    const val RESPONSE_INTRODUCTORY_PRICE_PERIOD = "introductoryPricePeriod"
    const val RESPONSE_INTRODUCTORY_PRICE_CYCLES = "introductoryPriceCycles"

    object ReplacementMode {
        var UNKNOWN_REPLACEMENT_MODE = 0
        var WITH_TIME_PRORATION = 1
        var CHARGE_PRORATED_PRICE = 2
        var WITHOUT_PRORATION = 3
        var CHARGE_FULL_PRICE = 5
        var DEFERRED = 6
    }

}