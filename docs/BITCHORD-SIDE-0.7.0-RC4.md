# BitChord full app beside the dashboard — rc4 trial

The user clarified that the complete BitChord UI should remain visible beside JAKIM, not just the remote transport panel. rc3 only supplied remote controls. This trial adds Android's native split-screen pairing. It is not a child View embedded inside iDash: Android owns two Activity windows and the divider. Exact positioning and divider limits depend on firmware.

## Use on API27 TEST9278B

1. Keep the official BitChord Android phone ARM32 APK installed.
2. Drive → B → **UI penuh sebelah dashboard**.
3. Read the explanation and enable **iDash split actions** in Android Accessibility. Return to iDash.
4. Press B again. A separate resizable iDash dashboard Activity docks on the left, then the installed BitChord Activity opens adjacent on the right.
5. Drag the Android divider to adjust the ratio. Android may place the panes differently on a particular OEM ROM.

The normal full-screen launcher is not the docked Activity. The paired dashboard allocates no map, WebView or extra player; it shows the rail, prayer, media and date cards. Unrelated source icons are hidden in this pane. Its countdown and media callbacks continue while visible but paused by Android's single-focus multi-window lifecycle. Decorative animation still follows existing focus/performance restrictions. Home returns to the normal launcher. Back closes the paired dashboard; Android controls how the remaining window expands.

This Android API26–30 trial uses a minimal AccessibilityService solely for explicit global split/Recents actions. It requests no event types and cannot retrieve window content. There is no root, overlay, foreground service, task embedding, screen capture or audio decoding in the product. The user must enable the service themselves. The explanation is shown before opening accessibility settings; no automatic grant is attempted in the app. If the ROM rejects docking, the app reports it and offers manual Recent Apps pairing or return. Modern Android versions are directed to manual system split.

The source guard now permits the public native split-screen global action constant that this explicitly requested feature needs; the VirtualDisplay/ActivityView/other embedding guard remains. Historical notes saying split actions were removed apply to older versions.

## Version and rollback

Branch `feat/idash4-0.7.0-rc4-bitchord-side-ui`; backup `backup/pre-bitchord-side-rc4`. Main is untouched.
Candidate `dist/iDash4-0.7.0-rc4-debug.apk`, code35. Rollback `dist/iDash4-0.7.0-rc3-rollback-debug.apk`, code36, same package/key and prior rc3 production source with version metadata changed. Install over the trial; do not uninstall if keeping settings. Disable the optional iDash split actions service after rollback. Normal subsequent updates need code37+ after installing rollback.

## Validation limits

Physical TEST9278B multi-window behavior and real BitChord rendering/streaming remain to be tested. Root reported in the user's screenshots was not used or relied upon. Any external test fixture shown in emulator evidence is explicitly labelled and is not the BitChord app; it is never bundled in the downloadable APK.

Validation: build, 79 JVM tests, lint and static APK guard passed. Existing BitChord native integration (34) and Drive flow (42) checks passed during this change. Final system pairing passed **9 assertions** against the final APK with two distinct top visible task windows and a continuing clock while paused/visible. The complete native image was visually checked: [system split test fixture](audit-evidence/idash4-0.7.0-rc4/system-split-test-fixture.png). This image intentionally shows a labelled test Activity, not BitChord. The synthetic APK was removed and the emulator's prior accessibility settings restored afterward. Test fixture source and harness assumptions are documented in `tools/ci/fixtures/side-app/`.

The first smoke attempt lost its bound service when instrumentation restarted the target process. The test now retains accessibility services and explicitly rebinds after that restart. An intermediate smoke passed weaker state assertions but its screenshot showed incorrect placement; that result is retained as **incomplete**, not accepted proof. The final check additionally requires both apps to be top visible tasks and visual review. Launch ordering now waits for docking and launches the second app through the application context into the system's companion workspace, avoiding the adjacent-launch flag during the Recents transition.
