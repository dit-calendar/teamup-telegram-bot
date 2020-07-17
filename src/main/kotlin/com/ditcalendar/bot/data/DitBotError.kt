package com.ditcalendar.bot.data

import com.ditcalendar.bot.telegram.formatter.withMDEscape

sealed class DitBotError(description: String) : RuntimeException(description)

class InvalidRequest(errorMessage: String?) : DitBotError(errorMessage?.withMDEscape() ?: "request invalid") {
    constructor() : this(null)
}

class ServerNotReachable : DitBotError("server need to startup. try again".withMDEscape())
class NoSubcalendarFound(name: String) : DitBotError("no subcalendar found with name $name".withMDEscape())
class MultipleSubcalendarsFound : DitBotError("found more than one subcalendar")
