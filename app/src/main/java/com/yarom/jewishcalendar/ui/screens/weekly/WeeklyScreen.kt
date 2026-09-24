package com.yarom.jewishcalendar.ui.screens.weekly

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarom.jewishcalendar.R
import com.yarom.jewishcalendar.data.repository.AppSettings
import com.yarom.jewishcalendar.data.repository.EventOccurrence
import com.yarom.jewishcalendar.domain.hebrew.HebrewDate
import com.yarom.jewishcalendar.domain.zmanim.ZmanType
import com.yarom.jewishcalendar.ui.CalendarViewModel
import com.yarom.jewishcalendar.ui.EventViewModel
import com.yarom.jewishcalendar.ui.components.CalendarPageFrame
import com.yarom.jewishcalendar.ui.components.DateSearchDialog
import com.yarom.jewishcalendar.ui.components.DayBadge
import com.yarom.jewishcalendar.ui.components.formatTime
import com.yarom.jewishcalendar.ui.components.compactLabel
import com.yarom.jewishcalendar.ui.components.labelRes
import com.yarom.jewishcalendar.ui.components.rememberCurrentDate
import com.yarom.jewishcalendar.ui.components.ViewControlsRow
import com.yarom.jewishcalendar.ui.screens.addevent.AddEventSheet
import com.yarom.jewishcalendar.ui.theme.BrassGold
import com.yarom.jewishcalendar.ui.theme.Burgundy
import com.yarom.jewishcalendar.ui.theme.DeepTeal
import com.yarom.jewishcalendar.ui.theme.EventColor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

private const val ANCHOR_PAGE = Int.MAX_VALUE / 2
private const val MIN_ZOOM = 0.7f
private const val MAX_ZOOM = 2.2f

