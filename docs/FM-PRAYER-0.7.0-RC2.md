# iDash4 0.7.0-rc2 — compact FM and visible prayer motion

Trial branch `feat/idash4-0.7.0-rc2-fm-prayer-motion`; main is untouched. Backup tag `backup/pre-fm-prayer-rc2` preserves rc1.

FM preset capsules are 40dp visually within 48dp touch rows, with 12sp auto-sized labels. Reduced padding, status height and ruler height free space for the frequency. The frequency now scales down to 14sp in short panels rather than forcing a 22sp minimum into a smaller line box. Both Home and Drive use the same premium component. OEM tuning, Start/Stop, source selection, presets and Classic fallback are retained.

JAKIM animates both endpoint icons, plus the central Syuruk sun. The image artwork stays in place with softly moving icon highlights, a slow orbiting light, stronger breathing glow, larger twinkling stars at night and a faster arc light. Countdown and arc progress still use official timestamps, including Syuruk as an additional marker and the overnight Isyak/Subuh transition. No new bitmaps, blur passes or libraries are created per frame.

Motion requires Smooth Transitions ON, a visible/focused/resumed widget and an available timetable. Low performance mode (including Auto when it detects low RAM/slow frames) continues to stop decorative animation. Select Penuh/Full performance to evaluate the complete motion; do not infer ARM32 performance from this software emulator. Hidden/paused widgets cancel their frame callback.

Candidate APK: `dist/iDash4-0.7.0-rc2-debug.apk` (code31).
Rollback APK: `dist/iDash4-0.7.0-rc1-rollback-debug.apk` (code32), same package/signing key, install over rc2 without uninstalling. Future normal updates after rollback need code33+. Backup source differs only in version metadata when building this rollback.

Validation evidence: `docs/audit-evidence/idash4-0.7.0-rc2/`. Physical FM reception, ARM32 frame pacing and head unit GPU behavior require a real unit.

Build, lint and Android test APK succeeded: 78 JVM tests with zero failures, lint 0 errors / 90 warnings, static APK guard passed. New focused instrumentation passed **50 assertions**: complete frequency line at 400×230, 520×260 and 600×340, all ten compact preset visuals within 48dp targets, different real animation pixels on Home/Drive, and callback cancellation for disabled/LOW/hidden states. The first focused test run was interrupted by an emulator System UI ANR obscuring the launcher; its failure is retained. The corrected test waits for window focus before drawing, and passed without weakening the focus requirement. These software-emulator results do not establish head unit FPS.

Existing PremiumFmChecks passed **55 assertions**, covering preset recall/save, editor validation, source selection, lifecycle and Classic/Drive host switching. PrayerJourneyChecks passed **59 assertions**, including six official phases, image bounds, true timeline progress and animation cancellation. Total scoped native assertions: **164**. No RF/audio output claim is made for the emulator.

Final native previews passed the focused-window capture gate and were visually reviewed: [Home FM](audit-evidence/idash4-0.7.0-rc2/home-panel-native-1024x600.png), [Drive FM](audit-evidence/idash4-0.7.0-rc2/drive-fm-native-1024x600.png). Both show the complete frequency and smaller preset capsules without a system dialog. Previews disable motion for settled still captures; animation itself is verified by the changing-frame instrumentation above.
