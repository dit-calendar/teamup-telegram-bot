package com.ditcalendar.bot.domain.data


typealias TelegramLinks = List<TelegramLink>

data class TelegramLink(val chatId: Long,
                        val telegramUserId: Int,
                        val userName: String? = null,
                        val firstName: String? = null)