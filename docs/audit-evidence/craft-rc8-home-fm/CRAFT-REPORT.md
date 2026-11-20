# Investigation report

**Question:** Audit the owned iDash3 rc8 Home and native FM panel at 800x480 and 1024x600: card geometry, text clipping, toolbar/dock alignment and controls.

**Purpose:** Verify the current UI against the accepted compact-card layout and identify reproducible defects before editing app code.

**Done when:** A reviewable evidence-backed audit with native screenshots at both sizes, verified geometry and fixes only for confirmed defects.

## What we looked at

- iDash4-0.6.0-rc8-debug.apk (apk)
- /workspace/iDash3/dist/iDash4-0.6.0-rc8-debug.apk

## What we found

### The owned artifact is com.shazwan.idash3 0.6.0-rc8, versionCode 28, minSdk 26/targetSdk 35; the built-in parser verified its APK signing evidence and found no packaged native ABI libraries. This is package metadata, not a runtime performance claim.

- **Confidence:** Seen it (observed)
- **Evidence:**
  - `ev_12d0ba9706c516c68e6724189d55e40c22c2618ec431ce85b398f11549915197` — com.shazwan.idash3 (iDash4-0.6.0-rc8-debug.apk, version 0.6.0-rc8, SDK 26–35): 14 permissions, 2 components (2 exported), 3 DEX file(s), native code for no ABI; signed with scheme v2 (present but NOT verified). (path=/workspace/iDash3/dist/iDash4-0.6.0-rc8-debug.apk)

### Four settled, focused native windows were captured: Home and FM at logical 1024x600 and 800x480. The two Home cards measured 224x114 pixels each in the observed night scene, shared their left edge and retained a 14-pixel gap. Dock measured 349x64 pixels; all stayed inside the viewport.

- **Confidence:** Worked it out (derived)
- **Evidence:**
  - `ev_7152f3248bca3fe4f0c390244d19f5a46ed0fcc98e2fc2904880a4b1a405a0c1` — Pulled /sdcard/Android/data/com.shazwan.idash3/files/craft-home-1024x600.json to /workspace/craft-audits/rc8-native-final/home-1024x600-metrics. (path=/workspace/craft-audits/rc8-native-final/home-1024x600-metrics)
  - `ev_bca67c04ed449715c49d4584357ae79990c8b5f8b41c51a405e2f3d0447e17b6` — Pulled /sdcard/Android/data/com.shazwan.idash3/files/craft-fm-1024x600.json to /workspace/craft-audits/rc8-native-final/fm-1024x600-metrics. (path=/workspace/craft-audits/rc8-native-final/fm-1024x600-metrics)
  - `ev_f07f3097e514988958d7d9df863d648f8aa20d90a4fa79e7ed4578228d8f81c3` — Pulled /sdcard/Android/data/com.shazwan.idash3/files/craft-home-800x480.json to /workspace/craft-audits/rc8-native-final/home-800x480-metrics. (path=/workspace/craft-audits/rc8-native-final/home-800x480-metrics)
  - `ev_8abe0f150f785c38f0daf57ecdb005ccfe373bfed06ddfd6660ae76ba28e3167` — Pulled /sdcard/Android/data/com.shazwan.idash3/files/craft-fm-800x480.json to /workspace/craft-audits/rc8-native-final/fm-800x480-metrics. (path=/workspace/craft-audits/rc8-native-final/fm-800x480-metrics)
  - `ev_2fb6879dee2fd210cb6b5fac4c7fcdc00ceb24ecbde1f0ff50d7cf118f7e757b` — Saved a 535324-byte screenshot to /workspace/craft-audits/rc8-native-final/home-1024x600-screen/screen.png. (path=/workspace/craft-audits/rc8-native-final/home-1024x600-screen/screen.png)
  - `ev_6220eeb780b8d964e6e5016136bcd8ed16e6663748f8da26f94dd2a33b6410bf` — Saved a 668921-byte screenshot to /workspace/craft-audits/rc8-native-final/fm-1024x600-screen/screen.png. (path=/workspace/craft-audits/rc8-native-final/fm-1024x600-screen/screen.png)
  - `ev_c4f67a1b8eac3728930bba6a0098b3f1bcbb086526aabd9f36068934f6a4dfad` — Saved a 480662-byte screenshot to /workspace/craft-audits/rc8-native-final/home-800x480-screen/screen.png. (path=/workspace/craft-audits/rc8-native-final/home-800x480-screen/screen.png)
  - `ev_68a67afd0229ce599e39e621eb6aa0519ee248d5e2c938dba6da842727af7b3c` — Saved a 599359-byte screenshot to /workspace/craft-audits/rc8-native-final/fm-800x480-screen/screen.png. (path=/workspace/craft-audits/rc8-native-final/fm-800x480-screen/screen.png)

