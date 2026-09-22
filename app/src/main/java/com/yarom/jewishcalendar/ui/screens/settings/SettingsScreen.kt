package com.yarom.jewishcalendar.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yarom.jewishcalendar.R
import com.yarom.jewishcalendar.data.repository.ThemeMode
import com.yarom.jewishcalendar.domain.location.CityPresets
import com.yarom.jewishcalendar.domain.zmanim.CalculationMethod
import com.yarom.jewishcalendar.domain.zmanim.ZmanType
import com.yarom.jewishcalendar.ui.SettingsViewModel
import com.yarom.jewishcalendar.ui.components.CalendarPageFrame
import com.yarom.jewishcalendar.ui.components.labelRes
import com.yarom.jewishcalendar.ui.theme.BrassGold
import com.yarom.jewishcalendar.ui.theme.DeepTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel, onManageEvents: () -> Unit) {
    val settings by settingsViewModel.settings.collectAsState()
    val current = settings ?: return
    var citySearch by remember { mutableStateOf("") }

    CalendarPageFrame(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            item {
                Text(
                    stringResource(R.string.settings_location),
                    style = MaterialTheme.typography.titleLarge,
                    color = DeepTeal,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.settings_use_gps))
                    Switch(
                        checked = current.useGps,
                        onCheckedChange = { if (it) settingsViewModel.useDeviceLocation() },
                    )
                }

                // Emphasized so the city zmanim are actually computed for is always obvious
                // (spec follow-up).
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrassGold.copy(alpha = 0.16f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = DeepTeal)
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text("מיקום נוכחי", style = MaterialTheme.typography.labelMedium, color = DeepTeal.copy(alpha = 0.75f))
                        Text(
                            current.coordinates.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal,
                        )
                    }
                }

                OutlinedTextField(
                    value = citySearch,
                    onValueChange = { citySearch = it },
                    label = { Text("חיפוש עיר/יישוב") },
                    placeholder = { Text("לדוגמה: תל אביב") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                // Search-only, per spec follow-up - no browsable default list, just matches as you type.
                val results = remember(citySearch) { CityPresets.search(citySearch) }
                if (results.isNotEmpty()) {
                    Column(modifier = Modifier.heightIn(max = 260.dp)) {
                        results.take(20).forEach { city ->
                            ListItem(
                                headlineContent = { Text(city.name) },
                                modifier = Modifier.clickable {
                                    settingsViewModel.selectCity(city)
                                    citySearch = ""
                                },
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = DeepTeal.copy(alpha = 0.2f))
                Text(
                    stringResource(R.string.settings_calculation_method),
                    style = MaterialTheme.typography.titleLarge,
                    color = DeepTeal,
                )

                var methodMenuExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = methodMenuExpanded, onExpandedChange = { methodMenuExpanded = it }) {
                    OutlinedTextField(
                        value = current.calculationMethod.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    DropdownMenu(
                        expanded = methodMenuExpanded,
                        onDismissRequest = { methodMenuExpanded = false },
                    ) {
                        CalculationMethod.entries.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.displayName) },
                                onClick = {
                                    settingsViewModel.setCalculationMethod(method)
                                    methodMenuExpanded = false
                                },
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = DeepTeal.copy(alpha = 0.2f))
                Text(
                    stringResource(R.string.settings_visible_zmanim),
                    style = MaterialTheme.typography.titleLarge,
                    color = DeepTeal,
                )
            }

            items(ZmanType.entries) { type ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(type.labelRes()))
                    Checkbox(
                        checked = type in current.visibleZmanim,
                        onCheckedChange = { settingsViewModel.toggleZman(type, it) },
                    )
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = DeepTeal.copy(alpha = 0.2f))
                Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleLarge, color = DeepTeal)

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    ThemeMode.entries.forEach { mode ->
                        Button(onClick = { settingsViewModel.setThemeMode(mode) }) {
                            Text(
                                when (mode) {
                                    ThemeMode.SYSTEM -> "מערכת"
                                    ThemeMode.LIGHT -> "בהיר"
                                    ThemeMode.DARK -> "כהה"
                                },
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Material You (צבעים דינמיים)")
                    Switch(checked = current.useDynamicColor, onCheckedChange = { settingsViewModel.setUseDynamicColor(it) })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = DeepTeal.copy(alpha = 0.2f))
                ListItem(
                    headlineContent = { Text("ניהול אירועים") },
                    supportingContent = { Text("צפייה, עריכה ומחיקה של כל האירועים") },
                    leadingContent = { Icon(Icons.Default.Event, contentDescription = null, tint = DeepTeal) },
                    trailingContent = { Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = DeepTeal) },
                    modifier = Modifier.clickable(onClick = onManageEvents),
                )
            }
        }
    }
}
