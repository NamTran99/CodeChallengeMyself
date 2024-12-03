package com.example.myapplication.data.dto

import com.google.gson.annotations.SerializedName

data class VulanTokenResponse(
    @SerializedName("AccessToken") val accessToken: String,
    @SerializedName("RefreshToken") val refreshToken: String,
    @SerializedName("AccessTokenExpiration") val accessTokenExpiration: String,
    @SerializedName("RefreshTokenExpiration") val refreshTokenExpiration: String
)
