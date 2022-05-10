package com.ditcalendar.bot.domain.dao

import com.ditcalendar.bot.domain.data.TelegramLink
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object TelegramLinksTable : IntIdTable() {
    val chatId = long("chatId").uniqueIndex()
    val telegramUserId = long("telegramUserId").uniqueIndex()
    val firstName = varchar("firstName", 50).nullable()
}

fun findOrCreate(newChatId: Long, msgUserId: Long): TelegramLink = transaction {
    val result = TelegramLink.find { TelegramLinksTable.telegramUserId eq msgUserId }
    if (result.count() == 0L) {
        TelegramLink.new {
            chatId = newChatId
            telegramUserId = msgUserId
            firstName = null
        }
    } else result.elementAt(0)
}

fun find(msgUserIds: List<Long>): List<TelegramLink> = transaction {
    msgUserIds.map {
        TelegramLink.find { TelegramLinksTable.telegramUserId eq it }
                .elementAtOrElse(0) { _ ->
                    // after DB drop, users are still assigned in teamup
                    TelegramLink.new {
                        chatId = it
                        telegramUserId = it
                        firstName = null
                    }
                }
    }
}

fun updateName(telegramLink: TelegramLink, newFirstName: String?): TelegramLink = transaction {
    TelegramLinksTable.update({ TelegramLinksTable.id eq telegramLink.id }) {
        it[firstName] = newFirstName
    }
    telegramLink.also { it.firstName = newFirstName }
}
