package com.yarom.jewishcalendar.ui.screens.weekly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yarom.jewishcalendar.data.repository.EventOccurrence
import com.yarom.jewishcalendar.domain.zmanim.ZmanType
import com.yarom.jewishcalendar.ui.CalendarViewModel
import com.yarom.jewishcalendar.ui.EventViewModel
import com.yarom.jewishcalendar.ui.components.DayCell
import com.yarom.jewishcalendar.ui.components.ZmanRow
import com.yarom.jewishcalendar.ui.screens.addevent.AddEventSheet
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

@Composable
fun WeeklyScreen(calendarViewModel: CalendarViewModel, eventViewModel: EventViewModel) {
    val selectedDate by calendarViewModel.selectedDate.collectAsState()
    val settings by calendarViewModel.settings.collectAsState()
    var sheetDate by remember { mutableStateOf<LocalDate?>(null) }

    val weekDates = remember(selectedDate) { calendarViewModel.weekDates(selectedDate) }

    var occurrences by remember { mutableStateOf<List<EventOccurrence>>(emptyList()) }
    androidx.compose.runtime.LaunchedEffect(weekDates.first(), weekDates.last()) {
        calendarViewModel.eventRepository.observeOccurrences(weekDates.first()..weekDates.last())
            .collectLatest { occurrences = it }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { calendarViewModel.selectDate(selectedDate.minusWeeks(1)) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
            val hebrewHeader = calendarViewModel.hebrewDateFor(weekDates.first())
            Text(
                text = "${hebrewHeader.hebrewMonthName} ${hebrewHeader.hebrewYearLabel}",
                style = MaterialTheme.typography.titleLarge,
            )
            IconButton(onClick = { calendarViewModel.selectDate(selectedDate.plusWeeks(1)) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            weekDates.forEach { date ->
                val hebrewDate = calendarViewModel.hebrewDateFor(date)
                DayCell(
                    date = date,
                    hebrewDate = hebrewDate,
                    isSelected = date == selectedDate,
                    hasEvents = occurrences.any { it.date == date },
                    onClick = { calendarViewModel.selectDate(date) },
                    onLongPress = { sheetDate = date },
                    modifier = Modifier.weight(1f).padding(2.dp),
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        val daySettings = settings
        if (daySettings != null) {
            val zmanim = remember(selectedDate, daySettings) {
                calendarViewModel.zmanimFor(selectedDate, daySettings.coordinates, daySettings)
            }
            val hebrewSelected = calendarViewModel.hebrewDateFor(selectedDate)
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                item {
                    Text(
                        text = listOfNotNull(hebrewSelected.holidayName, hebrewSelected.parashaName)
                            .joinToString("  ·  ")
                            .ifBlank { "יום ${hebrewSelected.hebrewDayOfMonthLabel} ל${hebrewSelected.hebrewMonthName}" },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
                items(ZmanType.entries.filter { it in daySettings.visibleZmanim }) { type ->
                    ZmanRow(type = type, time = zmanim.times[type])
                }
                val dayEvents = occurrences.filter { it.date == selectedDate }
                if (dayEvents.isNotEmpty()) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("אירועים", style = MaterialTheme.typography.titleMedium)
                    }
                    items(dayEvents) { occurrence ->
                        Text(
                            text = occurrence.event.title + (occurrence.event.location?.let { " · $it" } ?: ""),
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }

    sheetDate?.let { date ->
        AddEventSheet(date = date, eventViewModel = eventViewModel, onDismiss = { sheetDate = null })
    }
}
