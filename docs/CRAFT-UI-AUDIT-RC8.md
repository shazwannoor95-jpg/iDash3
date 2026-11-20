# Craft Anything UI audit — rc8

**Result: all 92 declared layout conditions passed across eight settled native windows.** No production layout defect was confirmed in this scope, so the rc8 app implementation and APK remain intact. This branch adds an audit-only instrumentation harness and evidence.

Artifact: `com.shazwan.idash3`, 0.6.0-rc8 / code 28, minSdk 26, targetSdk 35. SHA-256: `f6201e2e1743b32c305713eb46266184455504d190188d1b8387b25f7f6614ac`.

Craft Anything 0.6.1's built-in Android parser inspected the owned APK and signing metadata. Craft's ADB tools captured screenshots, downloaded measurements and recorded evidence ids; its investigation tools recorded hypotheses, findings, unknowns and a report. The Android test harness exports settled View/TextView geometry; the retained Python scripts compute the declared conditions. Craft did not autonomously generate or modify the Android app.

## Observations

| View | Logical viewport | Result | Preview |
|---|---|---|---|
| Home | 1024×600 | Equal 224×114 cards in the observed night scene, 14px gap, 349×64 dock | [Image](audit-evidence/craft-rc8-home-fm/screens/home-1024x600.png) |
| Home + FM | 1024×600 | Panel filled; all ten presets visible and ≥48×48 | [Image](audit-evidence/craft-rc8-home-fm/screens/fm-1024x600.png) |
| Home | 800×480 | Same card dimensions/gap and dock, within viewport | [Image](audit-evidence/craft-rc8-home-fm/screens/home-800x480.png) |
| Home + FM | 800×480 | Panel filled; all ten presets visible and ≥48×48 | [Image](audit-evidence/craft-rc8-home-fm/screens/fm-800x480.png) |
| Apps | 1024×600 | Six actions in one row; eight visible tiles in a 4×2 grid | [Image](audit-evidence/craft-rc8-home-fm/screens/apps-1024x600.png) |
| Apps | 800×480 | Toolbar, visible app grid and dock remain in bounds | [Image](audit-evidence/craft-rc8-home-fm/screens/apps-800x480.png) |
| Drive | 1024×600 | Sidebar targets ≥48×48; three cards visible; native map present | [Image](audit-evidence/craft-rc8-home-fm/screens/drive-1024x600.png) |
| Drive | 800×480 | Same constraints passed; GPS remained disabled | [Image](audit-evidence/craft-rc8-home-fm/screens/drive-800x480.png) |

No measured standard TextView clipping or ellipsis occurred in the final captures. Home/FM contributed 50 declared conditions; Drive/Apps contributed 42. Sizes above are logical pixels at density 160. On this emulator, 800×480 logical windows are composited into a 1024×600 physical PNG, so PNG raster dimensions alone must not be used to infer layout bounds.

## Limits and capture correction

These are normal-font/small-dock, static captures with Smooth Transitions disabled for repeatable measurement. This audit does not claim dynamic-animation FPS, physical radio RF/playback, GPS navigation, permission flows, all app-library pages or exhaustive interaction testing. Drive tiles were deliberately offline; the map remains a real MapView, not an image replacement.

The small custom-painted JAKIM footer is outside the TextView clipping scanner. Its readability and the small dock captions at real head-unit viewing distance remain an open physical-device check. The compact dimensions were explicitly requested, so a subjective readability concern was not used to enlarge the cards again.

An initial screenshot set was obscured by a **System UI** ANR dialog in the software emulator. It was excluded. Final capture requires real app-window focus and every final image was visually reviewed. The ledger retains and labels the excluded attempt; no claim that iDash3 caused the System UI dialog is made.

## Reproduce and inspect

- [Craft-generated findings/report with evidence ids](audit-evidence/craft-rc8-home-fm/CRAFT-REPORT.md)
- [Exported Craft evidence bundle](audit-evidence/craft-rc8-home-fm/craft-evidence.json)
- [Home/FM measurements and conditions](audit-evidence/craft-rc8-home-fm/measured-summary.json)
- [Drive/Apps measurements and conditions](audit-evidence/craft-rc8-home-fm/pages-summary.json)

The published `analyze-ui.py` and `analyze-pages.py` scripts replay the geometry checks against the sibling `craft-*.json` files. `CraftUiAudit` and `CraftPagesAudit` are Android-test-only capture helpers; the host coordinator reads their ready markers, calls Craft's screenshot/download tools, then acknowledges the next frame. Each Craft capture needs a fresh output folder. The test APK built successfully; all eight native audit cases completed successfully.
