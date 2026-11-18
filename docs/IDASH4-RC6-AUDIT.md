# iDash4 0.4.0-rc6 — compact, disappearing video menu

Branch `fix/idash4-0.4.0-rc6`, based on rc5 `99c0e3a`. Package unchanged; versionCode 11. Main and glass renderer unchanged.

The video `⋯` menu artwork is inset 8dp inside the existing 48dp target, giving a 32dp visible background. The menu and toolbar both hide after 3 seconds without interaction. A touch on the video reveals controls while still forwarding the gesture to the provider. D-pad navigation/Enter also reveals controls, with Back handling preserved.

Keyboard-focused toolbar/menu controls and an accessibility-focused menu remain visible; the timer retries while focus is held. Exit/pause cancel the timer through existing fullscreen cleanup. Fill remains the fullscreen default; Fit is still available.

Verification: targeted offline PanelFullscreenChecks covers timed hide, 48dp hit bounds, touch reveal, repeat hide and D-pad reveal, alongside crop/fullscreen/cleanup regressions. Unit/lint/build and runtime results live under `audit-evidence/idash4-0.4.0-rc6/`. Native provider gestures may also react to a reveal tap (for example showing playback controls); the app does not swallow provider touches. Physical-device/live-stream playback still needs user validation.

Result: 56 unit tests and 30 panel runtime assertions passed. Lint: 0 errors, 63 warnings. Static/APK guard passed.
