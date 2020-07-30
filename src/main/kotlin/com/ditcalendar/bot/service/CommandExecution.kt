package com.ditcalendar.bot.service

import com.ditcalendar.bot.domain.dao.findOrCreate
import com.ditcalendar.bot.domain.dao.updateName
import com.ditcalendar.bot.domain.data.InvalidRequest
import com.ditcalendar.bot.domain.data.TelegramLink
import com.ditcalendar.bot.teamup.data.SubCalendar
import com.ditcalendar.bot.teamup.data.core.Base
import com.github.kittinunf.result.Result
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

const val assignDeepLinkCommand = "assign_"
const val unassignCallbackCommand = "unassign_"
const val reloadCallbackCommand = "reloadCalendar_"
const val assingWithNameCallbackCommand = "assignme_"
const val assingAnnonCallbackCommand = "assignmeAnnon_"

class CommandExecution(private val calendarService: CalendarService) {

    fun executeCallback(chatId: Int, msgUserId: Int, msgUserFirstName: String, callbaBackData: String): Result<Base, Exception> =
            if (callbaBackData.startsWith(unassignCallbackCommand)) {
                val taskId: String = callbaBackData.substringAfter(unassignCallbackCommand)
                if (taskId.isNotBlank()) {
                    // if user not existing, the DB of Bot was maybe dropped
                    val telegramLink = findOrCreate(chatId, msgUserId)
                    calendarService.unassignUserFromTask(taskId, telegramLink)
                } else
                    Result.error(InvalidRequest())
            } else if (callbaBackData.startsWith(reloadCallbackCommand)) {
                val variables = callbaBackData.substringAfter(reloadCallbackCommand).split("_")
                val subCalendarId = variables.getOrNull(0)?.toInt()
                val startDate = variables.getOrNull(1)
                val endDate = variables.getOrNull(2)

                if (subCalendarId != null && startDate != null && endDate != null)
                    calendarService.getCalendarAndTask(subCalendarId, startDate, endDate)
                else
                    Result.error(InvalidRequest())
            } else if (callbaBackData.startsWith(assingWithNameCallbackCommand)) {
                var telegramLink = findOrCreate(chatId, msgUserId)
                telegramLink = updateName(telegramLink, msgUserFirstName)
                executeTaskAssignmentCommand(telegramLink, callbaBackData)
            } else if (callbaBackData.startsWith(assingAnnonCallbackCommand)) {
                var telegramLink = findOrCreate(chatId, msgUserId)
                telegramLink = updateName(telegramLink, null)
                executeTaskAssignmentCommand(telegramLink, callbaBackData)
            } else
                Result.error(InvalidRequest())

    fun executeTaskAssignmentCommand(telegramLink: TelegramLink, opts: String): Result<Base, Exception> {
        val taskId: String = opts.substringAfter("_")
        return if (taskId.isNotBlank())
            calendarService.assignUserToTask(taskId, telegramLink)
        else
            Result.error(InvalidRequest())
    }

    fun executePublishCalendarCommand(opts: String): Result<SubCalendar, Exception> {
        val variables = opts.split(" ")
        val subCalendarName = variables.getOrNull(0)
        val startDate = variables.getOrNull(1)
        var endDate = variables.getOrNull(2)

        return if (subCalendarName != null) {

            if (isDateInputValid(startDate, endDate)) {
                if (endDate == null)
                    endDate = nextDayAfterMidnight(startDate!!)

                calendarService.getCalendarAndTask(subCalendarName, startDate!!, endDate)
            } else
                Result.error(InvalidRequest("Dateformat sholud be yyyy-MM-dd e.g. 2015-12-31"))

        } else Result.error(InvalidRequest())
    }

    fun executeUpdateMessageAfterAssignment(callbaBackData: String): Result<SubCalendar, Exception> {
        return if (callbaBackData.startsWith(assingWithNameCallbackCommand) || callbaBackData.startsWith(assingAnnonCallbackCommand)) {
            val opts = callbaBackData.removePrefix(assingWithNameCallbackCommand).removePrefix(assingAnnonCallbackCommand)
            val variables = opts.split("_")
            val subCalendarId = variables.getOrNull(0)?.toInt()
            val startDate = variables.getOrNull(1)
            val endDate = variables.getOrNull(2)

            if (subCalendarId != null && startDate != null && endDate != null)
                calendarService.getCalendarAndTask(subCalendarId, startDate, endDate)
            else Result.error(InvalidRequest())
        } else Result.error(InvalidRequest())
    }
}

private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")

private fun isDateInputValid(startDate: String?, endDate: String?): Boolean {
    if (startDate == null)
        return false

    val checkDateInput = Result.of<Unit, Exception> {
        df.parse(startDate)
        if (endDate != null) df.parse(endDate)
    }
    return when (checkDateInput) {
        is Result.Failure -> false
        is Result.Success -> true
    }
}

private fun nextDayAfterMidnight(startDate: String): String {
    val c = Calendar.getInstance()
    c.time = df.parse(startDate)
    c.add(Calendar.DATE, 1)
    return df.format(c.time) + "T04:00:00"
}