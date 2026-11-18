# iDash4 0.4.0-rc7 — FM controls and stronger icon colors

Branch `feat/idash4-0.4.0-rc7-fm`, base rc6 `023db25`. VersionCode 12; package unchanged. Main and glass panel renderer remain unchanged.

## FM

- Header: original outline Start/Stop and Tune glyphs beside Auto and options. These are iOS-inspired native control drawings, not imported SF Symbols or artwork from the app icon pack. Hold/tooltip/accessibility names identify their function; the active power control uses a stop mark.
- Frequency is centered between minus/plus tuning controls. Ten presets appear directly underneath in two rows of five; compact layout no longer hides them.
- Existing zero-based `preset_0`–`preset_5` values are retained. New slots `preset_6`–`preset_9` use the same integer-kHz format. Empty slots are shown as empty; no station names or frequencies are invented.
- Tap a saved channel to select its frequency. Hold any channel to save the current frequency. Selection uses a thicker outline. Existing source selection, manual tune, start/stop and hardware protocols remain unchanged.
- Stations returned by the OEM service remain accessible through options. Emulator tests do not verify RF reception or audible playback.

## Icon appearance

The screenshot's pastel look came from the upstream Liquid Glass PNGs. App Library did not lower their alpha in response to head-unit connection; no such synchronization governs these icons.

The shared decoder now applies an RGB-only saturation/contrast adjustment before caching. Original PNGs and provenance hashes remain unchanged, as do bitmap dimensions, alpha and the 1 MiB cache bound. App Library, dock and driving workspace use the same stronger decoded colors. This does not recover original pre-transformation artwork or remove deliberate glass highlights. Decode allocation failure falls back to the original artwork. Start FM is no longer deliberately dimmed when idle.

## Validation

Build/unit/lint and offline API27 x86_64 instrumentation results are under `audit-evidence/idash4-0.4.0-rc7/`. FM checks cover layout at 560×330 and 440×260, ten visible 48dp targets, legacy preset recall, channel 10 save/recall and persistence across panel recreation. Icon checks verify original asset hashes, decode/cache bounds and stronger RGB while preserving alpha. Screenshots are emulator fixtures, not evidence of real RF audio. Very short physical panel areas may still need layout adjustments; the existing FM virtual stage can scale controls below their design size on small panels.

Final results: 56 unit tests and 231 runtime assertions passed (FM 36, icons 195). Lint: 0 errors, 65 warnings, including existing/native layout and localization warnings. Static/APK guard passed. Screenshots capture the emulator's actual letterboxed framebuffer, not the physical head unit.
