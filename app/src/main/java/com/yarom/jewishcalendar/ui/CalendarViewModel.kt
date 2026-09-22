package com.yarom.jewishcalendar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yarom.jewishcalendar.data.repository.AppSettings
import com.yarom.jewishcalendar.data.repository.EventRepository
import com.yarom.jewishcalendar.data.repository.SettingsRepository
import com.yarom.jewishcalendar.domain.hebrew.HebrewDate
import com.yarom.jewishcalendar.domain.hebrew.HebrewDateConverter
import com.yarom.jewishcalendar.domain.zmanim.Coordinates
import com.yarom.jewishcalendar.domain.zmanim.DayZmanim
import com.yarom.jewishcalendar.domain.zmanim.ZmanimEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

/** Shared calendar state: selected day, app settings, and derived Hebrew date / zmanim lookups. */
class CalendarViewModel(
    private val settingsRepository: SettingsRepository,
    val eventRepository: EventRepository,
    private val hebrewDateConverter: HebrewDateConverter,
    private val zmanimEngine: ZmanimEngine,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val settings: StateFlow<AppSettings?> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun hebrewDateFor(date: LocalDate): HebrewDate = hebrewDateConverter.fromGregorian(date)

    /** For the date-search control (spec follow-up): Hebrew date -> Gregorian date. */
    fun gregorianForHebrew(year: Int, month: Int, day: Int): LocalDate? =
        runCatching { hebrewDateConverter.hebrewDateToGregorian(year, month, day) }.getOrNull()

    fun zmanimFor(date: LocalDate, coordinates: Coordinates, settings: AppSettings): DayZmanim =
        zmanimEngine.calculate(date, coordinates, settings.calculationMethod)

    /** Sunday-to-Saturday week containing [date], matching the Israeli week convention. */
    fun weekDates(date: LocalDate): List<LocalDate> {
        val daysFromSunday = (date.dayOfWeek.value % 7) // Monday=1..Sunday=7 -> Sunday=0
        val sunday = date.minusDays(daysFromSunday.toLong())
        return (0..6).map { sunday.plusDays(it.toLong()) }
    }

    /** Full grid (including leading/trailing days from adjacent months) for a month view. */
    fun monthGridDates(month: YearMonth): List<LocalDate> {
        val firstOfMonth = month.atDay(1)
        val leading = firstOfMonth.dayOfWeek.value % 7
        val gridStart = firstOfMonth.minusDays(leading.toLong())
        val totalCells = ((leading + month.lengthOfMonth() + 6) / 7) * 7
        return (0 until totalCells).map { gridStart.plusDays(it.toLong()) }
    }
}
