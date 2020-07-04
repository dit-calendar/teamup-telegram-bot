package com.ditcalendar.bot.telegram

inline fun checkGlobalStateBeforeHandling(msgId: String, requestHandling: () -> Unit) {
    if (globalStateForFirstMessage == null || globalStateForFirstMessage != msgId) {
        globalStateForFirstMessage = msgId
        requestHandling()
    }
}

var globalStateForFirstMessage: String? = null