# iDash4 0.4.0-rc5 — crop by default

Branch: `fix/idash4-0.4.0-rc5`; based on rc4 `914e501`. VersionCode 10, package unchanged. Main and glass renderer unchanged.

Per user request, new custom fullscreen sessions start in **Isi · potong tepi** rather than Muat. The existing uniform aspect-based fill transform now applies automatically. Tap `⋯`, then the sizing control, to return to **Muat** and retain the full picture. Each new fullscreen session starts in Isi; the temporary toggle is not persisted.

The crop may remove picture edges, subtitles or provider controls. Embedded black borders inside the source video are not automatically detected. Existing aspect-ratio fallback and maximum zoom bound remain unchanged.

Verification: build/unit/lint results in `audit-evidence/idash4-0.4.0-rc5/build-results.json`; offline emulator PanelFullscreenChecks verifies initial crop geometry and switching Muat → Isi, in addition to cleanup/expansion. This does not certify live YouTube playback or physical-head-unit behavior. Maps/native YouTube compatibility issues are unaffected.

Result: 56 unit tests and 25 panel runtime assertions passed; lint 0 errors / 63 warnings; static/APK guard passed.
