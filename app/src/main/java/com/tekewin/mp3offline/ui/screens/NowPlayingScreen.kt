package com.tekewin.mp3offline.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.tekewin.mp3offline.MainViewModel
import com.tekewin.mp3offline.R
import com.tekewin.mp3offline.data.Song
import com.tekewin.mp3offline.playback.PlayerState
import com.tekewin.mp3offline.ui.AppIcons
import com.tekewin.mp3offline.ui.UiActions
import com.tekewin.mp3offline.ui.components.RoundIconButton
import com.tekewin.mp3offline.ui.components.formatDuration
import com.tekewin.mp3offline.ui.components.initialsOf
import com.tekewin.mp3offline.ui.components.rememberArtwork
import com.tekewin.mp3offline.ui.components.tileColors
import com.tekewin.mp3offline.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
fun NowPlayingScreen(
    vm: MainViewModel,
    state: PlayerState,
    song: Song?,
    actions: UiActions,
) {
    val extra = AppTheme.extra
    val playback = vm.playback

    var positionMs by remember { mutableLongStateOf(0L) }
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    LaunchedEffect(state.mediaId, state.isPlaying) {
        while (true) {
            positionMs = playback.positionMs()
            if (!state.isPlaying) break
            delay(500)
        }
    }
    val duration = state.durationMs
    val fraction = dragFraction ?: if (duration > 0) (positionMs.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val shownPosition = if (dragFraction != null) (dragFraction!! * duration).toLong() else positionMs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIconButton(AppIcons.ChevronDown, stringResource(R.string.close_player), { vm.back() }, iconSize = 26.dp)
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.playing_from).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = extra.textTertiary,
                )
                Text(
                    vm.playingFrom.ifEmpty { state.artist },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.size(44.dp))
        }

        // Cover art: the largest square that fits.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            LargeArt(song = song, fallbackName = state.artist, modifier = Modifier.widthIn(max = 460.dp).aspectRatio(1f))
        }

        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .padding(horizontal = 28.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(state.title, style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        listOfNotNull(state.artist.ifEmpty { null }, state.album).joinToString(" · "),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                RoundIconButton(
                    AppIcons.PlaylistAdd, stringResource(R.string.add_to_playlist),
                    { song?.let(actions.addToPlaylist) },
                    container = MaterialTheme.colorScheme.surfaceVariant, size = 48.dp,
                )
            }

            val seekLabel = stringResource(R.string.seek)
            Slider(
                value = fraction,
                onValueChange = { dragFraction = it },
                onValueChangeFinished = {
                    dragFraction?.let { f ->
                        val target = (f * duration).toLong()
                        playback.seekTo(target)
                        positionMs = target
                    }
                    dragFraction = null
                },
                enabled = duration > 0,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = extra.track,
                ),
                modifier = Modifier
                    .padding(top = 20.dp)
                    .semantics { contentDescription = seekLabel },
            )
            Row(Modifier.fillMaxWidth()) {
                Text(formatDuration(shownPosition), style = MaterialTheme.typography.bodySmall, color = extra.textTertiary)
                Spacer(Modifier.weight(1f))
                Text(formatDuration(duration), style = MaterialTheme.typography.bodySmall, color = extra.textTertiary)
            }

            // Transport controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ToggleIcon(
                    icon = AppIcons.Shuffle,
                    on = state.shuffle,
                    label = stringResource(if (state.shuffle) R.string.shuffle_on else R.string.shuffle_off),
                    onClick = playback::toggleShuffle,
                )
                RoundIconButton(AppIcons.Previous, stringResource(R.string.previous_song), playback::previous, size = 56.dp, iconSize = 30.dp)
                Surface(
                    onClick = playback::togglePlayPause,
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(80.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (state.isPlaying) AppIcons.Pause else AppIcons.Play,
                            contentDescription = stringResource(if (state.isPlaying) R.string.pause else R.string.play),
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
                RoundIconButton(AppIcons.Next, stringResource(R.string.next_song), playback::next, size = 56.dp, iconSize = 30.dp)
                ToggleIcon(
                    icon = if (state.repeatMode == Player.REPEAT_MODE_ONE) AppIcons.RepeatOne else AppIcons.Repeat,
                    on = state.repeatMode != Player.REPEAT_MODE_OFF,
                    label = stringResource(
                        when (state.repeatMode) {
                            Player.REPEAT_MODE_ALL -> R.string.repeat_all
                            Player.REPEAT_MODE_ONE -> R.string.repeat_one
                            else -> R.string.repeat_off
                        }
                    ),
                    onClick = playback::cycleRepeat,
                )
            }
        }

        Spacer(Modifier.weight(0.15f))

        val next = state.nextTitle
        if (next != null) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(R.string.up_next).uppercase(), style = MaterialTheme.typography.labelMedium, color = extra.textTertiary)
                    Text(next, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        } else {
            Spacer(Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ToggleIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, on: Boolean, label: String, onClick: () -> Unit) {
    RoundIconButton(
        icon = icon,
        contentDescription = label,
        onClick = onClick,
        size = 48.dp,
        container = if (on) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        content = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun LargeArt(song: Song?, fallbackName: String, modifier: Modifier = Modifier) {
    val art = rememberArtwork(song, maxPx = 900)
    val shape = RoundedCornerShape(32.dp)
    if (art != null) {
        Image(
            bitmap = art,
            contentDescription = stringResource(R.string.cover_art),
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape),
        )
    } else {
        val (bg, fg) = tileColors(fallbackName)
        Box(modifier.clip(shape).background(bg), contentAlignment = Alignment.Center) {
            Text(initialsOf(fallbackName), color = fg, fontSize = 84.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-2).sp)
        }
    }
}
