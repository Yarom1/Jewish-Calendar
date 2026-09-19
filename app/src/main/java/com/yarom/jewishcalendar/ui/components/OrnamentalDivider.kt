package com.yarom.jewishcalendar.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarom.jewishcalendar.ui.theme.BrassGold

/** A small flourish rule, like the decorative borders on a printed Hebrew calendar page. */
@Composable
fun OrnamentalDivider(modifier: Modifier = Modifier, color: Color = BrassGold) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = color, thickness = 1.dp)
        Text("❧", color = color, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 8.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), color = color, thickness = 1.dp)
    }
}
