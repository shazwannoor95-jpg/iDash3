# BitChord native host source

Upstream: https://github.com/kushagrasinghx/BitChord
Pinned commit: acf219a7e4a7f52f026d4336d5c7ac5cf7bfafcc (1.9.1)
License: GPL-3.0 (UPSTREAM-LICENSE). Original iDash MIT grant remains in LICENSE-MIT.

`app`, `shared`, `sharedUi`, `native` and `wearos/protocol` are source copies at this pin. Build output, local credentials and standalone app signing configuration are excluded. The Android app module is built as a library and hosted by iDash's AppCompatActivity and a ComposeView, rather than launching the upstream Activity. MainActivity.kt retains the original private BitChordApp root; BitChordEmbedded exposes it within measured parent constraints.

Host changes:

- Lazy, idempotent Application initialization when the native panel or its own playback service is first needed.
- Finite native layout constraints for Compose scrollables, including the provisional pass made by Android weighted rows.
- Actual upstream Compose navigation, repositories, Media3 service and player. Same-process platform MediaSession token feeds the existing left media card; no notification listener grant is needed for this owned session.
- Android Activity lifecycle/saved-state/Back dispatcher on the host, composition disposed on source exit. Upstream player can continue through its foreground playback service when the user switches back to the map.
- R8 and resource shrinking enabled using upstream reflection/JNI rules and explicit stable host/media interfaces.
- Standalone BitChord AppWidget providers and refresh hooks excluded; the iDash native media card supplies controls.
- Standalone BitChord APK update checks/download/installer disabled in the host. Watch APK bundling and ADB installation excluded; the upstream availability check hides this entry when no watch APK exists.
- Public active-user presence reporting disabled for the host. Debug sink disabled. Existing authentication and network features otherwise remain user operated.
- Image memory cache bounded at 12 MiB; disk artwork cache 100 MiB; host default audio disk cache 256 MiB (minimum 128 MiB); looping canvas video off by default. No forced display refresh rate in the embedded root.
- Proprietary SF Pro assets and SF Symbols vectors excluded from the host variant. Inter (OFL) and Apache-2.0 Material icons occupy the original typography/icon slots. Original navigation and player structure remain native Compose.
- Build stack AGP 9.3.2, Kotlin 2.4.20, Gradle 9.7.1 and compile SDK 37. Runtime minimum remains API 26. ARM32 and x86_64 native outputs are included for hardware/emulator validation.

## Source-built dependencies

JitPack returns HTTP 403 in the build environment. Pinned public sources were built locally; the resulting artifacts and publication metadata are in `third_party/maven`, with corresponding source archives and rebuild notes in `third_party/source`. Gradle uses that checked-in repository before Google/Maven Central; a user's ~/.m2 cache or this workspace's absolute paths are not required.

- NewPipeExtractor v0.26.3, caae86c943857cc6e1a762e3488d6a14e9cf7800, GPL-3.0-or-later. Source target remains Java 11; actual Java 21 compiler used. Its original Utils class is stripped by the upstream build task because BitChord supplies its patched version.
- nanojson e9d656ddb49a412a5a0a5d5ef20ca7ef09549996, MIT.
- InnerTubeX v0.7.0, 4a2d2926145ebadc9ac0a317f54e3f5efc370378, GPL-3.0. Published under source project's group `com.github.MetrolistGroup`; dependency coordinates adjusted from JitPack's nested group. All tests, ktlint, API compatibility and assembly passed during source build.
- Existing upstream libffmpegJNI binaries are paired with the upstream native/ffmpeg/build.sh, JNI source and exact librempeg source 8e8b221e207f685119fcfe276ae28c94ff6f5aa0. The script documents GPLv3 configuration and codec set.
- Oboe 1.11.0 (Apache-2.0), libusb 1.0.27 (LGPL-2.1), Signalsmith Stretch/Linear (MIT), DST and bit-perfect adaptations: retain native source/license notices and exact CMake source hashes. LGPL code remains rebuildable/relinkable; build instructions are supplied.

No private device identifiers, cookies, credentials or real library/account content are included in this source copy or audit evidence.
