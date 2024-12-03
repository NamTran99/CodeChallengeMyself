package com.example.myapplication.datasource.remote

import okhttp3.Call
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url


interface AppApi {
    @Streaming
    @GET
    fun downloadFile(@Url fileUrl: String?): Call<ResponseBody>
}
