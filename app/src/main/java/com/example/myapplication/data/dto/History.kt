package com.example.myapplication.data.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class History(
    @SerializedName("role")
    val role: String,

    @SerializedName("content")
    val content: String
) : Parcelable