package com.ditcalendar.bot.endpoint

import com.ditcalendar.bot.config.config
import com.ditcalendar.bot.config.teamup_calendar_key
import com.ditcalendar.bot.config.teamup_token
import com.ditcalendar.bot.config.teamup_url
import com.ditcalendar.bot.data.Events
import com.ditcalendar.bot.data.Task
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.serialization.responseObject
import com.github.kittinunf.result.Result
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class EventEndpoint {

    private val config by config()

    private val json = Json(JsonConfiguration.Stable.copy(ignoreUnknownKeys = true))

    private val teamupUrl = config[teamup_url]
    private val teamupToken = config[teamup_token]
    private val teamupCalendarKey = config[teamup_calendar_key]

    fun findEvents(subcalendarId: Int, startDate: String, endDate: String): Result<Events, Exception> =
            "$teamupUrl/$teamupCalendarKey/events?startDate=$startDate&endDate=$endDate&subcalendarId[]=$subcalendarId"
                    .httpGet()
                    .header(Pair(TEAMUP_TOKEN_HEADER, teamupToken))
                    .responseObject(loader = Events.serializer(), json = json)
                    .third

    fun getEvent(eventId: String): Result<Task, Exception> =
            "$teamupUrl/$teamupCalendarKey/event/$eventId"
                    .httpGet()
                    .header(Pair(TEAMUP_TOKEN_HEADER, teamupToken))
                    .responseObject(loader = Task.serializer(), json = json)
                    .third

    fun updateEvent(task: Task): Result<Task, Exception> =
            "$teamupUrl/$teamupCalendarKey/event/${task.id}"
                    .httpPut()
                    .header(Pair(TEAMUP_TOKEN_HEADER, teamupToken))
                    .body(json.stringify(Task.serializer(), task))
                    .responseObject(loader = Task.serializer(), json = json)
                    .third
}