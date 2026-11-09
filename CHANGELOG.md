# Changelog

Format ringkas berasaskan [Keep a Changelog](https://keepachangelog.com/). Tarikh dalam UTC.

## [0.2.0-dev] — 2026-11-07 (belum dikeluarkan sebagai release)

### Ditambah
- **Drive** (skrin gaya Android Auto): pilih app peta dan app media, dok automatik ke split-screen Android (peta kiri / media kanan, boleh ditukar), bar terapung dengan jam, Back/Home/Recents, kelantangan, pintasan dan keluar. `SplitService` (aksesibiliti, hanya tindakan global), `DriveController`, `DriveBarService`, `DrivePage`, `DrivePlan` (diuji unit), `DriveChecks` (instrumentation). Lihat `docs/DRIVE-MODE.md`.
- **Susun atur & skrin penuh gaya Android Auto:** butang **⇄** kini menukar **navigasi dekat pemandu** / **media dekat pemandu** (Change Layout; pemandu kanan lalai, pilihan disimpan dan migrasi dari tetapan lama), dan butang **segi empat** pada bar membuat app fokus skrin penuh, tekan lagi untuk dok semula pasangan split.
- **Resolusi panel gaya PiP** (`ResolutionStage`): kandungan panel media dilukis pada resolusi maya 16:9 (360p/480p/540p/720p, lalai 480p) dan dikecilkan seragam supaya muat kotak panel; sentuhan dipetakan semula. Butang resolusi pada kepala panel. Digunakan untuk YouTube web dan FM.
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
- **Bar Drive gaya Android Auto:** bar bawah dengan **App launcher** (grid app + tile Exit + butang dashboard), pintasan peta/media/telefon/Settings, dan **status bateri + jam**. Mikrofon dikecualikan sepenuhnya; butang Back/Home/Recents, volume dan ✕ dibuang daripada bar (guna kawalan peranti).
- **Bar terapung Drive dikecilkan:** sel butang 52dp → 38dp, jam 20sp → 15sp, padding pil dan radius sudut dikurangkan, jarak dari tepi bawah 10dp → 6dp — pil tidak lagi menutup kandungan split-screen.
- Home: tajuk "Shazwan's Saga"; kad jam kini memaparkan "TODAY" + jam + tarikh di tengah (kad Today berasingan dibuang); kad waktu solat berpusat dengan hierarki font (waktu seterusnya > baris JAKIM > jadual); susunan kad dipusatkan secara menegak.
- Panel media: saiz frekuensi FM menyesuaikan ruang (tidak lagi terlindung oleh butang).
- Label TODAY tidak lagi mempunyai bulatan scrim gelap (label aksen dikecualikan daripada scrim, diberi bayang teks sendiri).
- Kaca baked hanya untuk panel yang dipaparkan pada window (view offscreen kekal dengan frost).
- Pakej baharu **`com.shazwan.idash3`** (standalone; dipasang bersama iDash/iDash2). Nama permission dalaman juga ditukar untuk elak konflik pemasangan.
- Kad kiri Home lebih lapang (jam besar, label diringkaskan, waktu solat 3 baris); panel kanan kosong secara lalai.
- Kegemaran dock "Files" mencuba `com.mediatek.filemanager` dahulu.

### Dibuang
- Aset MRiOS (ikon/wallpaper), font SF Pro dan SmartTube companion — lesen pengedaran tidak sah.
- Mod panel native (VirtualDisplay/embedding) — eksperimen yang tidak berjaya.

### Dibetulkan
- **Kad Drive terpotong pada skrin rendah:** label PETA/MEDIA dan nama app kini muat dalam kad — ikon mengambil baki tinggi (lebar maksimum 64dp, `FIT_CENTER`) dan hint "Tekan untuk tukar" disembunyikan apabila kad lebih pendek daripada 120dp. APK QA dalam `dist/` dibina semula daripada commit ini.
- **Video YouTube hitam dalam panel (audio ada):** panel kini melaporkan VP9/AV1 sebagai tidak disokong supaya YouTube memilih H.264 (kebanyakan cip head unit tiada penyahkod VP9/AV1); resolusi PiP untuk laman web kini melalui lebar viewport halaman, bukan skala pada WebView (skala boleh menyembunyikan lapisan video).
- Perlindungan kontras teks pada kaca "baked" kini dikira daripada bitmap baked sebenar dan alfa badan yang berkesan (sebelum ini teks putih boleh jatuh bawah 4.5:1 selepas bake selesai; dikesan oleh `GlassRenderChecks`).
- `panel_mode=3` lama dipindahkan ke mod sah (disahkan oleh `PanelMigrationChecks`).
- Baris tetapan Home & Dock tidak lagi terpotong pada 1024×600.
- Swipe halaman pada ruang kosong grid; tiada lukisan semula ketika jari ditahan.

### Asal-usul
- Berasaskan iDash 0.1.8 (Fasa 6C) yang distabilkan dalam iDash2. Repo iDash/iDash2 tidak diubah.
