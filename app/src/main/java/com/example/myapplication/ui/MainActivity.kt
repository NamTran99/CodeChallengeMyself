package com.example.myapplication.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.mybase.core.platform.BaseActivity
import com.example.mybase.core.platform.storage.BaseDataStore
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Person(val name: String)

@AndroidEntryPoint
class MainActivity(override val layoutID: Int = R.layout.activity_main) :
    BaseActivity<ActivityMainBinding>() {
    private val CHANNEL_ID = "critical_alert_channel"
    val data by lazy {
        BaseDataStore(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data.saveData("2",Person("name"))
        lifecycleScope.launch {
            delay(1000)
            data.getData<Person>("2").collect{
                Log.d("TAG", "onCreate: NamTD8 $it")
            }
        }
        createNotificationChannel()
        // Subscribe to a critical topic (could be customized based on your backend logic)
        FirebaseMessaging.getInstance().subscribeToTopic("critical_incidents")
            .addOnCompleteListener { task ->
                val msg =
                    if (task.isSuccessful) "Subscribed to critical incidents" else "Subscription failed"
                Log.d("FCM", msg)
            }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d("TAG", "NamTD8 my token ${token}")
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Critical Alerts"
            val descriptionText = "Channel for critical incident alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
                setBypassDnd(true) // Allow notifications even in Do Not Disturb mode
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}