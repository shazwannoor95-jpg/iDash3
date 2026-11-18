# iDash4 0.5.0-rc4 — FM panel and direct Drive rail

Branch `feat/idash4-0.5.0-rc4-panel-rail`; base `15729e6`. Package retained, versionCode 16. Shared glass renderer and main branch unchanged.

## Layout

- Home: Mode, Layout and Close share the top row with Panel, Customize and Control Center while a panel is open. Smaller text/icons and 36dp visible pills retain 48dp touch targets. The redundant Media panel header is removed; panel padding/top gap is reduced.
- Native FM fills the available panel directly in both Home and Drive, independent of the web layout preset. The fixed 560×330 ResolutionStage is removed from both FM hosts. Layout selects the website viewport only; FM and the car view follow the actual panel bounds. FM controls, presets, OEM controller, source selection and Classic fallback are retained.
- Drive order: left icon rail → JAKIM/media/date cards → large map/player. The former dashboard mode chooser is removed. Peta, Media, YouTube and Radio FM icons select their content directly, with no intermediary chooser. The phone shortcut is replaced by YouTube.
- Add App is an icon beneath the media-source controls. Choose an installed app to save one shortcut; its actual app icon replaces the plus glyph. Tap to launch normally; hold to replace or remove. Apps grid and Home remain at the bottom. Selected app preference is `dashboard_app`; missing/uninstalled apps return to Add App behavior.
- The radio/plus glyphs are original Canvas paths. No Apple font/symbol/runtime assets or new dependencies are introduced. No external-app embedding or split-screen API is added.

## Verification

Build, 56 unit tests (0 failures), lint (0 errors; 88 warnings), and APK guard passed. Final runtime: **215 assertions passed** (PanelLayoutChecks 72, DashboardLayoutChecks 54, PremiumFmChecks 35, DriveDashboardChecks 24, PanelFullscreenChecks 30). Runtime results are retained under `audit-evidence/idash4-0.5.0-rc4/`.

The first runtime attempt could not launch PanelLayoutChecks: placing its declaration before the default runner let the Android manifest merger replace it. The declaration was moved after SmokeChecks and the test APK rebuilt. DashboardLayoutChecks also initially checked shortcut removal before Android delivered the dialog button callback; the check now waits for the next UI turn. Both are test harness corrections, with the same application APK retained. First-attempt results remain visible; final layout results are recorded separately. The software emulator also displayed a System UI not responding dialog during an earlier FM capture; that interrupted image is not used as the final layout screenshot. Software API27 x86_64 emulator checks do not certify RF audio, hardware FM integration or ARM32 head-unit performance. No live YouTube playback or live map-tile claim is made by the layout fixtures.

APK SHA-256: `607b3043e41d2db73bb984e3e7efdfb725275cf6d29be7b0fe24b079fcbb7d8b`.

Final screenshots: `screens/home-fm-1024x600.png` and `screens/drive-rail-1024x600.png` in the evidence directory. Both are actual unedited screenshots from the native landscape API27 emulator after dismissing its System UI dialog, opened using normal UI taps. The Drive screenshot shows the pre-playback YouTube state, not live streaming.
