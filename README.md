# iDash3

**Launcher "Liquid Glass" gaya iOS/iPadOS untuk head unit kereta Android lama** — dibina dan diuji untuk head unit Proton Saga VVT 2016 (Android 8.1 / API 27, ARM 32-bit, ±1 GB RAM, skrin landskap 1024×600 dan 800×480).

> *English summary:* iDash3 is a lightweight iOS/iPadOS-inspired "Liquid Glass" home launcher for old Android 8.1 car head units (32-bit ARM, ~1 GB RAM). Pure Java Views, no runtime dependencies, no Google Play Services. Download the APK from [`dist/`](dist), read [`CHANGELOG.md`](CHANGELOG.md), and see [`CONTRIBUTING.md`](CONTRIBUTING.md) to help.

## Muat turun

| Fail | Nota |
|---|---|
| [**iDash3-0.2.0-dev-debug-QA.apk**](dist/iDash3-0.2.0-dev-debug-QA.apk?raw=true) | APK ujian (debug). SHA256 dan nota pemasangan dalam [`dist/README.md`](dist/README.md) |

- Pakej `com.shazwan.idash3` — dipasang **bersama** launcher sedia ada (tidak menggantikannya). Kekalkan launcher asal sebagai sandaran.
- Selepas pasang: buka iDash3, kemudian **Control Center → Choose default launcher** (atau tekan butang HOME dan pilih iDash3).
- Ini **bukan** binaan release: ia ditandatangani kunci debug. Lihat [Had semasa](#had-semasa).

## Ciri

**Home**
- Kiri: jam besar, kalendar, **waktu solat JAKIM** (zon dipilih sendiri; jadual rasmi 2026 untuk SGR01–03 & WLY01 dibundel, zon lain dimuat turun).
- Kanan: kosong secara lalai (wallpaper kelihatan) — atau **panel media** (YouTube web, Radio FM OEM, paparan kereta Saga 8 sudut) atau **grid app berhalaman** dengan folder (pilihan).
- Dock terapung: 3 kegemaran (Bluetooth, Files, YouTube — tekan lama untuk tukar), Settings, Apps.
- App Library dengan carian, Control Center (Wi-Fi, Bluetooth, kecerahan, kelantangan, media), Settings lengkap.

**Liquid Glass (dibina khas untuk Android 8.1)**
- **Kaca refraksi "baked":** refraksi tepi, vibrancy dan bevel dikira **sekali** per panel di thread latar (bukan setiap bingkai) — rupa kaca iOS tanpa shader moden yang tiada pada API 27.
- Tint dan warna teks **adaptif per panel** dengan perlindungan kontras teks (≥4.9:1).
- Specular ikut sentuhan, bayang ikut saiz panel, peralihan reveal (lens + blur), spring dan **morph cecair** (folder), tekan anjal.
- **Ikon gaya iOS**: jubin squircle + glyph asli untuk app head unit biasa (Radio, Music, Bluetooth, Backcar, CAN, Car Settings, Maps/Navi, Files, Gallery, Video, dll.), 5 varian (Default, Dark, Clear, Tinted terang/gelap). Ikon dilukis sendiri — **tiada aset Apple**.
- Font **Inter / Inter Display** (SIL OFL) — paling hampir dengan SF Pro yang boleh diedarkan secara sah.
- **Mod prestasi**: Auto (ikut masa bingkai sebenar & RAM) / Rendah / Penuh.

Tetapan: **Settings → Home & Dock → Liquid Glass** (gaya ikon, mod prestasi, grid app, ikon gaya iOS).

## Keperluan peranti
- Android 8.0+ (minSdk 26; sasaran ujian API 27), skrin landskap.
- Tiada pustaka native (berjalan pada ARM 32/64-bit). Tiada Google Play Services diperlukan.
- Kebenaran: Internet (JAKIM/YouTube web, hanya bila diminta), tetapan audio; pilihan: akses notifikasi (kawalan media), tulis tetapan sistem (kecerahan).

## Had semasa
- **Belum diuji secara fizikal** pada head unit sebenar oleh penyelenggara — keputusan emulator sahaja (lihat [`docs/VALIDATION.md`](docs/VALIDATION.md)). Laporan daripada pengguna sangat dialu-alukan.
- APK debug: kemas kini antara binaan debug yang ditandatangani kunci berbeza memerlukan nyahpasang dahulu.
- Panel FM bergantung pada perkhidmatan radio OEM (`com.android.fmradio` / `com.acloud.stub.extradio`); ia tidak membekalkan pemacu radio.
- Panel YouTube web bergantung pada WebView peranti — pada Android 8.1, tetapkan **Developer options → WebView implementation → Chrome**.

## Bina sendiri
JDK 17+, Android SDK 35, Gradle wrapper 8.9:
```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
```
APK: `app/build/outputs/apk/debug/app-debug.apk`. CI GitHub Actions (`.github/workflows/android-ci.yml`) menjalankan build, ujian unit, lint, pengawal statik dan ujian emulator API 27.

## Dokumen
| Fail | Isi |
|---|---|
| [`CHANGELOG.md`](CHANGELOG.md) | Sejarah perubahan |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Cara menyumbang & melapor isu head unit |
| [`docs/DESIGN.md`](docs/DESIGN.md) | Reka bentuk Liquid Glass & Home |
| [`docs/VALIDATION.md`](docs/VALIDATION.md) | Keputusan ujian sebenar (PASS/FAIL/NOT RUN) |
| [`docs/HEADUNIT-APPS.md`](docs/HEADUNIT-APPS.md) | App head unit Saga, audit keserasian, cadangan Waze/YouTube/debloat |
| [`docs/AUDIT.md`](docs/AUDIT.md), [`docs/REVIEW.md`](docs/REVIEW.md), [`docs/SOURCE-INVENTORY.md`](docs/SOURCE-INVENTORY.md) | Audit asal & asal-usul kod |

## Lesen
Kod: MIT ([`LICENSE`](LICENSE)). Font Inter: SIL OFL 1.1. Atribusi lain (JAKIM, NexaDash) dalam [`NOTICE`](NOTICE).
