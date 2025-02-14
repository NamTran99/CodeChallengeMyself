package com.example.myapplication.service.socket

import AnswerResponse
import com.example.myapplication.service.socket.dto.AskQuestionConfig
import com.example.myapplication.service.socket.dto.AskQuestionRequest
import com.example.myapplication.ui.frontend_uuid
import com.example.mybase.extensions.fromJson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

sealed class Resource<T> {
    data class Loading<T>(val data: T) : Resource<T>()
    data class Completed<T>(val data: T) : Resource<T>()
    data class Error<T>(val errorMessage: String) : Resource<T>()
}

class PerplexityWebSocketClient {
    fun askQuestion(question: String): Flow<Resource<AnswerResponse>> = callbackFlow {
        val askRequest = AskQuestionRequest.genAskQuestionRequest(question, AskQuestionConfig(frontendUuid = frontend_uuid)).toJsonObject()
        SocketProcessor.query(askRequest, object : ISocketListener {
            override fun onMessage(code: String, jsonContent: String) {
                when(code){
                    SocketMessageCode.ANSWER_QUESTION_PENDING.code -> {
                        val data = jsonContent.fromJson<AnswerResponse>()
                        if (data != null) {
                            trySend(Resource.Loading(data))
                        }
                    }
                    SocketMessageCode.ANSWER_QUESTION_COMPLETED.code ->{
                        val data = jsonContent.fromJson<AnswerResponse>()
                        if (data != null) {
                            trySend(Resource.Completed(data))
                        }
                        close()
                    }
                }
            }
        })
        awaitClose { }
    }
}


