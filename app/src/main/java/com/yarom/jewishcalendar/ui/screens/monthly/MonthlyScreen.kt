package com.yarom.jewishcalendar.ui.screens.monthly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yarom.jewishcalendar.data.repository.EventOccurrence
import com.yarom.jewishcalendar.ui.CalendarViewModel
import com.yarom.jewishcalendar.ui.EventViewModel
import com.yarom.jewishcalendar.ui.components.DayCell
import com.yarom.jewishcalendar.ui.screens.addevent.AddEventSheet
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val weekdayLabels = listOf("א", "ב", "ג", "ד", "ה", "ו", "ש")

@Composable
fun MonthlyScreen(calendarViewModel: CalendarViewModel, eventViewModel: EventViewModel) {
    val selectedDate by calendarViewModel.selectedDate.collectAsState()
    var currentMonth by remember(selectedDate) { mutableStateOf(YearMonth.from(selectedDate)) }
    var sheetDate by remember { mutableStateOf<LocalDate?>(null) }

    val gridDates = remember(currentMonth) { calendarViewModel.monthGridDates(currentMonth) }

    var occurrences by remember { mutableStateOf<List<EventOccurrence>>(emptyList()) }
    androidx.compose.runtime.LaunchedEffect(gridDates.first(), gridDates.last()) {
        calendarViewModel.eventRepository.observeOccurrences(gridDates.first()..gridDates.last())
            .collectLatest { occurrences = it }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
            val monthLabel = currentMonth.month.getDisplayName(TextStyle.FULL, Locale("he")) + " " + currentMonth.year
            Text(monthLabel, style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            weekdayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        gridDates.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                week.forEach { date ->
                    val hebrewDate = calendarViewModel.hebrewDateFor(date)
                    val inMonth = YearMonth.from(date) == currentMonth
                    DayCell(
                        date = date,
                        hebrewDate = hebrewDate,
                        isSelected = date == selectedDate,
                        hasEvents = occurrences.any { it.date == date },
                        onClick = { calendarViewModel.selectDate(date) },
                        onLongPress = { sheetDate = date },
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .alpha(if (inMonth) 1f else 0.35f),
                    )
                }
            }
        }
    }

    sheetDate?.let { date ->
        AddEventSheet(date = date, eventViewModel = eventViewModel, onDismiss = { sheetDate = null })
    }
}
