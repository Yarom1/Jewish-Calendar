package com.yarom.jewishcalendar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Serif for the classic printed-calendar headings/numerals; sans-serif stays for body copy
// so long Hebrew text (notes, event titles) remains easy to read.
private val ClassicSerif = FontFamily.Serif

val JewishCalendarTypography = Typography(
    headlineMedium = TextStyle(fontFamily = ClassicSerif, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    titleLarge = TextStyle(fontFamily = ClassicSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = ClassicSerif, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = ClassicSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp),
)
