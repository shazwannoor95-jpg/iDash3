# Investigation report

**Question:** Integrate the full upstream BitChord Compose UI and music service into iDash Drive as native source, without split-screen or a separate app

**Purpose:** Source interoperability on an API27, ARM32, 1GB head unit

**Done when:** Full native UI is inside the right panel beside JAKIM, real service audio and lifecycle verified, ARM32 APK and rollback available

## What we looked at

- JioSaavnService.kt (text)
- NativePlaybackChecks-final.txt (text)
- build-and-lint.txt (text)
- innertubex-local-build.log (text)
- result.json (text)
- validation-summary.txt (text)
- /workspace/iDash3
- /workspace/BitChord

## What we found

### Upstream BitChord JioSaavnService implements search.getResults and song.getDetails and accepts both songs-array and keyed-object detail responses. This provides a small source module that can be adapted without porting the entire Compose app.

- **Confidence:** Seen it (observed)
- **Evidence:**
  - `ev_ae90973cb3989c818415a5656d63e1d74a2c1dd06d21869d9a592e81c926f839` — JioSaavnService.kt: lines 186–245 of 246 (UTF-8). (path=/workspace/BitChord/app/src/main/java/com/music/bitchord/data/jiosaavn/JioSaavnService.kt)

### NativePlaybackChecks reports PASS for 16 Android decoder/session assertions using synthetic PCM. This verifies the native playback path and queue rendering, not live online streaming or physical head-unit audio.

- **Confidence:** Seen it (observed)
- **Evidence:**
  - `ev_b6b42b9d2f599674ef3146b6565ede777c7c9e5aced445b0ed2e109c8f955bbe` — NativePlaybackChecks-final.txt: lines 1–3 of 3 (UTF-8). (path=/workspace/iDash3/docs/audit-evidence/idash4-0.8.0-rc1/NativePlaybackChecks-final.txt)

### Pinned InnerTubeX v0.7.0 source build completed successfully: allTests, ktlintCheck, apiCheck, assemble and publishToMavenLocal (98 tasks). This validates the dependency build, not end-to-end iDash playback.

- **Confidence:** Seen it (observed)
- **Evidence:**
  - `ev_73da26faa51b63d4759159d7828a43a2452c32a5ba2364c666e05349f6d85e38` — innertubex-local-build.log: lines 1–244 of 244 (UTF-8). (path=/tmp/innertubex-local-build.log)

### The full BitChord Android library and native iDash host build successfully with debug APK, instrumentation APK, 78 iDash unit tests, and lintDebug with zero errors. This is build/static evidence; runtime UI/playback verification is still pending.

- **Confidence:** Seen it (observed)
- **Evidence:**
  - `ev_580db254b72d715f6788495cbcc0ab934c7204c4e3243f0c6d737bc72d31bb84` — build-and-lint.txt: lines 1–360 of 360 (UTF-8). (path=/workspace/iDash3/docs/audit-evidence/idash4-0.8.0-rc2/build-and-lint.txt)

### Final rc2 hosts the real upstream Compose UI inside iDash: 13 native UI assertions pass, with an actual Library screenshot beside JAKIM. Actual upstream Media3 service passes 10 PCM playback/session/queue checks. Existing Drive, Home and App Library add 42, 57 and 17 passes, respectively (139 Android assertions). JVM tests:78; lint errors:0. The 80,563,231-byte ARM32 artifact has four DEX payloads identical to the tested x86_64 candidate. These are offline emulator results, not live streaming or physical ARM performance.

- **Confidence:** Seen it (observed)
- **Evidence:**
  - `ev_9643c0598f5705e2b6cbce37553ca0313408b0266a826b7f47c6a403037753ab` — validation-summary.txt: lines 1–29 of 29 (UTF-8). (path=/workspace/iDash3/docs/audit-evidence/idash4-0.8.0-rc2/validation-summary.txt)

