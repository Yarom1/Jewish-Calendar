package com.yarom.jewishcalendar.domain.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yarom.jewishcalendar.domain.zmanim.Coordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

/** Wraps FusedLocationProviderClient for spec 4.a "איתור מיקום ... לפי GPS". */
class LocationRepository(private val context: Context) {

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCoordinates(): Coordinates? {
        if (!hasLocationPermission()) return null
        val client = LocationServices.getFusedLocationProviderClient(context)
        val location = suspendCancellableCoroutine<Location?> { continuation ->
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(null) }
        } ?: return null

        return Coordinates(
            name = reverseGeocodeName(location.latitude, location.longitude) ?: "המיקום הנוכחי",
            latitude = location.latitude,
            longitude = location.longitude,
            elevationMeters = if (location.hasAltitude()) location.altitude else 0.0,
            timeZoneId = TimeZone.getDefault().id,
        )
    }

    /** Resolves GPS coordinates to the city/area name the device identifies (spec follow-up: the
     * automatic-location card showed a generic "current location" placeholder instead of the
     * actual place). Falls back to null (caller shows the placeholder) when offline or the
     * platform geocoder has nothing - this is a display label only, never required for the
     * zmanim calculation itself, which stays fully offline. */
    private suspend fun reverseGeocodeName(latitude: Double, longitude: Double): String? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale("he"))
        return withContext(Dispatchers.IO) {
            try {
                val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine<Address?> { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            continuation.resume(addresses.firstOrNull())
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
                }
                address?.let { it.locality ?: it.subAdminArea ?: it.adminArea ?: it.featureName }
            } catch (e: Exception) {
                null
            }
        }
    }
}
