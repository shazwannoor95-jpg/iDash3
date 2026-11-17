# 0.2.1-rc1 — crash/lag fixes without Liquid Glass changes

Branch: `fix/crash-lag-no-glass`. Based on audit commit `c44bd22`; main is not merged or updated.

Changes:
- Custom wallpaper bounds/pixel allocation errors become IOException. Import handles allocation failure, and a failed active wallpaper decode preserves the previous image instead of publishing an empty replacement.
- App discovery catches package-query/icon allocation failures and keeps the previous list. It no longer allocates another bitmap after an OOM. Repeated refresh requests coalesce into at most one follow-up scan, and completion only rebuilds Home or updates the Apps list, not Settings/Drive.
- JAKIM cache/network work has a separate single worker, shut down on Activity destruction. Network waits no longer occupy the existing worker used by image loading and radio controls.
- Drive package scans and both icon sizes are prepared on a service worker, using the same icon rendering parameters. Missing packages are skipped. Existing snapshots survive scan failure, and request generations prevent publication after service destruction or a cancelled app-sheet opening. Shortcut targets outside the discovered launcher list are also resolved off the main thread.

Liquid Glass boundary:
- No changes to GlassBake, GlassSurface, GlassBackdrop, GlassPerf, GlassAppearance, IosStyle, IconStyler, IosGlyphs, Spring/SpringDriver, Elastic, MorphOverlay, AppearanceEditor, Squircle, resources or assets.
- Existing successful wallpaper preparation/publication and normal icon rendering parameters are retained. Only failure handling/scheduling/data refresh changed at their callers.
- Audit A2 (glass bake queue), A3 (Auto performance), A7 (glass cache trim), and full-view navigation rebuilding remain outside this fix. This build does not claim all lag is solved.

Validation: 47 unit tests passed (0 failures/errors/skips), app lint 0 errors / 59 warnings, APK guard and signature verification passed. CrashSafetyChecks passed 10 assertions and SmokeChecks passed 57 assertions. The first CrashSafetyChecks attempt failed at network-worker startup readiness with a 5-second timeout; the rerun uses 45 seconds to drain initial JAKIM work on the software emulator, while worker-isolation assertions remain required. StabilityChecks passed 104 assertions across eight wallpaper/Appearance cycles. All three runners reported INSTRUMENTATION_CODE: -1 and stream=PASS. Weak-reference observations after the stress run were 0/8 old wallpapers and 1/8 editors (the runner may retain its last editor on the stack; this is informational, not a leak verdict).

Validation details and completed instrumentation outputs are in `audit-evidence/0.2.1-rc1/`. Emulator is API27 x86_64 TCG/SwiftShader without KVM; it cannot certify Cortex-A7/Mali-450 performance or reproduce every low-memory/OEM crash. OOM decode tests inject the error rather than exhausting the entire emulator heap.

APK is debug signed and versionCode 2. The signing certificate differs from the old committed QA APK, so an in-place update from that APK may be rejected. Preserve settings before replacing an installation; do not automatically uninstall a user's launcher. Keep the stock launcher available when testing on the head unit.
