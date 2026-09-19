package com.yarom.jewishcalendar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yarom.jewishcalendar.data.local.entity.CalendarOwner
import com.yarom.jewishcalendar.data.local.entity.EventEntity
import com.yarom.jewishcalendar.data.local.entity.RecurrenceEnd
import com.yarom.jewishcalendar.data.local.entity.RecurrenceType
import com.yarom.jewishcalendar.data.repository.EventRepository
import com.yarom.jewishcalendar.domain.hebrew.HebrewDateConverter
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Draft state backing the long-press "add event" bottom sheet (spec 3). */
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
)

class EventViewModel(
    private val eventRepository: EventRepository,
    private val hebrewDateConverter: HebrewDateConverter,
) : ViewModel() {

    fun save(draft: NewEventDraft, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val hebrewAnchor = if (draft.recurrenceType == RecurrenceType.YEARLY_HEBREW) {
                hebrewDateConverter.fromGregorian(draft.date)
            } else null

            eventRepository.save(
                EventEntity(
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
