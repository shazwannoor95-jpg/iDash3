# Audit crash dan lag — 17 November 2026

Kod: `a96888d`. Sasaran: Proton Saga VVT 2016, head unit TEST9278B / MT8227L, 4× Cortex-A7 ARM32, Mali-450 MP ES2, RAM 1 GB, 1024×600 mdpi, API 27. Spesifikasi daripada diagnostik yang direkodkan dalam `HARDWARE-BASELINE.md`, bukan pemeriksaan ADB baharu pada kereta. Pengguna melaporkan masalah ketika navigasi, YouTube/Drive, dan wallpaper/Appearance.

Audit ini tidak mengubah kod aplikasi. Penemuan kod di bawah ialah laluan risiko yang boleh diperiksa, bukan pengesahan bahawa semuanya menyebabkan crash pengguna. Tiada log crash daripada head unit tersedia. Semakan runtime menggunakan emulator API27 x86_64 TCG / SwiftShader tanpa KVM; masa dan memori emulator bukan benchmark ARM32.

## Penemuan mengikut keutamaan

### A1 — Tinggi: kegagalan peruntukan imej boleh menamatkan proses

`CustomWallpaper.java:12–17,25` mendekod imej tanpa menangkap `OutOfMemoryError`. Pemanggil import (`LauncherActivity.java:268`) hanya menangkap IOException/RuntimeException; pemanggil wallpaper aktif (`:277`) hanya menangkap IOException sekitar decode. Catch OOM dalam baris itu hanya meliputi `GlassBackdrop.prepare`, selepas decode selesai. Had fail 16 MiB dan pensampelan 1280 memang mengecilkan risiko, tetapi tidak menjamin peruntukan berjaya apabila memori sudah sempit.

`LauncherActivity.java:197` juga cuba memperuntukkan bitmap kedua selepas peruntukan ikon gagal dengan OOM; jika fallback turut gagal, OOM terlepas. `info.loadIcon` sendiri berada di luar catch OOM dalaman.

Worker menggunakan `ExecutorService.execute`, tanpa pengendali ralat khusus. Pada Android, exception/error yang tidak ditangkap pada thread latar boleh sampai kepada pengendali fatal proses. ThreadPoolExecutor menggantikan thread bukan jaminan aplikasi selamat. **Dakwaan bertentangan pada P9 audit lama tidak tepat.**

Cadangan: jadikan kegagalan decode/peruntukan sebagai hasil gagal yang terkawal, kekalkan wallpaper lama, bersihkan fail sementara, dan elakkan fallback yang memerlukan peruntukan besar selepas OOM. Uji import dan muat semula custom wallpaper di bawah tekanan memori. Belum direproduksi pada head unit.

### A2 — Tinggi: kerja bake kaca yang sudah lapuk terus diproses

`IosStyle.java:41–46` mengubah translationY semasa reveal. `GlassSurface.java:64–95` menggunakan kedudukan skrin dalam kunci bake lalu menghantar kerja baharu apabila koordinat integer berubah. `GlassBake.java:25,45–53` menggunakan satu executor dengan baris gilir tanpa had; tiada pembatalan atau semakan kelapukan sebelum pengiraan. Hasil lapuk hanya ditolak dalam callback selepas kerja siap (`GlassSurface.java:95`).

Ini bukan semestinya satu kerja setiap bingkai: koordinat dibundarkan, dan `pending` menyekat kunci sama. Namun beberapa kedudukan bagi setiap panel, navigasi berulang dan wallpaper baharu tetap menghasilkan kerja berbeza. Setiap tugas menyalin piksel sumber, memperuntukkan output, dan membuat pengiraan per piksel. Tugas juga menahan sumber dan callback permukaan sehingga siap. Cache 6 MiB mengehadkan cache sahaja, bukan keseluruhan baris gilir atau bitmap yang masih dirujuk permukaan.

Kesan dijangka: CPU/GC berterusan selepas peralihan, paparan respons lambat, dan tekanan memori tambahan pada Cortex-A7. Cadangan: gabungkan permintaan kepada geometri terkini dan batalkan/langkau kerja lapuk sebelum bake, termasuk apabila pemilik telah dilepaskan. Kekalkan rupa kaca pada keadaan stabil; jangan jadikan perubahan automatik kepada LOW sebagai satu-satunya pembetulan. Bilangan tugas sebenar pada head unit belum diukur.

