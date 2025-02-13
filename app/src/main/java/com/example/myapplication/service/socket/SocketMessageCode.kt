package com.example.myapplication.service.socket

enum class SocketMessageCode(val code: String) {
    SUCCESS_HANDSHAKE("0"),
    CONNECT_CONFIRMATION("40"),
    ASK_QUESTION("421"),
    ANSWER_QUESTION_PENDING("42"),
    ANSWER_QUESTION_COMPLETED("431"),
    MESSAGE_RESPONSE("42"),
    PING("2"),
    PONG("3")
}