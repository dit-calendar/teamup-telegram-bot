package com.ditcalendar.bot.service

import com.ditcalendar.bot.data.*
import com.ditcalendar.bot.data.core.Base
import com.ditcalendar.bot.endpoint.CalendarEndpoint
import com.ditcalendar.bot.endpoint.EventEndpoint
import com.ditcalendar.bot.error.InvalidRequest
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map

const val unassignCallbackCommand = "unassign_"
const val reloadCallbackCommand = "reloadCalendar_"
const val assingWithNameCallbackCommand = "assignme_"
const val assingAnnonCallbackCommand = "assignmeAnnon_"

class DitCalendarService {

    private val calendarEndpoint = CalendarEndpoint()
    private val eventEndpoint = EventEndpoint()

    fun executeCallback(telegramLink: TelegramLink, callbaBackData: String): Result<Base, Exception> =
            if (callbaBackData.startsWith(unassignCallbackCommand)) {
                val taskId: String = callbaBackData.substringAfter("_")
                if (taskId.isNotBlank())
                    unassignUserFromTask(taskId, telegramLink)
                else
                    Result.error(InvalidRequest())
            } else if (callbaBackData.startsWith(reloadCallbackCommand)) {
                val variables = callbaBackData.substringAfter("_").split("_")
                val subCalendarName = variables.getOrNull(0)
                val startDate = variables.getOrNull(1)
                val endDate = variables.getOrNull(2)

                if(subCalendarName != null && startDate != null && endDate != null)
                    getCalendarAndTask(subCalendarName, startDate, endDate)
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
            assignUserToTask(taskId, telegramLink)
        else
            Result.error(InvalidRequest())
    }

    fun executePublishCalendarCommand(opts: String?): Result<Base, Exception> {
        return if (opts != null) {
            val variables = opts.split(" ")
            val subCalendarName = variables.getOrNull(0)
            val startDate = variables.getOrNull(1)
            val endDate = variables.getOrNull(2)
            if(subCalendarName != null && startDate != null && endDate != null)
                getCalendarAndTask(subCalendarName, startDate, endDate)
            else
                Result.error(InvalidRequest())
        }
        else
            Result.error(InvalidRequest())
    }


    private fun getCalendarAndTask(subCalendarName: String, startDate: String, endDate: String): Result<SubCalendar, Exception> {
        val calendarResult = calendarEndpoint.findSubcalendar(subCalendarName)

        return calendarResult.flatMap { calendar: SubCalendar ->
            val tasksResulst = eventEndpoint.findEvents(calendar.id, startDate, endDate)
            tasksResulst.map {
                calendar.apply { tasks = it.events.map { task -> TelegramTaskForAssignment(task, null!!) } } //TODO build telegramLinks from task.who
            }
        }
    }

    private fun assignUserToTask(taskId: String, telegramLink: TelegramLink): Result<TelegramTaskForUnassignment, Exception> =
            eventEndpoint.getEvent(taskId)
                    .flatMap { task ->
                        task.apply { who = "" } //TODO build telegramLinks from string
                        eventEndpoint.updateEvent(task)
                                .map { TelegramTaskForUnassignment(it, null!!) }
                    }

    private fun unassignUserFromTask(taskId: String, telegramLink: TelegramLink): Result<TelegramTaskAfterUnassignment, Exception> =
            eventEndpoint.getEvent(taskId)
                    .flatMap { task ->
                        task.apply { who = "" } //TODO build telegramLinks from string
                        eventEndpoint.updateEvent(task)
                                .map { TelegramTaskAfterUnassignment(it, null!!) }
                    }
}