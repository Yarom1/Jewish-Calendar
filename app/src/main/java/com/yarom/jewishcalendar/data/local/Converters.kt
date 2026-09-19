package com.yarom.jewishcalendar.data.local

import androidx.room.TypeConverter
import com.yarom.jewishcalendar.data.local.entity.CalendarOwner
import com.yarom.jewishcalendar.data.local.entity.RecurrenceEnd
import com.yarom.jewishcalendar.data.local.entity.RecurrenceType

class Converters {
    @TypeConverter
    fun fromCalendarOwner(value: CalendarOwner): String = value.name

    @TypeConverter
    fun toCalendarOwner(value: String): CalendarOwner = CalendarOwner.valueOf(value)

    @TypeConverter
    fun fromRecurrenceType(value: RecurrenceType): String = value.name

    @TypeConverter
    fun toRecurrenceType(value: String): RecurrenceType = RecurrenceType.valueOf(value)

    @TypeConverter
    fun fromRecurrenceEnd(value: RecurrenceEnd): String = value.name

    @TypeConverter
    fun toRecurrenceEnd(value: String): RecurrenceEnd = RecurrenceEnd.valueOf(value)
}
