# iDash4 0.6.0-rc1 — Native JAKIM prayer journey

Branch `feat/idash4-0.6.0-rc1-prayer-journey`; base `c271589`. versionCode 19; package retained.

## Behavior

Home and Drive JAKIM cards now contain a native Canvas journey: current and next event/time, real remaining hours/minutes, elapsed-time arc, moving light limited to completed progress, active indicator glow, small icon motion, night stars, vector mountain/mosque backdrop and six phase labels. Phase-specific navy/day/sunset colors and original icons follow the supplied visual references; this is a lightweight vector interpretation, not a pixel-identical copy of the photorealistic illustrations. No React, WebView, new image assets, third-party icon code or libraries are added. Shared glass renderer and other panels remain unchanged.

Sequence: Subuh → Syuruk → Zohor → Asar → Maghrib → Isyak → next-day Subuh. Syuruk is explicitly labeled as an additional marker, not a fardu prayer. At the exact event timestamp the active icon switches immediately and the next interval begins. Isyak progress continues through midnight, year and leap-day boundaries without resetting. The main card uses the six consecutive intervals, including a separate Subuh→Syuruk segment, rather than a three-node Subuh→Zohor arc with a Syuruk midpoint.

JAKIM source/zone and MYT are reused. No sample image times are hardcoded. Missing previous/next boundary data gives a clear incomplete-schedule state; no time is extrapolated. Tap the widget to open existing zone/timetable/refresh controls. Annual schedule coverage still depends on available official data/cache; missing next-year data must be refreshed through those controls. Existing Home prayer formatter and fardu-only selection remain for internal compatibility; their old text views are hidden behind the new widget.

## Motion and lifecycle

Decorative frames are capped at about 15 per second (66ms interval), only when attached, shown, intersecting the visible screen, focused, owner resumed, Smooth Transitions enabled, system animator scale enabled and GlassPerf not low. Active-loop eligibility is rechecked before every frame. Pausing, detaching, hiding and focus loss remove pending callbacks; the owner second ticker restarts eligible motion when visible again. In low/reduced-motion mode there is no decorative frame loop or fade; real time still updates via the existing foreground ticker. Progress is timestamp-derived and independent of decorative motion.

Phase content crossfades for 450ms when allowed. Cached Paint/Path/PathMeasure and size-independent vector geometry are reused. Gradients/timelines are prepared on phase/data/day changes, not every frame; no bitmap blur or background service is used. The entire card is a focusable timetable button with a spoken current/next/countdown label. Home's countdown preference is retained.

## Verification

Evidence under `audit-evidence/idash4-0.6.0-rc1/`. JVM tests cover all six exact boundaries and the preceding instant, timestamp progress, rounded remaining minutes, Syuruk phase, midnight/year rollover, leap day, missing previous and next data. API27 instrumentation exercises six official schedule states, native rendering at 320×140 and 480×180, Home/Drive mounting, motion-off, low mode, hidden, focus loss, pause and detach. The six PNGs are static native rendering fixtures with motion disabled at controlled official schedule timestamps; they are not proof of live animation frame rate. Physical ARM32/1GB performance and API26/device appearance still require device validation.

Build, **73 JVM tests**, lint (0 errors; 89 warnings including the native-only widget constructor warning), APK guard and **191 unique runtime assertions** passed. After the final Isyak mosque-glyph adjustment, PrayerJourneyChecks was rerun successfully on the final APK and all six static previews refreshed (`final-icons/`). Other regression results precede that visual-only glyph change. No hardware frame-rate guarantee is made.

APK SHA-256: `fdfdf5ddbd4d00e20d96df27ae64ecb0cb335614c98cc86378029c3c26cbe094`.
