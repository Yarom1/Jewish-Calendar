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

/** A round, gold-framed "cartouche" day marker, echoing the printed Hebrew luach day badge. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DayBadge(
    date: LocalDate,
    hebrewDate: HebrewDate,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSpecial = hebrewDate.isShabbos || hebrewDate.isYomTov
    val ringColor = when {
        isSpecial -> Burgundy
        else -> BrassGold
    }
    val fillColor = if (isSelected) DeepTeal else Parchment
    val letterColor = if (isSelected) Parchment else if (isSpecial) Burgundy else DeepTeal

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongPress),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(fillColor, CircleShape)
                .border(BorderStroke(2.dp, ringColor), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = hebrewDate.hebrewDayOfMonthLabel,
                    color = letterColor,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
                Text(
                    text = date.dayOfMonth.toString(),
                    color = letterColor.copy(alpha = 0.75f),
                    fontSize = 10.sp,
                )
            }
        }
        Text(
            text = hebrewWeekdayNames[date.dayOfWeek.value % 7],
            color = if (isSpecial) Burgundy else DeepTeal,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
        )
    }
}
