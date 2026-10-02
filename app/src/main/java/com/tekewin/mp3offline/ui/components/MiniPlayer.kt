package com.tekewin.mp3offline.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tekewin.mp3offline.R
import com.tekewin.mp3offline.data.Song
import com.tekewin.mp3offline.playback.PlayerState
import com.tekewin.mp3offline.ui.AppIcons
import com.tekewin.mp3offline.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
fun MiniPlayer(
    state: PlayerState,
    song: Song?,
    positionMs: () -> Long,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extra = AppTheme.extra
    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(state.mediaId, state.isPlaying, state.durationMs) {
        while (true) {
            progress = if (state.durationMs > 0) (positionMs().toFloat() / state.durationMs).coerceIn(0f, 1f) else 0f
            if (!state.isPlaying) break
            delay(1000)
        }
    }

    Surface(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = extra.player,
        contentColor = extra.onPlayer,
    ) {
        Box {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CoverArt(song = song, fallbackName = state.artist, size = 44.dp, corner = 12.dp, maxPx = 160)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(state.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        state.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = extra.onPlayerVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                RoundIconButton(
                    icon = if (state.isPlaying) AppIcons.Pause else AppIcons.Play,
                    contentDescription = stringResource(if (state.isPlaying) R.string.pause else R.string.play),
                    onClick = onPlayPause,
                    container = extra.playerButton,
                    content = extra.onPlayerButton,
                    iconSize = 18.dp,
                )
                RoundIconButton(
                    icon = AppIcons.Next,
                    contentDescription = stringResource(R.string.next_song),
                    onClick = onNext,
                    content = extra.onPlayer,
                )
            }
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(progress)
                    .height(3.dp)
                    .background(extra.progress)
            )
        }
    }
}

/** Embedded cover art if the MP3 has it, otherwise the artist's initials tile. */
@Composable
fun CoverArt(
    song: Song?,
    fallbackName: String,
    size: androidx.compose.ui.unit.Dp,
    corner: androidx.compose.ui.unit.Dp,
    maxPx: Int,
    modifier: Modifier = Modifier,
) {
    val art = rememberArtwork(song, maxPx)
    if (art != null) {
        Image(
            bitmap = art,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(corner)),
        )
    } else {
        InitialsTile(fallbackName, size, modifier, corner = corner)
    }
}
