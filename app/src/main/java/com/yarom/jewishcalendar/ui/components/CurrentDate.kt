package com.yarom.jewishcalendar.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * The "today" date, refreshed exactly at midnight (spec follow-up: the today marker must move
 * on its own if the app is left open across a day rollover, not just on the next unrelated
 * recomposition).
 */
@Composable
fun rememberCurrentDate(): State<LocalDate> = produceState(initialValue = LocalDate.now()) {
    while (true) {
        value = LocalDate.now()
        val now = LocalDateTime.now()
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
        delay(Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1_000L))
    }
}
