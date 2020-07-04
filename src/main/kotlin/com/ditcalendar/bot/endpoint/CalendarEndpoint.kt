package com.ditcalendar.bot.endpoint

import com.ditcalendar.bot.config.config
import com.ditcalendar.bot.config.teamup_calendar_key
import com.ditcalendar.bot.config.teamup_token
import com.ditcalendar.bot.config.teamup_url
import com.ditcalendar.bot.data.SubCalendar
import com.ditcalendar.bot.data.Subcalendars
import com.ditcalendar.bot.error.MultipleSubcalendarsFound
import com.ditcalendar.bot.error.NoSubcalendarFound
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.serialization.responseObject
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class CalendarEndpoint {

    private val config by config()

    private val json = Json(JsonConfiguration.Stable.copy(ignoreUnknownKeys = true))

    private val teamupUrl = config[teamup_url]
    private val teamupToken = config[teamup_token]
    private val teamupCalendarKey = config[teamup_calendar_key]

    fun findSubcalendar(subCalendarName: String): Result<SubCalendar, Exception> {

        val subcalendars = "$teamupUrl/calendarentries/$teamupCalendarKey/subcalendars"
                .httpGet()
                .header(Pair(TEAMUP_TOKEN_HEADER, teamupToken))
                .responseObject(loader = Subcalendars.serializer(), json = json)
                .third

        return subcalendars.map {
            return when {
                it.subcalendars.isEmpty() -> Result.error(NoSubcalendarFound(subCalendarName))
                it.subcalendars.size != 1 -> Result.error(MultipleSubcalendarsFound())
                else -> Result.success(it.subcalendars[0])
            }
        }
    }
}