package com.example.myapplication.data.dto

import com.google.gson.annotations.SerializedName

data class VulanChatRequest(
    @SerializedName("messages")
    val messenger: ArrayList<History>,

    @SerializedName("model")
    val model: String,

    @SerializedName("nsfw_check")
    val nsfwCheck: Boolean,

    @SerializedName("user")
    val user: String
)