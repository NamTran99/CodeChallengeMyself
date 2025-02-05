package com.example.ads.activity.listener.keep

/**
 * An interface that defines methods for retrieving lists of product IDs for different purchase scenarios.
 */
interface SDKIAPProductIDProvider {

    val enableIAPFunction: Boolean


    /**
     * Retrieves a list of product IDs for subscription products.
     *
     * @return An ArrayList of Strings containing the product IDs for subscription products.
     */
    fun listProductIDsSubscription(): ArrayList<String>{
        return arrayListOf()
    }

    /**
     * Retrieves a list of product IDs for one-time purchase products.
     *
     * @return An ArrayList of Strings containing the product IDs for one-time purchase products.
     */
    fun listProductIDsPurchase(): ArrayList<String>{
        return arrayListOf()
    }

    /**
     * Retrieves a list of product IDs for products that remove advertisements after purchase.
     *
     * @return An ArrayList of Strings containing the product IDs for ad-removal products.
     */
    fun listProductIDsRemoveAd(): ArrayList<String>{
        return arrayListOf()
    }

    /**
     * Retrieves a list of product IDs for products that can be purchased multiple times.
     *
     * @return An ArrayList of Strings containing the product IDs for multi-purchase products.
     */
    fun listProductIDsCanPurchaseMultiTime(): ArrayList<String>{
        return arrayListOf()
    }
}