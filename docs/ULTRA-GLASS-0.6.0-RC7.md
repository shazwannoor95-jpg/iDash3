# Supplied ULTRA Liquid Glass update — 0.6.0-rc7

User requested the ULTRA ZIP update. Candidate branch `feat/idash4-0.6.0-rc7-ultra-glass`, based on rc6 `069a587ef5493d6511ac90c0f5d040149e671800`. Version code 26.

## Scope and provenance

Imported all six Java classes and the unchanged `ultraglass/mosque_road.webp` from `docs/imports/iDash3_ULTRA_LiquidGlass_Drive_Apps.zip`. Its reference screenshots, integration snippets and prompts were treated as reference material and adapted to current native Drive. Older two-row toolbars, the iDash4 header label and obsolete split-screen hooks from that reference were not restored.

Only Drive/Apps receive the ULTRA scene and prism/halo/glint overlay. The existing PageGlassSkin hook is now a facade to a single ULTRA implementation; the previous scenic/rim implementation is no longer run alongside it. Home/JAKIM/FM artwork, native map SDK, rail modes, app icons, search, pagination, favorites and click handlers retain their existing implementations. The rc6 one-row toolbar and padded dock stay intact.

## Adaptations to prototype

- One Activity session updates visible overlays, targeting the kit's 16ms interval. Smooth Transitions and system animator scale are respected. Pause, loss of focus, detach/hide, page replacement and destroy stop the corresponding animation. Session bindings use weak references and dispose/restoration, preventing duplicate layers and callbacks after filtering/re-render.
- Rounded corners and inset capsule bounds come from existing GlassSurface backgrounds. Custom foreground focus/ripple remains topmost; known legacy neon rims are replaced, not stacked. Map and WebView internals are excluded from traversal.
- Gradients, paths and halo caches rebuild on bounds changes, not every draw. Halo bitmap dimensions are bounded to 768×512; the supplied Drive bitmap is 1009×551 (~2.12MiB decoded). Bitmap retirement is left to GC to avoid recycling a RenderThread resource in flight.
- Android 13 RuntimeShader and optional Android 12 RenderEffect calls are isolated in version-gated classes. AGSL colour is premultiplied correctly; caustics are restricted to the border to preserve readable content. Optional blur preview is included but not installed over live map/text.
- ULTRA intentionally retains its maximum animation profile, independent of automatic GlassPerf quality gating; the existing underlying glass material's own policy is unchanged. A 16ms callback target is not a measured 60fps guarantee.

This is an animated rim/static scenic treatment, not live-map backdrop refraction/blur. Exact reference-image pixel identity is not claimed.

## Backup / rollback

Pre-update rc6 is preserved at tag `backup/pre-ultra-glass-0.6.0-rc6`, source archive and original APK under `/workspace/backups/idash4-pre-ultra-rc6/`, and its original branch/APK remain available.

`dist/iDash4-0.6.0-rc6-rollback-debug.apk` is built from that exact source archive with only version metadata changed: code **27**, name `0.6.0-rc6-rollback`. It can install over candidate code 26 without an uninstall. Both are debug-signed with the same certificate. A future normal forward update after rollback needs code **28+**. Tests use `adb install -r -d` to put the code-26 candidate back after verifying rollback.

## Validation and limits

Results are under `audit-evidence/idash4-0.6.0-rc7/`. Scope includes old-API class loading, session/visibility/focus/pause/resume, no layer stacking, drawable bounds/alpha, real map preservation, search/paging/favorites, rc6 toolbar/dock bounds, and Home card separation. For the live-motion test, UI work is synchronized with runOnMainSync, not an unbounded waitForIdleSync while continuous animation is enabled; the initial idle-wait attempt was stopped and the harness corrected. Geometry/interaction tests disable motion.

Native preview screenshots are captured from the software API27 emulator; Drive map tiles may be offline. No physical head-unit FPS, radio RF, GPU-memory or streaming performance claim. Only API27 is installed here: Android13+ AGSL and Android12+ RenderEffect are compiled/linted and version-guarded, but their modern runtime paths have **not** been exercised. Initial software-emulator boot also showed a System UI ANR before the app tests; its dialog was dismissed before testing.

Build, **76 unit tests**, lint (**0 errors; 90 warnings**), APK guard and **528 unique runtime assertions** passed. The assertions count ticker advancement/lifecycle, not GPU presentation FPS. Rollback source verification compared all 717 other tracked files byte-for-byte with rc6; only version metadata differs.

In-place rollback code 27 and test-only forward reinstall code 26 both succeeded with identical full preference-file SHA-256 before/after each install. Settled native previews include Apps, Drive (offline map tiles), and the unchanged Home + FM panel.
