# Scoped emulator results

API27 x86_64. Native app-view fixtures; no physical head-unit performance claim.

| Class | Result | Assertions |
|---|---|---:|
| PageGlassChecks | PASS | 25 |
| AppLibraryChecks | PASS | 17 |
| DriveDashboardChecks | PASS | 24 |
| DashboardLayoutChecks | PASS | 71 |
| ClockGlassChecks | PASS | 61 |

Unique assertions: **198**.

All five classes completed PASS. Editing the harness default list during execution caused a summary-phase shell parse error after the gate loop; bash -n succeeds on final harness. Latest PageGlass fixture export rerun recorded separately.
