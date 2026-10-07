@echo off
rem Builds a signed release APK into dist\mp3-offline.apk.
rem Needs keystore.properties + mp3offline-release.jks in this folder (both git-ignored).
rem If the Android SDK is missing, offers to download the parts the build needs.
setlocal
cd /d "%~dp0"
if not exist keystore.properties (
  echo keystore.properties not found next to this script, so the APK can't be signed.
  goto :fail
)

rem ---- Java 17+ ----
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto :java_ok
for %%D in ("%ProgramFiles%\Android\Android Studio\jbr" "%LOCALAPPDATA%\Programs\Android Studio\jbr") do (
  if exist "%%~D\bin\java.exe" (
    set "JAVA_HOME=%%~D"
    goto :java_ok
  )
)
echo Could not find Java 17 or newer. Install a JDK (e.g. Eclipse Temurin 21) or set JAVA_HOME.
goto :fail
:java_ok
echo Using JAVA_HOME=%JAVA_HOME%

rem ---- Android SDK (path must match sdk.dir in local.properties) ----
set "SDK=%LOCALAPPDATA%\Android\Sdk"
set "ANDROID_HOME=%SDK%"
if exist "%SDK%\platforms\android-36\android.jar" if exist "%SDK%\build-tools\35.0.0" goto :sdk_ok
echo.
echo The Android SDK was not found at:
echo   %SDK%
echo This script can download the parts the build needs (about 300 MB) from Google:
echo command-line tools, Android 16 platform (API 36), build-tools 35.0.0, platform-tools.
echo Continuing also ACCEPTS the Android SDK License Agreement on your behalf.
echo Press any key to continue, or close this window to cancel.
pause >nul
if not exist "%SDK%\cmdline-tools" mkdir "%SDK%\cmdline-tools"
if exist "%SDK%\cmdline-tools\latest\bin\sdkmanager.bat" goto :have_sdkmanager
echo Downloading command-line tools...
curl -fL -o "%TEMP%\android-cmdline-tools.zip" https://dl.google.com/android/repository/commandlinetools-win-13114758_latest.zip || goto :fail
if exist "%SDK%\cmdline-tools\cmdline-tools" rmdir /s /q "%SDK%\cmdline-tools\cmdline-tools"
tar -xf "%TEMP%\android-cmdline-tools.zip" -C "%SDK%\cmdline-tools" || goto :fail
ren "%SDK%\cmdline-tools\cmdline-tools" latest || goto :fail
del "%TEMP%\android-cmdline-tools.zip"
:have_sdkmanager
set "SDKM=%SDK%\cmdline-tools\latest\bin\sdkmanager.bat"
echo Accepting SDK licenses...
(for /l %%i in (1,1,40) do @echo y) | "%SDKM%" --sdk_root="%SDK%" --licenses >nul
echo Installing SDK packages (this takes a few minutes)...
call "%SDKM%" --sdk_root="%SDK%" "platforms;android-36" "build-tools;35.0.0" "platform-tools" || goto :fail
:sdk_ok

rem ---- Build ----
echo.
echo Building the release APK...
call gradlew.bat assembleRelease || goto :fail
if not exist dist mkdir dist
copy /y "app\build\outputs\apk\release\app-release.apk" "dist\mp3-offline.apk" >nul || goto :fail
echo.
echo Done: dist\mp3-offline.apk
if "%~1"=="" pause
exit /b 0
:fail
echo.
echo BUILD FAILED - see the messages above.
if "%~1"=="" pause
exit /b 1
