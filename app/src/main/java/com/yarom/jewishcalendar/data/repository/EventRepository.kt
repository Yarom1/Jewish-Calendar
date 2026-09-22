package com.yarom.jewishcalendar.data.repository

import com.yarom.jewishcalendar.data.local.dao.EventDao
import com.yarom.jewishcalendar.data.local.entity.EventEntity
import com.yarom.jewishcalendar.data.local.entity.RecurrenceEnd
import com.yarom.jewishcalendar.data.local.entity.RecurrenceType
import com.yarom.jewishcalendar.domain.hebrew.HebrewDateConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

data class EventOccurrence(
    val event: EventEntity,
    val date: LocalDate,
)

/**
 * Owns event CRUD and expands recurrence rules (spec 3 — Gregorian + Hebrew-native cadences)
 * into concrete occurrences for a requested date window, since Room can only store the rule,
 * not evaluate Hebrew-calendar arithmetic.
 */
class EventRepository(
    private val dao: EventDao,
    private val hebrewDateConverter: HebrewDateConverter = HebrewDateConverter(),
) {

    fun observeOccurrences(range: ClosedRange<LocalDate>): Flow<List<EventOccurrence>> =
        dao.observeRelevant(range.start.toEpochDay()).map { events ->
            events.flatMap { occurrencesFor(it, range) }.sortedBy { it.date }
        }

    /** The raw event rules (not expanded occurrences) - for a management/edit list. */
    fun observeAll(): Flow<List<EventEntity>> = dao.observeAll()

    suspend fun save(event: EventEntity): Long = dao.upsert(event)

    suspend fun delete(event: EventEntity) = dao.delete(event)

    suspend fun getById(id: Long): EventEntity? = dao.getById(id)

    private fun occurrencesFor(event: EventEntity, range: ClosedRange<LocalDate>): List<EventOccurrence> {
        val anchor = LocalDate.ofEpochDay(event.startEpochDay)
        val rangeStart = maxOf(anchor, range.start)
        val endCap = endCap(event, range.endInclusive)
        if (rangeStart > endCap) return emptyList()

        return when (event.recurrenceType) {
            RecurrenceType.NONE ->
                if (anchor in range.start..range.endInclusive) listOf(EventOccurrence(event, anchor)) else emptyList()

            RecurrenceType.DAILY -> stepOccurrences(event, anchor, rangeStart, endCap) { it.plusDays(event.recurrenceInterval.toLong()) }

            RecurrenceType.WEEKLY -> stepOccurrences(event, anchor, rangeStart, endCap) { it.plusWeeks(event.recurrenceInterval.toLong()) }

            RecurrenceType.MONTHLY_GREGORIAN -> stepOccurrences(event, anchor, rangeStart, endCap) { it.plusMonths(event.recurrenceInterval.toLong()) }

            RecurrenceType.YEARLY_GREGORIAN -> stepOccurrences(event, anchor, rangeStart, endCap) { it.plusYears(event.recurrenceInterval.toLong()) }

            RecurrenceType.ROSH_CHODESH -> {
                var date = anchor
                val results = mutableListOf<EventOccurrence>()
                var occurrenceIndex = 0
                while (date <= endCap) {
                    val hebrew = hebrewDateConverter.fromGregorian(date)
                    if (hebrew.isRoshChodesh) {
                        occurrenceIndex++
                        if (date >= rangeStart && withinCount(event, occurrenceIndex)) {
                            results += EventOccurrence(event, date)
                        }
                    }
                    date = date.plusDays(1)
                }
                results
            }

            RecurrenceType.YEARLY_HEBREW -> {
                val hYear = event.hebrewYear ?: return emptyList()
                val hMonth = event.hebrewMonth ?: return emptyList()
                val hDay = event.hebrewDay ?: return emptyList()
                val startYearOffset = hebrewDateConverter.fromGregorian(rangeStart).hebrewYear - hYear
                val results = mutableListOf<EventOccurrence>()
                var offset = maxOf(0, startYearOffset - 1)
                while (true) {
                    val candidateYear = hYear + offset
                    val date = runCatching {
                        hebrewDateConverter.hebrewDateToGregorian(candidateYear, hMonth, hDay)
                    }.getOrNull() ?: break
                    if (date > endCap) break
                    if (date >= rangeStart && withinCount(event, offset + 1)) {
                        results += EventOccurrence(event, date)
                    }
                    offset++
                    if (offset > 500) break // safety valve
                }
                results
            }
        }
    }

    private inline fun stepOccurrences(
        event: EventEntity,
        anchor: LocalDate,
        rangeStart: LocalDate,
        endCap: LocalDate,
        step: (LocalDate) -> LocalDate,
    ): List<EventOccurrence> {
        val results = mutableListOf<EventOccurrence>()
        var date = anchor
        var occurrenceIndex = 1
        while (date <= endCap) {
            if (date >= rangeStart && withinCount(event, occurrenceIndex)) {
                results += EventOccurrence(event, date)
            }
            date = step(date)
            occurrenceIndex++
        }
        return results
    }

    private fun withinCount(event: EventEntity, occurrenceIndex: Int): Boolean =
        event.recurrenceEnd != RecurrenceEnd.AFTER_COUNT || occurrenceIndex <= (event.recurrenceEndCount ?: Int.MAX_VALUE)

    private fun endCap(event: EventEntity, rangeEnd: LocalDate): LocalDate {
        val recurrenceEndDate = event.recurrenceEndEpochDay?.let(LocalDate::ofEpochDay)
        return if (event.recurrenceEnd == RecurrenceEnd.ON_DATE && recurrenceEndDate != null) {
            minOf(rangeEnd, recurrenceEndDate)
        } else {
            rangeEnd
        }
    }
}
