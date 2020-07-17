package com.ditcalendar.bot.telegram

import com.ditcalendar.bot.domain.data.WithInline
import com.ditcalendar.bot.domain.data.WithMessage
import com.ditcalendar.bot.teamup.data.core.Base
import com.ditcalendar.bot.telegram.formatter.parseResponse
import com.elbekD.bot.Bot
import com.elbekD.bot.types.CallbackQuery
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.Message
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.failure
import com.github.kittinunf.result.success

fun Bot.messageResponse(response: Result<Base, Exception>, msg: Message) {
    when (val result = parseResponse(response)) {
        is WithMessage ->
            sendMessage(msg.chat.id, result.message, "MarkdownV2", true)
        is WithInline -> {
            val inlineButton = InlineKeyboardButton(result.callBackText, callback_data = result.callBackData)
            val inlineKeyboardMarkup = InlineKeyboardMarkup(listOf(listOf(inlineButton)))
            sendMessage(msg.chat.id, result.message, "MarkdownV2", true, markup = inlineKeyboardMarkup)
        }
    }
}

fun Bot.callbackResponse(response: Result<Base, Exception>, callbackQuery: CallbackQuery, originallyMessage: Message) {
    when (val result = parseResponse(response)) {
        is WithMessage -> {
            response.failure { answerCallbackQuery(callbackQuery.id, result.message, alert = true) }
            response.success {
                if (result.callbackNotificationText != null)
                    answerCallbackQuery(callbackQuery.id, result.callbackNotificationText)
                editMessageText(originallyMessage.chat.id, originallyMessage.message_id, text = result.message,
                        parseMode = "MarkdownV2")
            }
        }
        is WithInline -> {
            answerCallbackQuery(callbackQuery.id, result.callbackNotificationText)
            val inlineButton = InlineKeyboardButton(result.callBackText, callback_data = result.callBackData)
            val inlineKeyboardMarkup = InlineKeyboardMarkup(listOf(listOf(inlineButton)))
            editMessageText(originallyMessage.chat.id, originallyMessage.message_id, text = result.message,
                    parseMode = "MarkdownV2", disableWebPagePreview = true, markup = inlineKeyboardMarkup)
        }
    }
}