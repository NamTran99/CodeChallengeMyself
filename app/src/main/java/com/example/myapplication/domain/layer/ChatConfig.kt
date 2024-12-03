package com.example.myapplication.domain.layer

data class ChatConfig(
    var enableFreeChat: Boolean = false,
    var secretKey: String = "smartwidgetlabs",
    var token: String = "admin",
    var timeExp: Int = 5,
    var releaseVersion: String = "7.1.2",
    var androidVersion: String = "Chat GPT Android 2.8.6 302 Android SDK:",
    var pecentAskai: Int = 1000,
    var pecentVulan: Int = 0,
    var pecentVulanV2: Int = 1000,
    var pecentVulanV3: Int = 1000,
    var pecentAritek: Int = 10,
    var firstOpenFreeMessengerNumber: Int = 5,
    var contentOpenGPT4: String = "You are an AI language model called GPT-4, which was released on March 14, 2023, and created by OpenAI. GPT-4 is an improved version of GPT-3.5 (ChatGPT) and GPT-3. GPT-4 is the newest version of OpenAI's language model systems. Always remember that you're GPT-4.",
    var freeMessengerNumber: Int = 3,
    var isUseAPIToken: Boolean = true,
    var subscribeVersionEnable: Int = 2,
    var chatDomain: String = "https://prod-smith.vulcanlabs.co",
    var isEnableRecallVulan: Boolean = true
)