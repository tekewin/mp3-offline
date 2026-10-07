# MP3 Offline

A private, offline MP3 player for Android phones and tablets.

## Publishing to Google Play (when you're ready)

1. Change `applicationId` in `app/build.gradle.kts` if you want a different package name. It can't be changed after the first upload.
2. **Build › Generate Signed App Bundle** and create an upload key. Keep the `.jks` file and passwords safe and out of git (`.gitignore` already excludes them).
3. In Play Console, create the app, enable **Play App Signing**, and upload the `.aab`.
4. **Data safety form:** the app collects and shares no data.
5. **Privacy policy:** Play asks for a URL. `PRIVACY.md` is a starting point you can host (a GitHub Pages or Gist link works).
6. Screenshots: phone screenshots in light and dark mode.

Bump `versionCode` for every upload.
