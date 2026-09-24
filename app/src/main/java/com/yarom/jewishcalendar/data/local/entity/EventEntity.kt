package com.yarom.jewishcalendar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CalendarOwner { PERSONAL, FAMILY }

/** Recurrence rules per spec 3 — both Gregorian cadences and Hebrew-calendar-native ones. */
enum class RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY_GREGORIAN,
    YEARLY_GREGORIAN,
    ROSH_CHODESH,
    YEARLY_HEBREW,
}

enum class RecurrenceEnd { NEVER, AFTER_COUNT, ON_DATE }

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val location: String? = null,
    val notes: String? = null,
    val calendarOwner: CalendarOwner = CalendarOwner.PERSONAL,
    /** Epoch day (LocalDate.toEpochDay) of the first/anchor occurrence. */
    val startEpochDay: Long,
    /** Minutes since midnight, local time; null means an all-day event. */
    val startMinuteOfDay: Int? = null,
    val isAllDay: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceInterval: Int = 1,
    val recurrenceEnd: RecurrenceEnd = RecurrenceEnd.NEVER,
    val recurrenceEndCount: Int? = null,
    val recurrenceEndEpochDay: Long? = null,
    /** Anchor Hebrew date, populated when [recurrenceType] is YEARLY_HEBREW (birthdays/yahrzeit). */
    val hebrewYear: Int? = null,
    val hebrewMonth: Int? = null,
    val hebrewDay: Int? = null,
    val reminderMinutesBefore: Int? = 15,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    /** Absolute path to a cropped photo saved in app-private storage, null if none was attached. */
    val imagePath: String? = null,
)
