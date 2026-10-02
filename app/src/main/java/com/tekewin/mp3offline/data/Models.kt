package com.tekewin.mp3offline.data

import android.net.Uri
import androidx.compose.runtime.Immutable

/** One MP3 file found in Internal storage › Music. */
@Immutable
data class Song(
    /** MediaStore row id. Used as the Media3 mediaId. */
    val id: Long,
    /**
     * Stable identity of the file: its path relative to the storage root,
     * e.g. "Music/Aurora Vale/Lanterns in the Rain.mp3". Playlists store this,
     * so they survive a MediaStore rebuild that hands out new row ids.
     */
    val key: String,
    val title: String,
    val artist: String,
    val album: String?,
    val durationMs: Long,
    val uri: Uri,
) {
    val mediaId: String get() = id.toString()
}

@Immutable
data class Artist(
    val name: String,
    /** Sorted A–Z by title. */
    val songs: List<Song>,
) {
    val durationMs: Long get() = songs.sumOf { it.durationMs }
}

@Immutable
data class Playlist(
    val id: String,
    val name: String,
    /** Song keys, in play order. */
    val songKeys: List<String>,
)

enum class ThemeMode { System, Light, Dark }

const val PLAYLIST_NAME_MAX = 40
