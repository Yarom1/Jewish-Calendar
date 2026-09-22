package com.yarom.jewishcalendar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

private val hebrewMonthNames = listOf(
    "ניסן", "אייר", "סיון", "תמוז", "אב", "אלול",
    "תשרי", "חשון", "כסלו", "טבת", "שבט", "אדר", "אדר ב'",
)

/** A jump-to-date control (spec follow-up): Gregorian or Hebrew date search. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSearchDialog(
    onDismiss: () -> Unit,
    onGregorianDateChosen: (LocalDate) -> Unit,
    onHebrewDateChosen: (year: Int, month: Int, day: Int) -> Unit,
) {
    var isHebrewMode by remember { mutableStateOf(false) }
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf(1) }
    var year by remember { mutableStateOf("") }
    var monthMenuExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("קפיצה לתאריך") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { isHebrewMode = false }) { Text(if (!isHebrewMode) "● לועזי" else "לועזי") }
                    TextButton(onClick = { isHebrewMode = true }) { Text(if (isHebrewMode) "● עברי" else "עברי") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = day,
                        onValueChange = { day = it.filter(Char::isDigit).take(2) },
                        label = { Text("יום") },
                        modifier = Modifier.weight(1f),
                    )
                    if (isHebrewMode) {
                        ExposedDropdownMenuBox(
                            expanded = monthMenuExpanded,
                            onExpandedChange = { monthMenuExpanded = it },
                            modifier = Modifier.weight(2f),
                        ) {
                            OutlinedTextField(
                                value = hebrewMonthNames[month - 1],
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("חודש") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthMenuExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                            )
                            DropdownMenu(expanded = monthMenuExpanded, onDismissRequest = { monthMenuExpanded = false }) {
                                hebrewMonthNames.forEachIndexed { index, name ->
                                    DropdownMenuItem(text = { Text(name) }, onClick = { month = index + 1; monthMenuExpanded = false })
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = month.toString(),
                            onValueChange = { month = it.filter(Char::isDigit).take(2).toIntOrNull()?.coerceIn(1, 12) ?: month },
                            label = { Text("חודש") },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it.filter(Char::isDigit).take(4) },
                        label = { Text("שנה") },
                        modifier = Modifier.weight(1.3f),
                    )
                }
                error?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val d = day.toIntOrNull()
                val y = year.toIntOrNull()
                if (d == null || y == null) {
                    error = "נא למלא יום ושנה"
                    return@Button
                }
                if (isHebrewMode) {
                    onHebrewDateChosen(y, month, d)
                } else {
                    val date = runCatching { LocalDate.of(y, month, d) }.getOrNull()
                    if (date == null) {
                        error = "תאריך לא תקין"
                    } else {
                        onGregorianDateChosen(date)
                    }
                }
            }) { Text("עבור") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ביטול") }
        },
    )
}
