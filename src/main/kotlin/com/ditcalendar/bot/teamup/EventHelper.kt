package com.ditcalendar.bot.teamup

fun removeUserFromWho(oldWho: String?, telegramLinkUserId: String): String? =
        when {
            oldWho.isNullOrBlank() -> oldWho

            telegramLinkUserId in oldWho ->
                oldWho.replace(telegramLinkUserId, "").replace(";;", ";")

            else -> oldWho
        }

fun addUserToWho(oldWho: String?, telegramLinkUserId: String): String =
        when {
            oldWho.isNullOrBlank() -> telegramLinkUserId
            telegramLinkUserId in oldWho -> oldWho
            else -> "$telegramLinkUserId;$oldWho"
        }

fun parseWhoToIds(who: String?): List<Long> =
        if (who.isNullOrBlank()) listOf()
        else who.split(";")
                .filter { it.isNotBlank() }
                .mapNotNull { it.toLongOrNull() }