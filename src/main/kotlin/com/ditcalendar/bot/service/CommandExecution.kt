package com.ditcalendar.bot.service

import com.ditcalendar.bot.domain.dao.findOrCreate
import com.ditcalendar.bot.domain.dao.updateName
import com.ditcalendar.bot.domain.data.InvalidRequest
import com.ditcalendar.bot.domain.data.PostCalendarMetaInfo
import com.ditcalendar.bot.domain.data.TelegramLink
import com.ditcalendar.bot.domain.data.TelegramTaskForUnassignment
import com.ditcalendar.bot.teamup.data.SubCalendar
import com.ditcalendar.bot.teamup.data.core.Base
import com.elbekD.bot.types.Message
import com.github.kittinunf.result.Result

const val assignDeepLinkCommand = "assign_"
const val unassignCallbackCommand = "unassign_"
const val reloadCallbackCommand = "reloadCalendar_"
const val assingWithNameCallbackCommand = "assignme_"
const val assingAnnonCallbackCommand = "assignmeAnnon_"

class CommandExecution(private val calendarService: CalendarService) {

    fun executeCallback(chatId: Int, msgUserId: Int, msgUserFirstName: String, callbaBackData: String, msg: Message): Result<Base, Exception> =
            if (callbaBackData.startsWith(unassignCallbackCommand)) {
                val taskId: String = callbaBackData.substringAfter(unassignCallbackCommand)
                if (taskId.isNotBlank()) {
                    // if user not existing, the DB of Bot was maybe dropped
                    val telegramLink = findOrCreate(chatId, msgUserId)
                    calendarService.unassignUserFromTask(taskId, telegramLink)
                } else
                    Result.error(InvalidRequest())
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
                Result.error(InvalidRequest())

    fun executeTaskAssignmentCommand(telegramLink: TelegramLink, opts: String): Result<TelegramTaskForUnassignment, Exception> {
        val variables = opts.substringAfter("_").split("_")
        val taskId = variables.getOrNull(0)
        return if (taskId != null && taskId.isNotBlank())
            calendarService.assignUserToTask(taskId, telegramLink)
        else
            Result.error(InvalidRequest())
    }

    fun executePublishCalendarCommand(opts: String, msg: Message): Result<SubCalendar, Exception> {
        val variables = opts.split(" ")
        val subCalendarName = variables.getOrNull(0)
        val startDate = variables.getOrNull(1)
        var endDate = variables.getOrNull(2)

        return if (subCalendarName != null) {

            if (isDateInputValid(startDate, endDate)) {
                if (endDate == null)
                    endDate = nextDayAfterMidnight(startDate!!)

                calendarService.getCalendarAndTask(subCalendarName, startDate!!, endDate, msg.chat.id, msg.message_id)
            } else
                Result.error(InvalidRequest("Dateformat sholud be yyyy-MM-dd e.g. 2015-12-31"))

        } else Result.error(InvalidRequest())
    }

    fun reloadCalendar(opts: String, chatId: Long, messageId: Int): Result<SubCalendar, Exception> {
        val variables = opts.split("_")
        val subCalendarId = variables.getOrNull(0)?.toIntOrNull()
        val startDate = variables.getOrNull(1)
        val endDate = variables.getOrNull(2)

        return if (subCalendarId != null && startDate != null && endDate != null)
            calendarService.getCalendarAndTask(subCalendarId, startDate, endDate, chatId, messageId)
        else Result.error(InvalidRequest())
    }

    fun reloadCalendar(postCalendarMetaInfo: PostCalendarMetaInfo?): Result<SubCalendar, Exception> {
        return if (postCalendarMetaInfo != null)
            calendarService.getCalendarAndTask(postCalendarMetaInfo.subCalendarId, postCalendarMetaInfo.startDate, postCalendarMetaInfo.endDate, postCalendarMetaInfo.chatId, postCalendarMetaInfo.messageId)
        else Result.error(InvalidRequest())
    }
}
