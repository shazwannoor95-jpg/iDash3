# iDash4 0.4.0-rc4 — fullscreen viewing area

Branch: `fix/idash4-0.4.0-rc4`; base rc3 `351f8d8`; package unchanged, versionCode 9. Main unchanged.

The user's rc3 screenshots show working playback/custom fullscreen but a small centered image and a permanently reserved control row. Different website layout presets are not a fullscreen video sizing control.

## Change

- Custom video now receives the entire fullscreen overlay height. Exit/Expand/Fit controls float over it instead of reserving 48dp below the video.
- Controls hide after 3 seconds when they do not hold keyboard focus. Tap the video or the persistent 48dp `⋯` button to reveal them. Native web/video touch dispatch is preserved.
- **Muat** keeps the provider's normal fit behavior. **Isi · potong tepi** applies a uniform, reversible zoom to the enclosing video host, clipped to the viewport. It uses the HTML video's reported intrinsic aspect ratio where available; falls back to 16:9 and limits zoom to 4× for extreme geometry. No provider child views are resized individually and the supplied custom view's transforms are not changed.
- Fill can crop pictures, subtitles and the provider's own controls. Fit is the default each time custom fullscreen starts. Encoded black borders or provider-specific sizing can still leave bars; this is not automatic black-border detection.
- Recompute the fill transform after resizing/expanding. Exit/pause remove the floating control timer and detach the video before the provider callback.
- Layout selection while custom fullscreen is active asks the user to exit video first. Layout settings continue to control the website only.
- Glass renderer, material selection and Classic fallback unchanged.

## Verification and limits

See `audit-evidence/idash4-0.4.0-rc4/`. Offline panel checks exercise full-height geometry, reversible uniform zoom, fullscreen expansion, flag restoration and callback cleanup. Unit checks exercise aspect ratios, invalid input and the zoom bound. Existing rc2 regressions cover the independent launcher behavior.

These checks do not prove YouTube codec/network behavior or the visual content of the provider's real decoded video. User verification is still needed with the same video in both Muat and Isi, on the emulator and head unit. An aspect-preserving fit can legitimately leave bars when video and panel proportions differ. No claim of full-frame visibility while using crop.

Maps update-required and native YouTube error 400 remain separate, unresolved compatibility issues. This version addresses the website player's viewing area and controls.

Verification result: 56 unit tests, 0 failures; lint 0 errors / 63 warnings; 45 targeted runtime assertions passed. Static/APK guard passed.
