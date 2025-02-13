package com.example.myapplication.ui

import android.os.Bundle
import android.util.Log
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.service.socket.SocketProcessor
import com.example.mybase.core.platform.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale


@AndroidEntryPoint
class SocketActivity(override val layoutID: Int = R.layout.activity_main) :
    BaseActivity<ActivityMainBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SocketProcessor.connect()

        Log.d("TAG", "onCreate: ${Locale.getDefault().toString()}")
    }
}