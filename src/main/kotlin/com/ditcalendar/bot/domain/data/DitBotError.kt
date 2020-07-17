package com.ditcalendar.bot.domain.data

import com.ditcalendar.bot.telegram.formatter.withMDEscape

sealed class DitBotError(description: String) : RuntimeException(description)

class InvalidRequest(errorMessage: String?) : DitBotError(errorMessage?.withMDEscape() ?: "request invalid") {
    constructor() : this(null)
}

class ServerNotReachable : DitBotError("server need to startup. try again".withMDEscape())
class NoSubcalendarFound(name: String) : DitBotError("no subcalendar found with name $name".withMDEscape())
class MultipleSubcalendarsFound : DitBotError("found more than one subcalendar")
class DBUserNotFound : DitBotError("user could not be found")
