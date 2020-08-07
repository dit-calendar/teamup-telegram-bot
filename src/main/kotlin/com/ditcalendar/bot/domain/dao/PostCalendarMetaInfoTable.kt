package com.ditcalendar.bot.domain.dao

import com.ditcalendar.bot.domain.data.PostCalendarMetaInfo
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object PostCalendarMetaInfoTable : IntIdTable() {
    val chatId = long("chatId")
    val messageId = integer("messageId").uniqueIndex()
    val subCalendarId = integer("subCalendarId")
    val startDate = varchar("startDate", 50)
    val endDate = varchar("endDate", 50)
}

fun findOrCreate(newChatId: Long, msgUserId: Int, subCalendar: Int, start: String, end: String): PostCalendarMetaInfo = transaction {
    val result = PostCalendarMetaInfo.find { PostCalendarMetaInfoTable.messageId eq msgUserId }
    if (result.count() == 0L) {
        PostCalendarMetaInfo.new {
            chatId = newChatId
            messageId = msgUserId
            subCalendarId = subCalendar
            startDate = start
            endDate = end
        }
    } else result.elementAt(0)
}

fun find(id: Int): PostCalendarMetaInfo? = transaction {
    PostCalendarMetaInfo.find { PostCalendarMetaInfoTable.messageId eq id }.firstOrNull()
}