@Composable
fun WeeklyScreen(
    calendarViewModel: CalendarViewModel,
    eventViewModel: EventViewModel,
    onDayOpened: () -> Unit,
) {
    val selectedDate by calendarViewModel.selectedDate.collectAsState()
    val settings by calendarViewModel.settings.collectAsState()
    val isFullscreen by calendarViewModel.isFullscreen.collectAsState()
    val today by rememberCurrentDate()
    var sheetDate by remember { mutableStateOf<LocalDate?>(null) }
    var editingEvent by remember { mutableStateOf<com.yarom.jewishcalendar.data.local.entity.EventEntity?>(null) }
    var zoomScale by remember { mutableStateOf(1f) }
    var showDateSearch by remember { mutableStateOf(false) }

    val anchorWeekStart = remember { calendarViewModel.weekDates(selectedDate).first() }
    val initialSelectedDate = remember { selectedDate }
    // Right-to-left week paging: HorizontalPager mirrors automatically under an RTL layout
    // direction (the whole app is Hebrew/RTL), so a leftward swipe already advances the page.
    val pagerState = rememberPagerState(initialPage = ANCHOR_PAGE) { Int.MAX_VALUE }
    val displayedWeekStart = remember(pagerState.currentPage) {
        anchorWeekStart.plusWeeks((pagerState.currentPage - ANCHOR_PAGE).toLong())
    }
    val coroutineScope = rememberCoroutineScope()

    // Keep the shared selectedDate (used by the daily/monthly tabs) in sync with paging,
    // preserving the day-of-week offset rather than resetting to the week's Sunday.
    LaunchedEffect(pagerState.currentPage) {
        val pageDelta = pagerState.currentPage - ANCHOR_PAGE
        calendarViewModel.selectDate(initialSelectedDate.plusWeeks(pageDelta.toLong()))
    }

    // Jump-to-date (spec follow-up): scroll the pager to the target week, then re-assert the
    // exact searched date as selected once the scroll settles (overriding the page-delta
    // derivation above, which otherwise preserves the wrong day-of-week offset for a jump).
    fun jumpToDate(date: LocalDate) {
        val targetWeekStart = calendarViewModel.weekDates(date).first()
        val weeksDelta = ChronoUnit.WEEKS.between(anchorWeekStart, targetWeekStart)
        val targetPage = ANCHOR_PAGE + weeksDelta.toInt()
        calendarViewModel.selectDate(date)
        coroutineScope.launch {
            pagerState.animateScrollToPage(targetPage)
            calendarViewModel.selectDate(date)
        }
    }

    CalendarPageFrame(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            WeekHeader(
                calendarViewModel = calendarViewModel,
                weekStart = displayedWeekStart,
                zoomScale = zoomScale,
                onZoomChange = { zoomScale = it },
                onSearchClick = { showDateSearch = true },
                onTodayClick = { jumpToDate(today) },
                isFullscreen = isFullscreen,
                onFullscreenToggle = { calendarViewModel.toggleFullscreen() },
            )

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().weight(1f)) { page ->
                val weekStart = anchorWeekStart.plusWeeks((page - ANCHOR_PAGE).toLong())
                val weekDates = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }

                var occurrences by remember(weekStart) { mutableStateOf<List<EventOccurrence>>(emptyList()) }
                LaunchedEffect(weekStart) {
                    calendarViewModel.eventRepository.observeOccurrences(weekDates.first()..weekDates.last())
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
                        .verticalScroll(rememberScrollState()),
                ) {
                    weekDates.forEachIndexed { index, date ->
                        val hebrewDate = calendarViewModel.hebrewDateFor(date)
                        WeekDayRow(
                            date = date,
                            hebrewDate = hebrewDate,
                            isSelected = date == selectedDate,
                            today = today,
                            events = occurrences.filter { it.date == date },
                            rowTint = if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            visibleZmanim = settings?.visibleZmanim ?: ZmanType.DEFAULT_VISIBLE,
                            zmanTimes = settings?.let { calendarViewModel.zmanimFor(date, it.coordinates, it).times } ?: emptyMap(),
                            zoomScale = zoomScale,
                            onClick = {
                                calendarViewModel.selectDate(date)
                                onDayOpened()
                            },
                            onLongPress = { sheetDate = date },
                            onEventClick = { editingEvent = it },
                        )
                    }
                }
            }

            val displayedWeekDates = remember(displayedWeekStart) { (0..6).map { displayedWeekStart.plusDays(it.toLong()) } }
            settings?.let { ShabbatBar(calendarViewModel, displayedWeekDates, it) }
        }
    }

    sheetDate?.let { date ->
        AddEventSheet(date = date, calendarViewModel = calendarViewModel, eventViewModel = eventViewModel, onDismiss = { sheetDate = null })
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

@Composable
private fun WeekHeader(
    calendarViewModel: CalendarViewModel,
    weekStart: LocalDate,
    zoomScale: Float,
    onZoomChange: (Float) -> Unit,
    onSearchClick: () -> Unit,
    onTodayClick: () -> Unit,
    isFullscreen: Boolean,
    onFullscreenToggle: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp)) {
        // Split across two rows - controls above, month labels below (spec follow-up: with the
        // fullscreen button added, five icons plus both month labels no longer fit on one line
        // without the Gregorian label wrapping awkwardly).
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            ViewControlsRow(
                zoomScale = zoomScale,
                onZoomChange = onZoomChange,
                onSearchClick = onSearchClick,
                onTodayClick = onTodayClick,
                isFullscreen = isFullscreen,
                onFullscreenToggle = onFullscreenToggle,
                minZoom = MIN_ZOOM,
                maxZoom = MAX_ZOOM,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val hebrewHeader = calendarViewModel.hebrewDateFor(weekStart)
            Text(
                text = "${hebrewHeader.hebrewMonthName} ${hebrewHeader.hebrewYearLabel}",
                style = MaterialTheme.typography.titleMedium,
                color = DeepTeal,
            )
            val gregorianLabel = weekStart.month.getDisplayName(TextStyle.FULL, Locale("he")) + " " + weekStart.year
            Text(gregorianLabel, style = MaterialTheme.typography.titleMedium, color = DeepTeal)
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun WeekDayRow(
    date: LocalDate,
    hebrewDate: HebrewDate,
    isSelected: Boolean,
    today: LocalDate,
    events: List<EventOccurrence>,
    rowTint: androidx.compose.ui.graphics.Color,
    visibleZmanim: Set<ZmanType>,
    zmanTimes: Map<ZmanType, ZonedDateTime?>,
    zoomScale: Float,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onEventClick: (com.yarom.jewishcalendar.data.local.entity.EventEntity) -> Unit,
) {
    val isSpecial = hebrewDate.isShabbos || hebrewDate.isYomTov
    val isToday = date == today
    // The fill marks whichever day is selected (moves with the tap); today gets its own
    // distinct marker - a frame around the whole row, not just the small day badge - so the
    // two states are never confused with one another (spec follow-up).
    val background = when {
        isSelected -> DeepTeal.copy(alpha = 0.12f)
        isSpecial -> BrassGold.copy(alpha = 0.16f)
        else -> rowTint
    }

    val fontSizeSp = 12f * zoomScale
    val lineHeightSp = fontSizeSp * 1.2f
    val labelWidth = (92 * zoomScale).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .then(
                if (isToday) {
                    Modifier.border(2.dp, DeepTeal, RoundedCornerShape(6.dp))
                } else {
                    Modifier
                },
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DayBadge(
            date = date,
            hebrewDate = hebrewDate,
            isSelected = isSelected,
            today = today,
            onClick = onClick,
            onLongPress = onLongPress,
            zoomScale = zoomScale,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp),
        ) {
            // Parasha is shown once at the bottom of the week, not per day (spec follow-up).
            val noteLine = hebrewDate.holidayName
            if (!noteLine.isNullOrBlank()) {
                Text(
                    text = noteLine,
                    color = Burgundy,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSizeSp.sp,
                    lineHeight = lineHeightSp.sp,
                    maxLines = 1,
                )
            }
            for (occurrence in events) {
                Text(
                    text = "• ${occurrence.event.title}",
                    color = EventColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSizeSp.sp,
                    lineHeight = lineHeightSp.sp,
                    maxLines = 1,
                    modifier = Modifier.clickable { onEventClick(occurrence.event) },
                )
            }
            // Candle lighting / havdalah are ritual entry/exit times for the day, not plain
            // zmanim - shown as their own highlighted lines whenever they apply (every erev
            // Shabbos/Yom Tov and every Motzaei Shabbos/Yom Tov, not just the week's Friday).
            if (hebrewDate.isErevShabbosOrYomTov) {
                ZmanLine(
                    label = stringResource(R.string.zman_candle_lighting),
                    time = zmanTimes[ZmanType.CANDLE_LIGHTING].formatTime(),
                    color = Burgundy,
                    bold = true,
                    fontSize = fontSizeSp.sp,
                    lineHeight = lineHeightSp.sp,
                    labelWidth = labelWidth,
                )
            }
            if (hebrewDate.isMotzaeiShabbosOrYomTov) {
                ZmanLine(
                    label = if (hebrewDate.isShabbos) "צאת השבת" else "צאת החג",
                    time = zmanTimes[ZmanType.TZEIS_HAKOCHAVIM].formatTime(),
                    color = Burgundy,
                    bold = true,
                    fontSize = fontSizeSp.sp,
                    lineHeight = lineHeightSp.sp,
                    labelWidth = labelWidth,
                )
            }
            // Natural chronological order (same order the settings screen lists them in),
            // matching every zman the user enabled - nothing is capped or reprioritized.
            for (type in ZmanType.entries) {
                if (type !in visibleZmanim) continue
                if (type == ZmanType.TZEIS_HAKOCHAVIM && hebrewDate.isMotzaeiShabbosOrYomTov) continue
                ZmanLine(
                    label = type.compactLabel(),
                    time = zmanTimes[type].formatTime(),
                    color = DeepTeal,
                    fontSize = fontSizeSp.sp,
                    lineHeight = lineHeightSp.sp,
                    labelWidth = labelWidth,
                )
            }
        }
    }
}

/** A zman row anchored to one side (left, since it sits beside the time itself), with the
 * label right-aligned within a fixed column so every line's label starts at the same edge. */
@Composable
private fun ZmanLine(
    label: String,
    time: String,
    color: androidx.compose.ui.graphics.Color,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    labelWidth: Dp,
    bold: Boolean = false,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = label,
            color = color,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontSize = fontSize,
            lineHeight = lineHeight,
            maxLines = 1,
            textAlign = TextAlign.Right,
            modifier = Modifier.width(labelWidth),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(time, color = color, fontWeight = FontWeight.Bold, fontSize = fontSize, lineHeight = lineHeight, maxLines = 1)
    }
}

