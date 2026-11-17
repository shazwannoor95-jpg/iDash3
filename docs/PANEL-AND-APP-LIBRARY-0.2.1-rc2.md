# 0.2.1-rc2 — App Library dan fullscreen video dalam panel

Branch `fix/maps-panel-app-layout`, berasaskan `a42b470` (rc1). Main tidak dikemas kini.

## App Library

Butang Apps membuka grid empat lajur, dua baris, lapan app setiap halaman. Ikon dibesarkan mengikut ruang tersedia sehingga 120 dp; label berada di bawah. Swipe kiri/kanan, anak panah dan titik halaman digunakan untuk navigasi. Hanya lapan tile halaman semasa dibina. Search dibuka melalui butang Search; Refresh dan long-press untuk pilih favorite kekal tersedia. Urutan app kekal mengikut senarai app dipasang (abjad); nama/ikon Apple atau app yang tidak dipasang tidak dicipta.

Sasaran screenshot ialah App Library sahaja, seperti disahkan pengguna. Grid Home, senarai Drive, wallpaper dan dock tidak direka semula.

## YouTube

Apabila WebView membekalkan custom fullscreen video surface, iDash meletakkannya memenuhi ruang video dalam panel. Pilihan Full screen pada overlay membesarkan permukaan video yang sama ke skrin Activity. Exit fullscreen/Back/pause mengembalikan keadaan dan callback provider; tiada pemain atau WebView kedua dicipta. Ini mengubah hos kandungan video sahaja, bukan glass panel di sekelilingnya.

Tingkah laku provider YouTube sebenar pada WebView head unit masih perlu diuji. Ujian offline menggunakan custom video surface dan menyemak attachment, bounds, expansion, pemulihan window flags, callback tepat sekali, dan cleanup semasa pause. Ia tidak membuktikan streaming/decoder pada unit kereta.

Resolusi panel dalam iDash menentukan susun atur halaman, bukan resolusi stream YouTube. Jika media lain berat pada unit 1 GB, cuba tetapan kualiti video 240p/360p di pemain dan bandingkan tanpa app peta serentak; penambahbaikan kelajuan tidak dijamin oleh perubahan ini.

## Google Maps yang tersekat semasa search

Pengguna mengesahkan Google Maps berjaya dibuka, kemudian menjadi stagnant semasa mencari tempat. Oleh itu urutan launch/split iDash tidak diubah tanpa bukti bahawa itulah puncanya. Tiada akses kepada head unit fizikal atau versi Google Maps dipasang, dan APK Google Maps sendiri tidak diubah.

Langkah praktikal pada kereta:
1. Keluar daripada panel YouTube/Drive dan cuba carian yang sama dalam Google Maps sahaja. Catat versi Maps melalui App info.
2. Jika masih tersekat, bandingkan dengan Waze yang serasi Android 8.1 dan ARM32 daripada sumber dipercayai. Selepas dipasang, pilih Drive → PETA → Waze. Jangan anggap build Android 10+/ARM64 boleh berjalan pada unit ini.
3. Jika Waze juga tersekat, tangkap log crash/ANR dan memori pada unit. Semak sambungan Internet, GPS serta kesihatan komponen Google yang sedia ada sebelum menentukan versi ganti. Versi tertentu belum disahkan pada kereta ini dalam sesi ini.

## Sempadan glass panel

GlassSurface, GlassBake, GlassBackdrop, GlassPerf, GlassAppearance, IosStyle, IconStyler, IosGlyphs, AppearanceEditor, Spring/SpringDriver, Elastic, MorphOverlay dan Squircle dikekalkan byte-for-byte berbanding rc1. Res dan assets tidak diubah. Frame/material/tint/blur/border dan animasi glass panel Home/media tidak direka semula. Pemeriksaan git diff terhadap fail-fail ini mesti kosong.

## Pengesahan

Binaan debug lulus. 47 ujian unit lulus (0 gagal/errors/skips); lint 0 errors / 62 warnings; APK guard dan signature verification lulus. AppLibraryChecks lulus 17 assertion, PanelFullscreenChecks 13, dan SmokeChecks 57. Ketiga-tiga runner menghasilkan stream=PASS dan INSTRUMENTATION_CODE: -1. Grid diukur pada bounds 1024×380 dan 800×260 untuk mewakili ruang selepas chrome aplikasi pada skrin 1024×600 / 800×480; ini bukan ujian head unit fizikal. Tangkapan skrin App Library pada emulator turut diperiksa untuk susunan 4×2. Keputusan runner yang selesai disimpan di `audit-evidence/0.2.1-rc2/`. Emulator API27 x86_64 TCG/SwiftShader tidak mengesahkan prestasi ARM32/Mali-450.

Percubaan awal AppLibraryChecks gagal kerana helper ujian membandingkan contentDescription null; helper diperbetulkan sebelum ujian diulang. SmokeChecks lama kemudian terhenti pada assertion yang menjangka grid kosong mempunyai satu child; grid berhalaman mempunyai viewport dan indikator. Pemeriksaan diganti dengan pengesahan TextView sebenar berteks No apps found dan tiadanya mesej itu selepas carian dikosongkan. Tiada pemeriksaan tingkah laku carian dibuang.

APK debug rc2 menggunakan versionCode 3 dan kunci debug yang sama dengan rc1 dalam persekitaran ini. Kunci ini berbeza daripada QA lama yang asal. Tiada uninstall automatik dilakukan.
