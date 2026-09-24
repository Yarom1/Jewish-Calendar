package com.yarom.jewishcalendar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yarom.jewishcalendar.data.local.dao.EventDao
import com.yarom.jewishcalendar.data.local.entity.EventEntity

@Database(entities = [EventEntity::class], version = 2, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jewish_calendar.db",
                )
                    // Pre-release app with no migration history yet (spec follow-up: added
                    // EventEntity.imagePath in version 2) - acceptable to wipe local data on a
                    // schema bump rather than write a real migration at this stage.
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
