package com.tekewin.mp3offline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tekewin.mp3offline.R
import com.tekewin.mp3offline.ui.theme.AppTheme

/** Stable color for a name: the same artist always gets the same tile color. */
@Composable
fun tileColors(name: String): Pair<Color, Color> {
    val tiles = AppTheme.extra.tiles
    return tiles[Math.floorMod(name.hashCode(), tiles.size)]
}

fun initialsOf(name: String): String {
    val words = name.split(' ', '-', '_', '&', '.', ',')
        .filter { it.isNotBlank() && it.first().isLetterOrDigit() }
    val significant = if (words.size > 1 && words.first().equals("the", ignoreCase = true)) words.drop(1) else words
    return when {
        significant.isEmpty() -> "♪"
        significant.size == 1 -> significant[0].take(2).replaceFirstChar { it.uppercase() }
        else -> "${significant[0].first()}${significant[1].first()}".uppercase()
    }
}

/** Colored rounded square with an artist's initials, used wherever there is no cover art. */
@Composable
fun InitialsTile(
    name: String,
    size: Dp,
    modifier: Modifier = Modifier,
    corner: Dp = size * 0.29f,
    fontSize: TextUnit = (size.value * 0.36f).sp,
) {
    val (bg, fg) = tileColors(name)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initialsOf(name),
            color = fg,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

/** 2×2 mosaic of artist colors for a playlist. */
@Composable
fun PlaylistMosaic(artistNames: List<String>, size: Dp, modifier: Modifier = Modifier, corner: Dp = size * 0.27f) {
    val names = when {
        artistNames.isEmpty() -> listOf("", "", "", "")
        else -> List(4) { artistNames[it % artistNames.size] }
    }
    val gap = 2.dp
    Column(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(corner)),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        for (row in 0..1) {
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                for (col in 0..1) {
                    val name = names[row * 2 + col]
                    val color = if (name.isEmpty()) MaterialTheme.colorScheme.surfaceVariant else tileColors(name).first
                    Spacer(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(color)
                    )
                }
            }
        }
    }
}

/** 44dp round icon button with an optional filled background. */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = Color.Transparent,
    content: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(size),
        colors = IconButtonDefaults.iconButtonColors(containerColor = container, contentColor = content),
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(iconSize))
    }
}

fun formatDuration(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

/** "12 songs · 47 min" */
@Composable
fun songsAndLength(count: Int, totalMs: Long): String {
    val res = LocalContext.current.resources
    val songs = res.getQuantityString(R.plurals.song_count, count, count)
    if (totalMs <= 0) return songs
    val minutes = ((totalMs + 30_000) / 60_000).toInt()
    val length = if (minutes >= 60) {
        res.getString(R.string.length_hours, minutes / 60, minutes % 60)
    } else {
        res.getString(R.string.length_minutes, minutes)
    }
    return "$songs · $length"
}

@Composable
fun songCount(count: Int): String =
    LocalContext.current.resources.getQuantityString(R.plurals.song_count, count, count)
