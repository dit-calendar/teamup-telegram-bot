package com.ditcalendar.bot.formatter

import com.ditcalendar.bot.data.SubCalendar
import com.ditcalendar.bot.data.TelegramTaskAfterUnassignment
import com.ditcalendar.bot.data.TelegramTaskForAssignment
import com.ditcalendar.bot.data.TelegramTaskForUnassignment
import com.ditcalendar.bot.data.core.Base
import com.ditcalendar.bot.error.*
import com.ditcalendar.bot.service.TelegramResponse
import com.ditcalendar.bot.service.WithInline
import com.ditcalendar.bot.service.WithMessage
import com.ditcalendar.bot.service.reloadCallbackCommand
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.result.Result
import kotlinx.serialization.json.JsonDecodingException
import java.util.*

fun parseResponse(result: Result<Base, Exception>): TelegramResponse =
        when (result) {
            is Result.Success -> parseSuccess(result.value)
            is Result.Failure -> {
                result.error.printStackTrace()
                parseError(result.error)
            }
        }

private fun parseSuccess(result: Base): TelegramResponse =
        when (result) {
            is SubCalendar ->
                WithInline(result.toMarkdown() + System.lineSeparator(), "reload", "$reloadCallbackCommand${result.id}", "calendar wurde neugeladen")
            is TelegramTaskForUnassignment ->
                WithInline(result.toMarkdown(),
                        "unassign me", "unassign_${result.task.id}", null)
            is TelegramTaskForAssignment ->
                WithMessage("nicht implementiert", null)
            is TelegramTaskAfterUnassignment ->
                WithMessage(result.toMarkdown(), "erfolgreich ausgetragen")
            else ->
                WithMessage("interner server Fehler", null)
        }

private fun parseError(error: Exception): TelegramResponse =
        WithMessage(when (error) {
            is FuelError -> {
                when (error.response.statusCode) {
                    401 -> "Bot is missing necessary access rights"
                    403 -> "Bot is missing necessary access rights"
                    404 -> "calendar or task not found"
                    503 -> "server not reachable, try again in a moment"
                    else -> if (error.cause is JsonDecodingException) {
                        "unexpected server response"
                    } else if (error.message != null)
                        "Error: " + error.message.toString().withMDEscape()
                    else "unkown Error"
                }
            }
            is DitBotError -> {
                when (error) {
                    is InvalidRequest -> "incorrect request to server"
                    is ServerNotReachable -> "server need to startup, try again"
                    is NoSubcalendarFound -> TODO()
                    is MultipleSubcalendarsFound -> TODO()
                }
            }
            else -> "unknown error"
        }, null)