### A3 — Tinggi: pengesanan Auto tidak semestinya melindungi unit 1 GB

`GlassPerf.java:32` menggunakan `isLowRamDevice() || maxMemory() < 96 MiB`. Jika heap tepat 96 MiB dan flag OEM low-RAM palsu, kedua-dua syarat palsu. RAM fizikal 1 GB atau RAM bebas 315 MB tidak diperiksa. Unit tersebut boleh bermula dengan efek penuh sehingga polisi masa bingkai merendahkan kualiti.

Polisi bingkai memerlukan 30 callback awal yang dilangkau dan tiga tetingkap 90 bingkai buruk (`GlassPerf.java:15,85–98`). Ia tidak menjamin bantuan segera ketika mula atau penggunaan yang menghasilkan sedikit bingkai.

Ini risiko bersyarat: heap dan flag sebenar kereta belum diketahui. Cadangan: ukur kedua-duanya dahulu; tentukan polisi bajet memori/kerja secara berasingan daripada rupa kaca. LOW manual ialah langkah sementara yang mengubah penampilan, bukan pembetulan neutral visual.

### A4 — Sederhana: navigasi dan carian membina semula banyak view pada UI thread

`LauncherActivity.java:84–115` membina semula stage, header, kandungan, dock dan bahan kaca pada `render()`, termasuk apabila HOME intent diterima (`:61`). `filter()` (`:175–180`) membuang dan mencipta semua tile yang sepadan pada setiap perubahan teks; tiada penggunaan semula view atau debounce. Selesai muat ikon juga boleh memanggil render pada halaman selain Home (`:201`).

Kesan dijangka: stutter ketika Settings/Apps/Home, kos layout dan shader berulang, serta reset halaman ketika discovery siap. Cadangan: kemas kini bahagian yang berubah sahaja, guna semula tile, hadkan refresh kepada halaman yang memerlukan data. Jangan anggap ANR >5 saat sudah dibuktikan; tiada ukuran itu pada kereta.

### A5 — Sederhana: rangkaian JAKIM boleh menahan kerja wallpaper, ikon dan Wi-Fi/BT

Satu `worker` (`LauncherActivity.java:22`) digunakan untuk discovery ikon, import/decode wallpaper, frost, JAKIM, preview Appearance, dan toggle radio (`QuickControls.java:68`). `JakimCalendar.refresh` menetapkan connect/read timeout 8 saat setiap satu (`:56–57`). Kerja yang menunggu executor tidak bermula selagi tugas terdahulu belum selesai.

UI boleh terus dilukis tetapi butang/preview nampak tidak memberi respons. Jangan tafsir dua timeout itu sebagai had keseluruhan tepat 16 saat: read timeout terpakai kepada operasi baca, bukan keseluruhan pemindahan. Cadangan: asingkan I/O rangkaian daripada pemprosesan imej dengan concurrency terhad dan pembatalan tugas lapuk.

### A6 — Sederhana: Drive memuat ikon pakej secara sinkron pada main thread

`DriveBarService.java:86–95` menjalankan query pakej, loadLabel dan loadIcon sehingga 60 app. Ia dipanggil oleh `addBar()` daripada `onStartCommand` dan setiap pembukaan sheet app (`:104–106,206–208`). Ini berbeza daripada launcher utama yang menggunakan worker. Tiada catch per-pakej bagi RuntimeException semasa label/ikon dimuat.

Kesan dijangka: bar/sheet Drive tersangkut ketika storan sibuk dengan Waze/video. Ralat sumber/pakej ketika imbasan juga boleh terlepas pada main thread dan menamatkan proses; kejadian itu belum direproduksi. Cadangan: snapshot senarai di worker, bitmap bersaiz terhad, tangkap kegagalan per-pakej dan kemas kini UI hanya jika service masih aktif.

### A7 — Sederhana: cache kaca tiada respons kepada tekanan memori sistem

`GlassBake.java:19–25` menyimpan cache statik 6 MiB. Tiada `onTrimMemory` atau `onLowMemory` dalam kod aplikasi untuk mengurangkan cache/kerja tertangguh ketika launcher masuk latar. Permukaan aktif boleh terus memegang bitmap walaupun entri diusir daripada LRU. Dengan aplikasi peta/video berjalan bersama, ini menambah tekanan sistem tetapi tidak sendiri membuktikan kebocoran atau OOM.

