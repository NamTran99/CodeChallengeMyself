package com.example.myapplication.service.socket

import AnswerResponse
import android.util.Log
import com.example.myapplication.service.socket.SocketHelper.extractAndRemoveLeadingNumbers
import com.example.myapplication.service.socket.SocketHelper.extractCodeAndJsonContent
import com.example.myapplication.service.socket.SocketHelper.extractJsonString
import com.example.myapplication.service.socket.SocketHelper.generateWebSocketKey
import com.example.myapplication.service.socket.SocketHelper.getLanguage
import com.example.myapplication.service.socket.dto.HandshakeResponse
import com.example.mybase.extensions.fromJson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

interface ISocketListener{
    fun onMessage(code: String, jsonContent: String)
}

object SocketProcessor {
    private val TAG = "PerplexityWebSocketClient"

    private val client = OkHttpClient()
    private val headerBuilder = HashMap<String, String>()
    private const val URL_SOCKET = "wss://www.perplexity.ai/socket.io/?EIO=4&transport=websocket"
    private var dataRemoteConfig = SocketRemoteConfig()
    private var handshakeResponse: HandshakeResponse? = null

    private var mWebSocket: WebSocket? = null
    private var mSocketListener: ISocketListener?= null

    init {
        fetchRemoteServer()
        initHeader()
    }

    private fun fetchRemoteServer(data: SocketRemoteConfig = SocketRemoteConfig()) {
        dataRemoteConfig = data

        headerBuilder["X-App-ApiVersion"] = dataRemoteConfig.apiVersion
        headerBuilder["X-App-Version"] = dataRemoteConfig.appVersion
        headerBuilder["X-Client-Version"] = dataRemoteConfig.clientVersion
    }

    private fun initHeader() {
        headerBuilder["Accept-Encoding"] = "gzip"
        headerBuilder["Accept-Language"] = getLanguage()
        headerBuilder["Connection"] = "Upgrade"
        headerBuilder["Host"] = "www.perplexity.ai"
        headerBuilder["Sec-WebSocket-Version"] = "13"
        headerBuilder["Upgrade"] = "websocket"
        headerBuilder["X-App-ApiClient"] = "android"
        headerBuilder["X-Client-Env"] = "prod"
        headerBuilder["X-Client-Name"] = "Perplexity-Android"
        headerBuilder["Sec-WebSocket-Accept"] = generateWebSocketKey()
    }

    fun query(toString: String, listener: ISocketListener? = null) {
        Log.d(TAG, "query: ${toString}")
        mWebSocket?.send(toString)
        mSocketListener = listener
    }

    fun connect() {
        val request = Request.Builder()
            .url(URL_SOCKET).apply {
                headerBuilder.forEach { (key, value) ->
                    header(key, value)
                }
            }.build()

        val webSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                Log.d(TAG, "onOpen: ")
                mWebSocket = webSocket
                webSocket.send(SocketMessageCode.CONNECT_CONFIRMATION.code) // xác nhan ket noi

//                val message = """420["perplexity_ask","How much are Stradivarius violins?",{
//                    "source":"android",
//                    "version":"2.15",
//                    "frontend_uuid":"2841bbb3-72f1-4078-b246-947e1de750f9",
//                    "use_inhouse_model":false,
//                    "android_device_id":"9c94edee38ee7427",
//                    "mode":"concise",
//                    "search_focus":"internet",
//                    "is_related_query":false,
//                    "is_voice_to_voice":false,
//                    "timezone":"Asia/Bangkok",
//                    "language":"vi-VN",
//                    "query_source":"helper",
//                    "is_incognito":false
//                }]"""
//                webSocket.send(message)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val (code, content) = extractCodeAndJsonContent(text)
                Log.d(TAG, "onMessage: $code - $content")
                mSocketListener?.onMessage(code, content)
                when (code) {
                    SocketMessageCode.SUCCESS_HANDSHAKE.code -> {
                        handshakeResponse = content.fromJson()
                    }

                    SocketMessageCode.PING.code -> {
                        query(SocketMessageCode.PONG.code)
                    }

                    SocketMessageCode.ANSWER_QUESTION_PENDING.code -> {
                        val data = extractJsonString(content)?.fromJson<AnswerResponse>()
                        Log.d(TAG, "onMessage: NamTD88 - ${data}")
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d(TAG, "Received ByteString: ${bytes.hex()}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Received ByteString: $reason}")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket Closed: $reason")
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: okhttp3.Response?
            ) {
                Log.d(TAG, "WebSocket Error: ${t.message}")
            }
        }

        client.newWebSocket(request, webSocketListener)
        client.dispatcher.executorService.shutdown()
    }


}


//// Define the CookieJar interface, similar to InterfaceC4265p
//interface CookieJar {
//    fun saveCookies(url: String, cookies: List<String>)
//    fun loadCookies(url: String): List<String>
//}
//
//class SimpleCookieJar : CookieJar {
//
//    // Store cookies per domain in a thread-safe map
//    private val cookieStore: ConcurrentHashMap<String, MutableList<String>> = ConcurrentHashMap()
//
//    override fun saveCookies(url: String, cookies: List<String>) {
//        val domain = extractDomain(url)
//        if (domain != null) {
//            val existingCookies = cookieStore.getOrPut(domain) { mutableListOf() }
//            cookies.forEach { cookie ->
//                if (!existingCookies.contains(cookie)) {
//                    existingCookies.add(cookie)
//                }
//            }
//        }
//    }
//
//    override fun loadCookies(url: String): List<String> {
//        val domain = extractDomain(url)
//        return if (domain != null) {
//            cookieStore[domain]?.toList() ?: emptyList()
//        } else {
//            emptyList()
//        }
//    }
//
//    // Extracts the domain from the given URL
//    private fun extractDomain(url: String): String? {
//        val pattern = Pattern.compile("^(?:https?://)?(?:www\\.)?([^/]+)")
//        val matcher = pattern.matcher(url)
//        return if (matcher.find()) matcher.group(1) else null
//    }
//}
