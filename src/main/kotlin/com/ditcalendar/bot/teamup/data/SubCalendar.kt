package com.ditcalendar.bot.teamup.data

import com.ditcalendar.bot.domain.data.TelegramTaskForAssignment
import com.ditcalendar.bot.teamup.data.core.Base
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SubCalendar(var id: Int,
                       var name: String,
                       var readonly: Boolean,
                       @Transient
                       var tasks: List<TelegramTaskForAssignment> = listOf(),
                       @Transient
                       var startDate: String? = null,
                       @Transient
                       var endDate: String? = null) : Base()

@Serializable
data class Subcalendars(var subcalendars: List<SubCalendar>)
