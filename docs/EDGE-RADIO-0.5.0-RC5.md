# iDash4 0.5.0-rc5 — Right edge and FM alignment

Branch `fix/idash4-0.5.0-rc5-edge-radio`; base `d3c55ab`. Package retained; versionCode 17. Shared glass renderer, FM controller and main branch unchanged.

- Home with an open panel and Drive use a 4dp right gutter. Drive also removes the redundant 8dp right margin on its main map/player column.
- ContentInsets replaces Home's cached bar padding and Drive's unconditional inset addition. Requested immersive navigation/status bars do not retain stale system-bar space. Visible overlapping bars and physical display cutouts remain protected; space already reserved by the decor is subtracted. Insets are reapplied on layout and system-UI visibility changes.
- FM Power, Tune and more controls draw a centered 24dp glyph inside each 48dp target, replacing top compound drawables. The toolbar has one aligned 48dp row and consistent 6dp gaps; source selection stays between Tune and more.
- FM header, tuner and two preset rows share 8dp side padding. Presets have consistent 6dp gaps with 52dp targets, and plus/minus align with the preset grid's outer edges. No fixed-width or fixed-aspect FM wrapper returns.
- The Drive rail reduces vertical spacing on short landscape screens while retaining all seven 48dp controls, so Home does not fall below a 400dp screen.
- Home's compact header sets padding after its background and keeps text on one line so artwork stays inside each pill.

## Verification

Build, 62 unit tests (0 failures), lint (0 errors; 88 warnings), and APK guard passed. Runtime results and unedited emulator screenshots are retained under `audit-evidence/idash4-0.5.0-rc5/`. Software API27 emulator results do not certify OEM RF audio or physical head-unit behavior. No shared material or icon asset files are modified.

Coverage includes stale hidden-navigation insets, visible/partly consumed bars and cutout arithmetic, actual Home/Drive right-edge geometry, toolbar glyph centering and equal gaps, ten presets, native FM host sizing, direct rail switching and a 400dp-height rail. Physical cutout rendering on API28+ is guarded in code and checked by lint; the runtime emulator is API27.

Runtime: **268 assertions passed** across five instrumentation runners.

APK SHA-256: `446b345407fce2a344e8e253d7db16df1b6039fa85e4eebf92688bed7e89099f`.

The software emulator displayed a System UI not responding dialog during an automatic fixture capture. Final appearance screenshots are unedited captures after dismissing that system dialog and opening the views with normal UI taps; these do not claim live RF or video playback.

Final 1024×600 screenshots in the evidence directory: `screens/home-radio-1024x600.png`, `screens/drive-radio-1024x600.png` and `screens/home-web-1024x600.png`. The web screenshot is the pre-playback state; all three show the actual available right edge.
