package com.tekewin.mp3offline.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tekewin.mp3offline.MainViewModel
import com.tekewin.mp3offline.R
import com.tekewin.mp3offline.data.Playlist
import com.tekewin.mp3offline.data.Song
import com.tekewin.mp3offline.ui.AppIcons
import com.tekewin.mp3offline.ui.UiActions
import com.tekewin.mp3offline.ui.components.MenuItem
import com.tekewin.mp3offline.ui.components.PlaylistMosaic
import com.tekewin.mp3offline.ui.components.RoundIconButton
import com.tekewin.mp3offline.ui.components.SongRow
import com.tekewin.mp3offline.ui.components.songsAndLength
import com.tekewin.mp3offline.ui.theme.AppTheme

@Composable
fun PlaylistScreen(
    vm: MainViewModel,
    playlist: Playlist?,
    byKey: Map<String, Song>,
    currentMediaId: String?,
    actions: UiActions,
) {
    // Gone (deleted, or its files removed): App pops this screen.
    if (playlist == null) return
    val extra = AppTheme.extra
    val songs = vm.songsOf(playlist, byKey)
    var menuOpen by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        DetailTopBar(onBack = { vm.back() }) {
            Box {
                RoundIconButton(
                    AppIcons.More, stringResource(R.string.playlist_options), { menuOpen = true },
                    container = MaterialTheme.colorScheme.surfaceVariant, iconSize = 20.dp,
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    MenuItem(stringResource(R.string.rename), AppIcons.Pencil, {
                        menuOpen = false; actions.renamePlaylist(playlist)
                    })
                    MenuItem(
                        stringResource(R.string.delete_playlist), AppIcons.Trash,
                        { menuOpen = false; actions.deletePlaylist(playlist) },
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            item(key = "hero") {
                Row(
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    PlaylistMosaic(songs.map { it.artist }.distinct().take(4), 104.dp, corner = 28.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            stringResource(R.string.playlist_label).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = extra.textTertiary,
                        )
                        Text(
                            playlist.name,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.semantics { heading() },
                        )
                        Text(
                            songsAndLength(songs.size, songs.sumOf { it.durationMs }),
                            style = MaterialTheme.typography.bodyMedium,
                            color = extra.textTertiary,
                        )
                    }
                }
                PlayShuffleButtons(
                    playLabel = stringResource(R.string.play),
                    enabled = songs.isNotEmpty(),
                    onPlay = { vm.play(songs, from = playlist.name) },
                    onShuffle = { vm.play(songs, shuffle = true, from = playlist.name) },
                )
                if (songs.isEmpty()) {
                    Text(
                        stringResource(R.string.empty_playlist),
                        style = MaterialTheme.typography.bodyMedium,
                        color = extra.textTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 24.dp),
                    )
                }
            }
            itemsIndexed(songs, key = { _, s -> s.key }) { index, song ->
                SongRow(
                    title = song.title,
                    subtitle = song.artist,
                    durationMs = song.durationMs,
                    isCurrent = song.mediaId == currentMediaId,
                    tileName = song.artist,
                    onClick = { vm.play(songs, startIndex = index, from = playlist.name) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                ) { dismiss ->
                    MenuItem(stringResource(R.string.add_to_playlist), AppIcons.PlaylistAdd, {
                        dismiss(); actions.addToPlaylist(song)
                    })
                    MenuItem(stringResource(R.string.remove_from_playlist), AppIcons.Remove, {
                        dismiss(); vm.setInPlaylist(playlist.id, song, include = false)
                    })
                }
            }
        }
    }
}
