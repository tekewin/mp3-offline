package com.tekewin.mp3offline.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tekewin.mp3offline.LibraryState
import com.tekewin.mp3offline.MainViewModel
import com.tekewin.mp3offline.R
import com.tekewin.mp3offline.Screen
import com.tekewin.mp3offline.data.MusicRepository
import com.tekewin.mp3offline.ui.AppIcons
import com.tekewin.mp3offline.ui.UiActions
import com.tekewin.mp3offline.ui.components.InitialsTile
import com.tekewin.mp3offline.ui.components.MenuItem
import com.tekewin.mp3offline.ui.components.RoundIconButton
import com.tekewin.mp3offline.ui.components.SongRow
import com.tekewin.mp3offline.ui.components.songCount
import com.tekewin.mp3offline.ui.theme.AppTheme

@Composable
fun SearchScreen(
    vm: MainViewModel,
    library: LibraryState,
    currentMediaId: String?,
    actions: UiActions,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { focus.requestFocus() }

    val q = query.trim()
    val artists = remember(q, library) {
        if (q.isEmpty()) emptyList() else library.artists.filter { it.name.contains(q, ignoreCase = true) }.take(6)
    }
    val songs = remember(q, library) {
        if (q.isEmpty()) emptyList() else {
            val collator = MusicRepository.collator()
            library.songs
                .filter {
                    it.title.contains(q, true) || it.artist.contains(q, true) || it.album?.contains(q, true) == true
                }
                .sortedWith { a, b -> collator.compare(a.title, b.title) }
                .take(200)
        }
    }
    val extra = AppTheme.extra
    val searchLabel = stringResource(R.string.search)

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            RoundIconButton(AppIcons.Back, stringResource(R.string.back), { vm.back() }, iconSize = 24.dp)
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(26.dp),
                leadingIcon = { Icon(AppIcons.Search, null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        RoundIconButton(AppIcons.Close, stringResource(R.string.clear_search), { query = "" }, iconSize = 20.dp)
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focus),
            )
        }

        LazyColumn(contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp)) {
            if (artists.isNotEmpty()) {
                item(key = "artists-header") { SectionHeader(stringResource(R.string.artists)) }
                items(artists, key = { "artist:" + it.name }) { artist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { vm.open(Screen.ArtistDetail(artist.name)) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        InitialsTile(artist.name, 44.dp)
                        Column(Modifier.weight(1f)) {
                            Text(artist.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(songCount(artist.songs.size), style = MaterialTheme.typography.bodySmall, color = extra.textTertiary)
                        }
                    }
                }
            }
            if (songs.isNotEmpty()) {
                item(key = "songs-header") { SectionHeader(stringResource(R.string.songs)) }
                itemsIndexed(songs, key = { _, s -> "song:" + s.id }) { index, song ->
                    SongRow(
                        title = song.title,
                        subtitle = song.artist,
                        durationMs = song.durationMs,
                        isCurrent = song.mediaId == currentMediaId,
                        tileName = song.artist,
                        onClick = { vm.play(songs, startIndex = index, from = searchLabel) },
                    ) { dismiss ->
                        MenuItem(stringResource(R.string.play_only_this), AppIcons.Play, {
                            dismiss(); vm.play(listOf(song), from = song.artist)
                        })
                        MenuItem(stringResource(R.string.add_to_playlist), AppIcons.PlaylistAdd, {
                            dismiss(); actions.addToPlaylist(song)
                        })
                    }
                }
            }
            if (q.isNotEmpty() && artists.isEmpty() && songs.isEmpty()) {
                item(key = "none") {
                    Text(
                        stringResource(R.string.no_results, q),
                        style = MaterialTheme.typography.bodyMedium,
                        color = extra.textTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = AppTheme.extra.textTertiary,
        modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 6.dp),
    )
}
