# Drive + Apps skin — 0.6.0-rc5

User request: try the supplied Drive/Apps Liquid Glass skin ZIP and preserve a recoverable version from before installation.

## Installation

Imported the three Java classes from `imports/iDash3_Drive_Apps_LiquidGlass_Java_Skin.zip`. The archive's README/integration suggestions were reference material, adapted to the current native Drive implementation. No obsolete split-screen integration was added.

- Drive and Apps get the kit's scenic blue/magenta background and cyan/purple rim/gloss over existing glass surfaces.
- Cached paths and gradients rebuild only on drawable bounds changes; no new frame loop, repeated blur or dependency.
- Existing foregrounds, stateful focus/pressed backgrounds, original text shadows, native map and WebView internals are preserved. The glass radius is read from the existing material rather than guessed before layout.
- Apply at both the Drive early-return render path and the Apps normal render path. Refresh rebuilt Apps grids and Drive player content.
- Home, Settings, global wallpaper preference, JAKIM artwork/motion, layout geometry, app icons and click handlers retain their existing implementation.

## Backup and return

Exact pre-install source: `542ea1ef26f50555e6d608b22d0d9efa149fe1eb` (rc4), preserved at annotated tag **`backup/pre-drive-apps-skin-0.6.0-rc4`**. Original rc4 APK remains `dist/iDash4-0.6.0-rc4-debug.apk`.

A verified Git bundle, source tar.gz and original APK are also saved outside the checkout at `/workspace/backups/idash4-pre-drive-apps-skin-rc4/`.

**Simple return:** install `dist/iDash4-0.6.0-rc4-rollback-debug.apk` over this candidate. It is built from the exact rc4 source archive, with only version metadata changed to `versionName 0.6.0-rc4-rollback`, `versionCode 24`, allowing Android to treat it as an update rather than a blocked downgrade. The installed skin candidate is code 23. Both use the same debug signing certificate. No uninstall or app-data clear is needed.

After installing rollback code 24, any future forward build must use code **25 or higher** for normal installation. Original code-22 rc4 APK can instead be restored with `adb install -r -d` because it is a debug APK.

To inspect/edit the exact old source without resetting current work:

```sh
git switch -c restore/pre-drive-apps-skin backup/pre-drive-apps-skin-0.6.0-rc4
```

Main is not changed. Candidate branch: `feat/idash4-0.6.0-rc5-drive-apps-glass`.

## Verification

Build/unit/lint/guard and scoped emulator runtime results are under `audit-evidence/idash4-0.6.0-rc5/`. Drive preview PNGs are native app-view Canvas renders at 800×480 and 1024×600 with offline map tiles. Apps-native PNGs are settled software-emulator screenshots with 1024×600 and 800×480 logical viewports; physical screen capture output stays 1024×600 (see screens/README.md). Neither is a physical head-unit screenshot. Physical ARM32 performance, live radio hardware and streaming playback require head-unit testing.

Candidate and rollback APK builds completed. **76 unit tests**, lint (**0 errors, 89 warnings**), APK guard and **198 unique runtime assertions** across PageGlass, AppLibrary, DriveDashboard, DashboardLayout and ClockGlass completed successfully. The initial runner hit a summary-only shell parse error after all five classes returned PASS; details and the subsequent fixture rerun are retained in the audit folder.

In-place rollback install (code 24), followed by test-only forward install (`-d`, code 23), succeeded on API27; the full preference XML SHA-256 stayed identical through both installs. Cold-start timing on the software emulator exceeded the am-start wait threshold (~11s); a System UI ANR was observed during the rollback startup, and the launcher subsequently displayed. These checks establish package compatibility and preference preservation, not head-unit responsiveness.
