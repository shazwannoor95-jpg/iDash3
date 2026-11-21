# Native BitChord UI in iDash — 0.8.0-rc2

The actual upstream BitChord Compose application is built as a library in iDash's process and displayed in the Drive panel beside JAKIM, media and date cards. It uses the original navigation, data sources, library, search, player and Media3 playback service. It does not use split-screen, an external BitChord APK or an embedded external Activity. The existing Map, FM and YouTube sources remain selectable.

This replaces rc1's small JioSaavn-only Java player. rc1 is retained on `feat/idash4-0.8.0-rc1-native-music` at 3ca2fd8; the pre-integration rc5 is tagged `backup/pre-native-music`. Work is isolated on `feat/idash4-0.8.0-rc2-full-bitchord-ui`; main must remain a96888d60533c1dc26003d88136a93d54f029f5d.

## Use

Open Drive, then choose B / BitChord on the left rail. Navigate the native Home, Search and Library tabs inside the right panel. The upstream library can use device audio with Android's relevant user-granted access. Online sources and account features retain their upstream connection/login requirements; no account is preconfigured. Source exit disposes the composition. Playback is managed by the upstream foreground service, so switching back to the map does not discard a playing queue. Pause/next/previous and metadata on the iDash left card use the same owned session.

## Head-unit profile and host changes

Minimum API 26; ARM32 native libraries included. User hardware reports API 27 (despite conflicting vendor Android-version labels), Mali-450 GLES2, RAM 1 GiB, 1024x600/mdpi. No root, Accessibility service or special Activity-embedding privileges are used. Native library load/draw performance on that physical head unit still needs a device check.

The module initializes only when first needed. R8 and resource shrinking use upstream reflection/JNI rules; the optimized payload has four DEX files instead of the unoptimized build's 24. Artwork cache memory is bounded at 12 MiB, artwork disk at 100 MiB, audio disk cache defaults to 256 MiB with a 128 MiB floor, and looping canvas video defaults off. The embedded root does not force a display refresh rate. Public active-user presence and standalone BitChord APK updating are disabled. Watch APK installation and standalone BitChord AppWidget providers/refresh are excluded. The integrated iDash media card supplies the player controls. SF Pro fonts/SF Symbols are replaced with Inter and Material vectors, retaining the native screen structure and controls.

The full music stack has a larger APK and memory footprint than the rc1 Java subset. The old 8 MB/ABI-neutral guard is replaced by explicit expected-native-ABI checks; this build intentionally includes codecs and ARM32 binaries. Debug emulator measurements are evidence of function, not a promised frame rate or RAM use on the real device.

## Build and corresponding source

See bitchord/PROVENANCE.md and third_party/source/README.md. Upstream commit is pinned, native dependencies have matching rebuild sources, and formerly JitPack-only dependencies are built from pinned public sources into a checked-in local Maven repository. Ordinary dependencies resolve from Google/Maven Central. Build uses JDK 21, AGP 9.3.2, Gradle 9.7.1, Kotlin 2.4.20, compile SDK 37; runtime minimum is unchanged at 26. No credentials or device identifiers are committed.

## Verification record

- `:app:assembleDebug`, `:app:assembleDebugAndroidTest`, 78 JVM unit tests and `:app:lintDebug` pass; lint reports zero errors.
- API27/x86_64, 1024x600: 13 full upstream UI assertions pass (Home/Search/Library, measured native viewport, JAKIM separation, composition disposal/reopen, retained host focus).
- 10 assertions exercise actual upstream Media3 service playback with explicitly labelled synthetic PCM: clock advance, duration, pause, seek, next, shared left-card metadata/session and continued playback when switching to Map. This is not a live stream test.
- 42 existing native Drive flow checks, 57 Home/UI smoke assertions and 17 App Library assertions pass: 139 Android assertions in total. Home/library checks cover the shared Activity/back-dispatch integration. A real API27 install round trip also passes: rc2 -> rc5 rollback -> rc2 again, without uninstalling, preserving all 17 pre-existing iDash preference keys.
- The ARM32 APK is 80,563,231 bytes, minimum API26, version code41. Its four DEX payloads are byte-for-byte identical to the tested x86_64/universal candidate. ARM32 native libraries are verified present; physical ARM execution remains untested here.
- The saved preview is an actual emulator screenshot, not an image-generation mockup. Its listening-history example is labelled `Native audio B / Test PCM`, generated solely by decoder tests. No real account is signed in.
- Earlier failed attempts and their causes are retained in the evidence: unbounded weighted-layout measurement, an unused AppWidget initializer blocking the service, and UI-automation window/label matching. Fixes and final pass logs are separate.

Live catalogue, Google/Spotify authentication, real online streaming and optional USB/Cast/addon integrations are not verified. Some host APIs return HTTP403; emulator DNS is unavailable. No fake online catalogue is injected. Performance on the physical 1GB/Mali-450 unit still needs a device check; software-emulator timing is not a frame-rate guarantee.

## Backup and reverting

Install `dist/iDash4-0.8.0-rc2-arm32-debug.apk` on the ARM32 head unit. To return to the pre-integration rc5 code/UI, install `dist/iDash4-0.7.0-rc5-rollback-full-debug.apk` over it. Both carry the same package, signing certificate and versionCode41 so replacement can work in either direction without uninstalling. The rollback is built from backup/pre-native-music (da2dead), changing only versionCode/name for installation. It preserves the application's existing data; it is a code backup, not a copy of personal account data. The intermediate rc1 core checkpoint remains available on its separate branch.

Craft Anything was used for source evidence, findings and investigation memory. Its report is included in this evidence folder; it did not automatically transplant the application. Source adaptation, library conversion, host integration and Android verification are implemented on the trial branch.
