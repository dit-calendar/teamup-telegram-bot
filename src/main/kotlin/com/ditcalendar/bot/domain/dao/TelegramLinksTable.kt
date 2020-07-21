package com.ditcalendar.bot.domain.dao

import com.ditcalendar.bot.domain.data.TelegramLink
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object TelegramLinksTable : IntIdTable() {
    val chatId = integer("chatId").uniqueIndex()
    val telegramUserId = integer("telegramUserId").uniqueIndex()
    val firstName = varchar("firstName", 50).nullable()
}

fun findOrCreate(newChatId: Int, msgUserId: Int): TelegramLink =
        transaction {
            val result = TelegramLink.find { TelegramLinksTable.telegramUserId eq msgUserId }
            if (result.count() == 0L) {
                TelegramLink.new {
                    chatId = newChatId
                    telegramUserId = msgUserId
                    firstName = null
                }
            } else result.elementAt(0)
        }

fun find(msgUserId: Int): TelegramLink? =
        transaction {
            TelegramLink.find { TelegramLinksTable.telegramUserId eq msgUserId }
                    .elementAtOrNull(0)
        }

fun find(msgUserIds: List<Int>): List<TelegramLink> =
        transaction {
            msgUserIds.mapNotNull {
                TelegramLink.find { TelegramLinksTable.telegramUserId eq it }
                        .elementAtOrNull(0)
            }
        }

fun updateName(telegramLink: TelegramLink, newFirstName: String?): TelegramLink =
        transaction {
            TelegramLinksTable.update({ TelegramLinksTable.id eq telegramLink.id }) {
                it[firstName] = newFirstName
            }
            telegramLink.also { it.firstName = newFirstName }
        }
