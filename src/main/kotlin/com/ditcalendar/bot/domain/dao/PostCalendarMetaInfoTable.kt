package com.ditcalendar.bot.domain.dao

import org.jetbrains.exposed.dao.id.IntIdTable

object PostCalendarMetaInfoTable : IntIdTable() {
    val chatId = long("chatId")
    val messageId = integer("messageId").uniqueIndex()
    val subCalendarId = integer("subCalendarId")
    val startDate = varchar("startDate", 50)
    val endDate = varchar("endDate", 50)
}