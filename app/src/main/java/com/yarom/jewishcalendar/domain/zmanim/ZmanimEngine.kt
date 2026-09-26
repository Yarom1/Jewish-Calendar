package com.yarom.jewishcalendar.domain.zmanim

import com.kosherjava.zmanim.ComplexZmanimCalendar
import com.kosherjava.zmanim.util.GeoLocation
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

data class Coordinates(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double = 0.0,
    val timeZoneId: String = "Asia/Jerusalem",
)

/**
 * Computes the day's halachic times fully offline via KosherJava (spec 4.a), for the given
 * coordinates and calculation method (spec 4.c). No network call is made or required.
 *
 * NOTE: the mapping from [CalculationMethod] to specific KosherJava formulas below is a
 * reasonable default for an MVP and should be reviewed against a posek/halachic authority
 * before this is relied on for religious observance in production.
 */
class ZmanimEngine {

    fun calculate(
        date: LocalDate,
        coordinates: Coordinates,
        method: CalculationMethod,
    ): DayZmanim {
        val timeZone = TimeZone.getTimeZone(coordinates.timeZoneId)
        val geoLocation = GeoLocation(
            coordinates.name,
            coordinates.latitude,
            coordinates.longitude,
            coordinates.elevationMeters,
            timeZone,
        )
        val calendar = ComplexZmanimCalendar(geoLocation).apply {
            this.calendar = Calendar.getInstance(timeZone).apply {
                clear()
                set(date.year, date.monthValue - 1, date.dayOfMonth)
            }
            candleLightingOffset = if (method == CalculationMethod.JERUSALEM_SEA_LEVEL) 40.0 else 18.0
        }

        val zoneId = ZoneId.of(coordinates.timeZoneId)
        fun Date?.toZoned(): ZonedDateTime? = this?.toInstant()?.atZone(zoneId)

        // Sof zman shema has its own always-visible GRA and MGA rows, so it must never switch
        // with the general method setting (that was a real bug: picking Magen Avraham silently
        // replaced the value shown on the row explicitly labeled "(גר"א)").  Only the zmanim
        // without a separate per-method row (alos, sof zman tefila, tzeis) follow the setting.
        val (sofZmanTefila, alos, tzeis) = when (method) {
            // Jerusalem/Bnei Brak use the same GRA-based times here (they only differ from plain
            // GRA in candle-lighting offset, set above).
            CalculationMethod.GRA, CalculationMethod.JERUSALEM_SEA_LEVEL, CalculationMethod.BEIT_BENEI_BRAK -> Triple(
                calendar.sofZmanTfilaGRA.toZoned(),
                calendar.alosHashachar.toZoned(),
                calendar.tzais.toZoned(),
            )
            CalculationMethod.MAGEN_AVRAHAM -> Triple(
                calendar.sofZmanTfilaMGA.toZoned(),
                calendar.alos72.toZoned(),
                calendar.tzais72.toZoned(),
            )
            CalculationMethod.RABBI_OVADIA_YOSEF -> Triple(
                calendar.sofZmanTfilaMGA16Point1Degrees.toZoned(),
                calendar.alosHashachar.toZoned(),
                calendar.tzaisGeonim7Point083Degrees.toZoned(),
            )
        }

        return DayZmanim(
            date = date,
            times = buildMap {
                put(ZmanType.ALOS_HASHACHAR, alos)
                put(ZmanType.SUNRISE, calendar.sunrise.toZoned())
                put(ZmanType.SOF_ZMAN_SHEMA_GRA, calendar.sofZmanShmaGRA.toZoned())
                put(ZmanType.SOF_ZMAN_SHEMA_MGA, calendar.sofZmanShmaMGA.toZoned())
                put(ZmanType.SOF_ZMAN_TEFILA, sofZmanTefila)
                put(ZmanType.CHATZOS, calendar.chatzos.toZoned())
                put(ZmanType.MINCHA_GEDOLA, calendar.minchaGedola.toZoned())
                put(ZmanType.MINCHA_KETANA, calendar.minchaKetana.toZoned())
                put(ZmanType.PLAG_HAMINCHA, calendar.plagHamincha.toZoned())
                put(ZmanType.SUNSET, calendar.sunset.toZoned())
                put(ZmanType.TZEIS_HAKOCHAVIM, tzeis)
                put(ZmanType.CANDLE_LIGHTING, calendar.candleLighting.toZoned())
            },
        )
    }
}

data class DayZmanim(
    val date: LocalDate,
    val times: Map<ZmanType, ZonedDateTime?>,
)