### All 50 declared geometry conditions passed across the four final cases: measured standard TextViews had no clipping or ellipsis, toolbar controls aligned and separated, FM filled its padded host, and all ten presets remained visible with at least 48x48-pixel targets. This does not certify custom Canvas-text legibility or RF playback.

- **Confidence:** Worked it out (derived)
- **Evidence:**
  - `ev_7152f3248bca3fe4f0c390244d19f5a46ed0fcc98e2fc2904880a4b1a405a0c1` — Pulled /sdcard/Android/data/com.shazwan.idash3/files/craft-home-1024x600.json to /workspace/craft-audits/rc8-native-final/home-1024x600-metrics. (path=/workspace/craft-audits/rc8-native-final/home-1024x600-metrics)
  - `ev_bca67c04ed449715c49d4584357ae79990c8b5f8b41c51a405e2f3d0447e17b6` — Pulled /sdcard/Android/data/com.shazwan.idash3/files/craft-fm-1024x600.json to /workspace/craft-audits/rc8-native-final/fm-1024x600-metrics. (path=/workspace/craft-audits/rc8-native-final/fm-1024x600-metrics)
  - `ev_f07f3097e514988958d7d9df863d648f8aa20d90a4fa79e7ed4578228d8f81c3` — Pulled /sdcard/Android/data/com.shazwan.idash3/files/craft-home-800x480.json to /workspace/craft-audits/rc8-native-final/home-800x480-metrics. (path=/workspace/craft-audits/rc8-native-final/home-800x480-metrics)
  - `ev_8abe0f150f785c38f0daf57ecdb005ccfe373bfed06ddfd6660ae76ba28e3167` — Pulled /sdcard/Android/data/com.shazwan.idash3/files/craft-fm-800x480.json to /workspace/craft-audits/rc8-native-final/fm-800x480-metrics. (path=/workspace/craft-audits/rc8-native-final/fm-800x480-metrics)

### Final screenshots visually show unobstructed Home/FM windows and the retained compact rc8 design. Initial captures with an external System UI ANR dialog were excluded and recaptured after dismissal with an app-window-focus gate.

- **Confidence:** Seen it (observed)
- **Evidence:**
  - `ev_2fb6879dee2fd210cb6b5fac4c7fcdc00ceb24ecbde1f0ff50d7cf118f7e757b` — Saved a 535324-byte screenshot to /workspace/craft-audits/rc8-native-final/home-1024x600-screen/screen.png. (path=/workspace/craft-audits/rc8-native-final/home-1024x600-screen/screen.png)
  - `ev_6220eeb780b8d964e6e5016136bcd8ed16e6663748f8da26f94dd2a33b6410bf` — Saved a 668921-byte screenshot to /workspace/craft-audits/rc8-native-final/fm-1024x600-screen/screen.png. (path=/workspace/craft-audits/rc8-native-final/fm-1024x600-screen/screen.png)
  - `ev_c4f67a1b8eac3728930bba6a0098b3f1bcbb086526aabd9f36068934f6a4dfad` — Saved a 480662-byte screenshot to /workspace/craft-audits/rc8-native-final/home-800x480-screen/screen.png. (path=/workspace/craft-audits/rc8-native-final/home-800x480-screen/screen.png)
  - `ev_68a67afd0229ce599e39e621eb6aa0519ee248d5e2c938dba6da842727af7b3c` — Saved a 599359-byte screenshot to /workspace/craft-audits/rc8-native-final/fm-800x480-screen/screen.png. (path=/workspace/craft-audits/rc8-native-final/fm-800x480-screen/screen.png)

### Apps and Drive were captured in four additional focused windows at logical 1024x600 and 800x480. All 42 declared additional conditions passed: Apps toolbar stayed separated on one row, eight visible app tiles formed a 4x2 grid, Drive rail targets were at least 48x48 and non-overlapping, all three sidebar cards fit, and the native map object remained present with GPS tracking disabled. Standard TextViews had no clipping or ellipsis. Map tile networking was disabled in the capture.

