package com.tekewin.mp3offline.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tekewin.mp3offline.MainViewModel
import com.tekewin.mp3offline.R
import com.tekewin.mp3offline.Screen
import com.tekewin.mp3offline.data.Playlist
import com.tekewin.mp3offline.data.Song
import com.tekewin.mp3offline.ui.components.AddToPlaylistSheet
import com.tekewin.mp3offline.ui.components.AppearanceDialog
import com.tekewin.mp3offline.ui.components.DeletePlaylistDialog
import com.tekewin.mp3offline.ui.components.MiniPlayer
import com.tekewin.mp3offline.ui.components.PlaylistNameDialog
import com.tekewin.mp3offline.ui.screens.ArtistScreen
import com.tekewin.mp3offline.ui.screens.LibraryScreen
import com.tekewin.mp3offline.ui.screens.NowPlayingScreen
import com.tekewin.mp3offline.ui.screens.PermissionScreen
import com.tekewin.mp3offline.ui.screens.PlaylistScreen
import com.tekewin.mp3offline.ui.screens.SearchScreen

/** Callbacks that open the app-wide sheets and dialogs. */
class UiActions(
    val addToPlaylist: (Song) -> Unit,
    val newPlaylist: () -> Unit,
    val renamePlaylist: (Playlist) -> Unit,
    val deletePlaylist: (Playlist) -> Unit,
)

@Composable
fun MP3OfflineApp(vm: MainViewModel) {
    val hasPermission by vm.hasPermission.collectAsStateWithLifecycle()
    // Re-check when returning from Android Settings.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.checkPermission() }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        if (hasPermission) MainContent(vm) else PermissionScreen(onResult = vm::checkPermission)
    }
}

@Composable
private fun MainContent(vm: MainViewModel) {
    val library by vm.library.collectAsStateWithLifecycle()
    val playlists by vm.playlists.collectAsStateWithLifecycle()
    val player by vm.player.collectAsStateWithLifecycle()
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(vm) { vm.messages.collect { snackbar.showSnackbar(it) } }

    var addTarget by remember { mutableStateOf<Song?>(null) }
    var creating by remember { mutableStateOf(false) }
    var createWithSong by remember { mutableStateOf<Song?>(null) }
    var renaming by remember { mutableStateOf<Playlist?>(null) }
    var deleting by remember { mutableStateOf<Playlist?>(null) }
    var showAppearance by remember { mutableStateOf(false) }

    val actions = remember {
        UiActions(
            addToPlaylist = { addTarget = it },
            newPlaylist = { createWithSong = null; creating = true },
            renamePlaylist = { renaming = it },
            deletePlaylist = { deleting = it },
        )
    }

    val screen = vm.backStack.last()
    BackHandler(enabled = vm.backStack.size > 1) { vm.back() }

    // Leave a detail page whose artist or playlist no longer exists.
    LaunchedEffect(screen, library, playlists, player.hasMedia) {
        val gone = when (screen) {
            is Screen.ArtistDetail -> !library.loading && library.artists.none { it.name == screen.name }
            is Screen.PlaylistDetail -> playlists.none { it.id == screen.id }
            Screen.NowPlaying -> !player.hasMedia
            else -> false
        }
        if (gone) vm.back()
    }

    val currentSong = player.mediaId?.let { library.byMediaId[it] }
    val artistsOf: (Playlist) -> List<String> = { p ->
        vm.songsOf(p, library.byKey).map { it.artist }.distinct().take(4)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (player.hasMedia && screen != Screen.NowPlaying) {
                MiniPlayer(
                    state = player,
                    song = currentSong,
                    positionMs = vm.playback::positionMs,
                    onOpen = { vm.open(Screen.NowPlaying) },
                    onPlayPause = vm.playback::togglePlayPause,
                    onNext = vm.playback::next,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "screen",
            ) { target ->
                when (target) {
                    Screen.Library -> LibraryScreen(
                        vm = vm,
                        library = library,
                        playlists = playlists,
                        currentMediaId = player.mediaId,
                        artistsOf = artistsOf,
                        actions = actions,
                        onAppearance = { showAppearance = true },
                    )
                    is Screen.ArtistDetail -> ArtistScreen(
                        vm = vm,
                        artist = library.artists.firstOrNull { it.name == target.name },
                        currentMediaId = player.mediaId,
                        actions = actions,
                    )
                    is Screen.PlaylistDetail -> PlaylistScreen(
                        vm = vm,
                        playlist = playlists.firstOrNull { it.id == target.id },
                        byKey = library.byKey,
                        currentMediaId = player.mediaId,
                        actions = actions,
                    )
                    Screen.NowPlaying -> NowPlayingScreen(
                        vm = vm,
                        state = player,
                        song = currentSong,
                        actions = actions,
                    )
                    Screen.Search -> SearchScreen(
                        vm = vm,
                        library = library,
                        currentMediaId = player.mediaId,
                        actions = actions,
                    )
                }
            }
        }
    }

    addTarget?.let { song ->
        AddToPlaylistSheet(
            song = song,
            playlists = playlists,
            artistsOf = artistsOf,
            onToggle = { p, include -> vm.setInPlaylist(p.id, song, include) },
            onNewPlaylist = {
                createWithSong = song
                creating = true
            },
            onDismiss = { addTarget = null },
        )
    }

    if (creating) {
        PlaylistNameDialog(
            title = stringResource(R.string.new_playlist),
            initialName = "",
            confirmLabel = stringResource(R.string.create),
            onConfirm = { name ->
                vm.createPlaylist(name, createWithSong)
                creating = false
                createWithSong = null
            },
            onDismiss = {
                creating = false
                createWithSong = null
            },
        )
    }

    renaming?.let { p ->
        PlaylistNameDialog(
            title = stringResource(R.string.rename_playlist),
            initialName = p.name,
            confirmLabel = stringResource(R.string.save),
            onConfirm = { name ->
                vm.renamePlaylist(p.id, name)
                renaming = null
            },
            onDismiss = { renaming = null },
        )
    }

    deleting?.let { p ->
        DeletePlaylistDialog(
            name = p.name,
            onConfirm = {
                vm.deletePlaylist(p.id)
                deleting = null
            },
            onDismiss = { deleting = null },
        )
    }

    if (showAppearance) {
        AppearanceDialog(
            current = themeMode,
            onSelect = vm::setThemeMode,
            onDismiss = { showAppearance = false },
        )
    }
}
