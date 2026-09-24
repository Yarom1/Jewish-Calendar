package com.yarom.jewishcalendar.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yarom.jewishcalendar.ui.theme.DeepTeal

/**
 * The zoom (+/-), jump-to-date search, "back to today" and fullscreen controls shared by every
 * calendar view (spec follow-up: these controls should be available consistently everywhere, not
 * just in the weekly view).
 */
@Composable
fun ViewControlsRow(
    zoomScale: Float,
    onZoomChange: (Float) -> Unit,
    onSearchClick: () -> Unit,
    onTodayClick: () -> Unit,
    isFullscreen: Boolean,
    onFullscreenToggle: () -> Unit,
    onPrintClick: () -> Unit,
    modifier: Modifier = Modifier,
    minZoom: Float = 0.7f,
    maxZoom: Float = 2.2f,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrintClick, modifier = Modifier.width(28.dp)) {
            Icon(Icons.Default.Print, contentDescription = "הדפסה", tint = DeepTeal)
        }
        IconButton(onClick = onFullscreenToggle, modifier = Modifier.width(28.dp)) {
            Icon(
                if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription = if (isFullscreen) "יציאה ממסך מלא" else "מסך מלא",
                tint = DeepTeal,
            )
        }
        IconButton(onClick = onTodayClick, modifier = Modifier.width(28.dp)) {
            Icon(Icons.Default.Today, contentDescription = "חזרה להיום", tint = DeepTeal)
        }
        IconButton(onClick = onSearchClick, modifier = Modifier.width(28.dp)) {
            Icon(Icons.Default.Search, contentDescription = "קפיצה לתאריך", tint = DeepTeal)
        }
        IconButton(
            onClick = { onZoomChange((zoomScale - 0.15f).coerceIn(minZoom, maxZoom)) },
            modifier = Modifier.width(28.dp),
        ) {
            Icon(Icons.Default.Remove, contentDescription = "הקטן תצוגה", tint = DeepTeal)
        }
        IconButton(
            onClick = { onZoomChange((zoomScale + 0.15f).coerceIn(minZoom, maxZoom)) },
            modifier = Modifier.width(28.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "הגדל תצוגה", tint = DeepTeal)
        }
    }
}
