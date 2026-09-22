package com.yarom.jewishcalendar.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarom.jewishcalendar.domain.hebrew.HebrewDate
import com.yarom.jewishcalendar.ui.theme.BrassGold
import com.yarom.jewishcalendar.ui.theme.Burgundy
import com.yarom.jewishcalendar.ui.theme.DeepTeal
import com.yarom.jewishcalendar.ui.theme.Parchment
import java.time.LocalDate

private val hebrewWeekdayNames = listOf(
    "ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת קודש",
) // Sun..Sat

/**
 * A round, gold-framed "cartouche" day marker, echoing the printed Hebrew luach day badge.
 *
 * [isSelected] (tapped/focused, drives cross-tab navigation) gets the strongest treatment - a
 * filled teal circle. Today gets its own distinct marker (a thicker ring) when it isn't also the
 * selected day, so the two states never look identical.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DayBadge(
    date: LocalDate,
    hebrewDate: HebrewDate,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    zoomScale: Float = 1f,
) {
    val isToday = date == LocalDate.now()
    val isSpecial = hebrewDate.isShabbos || hebrewDate.isYomTov
    val ringColor = when {
        isSpecial -> Burgundy
        isToday -> DeepTeal
        else -> BrassGold
    }
    val ringWidth = if (isToday && !isSelected) 2.5.dp else 1.2.dp
    val fillColor = if (isSelected) DeepTeal else Parchment
    val letterColor = if (isSelected) Parchment else if (isSpecial) Burgundy else DeepTeal
    val size = (36 * zoomScale).dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongPress),
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(fillColor, CircleShape)
                .border(BorderStroke(ringWidth, ringColor), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = hebrewDate.hebrewDayOfMonthLabel,
                    color = letterColor,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = (12 * zoomScale).sp,
                    lineHeight = (13 * zoomScale).sp,
                )
                Text(
                    text = date.dayOfMonth.toString(),
                    color = letterColor.copy(alpha = 0.75f),
                    fontSize = (7 * zoomScale).sp,
                    lineHeight = (8 * zoomScale).sp,
                )
            }
        }
        Text(
            text = hebrewWeekdayNames[date.dayOfWeek.value % 7],
            color = if (isSpecial) Burgundy else DeepTeal,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (8 * zoomScale).sp,
            lineHeight = (9 * zoomScale).sp,
            maxLines = 1,
        )
    }
}