/** Bottom-of-week strip: candle lighting (right), parasha + haftarah (center), Shabbos exit
 * (left) - like the boxed "הדלקת נרות" / "צאת השבת" summary on a printed luach. */
@Composable
private fun ShabbatBar(
    calendarViewModel: CalendarViewModel,
    weekDates: List<LocalDate>,
    settings: AppSettings,
) {
    val friday = weekDates[5]
    val saturday = weekDates[6]
    val candleLighting = calendarViewModel.zmanimFor(friday, settings.coordinates, settings).times[ZmanType.CANDLE_LIGHTING]
    val havdalah = calendarViewModel.zmanimFor(saturday, settings.coordinates, settings).times[ZmanType.TZEIS_HAKOCHAVIM]
    val hebrewSaturday = calendarViewModel.hebrewDateFor(saturday)
    // Weeks with no regular parasha (e.g. during Sukkot/Pesach) show the holiday name instead.
    val centerLine = hebrewSaturday.parashaName ?: hebrewSaturday.holidayName

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrassGold.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShabbatTimeBox(stringResource(R.string.zman_candle_lighting), candleLighting.formatTime(), Burgundy)
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!centerLine.isNullOrBlank()) {
                Text(
                    text = centerLine,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DeepTeal,
                    maxLines = 1,
                )
            }
            if (!hebrewSaturday.haftarahName.isNullOrBlank()) {
                Text(
                    text = "${hebrewSaturday.haftarahHeading ?: "הפטרת השבת"}:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DeepTeal.copy(alpha = 0.9f),
                    maxLines = 1,
                )
                Text(
                    text = hebrewSaturday.haftarahName,
                    fontSize = 9.sp,
                    color = DeepTeal.copy(alpha = 0.75f),
                    maxLines = 2,
                )
            }
        }
        ShabbatTimeBox("צאת השבת", havdalah.formatTime(), DeepTeal)
    }
}

@Composable
private fun ShabbatTimeBox(label: String, time: String, accent: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = accent, maxLines = 1)
        Text(time, fontFamily = FontFamily.Serif, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = accent, maxLines = 1)
    }
}
