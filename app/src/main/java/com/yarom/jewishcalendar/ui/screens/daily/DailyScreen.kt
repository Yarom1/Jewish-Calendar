package com.yarom.jewishcalendar.ui.screens.daily

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yarom.jewishcalendar.data.local.entity.EventEntity
import com.yarom.jewishcalendar.data.repository.EventOccurrence
import com.yarom.jewishcalendar.domain.zmanim.ZmanType
import com.yarom.jewishcalendar.ui.CalendarViewModel
import com.yarom.jewishcalendar.ui.EventViewModel
import com.yarom.jewishcalendar.ui.components.OrnamentalDivider
import com.yarom.jewishcalendar.ui.components.CalendarPageFrame
import com.yarom.jewishcalendar.ui.components.ZmanRow
import com.yarom.jewishcalendar.ui.screens.addevent.AddEventSheet
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DailyScreen(calendarViewModel: CalendarViewModel, eventViewModel: EventViewModel) {
    val selectedDate by calendarViewModel.selectedDate.collectAsState()
    val settings by calendarViewModel.settings.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<EventEntity?>(null) }

    var occurrences by remember { mutableStateOf<List<EventOccurrence>>(emptyList()) }
    androidx.compose.runtime.LaunchedEffect(selectedDate) {
        calendarViewModel.eventRepository.observeOccurrences(selectedDate..selectedDate)
            .collectLatest { occurrences = it }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
    ) { padding ->
        CalendarPageFrame(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { calendarViewModel.selectDate(selectedDate.minusDays(1)) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    modifier = Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = { showAddSheet = true },
                    ),
                ) {
                    val gregorianLabel = selectedDate.format(
                        DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("he")),
                    )
                    Text(gregorianLabel, style = MaterialTheme.typography.titleMedium)
                    val hebrewDate = calendarViewModel.hebrewDateFor(selectedDate)
                    Text(
                        "${hebrewDate.hebrewDayOfMonthLabel} ${hebrewDate.hebrewMonthName} ${hebrewDate.hebrewYearLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                IconButton(onClick = { calendarViewModel.selectDate(selectedDate.plusDays(1)) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null)
                }
            }

            OrnamentalDivider()

            val daySettings = settings
            if (daySettings != null) {
                val hebrewDate = calendarViewModel.hebrewDateFor(selectedDate)
                val zmanim = remember(selectedDate, daySettings) {
                    calendarViewModel.zmanimFor(selectedDate, daySettings.coordinates, daySettings)
                }
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    item {
                        val summary = listOfNotNull(hebrewDate.holidayName, hebrewDate.parashaName).joinToString("  ·  ")
                        if (summary.isNotBlank()) {
                            Text(summary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        if (hebrewDate.dayOfOmer in 1..49) {
                            Text("היום ${hebrewDate.dayOfOmer} לעומר", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("זמני היום", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                    }
                    items(ZmanType.entries.filter { it in daySettings.visibleZmanim }) { type ->
                        ZmanRow(type = type, time = zmanim.times[type])
                    }
                    if (occurrences.isNotEmpty()) {
                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            Text("אירועים", style = MaterialTheme.typography.titleMedium)
                        }
                        items(occurrences) { occurrence ->
                            val minuteOfDay = occurrence.event.startMinuteOfDay
                            val timeLabel = if (minuteOfDay != null) {
                                "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60)
                            } else "כל היום"
                            Text(
                                "$timeLabel  ${occurrence.event.title}",
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .clickable { editingEvent = occurrence.event },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddEventSheet(date = selectedDate, eventViewModel = eventViewModel, onDismiss = { showAddSheet = false })
    }
    editingEvent?.let { event ->
        AddEventSheet(
            date = LocalDate.ofEpochDay(event.startEpochDay),
            eventViewModel = eventViewModel,
            onDismiss = { editingEvent = null },
            existingEvent = event,
        )
    }
}
