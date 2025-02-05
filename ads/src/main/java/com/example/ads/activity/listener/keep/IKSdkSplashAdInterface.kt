package com.example.ads.activity.listener.keep

import android.app.Activity
import androidx.lifecycle.Lifecycle
import com.example.ads.activity.listener.pub.IKLoadAdListener
import com.example.ads.activity.listener.pub.IKNoneSplashAdListener
import com.example.ads.activity.listener.pub.IKShowAdListener
import kotlinx.coroutines.Job

/**
 * Interface for interacting with splash screen ads.
 *
 * This interface provides methods for displaying and loading splash screen ads.
 */
interface IKSdkSplashAdInterface {

    /**
     * Attaches the lifecycle of an Activity to the splash ad.
     *
     * This method should be called to ensure that the splash ad is properly handled
     * throughout the Activity's lifecycle.
     *
     * @param life The lifecycle of the Activity.
     */
    fun attachLifecycle(life: Lifecycle)

    /**
     * Shows the splash screen ad.
     *
     * This method displays the splash screen ad. It is a suspending function, so it
     * should be called from a coroutine.
     *
     * @param activity The Activity on which to display the ad.
     * @param listener A listener to receive events related to the ad display.
     */
    suspend fun showSplashScreenAd(
        activity: Activity?,
        listener: IKShowAdListener?
    )

    /**
     * Shows the splash screen ad synchronously.
     *
     * This method displays the splash screen ad synchronously. It should be called
     * from the main thread.
     *
     * @param activity The Activity on which to display the ad.
     * @param listener A listener to receive events related to the ad display.
     */
    fun showSplashScreenAdNor(
        activity: Activity?,
        listener: IKShowAdListener?
    )

    /**
     * Loads and shows the splash screen ad synchronously.
     *
     * This method loads and displays the splash screen ad synchronously. It should be called
     * from the main thread.
     *
     * @param activity The Activity on which to display the ad.
     * @param listener A listener to receive events related to the ad display.
     */
    fun loadAndShowSplashScreenAdNonAsync(activity: Activity?, listener: IKShowAdListener?)

    /**
     * Loads and shows the splash screen ad.
     *
     * This method loads and displays the splash screen ad. It is a suspending function, so it
     * should be called from a coroutine.
     *
     * @param activity The Activity on which to display the ad.
     * @param listener A listener to receive events related to the ad display.
     * @return A Job representing the ad loading and display operation.
     */
    suspend fun loadAndShowSplashScreenAd(
        activity: Activity?,
        listener: IKShowAdListener?
    ): Job?

    /**
     * Loads the splash screen ad.
     *
     * This method loads the splash screen ad without displaying it.
     *
     * @param activity The Activity on which to load the ad.
     * @param listener A listener to receive events related to the ad loading.
     */
    fun loadSplashScreenAd(activity: Activity?, listener: IKLoadAdListener?)

    /**
     * Notifies that the splash ad will not be shown.
     *
     * This method is called when the splash ad is not shown, for example,
     * if the ad is not available or if the user has opted out of seeing ads.
     *
     * @param activity The Activity on which the ad would have been shown.
     * @param listener A listener to receive events related to the ad not being shown.
     */
    fun noneShowSplashAd(activity: Activity?, listener: IKNoneSplashAdListener)

    /**
     * Destroys the splash ad.
     *
     * This method should be called to release any resources held by the splash ad.
     */
    fun destroy()
}