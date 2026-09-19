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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yarom.jewishcalendar.R
import com.yarom.jewishcalendar.data.local.entity.CalendarOwner
import com.yarom.jewishcalendar.data.local.entity.RecurrenceType
import com.yarom.jewishcalendar.ui.EventViewModel
import com.yarom.jewishcalendar.ui.NewEventDraft
import java.time.LocalDate

private val recurrenceLabels = mapOf(
    RecurrenceType.NONE to R.string.event_recurrence_none,
    RecurrenceType.DAILY to R.string.event_recurrence_daily,
    RecurrenceType.WEEKLY to R.string.event_recurrence_weekly,
    RecurrenceType.MONTHLY_GREGORIAN to R.string.event_recurrence_monthly,
    RecurrenceType.YEARLY_GREGORIAN to R.string.event_recurrence_yearly,
    RecurrenceType.ROSH_CHODESH to R.string.event_recurrence_hebrew_monthly,
    RecurrenceType.YEARLY_HEBREW to R.string.event_recurrence_hebrew_yearly,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventSheet(
    date: LocalDate,
    eventViewModel: EventViewModel,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf(20) }
    var minute by remember { mutableStateOf(0) }
    var isFamily by remember { mutableStateOf(false) }
    var recurrenceType by remember { mutableStateOf(RecurrenceType.NONE) }
    var recurrenceMenuExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResourceCompat(R.string.add_event_title),
                fontWeight = FontWeight.Bold,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResourceCompat(R.string.event_name_label)) },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text(stringResourceCompat(R.string.event_location_label)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(stringResourceCompat(R.string.event_time_label), modifier = Modifier.padding(end = 12.dp))
                TimeStepper(value = hour, range = 0..23, onChange = { hour = it })
                Text(":", modifier = Modifier.padding(horizontal = 4.dp))
                TimeStepper(value = minute, range = 0..55, step = 5, onChange = { minute = it })
            }

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(stringResourceCompat(R.string.event_calendar_personal), modifier = Modifier.padding(end = 8.dp))
                Switch(checked = isFamily, onCheckedChange = { isFamily = it })
                Text(stringResourceCompat(R.string.event_calendar_family), modifier = Modifier.padding(start = 8.dp))
            }

            ExposedDropdownMenuBox(
                expanded = recurrenceMenuExpanded,
                onExpandedChange = { recurrenceMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = stringResourceCompat(recurrenceLabels.getValue(recurrenceType)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("חזרתיות") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurrenceMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                DropdownMenu(
                    expanded = recurrenceMenuExpanded,
                    onDismissRequest = { recurrenceMenuExpanded = false },
                ) {
                    recurrenceLabels.forEach { (type, resId) ->
                        DropdownMenuItem(
                            text = { Text(stringResourceCompat(resId)) },
                            onClick = {
                                recurrenceType = type
                                recurrenceMenuExpanded = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResourceCompat(R.string.event_notes_label)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text(stringResourceCompat(R.string.cancel))
                }
                Button(
                    onClick = {
                        eventViewModel.save(
                            NewEventDraft(
                                date = date,
                                title = title,
                                location = location,
                                notes = notes,
                                hour = hour,
                                minute = minute,
                                calendarOwner = if (isFamily) CalendarOwner.FAMILY else CalendarOwner.PERSONAL,
                                recurrenceType = recurrenceType,
                            ),
                            onSaved = onDismiss,
                        )
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResourceCompat(R.string.save))
                }
            }
        }
    }
}

@Composable
private fun TimeStepper(value: Int, range: IntRange, step: Int = 1, onChange: (Int) -> Unit) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        IconButton(onClick = { onChange(((value - step - range.first).mod(range.last - range.first + 1)) + range.first) }) {
            Icon(Icons.Default.Remove, contentDescription = null)
        }
        Text(value.toString().padStart(2, '0'))
        IconButton(onClick = { onChange(((value + step - range.first).mod(range.last - range.first + 1)) + range.first) }) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }
}

@Composable
private fun stringResourceCompat(id: Int): String = androidx.compose.ui.res.stringResource(id)
