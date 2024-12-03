package com.example.myapplication.data.dto

import com.google.gson.annotations.SerializedName

data class Choices(
    @SerializedName("Message") val message: History
)
