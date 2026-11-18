# iDash4 0.6.0-rc4 — Home card separation

Branch `fix/idash4-0.6.0-rc4-card-spacing`; base `550cbd6`; versionCode 22. Main unchanged.

HomeWidgets previously reduced the top margin of the prayer card to zero in its most compact profile. The cards could therefore touch visually even though the previous overlap assertion (`clock.bottom <= prayer.top`) passed.

All profiles now reserve a minimum **14dp** card gap. Preferred Home prayer-view height is reduced from 160dp to 156dp and the existing available-height calculation resizes it further when needed, retaining the 96dp floor. Clock padding, 104dp minimum, legible text floors, photo aspect ratio and neon rims remain intact. No changes to prayer data, animation, artwork assets, shared glass engine, Drive or media players.

## Validation

ClockGlassChecks now requires the actual 14dp gap, full card bounds and unclipped clock text at 1024×600, 800×480 and 800×400 with 1.0×/1.3× font sizes. Native app-view fixtures are rendered with Android Canvas at each tested size; these are not screenshots of a physical head unit. Prayer rendering/lifecycle regression checks and build/unit/lint/guard results are recorded in `audit-evidence/idash4-0.6.0-rc4/`.

Build, **76 unit tests**, lint (0 errors; 89 warnings), APK guard and **120 runtime assertions** passed. Six actual native app-view renders are under `screens/`; font10/font13 mean 1.0×/1.3×. APK SHA-256: `3c57b7abbf2d170e4eced51d74b2e92399a7dca459bf2c5227b7d181f70f5e2f`.
