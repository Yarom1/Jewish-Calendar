package com.yarom.jewishcalendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarom.jewishcalendar.domain.hebrew.HebrewDate
import java.time.LocalDate

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
    val background = when {
        isSelected -> MaterialTheme.colorScheme.primary
        hebrewDate.isShabbos || hebrewDate.isYomTov -> MaterialTheme.colorScheme.secondaryContainer
        else -> Color.Transparent
    }
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = contentColor,
            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
            fontSize = 16.sp,
        )
        Text(
            text = hebrewDate.hebrewDayOfMonthLabel,
            color = contentColor,
            fontSize = 11.sp,
        )
        if (hasEvents) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (isSelected) contentColor else MaterialTheme.colorScheme.primary)
                    .aspectRatio(1f)
                    .fillMaxWidth(0.15f),
            )
        }
    }
}
