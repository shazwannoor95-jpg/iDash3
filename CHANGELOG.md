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

### Dokumentasi
- **README dua bahasa:** Bahasa Inggeris dan Bahasa Melayu dalam satu fail, dengan jadual muat turun dikongsi dan pautan bahasa di bahagian atas, supaya pengguna bukan Melayu boleh memahami ciri, keperluan dan had projek.
- **Panduan tangkapan skrin:** [`docs/SCREENSHOTS.md`](docs/SCREENSHOTS.md) menyenaraikan enam skrin pratonton (nama fail, apa yang patut kelihatan, arahan `adb`) dan [`tools/screenshots.sh`](tools/screenshots.sh) merakamnya ke `docs/images/`. Tiada PNG dimasukkan lagi: persekitaran bina tiada head unit ARM32 dan emulator tidak boleh berjalan tanpa KVM.

### Dibetulkan
- **Butang petak Drive tidak menghentikan split-screen:** butang segi empat pada bar kini menamatkan pasangan split-screen dan membiarkan app fokus skrin penuh, satu hala (tekan lagi tidak mendok semula). `Keluar` tidak lagi menghantar togol yang akan mendok semula app selepas split dihentikan.
- **Bar terapung kekal selepas keluar split:** apabila launcher iDash3 kembali ke hadapan (tekan Home atau app split ditutup sistem), bar Drive ditutup dan sesi Drive ditamatkan, jadi pil itu tidak lagi terapung di atas skrin Home.
- **Butang petak tidak menghentikan split:** flag "sudah skrin penuh" dibuang — setiap tekan bertanya kepada sistem sekali lagi dan tidak lagi pulang awal dengan toast palsu. Tekan lama butang itu keluar Drive sepenuhnya (tidak memerlukan aksesibiliti).
- **Susunan grid Aplikasi tidak teratur:** baris terakhir yang tidak penuh tidak lagi meregang; grid kini 4–6 lajur tetap (1024×600 = 5) dengan ruang pengisi supaya ikon dan label lurus.
- **Panel Aplikasi Drive kosong:** senarai app dimuat semula setiap kali panel dibuka, panel bersaiz mengikut kandungan (grid menatal selepas separuh skrin) dan bukannya kotak 66% tetap, dan jika tiada app dijumpai ia memaparkan bilangan app dengan arahan tekan untuk muat semula.
- **P1 dan P2 di-revert semula (2026-11-10):** dua pembetulan prestasi yang menyentuh kaca — pengesanan memori Auto (`GlassPerf`) dan tunda permintaan bake semasa animasi masuk (`GlassSurface`) — dibuang semula atas permintaan kerana ia mengubah penampilan kaca pada head unit 1 GB. Kaca kembali sama seperti sebelum audit: tiada lensa, bevel, specular, refleksi atau refraksi yang hilang secara automatik. Mod **Rendah** masih boleh dipilih secara manual dalam Settings → Mod prestasi.
- **Keluar Drive masih menjalankan langkah tertangguh:** rantaian `postDelayed` (dock, app kedua, bar) kini dibatalkan oleh `cancelPending()` dalam `stop()`, jadi bar/split tidak muncul semula selepas pengguna keluar.
- **Launcher dibina semula apabila saiz fon/resolusi/`uiMode` berubah:** `configChanges` dilengkapkan (`smallestScreenSize`, `screenLayout`, `fontScale`, `uiMode`, `locale`, `layoutDirection`, `keyboard`, `navigation`); `onConfigurationChanged` → `render()` sudah wujud.
- **Perkhidmatan aksesibiliti menerima `typeWindowStateChanged` tanpa sebab:** atribut dibuang daripada `res/xml/split_service.xml`; ia hanya menggunakan tindakan global (`performGlobalAction`).
- **Kotak hitam kosong di atas app:** bar terapung kini pil **kompak di tengah bawah** (lebar `WRAP_CONTENT`, tanpa `FLAG_LAYOUT_NO_LIMITS`) dan butang launcher membuka **panel separa telus 66% tinggi** yang boleh ditutup dengan ✕ atau tekan kawasan gelap — bukan lagi tetingkap hitam penuh skrin. Panel juga memaparkan mesej jika senarai app gagal dibaca, jadi ia tidak pernah menjadi kotak hitam tanpa isi.
- **Kad Drive terpotong pada skrin rendah:** label PETA/MEDIA dan nama app kini muat dalam kad — ikon mengambil baki tinggi (lebar maksimum 64dp, `FIT_CENTER`) dan hint "Tekan untuk tukar" disembunyikan apabila kad lebih pendek daripada 120dp. APK QA dalam `dist/` dibina semula daripada commit ini.
- **Video YouTube hitam dalam panel (audio ada):** panel kini melaporkan VP9/AV1 sebagai tidak disokong supaya YouTube memilih H.264 (kebanyakan cip head unit tiada penyahkod VP9/AV1); resolusi PiP untuk laman web kini melalui lebar viewport halaman, bukan skala pada WebView (skala boleh menyembunyikan lapisan video).
- Perlindungan kontras teks pada kaca "baked" kini dikira daripada bitmap baked sebenar dan alfa badan yang berkesan (sebelum ini teks putih boleh jatuh bawah 4.5:1 selepas bake selesai; dikesan oleh `GlassRenderChecks`).
- `panel_mode=3` lama dipindahkan ke mod sah (disahkan oleh `PanelMigrationChecks`).
- Baris tetapan Home & Dock tidak lagi terpotong pada 1024×600.
- Swipe halaman pada ruang kosong grid; tiada lukisan semula ketika jari ditahan.

### Asal-usul
- Berasaskan iDash 0.1.8 (Fasa 6C) yang distabilkan dalam iDash2. Repo iDash/iDash2 tidak diubah.
