package com.ditcalendar.bot.telegram.formatter

import com.ditcalendar.bot.config.bot_name
import com.ditcalendar.bot.config.config
import com.ditcalendar.bot.domain.data.*
import com.ditcalendar.bot.teamup.data.Event
import com.ditcalendar.bot.teamup.data.SubCalendar
import java.text.SimpleDateFormat


private val config by config()

private val botName = config[bot_name]

private val formatter = SimpleDateFormat("HH:mm")

private val dateFormatter = SimpleDateFormat("dd.MM")

fun TelegramTaskAssignment.toMarkdown(): String =
        when (this) {
            is TelegramTaskForAssignment ->
                """
                    *${task.formatTime()}* \- ${task.title.withMDEscape()}
                    Wer?: ${assignedUsers.toMarkdown()} [assign me](https://t.me/$botName?start=assign_${task.id})
                """.trimIndent()

            is TelegramTaskForUnassignment -> {
                val formattedDescription = ""
                /*if (task.notes!!.isNotBlank())
                    System.lineSeparator() + task.notes!!.withMDEscape()
                else ""*/
                "*erfolgreich hinzugefügt:*" + System.lineSeparator() +
                        "*${formatter.format(task.startDate.time)} Uhr* \\- ${task.title.withMDEscape()}$formattedDescription" + System.lineSeparator() +
                        "Wer?: ${assignedUsers.toMarkdown()}"
            }

            is TelegramTaskAfterUnassignment ->
                """
                    *erfolgreich ausgetragen*:
                    *${formatter.format(task.startDate.time)} Uhr* \- ${task.title.withMDEscape()}
                """.trimIndent()
        }

private fun Event.formatTime(): String {
    var timeString = formatter.format(this.startDate)
    timeString += if (this.endDate != null) " \\- " + formatter.format(this.endDate) else ""
    return timeString + " Uhr"
}

@JvmName("toMarkdownForTelegramLinks")
private fun TelegramLinks.toMarkdown(): String {
    var firstNames = this.filter { it.firstName != null }.joinToString(", ") { it.firstName!!.withMDEscape() }
    val anonymousCount = this.count { it.firstName == null }
    firstNames += if (anonymousCount != 0) " \\+$anonymousCount" else ""
    return firstNames
}


fun TelegramTaskAssignments.toMarkdown(): String = System.lineSeparator() +
        joinToString(separator = System.lineSeparator()) { it.toMarkdown() }

fun SubCalendar.toMarkdown(): String {
    return """
            *$name* am ${dateFormatter.format(tasks[0].task.startDate).withMDEscape()}
        """.trimIndent() + tasks.toMarkdown()
}

fun String.withMDEscape() =
        this.replace("\"", "")
                .replace("\\", "\\\\")
                .replace("`", "\\`")
                .replace("!", "\\!")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("_", "\\_")
                .replace(".", "\\.")
                .replace("*", "\\*")
                .replace("#", "\\#")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")