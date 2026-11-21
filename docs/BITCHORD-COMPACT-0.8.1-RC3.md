# Half-size BitChord UI — emulator trial 0.8.1-rc3

The user selected **all BitChord elements at 50%, with the viewport remaining full size**. This trial supplies only an Android Studio x86_64 APK, on `feat/idash4-0.8.1-rc3-emulator-compact`. `backup/pre-bitchord-half-size` preserves rc2 at cfee5f2d39b0380f4678731876baa83dc3277b54; main is not changed.

The embedded BitChord composition provides half the host's pixel density while preserving its font-scale multiplier. Dp and sp dimensions therefore convert to half their previous pixel sizes, and the full pixel viewport exposes twice the logical width/height for responsive reflow. The same density drives measurement, text rendering, layout and pointer coordinate conversion. WindowInfo still reports the physical panel bounds. The native Android toolbar is reduced from 48dp/12sp to 24dp/6sp with corresponding padding and corner-radius changes. Drive's rail and other host panels retain their existing scale.

Responsive content still fills or caps itself according to its layout constraints; this is a smaller UI within the full viewport, rather than a reduced bitmap of the prior screen. The native in-process player and music routing from rc2 remain in use.

Install `dist/iDash4-0.8.1-rc3-emulator-x86_64-debug.apk` (iDash4, `com.shazwan.idash3`, versionCode44, API26+), then open Drive → B. To restore the previous scale on the emulator, install the preserved rc2 x86_64 debug APK with `adb install -r -d iDash4-0.8.1-rc2-emulator-x86_64-debug.apk`; both use the same debug signing certificate. This procedure restores code, not a personal-account backup. The prior rc2 report contains the executed downgrade/reinstall evidence; no new private settings or accounts are bundled.

Local testing uses the existing API27 software-rendered emulator without KVM/audio output. Cold startup and long audio playback have known limitations in this environment, described in the rc2 report. The UI scale change is not a frame-rate or sustained-playback performance claim. No new head-unit/ARM build is produced.

## Validation

The exact supplied x86_64 APK builds, installs on API27 and passes lint with zero errors/105 warnings. The existing native UI flow passes 18 assertions at the new scale, including Search/Library navigation, default expansion, restore/re-expansion, native composition identity, finite panel bounds and reopening. This change does not add tests that merely check the scale constant.

The software emulator displayed TIME_TICK ANR dialogs during first composition loads; “Wait” was selected so the functional UI checks could continue. These functional checks do not establish smooth cold startup. No live streaming or physical head-unit validation is claimed.

[Actual half-size native player preview](audit-evidence/idash4-0.8.1-rc3/native-player-half-size.png). It uses labelled synthetic PCM test audio; it is not a live catalogue result. The full physical viewport remains visible with the host rail at its previous scale.
