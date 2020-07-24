package com.ditcalendar.bot.domain.data

sealed class DitBotError(description: String) : RuntimeException(description)

class InvalidRequest(errorMessage: String?) : DitBotError(errorMessage ?: "request invalid") {
    constructor() : this(null)
}

class ServerNotReachable : DitBotError("server need to startup. try again")
class NoSubcalendarFound(name: String) : DitBotError("no subcalendar found with name $name")
class MultipleSubcalendarsFound : DitBotError("found more than one subcalendar")
