package com.yarom.jewishcalendar.ui.screens.addevent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarom.jewishcalendar.R
import com.yarom.jewishcalendar.data.local.entity.CalendarOwner
import com.yarom.jewishcalendar.data.local.entity.EventEntity
import com.yarom.jewishcalendar.data.local.entity.RecurrenceType
import com.yarom.jewishcalendar.ui.CalendarViewModel
import com.yarom.jewishcalendar.ui.EventViewModel
import com.yarom.jewishcalendar.ui.NewEventDraft
import com.yarom.jewishcalendar.ui.components.DateSearchDialog
import com.yarom.jewishcalendar.ui.theme.BrassGold
import com.yarom.jewishcalendar.ui.theme.Burgundy
import com.yarom.jewishcalendar.ui.theme.DeepTeal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** The three cadences exposed in the UI (spec follow-up); each maps to a concrete
 * [RecurrenceType] once combined with the Hebrew/Gregorian toggle below. */
private enum class RecurrenceFrequency { WEEKLY, MONTHLY, YEARLY }

private val hebrewWeekdayNames = listOf("ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת") // Sun..Sat

private fun RecurrenceType.toFrequencyOrNull(): RecurrenceFrequency? = when (this) {
    RecurrenceType.WEEKLY -> RecurrenceFrequency.WEEKLY
    RecurrenceType.MONTHLY_GREGORIAN, RecurrenceType.ROSH_CHODESH -> RecurrenceFrequency.MONTHLY
    RecurrenceType.YEARLY_GREGORIAN, RecurrenceType.YEARLY_HEBREW -> RecurrenceFrequency.YEARLY
    RecurrenceType.NONE, RecurrenceType.DAILY -> null
}

private fun RecurrenceType.isHebrewCadence(): Boolean =
    this == RecurrenceType.ROSH_CHODESH || this == RecurrenceType.YEARLY_HEBREW

private fun resolveRecurrenceType(isRecurring: Boolean, frequency: RecurrenceFrequency, isHebrew: Boolean): RecurrenceType {
    if (!isRecurring) return RecurrenceType.NONE
    return when (frequency) {
        RecurrenceFrequency.WEEKLY -> RecurrenceType.WEEKLY
        RecurrenceFrequency.MONTHLY -> if (isHebrew) RecurrenceType.ROSH_CHODESH else RecurrenceType.MONTHLY_GREGORIAN
        RecurrenceFrequency.YEARLY -> if (isHebrew) RecurrenceType.YEARLY_HEBREW else RecurrenceType.YEARLY_GREGORIAN
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventSheet(
    date: LocalDate,
    calendarViewModel: CalendarViewModel,
    eventViewModel: EventViewModel,
    onDismiss: () -> Unit,
    existingEvent: EventEntity? = null,
) {
    val sheetState = rememberModalBottomSheetState()
    val initialDraft = remember(existingEvent) {
        existingEvent?.let { NewEventDraft.from(it) } ?: NewEventDraft(date = date)
    }
    var title by remember { mutableStateOf(initialDraft.title) }
    var location by remember { mutableStateOf(initialDraft.location) }
    var notes by remember { mutableStateOf(initialDraft.notes) }
    var eventDate by remember { mutableStateOf(initialDraft.date) }
    var showDatePicker by remember { mutableStateOf(false) }
    var hour by remember { mutableStateOf(initialDraft.hour) }
    var minute by remember { mutableStateOf(initialDraft.minute) }
    var isFamily by remember { mutableStateOf(initialDraft.calendarOwner == CalendarOwner.FAMILY) }
    // A recurring event is settable from any entry point - creating it on a specific calendar
    // day only fixes the anchor date, never forecloses recurrence (spec follow-up).
    var isRecurring by remember { mutableStateOf(initialDraft.recurrenceType != RecurrenceType.NONE) }
    var frequency by remember {
        mutableStateOf(initialDraft.recurrenceType.toFrequencyOrNull() ?: RecurrenceFrequency.WEEKLY)
    }
    var isHebrewCadence by remember { mutableStateOf(initialDraft.recurrenceType.isHebrewCadence()) }

    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
        checkedTrackColor = DeepTeal,
        uncheckedThumbColor = BrassGold,
        uncheckedTrackColor = BrassGold.copy(alpha = 0.3f),
    )
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = DeepTeal,
        unfocusedBorderColor = DeepTeal.copy(alpha = 0.4f),
        focusedLabelColor = DeepTeal,
        cursorColor = DeepTeal,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = DeepTeal,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = if (existingEvent != null) "עריכת אירוע" else stringResourceCompat(R.string.add_event_title),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = DeepTeal,
                style = MaterialTheme.typography.titleLarge,
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResourceCompat(R.string.event_name_label)) },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text(stringResourceCompat(R.string.event_location_label)) },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )

            // The date the event (and, for a recurring rule, its cadence) anchors on - always
            // editable here, regardless of which day it was opened from (spec follow-up).
            SectionLabel("תאריך")
            val hebrewEventDate = remember(eventDate) { calendarViewModel.hebrewDateFor(eventDate) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text(
                        eventDate.format(DateTimeFormatter.ofPattern("EEEE, d/M/yyyy", Locale("he"))),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "${hebrewEventDate.hebrewDayOfMonthLabel} ${hebrewEventDate.hebrewMonthName} ${hebrewEventDate.hebrewYearLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepTeal.copy(alpha = 0.75f),
                    )
                }
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "בחירת תאריך", tint = DeepTeal)
                }
            }

            SectionLabel("שעה")
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Digital time always reads left-to-right, even in an RTL layout (nobody reads
                // "20:00" as "00:02") - force LTR just for the stepper pair (spec follow-up: the
                // hour/minute steppers previously inherited RTL mirroring and looked backwards).
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TimeStepper(value = hour, range = 0..23, onChange = { hour = it })
                        Text(":", modifier = Modifier.padding(horizontal = 4.dp), fontWeight = FontWeight.Bold, color = DeepTeal)
                        TimeStepper(value = minute, range = 0..55, step = 5, onChange = { minute = it })
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResourceCompat(R.string.event_calendar_personal), modifier = Modifier.padding(end = 8.dp))
                Switch(checked = isFamily, onCheckedChange = { isFamily = it }, colors = switchColors)
                Text(stringResourceCompat(R.string.event_calendar_family), modifier = Modifier.padding(start = 8.dp))
            }

            SectionLabel("חזרתיות")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("חד פעמי", modifier = Modifier.padding(end = 8.dp))
                Switch(checked = isRecurring, onCheckedChange = { isRecurring = it }, colors = switchColors)
                Text("מחזורי", modifier = Modifier.padding(start = 8.dp))
            }

            if (isRecurring) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FrequencyOption("שבועי", frequency == RecurrenceFrequency.WEEKLY) { frequency = RecurrenceFrequency.WEEKLY }
                    FrequencyOption("חודשי", frequency == RecurrenceFrequency.MONTHLY) { frequency = RecurrenceFrequency.MONTHLY }
                    FrequencyOption("שנתי", frequency == RecurrenceFrequency.YEARLY) { frequency = RecurrenceFrequency.YEARLY }
                }
                if (frequency != RecurrenceFrequency.WEEKLY) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("לועזי", modifier = Modifier.padding(end = 8.dp))
                        Switch(checked = isHebrewCadence, onCheckedChange = { isHebrewCadence = it }, colors = switchColors)
                        Text(
                            if (frequency == RecurrenceFrequency.MONTHLY) "עברי (ראש חודש)" else "עברי",
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
                Text(
                    text = recurrenceDescription(eventDate, hebrewEventDate, frequency, isHebrewCadence),
                    fontSize = 12.sp,
                    color = DeepTeal.copy(alpha = 0.8f),
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResourceCompat(R.string.event_notes_label)) },
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )

            if (existingEvent != null) {
                Button(
                    onClick = {
                        eventViewModel.delete(existingEvent)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("מחיקת אירוע")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = BrassGold, contentColor = DeepTeal),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResourceCompat(R.string.cancel))
                }
                Button(
                    onClick = {
                        eventViewModel.save(
                            NewEventDraft(
                                date = eventDate,
                                title = title,
                                location = location,
                                notes = notes,
                                hour = hour,
                                minute = minute,
                                calendarOwner = if (isFamily) CalendarOwner.FAMILY else CalendarOwner.PERSONAL,
                                recurrenceType = resolveRecurrenceType(isRecurring, frequency, isHebrewCadence),
                                id = existingEvent?.id ?: 0,
                            ),
                            onSaved = onDismiss,
                        )
                    },
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResourceCompat(R.string.save))
                }
            }
        }
    }

    if (showDatePicker) {
        DateSearchDialog(
            onDismiss = { showDatePicker = false },
            onGregorianDateChosen = { picked ->
                eventDate = picked
                showDatePicker = false
            },
            onHebrewDateChosen = { year, month, day ->
                calendarViewModel.gregorianForHebrew(year, month, day)?.let { eventDate = it }
                showDatePicker = false
            },
        )
    }
}

