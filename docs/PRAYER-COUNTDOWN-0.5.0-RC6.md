# iDash4 0.5.0-rc6 — Prayer countdown formatting

Branch `fix/idash4-0.5.0-rc6-prayer-countdown`; base `2899a6e`. versionCode 18.

The Drive JAKIM card formats remaining time as hours and minutes from 60 minutes onward: 445 minutes → `7h 25m to Subuh`, 285 minutes → `4h 45m to Subuh`, 60 minutes → `1h 0m`. Below one hour the existing `20 m to Asar` style remains. The real schedule, zone selection, next-prayer calculation and Home formatter are unchanged.

Five JVM cases cover zero, 59/60-minute boundary and both reported conversions. Final build/unit/lint/guard evidence is recorded in `audit-evidence/idash4-0.5.0-rc6/`. No new emulator or physical-head-unit run is claimed for this text-format change. The shared glass renderer and YouTube player are unchanged.
