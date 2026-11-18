# iDash4 0.6.0-rc3 — Cutout, motion and clock/glass

Branch `fix/idash4-0.6.0-rc3-glass-clock-motion`; base `18c7437`; versionCode 21. Same package, neutral ABI, API26+. Main is unchanged.

## Prayer artwork and animation

The existing five WebP files are byte-identical to rc2. A cached rounded cutout path and tighter per-scene content crop exclude the exterior rectangular black matte and lower external reflection area during native Canvas rendering. The dark sky **inside** the glass card stays intact; it is part of the illustration. The unpainted exterior/corners reveal the app wallpaper rather than a black rectangle. Cropped card bounds preserve aspect ratio and keep the neon border/artwork; no new image generation or replacement illustration is used.

Motion adds a short comet trail along completed progress, a clipped slow light sweep within the countdown capsule, subtle night/sunset star twinkles, and slightly stronger active-icon breathing. All new effects share the existing ~15fps decorative loop and its Smooth Transitions/system animation scale/GlassPerf/visibility/focus/lifecycle gates. Low mode remains static artwork with real time updates. No per-frame bitmap allocation or blur is introduced.

## Clock/date fit

The clock card has a 104dp minimum, vertical centering and at least 8dp vertical padding in the compact profile. HomeWidgets first fits text/spacing, then reduces the prayer artwork's 160dp preferred height toward a 96dp floor when necessary. A short landscape viewport therefore does not center an overflowing column and cut TODAY/date text. Normal, 480dp-height and compact 400dp-height layouts plus 1.3× fonts are checked with actual Android text layout metrics.

## ZIP integration scope

The user authorized only Liquid Glass pieces from `iDash3_Neon_LiquidGlass_Home_Java_Kit (1).zip`. The documentation and Java sources were read as reference; their broader Home-layout/model/script instructions are not the user's requested scope. Only **NeonGlassRim.java** was imported, unchanged. Its foreground gradient rim, layered transparent strokes, inner white edge and top reflection augment the existing GlassSurface on the clock, Home header controls, panel action controls and Home dock. The glass/frost/refraction engine remains in place. No NeonHomeScene, NeonPrayerJourney, PrayerScenicModel, NeonDateMiniCard, replacement landscape, installer script or new library is installed/executed.

## Verification

Evidence under `audit-evidence/idash4-0.6.0-rc3/`. Native instrumentation checks clock/date geometry and ink bounds at 1024×600, 800×480 and 800×400, font scales 1.0/1.3, the imported rim, image exterior alpha, phase rendering and motion-stop policies. Emulation does not certify ARM32/1GB hardware frame rate or RF/video behavior.

Build, **76 unit tests**, lint (0 errors; 89 warnings), APK guard and **258 runtime assertions** passed. APK SHA-256: `45b48497cf04abb12d8390df878877073c44b27e545a512b5d0f378ab841c744`.

Final Home capture: `screens/home-1024x600.png` is an unedited emulator screenshot opened by normal UI launch/taps after the timetable loaded. The phase PNGs are static native Canvas fixtures with transparent exteriors. No physical-head-unit or live-animation frame-rate claim is made.
