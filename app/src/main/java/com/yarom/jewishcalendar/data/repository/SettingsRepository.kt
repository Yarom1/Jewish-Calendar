package com.yarom.jewishcalendar.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yarom.jewishcalendar.domain.zmanim.CalculationMethod
import com.yarom.jewishcalendar.domain.zmanim.Coordinates
import com.yarom.jewishcalendar.domain.zmanim.ZmanType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val coordinates: Coordinates,
    val useGps: Boolean,
    val calculationMethod: CalculationMethod,
    val visibleZmanim: Set<ZmanType>,
    val themeMode: ThemeMode,
    val useDynamicColor: Boolean,
)

/** Backs spec 4.a "התאמה אישית של זמנים" and 4.c settings screen via Jetpack DataStore. */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val LOCATION_NAME = stringPreferencesKey("location_name")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val ELEVATION = doublePreferencesKey("elevation")
        val TIME_ZONE = stringPreferencesKey("time_zone")
        val USE_GPS = booleanPreferencesKey("use_gps")
        val CALCULATION_METHOD = stringPreferencesKey("calculation_method")
        val VISIBLE_ZMANIM = stringSetPreferencesKey("visible_zmanim")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            coordinates = Coordinates(
                name = prefs[Keys.LOCATION_NAME] ?: DEFAULT_CITY_NAME,
                latitude = prefs[Keys.LATITUDE] ?: DEFAULT_LATITUDE,
                longitude = prefs[Keys.LONGITUDE] ?: DEFAULT_LONGITUDE,
                elevationMeters = prefs[Keys.ELEVATION] ?: 0.0,
                timeZoneId = prefs[Keys.TIME_ZONE] ?: "Asia/Jerusalem",
            ),
            useGps = prefs[Keys.USE_GPS] ?: true,
            calculationMethod = prefs[Keys.CALCULATION_METHOD]?.let {
                runCatching { CalculationMethod.valueOf(it) }.getOrNull()
            } ?: CalculationMethod.GRA,
            visibleZmanim = prefs[Keys.VISIBLE_ZMANIM]
                ?.mapNotNull { name -> runCatching { ZmanType.valueOf(name) }.getOrNull() }
                ?.toSet()
                ?.ifEmpty { ZmanType.DEFAULT_VISIBLE }
                ?: ZmanType.DEFAULT_VISIBLE,
            themeMode = prefs[Keys.THEME_MODE]?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrNull()
            } ?: ThemeMode.SYSTEM,
            useDynamicColor = prefs[Keys.USE_DYNAMIC_COLOR] ?: false,
        )
    }

    suspend fun setLocation(coordinates: Coordinates, useGps: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LOCATION_NAME] = coordinates.name
            prefs[Keys.LATITUDE] = coordinates.latitude
            prefs[Keys.LONGITUDE] = coordinates.longitude
            prefs[Keys.ELEVATION] = coordinates.elevationMeters
            prefs[Keys.TIME_ZONE] = coordinates.timeZoneId
            prefs[Keys.USE_GPS] = useGps
        }
    }

    suspend fun setCalculationMethod(method: CalculationMethod) {
        context.dataStore.edit { it[Keys.CALCULATION_METHOD] = method.name }
    }

    suspend fun setVisibleZmanim(zmanim: Set<ZmanType>) {
        context.dataStore.edit { it[Keys.VISIBLE_ZMANIM] = zmanim.map { z -> z.name }.toSet() }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.USE_DYNAMIC_COLOR] = enabled }
    }

    companion object {
        const val DEFAULT_CITY_NAME = "ירושלים"
        const val DEFAULT_LATITUDE = 31.7683
        const val DEFAULT_LONGITUDE = 35.2137
    }
}
