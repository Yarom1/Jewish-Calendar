package com.yarom.jewishcalendar.ui.screens.weekly

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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
import com.yarom.jewishcalendar.ui.components.formatTime
import com.yarom.jewishcalendar.ui.components.labelRes
import com.yarom.jewishcalendar.ui.screens.addevent.AddEventSheet
import com.yarom.jewishcalendar.ui.theme.BrassGold
import com.yarom.jewishcalendar.ui.theme.Burgundy
import com.yarom.jewishcalendar.ui.theme.DeepTeal
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

private const val ANCHOR_PAGE = Int.MAX_VALUE / 2

@Composable
fun WeeklyScreen(calendarViewModel: CalendarViewModel, eventViewModel: EventViewModel) {
    val selectedDate by calendarViewModel.selectedDate.collectAsState()
    val settings by calendarViewModel.settings.collectAsState()
    var sheetDate by remember { mutableStateOf<LocalDate?>(null) }

    val anchorWeekStart = remember { calendarViewModel.weekDates(selectedDate).first() }
    val initialSelectedDate = remember { selectedDate }
    val pagerState = rememberPagerState(initialPage = ANCHOR_PAGE) { Int.MAX_VALUE }
    val displayedWeekStart = remember(pagerState.currentPage) {
        anchorWeekStart.plusWeeks((pagerState.currentPage - ANCHOR_PAGE).toLong())
    }

    // Keep the shared selectedDate (used by the daily/monthly tabs) in sync with paging,
    // preserving the day-of-week offset rather than resetting to the week's Sunday.
    LaunchedEffect(pagerState.currentPage) {
        val pageDelta = pagerState.currentPage - ANCHOR_PAGE
        calendarViewModel.selectDate(initialSelectedDate.plusWeeks(pageDelta.toLong()))
    }

    CalendarPageFrame(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            WeekHeader(calendarViewModel, displayedWeekStart)

            VerticalPager(state = pagerState, modifier = Modifier.fillMaxWidth().weight(1f)) { page ->
                val weekStart = anchorWeekStart.plusWeeks((page - ANCHOR_PAGE).toLong())
                val weekDates = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }

                var occurrences by remember(weekStart) { mutableStateOf<List<EventOccurrence>>(emptyList()) }
                LaunchedEffect(weekStart) {
                    calendarViewModel.eventRepository.observeOccurrences(weekDates.first()..weekDates.last())
                        .collectLatest { occurrences = it }
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            rotationX = -pageOffset * 55f
                            cameraDistance = 14f * density
                            transformOrigin = TransformOrigin(0.5f, if (pageOffset < 0f) 1f else 0f)
                            alpha = 1f - abs(pageOffset).coerceIn(0f, 1f) * 0.25f
                        },
                ) {
                    // Each row gets a fixed, equal slice of the measured height; the row itself
                    // then picks a font size that makes however many lines it needs fit that
                    // slice, instead of a static size that clips or a fixed line cap that hides
                    // zmanim.
                    val rowHeight = maxHeight / 7
                    Column(modifier = Modifier.fillMaxSize()) {
                        weekDates.forEachIndexed { index, date ->
                            val hebrewDate = calendarViewModel.hebrewDateFor(date)
                            WeekDayRow(
                                date = date,
                                hebrewDate = hebrewDate,
                                isSelected = date == selectedDate,
                                hasEvents = occurrences.any { it.date == date },
                                rowTint = if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                visibleZmanim = settings?.visibleZmanim ?: ZmanType.DEFAULT_VISIBLE,
                                zmanTimes = settings?.let { calendarViewModel.zmanimFor(date, it.coordinates, it).times } ?: emptyMap(),
                                onClick = { calendarViewModel.selectDate(date) },
                                onLongPress = { sheetDate = date },
                                rowHeight = rowHeight,
                                modifier = Modifier.height(rowHeight),
                            )
                        }
                    }
                }
            }

            val displayedWeekDates = remember(displayedWeekStart) { (0..6).map { displayedWeekStart.plusDays(it.toLong()) } }
            settings?.let { ShabbatBar(calendarViewModel, displayedWeekDates, it) }
        }
    }

    sheetDate?.let { date ->
        AddEventSheet(date = date, eventViewModel = eventViewModel, onDismiss = { sheetDate = null })
    }
}

