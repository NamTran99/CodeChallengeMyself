package com.example.myapplication.data.api

import com.example.myapplication.data.dto.VulanChatRequest
import com.example.myapplication.data.dto.VulanResponse
import com.example.myapplication.data.dto.VulanTokenRequest
import com.example.myapplication.data.dto.VulanTokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAIService {

    @POST("/api/v3/chat")
    suspend fun chatV3(
        @Body vulanChatRequest: VulanChatRequest
    ): VulanResponse

    @POST("/api/v6/chat")
    suspend fun chatV6(
        @Body vulanChatRequest: VulanChatRequest
    ): VulanResponse

    @POST("/api/v1/token")
    suspend fun getToken(
        @Body vulanTokenRequest: VulanTokenRequest
    ): VulanTokenResponse
}