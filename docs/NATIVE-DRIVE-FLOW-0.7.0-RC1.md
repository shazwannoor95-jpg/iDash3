# iDash4 0.7.0-rc1 — Native Drive flow trial

Trial branch: `feat/idash4-0.7.0-rc1-native-drive-flow`. No main branch changes.
Base backup tag: `backup/pre-native-drive-rc8` (`46b36e91dddd8d9e68b37d9efdb1f23f67849deb`).

The Android Auto/CarPlay comparison suggested predictable native interactions that fit this Java launcher. This implements the first set of those interactions. It does not implement phone projection, a navigation route engine, Siri or Google Assistant. Craft Anything supported the earlier inspection; implementation and tests here use the existing Android Views code directly. No web animation dependency was added.

## Changes

- Exactly one sidebar source (Map, Media, FM, YouTube) has a cyan selected outline. Keyboard focus/press remains distinct.
- Tapping the selected source keeps its current view/controller rather than releasing and rebuilding it.
- Map camera and zoom are retained when switching sources or visiting Apps, with validated Android instance-state storage. Unlaid-out maps cannot overwrite a valid saved camera.
- Apps opened from Drive returns to Drive through Done, Android Back or the Apps dock toggle. Home-origin Apps still returns Home.
- Back in Drive handles YouTube fullscreen/history first, then returns a non-map source to Map; Back on Map returns Home.
- The sidebar media card opens the full media panel when access exists and retains the access explanation when needed.

Compact Home clock/JAKIM sizing, dock dimensions, ULTRA appearance, OEM FM integration and permission flows are retained. Leaving Drive still releases its main view resources as before. Returning from Apps recreates that view with the selected source/camera; this is not a promise of uninterrupted YouTube/FM playback while outside Drive. Camera state is not saved as GPS history in preferences.

## Installation and rollback

- Trial: [`iDash4-0.7.0-rc1-debug.apk`](../dist/iDash4-0.7.0-rc1-debug.apk), versionCode **29**.
- Return to rc8: [`iDash4-0.6.0-rc8-rollback-debug.apk`](../dist/iDash4-0.6.0-rc8-rollback-debug.apk), versionCode **30**. Install over the trial; do not uninstall first if keeping settings.
- Rollback source matches the backup archive except `app/build.gradle` version metadata. It uses the same package/debug signing certificate. Its higher code permits normal in-place installation. Any subsequent normal update after rollback must use code **31+**.
- Git source rollback: create a branch at `backup/pre-native-drive-rc8`; main was never changed. Full source archive and original APK also exist under `/workspace/backups/idash4-pre-native-drive-rc8/` in this workspace.

## Validation

Results and native emulator previews are under [`audit-evidence/idash4-0.7.0-rc1/`](audit-evidence/idash4-0.7.0-rc1/). Emulator API27 x86_64 uses software emulation, not the target ARM32 head unit. This verifies native UI behavior, not physical radio, GPS reception, streaming smoothness, ARM performance or modern Android graphics paths. No head unit test was possible.

- Build, Android test APK, JVM tests and lint: successful; **78 JVM tests**, zero failures; lint **0 errors / 90 warnings**.
- Final native flow check: **42 assertions passed**, including view identity for all four selected sources, camera/zoom after a real layout, selected state, Back, Apps Done/dock toggle and saved instance-state fields.
- Existing layout regressions in the initial run passed: ToolbarDock **446**, ClockGlass **98**, PageGlass **25**, DriveDashboard **24** assertions. The initial new flow check exposed a before-layout camera assertion failure after 15 assertions. The final source now excludes unlaid-out map capture; the test checks restored coordinates after the next layout. The initial failure is retained under `initial/`; the corrected new flow and affected Drive check are rerun against the final APK.
- Static APK guard passed; no native libraries, same application ID, APK below 8 MB.
- Rollback installation and launch were exercised on the emulator. The complete `idash.xml` preferences file retained the same SHA-256 after rollback installation, after launching rc8 and after reinstalling the trial. `-d` was used only to reinstall code 29 in this test after code 30; ordinary updates after rollback need code 31+.
- The first preview batch was excluded because an emulator System UI ANR dialog covered the app. Preview capture now refuses an unfocused launcher window; final preview files must pass this gate and visual review. This emulator infrastructure issue is separate from the passing app instrumentation results.

## Previews

[Drive FM with selected radio icon](audit-evidence/idash4-0.7.0-rc1/drive-fm-native-1024x600.png) · [Drive map](audit-evidence/idash4-0.7.0-rc1/drive-native-1024x600.png) · [Home with FM panel](audit-evidence/idash4-0.7.0-rc1/home-panel-native-1024x600.png) · [Apps](audit-evidence/idash4-0.7.0-rc1/apps-native-1024x600.png).

Final preview batch passed the focused-window gate and all five native screenshots were visually reviewed without an overlay/dialog. The preview helper uses a bounded activity monitor and a settled main-thread barrier rather than waiting for global frame idleness. Map tiles are deliberately disabled in the preview; the empty map grid is not a live navigation demonstration. The emulator clock is UTC; JAKIM calculations use Malaysia time.
