# MP3 Offline

A private, offline MP3 player for Android phones and tablets.

- Finds MP3s in **Internal storage › Music** (sub-folders included)
- Library grouped by **artist**, songs sorted **A–Z by title**
- Play all songs by an artist (in order or shuffled), or a single song
- **Playlists**: create, name, rename, delete, add/remove songs. Songs whose files are gone are removed from playlists automatically (a short notice says how many)
- Background playback with notification, lock-screen and Bluetooth/headset controls
- Light and dark themes (follows the system, or choose in **Appearance**)
- No internet permission, no analytics, no accounts, no ads

## Build and run

1. Install the latest **Android Studio**.
2. **File › Open…** and pick this folder. Let Gradle sync (Studio downloads the SDK and libraries the first time, which needs internet on your PC; the app itself never does).
3. If Studio offers to upgrade the Android Gradle Plugin or libraries, accept: the versions here are known-good, not necessarily the newest.
4. Plug in your phone with USB debugging on (or start an emulator) and press **Run**.
5. Copy some `.mp3` files into the phone's `Music` folder, open the app and allow access to music.

To test on an emulator, drag MP3 files onto the emulator window. They land in `Download`, so move them into `Music` with the Files app.

## Project layout

```
app/src/main/java/com/tekewin/mp3offline/
├── MainActivity.kt          Edge-to-edge activity, theme switch, player connection
├── MainViewModel.kt         Library state, navigation, playlist actions, auto-prune
├── data/
│   ├── MusicRepository.kt   MediaStore query for MP3s in Music/, grouped by artist
│   ├── PlaylistStore.kt     Playlists as JSON in app-private storage (atomic writes)
│   └── SettingsStore.kt     Light / dark / system preference
├── playback/
│   ├── PlaybackService.kt   Media3 ExoPlayer + MediaSessionService (background play)
│   ├── PlaybackConnection.kt MediaController wrapper exposing a StateFlow to Compose
│   └── Artwork.kt           Embedded cover art (ID3) loader with an in-memory cache
└── ui/                      Jetpack Compose + Material 3 screens matching the design
```

## Design decisions

- **Playlists store file paths, not database ids.** Android's MediaStore can hand out new ids after a rescan or OS update; the file's path (`Music/Artist/Song.mp3`) is stable. If a path disappears, that entry is removed once a later scan, at least 5 minutes after the first miss, still can't find it. The first-missed time is saved with the playlists, so this works across app restarts. Moving or renaming a file counts as removing it.
- **Safety net:** if a scan finds no MP3s at all, playlists are left alone, since that's more likely a storage hiccup than every file being deleted. The two-scan, 5-minute rule above covers the partial case: while Android re-indexes (e.g. after a reboot) it can briefly list only some songs.
- **The playback service only plays MediaStore audio.** Other apps (and the system) can control playback, but they can't make the player open arbitrary files or URLs.
- **Nothing is backed up or shared.** `allowBackup` is off and backup/transfer rules exclude everything.
- **The network permission is explicitly removed** from the merged manifest, so no library can add it.
- Requirements: Android 8.0 (API 26) or newer. Targets Android 16 (API 36).

## Publishing to Google Play (when you're ready)

1. Change `applicationId` in `app/build.gradle.kts` if you want a different package name. It can't be changed after the first upload.
2. **Build › Generate Signed App Bundle** and create an upload key. Keep the `.jks` file and passwords safe and out of git (`.gitignore` already excludes them).
3. In Play Console, create the app, enable **Play App Signing**, and upload the `.aab`.
4. **Data safety form:** the app collects and shares no data.
5. **Privacy policy:** Play asks for a URL. `PRIVACY.md` is a starting point you can host (a GitHub Pages or Gist link works).
6. Screenshots: phone screenshots in light and dark mode.

Bump `versionCode` for every upload.
