package com.ditcalendar.bot.telegram

import com.ditcalendar.bot.data.TelegramLink
import com.ditcalendar.bot.data.core.Base
import com.ditcalendar.bot.data.InvalidRequest
import com.ditcalendar.bot.service.*
import com.github.kittinunf.result.Result

class CommandExecution(private val calendarService: CalendarService) {

    fun executeCallback(telegramLink: TelegramLink, callbaBackData: String): Result<Base, Exception> =
            if (callbaBackData.startsWith(unassignCallbackCommand)) {
                val taskId: String = callbaBackData.substringAfter("_")
                if (taskId.isNotBlank())
                    calendarService.unassignUserFromTask(taskId, telegramLink)
                else
                    Result.error(InvalidRequest())
            } else if (callbaBackData.startsWith(reloadCallbackCommand)) {
                val variables = callbaBackData.substringAfter("_").split("_")
                val subCalendarName = variables.getOrNull(0)
                val startDate = variables.getOrNull(1)
                val endDate = variables.getOrNull(2)

                if (subCalendarName != null && startDate != null && endDate != null)
                    calendarService.getCalendarAndTask(subCalendarName, startDate, endDate)
                else
                    Result.error(InvalidRequest())
            } else if (callbaBackData.startsWith(assingWithNameCallbackCommand)) {
                executeTaskAssignmentCommand(telegramLink, callbaBackData)
            } else if (callbaBackData.startsWith(assingAnnonCallbackCommand)) {
                executeTaskAssignmentCommand(telegramLink.copy(firstName = null, userName = null), callbaBackData)
            } else
                Result.error(InvalidRequest())

    fun executeTaskAssignmentCommand(telegramLink: TelegramLink, opts: String): Result<Base, Exception> {
        val taskId: String = opts.substringAfter("_")
        return if (taskId.isNotBlank())
            calendarService.assignUserToTask(taskId, telegramLink)
        else
            Result.error(InvalidRequest())
    }

    fun executePublishCalendarCommand(opts: String?): Result<Base, Exception> {
        return if (opts != null) {
            val variables = opts.split(" ")
            val subCalendarName = variables.getOrNull(0)
            val startDate = variables.getOrNull(1)
            val endDate = variables.getOrNull(2)
            if (subCalendarName != null && startDate != null && endDate != null)
                calendarService.getCalendarAndTask(subCalendarName, startDate, endDate)
            else
                Result.error(InvalidRequest())
        } else
            Result.error(InvalidRequest())
    }
}