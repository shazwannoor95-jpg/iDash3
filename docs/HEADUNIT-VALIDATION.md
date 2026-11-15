# HEADUNIT-VALIDATION — fungsi head unit & matriks ujian

Sasaran: TEST9278B / ALPS 8227L, Android 8.1 (SDK 27), 1024×600 mdpi (lihat `docs/HARDWARE-BASELINE.md`).
Asas kod: `main` @ `ad16f48`. Enjin Liquid Glass V2 **tidak diubah**; tiada ciri split/VirtualDisplay baharu ditambah.

Status yang digunakan: **PASS** (dijalankan, lulus) · **FAIL** (dijalankan, gagal) · **NOT RUN** (belum dijalankan) · **BLOCKED** (tidak boleh dijalankan dalam persekitaran ini).

> **Penting:** tiada peranti atau emulator ARM32 dalam sesi ini. Semua baris "peranti" di bawah ialah **NOT RUN/BLOCKED**, bukan lulus. Ujian yang benar-benar dijalankan ialah binaan, ujian unit, lint, guard dan pemeriksaan APK (lihat §4).

---

## 1. Matriks fungsi

Lajur **API biasa** = boleh dikawal dengan Android SDK standard tanpa root/firmware. **OEM** = memerlukan perkhidmatan/perkakasan pengeluar.

