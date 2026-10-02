package com.tekewin.mp3offline.data

import android.content.Context
import android.util.Log
import androidx.core.util.AtomicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Playlists, saved as a small JSON file in the app's private storage.
 * Writes are atomic, so a crash mid-save can't corrupt the file.
 */
class PlaylistStore(context: Context) {

    private val file = AtomicFile(File(context.filesDir, "playlists.json"))
    private val mutex = Mutex()
    private var loaded = false

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    /**
     * Playlist song keys that a scan didn't find, with when they were first missed.
     * Saved alongside the playlists so the grace period survives app restarts.
     */
    private var missingSince: Map<String, Long> = emptyMap()

    suspend fun ensureLoaded() {
        mutex.withLock {
            if (loaded) return
            val saved = withContext(Dispatchers.IO) { read() }
            _playlists.value = saved.playlists
            missingSince = saved.missingSince
            loaded = true
        }
    }

    suspend fun create(name: String, songKeys: List<String> = emptyList()): String {
        val id = UUID.randomUUID().toString()
        mutate { it + Playlist(id, cleanName(name), songKeys.distinct()) }
        return id
    }

    suspend fun rename(id: String, name: String) = mutate { lists ->
        lists.map { if (it.id == id) it.copy(name = cleanName(name)) else it }
    }

    suspend fun delete(id: String) = mutate { lists -> lists.filterNot { it.id == id } }

    suspend fun addSongs(id: String, keys: List<String>) = mutate { lists ->
        lists.map { p ->
            if (p.id == id) p.copy(songKeys = (p.songKeys + keys).distinct()) else p
        }
    }

    suspend fun removeSong(id: String, key: String) = mutate { lists ->
        lists.map { p -> if (p.id == id) p.copy(songKeys = p.songKeys - key) else p }
    }

    /**
     * Removes songs whose files no longer exist. Returns how many were removed,
     * per playlist name (only playlists that changed).
     *
     * A song is only removed once it has been missing from two scans at least
     * [PRUNE_GRACE_MS] apart. MediaStore can briefly return a partial list while it
     * re-indexes (e.g. after a reboot), and that shouldn't cost anyone their playlists.
     */
    suspend fun prune(existingKeys: Set<String>, now: Long = System.currentTimeMillis()): Map<String, Int> {
        val removed = LinkedHashMap<String, Int>()
        ensureLoaded()
        mutex.withLock {
            val lists = _playlists.value
            val missing = lists.flatMapTo(HashSet()) { it.songKeys }.filterNot { it in existingKeys }
            // Keep the first-missed time for keys still missing; forget keys that came back.
            val since = missing.associateWith { missingSince[it] ?: now }
            val gone = since.filterValues { now - it >= PRUNE_GRACE_MS }.keys

            val updated = lists.map { p ->
                val kept = p.songKeys.filterNot { it in gone }
                val count = p.songKeys.size - kept.size
                if (count > 0) {
                    removed[p.name] = (removed[p.name] ?: 0) + count
                    p.copy(songKeys = kept)
                } else p
            }
            val updatedSince = since - gone
            if (updated == lists && updatedSince == missingSince) return removed
            withContext(Dispatchers.IO) { write(updated, updatedSince) }
            _playlists.value = updated
            missingSince = updatedSince
        }
        return removed
    }

    private suspend fun mutate(transform: (List<Playlist>) -> List<Playlist>) {
        ensureLoaded()
        mutex.withLock {
            val updated = transform(_playlists.value)
            if (updated == _playlists.value) return
            withContext(Dispatchers.IO) { write(updated, missingSince) }
            _playlists.value = updated
        }
    }

    private fun cleanName(name: String): String =
        name.trim().replace(Regex("\\s+"), " ").take(PLAYLIST_NAME_MAX).ifEmpty { "Playlist" }

    private class Saved(val playlists: List<Playlist>, val missingSince: Map<String, Long>)

    private fun read(): Saved {
        if (!file.baseFile.exists()) return Saved(emptyList(), emptyMap())
        return try {
            val root = JSONObject(String(file.readFully(), Charsets.UTF_8))
            val array = root.optJSONArray("playlists") ?: JSONArray()
            val playlists = List(array.length()) { i ->
                val o = array.getJSONObject(i)
                val songs = o.optJSONArray("songs") ?: JSONArray()
                Playlist(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    songKeys = List(songs.length()) { j -> songs.getString(j) },
                )
            }
            val missing = root.optJSONObject("missingSince")
            val missingSince = missing?.keys()?.asSequence()
                ?.associateWith { missing.getLong(it) }
                .orEmpty()
            Saved(playlists, missingSince)
        } catch (e: Exception) {
            Log.w(TAG, "Couldn't read playlists", e)
            Saved(emptyList(), emptyMap())
        }
    }

    private fun write(lists: List<Playlist>, missing: Map<String, Long>) {
        val root = JSONObject()
            .put("version", 1)
            .put("playlists", JSONArray().apply {
                lists.forEach { p ->
                    put(
                        JSONObject()
                            .put("id", p.id)
                            .put("name", p.name)
                            .put("songs", JSONArray(p.songKeys))
                    )
                }
            })
            .put("missingSince", JSONObject().apply { missing.forEach { (k, t) -> put(k, t) } })
        val out = file.startWrite()
        try {
            out.write(root.toString().toByteArray(Charsets.UTF_8))
            file.finishWrite(out)
        } catch (e: Exception) {
            file.failWrite(out)
            throw e
        }
    }

    private companion object {
        const val TAG = "PlaylistStore"

        /** How long a song must stay missing, across at least two scans, before it's pruned. */
        const val PRUNE_GRACE_MS = 5 * 60 * 1000L
    }
}
