package com.example.myapplication.service.socket.dto

data class HandshakeResponse(
    val sid: String,
    val pingTimeout: Long,
    val pingInterval: Long
)