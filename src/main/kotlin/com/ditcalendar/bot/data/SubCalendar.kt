package com.ditcalendar.bot.data

import com.ditcalendar.bot.data.core.Base
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SubCalendar(var id: Int,
                       var name: String,
                       var readonly: Boolean,
                       @Transient
                       var tasks: List<TelegramTaskForAssignment> = listOf()) : Base()

@Serializable
data class Subcalendars(var subcalendars: List<SubCalendar>)
