package com.example.myapplication.service.socket.model

import com.google.gson.Gson
import org.json.JSONObject

data class HandshakeResponse(
    val sid: String,
    val pingTimeout: Long,
    val pingInterval: Long
)