/** Human-readable description of what the chosen cadence actually recurs on, computed from
 * [eventDate] - e.g. "כל יום שלישי" for a weekly event anchored on a Tuesday (spec follow-up:
 * previously this was implicit and unreviewable once the sheet was open). */
private fun recurrenceDescription(
    eventDate: LocalDate,
    hebrewEventDate: com.yarom.jewishcalendar.domain.hebrew.HebrewDate,
    frequency: RecurrenceFrequency,
    isHebrewCadence: Boolean,
): String = when (frequency) {
    RecurrenceFrequency.WEEKLY -> "יחזור כל יום ${hebrewWeekdayNames[eventDate.dayOfWeek.value % 7]}"
    RecurrenceFrequency.MONTHLY -> if (isHebrewCadence) {
        "יחזור בכל ראש חודש, החל מ-${eventDate.format(DateTimeFormatter.ofPattern("d/M/yyyy"))}"
    } else {
        "יחזור בכל ${eventDate.dayOfMonth} לחודש הלועזי"
    }
    RecurrenceFrequency.YEARLY -> if (isHebrewCadence) {
        "יחזור כל שנה ב-${hebrewEventDate.hebrewDayOfMonthLabel} ${hebrewEventDate.hebrewMonthName}"
    } else {
        val monthName = eventDate.month.getDisplayName(TextStyle.FULL, Locale("he"))
        "יחזור כל שנה ב-${eventDate.dayOfMonth} ב$monthName"
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DeepTeal.copy(alpha = 0.7f))
}

@Composable
private fun FrequencyOption(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = if (selected) "● $label" else label,
            color = if (selected) DeepTeal else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun TimeStepper(value: Int, range: IntRange, step: Int = 1, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onChange(((value - step - range.first).mod(range.last - range.first + 1)) + range.first) }) {
            Icon(Icons.Default.Remove, contentDescription = null, tint = DeepTeal)
        }
        Text(value.toString().padStart(2, '0'), fontWeight = FontWeight.Bold, color = DeepTeal)
        IconButton(onClick = { onChange(((value + step - range.first).mod(range.last - range.first + 1)) + range.first) }) {
            Icon(Icons.Default.Add, contentDescription = null, tint = DeepTeal)
        }
    }
}

@Composable
private fun stringResourceCompat(id: Int): String = androidx.compose.ui.res.stringResource(id)
