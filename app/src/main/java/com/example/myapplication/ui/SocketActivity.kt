package com.example.myapplication.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivitySocketBinding
import com.example.myapplication.service.socket.PerplexityWebSocketClient
import com.example.myapplication.service.socket.Resource
import com.example.myapplication.service.socket.SocketHelper.genUUID
import com.example.myapplication.service.socket.SocketProcessor
import com.example.myapplication.service.socket.dto.AskQuestionConfig
import com.example.myapplication.service.socket.dto.AskQuestionRequest
import com.example.mybase.core.platform.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

val frontend_uuid = genUUID()

@AndroidEntryPoint
class SocketActivity (override val layoutID: Int = R.layout.activity_socket) :
    BaseActivity<ActivitySocketBinding>() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SocketProcessor.connect()
        binding.apply {
            btSend.setOnClickListener {
                tvAnswer.text = ""
                lifecycleScope.launch {
                    PerplexityWebSocketClient().askQuestion(etAsk.text.toString()).collect{
                        when(it){
                            is Resource.Completed ->    tvAnswer.text = "${it.data.getAnswer}"
                            is Resource.Loading ->    tvAnswer.text = "${it.data.getAnswer}"
                            else -> Unit
                        }

                    }
                }
            }

        }
    }
}