MP3 Offline: build status

Updated 2026-10-02

Where things are
Design canvas (Claude Design artifact): "Offline MP3 Player", 8 light screens + 6 dark.
Source: C:\Users\User\dev\mp3_offline on Keith's PC (Android Studio project).
App name: MP3 Offline. Package / applicationId: com.tekewin.mp3offline.
Stack

Kotlin 2.2.0, AGP 8.10.1, Gradle 8.14.3, compileSdk/targetSdk 36, minSdk 26, Jetpack Compose (BOM 2025.06.00) + Material 3, Media3 1.6.1 (ExoPlayer + MediaSessionService), lifecycle 2.9.1. No Room/KSP/navigation/icon libraries: playlists are JSON in app-private storage (AtomicFile), navigation is a small back stack in the ViewModel, icons are custom ImageVectors.

Key decisions
Library = MediaStore query, RELATIVE_PATH Music/% (sub-folders included), MP3 only. Grouped by artist, songs A–Z by title.
Playlists store the file path (Music/…/x.mp3), not the MediaStore id. Missing paths are pruned automatically, with a snackbar saying how many, but only once two scans at least 5 minutes apart have both missed them (first-missed times persist in playlists.json as "missingSince"). Pruning is skipped entirely if a scan returns zero songs.
No INTERNET permission (explicitly removed with tools:node="remove"), backups disabled, and the session callback only plays MediaStore audio ids.
Theme: System / Light / Dark, set in Appearance.

Status
The first full version compiles: `gradlew assembleDebug assembleRelease` succeeds (no Kotlin warnings; R8/minified release builds). Toolchain on Keith's PC: Temurin JDK 21 (JAVA_HOME) and a headless Android SDK at %LOCALAPPDATA%\Android\Sdk (ANDROID_HOME; platform 36, build-tools 35/36, platform-tools). Android Studio can reuse that SDK.

Fixed after first compile: PlaybackConnection now drops queued controller actions on release, so a tap made while connecting can't start playback the next time the app opens.

Lint: only "newer version available" notes plus two non-issues (the exported MediaSessionService is required; mipmap-anydpi-v26 must stay, since aapt can't find the adaptive icon without the -v26 folder).

Emulator-tested (AVD "mp3test", Pixel 7, Android 16 / API 36), debug and the R8-minified release, no crashes: permission flow; library scan (Music/ sub-folders included, Download/ excluded, untagged file → "Unknown artist", listed last); case-insensitive A–Z; play from artist page; embedded cover art; Now Playing; create playlist from the add sheet; deleting a file prunes it from the playlist with a snackbar (re-tested with the two-scan rule: kept and marked on first miss, kept on a rescan within 5 minutes, pruned after, un-marked if the file comes back); Dark theme incl. system bar icons; background playback with media notification.

Not yet tested: a real phone, Bluetooth/headset buttons, large libraries, search, rename/delete playlist.