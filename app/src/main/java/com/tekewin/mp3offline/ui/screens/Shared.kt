package com.tekewin.mp3offline.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tekewin.mp3offline.R
import com.tekewin.mp3offline.ui.AppIcons
import com.tekewin.mp3offline.ui.components.RoundIconButton

/** Back button on the left, optional actions on the right. */
@Composable
fun DetailTopBar(onBack: () -> Unit, actions: @Composable () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundIconButton(AppIcons.Back, stringResource(R.string.back), onBack, iconSize = 24.dp)
        Spacer(Modifier.weight(1f))
        actions()
    }
}

/** "Play all" + "Shuffle" pair used on artist and playlist pages. */
@Composable
fun PlayShuffleButtons(playLabel: String, enabled: Boolean, onPlay: () -> Unit, onShuffle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onPlay,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
        ) { ButtonContent(AppIcons.Play, playLabel, 18) }
        FilledTonalButton(
            onClick = onShuffle,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
        ) { ButtonContent(AppIcons.Shuffle, stringResource(R.string.shuffle), 20) }
    }
}

@Composable
private fun ButtonContent(icon: ImageVector, label: String, iconDp: Int) {
    Icon(icon, null, modifier = Modifier.size(iconDp.dp))
    Spacer(Modifier.size(8.dp))
    Text(label)
}
