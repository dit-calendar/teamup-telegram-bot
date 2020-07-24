package com.ditcalendar.bot.teamup.endpoint

import com.ditcalendar.bot.config.config
import com.ditcalendar.bot.config.teamup_calendar_key
import com.ditcalendar.bot.config.teamup_token
import com.ditcalendar.bot.config.teamup_url
import com.ditcalendar.bot.domain.data.MultipleSubcalendarsFound
import com.ditcalendar.bot.domain.data.NoSubcalendarFound
import com.ditcalendar.bot.teamup.data.SubCalendar
import com.ditcalendar.bot.teamup.data.Subcalendars
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.serialization.responseObject
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class CalendarEndpoint {

    private val config by config()

    private val json = Json(JsonConfiguration.Stable.copy(ignoreUnknownKeys = true))

    private val teamupUrl = config[teamup_url]
    private val teamupToken = config[teamup_token]
    private val teamupCalendarKey = config[teamup_calendar_key]

    fun findSubcalendar(id: Int): Result<SubCalendar, Exception> =
            "$teamupUrl/$teamupCalendarKey/subcalendars/$id"
                    .httpGet()
                    .header(Pair(TEAMUP_TOKEN_HEADER, teamupToken))
                    .responseObject(loader = Subcalendar.serializer(), json = json)
                    .third
                    .map { it.subcalendar }

    fun findSubcalendar(subCalendarName: String): Result<SubCalendar, Exception> {

        val subcalendars = "$teamupUrl/$teamupCalendarKey/subcalendars"
                .httpGet()
                .header(Pair(TEAMUP_TOKEN_HEADER, teamupToken))
                .responseObject(loader = Subcalendars.serializer(), json = json)
                .third

        return subcalendars
                .map { it.subcalendars.filter { calendar -> calendar.name == subCalendarName } }
                .flatMap {
                    when {
                        it.isEmpty() -> Result.error(NoSubcalendarFound(subCalendarName))
                        it.size != 1 -> Result.error(MultipleSubcalendarsFound())
                        else -> Result.success(it[0])
                    }
                }
    }

    //Wrapper
    @Serializable
    private data class Subcalendar(val subcalendar: SubCalendar)
}