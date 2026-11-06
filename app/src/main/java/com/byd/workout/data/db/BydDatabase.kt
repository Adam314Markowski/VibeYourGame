package com.byd.workout.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [PlanItemEntity::class, LogEntryEntity::class, MediaItemEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class BydDatabase : RoomDatabase() {
    abstract fun planDao(): PlanDao
    abstract fun logDao(): LogDao
    abstract fun mediaDao(): MediaDao

    companion object {
        fun create(context: Context): BydDatabase =
            Room.databaseBuilder(context, BydDatabase::class.java, "byd.db").build()
    }
}
