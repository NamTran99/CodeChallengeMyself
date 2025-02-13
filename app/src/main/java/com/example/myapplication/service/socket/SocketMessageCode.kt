package com.example.myapplication.service.socket

import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

enum class SocketMessageCode(val code: String) {
     SUCCESS_HANDSHAKE("0"),
    CONNECT_CONFIRMATION("40"),
    MESSAGE_RESPONSE("420"),
    PING("2"),
    PONG("3")
}