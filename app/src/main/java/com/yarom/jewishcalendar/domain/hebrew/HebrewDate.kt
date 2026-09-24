package com.yarom.jewishcalendar.domain.hebrew

import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherjava.zmanim.hebrewcalendar.YerushalmiYomiCalculator
import com.kosherjava.zmanim.hebrewcalendar.YomiCalculator
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
    /** Candle-lighting-eve: erev Shabbos or erev Yom Tov (incl. 2nd-day Diaspora Yom Tov). */
    val isErevShabbosOrYomTov: Boolean,
    /** The day itself is Shabbos/Yom Tov and tomorrow isn't - havdalah/"exit" time applies. */
    val isMotzaeiShabbosOrYomTov: Boolean,
    val holidayName: String?,
    val parashaName: String?,
    /** "הפטרת השבת" for a regular week, or the special Shabbat's own name (e.g. "שבת חנוכה"). */
    val haftarahHeading: String?,
    val haftarahName: String?,
    val dayOfOmer: Int,
    /** "Daily study" (spec follow-up): Daf Yomi Bavli/Yerushalmi, null before their cycles started. */
    val dafYomiBavli: String?,
    val dafYomiYerushalmi: String?,
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
    }

    fun fromGregorian(date: LocalDate, timeZone: TimeZone = TimeZone.getDefault()): HebrewDate {
        val calendar = Calendar.getInstance(timeZone).apply {
            clear()
            set(date.year, date.monthValue - 1, date.dayOfMonth)
        }
        // This app is Israel-only (Hebrew UI, Israeli zmanim/city presets, Israeli week start) -
        // without this, KosherJava defaults to Diaspora two-day Yom Tov rules, which misclassifies
        // e.g. 16 Tishrei/22 Nissan as a second Yom Tov day instead of Chol Hamoed (spec follow-up).
        val jewishCalendar = JewishCalendar(calendar).apply { inIsrael = true }
        return toHebrewDate(jewishCalendar, date)
    }

    fun hebrewDateToGregorian(hebrewYear: Int, hebrewMonth: Int, hebrewDay: Int): LocalDate {
        val jewishCalendar = JewishCalendar().apply { inIsrael = true }
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
        val holidayName = if (yomTovIndex != -1) {
            formatter.formatYomTov(jewishCalendar).ifBlank { null }
        } else null

        val upcomingSaturday = advanceToSaturday(jewishCalendar)
        // NOTE: deliberately NOT KosherJava's own upcomingParshah - it always jumps to the
        // FOLLOWING Saturday's reading even when called on a Saturday itself (its own javadoc:
        // "next Shabbos's Parsha will be returned"), which silently mismatched the haftarah
        // against the parasha name shown for every ordinary Shabbat, and for a Saturday whose own
        // reading is genuinely NONE (Yom Tov/Chol Hamoed, e.g. Shabbat Sukkot) it substituted next
        // week's regular haftarah instead of leaving room for the Yom Tov override below (spec
        // follow-up: user-reported wrong haftarah for Shabbat Sukkot, and for "regular" weeks too).
        // upcomingSaturday is already positioned on the correct target Saturday (this one if
        // today is Saturday, otherwise the coming one), so its own .parshah is exactly right.
        val upcomingParsha = upcomingSaturday.parshah
        val parashaName = if (upcomingParsha == JewishCalendar.Parsha.NONE) {
            null
        } else {
            formatter.formatParsha(upcomingSaturday).ifBlank { null }
        }
        // NOT gated on upcomingParsha - a NONE reading is exactly the case (Yom Tov/Chol Hamoed
        // Shabbat) the overrides in specialShabbosFor() exist to cover; haftarahFor() already
        // falls back to null on its own when there's truly neither an override nor a regular
        // reading, so gating here a second time would just suppress those overrides outright.
        val haftarahHeading = specialShabbosLabel(upcomingSaturday)
        val haftarahName = haftarahFor(upcomingSaturday, upcomingParsha)

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
            isErevShabbosOrYomTov = jewishCalendar.isTomorrowShabbosOrYomTov,
            isMotzaeiShabbosOrYomTov = isMotzaei(jewishCalendar),
            holidayName = holidayName,
            parashaName = parashaName,
            haftarahHeading = haftarahHeading,
            haftarahName = haftarahName,
            dayOfOmer = jewishCalendar.dayOfOmer,
            dafYomiBavli = runCatching {
                formatter.formatDafYomiBavli(YomiCalculator.getDafYomiBavli(jewishCalendar))
            }.getOrNull(),
            dafYomiYerushalmi = runCatching {
                YerushalmiYomiCalculator.getDafYomiYerushalmi(jewishCalendar)?.let(formatter::formatDafYomiYerushalmi)
            }.getOrNull(),
        )
    }

    /** True when today is Shabbos/Yom Tov and tomorrow is a plain weekday (havdalah applies). */
    private fun isMotzaei(jewishCalendar: JewishCalendar): Boolean {
        val isTodayRestDay = jewishCalendar.dayOfWeek == Calendar.SATURDAY || jewishCalendar.isYomTov
        if (!isTodayRestDay) return false
        val tomorrow = jewishCalendar.clone() as JewishCalendar
        tomorrow.forward(Calendar.DATE, 1)
        val isTomorrowRestDay = tomorrow.dayOfWeek == Calendar.SATURDAY || tomorrow.isYomTov
        return !isTomorrowRestDay
    }

    /** A satellite calendar advanced to the coming Shabbos, used for both the parasha name (shown
     * all week per spec section 4.a/6) and the special-Shabbat/haftarah lookup. */
    private fun advanceToSaturday(jewishCalendar: JewishCalendar): JewishCalendar {
        val satelliteCalendar = jewishCalendar.clone() as JewishCalendar
        val daysUntilSaturday = (Calendar.SATURDAY - satelliteCalendar.dayOfWeek + 7) % 7
        // JewishDate.forward() rejects amounts < 1, so only call it when today isn't already Saturday.
        if (daysUntilSaturday > 0) {
            satelliteCalendar.forward(Calendar.DATE, daysUntilSaturday)
        }
        return satelliteCalendar
    }

    companion object {
        val ISRAEL_TIME_ZONE: TimeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        val ISRAEL_ZONE_ID: ZoneId = ZoneId.of("Asia/Jerusalem")
    }
}
