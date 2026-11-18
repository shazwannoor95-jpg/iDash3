# iDash4 0.5.0-rc3 — Dashboard layout

Branch `feat/idash4-0.5.0-rc3-dashboard-layout`; base `26db9e1`.
Package retained, versionCode 15. Main and shared glass renderer unchanged.

## Changes

- Main content no longer reserves the Drive Dashboard title or mode-name row. YouTube fills the main region to its top edge. The website's own UI and native player controls remain independent of this Dashboard layout.
- Mod is a 2×2 outline icon in the left rail. Rail controls have centered 32dp artwork in uniform 48dp targets with 4dp vertical margins. Permanent individual glass button frames were removed; the rail's glass stays, with a focus/pressed outline. Apps now uses a grid rather than an App Store shopping bag; Home uses a distinct house outline.
- Rail order: clock, map, media, phone, Mod; flexible space; Apps and Home. GPS controls/status overlay the native map only and do not reserve space in media modes.
- JAKIM is the first right-hand card: original crescent glyph, bold **20 m to Asar** style text, and a subtitle retaining the real time/zone. Tap the card for zone/timetable access. No large Zon/jadual button.
- Media card uses a music icon and compact icon-only transport targets. Tap the metadata/card for the existing explicit media-access flow. The icon is decorative, not a claimed album cover. Transport labels remain hidden child views for compatibility with the existing MediaSession bridge; accessible control names remain available.
- Calendar card uses an original white weekday/day tile and two real date lines. Tap for the date picker. It does not invent calendar events or read a user's calendar.
- The visual structure follows the reference while retaining iDash's glass/material preferences and licensed font; no Apple runtime or SF font/symbol assets are imported.

## Timetable compatibility

`JakimCalendar.nextPrayerTime` supplies a structured name/time/remaining-minute value using the same MYT timetable algorithm and obligatory-prayer selection as before. The existing `nextPrayer` output format for Home remains unchanged. The Dashboard uses total remaining minutes; its date badge follows the device date/time zone. Missing zone/year data still has an explicit empty state.

## Verification

Evidence under `audit-evidence/idash4-0.5.0-rc3/` includes layout/rail bounds at 1024×600 and 800×480, true twenty-minute/one-minute/two-minute countdowns, date badge day rollover, the legacy clock/cache formatter, multi-mode lifecycle and fullscreen regression checks. The first layout runner passed before the emulator process disappeared. Infrastructure-interrupted attempts are retained separately and do not count as successful regressions. The emulator was relaunched in an active execution session for the complete run.

Build, 56 unit tests, lint (0 errors; 88 warnings), APK guard, and all four instrumentation runners passed: 141 unique runtime assertions. The layout runner also passed again on a native 1024×600 AVD after replacing the old portrait framebuffer configuration.

`dashboard-layout.png` is an actual emulator fixture: the test measures both 1024×600 and 800×480, with its last compact layout visible within the native 1024×600 framebuffer. `dashboard-map-native-1024x600.png` shows the normal full-display Dashboard opened from Home on the native landscape AVD; its blank map grid reflects unavailable tile networking in this environment. `dashboard-youtube-native-1024x600.png` shows the YouTube mode opened using the relocated Mod control: no Dashboard title rows, with the player’s own search/transport UI still present before playback. These screenshots are not physical-head-unit photos, proof of live streaming or proof of live map tiles. Actual device font scaling/daylight visibility and media-provider behavior still need user validation. No new routing, calendar-event, background Live Activity or vehicle-data capability is introduced.

APK SHA-256: `f20e75f102308f7bcdf39e5b93a9b8e03a7093f2c528ac1909541ffaf69c5d15`.
