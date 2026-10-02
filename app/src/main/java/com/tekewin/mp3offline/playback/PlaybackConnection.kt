package com.tekewin.mp3offline.playback

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.tekewin.mp3offline.data.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/** What the UI needs to know about the player. */
data class PlayerState(
    val hasMedia: Boolean = false,
    val mediaId: String? = null,
    val title: String = "",
    val artist: String = "",
    val album: String? = null,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val nextTitle: String? = null,
)

/** The app's side of the media session: a MediaController plus a state flow for Compose. */
@OptIn(UnstableApi::class)
class PlaybackConnection(context: Context) {

    private val appContext = context.applicationContext
    private var future: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private val pending = ArrayList<(MediaController) -> Unit>()

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = publish(player)
    }

    fun connect() {
        if (future != null) return
        val token = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
        val f = MediaController.Builder(appContext, token).buildAsync()
        future = f
        f.addListener({
            if (future !== f) return@addListener // released meanwhile
            val c = runCatching { f.get() }.getOrNull() ?: run {
                future = null
                pending.clear()
                return@addListener
            }
            controller = c
            c.addListener(listener)
            publish(c)
            pending.forEach { it(c) }
            pending.clear()
        }, ContextCompat.getMainExecutor(appContext))
    }

    fun release() {
        controller?.removeListener(listener)
        future?.let { MediaController.releaseFuture(it) }
        // Don't replay taps queued while connecting the next time the app comes back.
        pending.clear()
        controller = null
        future = null
    }

    private fun withController(block: (MediaController) -> Unit) {
        val c = controller
        if (c != null) block(c) else {
            pending += block
            connect()
        }
    }

    fun play(songs: List<Song>, startIndex: Int = 0, shuffle: Boolean = false) {
        if (songs.isEmpty()) return
        withController { c ->
            val start = if (shuffle) Random.nextInt(songs.size) else startIndex.coerceIn(0, songs.lastIndex)
            c.shuffleModeEnabled = shuffle
            c.setMediaItems(songs.map { it.toMediaItem() }, start, 0L)
            c.prepare()
            c.play()
        }
    }

    fun togglePlayPause() = withController { c ->
        if (c.isPlaying) c.pause() else {
            if (c.playbackState == Player.STATE_ENDED) c.seekToDefaultPosition(0)
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
            c.play()
        }
    }

    fun next() = withController { it.seekToNext() }
    fun previous() = withController { it.seekToPrevious() }
    fun seekTo(positionMs: Long) = withController { it.seekTo(positionMs) }
    fun toggleShuffle() = withController { it.shuffleModeEnabled = !it.shuffleModeEnabled }

    fun cycleRepeat() = withController { c ->
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    /** Current position, read on demand (the UI polls it while visible). */
    fun positionMs(): Long = controller?.currentPosition?.coerceAtLeast(0) ?: 0L

    private fun publish(p: Player) {
        val item = p.currentMediaItem
        val meta = item?.mediaMetadata
        val nextIndex = p.nextMediaItemIndex
        _state.value = PlayerState(
            hasMedia = item != null,
            mediaId = item?.mediaId,
            title = meta?.title?.toString().orEmpty(),
            artist = meta?.artist?.toString().orEmpty(),
            album = meta?.albumTitle?.toString(),
            isPlaying = p.isPlaying,
            durationMs = p.duration.takeIf { it != C.TIME_UNSET && it > 0 } ?: 0L,
            shuffle = p.shuffleModeEnabled,
            repeatMode = p.repeatMode,
            nextTitle = if (nextIndex != C.INDEX_UNSET) {
                p.getMediaItemAt(nextIndex).mediaMetadata.title?.toString()
            } else null,
        )
    }
}

private fun Song.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setMediaId(mediaId)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .build()
        )
        .build()
