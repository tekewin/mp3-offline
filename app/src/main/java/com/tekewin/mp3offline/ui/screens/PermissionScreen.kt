package com.tekewin.mp3offline.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.tekewin.mp3offline.MainViewModel
import com.tekewin.mp3offline.R
import com.tekewin.mp3offline.ui.AppIcons
import com.tekewin.mp3offline.ui.theme.AppTheme

@Composable
fun PermissionScreen(onResult: () -> Unit) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val permission = MainViewModel.audioPermission()
    var blocked by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        // Denied and Android won't show the prompt again: send the user to Settings instead.
        blocked = !granted && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        onResult()
    }

    val folder = stringResource(R.string.music_folder_path)
    val body = stringResource(R.string.permission_body, folder)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val annotated = buildAnnotatedString {
        append(body)
        val start = body.indexOf(folder)
        if (start >= 0) addStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = onSurface), start, start + folder.length)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 28.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically),
        ) {
            Box(Modifier.size(132.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(132.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
                Box(
                    Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AppIcons.Music, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(40.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.permission_title),
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp, lineHeight = 40.sp),
                )
                Text(annotated, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Promise(AppIcons.NoWifi, stringResource(R.string.promise_offline))
                Promise(AppIcons.Shield, stringResource(R.string.promise_no_tracking))
                Promise(AppIcons.PlaylistAdd, stringResource(R.string.promise_local_playlists))
            }

            if (blocked) {
                Text(
                    stringResource(R.string.permission_blocked),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Button(
            onClick = {
                if (blocked) {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        )
                    )
                } else {
                    launcher.launch(permission)
                }
            },
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
        ) {
            Text(stringResource(if (blocked) R.string.open_settings else R.string.allow_access))
        }
        Text(
            stringResource(R.string.permission_footnote),
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.extra.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}

@Composable
private fun Promise(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
        }
        Text(text, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp), modifier = Modifier.weight(1f))
    }
}
