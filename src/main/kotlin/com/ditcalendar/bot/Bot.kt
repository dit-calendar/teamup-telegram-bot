package com.ditcalendar.bot

import com.ditcalendar.bot.config.*
import com.ditcalendar.bot.data.TelegramLink
import com.ditcalendar.bot.endpoint.CalendarEndpoint
import com.ditcalendar.bot.endpoint.EventEndpoint
import com.ditcalendar.bot.data.InvalidRequest
import com.ditcalendar.bot.service.*
import com.ditcalendar.bot.telegram.CommandExecution
import com.ditcalendar.bot.telegram.callbackResponse
import com.ditcalendar.bot.telegram.checkGlobalStateBeforeHandling
import com.ditcalendar.bot.telegram.messageResponse
import com.elbekD.bot.Bot
import com.elbekD.bot.server
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.Message
import com.github.kittinunf.result.Result

val helpMessage =
        """
            Mögliche Befehle sind
            /postcalendar {Hier Id einfügen} = Postet den Calendar mit der angegebenen ID
            /help = Zeigt alle Befehle an
        """.trimIndent()

fun main(args: Array<String>) {


    val config by config()

    val token = config[telegram_token]
    val herokuApp = config[heroku_app_name]
    val commandExecution = CommandExecution(CalendarService(CalendarEndpoint(), EventEndpoint()))

    val bot = if (config[webhook_is_enabled]) {
        Bot.createWebhook(config[bot_name], token) {
            url = "https://$herokuApp.herokuapp.com/$token"

            /*
            Jetty server is used to listen to incoming request from Telegram servers.
            */
            server {
                host = "0.0.0.0"
                port = config[server_port]
            }
        }
    } else Bot.createPolling(config[bot_name], token)

    bot.onCallbackQuery { callbackQuery ->
        checkGlobalStateBeforeHandling(callbackQuery.id) {
            val request = callbackQuery.data
            val originallyMessage = callbackQuery.message

            if (request == null || originallyMessage == null) {
                bot.answerCallbackQuery(callbackQuery.id, "fehlerhafte Anfrage")
            } else {
                val msgUser = callbackQuery.from
                val telegramLink = TelegramLink(originallyMessage.chat.id, msgUser.id, msgUser.username, msgUser.first_name)
                val response = commandExecution.executeCallback(telegramLink, request)

                bot.callbackResponse(response, callbackQuery, originallyMessage)
            }
        }
    }

    //for deeplinking
    bot.onCommand("/start") { msg, opts ->
        checkGlobalStateBeforeHandling(msg.message_id.toString()) {

            bot.deleteMessage(msg.chat.id, msg.message_id)
            val msgUser = msg.from
            //if message user is not set, we can't process
            if (msgUser == null) {
                bot.sendMessage(msg.chat.id, "fehlerhafte Anfrage")
            } else {
                if (opts != null && opts.startsWith("assign")) {

                    val taskId: String = opts.substringAfter("assign_")
                    if (taskId.isNotBlank()) {
                        val assignMeButton = InlineKeyboardButton("Mit Telegram Namen", callback_data = assingWithNameCallbackCommand + taskId)
                        val annonAssignMeButton = InlineKeyboardButton("Annonym", callback_data = assingAnnonCallbackCommand + taskId)
                        val inlineKeyboardMarkup = InlineKeyboardMarkup(listOf(listOf(assignMeButton, annonAssignMeButton)))
                        bot.sendMessage(msg.chat.id, "Darf ich dein Namen verwenden?", "MarkdownV2", true, markup = inlineKeyboardMarkup)
                    } else {
                        bot.messageResponse(Result.error(InvalidRequest()), msg)
                    }
                } else {
                    bot.sendMessage(msg.chat.id, helpMessage)
                }
            }
        }
    }

    bot.onCommand("/help") { msg, _ ->
        checkGlobalStateBeforeHandling(msg.message_id.toString()) {
            bot.sendMessage(msg.chat.id, helpMessage)
        }
    }

    fun postCalendarCommand(msg: Message, opts: String?) {
        checkGlobalStateBeforeHandling(msg.message_id.toString()) {
            bot.deleteMessage(msg.chat.id, msg.message_id)
            val response = commandExecution.executePublishCalendarCommand(opts)
            bot.messageResponse(response, msg)
        }
    }

    bot.onCommand("/postcalendar") { msg, opts ->
        postCalendarCommand(msg, opts)
    }

    bot.onChannelPost { msg ->
        val msgText = msg.text
        if (msgText != null && msgText.startsWith("/postcalendar"))
            postCalendarCommand(msg, msgText.substringAfter(" "))
    }

    bot.start()
}