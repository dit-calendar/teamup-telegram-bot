package com.ditcalendar.bot.telegram.formatter

import com.ditcalendar.bot.domain.data.*
import com.ditcalendar.bot.service.reloadCallbackCommand
import com.ditcalendar.bot.teamup.data.SubCalendar
import com.ditcalendar.bot.teamup.data.core.Base
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.result.Result
import kotlinx.serialization.json.JsonDecodingException

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
                WithInline(result.toMarkdown() + System.lineSeparator(), "reload", "$reloadCallbackCommand${result.id}_${result.startDate}_${result.endDate}", "calendar wurde neugeladen")
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
                    is InvalidRequest -> error.message!!
                    is ServerNotReachable -> "server need to startup, try again"
                    is NoSubcalendarFound -> error.message!!
                    is MultipleSubcalendarsFound -> error.message!!
                    is DBUserNotFound -> error.message!!
                }
            }
            else -> "unknown error"
        }, null)