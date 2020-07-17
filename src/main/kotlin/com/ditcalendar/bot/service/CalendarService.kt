package com.ditcalendar.bot.service

import com.ditcalendar.bot.domain.data.TelegramLink
import com.ditcalendar.bot.domain.data.TelegramTaskAfterUnassignment
import com.ditcalendar.bot.domain.data.TelegramTaskForAssignment
import com.ditcalendar.bot.domain.data.TelegramTaskForUnassignment
import com.ditcalendar.bot.teamup.data.SubCalendar
import com.ditcalendar.bot.teamup.endpoint.CalendarEndpoint
import com.ditcalendar.bot.teamup.endpoint.EventEndpoint
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map

const val unassignCallbackCommand = "unassign_"
const val reloadCallbackCommand = "reloadCalendar_"
const val assingWithNameCallbackCommand = "assignme_"
const val assingAnnonCallbackCommand = "assignmeAnnon_"

class CalendarService(private val calendarEndpoint: CalendarEndpoint,
                      private val eventEndpoint: EventEndpoint) {

    fun getCalendarAndTask(subCalendarName: String, startDate: String, endDate: String): Result<SubCalendar, Exception> =
            calendarEndpoint.findSubcalendar(subCalendarName)
                    .fillCalendar(startDate, endDate)

    fun getCalendarAndTask(id: Int, startDate: String, endDate: String): Result<SubCalendar, Exception> =
            calendarEndpoint.findSubcalendar(id)
                    .fillCalendar(startDate, endDate)

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

    private fun Result<SubCalendar, Exception>.fillCalendar(startDate: String, endDate: String) =
            this.flatMap { calendar: SubCalendar ->
                val tasksResulst = eventEndpoint.findEvents(calendar.id, startDate, endDate)
                tasksResulst.map {
                    calendar.apply {
                        this.startDate = startDate
                        this.endDate = endDate
                        this.tasks = it.events
                                .map { task -> TelegramTaskForAssignment(task, listOf()) } //TODO build telegramLinks from task.who
                    }
                }
            }
}