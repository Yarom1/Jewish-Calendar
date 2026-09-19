package com.yarom.jewishcalendar.ui.screens.weekly

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
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
            // A plain, evenly-weighted Column (not LazyColumn) so all 7 days always fit on
            // screen without scrolling, per spec 2 "תצוגה שבועית" - each row shrinks to share
            // the available height equally instead of scrolling past it.
            Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                weekDates.forEachIndexed { index, date ->
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
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    sheetDate?.let { date ->
        AddEventSheet(date = date, eventViewModel = eventViewModel, onDismiss = { sheetDate = null })
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
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
    modifier: Modifier = Modifier,
) {
    val isSpecial = hebrewDate.isShabbos || hebrewDate.isYomTov
    val background = when {
        isSelected -> DeepTeal.copy(alpha = 0.12f)
        isSpecial -> BrassGold.copy(alpha = 0.16f)
        else -> rowTint
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(horizontal = 8.dp, vertical = 1.dp),
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
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            val noteLine = listOfNotNull(hebrewDate.holidayName, hebrewDate.parashaName).joinToString("  ·  ")
            if (noteLine.isNotBlank()) {
                Text(
                    text = (if (hasEvents) "• " else "") + noteLine,
                    color = Burgundy,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    maxLines = 1,
                )
            } else if (hasEvents) {
                Text("• יש אירועים", color = Burgundy, fontSize = 10.sp, lineHeight = 12.sp, maxLines = 1)
            }
            // Candle lighting is a Shabbos/Yom Tov-eve ritual time, not a plain daily zman -
            // shown only on erev Shabbos/Yom Tov, per spec 4.c "תזכורת ערב שבת... הדלקת נרות".
            if (hebrewDate.isErevShabbosOrYomTov) {
                ZmanLine(
                    label = stringResource(R.string.zman_candle_lighting),
                    time = zmanTimes[ZmanType.CANDLE_LIGHTING].formatTime(),
                    color = Burgundy,
                    bold = true,
                )
            }
            // Cap to a fixed small set of "headline" zmanim (by priority) so all 7 days always
            // fit on screen without scrolling, regardless of how many the user enabled in
            // settings - the full list stays available on the daily view.
            val shown = weeklyPriorityOrder.filter { it in visibleZmanim }.take(3)
            for (type in shown) {
                ZmanLine(label = stringResource(type.labelRes()), time = zmanTimes[type].formatTime(), color = DeepTeal)
            }
        }
    }
}

private val weeklyPriorityOrder = listOf(
    ZmanType.SUNRISE,
    ZmanType.SUNSET,
    ZmanType.TZEIS_HAKOCHAVIM,
    ZmanType.SOF_ZMAN_SHEMA_GRA,
    ZmanType.SOF_ZMAN_SHEMA_MGA,
    ZmanType.MINCHA_GEDOLA,
    ZmanType.MINCHA_KETANA,
    ZmanType.PLAG_HAMINCHA,
    ZmanType.CHATZOS,
    ZmanType.ALOS_HASHACHAR,
    ZmanType.SOF_ZMAN_TEFILA,
)

@Composable
private fun ZmanLine(label: String, time: String, color: androidx.compose.ui.graphics.Color, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = color, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp, lineHeight = 13.sp, maxLines = 1)
        Text(time, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 13.sp, maxLines = 1)
    }
}
