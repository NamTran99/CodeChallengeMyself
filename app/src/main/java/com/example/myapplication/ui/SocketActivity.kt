package com.example.myapplication.ui

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.service.socket.SocketManager
import com.example.mybase.core.platform.BaseActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


@AndroidEntryPoint
class SocketActivity(override val layoutID: Int = R.layout.activity_main) :
    BaseActivity<ActivityMainBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val socketManager = SocketManager()
        socketManager.connectSocket()

        val message = JSONArray().apply {
            put("perplexity_ask")
            put("What are the unique characteristics of Stradivarius violins")
            put(JSONObject().apply {
                put("source", "android")
                put("version", "2.15")
                put("frontend_uuid", "eb5e20c1-d057-4cab-abf0-e07be4d717d0")
                put("last_backend_uuid", "2273ad5b-720d-4455-8137-299f8bcb2c64")
                put("use_inhouse_model", false)
                put("read_write_token", "1f7e4c36-9529-49f0-9262-9f34da3e052b")
                put("android_device_id", "9c94edee38ee7427")
                put("mode", "concise")
                put("search_focus", "internet")
                put("is_related_query", true)
                put("is_voice_to_voice", false)
                put("timezone", "Asia/Bangkok")
                put("language", "vi-VN")
                put("query_source", "related")
                put("is_incognito", false)
            })
        }
        socketManager.sendMessage("submit query",message)

        val socket = socketManager.getSocket()
        lifecycleScope.launch {
            delay(3000)
            if (socket.connected()) {
                Log.d("TAG", "onCreate: NamTD8 success")
            } else {
                Log.d("TAG", "onCreate: NamTD8 unSuccess")

            }

        }

    }

}