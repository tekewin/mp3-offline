package com.tekewin.mp3offline.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.tekewin.mp3offline.data.Song
import com.tekewin.mp3offline.playback.Artwork

@Composable
fun rememberArtwork(song: Song?, maxPx: Int): ImageBitmap? {
    val context = LocalContext.current
    val state: State<ImageBitmap?> = produceState<ImageBitmap?>(initialValue = null, song?.uri, maxPx) {
        value = song?.let { Artwork.load(context, it.uri, maxPx) }
    }
    return state.value
}
