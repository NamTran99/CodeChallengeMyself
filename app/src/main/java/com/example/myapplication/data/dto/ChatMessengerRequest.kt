package com.example.myapplication.data.dto

import com.google.gson.annotations.SerializedName

data class ChatMessengerRequest(
    @SerializedName("stream")
    val stream: Boolean,

    @SerializedName("uuid")
    val uuid: String,

    @SerializedName("session_id")
    val sessionId: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("history")
    val history: ArrayList<History>
)