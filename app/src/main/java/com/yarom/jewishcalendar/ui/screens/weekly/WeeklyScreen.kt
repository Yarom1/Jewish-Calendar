package com.yarom.jewishcalendar.ui.screens.weekly

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarom.jewishcalendar.R
import com.yarom.jewishcalendar.data.repository.EventOccurrence
import com.yarom.jewishcalendar.domain.hebrew.HebrewDate
import com.yarom.jewishcalendar.domain.zmanim.ZmanType
import com.yarom.jewishcalendar.ui.CalendarViewModel
import com.yarom.jewishcalendar.ui.EventViewModel
import com.yarom.jewishcalendar.ui.components.CalendarPageFrame
import com.yarom.jewishcalendar.ui.components.DayBadge
import com.yarom.jewishcalendar.ui.components.OrnamentalDivider
import com.yarom.jewishcalendar.ui.components.formatTime
import com.yarom.jewishcalendar.ui.components.labelRes
import com.yarom.jewishcalendar.ui.screens.addevent.AddEventSheet
import com.yarom.jewishcalendar.ui.theme.BrassGold
import com.yarom.jewishcalendar.ui.theme.Burgundy
import com.yarom.jewishcalendar.ui.theme.DeepTeal
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

    CalendarPageFrame(modifier = Modifier.fillMaxSize()) {
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
            OrnamentalDivider()

            val daySettings = settings
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(weekDates) { index, date ->
                    val hebrewDate = calendarViewModel.hebrewDateFor(date)
                    WeekDayRow(
                        date = date,
                        hebrewDate = hebrewDate,
                        isSelected = date == selectedDate,
                        hasEvents = occurrences.any { it.date == date },
                        rowTint = if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        visibleZmanim = daySettings?.visibleZmanim ?: ZmanType.DEFAULT_VISIBLE,
                        zmanTimes = daySettings?.let { calendarViewModel.zmanimFor(date, it.coordinates, it).times } ?: emptyMap(),
                        onClick = { calendarViewModel.selectDate(date) },
                        onLongPress = { sheetDate = date },
                    )
                }
            }

            if (daySettings != null) {
                ShabbatSummaryBar(calendarViewModel, weekDates, daySettings.coordinates, daySettings)
            }
        }
    }

    sheetDate?.let { date ->
        AddEventSheet(date = date, eventViewModel = eventViewModel, onDismiss = { sheetDate = null })
    }
}

@Composable
private fun WeekDayRow(
    date: LocalDate,
    hebrewDate: HebrewDate,
    isSelected: Boolean,
    hasEvents: Boolean,
    rowTint: androidx.compose.ui.graphics.Color,
    visibleZmanim: Set<ZmanType>,
    zmanTimes: Map<ZmanType, java.time.ZonedDateTime?>,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    val isSpecial = hebrewDate.isShabbos || hebrewDate.isYomTov
    val background = when {
        isSelected -> DeepTeal.copy(alpha = 0.12f)
        isSpecial -> BrassGold.copy(alpha = 0.16f)
        else -> rowTint
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DayBadge(
            date = date,
            hebrewDate = hebrewDate,
            isSelected = isSelected,
            onClick = onClick,
            onLongPress = onLongPress,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
        ) {
            val noteLine = listOfNotNull(hebrewDate.holidayName, hebrewDate.parashaName).joinToString("  ·  ")
            if (noteLine.isNotBlank()) {
                Text(
                    text = noteLine,
                    color = Burgundy,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
            val zmanLineBuilder = StringBuilder()
            for (type in ZmanType.entries) {
                if (type !in visibleZmanim) continue
                if (zmanLineBuilder.isNotEmpty()) zmanLineBuilder.append("   ·   ")
                zmanLineBuilder.append(stringResource(type.labelRes()))
                zmanLineBuilder.append(' ')
                zmanLineBuilder.append(zmanTimes[type].formatTime())
            }
            val zmanLine = zmanLineBuilder.toString()
            if (zmanLine.isNotBlank()) {
                Text(
                    text = zmanLine,
                    color = DeepTeal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
            }
            if (hasEvents) {
                Text("• יש אירועים", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ShabbatSummaryBar(
    calendarViewModel: CalendarViewModel,
    weekDates: List<LocalDate>,
    coordinates: com.yarom.jewishcalendar.domain.zmanim.Coordinates,
    settings: com.yarom.jewishcalendar.data.repository.AppSettings,
) {
    // weekDates is Sunday..Saturday, so index 5 = Friday, index 6 = Saturday.
    val friday = weekDates[5]
    val saturday = weekDates[6]
    val candleLighting = calendarViewModel.zmanimFor(friday, coordinates, settings).times[ZmanType.CANDLE_LIGHTING]
    val havdalah = calendarViewModel.zmanimFor(saturday, coordinates, settings).times[ZmanType.TZEIS_HAKOCHAVIM]

    OrnamentalDivider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        ShabbatTimeBox(stringResource(R.string.zman_candle_lighting), candleLighting.formatTime(), Burgundy)
        ShabbatTimeBox("צאת השבת", havdalah.formatTime(), DeepTeal)
    }
}

@Composable
private fun ShabbatTimeBox(label: String, time: String, accent: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = accent)
        Text(time, fontFamily = FontFamily.Serif, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = accent)
    }
}
