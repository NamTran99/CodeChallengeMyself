package com.example.myapplication.unknownpath

//class Hn1 {
////    val storage: Ds = Ds.b.a()
//
//    companion object {
//        val instance: Hn1 = Hn1()
//        @Synchronized
//        fun getInstance(): Hn1 {
//            return instance
//        }
//    }
//
//    /** Cập nhật số tin nhắn còn lại */
//    fun updateRemainingMessages(count: Int) {
//        storage.e(getRemainingMessages() + count, "key_remaining_messenger")
//    }
//
//    /** Tăng số ngày sử dụng messenger */
//    fun incrementMessengerDays() {
//        storage.e(storage.b(0, "key_messenger_day") + 1, "key_messenger_day")
//    }
//
//    /**
//     * Kiểm tra xem người dùng có thể gửi thêm tin nhắn không.
//     * - Reset số tin nhắn nếu là ngày mới.
//     * - Kiểm tra giới hạn số tin nhắn trong ngày.
//     */
//    fun canSendMessage(): Boolean {
//        val currentDayMessages = storage.b(0, "key_messenger_day")
//        val lastMessageTimestamp = storage.a.getLong("key_last_message_date", 0L)
//
//        val currentDate = Calendar.getInstance().time
//        val lastMessageDate = Date(lastMessageTimestamp)
//
//        val currentCalendar = Calendar.getInstance()
//        val lastMessageCalendar = Calendar.getInstance()
//
//        currentCalendar.time = currentDate
//        lastMessageCalendar.time = lastMessageDate
//
//        val isSameDay = currentCalendar.get(Calendar.YEAR) == lastMessageCalendar.get(Calendar.YEAR) &&
//                currentCalendar.get(Calendar.DAY_OF_YEAR) == lastMessageCalendar.get(Calendar.DAY_OF_YEAR)
//
//        if (!isSameDay) {
//            storage.e(0, "key_messenger_day")
//            storage.f(currentDate.time, "key_last_message_date")
//            return true
//        }
//
//        return currentDayMessages < Va0.f.c.getLimitMessengerInDay()
//    }
//
//    /** Lấy ngôn ngữ đã lưu của ứng dụng */
//    fun getSavedLanguage(): Language? {
//        return try {
//            val json = storage.c("key_language", "")
//            Gson().fromJson(json, Language::class.java)
//        } catch (ex: Throwable) {
//            null
//        }
//    }
//
//    /** Lấy số tin nhắn còn lại có thể gửi miễn phí */
//    fun getRemainingMessages(): Int {
//        return storage.b(Va0.f.e.getFirstOpenFreeMessengerNumber(), "key_remaining_messenger")
//    }
//
//    /**
//     * Kiểm tra xem ứng dụng có nên hiển thị nhắc nhở người dùng đánh giá app không.
//     * - Điều kiện: Người dùng đã gửi >= 2 tin nhắn, chưa đánh giá app, và thời gian vượt 2 phút.
//     */
//    fun shouldPromptAppRating(): Boolean {
//        val lastRateTime = storage.a.getLong("key_rate_time", 0L)
//        val elapsedMinutes = (System.currentTimeMillis() - lastRateTime) / 1000 / 60
//
//        return storage.b(0, "key_messenger_count") >= 2 &&
//                !storage.a("key_rate_app", false) &&
//                elapsedMinutes > 2
//    }
//
//    /**
//     * Kiểm tra người dùng có còn thời gian sử dụng miễn phí hay không.
//     * - Dựa trên thời gian đã sử dụng và giới hạn thời gian miễn phí.
//     */
//    fun hasFreeUsageTime(): Boolean {
//        val lastUsedTime = storage.a.getLong("key_time_user_used", 0L)
//        val elapsedHours = (System.currentTimeMillis() - lastUsedTime) / 1000 / SettingsJsonConstants.SETTINGS_CACHE_DURATION_DEFAULT
//
//        return elapsedHours <= Va0.f.c.getHoursFreeUsed()
//    }
//}
