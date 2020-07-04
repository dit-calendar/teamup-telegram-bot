package com.ditcalendar.bot.telegram

import com.ditcalendar.bot.data.InvalidRequest
import com.ditcalendar.bot.data.TelegramLink
import com.ditcalendar.bot.data.core.Base
import com.ditcalendar.bot.service.*
import com.github.kittinunf.result.Result
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


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
            val startDateString = variables.getOrNull(1)
            var endDateString = variables.getOrNull(2)

            if (subCalendarName != null && startDateString != null) {
                val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
                val checkDateInput = Result.of<Unit, Exception>{
                    df.parse(startDateString)
                    if(endDateString != null) df.parse(endDateString)
                }
                when (checkDateInput) {
                    is Result.Failure -> Result.error(InvalidRequest("Dateformat sholud be yyyy-MM-dd e.g. 2015-12-31"))
                    is Result.Success -> {
                        if(endDateString == null) { // use next day (after midnight)
                            val c = Calendar.getInstance()
                            c.time = df.parse(startDateString)
                            c.add(Calendar.DATE, 1)
                            endDateString = df.format(c.time) + "T04:00:00"
                        }
                        calendarService.getCalendarAndTask(subCalendarName, startDateString, endDateString!!)
                    }
                }
            }
            else
                Result.error(InvalidRequest())
        } else
            Result.error(InvalidRequest())
    }
}