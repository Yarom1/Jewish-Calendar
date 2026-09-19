package com.yarom.jewishcalendar.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Palette lifted from the classic printed Hebrew wall-calendar look: parchment pages, deep
// teal ink, burgundy and brass-gold accents for Shabbat/Yom Tov tags.
val Parchment = Color(0xFFFBF3E1)
val ParchmentSurface = Color(0xFFFFFCF5)
val InkBrown = Color(0xFF2B2015)
val DeepTeal = Color(0xFF184A47)
val DeepTealDark = Color(0xFF0E332F)
val BrassGold = Color(0xFFC9A227)
val Burgundy = Color(0xFF7A1F2B)
val BurgundyDark = Color(0xFF5C1620)

/** The muted "desk" tone behind the calendar page card, so its drop shadow reads clearly. */
val DeskBackground = Color(0xFFCFC9BA)

private val ClassicLightColors = lightColorScheme(
    primary = DeepTeal,
    onPrimary = Parchment,
    primaryContainer = DeepTeal,
    onPrimaryContainer = Parchment,
    secondary = BrassGold,
    onSecondary = InkBrown,
    secondaryContainer = BrassGold,
    onSecondaryContainer = InkBrown,
    tertiary = Burgundy,
    onTertiary = Parchment,
    tertiaryContainer = Color(0xFFE9D2A8),
    onTertiaryContainer = Burgundy,
    background = Parchment,
    onBackground = InkBrown,
    surface = ParchmentSurface,
    onSurface = InkBrown,
    surfaceVariant = Color(0xFFEFE1C4),
    onSurfaceVariant = InkBrown,
    outline = Color(0xFFB59A5C),
)

private val ClassicDarkColors = darkColorScheme(
    primary = BrassGold,
    onPrimary = InkBrown,
    primaryContainer = DeepTealDark,
    onPrimaryContainer = Parchment,
    secondary = BrassGold,
    onSecondary = InkBrown,
    secondaryContainer = Color(0xFF3A2E12),
    onSecondaryContainer = BrassGold,
    tertiary = Color(0xFFD98A94),
    onTertiary = BurgundyDark,
    tertiaryContainer = BurgundyDark,
    onTertiaryContainer = Color(0xFFF0D6DA),
    background = Color(0xFF17140F),
    onBackground = Parchment,
    surface = Color(0xFF1F1B14),
    onSurface = Parchment,
    surfaceVariant = Color(0xFF2A2419),
    onSurfaceVariant = Parchment,
    outline = Color(0xFF7A6B45),
)

@Composable
fun JewishCalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> ClassicDarkColors
        else -> ClassicLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = JewishCalendarTypography,
        content = content,
    )
}
