package com.byd.workout.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.byd.workout.data.db.MediaDao
import com.byd.workout.data.db.MediaItemEntity
import kotlinx.coroutines.flow.Flow

class MediaRepository(private val dao: MediaDao, private val context: Context) {

    val items: Flow<List<MediaItemEntity>> = dao.observeAll()

    suspend fun add(uris: List<Uri>) {
        val resolver = context.contentResolver
        for (uri in uris) {
            try {
                resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

                var title = "Unknown"
                resolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            val name = cursor.getString(nameIndex)
                            val dotIndex = name.lastIndexOf('.')
                            title = if (dotIndex > 0) name.substring(0, dotIndex) else name
                        }
                    }
                }

                val mime = resolver.getType(uri) ?: ""
                val type = if (mime.startsWith("video/")) "video" else "audio"

                dao.insert(
                    MediaItemEntity(
                        uri = uri.toString(),
                        title = title,
                        type = type,
                        durationMs = 0L,
                        addedAt = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                // Ignore failure for a specific URI, continue to others
            }
        }
    }

    suspend fun remove(item: MediaItemEntity) {
        try {
            context.contentResolver.releasePersistableUriPermission(
                Uri.parse(item.uri),
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: Exception) {
            // Ignore if already released or invalid
        }
        dao.delete(item)
    }

    suspend fun setDuration(id: Long, ms: Long) {
        dao.updateDuration(id, ms)
    }
}