- **Confidence:** Worked it out (derived)
- **Evidence:**
  - `ev_e10589db2ecfb333dc19602a59a9aaecd2b0108c47d6ebfee5c96475a0582503` — Saved a 711980-byte screenshot to /workspace/craft-audits/rc8-pages-final/apps-1024x600-screen/screen.png. (path=/workspace/craft-audits/rc8-pages-final/apps-1024x600-screen/screen.png)
  - `ev_0d6f6d861fe3952fffdf9d299a6235dc68b521abb376033fec7ca63a7b7cd5b9` — Saved a 613150-byte screenshot to /workspace/craft-audits/rc8-pages-final/apps-800x480-screen/screen.png. (path=/workspace/craft-audits/rc8-pages-final/apps-800x480-screen/screen.png)
  - `ev_f7ca6f9e444493aee50b148f2ba535bbdad21bae63a7b0277d1cdf28f6a4ee56` — Pulled /sdcard/Android/data/com.shazwan.idash3/files/craft-apps-800x480.json to /workspace/craft-audits/rc8-pages-final/apps-800x480-metrics. (path=/workspace/craft-audits/rc8-pages-final/apps-800x480-metrics)
  - `ev_767e6652c505d5762c12eab0b2f8434a5e1577bf33028b8028dc265e10a12e55` — Pulled /sdcard/Android/data/com.shazwan.idash3/files/craft-apps-1024x600.json to /workspace/craft-audits/rc8-pages-final/apps-1024x600-metrics. (path=/workspace/craft-audits/rc8-pages-final/apps-1024x600-metrics)
  - `ev_b6b76f71afd057ea3e71ae0e922f5f9f15a91e14050b45794da1efe0287553e9` — Saved a 427021-byte screenshot to /workspace/craft-audits/rc8-pages-final/drive-800x480-screen/screen.png. (path=/workspace/craft-audits/rc8-pages-final/drive-800x480-screen/screen.png)
  - `ev_182a650941619685861b58868457809b4e8ce56ebe5a76f0ecdf3e4837e9a90c` — Pulled /sdcard/Android/data/com.shazwan.idash3/files/craft-drive-1024x600.json to /workspace/craft-audits/rc8-pages-final/drive-1024x600-metrics. (path=/workspace/craft-audits/rc8-pages-final/drive-1024x600-metrics)
  - `ev_b012136023b426020caf0f57098b8d526877125aa13dd64a5f95a62ebf7ce583` — Pulled /sdcard/Android/data/com.shazwan.idash3/files/craft-drive-800x480.json to /workspace/craft-audits/rc8-pages-final/drive-800x480-metrics. (path=/workspace/craft-audits/rc8-pages-final/drive-800x480-metrics)
  - `ev_b548aa593c33475665726405ab20e2c727d2a8096f3574240748382ba723a9c4` — Saved a 450011-byte screenshot to /workspace/craft-audits/rc8-pages-final/drive-1024x600-screen/screen.png. (path=/workspace/craft-audits/rc8-pages-final/drive-1024x600-screen/screen.png)

## Open questions

- Are the small custom-painted JAKIM footer and dock captions readable at real head-unit viewing distance, and do FM RF/audio and frame pacing work on the physical head unit? Physical hardware was not connected. _(severity Medium, Open)_

## How this was done

- **Mode:** Clean room (nothing inside the original was read)
- **Engines:** adb, builtin
- **Limitations:**
  - Signatures ARE verified cryptographically here (v2/v3 signature over the signed data plus a recomputed APK content digest; v1 PKCS#7 over the .SF plus MANIFEST.MF per-file digests). NOT checked: certificate revocation, v3 key-rotation lineage/proof-of-rotation, rollback protection, source-stamp, and whether the signing certificate is one you trust (the certificate SHA-256 fingerprint is reported so you can compare it yourself).

## Handoff

Completed rc8 layout audit: 8 settled captures and 92 declared geometry/text conditions passed. Home/FM raw evidence: /workspace/craft-audits/rc8-native-final. Drive/Apps: /workspace/craft-audits/rc8-pages-final. Analysis scripts analyze-ui.py and analyze-pages.py; repo audit branch audit/idash4-rc8-home-fm-craft. APK unchanged, SHA256 f6201e2e1743b32c305713eb46266184455504d190188d1b8387b25f7f6614ac. Initial rc8-native captures had System UI ANR dialog; excluded, recaptured with focus gate. Small custom-painted footer readability at actual head-unit distance, physical RF/playback/GPS and frame pacing remain unverified. CLI tools need explicit --path per invocation; active target does not survive CLI processes. Native 800x480 logical windows are composited into 1024x600 screenshots. No production fix indicated by measured scope; report links all evidence ids.
