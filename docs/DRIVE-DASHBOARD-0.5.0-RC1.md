# iDash4 0.5.0-rc1 — Drive Dashboard

Branch: `feat/idash4-0.5.0-rc1-drive-dashboard`. Base: rc7 `e55d376`.
VersionCode 13; package/signing identity retained. Main unchanged.

## Delivered behavior

Drive now opens a native dashboard with a left icon rail, a large main region and persistent media/JAKIM/calendar cards on the right. The main **Mod** chooser offers Peta, Audio/media, Radio FM and YouTube web. The chosen mode is saved. Map/player components are disposed when switching modes, so there is one main renderer/player at a time; FM stops when its owned panel closes and the website requires Start again after background release. External music playback itself is not stopped when its MediaSession controls detach.

The Android split page, controller, floating bar, accessibility service and their source files are removed. Overlay/foreground-service permissions and the accessibility declaration/config are removed. The optional Ringkas workspace links to Drive Dashboard; no split setup entry remains. Historical reports/APKs still describe older versions.

## Native map

Uses osmdroid-android 6.1.20 (Apache 2.0) with OpenStreetMap standard raster tiles. This avoids Google Maps app/version and Play Services dependencies and does not introduce an OpenGL vector renderer. It supports pan/zoom and live foreground device position after explicit permission/opt-in.

Before a location fix, the map shows the Kuala Lumpur area with **“Peta kawasan · bukan lokasi anda”** and no device marker. Location uses Android GPS/network providers at a requested minimum 5-second/10-meter interval. Old fixes beyond two minutes are rejected. Moving the map stops automatic recentering; Lokasi enables following again. Subscriptions stop on pause, mode change and close. No background location, speed/gear inference, CAN data or phone projection is implemented. JAKIM zone selection remains manual and is not inferred from GPS.

This version has **no destination search, route calculation, turn instructions or voice navigation**. It is a real map component, not a Google Maps activity embedded in another app. Internet is needed for new tile areas; cached tiles may remain available. Cache settings request 32 in-memory tiles and a 50 MiB disk cache trimmed toward 40 MiB; actual viewport needs and in-flight tiles can temporarily exceed the memory count. Attribution and app User-Agent are configured. No bulk/offline-region download feature is provided. Licenses are bundled in `assets/licenses/` and listed in NOTICE.

## Live widgets

- MediaSession metadata/transport controls use the existing access flow. No automatic playback/audio-focus acquisition. Metadata appears only if the source app publishes a usable session and access is granted; web/FM are not promised to expose such sessions.
- JAKIM shows the next obligatory prayer, scheduled time and remaining minutes using the official cached timetable. It updates via the existing foreground ticker without rebuilding views, handles missing zone/date/data explicitly, and supports zone/calendar access. No fabricated timetable or GPS-derived prayer calculation.
- Calendar shows the device's current date and opens the date picker. It does not claim to read actual calendar events; that permission/data source has not been added.

These are native in-app widgets, not iOS ActivityKit or an Android home-screen widget/ongoing notification. The dashboard does not run a background Live Activity service.

## Verification

Results under `audit-evidence/idash4-0.5.0-rc1/`. API27 x86_64 emulator checks exercise a real MapView, no location opt-in by default, JAKIM one/two-minute boundaries and expired data, geometry at 1024×600 and 800×480, mode disposal, pause/resume, complete release and removal of the split service. Unchanged-area regressions cover Ringkas, worker isolation, material switching, fullscreen and clock/cache behavior. The final follow-up checks the final APK after rail layout/clock-format polish.

The map test disables network tile loading deliberately. Its screenshot shows an offline grid fixture with attribution, not a successful live map download. A separate HTTPS tile probe from this managed environment was blocked with CONNECT 403, so live OSM tile download, physical GPS accuracy, sustained ARM32 performance, radio audio and actual streaming remain unverified. No claim of full CarPlay navigation or pixel-identical Apple UI.

## On-device acceptance

1. Open Drive; verify Dashboard replaces split and no accessibility/overlay permission is needed.
2. Mod → Peta: internet on, pan/zoom and verify tiles load; select Lokasi, grant permission, verify actual position and behavior after background/return.
3. Select a JAKIM zone; verify countdown and date against the official timetable, including near midnight.
4. Switch Peta ↔ Audio ↔ FM ↔ YouTube; verify no old player remains running/attached, and the right widgets remain visible.
5. Test at both head-unit sizes, with larger text and day/night/material preferences. Existing glass material choices remain available.

Final result: 56 unit tests and 155 unique runtime assertions passed across six runners (final dashboard follow-up supersedes the earlier dashboard result). Lint: 0 errors, 83 warnings. Static/APK guard passed; APK has no native ABI libraries.
