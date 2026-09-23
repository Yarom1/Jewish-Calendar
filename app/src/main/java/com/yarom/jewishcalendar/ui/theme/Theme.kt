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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext

// Palette lifted from the classic printed Hebrew wall-calendar look: parchment pages, deep
// teal ink, burgundy and brass-gold accents for Shabbat/Yom Tov tags. These raw literals back
// the two color schemes below; screens should use the theme-aware properties further down
// (DeepTeal, Burgundy, BrassGold, Parchment) instead of these directly, so text stays legible
// in both light and dark mode.
private val ParchmentLight = Color(0xFFFBF3E1)
private val ParchmentSurfaceLight = Color(0xFFFFFCF5)
private val InkBrown = Color(0xFF2B2015)
private val DeepTealLight = Color(0xFF184A47)
private val BrassGoldBase = Color(0xFFC9A227)
private val BurgundyLight = Color(0xFF7A1F2B)

// Dark-mode ink: the light scheme's deep teal/burgundy are near-black and unreadable on a dark
// background, so the dark scheme uses brighter, higher-contrast variants of the same hues
// instead (spec follow-up: dark mode was unreadable before this).
private val ParchmentDarkInk = Color(0xFFEFE4C8)
private val TealSurfaceDark = Color(0xFF162622)
private val TealBackgroundDark = Color(0xFF0F1D1A)
private val TealCardDark = Color(0xFF1B2E29)
private val GoldOnDark = Color(0xFFD9B84A)
private val BurgundyOnDark = Color(0xFFE0919B)

// User-created events get their own dedicated hue (indigo), distinct from every other calendar
// color - teal is halachic times, burgundy is Shabbat/Yom Tov, gold is general accents (spec
// follow-up: event text/markers were reusing burgundy, indistinguishable from holiday text).
private val EventIndigoLight = Color(0xFF33448F)
private val EventIndigoDark = Color(0xFFA9B7EE)

/** The muted "desk" tone behind the calendar page card, so its drop shadow reads clearly. */
val DeskBackground = Color(0xFFCFC9BA)
val DeskBackgroundDark = Color(0xFF15120E)

private val ClassicLightColors = lightColorScheme(
    primary = DeepTealLight,
    onPrimary = ParchmentLight,
    primaryContainer = DeepTealLight,
    onPrimaryContainer = ParchmentLight,
    secondary = BrassGoldBase,
    onSecondary = InkBrown,
    secondaryContainer = BrassGoldBase,
    onSecondaryContainer = InkBrown,
    tertiary = BurgundyLight,
    onTertiary = ParchmentLight,
    tertiaryContainer = Color(0xFFE9D2A8),
    onTertiaryContainer = BurgundyLight,
    background = ParchmentLight,
    onBackground = InkBrown,
    surface = ParchmentSurfaceLight,
    onSurface = InkBrown,
    surfaceVariant = Color(0xFFEFE1C4),
    onSurfaceVariant = InkBrown,
    outline = Color(0xFFB59A5C),
)

private val ClassicDarkColors = darkColorScheme(
    primary = GoldOnDark,
    onPrimary = TealBackgroundDark,
    primaryContainer = TealCardDark,
    onPrimaryContainer = ParchmentDarkInk,
    secondary = BrassGoldBase,
    onSecondary = TealBackgroundDark,
    secondaryContainer = Color(0xFF3A2E12),
    onSecondaryContainer = BrassGoldBase,
    tertiary = BurgundyOnDark,
    onTertiary = TealBackgroundDark,
    tertiaryContainer = Color(0xFF4A2229),
    onTertiaryContainer = BurgundyOnDark,
    background = TealBackgroundDark,
    onBackground = ParchmentDarkInk,
    surface = TealSurfaceDark,
    onSurface = ParchmentDarkInk,
    surfaceVariant = TealCardDark,
    onSurfaceVariant = ParchmentDarkInk,
    outline = Color(0xFF8C8161),
)

/**
 * Theme-aware "ink" colors: use these (not raw literals) for text/tints/accents across the app
 * so contrast stays correct in both light and dark mode. They resolve to the same hues in light
 * mode as before, and to brighter, dark-mode-safe variants when the dark scheme is active.
 */
val DeepTeal: Color
    @Composable get() = MaterialTheme.colorScheme.primary

val Burgundy: Color
    @Composable get() = MaterialTheme.colorScheme.tertiary

val BrassGold: Color
    @Composable get() = MaterialTheme.colorScheme.secondary

val Parchment: Color
    @Composable get() = MaterialTheme.colorScheme.onPrimary

val EventColor: Color
    @Composable get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) EventIndigoDark else EventIndigoLight

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
