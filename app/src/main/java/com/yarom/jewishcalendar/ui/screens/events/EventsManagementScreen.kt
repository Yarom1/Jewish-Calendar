package com.yarom.jewishcalendar.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yarom.jewishcalendar.data.local.entity.CalendarOwner
import com.yarom.jewishcalendar.data.local.entity.EventEntity
import com.yarom.jewishcalendar.data.local.entity.RecurrenceType
import com.yarom.jewishcalendar.ui.EventViewModel
import com.yarom.jewishcalendar.ui.components.CalendarPageFrame
import com.yarom.jewishcalendar.ui.screens.addevent.AddEventSheet
import com.yarom.jewishcalendar.ui.theme.Burgundy
import com.yarom.jewishcalendar.ui.theme.DeepTeal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Centralized create/view/edit/delete for every event rule (spec follow-up), reachable from Settings. */
@Composable
fun EventsManagementScreen(eventViewModel: EventViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val events by eventViewModel.allEvents.collectAsState()
    var editingEvent by remember { mutableStateOf<EventEntity?>(null) }
    var creatingNew by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(onClick = { creatingNew = true }) {
                Icon(Icons.Default.Add, contentDescription = "אירוע חדש")
            }
        },
    ) { padding ->
        CalendarPageFrame(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "חזרה", tint = DeepTeal)
                    }
                    Text("ניהול אירועים", style = MaterialTheme.typography.titleLarge, color = DeepTeal)
                }
                HorizontalDivider(color = DeepTeal.copy(alpha = 0.2f))

                if (events.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("אין אירועים עדיין", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(events, key = { it.id }) { event ->
                            EventRow(
                                event = event,
                                onClick = { editingEvent = event },
                                onDelete = { eventViewModel.delete(event) },
                            )
                            HorizontalDivider(color = DeepTeal.copy(alpha = 0.15f))
                        }
                    }
                }
            }
        }
    }

    if (creatingNew) {
        AddEventSheet(
            date = LocalDate.now(),
            eventViewModel = eventViewModel,
            onDismiss = { creatingNew = false },
        )
    }
    editingEvent?.let { event ->
        AddEventSheet(
            date = LocalDate.ofEpochDay(event.startEpochDay),
            eventViewModel = eventViewModel,
            onDismiss = { editingEvent = null },
            existingEvent = event,
        )
    }
}

@Composable
private fun EventRow(event: EventEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium)
            val date = LocalDate.ofEpochDay(event.startEpochDay)
            val dateLabel = date.format(DateTimeFormatter.ofPattern("d/M/yyyy"))
            val ownerLabel = if (event.calendarOwner == CalendarOwner.FAMILY) "משפחתי" else "אישי"
            Text(
                "$dateLabel · ${recurrenceLabel(event.recurrenceType)} · $ownerLabel",
                style = MaterialTheme.typography.bodySmall,
                color = if (event.calendarOwner == CalendarOwner.FAMILY) DeepTeal else Color.Gray,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "מחיקה", tint = Burgundy)
        }
    }
}

private fun recurrenceLabel(type: RecurrenceType): String = when (type) {
    RecurrenceType.NONE -> "חד פעמי"
    RecurrenceType.DAILY -> "יומי"
    RecurrenceType.WEEKLY -> "שבועי"
    RecurrenceType.MONTHLY_GREGORIAN -> "חודשי"
    RecurrenceType.YEARLY_GREGORIAN -> "שנתי"
    RecurrenceType.ROSH_CHODESH -> "ראש חודש"
    RecurrenceType.YEARLY_HEBREW -> "שנתי עברי"
}
