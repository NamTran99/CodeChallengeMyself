package com.example.myapplication.data.services

import android.annotation.SuppressLint
import android.provider.Settings
import android.util.Log
import com.example.myapplication.app.MyApplication
import com.example.myapplication.data.api.OpenAIService
import com.example.myapplication.data.dto.ChatMessengerRequest
import com.example.myapplication.data.dto.History
import com.example.myapplication.data.dto.VulanChatRequest
import com.example.myapplication.data.dto.VulanResponse
import com.example.myapplication.data.dto.VulanTokenRequest
import com.example.myapplication.data.dto.VulanTokenResponse
import com.example.myapplication.domain.repository.MainRepository
import com.example.myapplication.domain.layer.ChatGPTModel
import kotlinx.coroutines.Job

class MainRemoteService(val openAIService: OpenAIService) {
    enum class NetWorkType { ARITEK, VULAN, ASKAI }

    private var vulanJob: Job? = null

    @SuppressLint("HardwareIds")
    val userID = Settings.Secure.getString(
        MyApplication.appContext.contentResolver,
        Settings.Secure.ANDROID_ID
    ).uppercase()


    fun sendMessenger(
        chatMessengerRequest: ChatMessengerRequest,
        chatMessengerCallBack: MainRepository.ChatMessengerCallBack
    ) {
//        chatWithVulan(chatMessengerRequest, chatMessengerCallBack, ChatGPTModel.GPT_4, "", 0)
    }

    @SuppressLint("HardwareIds")
    private suspend fun getVulanToken(): VulanTokenResponse {
        val vulanTokenRequest = VulanTokenRequest(userID, null, null, null, null)
        return openAIService.getToken(vulanTokenRequest)
    }

     suspend fun getToken(): String {
//        val cachedToken = Hn1.instance.preferences.getString("key_token", "") // get cached
        var accessToken = ""
        val cachedToken = ""
        var token = cachedToken ?: ""
        if (token.isEmpty()) {
            val response = getVulanToken()
            accessToken = response.accessToken // viet logic check loi thi lay lay token cu
            token = accessToken
            // save token to local
//            hn1.instance.preferences.edit().putString("key_token", token).apply()
        }
        // save token
        return token
    }

    suspend fun chatWithVulan(listHistory: ArrayList<History>): VulanResponse {
        val vulanChatRequest = VulanChatRequest(
            model = "gpt-3.5-turbo",
            user = userID,
            messenger = listHistory,
            nsfwCheck = true
        )
        return openAIService.chatV6(vulanChatRequest)
    }

    suspend fun chatWithVulan(
        token: String,
        chatMessengerRequest: ChatMessengerRequest,
        chatGPTModel: ChatGPTModel,
        mainRemoteService: MainRemoteService,
        callBack: MainRepository.ChatMessengerCallBack,
        listNetWorkType: ArrayList<NetWorkType>
    ) {
        // Simulating the same flow, assuming additional steps are included.
        val messenger = chatMessengerRequest.message // Assuming a `message` field in `ChatMessengerRequest`
        val addedText = StringBuilder()

//
        callBack.onCompletion(addedText.toString(), "")
    }
}