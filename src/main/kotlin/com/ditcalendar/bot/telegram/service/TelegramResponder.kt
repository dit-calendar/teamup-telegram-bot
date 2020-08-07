package com.ditcalendar.bot.telegram.service

import com.ditcalendar.bot.domain.data.InvalidRequest
import com.ditcalendar.bot.helpMessage
import com.ditcalendar.bot.service.assignDeepLinkCommand
import com.ditcalendar.bot.service.assingAnnonCallbackCommand
import com.ditcalendar.bot.service.assingWithNameCallbackCommand
import com.ditcalendar.bot.teamup.data.SubCalendar
import com.ditcalendar.bot.teamup.data.core.Base
import com.ditcalendar.bot.telegram.data.InlineMessageResponse
import com.ditcalendar.bot.telegram.data.MessageResponse
import com.ditcalendar.bot.telegram.formatter.parseResponse
import com.ditcalendar.bot.telegram.formatter.toMarkdown
import com.elbekD.bot.Bot
import com.elbekD.bot.types.CallbackQuery
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.Message
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.failure
import com.github.kittinunf.result.success
import java.util.concurrent.CompletableFuture

const val parseMode = "MarkdownV2"
const val wrongRequestResponse = "request invalid"

fun Bot.messageResponse(response: Result<Base, Exception>, chatId: Long): CompletableFuture<Message> {
    when (val result = parseResponse(response)) {
        is MessageResponse ->
            return sendMessage(chatId, result.message, parseMode, true)
        is InlineMessageResponse -> {
            val inlineButton = InlineKeyboardButton(result.callBackText, callback_data = result.callBackData)
            val inlineKeyboardMarkup = InlineKeyboardMarkup(listOf(listOf(inlineButton)))
            return sendMessage(chatId, result.message, parseMode, true, markup = inlineKeyboardMarkup)
        }
    }
}

fun Bot.callbackResponse(response: Result<Base, Exception>, callbackQuery: CallbackQuery, originallyMessage: Message) {
    when (val result = parseResponse(response)) {
        is MessageResponse -> {
            response.failure { answerCallbackQuery(callbackQuery.id, result.message, alert = true) }
            response.success {
                val telegramAnswer = editMessageText(originallyMessage.chat.id, originallyMessage.message_id, text = result.message,
                        parseMode = parseMode)
                telegramAnswer.handleCallbackQuery(this, callbackQuery.id, result.callbackNotificationText)
            }
        }
        is InlineMessageResponse -> {
            val inlineButton = InlineKeyboardButton(result.callBackText, callback_data = result.callBackData)
            val inlineKeyboardMarkup = InlineKeyboardMarkup(listOf(listOf(inlineButton)))
            val telegramAnswer = editMessageText(originallyMessage.chat.id, originallyMessage.message_id, text = result.message,
                    parseMode = parseMode, disableWebPagePreview = true, markup = inlineKeyboardMarkup)

            telegramAnswer.handleCallbackQuery(this, callbackQuery.id, result.callbackNotificationText)
        }
    }
}

fun CompletableFuture<Message>.handleCallbackQuery(bot: Bot, calbackQueryId: String, callbackNotificationText: String?) {
    this.handle { _, throwable ->
        if (throwable == null || throwable.message!!.contains("Bad Request: message is not modified"))
            if (callbackNotificationText != null)
                bot.answerCallbackQuery(calbackQueryId, callbackNotificationText)
    }
}

fun Bot.editOriginalCalendarMessage(calendar: SubCalendar, chatId: Long, messageId: Int) {
    editMessageText(chatId, messageId, text = calendar.toMarkdown(),
            parseMode = parseMode, disableWebPagePreview = true)
}

fun Bot.responseForDeeplink(chatId: Long, opts: String) {
    if (opts.startsWith(assignDeepLinkCommand)) {
        val callbackOpts: String = opts.substringAfter(assignDeepLinkCommand)
        if (callbackOpts.isNotBlank()) {
            val assignMeButton = InlineKeyboardButton("With telegram name", callback_data = assingWithNameCallbackCommand + callbackOpts)
            val annonAssignMeButton = InlineKeyboardButton("Annonym", callback_data = assingAnnonCallbackCommand + callbackOpts)
            val inlineKeyboardMarkup = InlineKeyboardMarkup(listOf(listOf(assignMeButton, annonAssignMeButton)))
            sendMessage(chatId, "Can I use your name?", parseMode, true, markup = inlineKeyboardMarkup)
        } else {
            messageResponse(Result.error(InvalidRequest()), chatId)
        }
    } else {
        sendMessage(chatId, helpMessage)
    }
}