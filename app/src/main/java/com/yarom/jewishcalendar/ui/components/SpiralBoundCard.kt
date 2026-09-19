package com.yarom.jewishcalendar.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.yarom.jewishcalendar.ui.theme.Parchment

/**
 * Wraps a screen's content in a "page" that reads as a physical object: a drop shadow lifting it
 * off the surrounding desk background, rounded card corners, and a punched spiral-binding strip
 * across the top like a real tear-off wall calendar.
 */
@Composable
fun SpiralBoundCard(
    modifier: Modifier = Modifier,
    ringStripColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        SpiralBindingStrip(color = ringStripColor)
        content()
    }
}

@Composable
private fun SpiralBindingStrip(color: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp)
            .background(color),
    ) {
        val holeRadiusPx = 4.5.dp.toPx()
        val spacingPx = 30.dp.toPx()
        val holeCount = (size.width / spacingPx).toInt().coerceAtLeast(4)
        val startX = (size.width - spacingPx * (holeCount - 1)) / 2f
        val centerY = size.height / 2f
        for (i in 0 until holeCount) {
            val cx = startX + spacingPx * i
            // Punched hole: parchment fill so the page shows through, with a thin dark ring for depth.
            drawCircle(color = Parchment, radius = holeRadiusPx, center = Offset(cx, centerY))
            drawCircle(
                color = Color.Black.copy(alpha = 0.18f),
                radius = holeRadiusPx,
                center = Offset(cx, centerY),
                style = Stroke(width = 1.dp.toPx()),
            )
        }
    }
}
