package com.example.myapplication.domain.repository

import com.example.myapplication.data.dto.History
import com.example.myapplication.data.services.MainRemoteService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainRepository(
    private val mainRemoteService: MainRemoteService,
) {
    private var listHistory: ArrayList<History> = ArrayList()

    interface ChatMessengerCallBack {
        fun onCompletion(str1: String, str2: String)
        fun onError()
        fun onStreamData(str1: String, str2: String)
//        fun startStreamData(chatMessengerResponse: ChatMessengerResponse)
    }

    suspend fun destroy(): Unit = withContext(Dispatchers.IO) {
//        MainRepository$destroy$2(this@MainRepository, null).invokeSuspend(Unit)
    }

//    suspend fun getHistory(): ArrayList<History> = withContext(Dispatchers.IO) {
//        mainRemoteService
////        MainRepository$getHistory$2(this@MainRepository, null).invokeSuspend(Unit) as ArrayList<History>
//    }

    fun initHistory(listHistory: ArrayList<History>) {
        this.listHistory.clear()
        this.listHistory.addAll(listHistory)
    }

//    suspend fun insertMessenger(str: String, chatRole: ChatRole): Unit = withContext(Dispatchers.IO) {
//        MainRepository$insertMessenger$2(this@MainRepository, str, chatRole, null).invokeSuspend(Unit)
//    }
//
//    suspend fun loadExample(input: String): Result<List<String>> = withContext(Dispatchers.IO) {
//        MainRepository$loadExample$2(input, null).invokeSuspend(Unit) as Result<List<String>>
//    }
//
//    suspend fun loadHistoryById(str: String): ArrayList<ChatMessengerData> = withContext(Dispatchers.IO) {
//        MainRepository$loadHistoryById$2(this@MainRepository, str, null).invokeSuspend(Unit) as ArrayList<ChatMessengerData>
//    }

    suspend fun sendMessenger(
        str: String,
        chatMessengerCallBack: ChatMessengerCallBack
    ): Unit = withContext(Dispatchers.IO) {
//        mainRemoteService.chatWithVulan()
//        mainRemoteService.sendingMessage()
    }
}
