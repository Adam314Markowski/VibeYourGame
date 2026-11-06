package com.byd.workout.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plan_item ORDER BY dayIndex, position")
    fun observeAll(): Flow<List<PlanItemEntity>>

    @Query("SELECT COUNT(*) FROM plan_item")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(items: List<PlanItemEntity>)

    @Query("DELETE FROM plan_item WHERE dayIndex = :dayIndex")
    suspend fun deleteDay(dayIndex: Int)

    @Transaction
    suspend fun replaceDay(dayIndex: Int, names: List<String>) {
        deleteDay(dayIndex)
        insertAll(names.mapIndexed { position, name -> PlanItemEntity(dayIndex = dayIndex, position = position, name = name) })
    }
}

@Dao
interface LogDao {
    @Query("SELECT * FROM log_entry ORDER BY date, position")
    fun observeAll(): Flow<List<LogEntryEntity>>

    @Query("SELECT * FROM log_entry WHERE date = :date ORDER BY position")
    suspend fun getDay(date: String): List<LogEntryEntity>

    @Query("SELECT COALESCE(MAX(position), -1) FROM log_entry WHERE date = :date")
    suspend fun maxPosition(date: String): Int

    @Insert
    suspend fun insertAll(entries: List<LogEntryEntity>)

    @Transaction
    suspend fun append(date: String, entries: List<LogEntryEntity>) {
        val start = maxPosition(date) + 1
        insertAll(entries.mapIndexed { offset, entry -> entry.copy(id = 0, date = date, position = start + offset) })
    }
}

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_item ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<MediaItemEntity>>

    @Insert
    suspend fun insert(item: MediaItemEntity): Long

    @Delete
    suspend fun delete(item: MediaItemEntity)

    @Query("UPDATE media_item SET durationMs = :durationMs WHERE id = :id")
    suspend fun updateDuration(id: Long, durationMs: Long)
}
