package com.yarom.jewishcalendar.ui

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yarom.jewishcalendar.JewishCalendarApp

fun appViewModelFactory(app: JewishCalendarApp) = viewModelFactory {
    initializer {
        CalendarViewModel(app.settingsRepository, app.eventRepository, app.hebrewDateConverter, app.zmanimEngine)
    }
    initializer {
        EventViewModel(app.eventRepository, app.hebrewDateConverter)
    }
    initializer {
        SettingsViewModel(app.settingsRepository, app.locationRepository)
    }
}