| Fungsi | Pelaksanaan | API biasa | Perlu OEM | Status | Nota / bukti |
|---|---|---|---|---|---|
| **Wi-Fi** | `WifiManager.getWifiState`, `setWifiEnabled` (API < 29) | Ya | Tidak | NOT RUN (peranti) | `QuickControls.java:66,75`; pada API 27 `setWifiEnabled` masih dibenarkan. Jika ROM menolak, app membuka Settings Wi-Fi. Kebenaran `ACCESS_WIFI_STATE` + `CHANGE_WIFI_STATE` (maxSdk 28) ada |
| **Bluetooth** | `BluetoothAdapter.getState/enable/disable` (API < 33) | Ya | Tidak | NOT RUN (peranti) | `QuickControls.java:43,67,76`; `BLUETOOTH_ADMIN` (maxSdk 30) ada. Peranti tiada BLE — iDash3 tidak memerlukan BLE |
| **GPS** | **Tidak digunakan langsung** | — | — | NOT APPLICABLE | Tiada `ACCESS_FINE_LOCATION`; zon JAKIM dipilih manual. Jika mahu ETA/navigasi, ia tugas app peta, bukan launcher |
| **App Drawer** | Grid + carian dalam app; `queryIntentActivities` pada worker | Ya | Tidak | NOT RUN (peranti) | `LauncherActivity.java:169-201`; ikon 56 px, muat pada worker, `catch (OutOfMemoryError)` fallback |
| **Floating Dock** | Dock bawah dalam app + bar terapung Drive (`TYPE_APPLICATION_OVERLAY`) | Ya (`SYSTEM_ALERT_WINDOW`) | Tidak | NOT RUN (peranti) | `LauncherActivity.java:152-158`; `DriveBarService.java:110-125`; bar hanya muncul jika `Settings.canDrawOverlays` |
| **Widget jam & tarikh** | `TextClock`/ticker 1 s + `SimpleDateFormat` mengikut `TimeZone` peranti | Ya | Tidak | NOT RUN (peranti) | `LauncherActivity.java:39,300-310`; bertukar 12/24 jam ikut pref; tiada `AlarmManager`, jadi tiada isu Doze |
| **Waktu solat JAKIM** | `https://www.e-solat.gov.my` + 4 jadual terbina (`SGR01-03`, `WLY01`) + cache `files/jakim-<zon>.json` | Ya (Internet) | Tidak | NOT RUN (peranti) | `JakimCalendar.java:45-60`; timeout 8 s; kegagalan mengekalkan cache lama dan memaparkan Toast; data disahkan format (regex tarikh/masa) sebelum diterima |
| **Kalendar** | `DatePickerDialog` paparan tarikh; **tiada** bacaan acara (tiada `READ_CALENDAR`) | Ya | Tidak | NOT RUN (peranti) | `LauncherActivity.java:showCalendar`; ini paparan, bukan penyegerakan kalendar |
| **MediaSession / Now Playing** | `NotificationListenerService` + `MediaSessionManager.getActiveSessions` | Ya (kebenaran khas) | Tidak | BLOCKED (perlu app media + kebenaran) | `ControlMedia.java:11-25`; hanya metadata + transport, tidak membaca kandungan notifikasi; `isNotificationListenerAccessGranted` API 27 |
| **Volume** | `AudioManager` `STREAM_MUSIC`, `setStreamVolume` | Ya | Tidak | NOT RUN (peranti) | `QuickControls.java:78-81`; had kadar 120 ms; menghormati `isVolumeFixed()`; tidak merampas butang keras |
| **Brightness** | `Settings.System.putInt(SCREEN_BRIGHTNESS)` + `WRITE_SETTINGS` | Ya (kebenaran khas) | **Mungkin** | NOT RUN (peranti) | `QuickControls.java:77`; jika RAM auto-brightness aktif, app enggan menulis dan menyuruh pengguna buka Display. Sesetengah head unit mengawal backlight melalui MCU — pada unit itu slider akan berkesan pada Android tetapi tidak pada skrin |
| **Wallpaper & Settings** | 8 wallpaper dijana + imej pengguna (disalin ke storan peribadi, maks 16 MiB) + Settings dalam app | Ya | Tidak | NOT RUN (peranti) | `Wallpapers.java`; `CustomWallpaper.java`; `DashSettings.java`. Settings Android dibuka melalui Intent, bukan diganti |
| **Radio FM OEM** | Protokol eksplisit `com.android.xygala.ACTION_CONTROL_RADIO` ke `com.android.fmradio/.FmtestService` **ATAU** `com.acloud.stub.extradio/.RadioService` | Tidak | **Ya** | BLOCKED (perlu firmware sebenar) | `DashFmController.java:10-45`; versi disemat ("3.1" / "2.3.24"); reflection `android.os.SystemProperties` untuk mod Auto; jika firmware lain → mesej "not supported", bukan crash. **Tiada root digunakan** |
| **DND / Focus** | `NotificationManager.setInterruptionFilter` + `ACCESS_NOTIFICATION_POLICY` | Ya (kebenaran khas) | Tidak | NOT RUN | `ControlUtilities.java:26-30` |
| **Timer / Kalkulator** | Intent `AlarmClock.ACTION_SHOW_TIMERS`, `CATEGORY_APP_CALCULATOR` + carian label | Ya | Tidak | NOT RUN | `ControlUtilities.java:21-22`; jika tiada app, butang dilumpuhkan dengan label jelas |

### Fungsi yang **tidak** boleh dikawal tanpa OEM (jangan cuba paksa)

| Fungsi | Sebab |
|---|---|
| Penalaan FM sebenar (audio keluar dari tuner) | Perkakasan tuner hanya boleh diakses melalui perkhidmatan OEM; iDash3 hanya menghantar niat yang didokumenkan oleh firmware tersebut |
| Kecerahan panel sebenar (backlight MCU) | Dikawal firmware/MCU, bukan `Settings.System` |
| Kawalan amplifier / DSP / channel | Tiada API awam; perlu protokol OEM |
| Data kenderaan (speed, RPM, CAN) | Tiada API awam; panel "Kereta · Saga" ialah **imej**, bukan model 3D hidup |
| Kamera undur, stereng, butang fizikal | Dikendalikan sistem; iDash3 tidak campur tangan |

---

## 2. Skrip ujian manual pada head unit (untuk anda jalankan)

Jalankan mengikut urutan; tandakan pada §3. Anggaran 15 minit.

