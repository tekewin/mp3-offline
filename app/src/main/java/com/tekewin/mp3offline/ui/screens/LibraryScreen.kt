package com.tekewin.mp3offline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tekewin.mp3offline.LibraryState
import com.tekewin.mp3offline.LibraryTab
import com.tekewin.mp3offline.MainViewModel
import com.tekewin.mp3offline.R
import com.tekewin.mp3offline.Screen
import com.tekewin.mp3offline.data.Artist
import com.tekewin.mp3offline.data.Playlist
import com.tekewin.mp3offline.ui.AppIcons
import com.tekewin.mp3offline.ui.UiActions
import com.tekewin.mp3offline.ui.components.InitialsTile
import com.tekewin.mp3offline.ui.components.MenuItem
import com.tekewin.mp3offline.ui.components.PlaylistMosaic
import com.tekewin.mp3offline.ui.components.RoundIconButton
import com.tekewin.mp3offline.ui.components.songCount
import com.tekewin.mp3offline.ui.components.songsAndLength
import com.tekewin.mp3offline.ui.theme.AppTheme

@Composable
fun LibraryScreen(
    vm: MainViewModel,
    library: LibraryState,
    playlists: List<Playlist>,
    currentMediaId: String?,
    artistsOf: (Playlist) -> List<String>,
    actions: UiActions,
    onAppearance: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.library),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            RoundIconButton(
                AppIcons.Search, stringResource(R.string.search), { vm.open(Screen.Search) },
                container = MaterialTheme.colorScheme.surfaceVariant,
            )
            RoundIconButton(
                AppIcons.Appearance, stringResource(R.string.appearance), onAppearance,
                container = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        TabPill(
            selected = vm.libraryTab,
            onSelect = { vm.libraryTab = it },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        )

        when (vm.libraryTab) {
            LibraryTab.Artists -> ArtistsTab(vm, library, currentMediaId)
            LibraryTab.Playlists -> PlaylistsTab(vm, library, playlists, artistsOf, actions)
        }
    }
}

@Composable
private fun TabPill(selected: LibraryTab, onSelect: (LibraryTab) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LibraryTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .then(if (isSelected) Modifier.shadow(1.dp, RoundedCornerShape(50)) else Modifier)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent)
                    .selectable(selected = isSelected, onClick = { onSelect(tab) }, role = Role.Tab),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(if (tab == LibraryTab.Artists) R.string.artists else R.string.playlists),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ArtistsTab(vm: MainViewModel, library: LibraryState, currentMediaId: String?) {
    val extra = AppTheme.extra
    val res = LocalContext.current.resources
    Row(
        modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(AppIcons.Folder, null, tint = extra.textTertiary, modifier = Modifier.size(16.dp))
        Text(
            stringResource(
                R.string.library_summary,
                res.getQuantityString(R.plurals.artist_count, library.artists.size, library.artists.size),
                songCount(library.songs.size),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = extra.textTertiary,
            modifier = Modifier.weight(1f),
        )
        if (library.loading) {
            CircularProgressIndicator(Modifier.padding(12.dp).size(20.dp), strokeWidth = 2.dp)
        } else {
            RoundIconButton(
                AppIcons.Refresh, stringResource(R.string.rescan), { vm.refresh() },
                content = MaterialTheme.colorScheme.onSurfaceVariant, iconSize = 20.dp,
            )
        }
    }

    if (!library.loading && library.artists.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.empty_library_title),
            body = stringResource(R.string.empty_library_body),
            action = stringResource(R.string.rescan),
            onAction = { vm.refresh() },
        )
        return
    }

    LazyColumn(contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp)) {
        items(library.artists, key = { it.name }) { artist ->
            ArtistRow(
                artist = artist,
                isPlaying = currentMediaId != null && artist.songs.any { it.mediaId == currentMediaId },
                onOpen = { vm.open(Screen.ArtistDetail(artist.name)) },
                onPlayAll = { vm.play(artist.songs, from = artist.name) },
            )
        }
    }
}

@Composable
private fun ArtistRow(artist: Artist, isPlaying: Boolean, onOpen: () -> Unit, onPlayAll: () -> Unit) {
    val extra = AppTheme.extra
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 64.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onOpen)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            InitialsTile(artist.name, 48.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    artist.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(songCount(artist.songs.size), style = MaterialTheme.typography.bodySmall, color = extra.textTertiary)
            }
        }
        RoundIconButton(
            icon = AppIcons.Play,
            contentDescription = stringResource(R.string.play_all_by, artist.name),
            onClick = onPlayAll,
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
            iconSize = 18.dp,
        )
    }
}

@Composable
private fun PlaylistsTab(
    vm: MainViewModel,
    library: LibraryState,
    playlists: List<Playlist>,
    artistsOf: (Playlist) -> List<String>,
    actions: UiActions,
) {
    LazyColumn(contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp)) {
        item(key = "new") {
            FilledTonalButton(
                onClick = actions.newPlaylist,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 18.dp),
            ) {
                Icon(AppIcons.Plus, null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.size(12.dp))
                Text(stringResource(R.string.new_playlist), modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }
        if (playlists.isEmpty()) {
            item(key = "empty") {
                Text(
                    stringResource(R.string.empty_playlists),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.extra.textTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                )
            }
        }
        items(playlists, key = { it.id }) { p ->
            val songs = vm.songsOf(p, library.byKey)
            PlaylistRow(
                playlist = p,
                meta = songsAndLength(songs.size, songs.sumOf { it.durationMs }),
                artists = artistsOf(p),
                onOpen = { vm.open(Screen.PlaylistDetail(p.id)) },
                onRename = { actions.renamePlaylist(p) },
                onDelete = { actions.deletePlaylist(p) },
            )
        }
    }
}

@Composable
private fun PlaylistRow(
    playlist: Playlist,
    meta: String,
    artists: List<String>,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 76.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onOpen)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PlaylistMosaic(artists, 58.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(playlist.name, style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(meta, style = MaterialTheme.typography.bodySmall, color = AppTheme.extra.textTertiary)
            }
        }
        Box {
            RoundIconButton(
                AppIcons.More, stringResource(R.string.more_options_for, playlist.name), { menuOpen = true },
                content = MaterialTheme.colorScheme.onSurfaceVariant, iconSize = 20.dp,
            )
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                MenuItem(stringResource(R.string.rename), AppIcons.Pencil, { menuOpen = false; onRename() })
                MenuItem(
                    stringResource(R.string.delete_playlist), AppIcons.Trash, { menuOpen = false; onDelete() },
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
fun EmptyState(title: String, body: String, action: String? = null, onAction: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Icon(
                AppIcons.Music, null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(18.dp).size(32.dp),
            )
        }
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (action != null) {
            FilledTonalButton(onClick = onAction, modifier = Modifier.heightIn(min = 48.dp)) { Text(action) }
        }
    }
}
