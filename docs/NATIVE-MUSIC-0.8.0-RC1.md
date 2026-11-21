# iDash native music, adapted BitChord core — 0.8.0-rc1

The player is now an actual Java View inside the Drive map region, backed by iDash's own Android MediaPlayer and MediaSession. It does not launch a separate BitChord APK, Activity, WebView or split-screen. The optional split Activity/service and automatic native-app launching introduced in rc4/rc5 were removed. The standard YouTube/FM/Map views remain.

## Included functionality

- Native search field and scrollable song results, title/artist/album, and queue view.
- Search and stream detail protocol adapted from BitChord's GPLv3 `JioSaavnService.kt`, including DES catalogue-URL decoding and both detail response envelopes. The original file is preserved under `third_party/bitchord/`, commit acf219a7e4a7f52f026d4336d5c7ac5cf7bfafcc.
- Native Play/Pause, seek, next/previous, completion advances queue, media metadata and audio focus. The left media card can control the iDash-owned session without broad notification access.
- Choose an audio file through Android's document picker and play it directly in the panel. Only the selected URI's temporary read grant is used; library scanning or permanent file access is not requested.
- Source changes/page exit close playback, session, workers and view callbacks. App pause pauses playback. This build is a foreground panel player, not a background audio service.

This is a **subset adaptation of BitChord functions**, not the whole upstream app or its Compose UI. Current online source is **JioSaavn**, one of BitChord's providers. YouTube Music extraction, Google/Spotify login, synced lyrics, downloads, video canvas, crossfade, Automix and external source modules are not ported. No screen is labelled as a functioning upstream feature when it is absent. The existing separate YouTube web source remains independently available.

## Low-memory design

One decoder and one MediaSession for this panel; bounded lists (20 search rows / 50 queue entries); 1MiB response limit; finite connection/read timeouts; cancelled stale searches and stream resolutions; no artwork decoding or per-frame animation in the music UI; progress only once per second while visible/playing. Seeking commits once after drag rather than seeking on every movement. No Kotlin, Compose, ONNX, FFmpeg, native libraries or new player dependencies are added. The music parser/client was adapted to Java to fit API26+, ARM32 / 1GB constraints. Provider quality is not falsely labelled as 320kbps or forcibly upgraded. Geo-IP spoofing headers from the upstream reference are not used.

## License and provenance

Combining the adapted GPLv3 BitChord source requires GPL distribution of this combined software build. `LICENSE` now contains GPL3, `LICENSE-MIT` retains the original iDash MIT grant, and existing font/artwork notices remain in `NOTICE`. The GPL text is included in the APK under `assets/licenses/BitChord-GPL-3.0.txt`. Corresponding Java code and the upstream reference file are on this trial branch. This change does not claim ownership or relicensing of pre-existing third-party artwork.

Craft Anything was explicitly used for investigation memory, source evidence and findings. Its skill recommends reading source directly for source questions. Its doctor/dry-run found the existing registration current; no setup was reapplied. Evidence `ev_ae90973cb3989c818415a5656d63e1d74a2c1dd06d21869d9a592e81c926f839` records the upstream search/detail methods. Investigation: `/workspace/craft-audits/bitchord-native.cany`. Craft did not automatically port an entire app; the Java adaptation and Android integration were implemented and tested in this branch.

## Validation / limitations

Build and Android test APK compile. 83 JVM tests passed, including real DES codec round-trip, malformed token/HTTP rejection and query encoding; lint has no errors. The live JioSaavn endpoint request from this workspace failed with proxy tunnel403. **Live catalogue search and internet streaming are unverified**, not reported as working from fixture parsing. Network errors are shown; no fake search results or stream-success indicators are supplied. Local playback is tested with an explicitly synthetic WAV fixture using the real Android decoder, audio focus and MediaSession. That test is not proof of CDN availability, music rights, catalogue coverage or head unit audio output. OEM/ARM32 performance still needs the physical device.

## Version / rollback

Trial branch `feat/idash4-0.8.0-rc1-native-music`; backup tag `backup/pre-native-music`. Main unchanged. Native candidate code39, `dist/iDash4-0.8.0-rc1-debug.apk`. Previous rc5 source rollback code40, `dist/iDash4-0.7.0-rc5-rollback-debug.apk`, same package/key and previous software with version metadata changed. Install rollback without uninstalling if keeping preferences. Disable any previously enabled iDash split actions service; this native build no longer declares it. Later normal upgrades after rollback need code41+. Previous pre-integration commits retain their original licensing.

Current core validation: 16 real decoder/session/lifecycle assertions passed on API27 using synthetic PCM, 12 native panel/geometry/disposal assertions and 42 existing Drive flow assertions. Final screenshot shows the native queue containing explicitly labelled test tracks: `native-music-player-1024x600.png`. The first run was obscured by a System UI ANR; another exposed the test's fixed700ms assumption before AudioTrack startup. The final test waits for real position advancement rather than assuming software-emulator startup time. Initial failures are retained.

A separate full upstream UI build probe is still being investigated. The unmodified source initially required a Java compiler launcher not installed with the runtime; a local compiler shim exposed the existing actual JDK21 compiler module. The latest source also requests compileSdk37 but the downloaded platform reports37.0 to an older AGP; a temporary copy was tested at SDK36 without changing the original checkout. That probe reached NewPipeExtractor resolution but JitPack returned403 for `com.github.TeamNewPipe:NewPipeExtractor:v0.26.3`. The full UI port is not claimed complete by this core checkpoint. An upstream source build of that dependency is being attempted separately.

This is a checkpoint, not completion of the user's requested full upstream UI port. That full port remains ongoing: pinned NewPipe/nanojson sources are being built locally to replace JitPack403 dependencies, followed by InnerTubeX and the UI module dependency audit. No claim of full upstream UI integration is made by this checkpoint.
