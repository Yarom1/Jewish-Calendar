package com.yarom.jewishcalendar.ui.components

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun ZonedDateTime?.formatTime(): String = this?.format(timeFormatter) ?: "--:--"
