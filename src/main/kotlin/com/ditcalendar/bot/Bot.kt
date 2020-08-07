package com.ditcalendar.bot

import com.ditcalendar.bot.config.*
import com.ditcalendar.bot.domain.dao.PostCalendarMetaInfoTable
import com.ditcalendar.bot.domain.dao.TelegramLinksTable
import com.ditcalendar.bot.domain.dao.find
import com.ditcalendar.bot.service.CalendarService
import com.ditcalendar.bot.service.CommandExecution
import com.ditcalendar.bot.service.assingAnnonCallbackCommand
import com.ditcalendar.bot.service.assingWithNameCallbackCommand
import com.ditcalendar.bot.teamup.endpoint.CalendarEndpoint
import com.ditcalendar.bot.teamup.endpoint.EventEndpoint
import com.ditcalendar.bot.telegram.service.*
import com.elbekD.bot.Bot
import com.elbekD.bot.server
import com.elbekD.bot.types.Message
import com.github.kittinunf.result.success
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI

val helpMessage =
        """
            Commands which the bot accept
            /postcalendar {Subcalendar name} {start date as yyyy-MM-dd} {optional end date as yyyy-MM-dd} = Post subcalendar in channel
            /help = show all bot commands
        """.trimIndent()

fun main(args: Array<String>) {

    val config by config()

    val token = config[telegram_token]
    val herokuApp = config[heroku_app_name]
    val commandExecution = CommandExecution(CalendarService(CalendarEndpoint(), EventEndpoint()))
    val databaseUrl = config[database_url]

    fun createDB() {
        val dbUri = URI(databaseUrl)
        val username = dbUri.userInfo.split(":")[0]
        val password = dbUri.userInfo.split(":")[1]
        var dbUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path

        if (herokuApp.isNotBlank()) //custom config logic needed because of config lib
            dbUrl += "?sslmode=require"

        Database.connect(dbUrl, driver = "org.postgresql.Driver",
                user = username, password = password)
        transaction { SchemaUtils.create(TelegramLinksTable, PostCalendarMetaInfoTable) }
    }

    createDB()

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
                bot.answerCallbackQuery(callbackQuery.id, wrongRequestResponse)
            } else {
                val msgUser = callbackQuery.from
                val response = commandExecution.executeCallback(originallyMessage.chat.id.toInt(), msgUser.id, msgUser.first_name, request, originallyMessage)

                bot.callbackResponse(response, callbackQuery, originallyMessage)
                response.success {
                    if (request.startsWith(assingWithNameCallbackCommand) || request.startsWith(assingAnnonCallbackCommand)) {
                        val optsAfterTaskId = request
                                .removePrefix(assingWithNameCallbackCommand)
                                .removePrefix(assingAnnonCallbackCommand)
                                .substringAfter("_")

                        val variables = optsAfterTaskId.split("_")
                        val messageId = variables.getOrNull(0)?.toIntOrNull()
                        if (messageId != null) {
                            var postCalendarMetaInfo = find(messageId)
                            if (postCalendarMetaInfo != null) {
                                commandExecution.reloadCalendar(postCalendarMetaInfo)
                                        .success { bot.editOriginalCalendarMessage(it, postCalendarMetaInfo.chatId, postCalendarMetaInfo.messageId) }
                            }
                        }
                    }
                }
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
                bot.sendMessage(msg.chat.id, wrongRequestResponse)
            } else {
                if (opts != null)
                    bot.responseForDeeplink(msg.chat.id, opts)
                else
                    bot.sendMessage(msg.chat.id, helpMessage)
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
            if (opts != null) {
                val response = commandExecution.executePublishCalendarCommand(opts, msg)
                bot.messageResponse(response, msg.chat.id)
            } else bot.sendMessage(msg.chat.id, helpMessage)
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