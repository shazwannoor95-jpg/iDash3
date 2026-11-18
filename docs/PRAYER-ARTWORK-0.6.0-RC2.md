# iDash4 0.6.0-rc2 — Reference-image prayer artwork

Branch `fix/idash4-0.6.0-rc2-original-artwork`; base `9122ea9`; versionCode 20. Shared glass renderer, media engines and prayer timeline remain unchanged.

## Visuals

The five user-provided image references are now the visual basis, replacing rc1's simplified vector landscapes/icons. Image editing produced clean plates retaining the detailed Liquid Glass frames, cyan/purple/gold glow, sky, clouds, mountains, mosque silhouettes and icon artwork while removing baked names, sample times, countdowns and arcs. These are edited derivatives, not byte-identical original files. Remaining decorative chevrons follow each clean plate.

The application draws real zone-specific names, event times, countdown, arc, nodes and indicator over the images. No example times from the references are shown as data. The original landscape composition and per-image aspect ratio are retained without cropping or stretching; the taller night image fits inside the card rather than cutting off its frame. Home and Drive JAKIM cards use the images' own glass frames instead of an extra glass background. Other glass surfaces stay unchanged.

Dawn is one Subuh→Zohor scene with Syuruk at the middle node. Subuh→Syuruk fills its first half, Syuruk→Zohor fills the second half. Syuruk is explicitly labeled as an additional marker. The countdown targets the next actual event. Other scenes cover Zohor→Asar, Asar→Maghrib, Maghrib→Isyak and Isyak→next-day Subuh. rc1's MYT timeline and exact event/midnight behavior are reused.

## Native motion and memory

Native Canvas overlays provide the progressing arc, travelling light, soft active-node glow, gentle source-pixel icon breathing and 450ms scene/content crossfade. Icons remain the raster artwork from the images; they are not replaced with new outline icons. The entire sky/landscape is not a continuously moving texture. Only the active icon region receives the subtle breathing overlay. Smooth Transitions, system animation scale, GlassPerf low mode, visibility, window focus and Activity lifecycle still gate the animation loop (~15fps cap). Low/reduced-motion mode retains the full detailed artwork and real countdown, with decorative frames stopped.

Five WebP assets total approximately 648 KiB, encoded at quality 93 without source resizing. BitmapFactory samples scene changes to at most 900px width, typically below 2 MiB per decoded ARGB bitmap. Only current/previous scene references are retained for a fade; detached views release them for GC. No decoding, resizing or disk IO occurs in onDraw, no bitmap blur and no new dependency/WebView is added. Data source, zone picker and refresh access remain unchanged. Missing schedule boundaries still show an explicit incomplete state.

## Verification

Evidence: `audit-evidence/idash4-0.6.0-rc2/`. JVM tests include the six-to-five scene mapping and continuous/bounded two-half Syuruk arc. Instrumentation verifies official phases, half-interval progress, real image decoding bounds and rendering at 320×160/480×260, plus motion-off/low/hidden/focus/pause/detach behavior. Static previews use controlled official timestamps with animation disabled; they are real Android image-plus-overlay renders, not Photoshop composites or live-video proofs. Head-unit ARM32/1GiB frame rate has not been measured.

Build, **76 JVM tests**, lint (0 errors; 89 warnings), APK guard and **209 unique runtime assertions** passed. After the final zone-label readability adjustment, PrayerJourneyChecks was repeated successfully on the final APK and all six native PNG previews refreshed (`final-footer/`). Other regression results precede that text-size-only adjustment. Final APK SHA-256: `e9bbdbdceb97278d34c0a17ea20ca19639e904de374989a4df6be7160f4d1c63`.
