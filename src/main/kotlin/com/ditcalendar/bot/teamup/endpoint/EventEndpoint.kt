package com.ditcalendar.bot.teamup.endpoint

import com.ditcalendar.bot.config.config
import com.ditcalendar.bot.config.teamup_calendar_key
import com.ditcalendar.bot.config.teamup_token
import com.ditcalendar.bot.config.teamup_url
import com.ditcalendar.bot.teamup.data.Event
import com.ditcalendar.bot.teamup.data.Events
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.serialization.responseObject
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import kotlinx.serialization.Serializable
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

    fun getEvent(eventId: String): Result<Event, Exception> =
            "$teamupUrl/$teamupCalendarKey/events/$eventId"
                    .httpGet()
                    .header(Pair(TEAMUP_TOKEN_HEADER, teamupToken), Pair("Accept", "application/json"))
                    .responseObject(loader = EventWrappper.serializer(), json = json)
                    .third
                    .map { it.event }

    fun updateEvent(task: Event): Result<Event, Exception> =
            "$teamupUrl/$teamupCalendarKey/events/${task.id}"
                    .httpPut()
                    .header(Pair(TEAMUP_TOKEN_HEADER, teamupToken), Pair("Content-Type", "application/json"), Pair("Accept", "application/json"))
                    .body(json.stringify(Event.serializer(), task))
                    .responseObject(loader = EventWrappper.serializer(), json = json)
                    .third
                    .map { it.event }

    //Wrapper
    @Serializable
    private data class EventWrappper(val event: Event)
}