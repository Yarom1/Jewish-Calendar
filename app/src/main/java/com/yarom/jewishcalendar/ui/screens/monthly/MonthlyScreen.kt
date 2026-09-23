package com.yarom.jewishcalendar.ui.screens.monthly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yarom.jewishcalendar.data.repository.EventOccurrence
import com.yarom.jewishcalendar.ui.CalendarViewModel
import com.yarom.jewishcalendar.ui.EventViewModel
import com.yarom.jewishcalendar.ui.components.CalendarPageFrame
import com.yarom.jewishcalendar.ui.components.DateSearchDialog
import com.yarom.jewishcalendar.ui.components.DayCell
import com.yarom.jewishcalendar.ui.components.OrnamentalDivider
import com.yarom.jewishcalendar.ui.components.ViewControlsRow
import com.yarom.jewishcalendar.ui.components.rememberCurrentDate
import com.yarom.jewishcalendar.ui.screens.addevent.AddEventSheet
import com.yarom.jewishcalendar.ui.theme.DeepTeal
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

private val weekdayLabels = listOf("א", "ב", "ג", "ד", "ה", "ו", "ש")
private const val MIN_ZOOM = 0.7f
private const val MAX_ZOOM = 1.6f
private const val ANCHOR_PAGE = Int.MAX_VALUE / 2

@Composable
fun MonthlyScreen(calendarViewModel: CalendarViewModel, eventViewModel: EventViewModel, onDayOpened: () -> Unit) {
    val selectedDate by calendarViewModel.selectedDate.collectAsState()
    val today by rememberCurrentDate()
    var sheetDate by remember { mutableStateOf<LocalDate?>(null) }
    var zoomScale by remember { mutableStateOf(1f) }
    var showDateSearch by remember { mutableStateOf(false) }

    val anchorMonth = remember { YearMonth.from(selectedDate) }
    val initialSelectedDate = remember { selectedDate }
    // Swipe-to-change-month, matching the daily/weekly views' paging gesture (spec follow-up:
    // the old +/- chevron buttons are gone, this is the only way to move between months now).
    val pagerState = rememberPagerState(initialPage = ANCHOR_PAGE) { Int.MAX_VALUE }
    val displayedMonth = remember(pagerState.currentPage) {
        anchorMonth.plusMonths((pagerState.currentPage - ANCHOR_PAGE).toLong())
    }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        val monthDelta = pagerState.currentPage - ANCHOR_PAGE
        calendarViewModel.selectDate(initialSelectedDate.plusMonths(monthDelta.toLong()))
    }

    fun jumpToDate(date: LocalDate) {
        val targetMonth = YearMonth.from(date)
        val monthsDelta = ChronoUnit.MONTHS.between(anchorMonth, targetMonth)
        val targetPage = ANCHOR_PAGE + monthsDelta.toInt()
        calendarViewModel.selectDate(date)
        coroutineScope.launch {
            pagerState.animateScrollToPage(targetPage)
            calendarViewModel.selectDate(date)
        }
    }

    CalendarPageFrame(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val monthLabel = displayedMonth.month.getDisplayName(TextStyle.FULL, Locale("he")) + " " + displayedMonth.year
                Text(monthLabel, style = MaterialTheme.typography.titleMedium, color = DeepTeal)
                ViewControlsRow(
                    zoomScale = zoomScale,
                    onZoomChange = { zoomScale = it },
                    onSearchClick = { showDateSearch = true },
                    onTodayClick = { jumpToDate(today) },
                    minZoom = MIN_ZOOM,
                    maxZoom = MAX_ZOOM,
                )
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                weekdayLabels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepTeal,
                    )
                }
            }
            OrnamentalDivider()

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().weight(1f)) { page ->
                val month = anchorMonth.plusMonths((page - ANCHOR_PAGE).toLong())
                val gridDates = remember(month) { calendarViewModel.monthGridDates(month) }

                var occurrences by remember(month) { mutableStateOf<List<EventOccurrence>>(emptyList()) }
                LaunchedEffect(month) {
                    calendarViewModel.eventRepository.observeOccurrences(gridDates.first()..gridDates.last())
                        .collectLatest { occurrences = it }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            rotationY = pageOffset * 55f
                            cameraDistance = 14f * density
                            transformOrigin = TransformOrigin(if (pageOffset < 0f) 1f else 0f, 0.5f)
                            alpha = 1f - abs(pageOffset).coerceIn(0f, 1f) * 0.25f
                        },
                ) {
                    gridDates.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 4.dp)) {
                            week.forEach { date ->
                                val hebrewDate = calendarViewModel.hebrewDateFor(date)
                                val inMonth = YearMonth.from(date) == month
                                DayCell(
                                    date = date,
                                    hebrewDate = hebrewDate,
                                    isSelected = date == selectedDate,
                                    hasEvents = occurrences.any { it.date == date },
                                    today = today,
                                    zoomScale = zoomScale,
                                    onClick = {
                                        calendarViewModel.selectDate(date)
                                        onDayOpened()
                                    },
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
            }
        }
    }

    sheetDate?.let { date ->
        AddEventSheet(date = date, calendarViewModel = calendarViewModel, eventViewModel = eventViewModel, onDismiss = { sheetDate = null })
    }
    if (showDateSearch) {
        DateSearchDialog(
            onDismiss = { showDateSearch = false },
            onGregorianDateChosen = { date ->
                showDateSearch = false
                jumpToDate(date)
            },
            onHebrewDateChosen = { year, month, day ->
                showDateSearch = false
                calendarViewModel.gregorianForHebrew(year, month, day)?.let { jumpToDate(it) }
            },
        )
    }
}
