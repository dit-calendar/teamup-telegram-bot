package com.ditcalendar.bot.config

import com.natpryce.konfig.*
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties


fun config(): Lazy<Configuration> {
    return lazy {
        systemProperties() overriding
                EnvironmentVariables() overriding
                ConfigurationProperties.fromResource("config.properties")
    }
}

val bot_name = Key("bot.name", stringType)
val webhook_is_enabled = Key("webhook.enabled", booleanType)

val server_port = Key("port", intType)
val telegram_token = Key("telegram.token", stringType)
val heroku_app_name = Key("heroku.app.name", stringType)

val teamup_url = Key("teamup.url", stringType)
val teamup_token = Key("teamup.token", stringType)
val teamup_calendar_key = Key("teamup.calendar.key", stringType)
