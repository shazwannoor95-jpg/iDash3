# Consolidated rc2 verification

Earlier failed attempts are retained for traceability. The evidence column identifies the successful final result for each check. Unchanged-area regressions use the first installed rc2 build; changed library/panel paths and QualityChecks use the final APK.

| Check | Assertions | Evidence |
|---|---:|---|
| MaterialContrastChecks | 36 | MaterialContrastChecks.txt |
| AppearanceChecks | 62 | AppearanceChecks.txt |
| MaterialSwitchChecks | 13 | MaterialSwitchChecks.txt |
| PanelFullscreenChecks | 14 | final-followup/PanelFullscreenChecks.txt |
| AppLibraryChecks | 17 | final-followup/AppLibraryChecks.txt |
| StabilityChecks | 104 | StabilityChecks.txt |
| DrivingWorkspaceChecks | 33 | DrivingWorkspaceChecks.txt |
| SettingsChecks | 27 | SettingsChecks.txt |
| CrashSafetyChecks | 11 | CrashSafetyChecks.txt |
| QualityChecks | 50 | final-followup/QualityChecks.txt |
| Rc2Checks | 22 | final-followup/Rc2Checks.txt |
| AppearancePersistenceChecks-write | 3 | final-followup/persistence-write.txt |
| AppearancePersistenceChecks-read | 6 | final-followup/persistence-read.txt |

Unit: {'tests': 51, 'failures': 0, 'errors': 0, 'skipped': 0}. Runtime assertions (unique per check): 398.
APK SHA256: 1d31c9c2f39a60387122f71294c0addee78b197583d935a5c9d06f6d5ee6f99a

Physical Maps search, live YouTube streaming and ARM32 performance remain unverified. Screenshots from AppearanceChecks show the actual 640×600 emulator capture with letterboxing; they are not native 1024×600 head-unit photographs. Manual library geometry checks cover 1024×600 and 800×480 layouts.