@Composable
private fun WeekHeader(calendarViewModel: CalendarViewModel, weekStart: LocalDate) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
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
    rowHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val isSpecial = hebrewDate.isShabbos || hebrewDate.isYomTov
    val background = when {
        isSelected -> DeepTeal.copy(alpha = 0.12f)
        isSpecial -> BrassGold.copy(alpha = 0.16f)
        else -> rowTint
    }

    val hasNoteLine = !hebrewDate.holidayName.isNullOrBlank() || hasEvents
    val genericTypes = weeklyPriorityOrder
        .filter { it in visibleZmanim }
        .filterNot { it == ZmanType.TZEIS_HAKOCHAVIM && hebrewDate.isMotzaeiShabbosOrYomTov }
    val lineCount = (if (hasNoteLine) 1 else 0) +
        (if (hebrewDate.isErevShabbosOrYomTov) 1 else 0) +
        (if (hebrewDate.isMotzaeiShabbosOrYomTov) 1 else 0) +
        genericTypes.size

    // Dp and Sp are both 1/160" at the default font scale, so treating the row's Dp height as
    // an Sp budget divided across its lines is a close, dependency-free stand-in for a real
    // pixel-accurate shrink-to-fit: fewer lines (a plain weekday) render larger, a Yom Tov row
    // with a holiday note plus candle-lighting plus zmanim shrinks so all of it still fits.
    val fontSizeSp = (rowHeight.value / lineCount.coerceAtLeast(1) / 1.35f).coerceIn(7f, 13f)
    val lineHeightSp = fontSizeSp * 1.15f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(horizontal = 6.dp),
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
                .padding(horizontal = 6.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            // Parasha is shown once at the bottom of the week, not per day (spec follow-up).
            val noteLine = hebrewDate.holidayName
            if (!noteLine.isNullOrBlank()) {
                Text(
                    text = (if (hasEvents) "• " else "") + noteLine,
                    color = Burgundy,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSizeSp.sp,
                    lineHeight = lineHeightSp.sp,
                    maxLines = 1,
                )
            } else if (hasEvents) {
                Text("• יש אירועים", color = Burgundy, fontSize = fontSizeSp.sp, lineHeight = lineHeightSp.sp, maxLines = 1)
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
                )
            }
            for (type in genericTypes) {
                ZmanLine(
                    label = stringResource(type.labelRes()),
                    time = zmanTimes[type].formatTime(),
                    color = DeepTeal,
                    fontSize = fontSizeSp.sp,
                    lineHeight = lineHeightSp.sp,
                )
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
private fun ZmanLine(
    label: String,
    time: String,
    color: androidx.compose.ui.graphics.Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    bold: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = color, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, fontSize = fontSize, lineHeight = lineHeight, maxLines = 1)
        Text(time, color = color, fontWeight = FontWeight.Bold, fontSize = fontSize, lineHeight = lineHeight, maxLines = 1)
    }
}

/** Bottom-of-week strip: candle lighting (right), parasha (center), Shabbos exit (left) - like
 * the boxed "הדלקת נרות" / "צאת השבת" summary on a printed luach. */
@Composable
private fun ShabbatBar(
    calendarViewModel: CalendarViewModel,
    weekDates: List<LocalDate>,
    settings: com.yarom.jewishcalendar.data.repository.AppSettings,
) {
    val friday = weekDates[5]
    val saturday = weekDates[6]
    val candleLighting = calendarViewModel.zmanimFor(friday, settings.coordinates, settings).times[ZmanType.CANDLE_LIGHTING]
    val havdalah = calendarViewModel.zmanimFor(saturday, settings.coordinates, settings).times[ZmanType.TZEIS_HAKOCHAVIM]
    val parashaName = calendarViewModel.hebrewDateFor(saturday).parashaName

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
            if (!parashaName.isNullOrBlank()) {
                Text(
                    text = parashaName,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DeepTeal,
                    maxLines = 1,
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
