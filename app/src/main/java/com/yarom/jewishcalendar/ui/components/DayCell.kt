package com.yarom.jewishcalendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

private val hebrewWeekdayLetters = listOf("א", "ב", "ג", "ד", "ה", "ו", "ש") // Sun..Sat

/** A day cell filling its grid slot edge-to-edge, like a printed physical wall-calendar grid. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DayCell(
    date: LocalDate,
    hebrewDate: HebrewDate,
    isSelected: Boolean,
    hasEvents: Boolean,
    today: LocalDate,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    zoomScale: Float = 1f,
) {
    val isToday = date == today
    val isSpecial = hebrewDate.isShabbos || hebrewDate.isYomTov

    val tagColor = when {
        isSelected -> DeepTeal
        isSpecial -> Burgundy
        else -> BrassGold
    }
    val bodyBackground = if (isSelected) DeepTeal.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
    val numberColor = if (isSelected) DeepTeal else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(bodyBackground)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(1.8.dp, DeepTeal, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                },
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongPress),
    ) {
        // Weekday "tag" strip, like the colored header on a printed calendar day.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(tagColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = hebrewWeekdayLetters[date.dayOfWeek.value % 7],
                color = Parchment,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = (11 * zoomScale).sp,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = numberColor,
                fontFamily = FontFamily.Serif,
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold,
                fontSize = (17 * zoomScale).sp,
            )
            Text(
                text = hebrewDate.hebrewDayOfMonthLabel,
                color = if (isSpecial) Burgundy else BrassGold,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = (10 * zoomScale).sp,
            )
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(if (hasEvents) 5.dp else 0.dp)
                    .clip(CircleShape)
                    .background(if (hasEvents) tagColor else Color.Transparent),
            )
        }
    }
}
