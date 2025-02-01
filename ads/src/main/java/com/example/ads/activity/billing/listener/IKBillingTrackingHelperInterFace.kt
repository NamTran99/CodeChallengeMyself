package com.example.ads.activity.billing.listener

interface IKBillingTrackingHelperInterFace {

    /**
     * Tracks the remote config load event.
     *
     * @param loadStatus Status of the load result (success or fail).
     * @param loadTime Load time in millisecond.
     */
    fun trackLoadRemoteConfig(loadStatus: String, loadTime: String)

    /**
     * Tracks the start of the price loading event.
     *
     */
    fun trackLoadPriceStart()

    /**
     * Tracks the completion of the price loading event.
     *
     * @param loadStatus Status of the load result (success or fail).
     * @param loadTime Load time in millisecond.
     * @param errorDetail Error details if the load fails, you can place error code here.
     */
    fun trackLoadPriceLoaded(
        loadStatus: String,
        loadTime: String,
        errorDetail: String? = null
    )

    /**
     * Tracks the event when a user accesses a premium screen.
     *
     * @param premiumScreenName Name of the premium screen accessed by the user.
     * @param screenFrom Name of the previous screen (from which screen the user entered the premium screen).
     */
    fun trackPremiumScreenOpen(premiumScreenName: String, screenFrom: String)

    /**
     * Tracks the event when a user selects a product.
     *
     * @param premiumScreenName Name of the premium screen the user is currently on.
     * @param productId Product ID (consistent with the ID obtained from the store).
     * @param productType Product type.
     * @param price Price of the product (as a string), maybe need div 1_000_000. Sample: 1.99, 19.99.
     * @param currency Currency of the product from the store.
     */
    fun trackPremiumProductSelect(
        premiumScreenName: String,
        productId: String,
        productType: String,
        price: String,
        currency: String
    )

    /**
     * Tracks the event when a user successfully completes a purchase.
     *
     * @param premiumScreenName Name of the premium screen the user is currently on.
     * @param productId Product ID (consistent with the ID obtained from the store).
     * @param productType Product type.
     * @param price Price of the product (as a string), maybe need div 1_000_000. Sample: 1.99, 19.99.
     * @param currency Currency of the product from the store.
     * @param purchaseStatus Purchase status (success, fail, cancel).
     * @param errorDetail Error details if the purchase fails, you can place error code here.
     */
    fun trackPremiumPurchaseFinish(
        premiumScreenName: String,
        productId: String,
        productType: String,
        price: String,
        currency: String,
        purchaseStatus: String,
        errorDetail: String? = null
    )

    /**
     * Tracks the restore event.
     *
     * @param premiumScreenName Name of the premium screen the user is currently on.
     * @param productId Product ID returned when the user restores (consistent with the ID obtained from the store).
     * @param productType Product name.
     * @param restoreStatus Status returned when the store succeeds or fails.
     * @param price Price of the product (as a string), maybe need div 1_000_000. Sample: 1.99, 19.99.
     * @param currency Currency of the product from the store.
     */
    fun trackPremiumRestore(
        premiumScreenName: String,
        productId: String,
        productType: String,
        restoreStatus: String,
        price: String,
        currency: String
    )
}