1. **Pasang & mula:** pasang APK, tekan HOME, pilih iDash → Home muncul < 3 s.
2. **App Drawer:** tekan ikon Apps pada dock → senarai app muncul; taip 3 huruf dalam carian → senarai menapis; Back → Home.
3. **Dock:** tetapkan Bluetooth/Files/YouTube (tekan lama → pilih app) → buka setiap satu → kembali ke Home.
4. **Widget:** tukar 12/24 jam dalam Settings → jam bertukar tanpa keluar app.
5. **JAKIM:** Settings → Waktu Solat → pilih zon anda → widget memaparkan waktu + countdown. Matikan Wi-Fi → "Kemas kini JAKIM" → mesej gagal, jadual lama kekal.
6. **Control Center:** buka, hidup/matikan Wi-Fi dan BT, laraskan volume dan kecerahan, tekan Previous/Play/Next dengan app muzik aktif.
7. **Drive:** pilih app peta + media → "Mula Drive" → app 1 buka, split, app 2 buka, bar terapung muncul di bawah tengah.
8. **Bar Drive:** tekan ikon grid → panel "Aplikasi" (bukan skrin hitam penuh) → ✕ menutup; tekan butang segi empat → satu app penuh; tekan lagi → kembali split.
9. **Keluar Drive:** tekan "Keluar Drive" → split tamat, bar hilang, kembali ke launcher.
10. **Paperwall/Settings:** tukar wallpaper, tukar saiz dock, reset settings (pengesahan muncul).
11. **Idle 5 minit:** tinggalkan di Home; sahkan tiada kelipan, tiada Toast berulang, tiada pemanasan melampau.
12. **Reboot:** selepas boot, launcher muncul sendiri (HOME ditetapkan), tetapan kekal.

---

## 3. Jadual keputusan (isi selepas ujian peranti)

| # | Semakan | Dijangka | Keputusan | Catatan |
|---|---|---|---|---|
| 1 | Pasang APK | Berjaya | ⬜ | |
| 2 | Lancar sebagai HOME | < 3 s | ⬜ | |
| 3 | App Drawer + carian | Berfungsi | ⬜ | |
| 4 | Lancar app luar & kembali | Berfungsi | ⬜ | |
| 5 | Back di Home | Kekal di Home | ⬜ | |
| 6 | Tekan HOME dari app lain | Lihat **A3** (kembali ke Home) | ⬜ | Adakah anda mahu ia kekal di halaman semasa? |
| 7 | Jam/tarikh | Betul, tidak terpotong | ⬜ | |
| 8 | JAKIM (dengan & tanpa Internet) | Jadual + fallback cache | ⬜ | |
| 9 | Control Center Wi-Fi/BT | Togol berjaya atau buka Settings | ⬜ | |
| 10 | Volume / kecerahan | Slider berkesan | ⬜ | Kecerahan mungkin dikawal MCU |
| 11 | Media Next/Play | Berfungsi selepas kebenaran | ⬜ | |
| 12 | Mula Drive | Split 2 app | ⬜ | |
| 13 | Bar Drive (grid/✕/segiempat) | Panel separa telus, boleh tutup | ⬜ | |
| 14 | Keluar Drive | Bar hilang, split tamat | ⬜ | |
| 15 | Rotasi/resolusi berubah | Tiada crash | ⬜ | |
| 16 | Idle 5 min | Tiada jank berterusan | ⬜ | |
| 17 | Reboot | Launcher & tetapan kekal | ⬜ | |
| 18 | iDash asal masih ada | Ya | ⬜ | `pm list packages` |

---

## 4. Matriks ujian automatik — dijalankan dalam audit ini

Persekitaran: JDK 17 (Temurin), Android SDK 35, Gradle 8.9, `GRADLE_USER_HOME` berasingan. **Tiada emulator** (tiada KVM) dan **tiada peranti ARM32**.

