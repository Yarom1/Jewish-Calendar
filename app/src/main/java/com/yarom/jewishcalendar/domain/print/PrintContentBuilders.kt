package com.yarom.jewishcalendar.domain.print

import android.content.Context
import com.yarom.jewishcalendar.data.repository.AppSettings
import com.yarom.jewishcalendar.domain.hebrew.HebrewDate
import com.yarom.jewishcalendar.domain.zmanim.ZmanType
import com.yarom.jewishcalendar.ui.CalendarViewModel
import com.yarom.jewishcalendar.ui.components.compactLabelPlain
import com.yarom.jewishcalendar.ui.components.formatTime
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Builds the same content each screen already shows on screen, as plain data for
 * [CalendarPrintRenderer] - assembled once, outside Compose, when the user confirms a print job. */

private fun motzaeiLabel(hebrewDate: HebrewDate): String = when {
    hebrewDate.isShabbos && hebrewDate.isYomTov -> "צאת שבת וחג"
    hebrewDate.isShabbos -> "צאת השבת"
    else -> "צאת החג"
}

private fun dayLines(
    calendarViewModel: CalendarViewModel,
    context: Context,
    date: LocalDate,
    settings: AppSettings,
    eventTitles: List<String>,
): PrintDayBlock {
    val hebrewDate = calendarViewModel.hebrewDateFor(date)
    val zmanTimes = calendarViewModel.zmanimFor(date, settings.coordinates, settings).times
    val noteLine = listOfNotNull(
        hebrewDate.holidayName,
        "ל\"א תחנון".takeIf { hebrewDate.noTachanun },
    ).joinToString(" · ").ifBlank { null }

    val lines = mutableListOf<PrintLine>()
    if (hebrewDate.isErevShabbosOrYomTov) {
        lines.add(PrintLine("הדלקת נרות", zmanTimes[ZmanType.CANDLE_LIGHTING].formatTime()))
    }
    if (hebrewDate.isMotzaeiShabbosOrYomTov) {
        lines.add(PrintLine(motzaeiLabel(hebrewDate), zmanTimes[ZmanType.TZEIS_HAKOCHAVIM].formatTime()))
    }
    for (type in ZmanType.entries) {
        if (type !in settings.visibleZmanim) continue
        if (type == ZmanType.TZEIS_HAKOCHAVIM && hebrewDate.isMotzaeiShabbosOrYomTov) continue
        // Candle lighting is always added via the erev block above instead - the previous guard
        // here only stopped the erev-day duplicate, but left it printing on every *other* day too
        // (mirrors the same bug just fixed in WeeklyScreen's own zman loop).
        if (type == ZmanType.CANDLE_LIGHTING) continue
        lines.add(PrintLine(type.compactLabelPlain(context), zmanTimes[type].formatTime()))
    }
    for (title in eventTitles) {
        lines.add(PrintLine("אירוע", title))
    }

    return PrintDayBlock(
        dateLabel = date.format(DateTimeFormatter.ofPattern("EEEE d/M", Locale("he"))),
        hebrewLabel = "${hebrewDate.hebrewDayOfMonthLabel} ${hebrewDate.hebrewMonthName}",
        noteLine = noteLine,
        isSpecial = hebrewDate.isShabbos || hebrewDate.isYomTov,
        isToday = date == LocalDate.now(),
        lines = lines,
    )
}

/** Same set shown in the on-screen "זמני השבוע" box (WeeklyScreen's WeekZmanimBox). */
private val weekRangeZmanim = listOf(
    ZmanType.ALOS_HASHACHAR,
    ZmanType.SUNRISE,
    ZmanType.SOF_ZMAN_SHEMA_GRA,
    ZmanType.SOF_ZMAN_SHEMA_MGA,
    ZmanType.SOF_ZMAN_TEFILA,
    ZmanType.CHATZOS,
    ZmanType.MINCHA_GEDOLA,
    ZmanType.MINCHA_KETANA,
    ZmanType.PLAG_HAMINCHA,
    ZmanType.SUNSET,
)

