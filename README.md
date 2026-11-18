# iDash4 — iDash3 integration

**Current candidate: iDash4 0.5.0-rc1** — native Drive Dashboard replaces Android split. Main region switches between OSM map, audio controls, FM and YouTube; live JAKIM/media/date widgets remain on the right. [Download debug APK](dist/iDash4-0.5.0-rc1-debug.apk?raw=true) · [Dashboard scope, audit and limitations](docs/DRIVE-DASHBOARD-0.5.0-RC1.md). No turn-by-turn routing yet. `main` and glass renderer remain unchanged.

**Revert panel appearance:** Settings → Home & Dock → Liquid Glass → Material panel → **Classic iDash3**. The new iOS26 panel material and the icon-pack switch are independent.

**[English](#english)** · **[Bahasa Melayu](#bahasa-melayu)**

**EN** — A lightweight iOS/iPadOS-inspired **"Liquid Glass" home launcher for old Android car head units** — built and tested for a 2016 Proton Saga VVT head unit (Android 8.1 / API 27, 32-bit ARM, ~1 GB RAM, 1024×600 and 800×480 landscape screens). Pure Java Views, no runtime dependencies, no Google Play Services.

**MY** — Launcher **"Liquid Glass" gaya iOS/iPadOS untuk head unit kereta Android lama** — dibina dan diuji untuk head unit Proton Saga VVT 2016 (Android 8.1 / API 27, ARM 32-bit, ±1 GB RAM, skrin landskap 1024×600 dan 800×480). Pure Java Views, tiada kebergantungan runtime, tiada Google Play Services.

## Downloads / Muat turun

| File / Fail | Notes / Nota |
|---|---|
| [**iDash3-0.2.0-dev-debug-QA.apk**](dist/iDash3-0.2.0-dev-debug-QA.apk?raw=true) | Launcher — QA debug APK. SHA256 and install notes in [`dist/README.md`](dist/README.md) / SHA256 dan nota pemasangan dalam [`dist/README.md`](dist/README.md) |
| [**iDash3-Debloat-1.0.apk**](dist/iDash3-Debloat-1.0.apk?raw=true) | One-shot tool: disables Saga head-unit bloatware, clears diagnostic logs/cache, sets WebView → Chrome. Reversible / Alat sekali guna: lumpuhkan bloatware head unit Saga, buang log diagnostik/cache, tetapkan WebView → Chrome. Boleh diundur |
| [Recommended apps](dist/RECOMMENDED-APPS.md) | Waze 5.9.5.4, NewPipe, SmartTube ≥30.56, Chrome 138 — official links & compatible versions |
| [ADB scripts](tools/headunit/) | `debloat-adb.sh` / `.bat` for PC |

- Package `com.shazwan.idash3` — installs **alongside** your existing launcher (it does not replace it). Keep the stock launcher as a fallback. / Pakej `com.shazwan.idash3` — dipasang **bersama** launcher sedia ada (tidak menggantikannya). Kekalkan launcher asal sebagai sandaran.
- After installing: open iDash3, then **Control Center → Choose default launcher** (or press HOME and select iDash3). / Selepas pasang: buka iDash3, kemudian **Control Center → Choose default launcher** (atau tekan butang HOME dan pilih iDash3).
- This is **not** a release build: it is signed with the debug key. See [Current limitations](#current-limitations) / [Had semasa](#had-semasa). / Ini **bukan** binaan release: ia ditandatangani kunci debug.

## Screenshots / Tangkapan skrin

There are **no screenshots committed yet** — the maintainer has no ARM32 head unit or KVM-capable emulator in the build environment, so the images must come from a real unit. The exact screen list, filenames and one-line commands are in **[`docs/SCREENSHOTS.md`](docs/SCREENSHOTS.md)**; the helper is [`tools/screenshots.sh`](tools/screenshots.sh).

Belum ada tangkapan skrin dalam repo — persekitaran bina tiada head unit ARM32 dan tiada emulator ber-KVM, jadi gambar perlu diambil dari peranti sebenar. Senarai skrin, nama fail dan arahan satu baris ada dalam **[`docs/SCREENSHOTS.md`](docs/SCREENSHOTS.md)**; pembantunya [`tools/screenshots.sh`](tools/screenshots.sh).

Already captured? Drop the PNGs into `docs/images/` with the documented names and add them to the gallery in this section. / Sudah ambil? Letakkan PNG ke `docs/images/` dengan nama yang didokumenkan dan tambah ke galeri di seksyen ini.

---

## English

### Features

**Home**
- Left: large clock, calendar, **JAKIM prayer times** (your own zone; the official 2026 timetable for SGR01–03 & WLY01 is bundled, other zones are downloaded).
- Right: empty by default (wallpaper visible) — or the **media panel** (YouTube web, OEM FM radio, Saga 8-angle car view) or a **paged app grid** with folders (optional).
- Floating dock: 3 favourites (Bluetooth, Files, YouTube — long-press to change), Settings, Apps.
- **Drive Dashboard:** native map/audio/FM/YouTube modes with persistent media, live JAKIM countdown and date cards. No Android split-screen, overlay bar or accessibility service. See [Dashboard audit](docs/DRIVE-DASHBOARD-0.5.0-RC1.md).
- App Library with search, Control Center (Wi-Fi, Bluetooth, brightness, volume, media) and a complete Settings screen.

**Liquid Glass (built for Android 8.1)**
- **Baked refraction glass:** edge refraction, vibrancy and bevel are computed **once** per panel on a background thread (not every frame) — the iOS glass look without shaders that API 27 does not have.
- Adaptive tint and text colour **per panel**, with contrast protection (≥4.9:1).
- Touch-driven specular highlight, size-aware shadow, reveal transition (lens + blur), spring and **liquid morph** (folders), elastic press.
- **iOS-style icons**: squircle tiles + original glyphs for common head-unit apps (Radio, Music, Bluetooth, Backcar, CAN, Car Settings, Maps/Navi, Files, Gallery, Video, …), 5 variants (Default, Dark, Clear, Tinted light/dark). Matched app icons now use the requested **iOS 26 Liquid Glass set from [SysAdminDoc/iOSIconPack](https://github.com/SysAdminDoc/iOSIconPack)**; unmatched apps keep the original renderer. Artwork provenance and upstream license notes are bundled.
- **Inter / Inter Display** font (SIL OFL) — the closest legal stand-in for SF Pro.
- **Performance mode**: Auto (real frame time & RAM) / Low / Full.

Settings: **Settings → Home & Dock → Liquid Glass** (icon style, performance mode, app grid, iOS-style icons).

### Requirements
- Android 8.0+ (minSdk 26; test target API 27), landscape screen.
- No native libraries (runs on ARM 32/64-bit). No Google Play Services required.
- Permissions: Internet (JAKIM/YouTube web, only when requested), audio settings; optional: notification access (media controls), write system settings (brightness).

### Current limitations
- **Not physically tested** on a real head unit by the maintainer — emulator results only (see [`docs/VALIDATION.md`](docs/VALIDATION.md)). User reports are very welcome.
- Debug APK: updating between debug builds signed with different debug keys requires uninstalling first.
- The FM panel depends on the OEM radio service (`com.android.fmradio` / `com.acloud.stub.extradio`); it does not ship a radio driver.
- The YouTube web panel depends on the device WebView — on Android 8.1 set **Developer options → WebView implementation → Chrome**.
- **Dashboard video:** website fullscreen fills the main player region; Expand requests the whole app window. Fit/Fill and disappearing controls remain available.

### Build
JDK 17+, Android SDK 35, Gradle wrapper 8.9:
```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
```
APK: `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions CI (`.github/workflows/android-ci.yml`) runs the build, unit tests, lint, the static guard and an API 27 emulator test.

### Docs
| File | Contents |
|---|---|
| [`CHANGELOG.md`](CHANGELOG.md) | Change history |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | How to contribute & report head-unit issues |
| [`docs/DESIGN.md`](docs/DESIGN.md) | Liquid Glass & Home design |
| [`docs/DRIVE-DASHBOARD-0.5.0-RC1.md`](docs/DRIVE-DASHBOARD-0.5.0-RC1.md) | Native Dashboard, widgets, permissions and remaining device tests |
| [`docs/SCREENSHOTS.md`](docs/SCREENSHOTS.md) | Which screenshots to capture, filenames and commands |
| [`docs/VALIDATION.md`](docs/VALIDATION.md) | Real test results (PASS/FAIL/NOT RUN) |
| [`docs/HEADUNIT-APPS.md`](docs/HEADUNIT-APPS.md) | Saga head-unit apps, compatibility audit, Waze/YouTube/debloat advice |
| [`docs/HARDWARE-BASELINE.md`](docs/HARDWARE-BASELINE.md) | TEST9278B head-unit specs and memory budget |
| [`docs/ANDROID-COMPATIBILITY-AUDIT.md`](docs/ANDROID-COMPATIBILITY-AUDIT.md) | API 27 compatibility, manifest, lifecycle, Back/Home |
| [`docs/PERFORMANCE-AUDIT.md`](docs/PERFORMANCE-AUDIT.md) | RAM, CPU, ANR, OOM on a 1 GB device |
| [`docs/INSTALL-SIGNING-AUDIT.md`](docs/INSTALL-SIGNING-AUDIT.md) | applicationId, signing, ABI, install procedure |
| [`docs/HEADUNIT-VALIDATION.md`](docs/HEADUNIT-VALIDATION.md) | API vs OEM behaviour and test matrix |
| [`docs/AUDIT.md`](docs/AUDIT.md), [`docs/REVIEW.md`](docs/REVIEW.md), [`docs/SOURCE-INVENTORY.md`](docs/SOURCE-INVENTORY.md) | Original audit & code provenance |

### License
Code: MIT ([`LICENSE`](LICENSE)). Inter font: SIL OFL 1.1. Other attribution (JAKIM, NexaDash) in [`NOTICE`](NOTICE).

---

## Bahasa Melayu

### Ciri

**Home**
- Kiri: jam besar, kalendar, **waktu solat JAKIM** (zon dipilih sendiri; jadual rasmi 2026 untuk SGR01–03 & WLY01 dibundel, zon lain dimuat turun).
- Kanan: kosong secara lalai (wallpaper kelihatan) — atau **panel media** (YouTube web, Radio FM OEM, paparan kereta Saga 8 sudut) atau **grid app berhalaman** dengan folder (pilihan).
- Dock terapung: 3 kegemaran (Bluetooth, Files, YouTube — tekan lama untuk tukar), Settings, Apps.
- **Drive Dashboard:** mod peta/audio/FM/YouTube dengan kad media, countdown JAKIM dan tarikh. Split-screen, floating bar dan servis aksesibiliti lama telah dibuang.
- App Library dengan carian, Control Center (Wi-Fi, Bluetooth, kecerahan, kelantangan, media), Settings lengkap.

**Liquid Glass (dibina khas untuk Android 8.1)**
- **Kaca refraksi "baked":** refraksi tepi, vibrancy dan bevel dikira **sekali** per panel di thread latar (bukan setiap bingkai) — rupa kaca iOS tanpa shader moden yang tiada pada API 27.
- Tint dan warna teks **adaptif per panel** dengan perlindungan kontras teks (≥4.9:1).
- Specular ikut sentuhan, bayang ikut saiz panel, peralihan reveal (lens + blur), spring dan **morph cecair** (folder), tekan anjal.
- **Ikon gaya iOS**: jubin squircle + glyph asli untuk app head unit biasa (Radio, Music, Bluetooth, Backcar, CAN, Car Settings, Maps/Navi, Files, Gallery, Video, dll.), 5 varian (Default, Dark, Clear, Tinted terang/gelap). Ikon sepadan menggunakan artwork **iOS 26 Liquid Glass SysAdminDoc/iOSIconPack**; renderer asal kekal untuk app tanpa padanan. Sumber dan nota lesen disertakan.
- Font **Inter / Inter Display** (SIL OFL) — paling hampir dengan SF Pro yang boleh diedarkan secara sah.
- **Mod prestasi**: Auto (ikut masa bingkai sebenar & RAM) / Rendah / Penuh.

Tetapan: **Settings → Home & Dock → Liquid Glass** (gaya ikon, mod prestasi, grid app, ikon gaya iOS).

### Keperluan peranti
- Android 8.0+ (minSdk 26; sasaran ujian API 27), skrin landskap.
- Tiada pustaka native (berjalan pada ARM 32/64-bit). Tiada Google Play Services diperlukan.
- Kebenaran: Internet (JAKIM/YouTube web, hanya bila diminta), tetapan audio; pilihan: akses notifikasi (kawalan media), tulis tetapan sistem (kecerahan).

### Had semasa
- **Belum diuji secara fizikal** pada head unit sebenar oleh penyelenggara — keputusan emulator sahaja (lihat [`docs/VALIDATION.md`](docs/VALIDATION.md)). Laporan daripada pengguna sangat dialu-alukan.
- APK debug: kemas kini antara binaan debug yang ditandatangani kunci berbeza memerlukan nyahpasang dahulu.
- Panel FM bergantung pada perkhidmatan radio OEM (`com.android.fmradio` / `com.acloud.stub.extradio`); ia tidak membekalkan pemacu radio.
- Panel YouTube web bergantung pada WebView peranti — pada Android 8.1, tetapkan **Developer options → WebView implementation → Chrome**.
- **Video dashboard:** fullscreen laman memenuhi ruang pemain; Expand menggunakan tetingkap app penuh. Pilihan Muat/Isi dan kawalan auto-hide dikekalkan.

### Bina sendiri
JDK 17+, Android SDK 35, Gradle wrapper 8.9:
```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
```
APK: `app/build/outputs/apk/debug/app-debug.apk`. CI GitHub Actions (`.github/workflows/android-ci.yml`) menjalankan build, ujian unit, lint, pengawal statik dan ujian emulator API 27.

### Dokumen
| Fail | Isi |
|---|---|
| [`CHANGELOG.md`](CHANGELOG.md) | Sejarah perubahan |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Cara menyumbang & melapor isu head unit |
| [`docs/DESIGN.md`](docs/DESIGN.md) | Reka bentuk Liquid Glass & Home |
| [`docs/DRIVE-DASHBOARD-0.5.0-RC1.md`](docs/DRIVE-DASHBOARD-0.5.0-RC1.md) | Native Dashboard, widgets, permissions and remaining device tests |
| [`docs/SCREENSHOTS.md`](docs/SCREENSHOTS.md) | Skrin mana yang dirakam, nama fail dan arahan |
| [`docs/VALIDATION.md`](docs/VALIDATION.md) | Keputusan ujian sebenar (PASS/FAIL/NOT RUN) |
| [`docs/HEADUNIT-APPS.md`](docs/HEADUNIT-APPS.md) | App head unit Saga, audit keserasian, cadangan Waze/YouTube/debloat |
| [`docs/HARDWARE-BASELINE.md`](docs/HARDWARE-BASELINE.md) | Spesifikasi head unit TEST9278B dan bajet memori |
| [`docs/ANDROID-COMPATIBILITY-AUDIT.md`](docs/ANDROID-COMPATIBILITY-AUDIT.md) | Keserasian API 27, manifest, lifecycle, Back/Home |
| [`docs/PERFORMANCE-AUDIT.md`](docs/PERFORMANCE-AUDIT.md) | RAM, CPU, ANR, OOM pada peranti 1 GB |
| [`docs/INSTALL-SIGNING-AUDIT.md`](docs/INSTALL-SIGNING-AUDIT.md) | applicationId, tandatangan, ABI, prosedur pasang |
| [`docs/HEADUNIT-VALIDATION.md`](docs/HEADUNIT-VALIDATION.md) | Fungsi API vs OEM dan matriks ujian |
| [`docs/AUDIT.md`](docs/AUDIT.md), [`docs/REVIEW.md`](docs/REVIEW.md), [`docs/SOURCE-INVENTORY.md`](docs/SOURCE-INVENTORY.md) | Audit asal & asal-usul kod |

### Lesen
Kod: MIT ([`LICENSE`](LICENSE)). Font Inter: SIL OFL 1.1. Atribusi lain (JAKIM, NexaDash) dalam [`NOTICE`](NOTICE).
