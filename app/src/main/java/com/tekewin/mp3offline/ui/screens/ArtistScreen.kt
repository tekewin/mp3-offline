package com.tekewin.mp3offline.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tekewin.mp3offline.MainViewModel
import com.tekewin.mp3offline.R
import com.tekewin.mp3offline.data.Artist
import com.tekewin.mp3offline.ui.AppIcons
import com.tekewin.mp3offline.ui.UiActions
import com.tekewin.mp3offline.ui.components.InitialsTile
import com.tekewin.mp3offline.ui.components.MenuItem
import com.tekewin.mp3offline.ui.components.SongRow
import com.tekewin.mp3offline.ui.components.songsAndLength
import com.tekewin.mp3offline.ui.theme.AppTheme

@Composable
fun ArtistScreen(
    vm: MainViewModel,
    artist: Artist?,
    currentMediaId: String?,
    actions: UiActions,
) {
    // Gone (deleted, or its files removed): App pops this screen.
    if (artist == null) return
    val extra = AppTheme.extra

    Column(Modifier.fillMaxSize()) {
        DetailTopBar(onBack = { vm.back() })
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            item(key = "hero") {
                Row(
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    InitialsTile(artist.name, 104.dp, corner = 28.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            artist.name,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.semantics { heading() },
                        )
                        Text(
                            songsAndLength(artist.songs.size, artist.durationMs),
                            style = MaterialTheme.typography.bodyMedium,
                            color = extra.textTertiary,
                        )
                    }
                }
                PlayShuffleButtons(
                    playLabel = stringResource(R.string.play_all),
                    enabled = artist.songs.isNotEmpty(),
                    onPlay = { vm.play(artist.songs, from = artist.name) },
                    onShuffle = { vm.play(artist.songs, shuffle = true, from = artist.name) },
                )
                Text(
                    stringResource(R.string.songs_az).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = extra.textTertiary,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 6.dp),
                )
            }
            itemsIndexed(artist.songs, key = { _, s -> s.id }) { index, song ->
                SongRow(
                    title = song.title,
                    subtitle = song.album ?: stringResource(R.string.unknown_album),
                    durationMs = song.durationMs,
                    isCurrent = song.mediaId == currentMediaId,
                    onClick = { vm.play(artist.songs, startIndex = index, from = artist.name) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                ) { dismiss ->
                    MenuItem(stringResource(R.string.play_only_this), AppIcons.Play, {
                        dismiss(); vm.play(listOf(song), from = artist.name)
                    })
                    MenuItem(stringResource(R.string.add_to_playlist), AppIcons.PlaylistAdd, {
                        dismiss(); actions.addToPlaylist(song)
                    })
                }
            }
        }
    }
}
