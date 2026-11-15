# REVIEW — Fasa 1 (audit reka bentuk & perbandingan)

Dibaca dari `iDash2@ci/fasa-8a` (= 6C + pembetulan). Fail-fail adalah satu baris padat (82 baris >400 aksara dalam `app/`; `GlassSurface.draw` ialah satu baris), jadi rujukan lokasi diberi pada nombor baris fail itu.

## 1. Sudah wujud dan berfungsi (jangan bina semula)
- **GlassSurface** (109 baris): body gradient, sheen, reflection radial, bevel, specular rim melengkung, edge rim, bayang dua lapis, frost `BitmapShader` yang diposisikan mengikut kedudukan skrin panel (fake backdrop), kualiti LOW/BALANCED/HIGH, gaya Glass/Transparent/Solid (`style` 0/1/2).
- **GlassBackdrop**: satu cache frost bagi setiap Activity, dihasilkan di worker thread; downsample ke 128/224/320 px, box-blur 3 laluan, purata warna = tint. `publish()` menyegarkan permukaan melalui `WeakReference`.
- **Adaptive**: `light` dikira dari luminans purata wallpaper; `protectionAlpha` sampel 9 piksel di belakang setiap teks/glyph dan tambah scrim setempat untuk nisbah kontras ≥4.9 (teks)/3.8 (glyph); `IosStyle.contrast` menukar warna teks.
- **GlassAppearance/AppearanceEditor**: nada, lutsinar 10–65, kualiti, tint, reflection, motion; draf tidak ditulis sehingga disimpan; respek `ANIMATOR_DURATION_SCALE`.
- Dashboard JAKIM (4 zon), kalendar, App Library, Control Center (+MediaSession), Settings, dock, galeri wallpaper + wallpaper peribadi, FM, Saga angle, WebView YouTube.

## 2. Hanya dalam eksperimen (tidak diimport)
`experiments/phase6d-a1` (host/probe VirtualDisplay), SmartTube companion, `system-theme` overlay, native fixture. A1 tidak berjaya dalam emulator.

## 3. Dibuang secara sengaja
Mod panel "SmartTube native": `DashNativeFrame/Proxy/Geometry`, `allowEmbedded`, `PanelChecks` (keputusan anda: embedding dibatalkan).

## 4. Masalah seni bina/prestasi yang boleh dibuktikan dari source
| # | Isu | Lokasi | Bukti |
|---|---|---|---|
| A | `draw()` setiap bingkai memanggil `getLocationOnScreen` dan `geometryHash()` yang menelusuri keseluruhan subpokok pandangan + `Layout` teks | `GlassSurface.java:45` | Kod; kos linear dengan bilangan anak × bilangan panel kaca. Belum diukur |
| B | `draw()` dengan `post()` kontras dan membina semula shader bila `revision` berubah | `:45` | Kod |
| C | Blur box O(radius) per piksel (bukan sliding window) | `GlassBackdrop.java` `blur()` | Kod; di worker thread, 320 px maks → boleh diterima |
| D | Tiada pensampelan per-panel tint; tint adaptif = satu purata wallpaper sejagat | `GlassBackdrop.prepare` | Kod. Ciri 3B #3 memerlukan purata **per panel** |
| E | Tiada lensing/refraction; "specular" statik (tidak ikut sentuhan/tilt) | `GlassSurface` | Kod |
| F | Tiada kelas spring/morph; animasi hanya fade/slide 180 ms | cari `Spring`/`morph`: tiada | grep |
| G | Ikon aplikasi tunggal-lapis (PNG MRiOS), tiada varian Dark/Clear/Tinted | `MrIosTheme.java` | Kod |
| H | Polling: ticker jam 1 s; `audioFallback` 2 s | `LauncherActivity:38`, `QuickControls:49` | Kod; dijeda semasa tidak kelihatan (`onStop`) |
| I | Gaya kod: satu baris panjang, sukar disemak/diuji | 82 baris >400 aksara | `awk` |
| J | Tiada ujian unit (`src/test` tiada); hanya instrumentation | `app/src` | `ls` |

## 5. Pemerhatian visual vs Liquid Glass iOS 26 (berdasarkan source, bukan paparan)
Ada: rim, sheen, frost, tint adaptif global, bayang. Tiada: lensing tepi, specular responsif, morph cecair, ikon berlapis, saiz-bergantung-kedalaman. Jurang ini ialah skop Fasa 3/3B.

## 6. Matriks sumber
| Fungsi | Lokasi asal | Versi terbaik | Tindakan | Risiko regresi |
|---|---|---|---|---|
| Glass engine | `GlassSurface/Backdrop/Appearance`, `IosStyle` | iDash2 (= 6C) | Import dahulu, refactor bertahap di Fasa 3 | Sederhana (draw satu baris) |
| Dashboard/Home | `LauncherActivity`, `HomeWidgets` | iDash2 | Import; pecahkan fail besar (44 KB) | Tinggi jika refactor serentak |
| JAKIM/Kalendar | `JakimCalendar`, `assets/jakim` | iDash2 (identik) | Import + atribusi NexaDash MIT | Rendah |
| Control Center/Media | `QuickControls`, `Control*`, `MediaAccessService` | iDash2 | Import | Sederhana (permission) |
| Settings | `DashSettings`, `Settings*` | iDash2 (tanpa SmartTube) | Import | Rendah |
| Panel web/FM/Saga | `DashWebPlayer`, `DashFm*`, `SagaAngleView` | iDash2 | Import; FM bergantung OEM | Sederhana |
| Panel native | `DashNative*` | iDash (dibuang) | **Jangan import** | — |
| `panel_mode` migrasi | `LauncherActivity:210` | iDash2 `ci/` | Import + `PanelMigrationChecks` | Rendah |
| CI | `.github/workflows`, `tools/ci` | iDash2 `ci/` | Rujuk, tulis semula (lihat Fasa 5) | — |
| Aset MRiOS/SF Pro | `assets/mrios`, `res/font` | — | **Ditahan** (lesen tak sah; lihat AUDIT) | Visual |

## Keputusan reka bentuk diperlukan daripada anda
1. Aset MRiOS (ikon, wallpaper, SF Pro): ganti dengan aset sendiri/berlesen, atau teruskan peribadi sahaja?
2. Font: ganti SF Pro dengan fon berlesen terbuka (mis. Inter/Roboto)?