Cadangan: lepaskan entri cache yang boleh dijana semula mengikut tahap tekanan memori, kurangkan kerja latar dan sahkan laluan kembali ke launcher. Jangan recycle bitmap yang masih dirujuk shader/display list.

## Pembetulan tafsiran audit lama

- P9: OOM worker tidak semestinya gagal senyap; Android boleh menamatkan proses (A1).
- P6: `HomeGrid.track` hanya memperoleh VelocityTracker apabila medan itu null. Ia bukan satu peruntukan native baharu untuk setiap tap seperti didakwa dahulu. Cleanup pada UP/CANCEL/detach masih wajar, tetapi kebocoran tanpa had per tap belum dibuktikan.
- P7: laluan return awal `GlassBake.request` ialah isu defensif, tetapi pemanggil semasa memastikan sumber wujud dan output sekurang-kurangnya 2×2. Jangan jadikannya punca utama tanpa laluan boleh dicapai.
- Total PSS, RAM bebas sistem dan had heap Java ialah metrik berbeza. Tambah anggaran WebView kepada PSS kemudian banding terus dengan heap 96 MiB tidak membuktikan OOM. Renderer WebView juga boleh menjadi proses berasingan.

## Bukti runtime sesi ini

APK app SHA256: `efe416d234d78f0e7f4cad0cf1b16f4d30c17a40e5faefa66bafaf1ec983d099`.

`PerformanceAuditChecks`, `phase=audit`, selesai dengan `stream=PASS: 10 performance protocol checks` dan `INSTRUMENTATION_CODE: -1`. Bukti penuh: `audit-evidence/2026-11-17/performance.txt` dan `performance.json`.

- Protokol menggunakan motion OFF, BALANCED 45%; ia **tidak** menguji banjir bake semasa animasi A2 atau YouTube/Drive sebenar.
- Idle 10 saat: 1 draw callback, 0 layout callback. Ticker idle bukan suspek utama dalam keadaan ini.
- Lima pembukaan Control Center: pembinaan 496–883 ms, mula draw 825–1416 ms **pada TCG sahaja**. Appearance ready ~2.60 saat. Ini ukuran kos senario emulator, bukan ramalan kelajuan kereta.
- PSS 28,918 KiB sebelum protokol, 39,146 KiB selepas, 36,206 KiB selepas permintaan GC. Perubahan singkat ini tidak membuktikan kebocoran jangka panjang.
- Semasa onboarding terdahulu pada kod sama: 44 ujian unit dan 57 assertion SmokeChecks lulus. Ia bukan pengesahan bebas crash pada head unit.

`StabilityChecks` selesai dengan `stream=PASS: 104 wallpaper/appearance stability checks` dan `INSTRUMENTATION_CODE: -1` (bukti: `audit-evidence/2026-11-17/stability.txt`). Lapan kitaran wallpaper/Appearance dan buka/tutup Control Center selesai tanpa crash. Pemerhatian weak reference selepas permintaan GC: wallpaper lama 0/8 dan editor lama 0/8 masih dirujuk. Ini tidak membuktikan bebas kebocoran jangka panjang, tidak meliputi custom wallpaper di bawah tekanan memori, dan tidak menguji video/peta sebenar. Buffer crash emulator yang diperiksa selepas ujian tidak memaparkan rekod crash.

## Keutamaan tindakan

1. Tutup laluan OOM tidak terkawal dan ralat imbasan pakej (A1/A6).
2. Hadkan/cancel bake lapuk dan asingkan rangkaian daripada kerja imej (A2/A5), sambil memeriksa rupa kaca.
3. Kurangkan render penuh dan kos Drive (A4/A6), kemudian tambah trim memori (A7).
4. Ukur flag/heap sebenar sebelum mengubah polisi Auto (A3).

Untuk membezakan Java crash, ANR dan proses dibunuh kerana memori pada kereta, tangkap selepas kejadian: `adb logcat -b crash -d`, logcat sistem sekitar masa kejadian (ActivityManager/lowmemorykiller), `adb shell dumpsys meminfo com.shazwan.idash3`, `adb shell getprop dalvik.vm.heapgrowthlimit`, `adb shell getprop dalvik.vm.heapsize`, dan `adb shell getprop ro.config.low_ram`. Ulang setiap satu daripada tiga senario dengan `gfxinfo ... framestats`. Tiada jaminan crash pengguna sudah dikenal pasti tanpa bukti ini.
