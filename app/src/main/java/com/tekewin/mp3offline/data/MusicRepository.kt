package com.tekewin.mp3offline.data

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator

/**
 * Reads MP3 files in Internal storage › Music (including sub-folders) from MediaStore.
 * Nothing is copied, cached or sent anywhere: this is a read-only query on the device.
 */
class MusicRepository(private val context: Context) {

    @Suppress("DEPRECATION") // MediaStore DATA column is only used below Android 10.
    suspend fun loadSongs(): List<Song> = withContext(Dispatchers.IO) {
        val modern = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val collection = if (modern) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.DISPLAY_NAME)
            add(if (modern) MediaStore.Audio.Media.RELATIVE_PATH else MediaStore.Audio.Media.DATA)
        }.toTypedArray()

        val storageRoot = legacyStorageRoot()
        val pathColumn = if (modern) {
            MediaStore.Audio.Media.RELATIVE_PATH
        } else {
            MediaStore.Audio.Media.DATA
        }
        val pathPrefix = if (modern) "Music/%" else "$storageRoot/Music/%"
        val selection = "$pathColumn LIKE ? AND " +
            "(${MediaStore.Audio.Media.MIME_TYPE} = ? OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?)"
        val args = arrayOf(pathPrefix, "audio/mpeg", "%.mp3")

        val songs = ArrayList<Song>()
        context.contentResolver.query(collection, projection, selection, args, null)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val pathCol = c.getColumnIndexOrThrow(pathColumn)

            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val displayName = c.getString(nameCol).orEmpty()
                val rawPath = c.getString(pathCol).orEmpty()
                val key = if (modern) {
                    rawPath + displayName
                } else {
                    rawPath.removePrefix("$storageRoot/")
                }
                if (key.isBlank()) continue

                songs += Song(
                    id = id,
                    key = key,
                    title = c.getString(titleCol).cleanTag()
                        ?: displayName.substringBeforeLast('.').ifBlank { displayName },
                    artist = c.getString(artistCol).cleanTag() ?: UNKNOWN_ARTIST,
                    album = c.getString(albumCol).cleanTag(),
                    durationMs = c.getLong(durationCol).coerceAtLeast(0),
                    uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id),
                )
            }
        }
        songs
    }

    @Suppress("DEPRECATION")
    private fun legacyStorageRoot(): String =
        Environment.getExternalStorageDirectory().absolutePath.trimEnd('/')

    private fun String?.cleanTag(): String? =
        this?.trim()?.takeIf { it.isNotEmpty() && it != MediaStore.UNKNOWN_STRING }

    companion object {
        const val UNKNOWN_ARTIST = "Unknown artist"

        /** Locale-aware, case-insensitive ordering for names and titles. */
        fun collator(): Collator = Collator.getInstance().apply { strength = Collator.SECONDARY }

        /** Groups songs by artist; artists A–Z, each artist's songs A–Z by title. */
        fun groupByArtist(songs: List<Song>): List<Artist> {
            val collator = collator()
            return songs.groupBy { it.artist }
                .map { (name, list) -> Artist(name, list.sortedWith { a, b -> collator.compare(a.title, b.title) }) }
                .sortedWith { a, b ->
                    // Keep "Unknown artist" at the end.
                    when {
                        a.name == UNKNOWN_ARTIST -> 1
                        b.name == UNKNOWN_ARTIST -> -1
                        else -> collator.compare(a.name, b.name)
                    }
                }
        }
    }
}
