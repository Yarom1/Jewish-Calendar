package com.yarom.jewishcalendar.ui.screens.daily

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarom.jewishcalendar.data.local.entity.EventEntity
import com.yarom.jewishcalendar.data.repository.EventOccurrence
import com.yarom.jewishcalendar.domain.zmanim.ZmanType
import com.yarom.jewishcalendar.ui.CalendarViewModel
import com.yarom.jewishcalendar.ui.EventViewModel
import com.yarom.jewishcalendar.ui.components.CalendarPageFrame
import com.yarom.jewishcalendar.ui.components.DateSearchDialog
import com.yarom.jewishcalendar.ui.components.OrnamentalDivider
import com.yarom.jewishcalendar.ui.components.ViewControlsRow
import com.yarom.jewishcalendar.ui.components.ZmanRow
import com.yarom.jewishcalendar.ui.components.rememberCurrentDate
import com.yarom.jewishcalendar.ui.screens.addevent.AddEventSheet
import com.yarom.jewishcalendar.ui.theme.DeepTeal
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

private const val ANCHOR_PAGE = Int.MAX_VALUE / 2
private const val MIN_ZOOM = 0.7f
private const val MAX_ZOOM = 2.0f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DailyScreen(calendarViewModel: CalendarViewModel, eventViewModel: EventViewModel) {
    val selectedDate by calendarViewModel.selectedDate.collectAsState()
    val settings by calendarViewModel.settings.collectAsState()
    val isFullscreen by calendarViewModel.isFullscreen.collectAsState()
    val today by rememberCurrentDate()
    var showAddSheet by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<EventEntity?>(null) }
    var zoomScale by remember { mutableStateOf(1f) }
    var showDateSearch by remember { mutableStateOf(false) }

    val anchorDate = remember { selectedDate }
    val pagerState = rememberPagerState(initialPage = ANCHOR_PAGE) { Int.MAX_VALUE }
    val coroutineScope = rememberCoroutineScope()

    // Horizontal RTL day-to-day swipe, matching the weekly view's paging (spec follow-up).
    LaunchedEffect(pagerState.currentPage) {
        val pageDelta = pagerState.currentPage - ANCHOR_PAGE
        calendarViewModel.selectDate(anchorDate.plusDays(pageDelta.toLong()))
    }

    fun jumpToDate(date: LocalDate) {
        val dayDelta = ChronoUnit.DAYS.between(anchorDate, date)
        calendarViewModel.selectDate(date)
        coroutineScope.launch {
            pagerState.animateScrollToPage(ANCHOR_PAGE + dayDelta.toInt())
            calendarViewModel.selectDate(date)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
    ) { padding ->
        CalendarPageFrame(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    ViewControlsRow(
                        zoomScale = zoomScale,
                        onZoomChange = { zoomScale = it },
                        onSearchClick = { showDateSearch = true },
                        onTodayClick = { jumpToDate(today) },
                        isFullscreen = isFullscreen,
                        onFullscreenToggle = { calendarViewModel.toggleFullscreen() },
                        minZoom = MIN_ZOOM,
                        maxZoom = MAX_ZOOM,
                    )
                }
                OrnamentalDivider()

                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().weight(1f)) { page ->
                    val date = anchorDate.plusDays((page - ANCHOR_PAGE).toLong())
                    val hebrewDate = calendarViewModel.hebrewDateFor(date)

                    var occurrences by remember(date) { mutableStateOf<List<EventOccurrence>>(emptyList()) }
                    LaunchedEffect(date) {
                        calendarViewModel.eventRepository.observeOccurrences(date..date)
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
                            }
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(onClick = {}, onLongClick = { showAddSheet = true })
                                .padding(vertical = 8.dp),
                        ) {
                            val gregorianLabel = date.format(
                                DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("he")),
                            )
                            Text(
                                gregorianLabel,
                                fontSize = (18 * zoomScale).sp,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                "${hebrewDate.hebrewDayOfMonthLabel} ${hebrewDate.hebrewMonthName} ${hebrewDate.hebrewYearLabel}",
                                fontSize = (14 * zoomScale).sp,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        val summary = listOfNotNull(hebrewDate.holidayName, hebrewDate.parashaName).joinToString("  ·  ")
                        if (summary.isNotBlank()) {
                            Text(
                                summary,
                                fontSize = (16 * zoomScale).sp,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                        if (hebrewDate.dayOfOmer in 1..49) {
                            Text("היום ${hebrewDate.dayOfOmer} לעומר", fontSize = (13 * zoomScale).sp)
                        }

                        Text(
                            "זמני היום",
                            fontSize = (16 * zoomScale).sp,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        )
                        // Daily always shows every zman - not limited by the settings toggles,
                        // which only govern the compact weekly rows (spec follow-up) - except
                        // candle lighting, which only ever applies on an erev Shabbos/Yom Tov,
                        // not as a "regular" daily zman.
                        settings?.let { daySettings ->
                            val zmanim = remember(date, daySettings) {
                                calendarViewModel.zmanimFor(date, daySettings.coordinates, daySettings)
                            }
                            for (type in ZmanType.entries) {
                                if (type == ZmanType.CANDLE_LIGHTING && !hebrewDate.isErevShabbosOrYomTov) continue
                                ZmanRow(type = type, time = zmanim.times[type], zoomScale = zoomScale)
                            }
                        }

                        if (hebrewDate.dafYomiBavli != null || hebrewDate.dafYomiYerushalmi != null) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            Text(
                                "לימוד יומי",
                                fontSize = (16 * zoomScale).sp,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            hebrewDate.dafYomiBavli?.let {
                                Text("דף יומי בבלי: $it", fontSize = (14 * zoomScale).sp, color = DeepTeal)
                            }
                            hebrewDate.dafYomiYerushalmi?.let {
                                Text("דף יומי ירושלמי: $it", fontSize = (14 * zoomScale).sp, color = DeepTeal)
                            }
                        }

                        if (occurrences.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            Text(
                                "אירועים",
                                fontSize = (16 * zoomScale).sp,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            for (occurrence in occurrences) {
                                val minuteOfDay = occurrence.event.startMinuteOfDay
                                val timeLabel = if (minuteOfDay != null) {
                                    "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60)
                                } else {
                                    "כל היום"
                                }
                                Text(
                                    "$timeLabel  ${occurrence.event.title}",
                                    fontSize = (14 * zoomScale).sp,
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .clickable { editingEvent = occurrence.event },
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddEventSheet(
            date = selectedDate,
            calendarViewModel = calendarViewModel,
            eventViewModel = eventViewModel,
            onDismiss = { showAddSheet = false },
        )
    }
    editingEvent?.let { event ->
        AddEventSheet(
            date = LocalDate.ofEpochDay(event.startEpochDay),
            calendarViewModel = calendarViewModel,
            eventViewModel = eventViewModel,
            onDismiss = { editingEvent = null },
            existingEvent = event,
        )
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
