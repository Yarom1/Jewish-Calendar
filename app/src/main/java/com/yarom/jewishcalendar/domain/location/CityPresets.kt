package com.yarom.jewishcalendar.domain.location

import com.yarom.jewishcalendar.domain.zmanim.Coordinates

/** Manual city picker fallback (spec 4.a "בחירה ידנית של עיר/יישוב"). */
object CityPresets {
    val cities: List<Coordinates> = listOf(
        Coordinates("ירושלים", 31.7683, 35.2137, 754.0, "Asia/Jerusalem"),
        Coordinates("תל אביב", 32.0853, 34.7818, 5.0, "Asia/Jerusalem"),
        Coordinates("חיפה", 32.7940, 34.9896, 20.0, "Asia/Jerusalem"),
        Coordinates("באר שבע", 31.2518, 34.7913, 280.0, "Asia/Jerusalem"),
        Coordinates("בני ברק", 32.0807, 34.8338, 32.0, "Asia/Jerusalem"),
        Coordinates("צפת", 32.9646, 35.4960, 900.0, "Asia/Jerusalem"),
        Coordinates("אילת", 29.5581, 34.9482, 12.0, "Asia/Jerusalem"),
        Coordinates("ניו יורק", 40.7128, -74.0060, 10.0, "America/New_York"),
        Coordinates("לוס אנג'לס", 34.0522, -118.2437, 71.0, "America/Los_Angeles"),
        Coordinates("לונדון", 51.5072, -0.1276, 11.0, "Europe/London"),
        Coordinates("פריז", 48.8566, 2.3522, 35.0, "Europe/Paris"),
        Coordinates("אנטוורפן", 51.2194, 4.4025, 8.0, "Europe/Brussels"),
        Coordinates("בואנוס איירס", -34.6037, -58.3816, 25.0, "America/Argentina/Buenos_Aires"),
        Coordinates("מלבורן", -37.8136, 144.9631, 31.0, "Australia/Melbourne"),
        Coordinates("טורונטו", 43.6532, -79.3832, 76.0, "America/Toronto"),
        Coordinates("מוסקבה", 55.7558, 37.6173, 156.0, "Europe/Moscow"),
    )
}
