package com.ditcalendar.bot.domain.dao

import com.ditcalendar.bot.domain.data.PostCalendarMetaInfo
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object PostCalendarMetaInfoTable : IntIdTable() {
    val chatId = long("chatId")
    var messageId = integer("messageId").index()
    val subCalendarId = integer("subCalendarId")
    val startDate = varchar("startDate", 50)
    val endDate = varchar("endDate", 50)
}

fun findOrCreate(newChatId: Long, msgUserId: Int, subCalendar: Int, start: String, end: String): PostCalendarMetaInfo = transaction {
    val result = PostCalendarMetaInfo.find { (PostCalendarMetaInfoTable.messageId eq msgUserId) and (PostCalendarMetaInfoTable.subCalendarId eq subCalendar) }
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

fun findByMessageIdAndSubcalendarId(id: Int, subcalendarId: Int): PostCalendarMetaInfo? = transaction {
    PostCalendarMetaInfo.find { (PostCalendarMetaInfoTable.messageId eq id) and (PostCalendarMetaInfoTable.subCalendarId eq subcalendarId) }
            .firstOrNull()
}

fun find(id: Int): PostCalendarMetaInfo? = transaction {
    PostCalendarMetaInfo.findById(id)
}

fun updateMessageId(metaInfo: PostCalendarMetaInfo, newMessageUserId: Int) = transaction {
    PostCalendarMetaInfoTable.update({ PostCalendarMetaInfoTable.id eq metaInfo.id }) {
        it[messageId] = newMessageUserId
    }
    metaInfo.also { it.messageId = newMessageUserId }
}