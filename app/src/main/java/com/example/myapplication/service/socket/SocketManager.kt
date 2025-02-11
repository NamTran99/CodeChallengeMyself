package com.example.myapplication.service.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import java.net.URISyntaxException

class SocketManager {

    private lateinit var socket: Socket

    fun getSocket(): Socket  = socket

    fun connectSocket() {
        try {
            val options = IO.Options().apply {
                transports = arrayOf("websocket", "polling")
                reconnection = true // Tự động kết nối lại
                timeout = 5000      // Timeout sau 5 giây
            }

            // Thay URL của server vào đây
            socket = IO.socket("wss://www.perplexity.ai/socket.io",options)

            socket.on(Socket.EVENT_CONNECT) {
                Log.d("TAG", "connectSocket: NamTD8")
            }

            // Lắng nghe khi socket ngắt kết nối
            socket.on(Socket.EVENT_DISCONNECT) {
                Log.d("TAG", "EVENT_DISCONNECT: NamTD8")
            }

            // Lắng nghe lỗi khi kết nối
            socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.d("TAG", "EVENT_CONNECT_ERROR: NamTD8 - ${args.map { it.toString() }}")
            }


            socket.on("your_event_name") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    println("Received data: $data")
                }
            }

            socket.connect()
        } catch (e: URISyntaxException) {
            Log.d("TAG", "connectSocket: NamTD8 ${e.message}")
            e.printStackTrace()
        }
    }

    fun sendMessage(event: String, data: JSONObject) {
        socket.emit(event, data)
    }

    fun sendMessage(event: String, data: JSONArray) {
        socket.emit(event, data)
    }

    fun disconnectSocket() {
        socket.disconnect()
    }
}
