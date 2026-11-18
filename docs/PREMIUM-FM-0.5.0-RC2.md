# iDash4 0.5.0-rc2 — Premium FM integration

Branch `feat/idash4-0.5.0-rc2-premium-fm`, based on `a1c6e3a` (Drive Dashboard rc1).
VersionCode 14; package retained. Main and shared glass renderer unchanged.

## Source and adaptation

The user supplied `iDash3_Premium_FM_UI_Java_Kit.zip` with `DashFmPanelPremium.java` and a reference illustration. The ZIP was inspected as source material; its suggested model/prompt text was not executed as commands. The code was adapted and compiled against the current project rather than applied as an untested two-line replacement.

`FmPanelHost` selects Premium by default in both Home's FM panel and Drive Dashboard's FM mode. Settings → Panel → **Gaya radio FM → Premium / Classic** changes the skin. The original `DashFmPanel.java` is retained unchanged; both skins share the existing `fm_panel` keys and OEM controller. Reverting the skin does not erase presets or change firmware sources.

## UI and corrections

- Native purple/navy gradients, cached decorative wave/halo, radio glyph, bold auto-sized frequency with smaller MHz suffix and a static tick ruler.
- Start/Stop and Tune outline icons remain next to Auto and options. They are original native drawings rather than redistributed Apple SF Symbols.
- Ten preset buttons remain in two rows, with a selected preset treatment and visible keyboard focus.
- Raised prototype 44dp header targets to 48dp and removed compact 42dp preset rows. Preset targets remain at least 48dp in the tested virtual layouts (560×330, 440×260).
- Centered icon-only controls without reserving an empty text line; long source labels ellipsize.
- Added an explicit Tune header control, tooltips, accessible channel/source/power descriptions, invalid-frequency editor handling and keyboard cleanup on dismiss.
- No full-screen mockup bitmap, per-frame blur, animation loop or new dependency. The existing outer Media panel/header and shared glass material are retained, so this is an adaptive native interpretation, not a pixel-identical rendering of the illustration.

## Behavior retained

OEM source selection, Start/Stop, integer-kHz tuning, 0.1 MHz stepping, reported station choices and diagnostics still delegate to the unchanged `DashFmController` and `DashFmBand`. Presets 1–10 use their existing keys. Pausing/leaving the panel stops any tuner session it owns; host detach also pauses it. Emulator absence of an OEM FM service is reported honestly; the UI does not invent reception or confirm audible audio.

## Verification

Evidence is under `audit-evidence/idash4-0.5.0-rc2/`: build/unit/lint, Premium preset/editor/layout/fallback checks, original Classic FM checks, Drive Dashboard mode/lifecycle checks and Settings regressions. Final Premium follow-up verifies the final APK after icon centering/source-label polish; unchanged-area regressions use the initial installed build.

The Premium checks cover default selection on Home, 10 channel targets at both virtual sizes, ruler presence, old-preset recall, channel 10 save/recall, invalid and valid frequency input, source selection, pause/resume, Classic fallback and Premium on Drive Dashboard. Screenshots are actual emulator captures with its known framebuffer letterboxing; fixtures are not physical-head-unit photos.

Actual OEM RF/audio, antenna behavior, long-duration ARM32 performance and physical touch sizes need head-unit verification. Existing ResolutionStage scaling can make virtual targets smaller physically on a short panel, despite valid design dimensions. Try both Premium and Classic on the target display.

Final results: 56 unit tests and 122 unique runtime assertions passed across four runners. Lint: 0 errors, 88 warnings. Static/APK guard passed. Controller, band, Classic panel and shared glass renderer are byte-identical to the base commit.
