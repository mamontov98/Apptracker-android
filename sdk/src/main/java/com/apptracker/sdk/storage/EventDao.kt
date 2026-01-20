package com.apptracker.sdk.storage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Data Access Object for EventEntity
@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>): List<Long>

    @Query("SELECT * FROM events ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getPendingEvents(limit: Int): List<EventEntity>

    @Query("SELECT COUNT(*) FROM events")
    suspend fun getEventCount(): Int

    @Query("SELECT COUNT(*) FROM events")
    fun getEventCountFlow(): Flow<Int>

    @Delete
    suspend fun delete(event: EventEntity)

    @Delete
    suspend fun deleteAll(events: List<EventEntity>)

    @Query("DELETE FROM events WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}



