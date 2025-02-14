package com.example.myapplication.service.socket.dto

import com.example.myapplication.app.MyApplication
import com.example.myapplication.service.socket.SocketHelper.formatMessage
import com.example.myapplication.service.socket.SocketHelper.genUUID
import com.example.myapplication.service.socket.SocketHelper.getAndroidDeviceId
import com.example.myapplication.service.socket.SocketHelper.getDeviceTimeZone
import com.example.myapplication.service.socket.SocketHelper.getLanguage
import com.example.myapplication.service.socket.SocketMessageCode
import com.google.gson.annotations.SerializedName

data class AskQuestionRequest(
    val type: String = "perplexity_ask",
    val query: String,
    val data: AskQuestionConfig
){
    companion object{
        fun genAskQuestionRequest(query: String, data: AskQuestionConfig) = AskQuestionRequest(
            query = query,
            data = data
        )
    }

    fun toJsonObject(): String {
        return formatMessage(SocketMessageCode.ASK_QUESTION.code, type, query, data)
    }
}

data class AskQuestionConfig(
    val source: String = "android",
    val version: String = "2.15",
    @SerializedName("frontend_uuid") val frontendUuid: String,
    @SerializedName("user_nextauth_id") val userNextauthId: String = genUUID(),
    @SerializedName("use_inhouse_model") val useInhouseModel: Boolean = false,
    @SerializedName("android_device_id") val androidDeviceId: String =getAndroidDeviceId(MyApplication.getInstance()) ,
    val mode: String = "concise", // check thêm
    @SerializedName("search_focus") val searchFocus: String = "internet",
    @SerializedName("is_related_query") val isRelatedQuery: Boolean = false,
    @SerializedName("is_voice_to_voice") val isVoiceToVoice: Boolean =false,
    val timezone: String = getDeviceTimeZone(),
    val language: String = getLanguage(),
    @SerializedName("query_source") val querySource: String = "home",
    @SerializedName("is_incognito") val isIncognito: Boolean = false
)
