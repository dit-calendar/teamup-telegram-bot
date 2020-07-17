package com.ditcalendar.bot.service

import com.ditcalendar.bot.data.*
import com.ditcalendar.bot.endpoint.CalendarEndpoint
import com.ditcalendar.bot.endpoint.EventEndpoint
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map

const val unassignCallbackCommand = "unassign_"
const val reloadCallbackCommand = "reloadCalendar_"
const val assingWithNameCallbackCommand = "assignme_"
const val assingAnnonCallbackCommand = "assignmeAnnon_"

class CalendarService(private val calendarEndpoint: CalendarEndpoint,
                      private val eventEndpoint: EventEndpoint) {


    fun getCalendarAndTask(subCalendarName: String, startDate: String, endDate: String): Result<SubCalendar, Exception> {
        val calendarResult = calendarEndpoint.findSubcalendar(subCalendarName)

        return calendarResult.flatMap { calendar: SubCalendar ->
            val tasksResulst = eventEndpoint.findEvents(calendar.id, startDate, endDate)
            tasksResulst.map {
                calendar.apply { tasks = it.events
                        .map { task -> TelegramTaskForAssignment(task, listOf()) } //TODO build telegramLinks from task.who
                }
            }
        }
    }

    fun assignUserToTask(taskId: String, telegramLink: TelegramLink): Result<TelegramTaskForUnassignment, Exception> =
            eventEndpoint.getEvent(taskId)
                    .flatMap { task ->
                        task.apply { who = "" } //TODO build telegramLinks from string
                        eventEndpoint.updateEvent(task)
                                .map { TelegramTaskForUnassignment(it, listOf()) }
                    }

    fun unassignUserFromTask(taskId: String, telegramLink: TelegramLink): Result<TelegramTaskAfterUnassignment, Exception> =
            eventEndpoint.getEvent(taskId)
                    .flatMap { task ->
                        task.apply { who = "" } //TODO build telegramLinks from string
                        eventEndpoint.updateEvent(task)
                                .map { TelegramTaskAfterUnassignment(it, listOf()) }
                    }
}