package com.ditcalendar.bot.data

import com.ditcalendar.bot.data.core.Base
import com.ditcalendar.bot.data.core.DateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class Task(var id: String,
           @SerialName("series_id")
           var seriesId: Int? = null,
           @SerialName("remote_id")
           var remoteId: String? = null,
           @SerialName("subcalendar_ids")
           var subcalendarIds: List<Int>,
           @SerialName("all_day")
           var isAllDay: Boolean,
           var rrule: String,
           var title: String,
           var who: String?,
           var location: String?,
           var notes: String?,
           var version: String,
           var readonly: Boolean,
           var tz: String?,
           @SerialName("start_dt")
           @Serializable(with = DateSerializer::class)
           var startDate: Date,
           @SerialName("end_dt")
           @Serializable(with = DateSerializer::class)
           var endDate: Date,
           @Serializable(with = DateSerializer::class)
           var ristart_dt: Date?) : Base()


@Serializable
data class Events(var events: List<Task>)
