# Compact Apps toolbar and padded dock — 0.6.0-rc6

Based on rc5 (`4d3d670`), on branch `fix/idash4-0.6.0-rc6-apps-toolbar-dock`. Version code **25** also allows normal update over the previous rc4 rollback APK (code 24).

The Apps page previously placed Panel/Customize/Control Center on one row and Search/Refresh/Done below. All six controls now share the top row, with smaller type and inset capsule backgrounds while retaining 48dp-high touch targets. Existing search/refresh/Done callbacks are preserved. Removed the iDash4 text label from the shared header, including Home.

The shared dock now reserves 8dp vertical and 12dp horizontal shell padding. Favorite and Settings/Drive/Apps tiles use matching inner padding, icon-to-label spacing and font-aware label heights. Dock height grows to contain the small/large icon option and larger system fonts. Shared glass materials and the rc5 Drive/Apps scenic skin are preserved.

Regression scope: one-row geometry, six target bounds, non-overlapping dock tiles, icon and label clearance, search toggle/filter/Done; 800×480 and 1024×600, font scales 1.0/1.3, both dock sizes. Also run existing AppLibrary, ClockGlass and PageGlass checks. Published preview PNGs are settled native emulator screenshots (not physical head-unit captures), including the current **Shazwan's Saga Home with FM panel open**.

Original pre-skin backup remains tagged `backup/pre-drive-apps-skin-0.6.0-rc4`; rc5 remains on its own branch. No changes to main.

The first compact Home regression check exposed insufficient widget height at 800×400 / font 1.3 after enlarging dock clearance. For larger fonts, Home now reclaims vertical inset and greeting spacing (greeting capped at 15sp (14sp minimum)), keeping the existing 14dp card gap and prayer artwork minimum. The failed initial result is retained; the corrected result is under final-home-recheck/.

Settled native-window screenshots are captured separately from geometry fixtures; use apps-native-1024x600.png and home-panel-native-1024x600.png for appearance review.

Final verification: 76 unit tests, lint 0 errors / 89 warnings, APK guard, 493 unique runtime assertions. A temporary API27 autosize min/max error during compact-header development was corrected before the final passing Home run. The software emulator also showed a System UI ANR during the first native screenshot capture; that capture was discarded and recaptured after dismissing the System UI dialog. No physical head-unit performance claim.
