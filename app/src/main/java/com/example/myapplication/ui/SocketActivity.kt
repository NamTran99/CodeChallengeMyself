package com.example.myapplication.ui

import android.os.Bundle
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivitySocketBinding
import com.example.myapplication.service.socket.SocketHelper.genUUID
import com.example.myapplication.service.socket.SocketProcessor
import com.example.myapplication.service.socket.model.AskQuestionConfig
import com.example.myapplication.service.socket.model.AskQuestionRequest
import com.example.mybase.core.platform.BaseActivity
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
val frontend_uuid = genUUID()

@AndroidEntryPoint
class SocketActivity (override val layoutID: Int = R.layout.activity_socket) :
    BaseActivity<ActivitySocketBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SocketProcessor.connect()
        binding.apply {
            btSend.setOnClickListener {
                AskQuestionRequest.genAskQuestionRequest(etAsk.text.toString(),
                    AskQuestionConfig(frontendUuid = frontend_uuid)
                ).toJsonObject().let {
                    SocketProcessor.query(it)
                }
            }
        }
    }
}