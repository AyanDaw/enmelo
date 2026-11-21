package io.github.ayandaw.enmelo

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log

fun loadSongs(context: Context): List<Song> {
    val songs = mutableListOf<Song>()
    val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DURATION
    )
    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
    val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

    context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idCol)
            songs += Song(
                id = id,
                title = cursor.getString(titleCol) ?: "Unknown",
                artist = cursor.getString(artistCol)?.takeIf { it != MediaStore.UNKNOWN_STRING } ?: "Unknown",
                album = cursor.getString(albumCol)?.takeIf { it != MediaStore.UNKNOWN_STRING } ?: "Unknown",
                durationMs = cursor.getLong(durationCol),
                uri = ContentUris.withAppendedId(collection, id)
            )
        }
    }

    return songs
}

fun debugFolders(context: Context) {
    val projection = arrayOf(MediaStore.Audio.Media.DATA)
    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
    val counts = mutableMapOf<String, Int>()
    context.contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null
    )?.use { c ->
        val col = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        while (c.moveToNext()) {
            val folder = c.getString(col)?.substringBeforeLast('/') ?: "?"
            counts[folder] = (counts[folder] ?: 0) + 1
        }
    }
    counts.entries.sortedByDescending { it.value }
        .forEach { Log.d("EnmeloDebug", "${it.value}  ${it.key}") }
}