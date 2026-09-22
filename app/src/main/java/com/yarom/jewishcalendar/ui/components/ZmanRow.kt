package com.yarom.jewishcalendar.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarom.jewishcalendar.domain.zmanim.ZmanType
import com.yarom.jewishcalendar.ui.theme.DeepTeal
import java.time.ZonedDateTime

/** A zman row styled like a printed calendar's time ticket: label, then a bold ink-teal time. */
@Composable
fun ZmanRow(type: ZmanType, time: ZonedDateTime?, modifier: Modifier = Modifier, zoomScale: Float = 1f) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
    ) {
        Text(
            stringResource(type.labelRes()),
            fontSize = (16 * zoomScale).sp,
        )
        Text(
            text = time.formatTime(),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = (19 * zoomScale).sp,
            color = DeepTeal,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
}
