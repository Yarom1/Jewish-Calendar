package com.yarom.jewishcalendar

import android.app.Application
import com.yarom.jewishcalendar.data.local.AppDatabase
import com.yarom.jewishcalendar.data.repository.EventRepository
import com.yarom.jewishcalendar.data.repository.SettingsRepository
import com.yarom.jewishcalendar.domain.hebrew.HebrewDateConverter
import com.yarom.jewishcalendar.domain.location.LocationRepository
import com.yarom.jewishcalendar.domain.zmanim.ZmanimEngine

/** Simple hand-rolled service locator (kept dependency-light instead of pulling in Hilt for the MVP). */
class JewishCalendarApp : Application() {

    val hebrewDateConverter by lazy { HebrewDateConverter() }
    val zmanimEngine by lazy { ZmanimEngine() }
    val settingsRepository by lazy { SettingsRepository(this) }
    val locationRepository by lazy { LocationRepository(this) }
    val eventRepository by lazy { EventRepository(AppDatabase.getInstance(this).eventDao(), hebrewDateConverter) }
}
