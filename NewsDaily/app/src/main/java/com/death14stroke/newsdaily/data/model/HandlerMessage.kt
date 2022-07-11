package com.death14stroke.newsdaily.data.model

enum class HandlerMessage(val code: Int) {
    MSG_INIT_NEWS(0),
    MSG_STOP_SERVICE(1),
    MSG_UPDATE_SOURCE(2),
    MSG_SHOW_NOTI(3);

    companion object {
        private val reverseMap = values().associateBy { it.code }
        fun from(code: Int) = reverseMap[code]
    }
}