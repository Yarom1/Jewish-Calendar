package com.yarom.jewishcalendar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yarom.jewishcalendar.data.local.entity.CalendarOwner
import com.yarom.jewishcalendar.data.local.entity.EventEntity
import com.yarom.jewishcalendar.data.local.entity.RecurrenceEnd
import com.yarom.jewishcalendar.data.local.entity.RecurrenceType
import com.yarom.jewishcalendar.data.repository.EventRepository
import com.yarom.jewishcalendar.domain.hebrew.HebrewDateConverter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Draft state backing the "add/edit event" bottom sheet (spec 3). [id] is 0 for a new event. */
data class NewEventDraft(
    val date: LocalDate,
    val title: String = "",
    val location: String = "",
    val notes: String = "",
    val hour: Int = 20,
    val minute: Int = 0,
    val isAllDay: Boolean = false,
    val calendarOwner: CalendarOwner = CalendarOwner.PERSONAL,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceEnd: RecurrenceEnd = RecurrenceEnd.NEVER,
    val recurrenceEndCount: Int? = null,
    val recurrenceEndDate: LocalDate? = null,
    val reminderMinutesBefore: Int? = 15,
    val id: Long = 0,
) {
    companion object {
        fun from(event: EventEntity): NewEventDraft {
            val minuteOfDay = event.startMinuteOfDay
            return NewEventDraft(
                date = LocalDate.ofEpochDay(event.startEpochDay),
                title = event.title,
                location = event.location ?: "",
                notes = event.notes ?: "",
                hour = if (minuteOfDay != null) minuteOfDay / 60 else 20,
                minute = if (minuteOfDay != null) minuteOfDay % 60 else 0,
                isAllDay = event.isAllDay,
                calendarOwner = event.calendarOwner,
                recurrenceType = event.recurrenceType,
                recurrenceEnd = event.recurrenceEnd,
                recurrenceEndCount = event.recurrenceEndCount,
                recurrenceEndDate = event.recurrenceEndEpochDay?.let(LocalDate::ofEpochDay),
                reminderMinutesBefore = event.reminderMinutesBefore,
                id = event.id,
            )
        }
    }
}

class EventViewModel(
    private val eventRepository: EventRepository,
    private val hebrewDateConverter: HebrewDateConverter,
) : ViewModel() {

    /** All event rules (not expanded occurrences), for the events management screen. */
    val allEvents: StateFlow<List<EventEntity>> = eventRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(draft: NewEventDraft, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val hebrewAnchor = if (draft.recurrenceType == RecurrenceType.YEARLY_HEBREW) {
                hebrewDateConverter.fromGregorian(draft.date)
            } else null

            eventRepository.save(
                EventEntity(
                    id = draft.id,
                    title = draft.title,
                    location = draft.location.ifBlank { null },
                    notes = draft.notes.ifBlank { null },
                    calendarOwner = draft.calendarOwner,
                    startEpochDay = draft.date.toEpochDay(),
                    startMinuteOfDay = if (draft.isAllDay) null else draft.hour * 60 + draft.minute,
                    isAllDay = draft.isAllDay,
                    recurrenceType = draft.recurrenceType,
                    recurrenceEnd = draft.recurrenceEnd,
                    recurrenceEndCount = draft.recurrenceEndCount,
                    recurrenceEndEpochDay = draft.recurrenceEndDate?.toEpochDay(),
                    hebrewYear = hebrewAnchor?.hebrewYear,
                    hebrewMonth = hebrewAnchor?.hebrewMonth,
                    hebrewDay = hebrewAnchor?.hebrewDayOfMonth,
                    reminderMinutesBefore = draft.reminderMinutesBefore,
                ),
            )
            onSaved()
        }
    }

    fun delete(event: EventEntity) {
        viewModelScope.launch { eventRepository.delete(event) }
    }
}
