# Matching compact Home cards and smaller dock — 0.6.0-rc8

User asked to make the clock/date and JAKIM cards equally sized and smaller, and shrink the bottom bar. Branch `fix/idash4-0.6.0-rc8-compact-home`, based on rc7 `d20101c59fffd39fb2533e6f619feb56a13e4049`. Version code **28**, allowing normal installation over the earlier rc6 rollback (code 27).

Home cards now share the same left edge, width and height. Width is capped at **224dp**, height at **116dp**, constrained by available space. Geometry follows the current JAKIM artwork's cropped aspect ratio, so the image fills the same bounds without stretching or losing its border. Both cards resize together when the prayer scene changes. The existing **14dp gap** is retained. Clock type is reduced from 46sp to 36sp (responsive floor 24sp); date from 14sp to 12sp (floor 10sp). Text-fit profiles use the actual card width and check vertical ink space.

The normal dock shrinks from approximately **397×72dp to 349×64dp**, with 28dp icons instead of 34dp, 52dp item widths instead of 60dp and 9sp captions. Large-dock mode is also reduced (34dp icons / 58dp items / 72dp base height). Font-aware caption/dock heights remain; 8dp outer vertical padding and 48dp minimum item touch targets are preserved.

ULTRA styling, toolbar arrangement, app data and JAKIM time/countdown/animation logic remain as rc7. Drive's prayer view retains its original sizing: scene-driven Home remeasurement is restricted to children of HomeWidgets.

## Verification

Build/unit/lint/guard results and runtime logs are under `audit-evidence/idash4-0.6.0-rc8/`. ClockGlass checks equal dimensions/alignment/compact bounds, text ink and card separation at 1024×600, 800×480 and 800×400 with normal and 1.3× fonts. All six official prayer phases are exercised for aspect-fit and equal-size cards. ToolbarDock checks both dock sizes, both screen sizes and font scales, including smaller bar bounds and preserved hit targets. PrayerJourney regression checks cover rendering and motion lifecycle.

Published previews are settled native API27 emulator screenshots, including **Home with the panel closed** as requested in the screenshot. No physical head-unit test or FPS claim.

## Backup

Previous rc7 source is preserved by tag `backup/pre-compact-home-0.6.0-rc7`, its original branch/APK, and a source archive + original APK under `/workspace/backups/idash4-pre-compact-rc7/`. Main remains unchanged.

The initial hardware-rendered API27 run exposed a native HWUI SpotShadow tessellation abort on the new dense 28dp squircle outlines. Small tiles on API26/27 now use a rounded-rectangle **shadow outline only**; the visible squircle painting remains unchanged and hardware acceleration remains enabled. Initial crash evidence is retained in initial-shadow-crash/. Subsequent corrected runs exercise real hardware-accelerated window drawing.

Final build, **76 unit tests**, lint (**0 errors, 90 warnings**), APK guard and **603 unique runtime assertions** passed. The final packaging also replaces obsolete dock-size numbers in Settings with compact/large-icon captions; this text-only correction followed the geometry runs and does not change the captured Home/Apps layout.
