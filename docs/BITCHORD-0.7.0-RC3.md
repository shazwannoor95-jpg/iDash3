# BitChord native Drive integration — 0.7.0-rc3

Trial branch `feat/idash4-0.7.0-rc3-bitchord-drive`, backup tag `backup/pre-bitchord-rc3`. Main is unchanged.

## Use

Select the **B / BitChord** icon on the left rail. The map region becomes a native BitChord control panel. Install the official Android phone APK from https://github.com/kushagrasinghx/BitChord/releases/latest once, then choose **Buka BitChord**, select a song in BitChord, and return to iDash Drive. Grant media access through the existing explanation/settings flow if remote controls are wanted. The full BitChord app opens in its own Activity; its UI is not embedded in iDash. The dashboard panel supplies title/artist/status and previous/play/pause/next only when the published MediaSession supports those actions.

For the known API27 ARM32 head unit, choose the **phone armeabi-v7a** APK, or Universal if available. Do not choose arm64-only, TV or Wear OS. Current upstream source declares minSdk26 and armeabi-v7a/arm64-v8a/x86_64 splits; a current release binary was not downloaded/validated here. This integration also recognizes the official developer package, but release is preferred for everyday use.

## Optimizations and behavior

- No additional WebView, embedded task, virtual display, player engine, audio decoding or per-frame polling in iDash. BitChord owns playback in its own app/service.
- Package-filtered MediaSession selection accepts `com.music.bitchord` and `com.dev.bitchord`. Other players cannot become the panel's controller. The generic media card keeps its existing all-app behavior.
- Repeated session-list callbacks with the same token retain the existing controller/callback rather than unregistering/re-registering it.
- Selecting the active BitChord source preserves the panel. Leaving the source closes its subscriptions; backgrounding pauses them, and returning restores them. BitChord's own playback is not stopped by iDash.
- Artwork is not copied into the panel, keeping the existing low-memory metadata/transport approach. Source switching releases the map, FM or YouTube view as before.
- Eight rail buttons keep 48dp targets and adapt margins for 800×480 and 1024×600. Android Back returns to Map; Apps Done returns to the selected BitChord panel.

Optimization here applies to **iDash integration**, not the internals/settings of the separately installed BitChord APK. On a 1GB unit, consider disabling BitChord's optional canvas/video, Automix and crossfade; those settings are not silently changed by iDash. Streaming, login, audio quality, battery/RAM and ARM32 playback require actual BitChord/head unit testing.

## Provenance

Inspected upstream commit `acf219a7e4a7f52f026d4336d5c7ac5cf7bfafcc`. BitChord is GPLv3. No upstream code, branding assets, native libraries or binaries are incorporated into iDash. This uses Android package launch and MediaSession APIs only. Upstream source checkout is unmodified. GitHub release API access returned proxy 403 in this environment; the app links to the official release page rather than guessing an asset URL.

## Builds / rollback

Candidate `dist/iDash4-0.7.0-rc3-debug.apk`, code33. Rollback to the prior rc2: `dist/iDash4-0.7.0-rc2-rollback-debug.apk`, code34, same package/debug key. Install rollback over the candidate without uninstalling to retain preferences. Later normal upgrades after rollback must use code35+. The old rc2 clamps unknown source4 to its existing YouTube source; choose Map manually after rollback if needed. No automatic stream starts.

Evidence: `docs/audit-evidence/idash4-0.7.0-rc3/`. Build, 79 JVM tests and lint passed (0 errors / 91 warnings, including the new programmatic custom-view constructor warning). No physical BitChord stream/head unit verification is claimed.

Scoped native checks passed **34 assertions**: source4 creation/persistence, unrelated-session rejection, same-token controller reuse, eight-button rail at both target sizes, pause/resume subscriptions, Apps return and Back-to-Map. The emulator has no BitChord installed; the missing-app/download state was captured and visually reviewed without overlays: [native preview](audit-evidence/idash4-0.7.0-rc3/drive-bitchord-native-1024x600.png). This is an integration test, not proof of actual BitChord streaming/login/transport responses.

NativeDriveFlowChecks also passed **42 assertions** after the change. Total scoped native assertions: **76**, plus 79 JVM tests. The final preview passed the focused-window gate and was visually reviewed.

User-provided hardware photos confirm: TEST9278B, armeabi-v7a/armeabi, four CPU cores (one observed instantaneous reading 1.12GHz, not a confirmed maximum), Mali-450MP / GLES2.0, 1024MB RAM (315.2MB available at capture), 1024×600 / 160dpi / density1 / 60Hz. SDK27 is the compatibility target despite the displayed version label9.1 and inconsistent fingerprint version text. Root is reported by the diagnostics app, but actual su privileges/embedded-activity support have not been verified. No device identifiers, serials, Android IDs or MAC addresses are included here. Do not infer physical storage size or a confirmed SoC solely from the reported build strings.
