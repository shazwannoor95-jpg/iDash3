# iDash4 0.4.0-rc3 — panel layout follow-up

Branch `fix/idash4-0.4.0-rc3`, based on rc2 (`a4004df`). Package retained; versionCode 8. Main remains unchanged.

User evidence: Android Studio emulator screenshots; user reports the same issues on the head unit. Maps explicitly requests an update; native YouTube reports server error 400, whereas the website player displays content. These do not establish that split-screen itself failed.

## Changes

- The selected virtual browser layout now uses both width and height with uniform fitting. Previously only CSS viewport width was changed while browser height remained the short panel height. Fixed layouts can display smaller website controls, but no longer treat a wide CSS viewport as if its full height fit the panel. Website content can still require scrolling.
- Replace misleading 360p/480p/540p/720p captions with actual layout dimensions. These are not video-stream quality controls. Existing saved indices retained. Default for a missing preference is the smaller 640×360 layout; existing selections are not overwritten.
- During website fullscreen, hide the ordinary Back/Search/Home/Stop row, search/status and underlying browser stage. Restore their visibility on exit; the fullscreen Exit/Expand toolbar stays available. There is one browser and one video surface.
- App Library keeps 4×2 with slightly smaller labels and tighter icon-label spacing, reserving more height for icons without reducing tile hit areas.
- Hold the selected app card on Drive to view installed app version and Android API, with an explicit App info action. Does not install, downgrade, reset or accept browser terms automatically.
- Glass renderer/material and Classic fallback unchanged. Settings layout is otherwise retained; no new claim of fixing the Settings spacing from screenshots.

## Validation

Build, unit tests, lint and targeted API27 emulator results are recorded under `audit-evidence/idash4-0.4.0-rc3/`. Panel checks cover fixed browser geometry, proportional fit and toolbar visibility/cleanup during fullscreen. These offline fixtures do not prove live YouTube playback or Google's service compatibility.

## Device follow-up

- Try Layout 640×360 and website fullscreen; compare with Auto. Check browser scrolling, touch positions, fullscreen/expand/exit and background return. The outer glass header and dashboard remain visible in panel fullscreen; Expand requests whole-screen video.
- Maps: inspect installed version through the Drive card. Use a supported update offered for the actual device, or explicitly select another compatible navigation app. An update-required server response cannot be fixed by launcher layout.
- Native YouTube error 400 still needs installed-version/service investigation. The website panel is a separate playback route; it does not repair the native app.
- Physical head-unit performance, Maps search and live YouTube streaming remain unverified by this environment.

Final verification: 51 unit tests passed; lint 0 errors / 63 warnings; 58 assertions passed across PanelFullscreenChecks (19), AppLibraryChecks (17) and Rc2Checks (22) on the emulator. Static/APK guard passed.
