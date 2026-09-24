package com.yarom.jewishcalendar.domain.print

/** One "label: value" line in a printed block (a zman, or an event). */
data class PrintLine(val label: String, val value: String)

/** One day's printable content - shared by the weekly (one per day) and daily (one block) pages. */
data class PrintDayBlock(
    val dateLabel: String,
    val hebrewLabel: String,
    val noteLine: String?,
    val lines: List<PrintLine>,
)

data class PrintWeekContent(
    val title: String,
    val days: List<PrintDayBlock>,
    val candleLighting: String,
    val havdalah: String,
    val parashaLine: String?,
    val haftarahLine: String?,
)

data class PrintDayContent(
    val title: String,
    val day: PrintDayBlock,
    val studyLines: List<PrintLine>,
)

data class PrintMonthCell(
    val dayNumber: String,
    val hebrewDay: String,
    val inMonth: Boolean,
    val isToday: Boolean,
    val hasEvents: Boolean,
)

data class PrintMonthContent(
    val title: String,
    val weekdayLabels: List<String>,
    /** Always a multiple of 7 - one row per week, matching the on-screen grid. */
    val cells: List<PrintMonthCell>,
)

/** What a single print job renders - exactly one of the three calendar views' current content. */
sealed interface PrintContent {
    data class Week(val content: PrintWeekContent) : PrintContent
    data class Day(val content: PrintDayContent) : PrintContent
    data class Month(val content: PrintMonthContent) : PrintContent
}