| Semakan | Arahan | Status | Bukti |
|---|---|---|---|
| Bina debug | `./gradlew :app:assembleDebug` | **PASS** | BUILD SUCCESSFUL (17 s) |
| Ujian unit | `./gradlew :app:testDebugUnitTest` | **PASS** | **46 ujian, 0 gagal** (10 kelas: Adaptive, DrivePlan, GlassBake, GlassPerf, HomeLayout, IosGlyphs, LiquidShape, ResolutionStage, Spring, WebVideoScript) |
| Lint | `./gradlew :app:lintDebug` | **PASS** (0 ralat) | 59 amaran: `SetTextI18n` 41, `ViewConstructor` 10, `ObsoleteSdkInt` 2, `ClickableViewAccessibility` 2, `PrivateApi` 1 (`DashFmController.java:44`), `DiscouragedApi` 1, `DataExtractionRules` 1, `DrawAllocation` 1 (`DriveBarService.java:310`) |
| Bina APK ujian | `./gradlew :app:assembleDebugAndroidTest` | **PASS** | 124,856 bait |
| Bina modul debloat | `./gradlew :debloat:assembleDebug` | **PASS** | 20,094 bait |
| Pengawal statik + APK | `bash tools/ci/guard.sh dist/iDash3-0.2.0-dev-debug-QA.apk` | **PASS** | pakej betul, tiada native lib, < 8 MB, tiada VirtualDisplay/aset terlarang |
| Sahkan tandatangan | `apksigner verify --print-certs -v` | **PASS** | v2 sah; sijil debug `cd73822…c8d60d` |
| Metadata APK | `aapt2 dump badging` / `xmltree` | **PASS** | `com.shazwan.idash3`, 1 / 0.2.0-dev, minSdk 26, targetSdk 35 |
| Instrumentation API 27 | `tools/ci/run-instrumentation.sh` (perlu emulator) | **NOT RUN** | Tiada emulator/KVM dalam persekitaran ini |
| Pasang & lancar (emulator) | `tools/ci/device-qa.sh` | **NOT RUN** | Sama |
| Back/Home, App Drawer, persistence (peranti) | manual | **BLOCKED** | Tiada peranti |
| Tangkapan skrin sebenar | — | **NOT RUN** | Tiada emulator/peranti |
| `logcat`, `dumpsys meminfo`, `dumpsys gfxinfo` | — | **NOT RUN** | Tiada peranti |

> Rujukan sejarah: `docs/VALIDATION.md` merekodkan emulator API 27 x86_64 (run 42320648951) **PASS** untuk pasang/lancar/rotasi/tiada ANR, PSS ≈ 29.8 MB dan p90 36 ms pada GPU **SwiftShader**. Angka itu **bukan** ukuran ARM32/Mali-450 dan tidak diulang dalam audit ini.

---

## 5. Apa yang perlu diperbaiki sebelum ujian peranti seterusnya

Mengikut keutamaan (butiran penuh dalam `docs/PERFORMANCE-AUDIT.md` dan `docs/ANDROID-COMPATIBILITY-AUDIT.md`):

| # | Item | Status |
|---|---|---|
| 1 | **P1** — ambang memori supaya mod Auto memilih LOW pada peranti 1 GB | **DI-REVERT** — mengubah penampilan semua panel |
| 2 | **A1** — lengkapkan `configChanges` | **DIBETULKAN** |
| 3 | **A2** — buang `accessibilityEventTypes` yang tidak digunakan | **DIBETULKAN** |
| 4 | **Drive callback** — batalkan langkah tertangguh dalam `stop()` | **DIBETULKAN** (`cancelPending`) |
| 5 | **P2** — jangan bake kaca semasa animasi masuk | **DI-REVERT** bersama P1 (mengubah fasa reveal) |
| 6 | **P6** — kitar semula `VelocityTracker` dalam grid | Terbuka |

Binaan QA semula selepas revert P1/P2: `assembleDebug` PASS, **44 ujian unit 0 gagal**, lint **0 ralat / 59 amaran**, `guard.sh` PASS. APK `dist/iDash3-0.2.0-dev-debug-QA.apk` (1,748,828 bait) dibina semula. Ujian pada peranti masih **NOT RUN/BLOCKED**; yang masih berkuat kuasa daripada sesi audit ini hanyalah `configChanges` (A1), `split_service.xml` (A2) dan pembatalan langkah tertangguh Drive.
