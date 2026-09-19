package com.yarom.jewishcalendar.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Optional online enrichment on top of the fully-offline KosherJava calculations
 * (spec 7 "ארכיטקטורה טכנולוגית" mentions both KosherJava and Hebcal). Not wired into any
 * ViewModel yet — the app must work fully offline per spec 4.a; this is a phase-2 addition for
 * richer holiday descriptions once a caching/offline-fallback strategy is designed.
 */
interface HebcalApi {
    @GET("hebcal")
    suspend fun getEvents(
        @Query("v") version: Int = 1,
        @Query("cfg") config: String = "json",
        @Query("maj") majorHolidays: Int = 1,
        @Query("min") minorHolidays: Int = 1,
        @Query("mod") modernHolidays: Int = 1,
        @Query("nx") noRoshChodesh: Int = 0,
        @Query("year") year: Int,
        @Query("month") month: Int,
        @Query("ss") shabbatTimes: Int = 1,
        @Query("geo") geo: String = "pos",
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("tzid") timeZoneId: String,
    ): HebcalResponse
}

@Serializable
data class HebcalResponse(
    val items: List<HebcalItem> = emptyList(),
)

@Serializable
data class HebcalItem(
    val title: String,
    val date: String,
    val category: String? = null,
    val hebrew: String? = null,
)
