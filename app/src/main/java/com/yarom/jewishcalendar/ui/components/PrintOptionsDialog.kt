package com.yarom.jewishcalendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.yarom.jewishcalendar.ui.theme.BrassGold
import com.yarom.jewishcalendar.ui.theme.DeepTeal

/** Lets the user pick how many copies of the printed page to tile onto one physical sheet
 * (1/2/4/8-up), then hands off to Android's own print dialog - which already offers "Save as
 * PDF" and every installed print app/service, so there's no separate save-vs-app-choice step. */
@Composable
fun PrintOptionsDialog(onDismiss: () -> Unit, onConfirm: (copiesPerPage: Int) -> Unit) {
    var copiesPerPage by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("הדפסה", color = DeepTeal) },
        text = {
            Column {
                Text("כמה עותקים בדף אחד?", color = DeepTeal.copy(alpha = 0.8f))
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(1, 2, 4, 8).forEach { option ->
                        val selected = option == copiesPerPage
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) DeepTeal else BrassGold.copy(alpha = 0.2f))
                                .clickable { copiesPerPage = option }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text(
                                option.toString(),
                                color = if (selected) BrassGold else DeepTeal,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(copiesPerPage) },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = DeepTeal),
            ) {
                Text("הדפסה", color = BrassGold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ביטול", color = DeepTeal)
            }
        },
    )
}
