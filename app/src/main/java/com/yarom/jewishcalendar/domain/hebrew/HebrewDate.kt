package com.yarom.jewishcalendar.domain.hebrew

import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.TimeZone

/**
 * Immutable snapshot of a single day expressed in both the Hebrew and Gregorian calendars,
 * plus the Jewish-calendar metadata (holiday, Rosh Chodesh, parasha, Omer count) that a
 * calendar UI needs to render a day cell without re-touching KosherJava.
 */
data class HebrewDate(
    val gregorianDate: LocalDate,
    val hebrewYear: Int,
    val hebrewMonth: Int,
    val hebrewDayOfMonth: Int,
    val hebrewMonthName: String,
    val hebrewDayOfMonthLabel: String,
    val hebrewYearLabel: String,
    val isShabbos: Boolean,
    val isRoshChodesh: Boolean,
    val isYomTov: Boolean,
    val isCholHamoed: Boolean,
    val isTaanis: Boolean,
    val holidayName: String?,
    val parashaName: String?,
    val dayOfOmer: Int,
)

/**
 * Thin wrapper around KosherJava's [JewishCalendar] that converts between Hebrew and Gregorian
 * dates entirely offline (no network dependency), matching spec section 4.a "חישוב זמנים אופליין".
 */
class HebrewDateConverter(private val useHebrewFormat: Boolean = true) {

    private val formatter = HebrewDateFormatter().apply {
        isHebrewFormat = useHebrewFormat
        isUseGershGershayim = true
        isUseLongHebrewYears = false
        isUseLongOmer = true
    }

    fun fromGregorian(date: LocalDate, timeZone: TimeZone = TimeZone.getDefault()): HebrewDate {
        val calendar = Calendar.getInstance(timeZone).apply {
            clear()
            set(date.year, date.monthValue - 1, date.dayOfMonth)
        }
        val jewishCalendar = JewishCalendar(calendar)
        return toHebrewDate(jewishCalendar, date)
    }

    fun hebrewDateToGregorian(hebrewYear: Int, hebrewMonth: Int, hebrewDay: Int): LocalDate {
        val jewishCalendar = JewishCalendar()
        jewishCalendar.setJewishDate(hebrewYear, hebrewMonth, hebrewDay)
        val cal = jewishCalendar.gregorianCalendar
        return LocalDate.of(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
        )
    }

    private fun toHebrewDate(jewishCalendar: JewishCalendar, gregorianDate: LocalDate): HebrewDate {
        val yomTovIndex = jewishCalendar.yomTovIndex
        val holidayName = if (yomTovIndex != JewishCalendar.NO_HOLIDAY) {
            formatter.formatYomTov(jewishCalendar).ifBlank { null }
        } else null

        val parashaName = upcomingParashaName(jewishCalendar)

        return HebrewDate(
            gregorianDate = gregorianDate,
            hebrewYear = jewishCalendar.jewishYear,
            hebrewMonth = jewishCalendar.jewishMonth,
            hebrewDayOfMonth = jewishCalendar.jewishDayOfMonth,
            hebrewMonthName = formatter.formatMonth(jewishCalendar),
            hebrewDayOfMonthLabel = formatter.formatHebrewNumber(jewishCalendar.jewishDayOfMonth),
            hebrewYearLabel = formatter.formatHebrewNumber(jewishCalendar.jewishYear),
            isShabbos = jewishCalendar.dayOfWeek == Calendar.SATURDAY,
            isRoshChodesh = jewishCalendar.isRoshChodesh,
            isYomTov = jewishCalendar.isYomTov,
            isCholHamoed = jewishCalendar.isCholHamoed,
            isTaanis = jewishCalendar.isTaanis,
            holidayName = holidayName,
            parashaName = parashaName,
            dayOfOmer = jewishCalendar.dayOfOmer,
        )
    }

    /** Returns the name of the coming Shabbos's parasha, shown all week per spec section 4.a/6. */
    private fun upcomingParashaName(jewishCalendar: JewishCalendar): String? {
        val upcoming = jewishCalendar.upcomingParshah
        if (upcoming == com.kosherjava.zmanim.hebrewcalendar.JewishCalendar.Parsha.NONE) return null
        val satelliteCalendar = jewishCalendar.clone() as JewishCalendar
        val daysUntilSaturday = (Calendar.SATURDAY - satelliteCalendar.dayOfWeek + 7) % 7
        satelliteCalendar.forward(Calendar.DATE, if (daysUntilSaturday == 0) 0 else daysUntilSaturday)
        return formatter.formatParsha(satelliteCalendar).ifBlank { null }
    }

    companion object {
        val ISRAEL_TIME_ZONE: TimeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        val ISRAEL_ZONE_ID: ZoneId = ZoneId.of("Asia/Jerusalem")
    }
}
