# Emulator-only native BitChord player — 0.8.1-rc2

This trial targets the Android Studio x86_64 emulator. Work is isolated on `feat/idash4-0.8.1-rc2-emulator-player`; `backup/pre-emulator-player` preserves the previous rc1 at 7a37e7919ae1fd3f0da5302d4ea14281fd02b2ad. Main is not a deployment target. No new head-unit/ARM APK is built for this trial.

Selecting B now expands music beside the Drive rail immediately. The existing restore button brings back JAKIM/media/date cards; leaving music restores them as before. The media card recognizes iDash's own session without requiring notification access, selects the native music page and requests its own Now Playing view.

The embedded Now Playing view is rendered as an opaque layer in the same ComposeView. The upstream Material `ModalBottomSheet` creates its own Activity-sized dialog window; its bounds are not constrained merely by overriding Compose's `LocalWindowInfo`. The embedded branch therefore uses its own inline surface and Back handling, while retaining the original standalone implementation. Opening/closing this surface does not recreate the player service. This also avoids its dialog-to-Activity volume/Back focus handoff for ordinary playback.

The host intercepts supported music URLs and explicit intents addressed to standalone `com.music.bitchord` while the native music panel is active. Those requests go through BitChord's existing in-process `MusicLink`/`PlayerDeepLink` relays, without Android resolving a separate installed music application. Sign-in and unrelated web URLs retain their normal flows. The playback service's session activity now explicitly targets iDash's own LauncherActivity with an open-music/player request. Receiving that request while Drive already exists preserves its composition instead of rendering a new dashboard.

These changes cover known escape routes and the separate player-dialog window. The user's exact standalone-app launch has not been captured as a device log; source inspection alone does not establish which route caused that specific occurrence. Instrumentation uses real synthetic local PCM, actual native player UI, viewport/focus checks and Android ActivityMonitors to check routing without live catalogue access.

## Install and revert

Install `dist/iDash4-0.8.1-rc2-emulator-x86_64-debug.apk`. This is package `com.shazwan.idash3`, label **iDash4**, versionCode43, Android API26+. No standalone BitChord installation is needed. Open iDash4 → Drive → B. Music opens expanded automatically; tap the native mini-player or the owned-session media card to open Now Playing.

To return to rc1 on the emulator without uninstalling, use the preserved previous x86_64 APK:

```
adb install -r -d iDash4-0.8.1-rc1-x86_64-debug.apk
```

Both builds are debug-signed with the same certificate. The `-d` flag is needed because rc1 uses versionCode42. This restores application code, not a personal-account backup. Existing licensing and corresponding-source notes in `NATIVE-BITCHORD-0.8.0-RC2.md` still apply. Physical head-unit performance and live streaming/login are not established by local emulator playback tests.

## Validation

The exact supplied x86_64 APK installs on the API27 software emulator. 78 JVM tests pass (zero failures/errors/skips), lint completes with zero errors/105 warnings, and 18 actual native UI assertions pass, including automatic expansion, manual restore/expand, composition identity, Search/Library navigation and disposal/reopening. Evidence and the APK SHA256 are in `audit-evidence/idash4-0.8.1-rc2/`.

`EmbeddedPlayerChecks` also passes 19 assertions against the final APK: real synthetic PCM starts and advances; the actual inline Now Playing surface appears; its bounds remain inside the host content and the Activity retains focus; a standalone BitChord intent and a supported music URL do not reach Android activity dispatch (ActivityMonitors); Back closes the inline player while retaining the expanded music page; native metadata, pause/seek/resume/next and playback after switching back to the map are checked. [Actual inline player preview](audit-evidence/idash4-0.8.1-rc2/native-inline-player.png).

The local emulator uses software CPU/GPU rendering with audio disabled. Its boot System UI ANR needed dismissal before testing, and the long player run logged generic-audio underruns and Media3 no-progress timeout/retry events despite the functional assertions completing. These passes are not evidence of uninterrupted sustained playback or smooth cold startup. Streaming/login on the user's emulator and reproduction with their separately installed BitChord APK remain unverified; routing is tested using Android dispatch monitors.
