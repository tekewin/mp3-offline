package com.tekewin.mp3offline.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Colors from the design that Material's ColorScheme has no slot for. */
@Immutable
data class ExtraColors(
    val textTertiary: Color,
    val player: Color,
    val onPlayer: Color,
    val onPlayerVariant: Color,
    val playerButton: Color,
    val onPlayerButton: Color,
    val progress: Color,
    val track: Color,
    val rowActive: Color,
    /** Artist tile colors: background to foreground pairs. */
    val tiles: List<Pair<Color, Color>>,
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF4F3BD6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5E0FF),
    onPrimaryContainer = Color(0xFF2B1D9C),
    secondary = Color(0xFF4F3BD6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5E0FF),
    onSecondaryContainer = Color(0xFF2B1D9C),
    background = Color(0xFFF4F5F8),
    onBackground = Color(0xFF12141A),
    surface = Color(0xFFF4F5F8),
    onSurface = Color(0xFF12141A),
    surfaceVariant = Color(0xFFE9EBF1),
    onSurfaceVariant = Color(0xFF424959),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color(0xFFE9EBF1),
    outline = Color(0xFF5B6273),
    outlineVariant = Color(0xFFDCDFE6),
    error = Color(0xFFB3261E),
    onError = Color.White,
    inverseSurface = Color(0xFF2E3038),
    inverseOnSurface = Color.White,
    inversePrimary = Color(0xFFA99BFF),
    scrim = Color(0xFF101218),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFA99BFF),
    onPrimary = Color(0xFF15103A),
    primaryContainer = Color(0xFF2C2659),
    onPrimaryContainer = Color(0xFFDCD5FF),
    secondary = Color(0xFFA99BFF),
    onSecondary = Color(0xFF15103A),
    secondaryContainer = Color(0xFF2C2659),
    onSecondaryContainer = Color(0xFFDCD5FF),
    background = Color(0xFF0E0F13),
    onBackground = Color(0xFFF1F2F6),
    surface = Color(0xFF0E0F13),
    onSurface = Color(0xFFF1F2F6),
    surfaceVariant = Color(0xFF23262E),
    onSurfaceVariant = Color(0xFFC0C5D1),
    surfaceContainerLowest = Color(0xFF14161B),
    surfaceContainerLow = Color(0xFF1B1D24),
    surfaceContainer = Color(0xFF1B1D24),
    surfaceContainerHigh = Color(0xFF1F2128),
    surfaceContainerHighest = Color(0xFF23262E),
    outline = Color(0xFF979DAC),
    outlineVariant = Color(0xFF2C2F38),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    inverseSurface = Color(0xFFE4E6EC),
    inverseOnSurface = Color(0xFF15171D),
    inversePrimary = Color(0xFF4F3BD6),
    scrim = Color.Black,
)

private val LightExtra = ExtraColors(
    textTertiary = Color(0xFF5B6273),
    player = Color(0xFF1A1C23),
    onPlayer = Color.White,
    onPlayerVariant = Color(0xFFB9BECC),
    playerButton = Color.White,
    onPlayerButton = Color(0xFF1A1C23),
    progress = Color(0xFF9D8CFF),
    track = Color(0xFFD5D8E0),
    rowActive = Color(0xFFECE9FF),
    tiles = listOf(
        Color(0xFFFFD9C2) to Color(0xFF7A3410),
        Color(0xFFCDE8D6) to Color(0xFF1F5B36),
        Color(0xFFD6DEFF) to Color(0xFF2A3A8F),
        Color(0xFFFBD3E4) to Color(0xFF86204F),
        Color(0xFFFFE9A8) to Color(0xFF6B4E00),
        Color(0xFFC9E6EE) to Color(0xFF155463),
        Color(0xFFE3D7FF) to Color(0xFF4A2A9A),
        Color(0xFFDCE5C4) to Color(0xFF3F4E14),
    ),
)

private val DarkExtra = ExtraColors(
    textTertiary = Color(0xFF979DAC),
    player = Color(0xFF262933),
    onPlayer = Color(0xFFF1F2F6),
    onPlayerVariant = Color(0xFFAEB4C2),
    playerButton = Color(0xFFA99BFF),
    onPlayerButton = Color(0xFF15103A),
    progress = Color(0xFFA99BFF),
    track = Color(0xFF3A3E49),
    rowActive = Color(0xFF1F1C38),
    tiles = listOf(
        Color(0xFF4A2614) to Color(0xFFFFC6A6),
        Color(0xFF183A26) to Color(0xFFA9DDBB),
        Color(0xFF1E2756) to Color(0xFFBCC8FF),
        Color(0xFF4D1A33) to Color(0xFFF6B7D2),
        Color(0xFF3F3208) to Color(0xFFF5D77A),
        Color(0xFF123943) to Color(0xFFA6DBE7),
        Color(0xFF2E2160) to Color(0xFFD3C4FF),
        Color(0xFF2C3510) to Color(0xFFCCDB9F),
    ),
)

val LocalExtraColors = staticCompositionLocalOf { LightExtra }

private val base = Typography()

private val AppTypography = Typography(
    headlineLarge = base.headlineLarge.copy(fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
    headlineMedium = base.headlineMedium.copy(fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.4).sp),
    headlineSmall = base.headlineSmall.copy(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp),
    titleLarge = base.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontSize = 16.sp),
    bodyMedium = base.bodyMedium.copy(fontSize = 14.sp),
    bodySmall = base.bodySmall.copy(fontSize = 13.sp),
    labelLarge = base.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
    labelMedium = base.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp),
)

/** Small caps-style section label, e.g. "SONGS · A–Z". */
val SectionLabel: TextStyle @Composable get() = MaterialTheme.typography.labelMedium

@Composable
fun MP3OfflineTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalExtraColors provides if (darkTheme) DarkExtra else LightExtra) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = AppTypography,
            content = content,
        )
    }
}

object AppTheme {
    val extra: ExtraColors @Composable get() = LocalExtraColors.current
}
