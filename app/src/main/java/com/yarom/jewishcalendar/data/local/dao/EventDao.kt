package com.yarom.jewishcalendar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yarom.jewishcalendar.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    /** All events whose anchor date could produce an occurrence on/after [fromEpochDay]; the
     * repository expands recurrence in memory since Room can't evaluate Hebrew-calendar rules. */
    @Query("SELECT * FROM events WHERE recurrenceType != 'NONE' OR startEpochDay >= :fromEpochDay ORDER BY startEpochDay ASC")
    fun observeRelevant(fromEpochDay: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY startEpochDay ASC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)
}
