package com.yarom.jewishcalendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yarom.jewishcalendar.ui.theme.BrassGold

/**
 * Wraps a screen's content as a flat printed page: a drop shadow lifting it off the surrounding
 * desk background, and a double gold rule frame like the decorative borders on a printed Hebrew
 * wall calendar / "luach" sheet.
 */
@Composable
fun CalendarPageFrame(
    modifier: Modifier = Modifier,
    frameColor: Color = BrassGold,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(6.dp), clip = false)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, frameColor, RoundedCornerShape(6.dp))
            .padding(3.dp)
            .border(0.75.dp, frameColor.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            .padding(6.dp),
    ) {
        content()
    }
}
