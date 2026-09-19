package com.yarom.jewishcalendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
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

/** A day "tag" styled after a classic printed Hebrew wall-calendar page. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DayCell(
    date: LocalDate,
    hebrewDate: HebrewDate,
    isSelected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isToday = date == LocalDate.now()
    val isSpecial = hebrewDate.isShabbos || hebrewDate.isYomTov

    val tagColor = when {
        isSelected -> DeepTeal
        isSpecial -> Burgundy
        else -> BrassGold
    }
    val bodyBackground = if (isSelected) DeepTeal.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    val numberColor = if (isSelected) DeepTeal else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .aspectRatio(0.72f)
            .clip(RoundedCornerShape(10.dp))
            .background(bodyBackground)
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
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 3.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = numberColor,
                fontFamily = FontFamily.Serif,
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold,
                fontSize = 22.sp,
            )
            Text(
                text = hebrewDate.hebrewDayOfMonthLabel,
                color = if (isSpecial) Burgundy else BrassGold,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
            )
            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .size(if (hasEvents) 6.dp else 0.dp)
                    .clip(CircleShape)
                    .background(if (hasEvents) tagColor else Color.Transparent),
            )
        }
    }
}
