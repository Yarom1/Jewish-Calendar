package com.yarom.jewishcalendar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yarom.jewishcalendar.data.repository.AppSettings
import com.yarom.jewishcalendar.data.repository.SettingsRepository
import com.yarom.jewishcalendar.data.repository.ThemeMode
import com.yarom.jewishcalendar.domain.location.LocationRepository
import com.yarom.jewishcalendar.domain.zmanim.CalculationMethod
import com.yarom.jewishcalendar.domain.zmanim.Coordinates
import com.yarom.jewishcalendar.domain.zmanim.ZmanType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings?> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectCity(coordinates: Coordinates) {
        viewModelScope.launch { settingsRepository.setLocation(coordinates, useGps = false) }
    }

    fun useDeviceLocation() {
        viewModelScope.launch {
            val coordinates = locationRepository.getCurrentCoordinates()
            if (coordinates != null) {
                settingsRepository.setLocation(coordinates, useGps = true)
            }
        }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        viewModelScope.launch { settingsRepository.setCalculationMethod(method) }
    }

    fun toggleZman(zman: ZmanType, visible: Boolean) {
        viewModelScope.launch {
            val current = settings.value?.visibleZmanim ?: ZmanType.DEFAULT_VISIBLE
            val updated = if (visible) current + zman else current - zman
            settingsRepository.setVisibleZmanim(updated)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setUseDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setUseDynamicColor(enabled) }
    }

    fun hasLocationPermission(): Boolean = locationRepository.hasLocationPermission()
}
