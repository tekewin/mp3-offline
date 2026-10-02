package com.tekewin.mp3offline

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tekewin.mp3offline.data.Artist
import com.tekewin.mp3offline.data.MusicRepository
import com.tekewin.mp3offline.data.Playlist
import com.tekewin.mp3offline.data.PlaylistStore
import com.tekewin.mp3offline.data.SettingsStore
import com.tekewin.mp3offline.data.Song
import com.tekewin.mp3offline.data.ThemeMode
import com.tekewin.mp3offline.playback.PlaybackConnection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Library : Screen
    data class ArtistDetail(val name: String) : Screen
    data class PlaylistDetail(val id: String) : Screen
    data object NowPlaying : Screen
    data object Search : Screen
}

enum class LibraryTab { Artists, Playlists }

data class LibraryState(
    val loading: Boolean = true,
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val byKey: Map<String, Song> = emptyMap(),
    val byMediaId: Map<String, Song> = emptyMap(),
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = MusicRepository(app)
    private val playlistStore = PlaylistStore(app)
    private val settings = SettingsStore(app)
    val playback = PlaybackConnection(app)

    private val _library = MutableStateFlow(LibraryState())
    val library: StateFlow<LibraryState> = _library.asStateFlow()
    val playlists: StateFlow<List<Playlist>> = playlistStore.playlists
    val themeMode: StateFlow<ThemeMode> = settings.themeMode
    val player = playback.state

    private val _hasPermission = MutableStateFlow(hasAudioPermission())
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    // Buffered so a message sent before the UI starts listening isn't lost.
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    // Simple in-memory navigation: a back stack of screens.
    val backStack = mutableStateListOf<Screen>(Screen.Library)
    var libraryTab by mutableStateOf(LibraryTab.Artists)
    var playingFrom by mutableStateOf("")
        private set

    private var observer: ContentObserver? = null
    private var refreshJob: Job? = null

    init {
        viewModelScope.launch { playlistStore.ensureLoaded() }
        if (_hasPermission.value) onPermissionGranted()
    }

    // ---- Permission & library -------------------------------------------------------------

    fun checkPermission() {
        val granted = hasAudioPermission()
        _hasPermission.value = granted
        if (granted && observer == null) onPermissionGranted()
    }

    private fun onPermissionGranted() {
        if (observer == null) {
            val o = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) = refresh(debounceMs = 800)
            }
            getApplication<Application>().contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, o
            )
            observer = o
        }
        refresh()
    }

    /** Re-reads the Music folder. Also prunes playlist entries whose files are gone. */
    fun refresh(debounceMs: Long = 0) {
        if (!_hasPermission.value) return
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            if (debounceMs > 0) delay(debounceMs)
            _library.update { it.copy(loading = true) }
            val songs = try {
                repository.loadSongs()
            } catch (e: Exception) {
                _library.update { it.copy(loading = false) }
                return@launch
            }
            _library.value = LibraryState(
                loading = false,
                songs = songs,
                artists = MusicRepository.groupByArtist(songs),
                byKey = songs.associateBy { it.key },
                byMediaId = songs.associateBy { it.mediaId },
            )
            // An empty result with playlists present is more likely a storage hiccup than
            // every file being deleted, so don't empty every playlist in that case.
            if (songs.isNotEmpty()) prunePlaylists(songs)
        }
    }

    private suspend fun prunePlaylists(songs: List<Song>) {
        val removed = playlistStore.prune(songs.mapTo(HashSet()) { it.key })
        if (removed.isEmpty()) return
        val res = getApplication<Application>().resources
        val total = removed.values.sum()
        val message = if (removed.size == 1) {
            res.getQuantityString(R.plurals.pruned_one_playlist, total, total, removed.keys.first())
        } else {
            res.getQuantityString(R.plurals.pruned_many_playlists, total, total)
        }
        _messages.trySend(message)
    }

    private fun hasAudioPermission(): Boolean = ContextCompat.checkSelfPermission(
        getApplication(), audioPermission()
    ) == PackageManager.PERMISSION_GRANTED

    // ---- Navigation --------------------------------------------------------------------------

    val currentScreen: Screen get() = backStack.last()

    fun open(screen: Screen) {
        if (backStack.last() != screen) backStack.add(screen)
    }

    fun back(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    // ---- Playback ----------------------------------------------------------------------------

    fun play(songs: List<Song>, startIndex: Int = 0, shuffle: Boolean = false, from: String) {
        if (songs.isEmpty()) return
        playingFrom = from
        playback.play(songs, startIndex, shuffle)
    }

    // ---- Playlists ---------------------------------------------------------------------------

    fun songsOf(playlist: Playlist, byKey: Map<String, Song>): List<Song> =
        playlist.songKeys.mapNotNull { byKey[it] }

    fun createPlaylist(name: String, withSong: Song? = null) = viewModelScope.launch {
        playlistStore.create(name, listOfNotNull(withSong?.key))
    }

    fun renamePlaylist(id: String, name: String) = viewModelScope.launch {
        playlistStore.rename(id, name)
    }

    fun deletePlaylist(id: String) = viewModelScope.launch {
        backStack.removeAll { it == Screen.PlaylistDetail(id) }
        playlistStore.delete(id)
    }

    fun setInPlaylist(playlistId: String, song: Song, include: Boolean) = viewModelScope.launch {
        if (include) playlistStore.addSongs(playlistId, listOf(song.key))
        else playlistStore.removeSong(playlistId, song.key)
    }

    // ---- Settings ----------------------------------------------------------------------------

    fun setThemeMode(mode: ThemeMode) = settings.setThemeMode(mode)

    override fun onCleared() {
        observer?.let { getApplication<Application>().contentResolver.unregisterContentObserver(it) }
        observer = null
        playback.release()
        super.onCleared()
    }

    companion object {
        fun audioPermission(): String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
            else Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
