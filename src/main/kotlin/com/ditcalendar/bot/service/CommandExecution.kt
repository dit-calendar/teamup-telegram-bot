package com.ditcalendar.bot.service

import com.ditcalendar.bot.config.config
import com.ditcalendar.bot.config.post_calendar_without_subcalendar_name
import com.ditcalendar.bot.domain.dao.findOrCreate
import com.ditcalendar.bot.domain.dao.updateName
import com.ditcalendar.bot.domain.data.InvalidRequest
import com.ditcalendar.bot.domain.data.PostCalendarMetaInfo
import com.ditcalendar.bot.domain.data.TelegramLink
import com.ditcalendar.bot.domain.data.TelegramTaskForUnassignment
import com.ditcalendar.bot.helpMessage
import com.ditcalendar.bot.teamup.data.SubCalendar
import com.ditcalendar.bot.teamup.data.core.Base
import com.elbekD.bot.types.Message
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map

const val assignDeepLinkCommand = "assign_"
const val unassignCallbackCommand = "unassign_"
const val reloadCallbackCommand = "reloadCalendar_"
const val assingWithNameCallbackCommand = "assignme_"
const val assingAnnonCallbackCommand = "assignmeAnnon_"

class CommandExecution(private val calendarService: CalendarService) {

    private val config by config()

    private val postCalendarWithoutSubcalendarName = config[post_calendar_without_subcalendar_name]

    fun executeCallback(chatId: Long, msgUserId: Long, msgUserFirstName: String, callbaBackData: String, msg: Message): Result<Base, Exception> =
            if (callbaBackData.startsWith(unassignCallbackCommand)) {
                val taskId: String = callbaBackData.substringAfter(unassignCallbackCommand).substringBefore("_")
                if (taskId.isNotBlank()) {
                    // if user not existing, the DB of Bot was maybe dropped
                    val telegramLink = findOrCreate(chatId, msgUserId)
                    calendarService.unassignUserFromTask(taskId, telegramLink)
                } else
                    Result.failure(InvalidRequest())
            } else if (callbaBackData.startsWith(reloadCallbackCommand)) {
                reloadCalendar(callbaBackData.substringAfter(reloadCallbackCommand), msg.chat.id, msg.message_id)
            } else if (callbaBackData.startsWith(assingWithNameCallbackCommand)) {
                var telegramLink = findOrCreate(chatId, msgUserId)
                telegramLink = updateName(telegramLink, msgUserFirstName)
                executeTaskAssignmentCommand(telegramLink, callbaBackData)
            } else if (callbaBackData.startsWith(assingAnnonCallbackCommand)) {
                var telegramLink = findOrCreate(chatId, msgUserId)
                telegramLink = updateName(telegramLink, null)
                executeTaskAssignmentCommand(telegramLink, callbaBackData)
            } else
                Result.failure(InvalidRequest())

    private fun executeTaskAssignmentCommand(telegramLink: TelegramLink, opts: String): Result<TelegramTaskForUnassignment, Exception> {
        val variables = opts.substringAfter("_").split("_")
        val taskId = variables.getOrNull(0)
        val metaInfoId = variables.getOrNull(1)?.toInt()
        return if (taskId != null && taskId.isNotBlank() && metaInfoId != null)
            calendarService.assignUserToTask(taskId, telegramLink, metaInfoId)
        else
            Result.failure(InvalidRequest())
    }

    fun executePublishCalendarCommand(opts: String, msg: Message): Result<List<SubCalendar>, Exception> {
        val splitOnDate = opts.split(Regex("\\d{4}-\\d{2}-\\d{2}"))
        val variablesAfterCalendarName = opts.removePrefix(splitOnDate.first()).split(" ")
        val subCalendarName = splitOnDate.first().trimEnd()
        val startDate = variablesAfterCalendarName.getOrNull(0)
        var endDate = variablesAfterCalendarName.getOrNull(1)

        return if (validatePostcalendarRequest(startDate, subCalendarName)) {
            if (isDateInputValid(startDate!!, endDate)) {
                if (endDate == null)
                    endDate = nextDayAfterMidnight(startDate)

                if (subCalendarName.isNotBlank())
                    calendarService.getCalendarAndTask(subCalendarName, startDate, endDate, msg.chat.id, msg.message_id)
                            .map { listOf(it) }
                else
                    calendarService.getCalendarsAndTasks(startDate, endDate, msg.chat.id, msg.message_id)
            } else
                Result.failure(InvalidRequest("Dateformat sholud be yyyy-MM-dd e.g. 2015-12-31"))

        } else Result.failure(InvalidRequest(helpMessage))
    }

    private fun reloadCalendar(opts: String, chatId: Long, messageId: Long): Result<SubCalendar, Exception> {
        val variables = opts.split("_")
        val subCalendarId = variables.getOrNull(0)?.toIntOrNull()
        val startDate = variables.getOrNull(1)
        val endDate = variables.getOrNull(2)

        return if (subCalendarId != null && startDate != null && endDate != null) {
            val postCalendarMetaInfo = findOrCreate(chatId, messageId, subCalendarId, startDate, endDate)
            calendarService.getCalendarAndTask(subCalendarId, startDate, endDate, postCalendarMetaInfo)
        } else Result.failure(InvalidRequest())
    }

    private fun validatePostcalendarRequest(startDate: String?, subCalendarName: String) =
            startDate != null && (postCalendarWithoutSubcalendarName || subCalendarName.isNotBlank())

    fun reloadCalendar(postCalendarMetaInfo: PostCalendarMetaInfo?): Result<SubCalendar, Exception> {
        return if (postCalendarMetaInfo != null)
            calendarService.getCalendarAndTask(postCalendarMetaInfo.subCalendarId, postCalendarMetaInfo.startDate, postCalendarMetaInfo.endDate, postCalendarMetaInfo)
        else Result.failure(InvalidRequest())
    }
}
