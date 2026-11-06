package com.byd.workout.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.byd.workout.data.LogDto
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

@Entity(tableName = "plan_item")
data class PlanItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayIndex: Int,
    val position: Int,
    val name: String,
)

@Entity(tableName = "log_entry", indices = [Index("date")])
data class LogEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val position: Int,
    val name: String,
    val weight: String,
    val sets: List<String>,
) {
    fun toDto(): LogDto = LogDto(name = name, weight = weight, sets = sets)
}

@Entity(tableName = "media_item")
data class MediaItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,
    val title: String,
    val type: String,
    val durationMs: Long,
    val addedAt: Long,
)

class Converters {
    private val listSerializer = ListSerializer(String.serializer())

    @TypeConverter
    fun stringListToJson(value: List<String>): String = Json.encodeToString(listSerializer, value)

    @TypeConverter
    fun jsonToStringList(value: String): List<String> = Json.decodeFromString(listSerializer, value)
}
