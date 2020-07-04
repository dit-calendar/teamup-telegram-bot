package com.ditcalendar.bot.error

sealed class DitBotError(description: String) : RuntimeException(description)

class InvalidRequest : DitBotError("request invalid")
class ServerNotReachable: DitBotError("server need to startup. try again")
class NoSubcalendarFound(name : String) : DitBotError("no subcalendar found with name $name")
class MultipleSubcalendarsFound() : DitBotError("found more than one subcalendar")