suspend fun buildWeekPrintContent(
    calendarViewModel: CalendarViewModel,
    context: Context,
    weekStart: LocalDate,
    settings: AppSettings,
): PrintWeekContent {
    val weekDates = (0..6).map { weekStart.plusDays(it.toLong()) }
    val occurrences = calendarViewModel.eventRepository.observeOccurrences(weekDates.first()..weekDates.last()).first()
    val days = weekDates.map { date ->
        dayLines(calendarViewModel, context, date, settings, occurrences.filter { it.date == date }.map { it.event.title })
    }

    val friday = weekDates[5]
    val saturday = weekDates[6]
    val candleLighting = calendarViewModel.zmanimFor(friday, settings.coordinates, settings).times[ZmanType.CANDLE_LIGHTING].formatTime()
    val havdalah = calendarViewModel.zmanimFor(saturday, settings.coordinates, settings).times[ZmanType.TZEIS_HAKOCHAVIM].formatTime()
    val hebrewSaturday = calendarViewModel.hebrewDateFor(saturday)
    val parashaLine = hebrewSaturday.parashaName ?: hebrewSaturday.holidayName
    val haftarahLine = hebrewSaturday.haftarahName?.let { "${hebrewSaturday.haftarahHeading ?: "הפטרת השבת"}: $it" }
    val hebrewWeekStart = calendarViewModel.hebrewDateFor(weekStart)

    val allZmanTimes = weekDates.map { calendarViewModel.zmanimFor(it, settings.coordinates, settings).times }
    val weekZmanimLines = weekRangeZmanim.mapNotNull { type ->
        val times = allZmanTimes.mapNotNull { it[type] }
        if (times.isEmpty()) return@mapNotNull null
        val earliest = times.min()
        val latest = times.max()
        val range = if (earliest == latest) earliest.formatTime() else "${earliest.formatTime()}-${latest.formatTime()}"
        PrintLine(type.compactLabelPlain(context), range)
    }
    val (dafBavli, dafYerushalmi) = calendarViewModel.dafYomiWeekRange(weekStart, weekDates.last())

    return PrintWeekContent(
        title = "${hebrewWeekStart.hebrewMonthName} ${hebrewWeekStart.hebrewYearLabel}",
        days = days,
        candleLighting = candleLighting,
        havdalah = havdalah,
        parashaLine = parashaLine,
        haftarahLine = haftarahLine,
        weekZmanimLines = weekZmanimLines,
        dafYomiBavli = dafBavli,
        dafYomiYerushalmi = dafYerushalmi,
    )
}

suspend fun buildDayPrintContent(
    calendarViewModel: CalendarViewModel,
    context: Context,
    date: LocalDate,
    settings: AppSettings,
): PrintDayContent {
    val hebrewDate = calendarViewModel.hebrewDateFor(date)
    val occurrences = calendarViewModel.eventRepository.observeOccurrences(date..date).first()
    val day = dayLines(calendarViewModel, context, date, settings, occurrences.map { it.event.title })

    val studyLines = listOfNotNull(
        hebrewDate.dafYomiBavli?.let { PrintLine("דף יומי בבלי", it) },
        hebrewDate.dafYomiYerushalmi?.let { PrintLine("דף יומי ירושלמי", it) },
    )

    return PrintDayContent(
        title = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("he"))),
        day = day,
        studyLines = studyLines,
    )
}

private val printWeekdayLabels = listOf("א", "ב", "ג", "ד", "ה", "ו", "ש")

fun buildMonthPrintContent(
    calendarViewModel: CalendarViewModel,
    month: YearMonth,
    today: LocalDate,
    hasEventsOn: (LocalDate) -> Boolean,
): PrintMonthContent {
    val gridDates = calendarViewModel.monthGridDates(month)
    val cells = gridDates.map { date ->
        val hebrewDate = calendarViewModel.hebrewDateFor(date)
        PrintMonthCell(
            dayNumber = date.dayOfMonth.toString(),
            hebrewDay = hebrewDate.hebrewDayOfMonthLabel,
            inMonth = YearMonth.from(date) == month,
            isToday = date == today,
            hasEvents = hasEventsOn(date),
            isSpecial = hebrewDate.isShabbos || hebrewDate.isYomTov,
        )
    }
    return PrintMonthContent(
        title = month.month.getDisplayName(TextStyle.FULL, Locale("he")) + " " + month.year,
        weekdayLabels = printWeekdayLabels,
        cells = cells,
    )
}
