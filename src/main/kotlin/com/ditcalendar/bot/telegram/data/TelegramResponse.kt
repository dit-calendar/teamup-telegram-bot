package com.ditcalendar.bot.telegram.data

sealed class TelegramResponse(val message: String, val callbackNotificationText: String?)
class MessageResponse(message: String, callbackNotificationText: String?) : TelegramResponse(message, callbackNotificationText)
class InlineMessageResponse(message: String, val callBackText: String, val callBackData: String,
                            callbackNotificationText: String?) : TelegramResponse(message, callbackNotificationText)