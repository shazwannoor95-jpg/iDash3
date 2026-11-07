# VALIDATION — iDash3

Rujukan: GitHub Actions **run 42320648951** (commit `1f90aae`): **kedua-dua job lulus.** Run seterusnya dengan perubahan visual (run 7, `ae037d1`) dan tema ikon iOS (run 8, `eaeb036`) juga **lulus** build + emulator; butiran jadual di bawah daripada run 42320648951.
Status: **PASS** = dijalankan dan lulus · **FAIL** = dijalankan dan gagal · **NOT RUN** = tidak dijalankan · **BLOCKED** = tidak boleh dijalankan dalam persekitaran ini.

> Semua ujian peranti dibuat pada **emulator API 27 x86_64** (GPU SwiftShader, 2 vCPU). Emulator menaikkan RAM 1024 MB yang diminta kepada **1536 MB** (log: "Increasing RAM size to 1536MB"), jadi profil 1 GB **tidak** disahkan. Angka emulator **bukan** prestasi head unit ARM32.

## Build (job "Build, unit tests, lint & guard")
| Semakan | Status | Butiran |
|---|---|---|
| Gradle wrapper validation | PASS | |
| Pengawal statik (`tools/ci/guard.sh`) | PASS | pakej, tiada embedding/VirtualDisplay, tiada aset MRiOS/SF Pro/SmartTube, JAKIM 4 zon |
| `assembleDebug`, `assembleDebugAndroidTest` | PASS | |
| `testDebugUnitTest` | PASS | 29 ujian JVM sebenar (Spring, GlassPerf, Adaptive, LiquidShape, HomeLayout), 0 gagal |
| `lintDebug` | PASS | 0 ralat, ±54 amaran (kebanyakan `SetTextI18n`) |
| Pengawal APK | PASS | `com.shazwan.idash3`, tiada pustaka native, ±1.3 MB |
| APK debug + SHA256 | PASS | artifact `qa-apk-debug` (`SHA256SUMS.txt`); ditandatangani kunci debug generik, **bukan release** |

## QA peranti (emulator, `tools/ci/device-qa.sh`)
| Semakan | Status | Butiran |
|---|---|---|
| Pasang | PASS | |
| Lancar | PASS | `am start -W` TotalTime 3784 ms (emulator) |
| Butang HOME (iDash3 ditetapkan sebagai home) | PASS | |
| Back di Home kekal di Home | PASS | |
| Buka app luar (Settings) | PASS | |
| Kembali ke Home dari app luar | PASS | |
| Tukar saiz skrin semasa berjalan → 800×480 | PASS | proses kekal hidup |
| Kembali → 1024×600 | PASS | |
| Tiada crash / tiada ANR dalam logcat | PASS | |
| Tangkapan skrin 1024×600, 800×480, mod Penuh/Rendah | PASS (dikumpul) | artifact `emulator-qa` → `qa-device/screens/` |
| meminfo | DIUKUR | PSS ±29.8 MB (Penuh) / ±29.7 MB (Rendah); `Idash3Checks` PSS 24.5 MB |
| gfxinfo, mod Penuh | DIUKUR | 103 bingkai, p90 36 ms, 96% "janky" — GPU perisian SwiftShader; **tidak mewakili head unit** |
| gfxinfo, mod Rendah | DIUKUR | 1 bingkai bagi input yang sama: efek sentuhan/anjal tidak melukis apa-apa. Grid hanya 1 halaman pada emulator ini, jadi swipe **tidak** menguji animasi halaman |

## Instrumentation (`tools/ci/run-instrumentation.sh`, satu kelas setiap kali, had masa 300 s)
| Kelas | Jenis | Status | Butiran |
|---|---|---|---|
| Idash3Checks | gate | PASS | grid Home, sasaran sentuh ≥48 dp, susunan disimpan, folder buka (morph)/tutup dengan Back, overlay dibuang, mod prestasi, 5 varian ikon, semua kaca selesai `reveal` |
| SmokeChecks | gate | PASS | 57 semakan UI |
| QualityChecks | gate | PASS | 50 |
| StabilityChecks | gate | PASS | 104 |
| WallpaperChecks | gate | PASS | 13 (wallpaper dijana) |
| GlassRenderChecks | gate | PASS | 36 |
| GlassMaterialChecks | gate | PASS | 29 |
| SettingsChecks | gate | PASS | 27 |
| PanelMigrationChecks | gate | PASS | 10 — pembetulan `panel_mode=3` kini **disahkan dengan ujian** |
| DeviceReadinessChecks | maklumat | PASS | 68 |
| SettingsCompleteChecks | maklumat | PASS | 271 (gagal pada run 2: baris tetapan terpotong; dibetulkan) |
| SettingsAccessChecks / SettingsResetChecks | maklumat | PASS | 7 / 8 |
| ControlLayoutChecks / ControlGlassChecks | maklumat | PASS | 56 / 36 |
| AppearanceChecks / RenderChecks / GradientChecks | maklumat | PASS | 62 / 221 / 5 |
| CompactChecks | maklumat | PASS | 107 |
| PerformanceAuditChecks | maklumat | PASS | 10 |
| SystemControlsChecks | maklumat | PASS | 17 (kelas ini tergantung dalam CI iDash2) |
| MediaUtilitiesChecks | maklumat | BLOCKED | memerlukan app fixture media yang tidak dibawa ke iDash3 |
| Glass6aChecks, ControlVisualChecks | maklumat | FAIL (persekitaran) | ujian tangkapan skrin warisan menjangka saiz paparan tertentu; tidak sepadan dengan profil emulator ini. Bukan kegagalan app; perlu dikemas kini atau dibuang |

## Belum disahkan (NOT RUN / BLOCKED)
| Perkara | Status | Sebab |
|---|---|---|
| Head unit Proton Saga sebenar (ARM32, Android 8.1) | NOT RUN | tiada akses peranti |
| Prestasi/kelancaran ARM32, frame time sebenar | NOT RUN | emulator x86_64 tidak mewakili |
| Profil RAM tepat 1 GB | BLOCKED | emulator naikkan ke 1536 MB |
| Animasi swipe halaman (lebih dari 1 halaman) di bawah gfxinfo | NOT RUN | emulator hanya ada ±20 app (1 halaman) |
| WebView YouTube, FM OEM, MediaSession sebenar | NOT RUN | bergantung pada peranti / rangkaian / app pihak ketiga |
| APK release | NOT RUN | keputusan signing/keystore belum dibuat |

## Cara uji pada head unit (cadangan)
1. Pasang `iDash3-debug-QA.apk` (artifact `qa-apk-debug`); ia dipasang bersama iDash/iDash2 tanpa konflik.
2. Settings → Home & Dock → **Liquid Glass → Mod prestasi**: bandingkan Penuh dan Rendah.
3. `adb shell dumpsys gfxinfo com.shazwan.idash3` selepas beberapa swipe/folder, dan `dumpsys meminfo com.shazwan.idash3`.
