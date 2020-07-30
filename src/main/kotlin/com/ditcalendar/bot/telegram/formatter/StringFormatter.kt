package com.ditcalendar.bot.telegram.formatter

import com.ditcalendar.bot.domain.data.*
import com.ditcalendar.bot.service.reloadCallbackCommand
import com.ditcalendar.bot.service.unassignCallbackCommand
import com.ditcalendar.bot.teamup.data.SubCalendar
import com.ditcalendar.bot.teamup.data.core.Base
import com.ditcalendar.bot.telegram.data.InlineMessageResponse
import com.ditcalendar.bot.telegram.data.MessageResponse
import com.ditcalendar.bot.telegram.data.TelegramResponse
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.result.Result
import kotlinx.serialization.json.JsonDecodingException

const val reloadButtonText = "reload"
const val calendarReloadCallbackNotification = "calendar was reloaded"

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
                InlineMessageResponse(result.toMarkdown() + System.lineSeparator(), reloadButtonText,
                        "$reloadCallbackCommand${result.id}_${result.startDate}_${result.endDate}",
                        calendarReloadCallbackNotification)
            is TelegramTaskForUnassignment ->
                InlineMessageResponse(result.toMarkdown(),
                        "unassign me", "$unassignCallbackCommand${result.task.id}", null)
            is TelegramTaskForAssignment ->
                MessageResponse("nicht implementiert", null)
            is TelegramTaskAfterUnassignment ->
                MessageResponse(result.toMarkdown(), "erfolgreich ausgetragen")
            else ->
                MessageResponse("interner server Fehler", null)
        }

private fun parseError(error: Exception): TelegramResponse =
        MessageResponse(when (error) {
            is FuelError -> {
                when (error.response.statusCode) {
                    401 -> "Bot is missing necessary access rights"
                    403 -> "Bot is missing necessary access rights"
                    404 -> "calendar or task not found"
                    503 -> "server not reachable, try again in a moment"
                    else -> if (error.cause is JsonDecodingException) {
                        "unexpected server response"
                    } else if (error.message != null)
                        "Error: " + error.message.toString()
                    else "unkown Error"
                }
            }
            is DitBotError -> {
                when (error) {
                    is InvalidRequest -> error.message!!
                    is NoSubcalendarFound -> error.message!!
                    is MultipleSubcalendarsFound -> error.message!!
                }
            }
            else -> "unknown error"
        }.withMDEscape(), null)