### API27 emulator replacement succeeds rc2 -> pre-integration rc5 rollback -> rc2 again without uninstalling. Matching signing certificate and equal versionCode41 allow both directions; all17 existing iDash preference keys are preserved in both transitions.

- **Confidence:** Seen it (observed)
- **Evidence:**
  - `ev_c912ce07356a931cea495e0fd65e4c29a2f6b282bea5c87185ad8a350cf5033b` — result.json: lines 1–7 of 7 (UTF-8). (path=/workspace/iDash3/docs/audit-evidence/idash4-0.8.0-rc2/rollback-roundtrip/result.json)

## Open questions

- What are cold-start time, frame pacing and steady memory on the actual API27 ARM32/1GB/Mali-450 head unit? Settle by installing the ARM32 trial APK and measuring on that unit; software x86 emulator timing does not answer this. _(severity Medium, Open)_
- Do live online catalogue, Google/Spotify login, streaming and optional addon/USB/Cast paths work on the actual unit? Settle with user-operated network/account/device checks; this environment has blocked APIs and emulator DNS failures. _(severity Medium, Open)_

## How this was done

- **Mode:** Clean room (nothing inside the original was read)
- **Engines:** builtin
- **Limitations:**
  - The file is read as text as it is stored: nothing is run, rendered or formatted, and text a program builds or downloads while it runs isn't here.

## Handoff

Full source integration completed on feat/idash4-0.8.0-rc2-full-bitchord-ui, source/artifact commit 7c0672b4155ce83618e8b747cb421f174f7eab17. ARM32 APK dist/iDash4-0.8.0-rc2-arm32-debug.apk (80563231 bytes), code41; rc5 rollback samecode/samecert supports bidirectional replacement, tested preserving17 prefs. Full UI13 + actual Media3 PCM10 + Drive42 + Home57 + AppLibrary17 =139 Android assertions;78 JVM tests; lint0. Main must stay a96888d60533c1dc26003d88136a93d54f029f5d. Native UI and same-process token/service are real, no separate APK/split/Activity embedding. Upstream sources pinned acf219a, checked-in source-built dependencies and GPL correspondence. R8, finite weighted-layout bounds, excluded external widgets and source-only host bootstrap fixed observed failures. UI automation needs early connection, interactive windows and normalized labels; screenshot is actual Library UI with synthetic Test PCM history, not real online songs. Live network/login/addon/USB/Cast and actual ARM hardware performance remain unknown. Next: push only trial branch/backup tags, verify raw artifact links and main hash, then return download/rollback/preview links. For future hardware issues inspect actual logs, do not reinstate split-screen or claim rc1 subset is full UI. All earlier failure evidence retained separately.

## Evidence ledger

- `ev_ae90973cb3989c818415a5656d63e1d74a2c1dd06d21869d9a592e81c926f839` **core_text_read** — JioSaavnService.kt: lines 186–245 of 246 (UTF-8). (Seen it)
- `ev_b6b42b9d2f599674ef3146b6565ede777c7c9e5aced445b0ed2e109c8f955bbe` **core_text_read** — NativePlaybackChecks-final.txt: lines 1–3 of 3 (UTF-8). (Seen it)
- `ev_73da26faa51b63d4759159d7828a43a2452c32a5ba2364c666e05349f6d85e38` **core_text_read** — innertubex-local-build.log: lines 1–244 of 244 (UTF-8). (Seen it)
- `ev_580db254b72d715f6788495cbcc0ab934c7204c4e3243f0c6d737bc72d31bb84` **core_text_read** — build-and-lint.txt: lines 1–360 of 360 (UTF-8). (Seen it)
- `ev_9643c0598f5705e2b6cbce37553ca0313408b0266a826b7f47c6a403037753ab` **core_text_read** — validation-summary.txt: lines 1–29 of 29 (UTF-8). (Seen it)
- `ev_c912ce07356a931cea495e0fd65e4c29a2f6b282bea5c87185ad8a350cf5033b` **core_text_read** — result.json: lines 1–7 of 7 (UTF-8). (Seen it)

