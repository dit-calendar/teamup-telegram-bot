package com.ditcalendar.bot.domain.data

import com.ditcalendar.bot.teamup.data.Event
import com.ditcalendar.bot.teamup.data.core.Base

typealias TelegramTaskAssignments = List<TelegramTaskAssignment>

sealed class TelegramTaskAssignment : Base() {
    abstract val task: Event
    abstract val assignedUsers: TelegramLinks
}

class TelegramTaskForAssignment(override val task: Event, override val assignedUsers: TelegramLinks) : TelegramTaskAssignment()

class TelegramTaskForUnassignment(override val task: Event, override val assignedUsers: TelegramLinks) : TelegramTaskAssignment()

class TelegramTaskAfterUnassignment(override val task: Event, override val assignedUsers: TelegramLinks) : TelegramTaskAssignment()