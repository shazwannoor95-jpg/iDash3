# Changelog

Format ringkas berasaskan [Keep a Changelog](https://keepachangelog.com/). Tarikh dalam UTC.

## [0.2.0-dev] — 2026-11-07 (belum dikeluarkan sebagai release)

### Ditambah
- **iDash3 Debloat 1.0** (`com.shazwan.idash3.debloat`, modul `:debloat`): lumpuhkan bloatware head unit Saga (automatik dengan root, wizard tanpa root), buang log MTK/cache, pintasan WebView → Chrome, pulihkan semua; skrip ADB `tools/headunit/debloat-adb.sh|.bat`.
- `dist/RECOMMENDED-APPS.md`: Waze/NewPipe/SmartTube/Chrome yang serasi dengan API 27 32-bit.
- **Ikon gaya iOS** untuk app head unit (Radio, localmusic, Bluetooth, Backcar, CarSet/CanBus, Car Settings, Maps/Net Navi, File Manager, Gallery, Video/VLC, Sound Effects, Downloads, Calendar dinamik, dll.) — glyph asli pada jubin squircle; boleh dimatikan.
- **Kaca refraksi "baked"** (`GlassBake`): refraksi tepi, vibrancy, bevel dikira sekali per panel di thread latar; cache LRU 6 MB.
- Font **Inter / Inter Display** (SIL OFL).
- Ikon app dan jubin dock berbentuk **squircle**; butang Apps/Settings dengan glyph sendiri.
- **Liquid Glass v2**: tint adaptif per panel, lensing tepi, specular ikut sentuhan, kedalaman ikut saiz, peralihan reveal, spring sendiri, morph cecair folder, tekan anjal.
- **Mod prestasi** Auto/Rendah/Penuh (FrameMetrics p90 + RAM rendah).
- **Grid app berhalaman** dengan folder ringkas dan tindakan long-press (tanpa seret, selamat untuk memandu) — pilihan.
- Wallpaper dijana dalam kod (tiada aset pihak ketiga).
- CI GitHub Actions: build, ujian unit JVM, lint, pengawal statik, emulator API 27 (QA peranti + instrumentation satu kelas setiap kali dengan had masa).
- Dokumen: reka bentuk, pengesahan, audit, inventori & audit keserasian app head unit.

### Diubah
- Pakej baharu **`com.shazwan.idash3`** (standalone; dipasang bersama iDash/iDash2). Nama permission dalaman juga ditukar untuk elak konflik pemasangan.
- Kad kiri Home lebih lapang (jam besar, label diringkaskan, waktu solat 3 baris); panel kanan kosong secara lalai.
- Kegemaran dock "Files" mencuba `com.mediatek.filemanager` dahulu.

### Dibuang
- Aset MRiOS (ikon/wallpaper), font SF Pro dan SmartTube companion — lesen pengedaran tidak sah.
- Mod panel native (VirtualDisplay/embedding) — eksperimen yang tidak berjaya.

### Dibetulkan
- Perlindungan kontras teks pada kaca "baked" kini dikira daripada bitmap baked sebenar dan alfa badan yang berkesan (sebelum ini teks putih boleh jatuh bawah 4.5:1 selepas bake selesai; dikesan oleh `GlassRenderChecks`).
- `panel_mode=3` lama dipindahkan ke mod sah (disahkan oleh `PanelMigrationChecks`).
- Baris tetapan Home & Dock tidak lagi terpotong pada 1024×600.
- Swipe halaman pada ruang kosong grid; tiada lukisan semula ketika jari ditahan.

### Asal-usul
- Berasaskan iDash 0.1.8 (Fasa 6C) yang distabilkan dalam iDash2. Repo iDash/iDash2 tidak diubah.
