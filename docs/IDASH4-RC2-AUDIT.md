# iDash4 0.4.0-rc2 — stability and readability

Date: 18 November 2026 (Malaysia). Branch: `fix/idash4-0.4.0-rc2`.
Base: `integration/idash4-0.4.0-rc1`, commit `447b2f8`.
Package remains `com.shazwan.idash3`; versionCode 7. Main is not modified.

## Changes

- Repeated HOME intents on an already-visible Home keep the root hierarchy. Actual page navigation still rebuilds the root; this is a bounded optimization, not a navigation architecture rewrite.
- Panel Mode/Size changes close the old player and replace only panel children. Wallpaper, Home widgets and dock remain mounted. A video reload is still required after replacing the browser; resolution changes do not promise seamless playback.
- App Library preserves/clamps the current page during app-list refresh and saves it plus Home pagination in instance state. A changed search query resets the library page. D-pad tiles/pagers have explicit focus and a visible outline; horizontal navigation can cross page edges. Labels reserve height based on font scale, using two lines for larger text. Grid remains 4×2.
- Web fullscreen uses one existing video surface, with a bottom toolbar instead of overlapping fixed-width controls. Video reserves space for that toolbar. Exit, expand and cleanup remain supported. Browser controls have 48dp height and retry retains the last completed URL in this player instance. No second WebView or JavaScript bridge is added.
- Drive → **Peta sahaja** cancels pending split-start steps, removes the Drive overlay and requests exit from a split pair started by iDash before launching the selected map normally. This avoids deliberately starting media alongside the map. OEM split behavior still needs physical validation.
- Settings → Home & Dock → **Paparan & kebolehbacaan**: Original, Day, Night, Auto system; increased panel contrast and reduced transparency. These are glass-panel overrides, not a full-app theme or ambient-light sensor. Auto follows Android UI night mode when reported and falls back to the stored glass tone otherwise. Some old ROMs never report changes.
- Original leaves the existing appearance path intact. Overrides don't write material choice or custom glass values. To revert, select Original and turn both switches off. Classic/iOS26 remains independently selectable.
- New labels use Android resources. Existing mixed BM/English strings are not all migrated. Appearance editor preview honors readability overrides while saving the underlying draft separately.

## Verification

Build, lint, unit tests and API27 x86_64 emulator results are recorded in `audit-evidence/idash4-0.4.0-rc2/`. Test details distinguish original installed build from the final follow-up install. These are not ARM32/Mali-450 performance measurements.

The new offline regression covers HOME root reuse, panel container reuse, page refresh/focus, saved page values, display override reversibility, and fullscreen expand/pause cleanup. Saved-value assertions do not prove complete real process-death restoration of third-party applications.

## Remaining issues / limits

1. Google Maps search freeze is **not confirmed fixed**. Need the physical head-unit Maps/WebView/Play Services versions, memory pressure and an isolated search reproduction. No map APK is downgraded or replaced.
2. YouTube network playback, codec negotiation and whether the installed OEM WebView emits its custom fullscreen callback require actual streaming tests. Offline surface tests are not that evidence.
3. No measured claim of fewer milliseconds of jank: changes remove particular allocations/rebuilds. Full navigation reuse and app icon memory redesign remain future work.
4. Large text reserves more room, but extremely small panels or unusually large OEM font settings may still truncate. Test 1024×600 and 800×480 physically; no controls should overlap.
5. Readability overrides affect glass surfaces, not the third-party Maps/YouTube UI. They do not change screen backlight brightness. No universal contrast guarantee is claimed.
6. Driving/Parked gating, CAN data and projection receiver integration are deferred as agreed. No vehicle state is inferred.
7. Existing glass renderer/refraction and icon artwork are not redesigned in rc2. Physical sun/night readability and sustained 1GB operation are still unverified.

## Head-unit acceptance

- Install rc2 as an update with the same signing identity; keep data. Retain rc1 APK as fallback, noting Android normally blocks downgrade from versionCode 7 to 6 without special tooling.
- Drive → choose Google Maps → Peta sahaja → search, select destination and navigate. Repeat with normal split mode and compare responsiveness. Repeat using Waze only if already installed/chosen.
- YouTube: play → website fullscreen → panel toolbar → expand → exit → background/return. Start retries the last URL but may not restore playback time/history after browser release.
- App Library: swipe/pager/D-pad, refresh on page 2+, search/no results, large font, and Home return.
- Readability: all four modes, both switches, wallpaper changes, Classic/iOS26; revert overrides to Original/off and verify saved custom appearance returns.

For the physical Maps investigation, `bash tools/headunit/collect-runtime.sh idash-runtime.txt` collects selected hardware/heap properties, package versions, active WebView provider and process memory using ADB. It does not change device settings/packages or collect logcat/account/location data. Requires one connected device, or `ANDROID_SERIAL` to select it. Run while Maps is affected, then repeat after using Peta sahaja. The script was syntax-checked and exercised on the emulator; no physical-head-unit report is included.

## Evidence and reproduction

See [consolidated final results](audit-evidence/idash4-0.4.0-rc2/FINAL-RESULTS.md) and `build-results.json` for counts and final APK identity. Earlier unsuccessful runs are retained, including runner/invocation issues and the D-pad focus regression that led to waiting for destination layout. The clock/cache test now drains both UI and network workers before asserting loaded JAKIM data.

With Android SDK/ADB configured, build using `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest`. Install app/test APKs before instrumentation. The default `tools/ci/run-instrumentation.sh` gate includes `Rc2Checks`; process persistence requires `tools/ci/run-appearance-persistence.sh` because write and read must run in separate processes. Run these on a disposable emulator: fixtures temporarily change preferences and restore them on normal completion.
