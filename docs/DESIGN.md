# DESIGN — iDash3 Liquid Glass v2 & Home

Sasaran: API 27, ARM32, ±1 GB RAM, 1024×600 / 800×480. Semua efek ialah teknik Android Views asli (Canvas, Shader cache). **Tiada blur langsung per bingkai, tiada RenderScript, tiada library.** Ini tiruan visual Liquid Glass (WWDC25 #219/#361), bukan kod atau aset Apple.

## Saluran kaca (setiap panel = satu `GlassSurface`)
| Lapisan | Teknik | Kos per bingkai | Dalam mod rendah |
|---|---|---|---|
| Frost (fake backdrop blur) | Wallpaper di-downsample (128/224/320 px) dan di-box-blur **sekali** di worker thread (`GlassBackdrop.prepare`); setiap panel melukis kepingan bitmap yang sama mengikut kedudukan skrinnya (`BitmapShader` + matriks) | 1 draw bitmap | Kekal (kualiti LOW = 128 px) |
| Tint adaptif per panel (3B #3) | Purata 5×5 sampel wallpaper **di belakang panel itu**; dikira semula hanya bila kedudukan/saiz/backdrop berubah (`adaptRegion`, kunci hash). Menentukan warna badan dan dakwat terang/gelap; scrim setempat sedia ada (6C) menjamin kontras ≥4.9:1 bagi teks | 0 (cache) | Kekal |
| Lensing tepi (3B #1) | Cincin tepi (Path EVEN_ODD) melukis frost yang **diskala 1.8–6.8%** dari pusat panel → sampel di tepi "terbengkok"; digabung bevel gelap/cerah sedia ada | +1 draw bitmap dalam klip cincin | **Mati** |
| Specular ikut sentuhan (3B #2) | `RadialGradient` di-cache; hanya matriks diterjemah ke titik sentuhan (disuap dari `dispatchTouchEvent`, tidak menelan acara); pudar 260 ms selepas lepas | Hanya ketika disentuh | **Mati** |
| Kedalaman ikut saiz (3B #4) | `depth` 0..1 dari sisi terpendek (56→240 dp): bayang lebih dalam, cincin lens lebih lebar dan kuat bagi panel besar; kawalan kecil kekal halus | 0 | Lensing mati; bayang kekal |
| Peralihan (3B #5) | `reveal` 0→1 pada spring: lensing ×3.5 → normal, frost kabur (bitmap ¼ saiz, sedia di-cache) silang ke frost biasa, badan menebal. Bukan fade biasa | Hanya semasa peralihan | Fallback fade/slide 180 ms |
| Rim, sheen, bevel, reflection | Gradient di-cache (warisan 6A–6C) | — | Dikurangkan (kualiti LOW) |

## Gerakan
- `Spring` (osilator teredam, sub-langkah tetap 1/240 s, stabil untuk sebarang dt; diuji unit) + `SpringDriver` (Choreographer; **tiada kerja bila rehat**).
- Morph cecair (3B #6) `MorphOverlay`: setiap tepi segi empat ada spring sendiri (tepi depan lebih kaku → regangan seperti cecair), jejari dan `reveal` ikut progres, dan **leher metaball ringan** (`LiquidShape.bridge/merge`, diuji unit) menyambung bentuk kepada sumber sehingga ia tertarik jauh. Digunakan untuk buka/tutup folder. Overlay membuang dirinya bila spring rehat.
- Tekan anjal: `Elastic` (skala 0.93 pada spring kurang teredam) pada ikon Home, folder, dock.
- Semua gerakan menghormati suis "Smooth transitions" dan `ANIMATOR_DURATION_SCALE`; mod rendah melangkau morph dan anjal.

## Ikon berlapis (3B #7) — `IconStyler`
Dirender **sekali** per app di worker thread ke bitmap 56 dp: (1) latar (lapisan latar `AdaptiveIconDrawable` jika ada, jika tidak gradient dari warna dominan), (2) glyph (lapisan depan/ikon asal) dengan bayang sendiri, (3) highlight (gradient atas, kilauan radial, rim gradient). Varian: Default, Dark, Clear, Tinted terang, Tinted gelap. Artwork = ikon app itu sendiri; tiada ikon Apple/MRiOS.

## Mod prestasi rendah — `GlassPerf`
- Pengguna: Settings → Home & Dock → **Mod prestasi**: Auto / Rendah / Penuh.
- Auto rendah jika `ActivityManager.isLowRamDevice()` atau heap maks < 96 MB, **atau** p90 masa bingkai sebenar (`Window.OnFrameMetricsAvailableListener`, tetingkap 90 bingkai) > 24 ms selama 3 tetingkap berturut-turut; pulih selepas 6 tetingkap < 14 ms. Hanya bingkai yang memang dilukis dikira — tiada polling.
- Kesan: kualiti kaca dihadkan ke LOW; lensing, specular sentuhan, morph, anjal dan bayang/kilauan ikon dimatikan.

## Home (Fasa 4)
- Kiri (37%): jam/tarikh, Waktu Solat JAKIM, kalendar (warisan 6C, `HomeWidgets` auto-muat).
- Kanan (63%): `HomeGrid` — semua app terpasang dalam grid berhalaman; sel 76×88 dp (sasaran sentuh ≥ 48 dp, diuji); lajur/baris dikira dari ruang sebenar, jadi 1024×600 dan 800×480 kedua-duanya muat. Swipe mendatar dengan spring snap + rubber band; titik halaman boleh ditekan (kawasan 40×28 dp).
- Panel media pilihan (YouTube web / FM / Kereta) menggantikan grid bila dibuka.
- Dock terapung (3 kegemaran + Settings + Apps) kekal.
- **Keselamatan kereta:** tiada seret-dan-lepas. Long-press membuka senarai tindakan besar: Alih ke kiri/kanan/permulaan, Masukkan ke folder…, Tetapkan ke dock…, Namakan semula folder, Keluarkan dari folder. Folder satu tahap sahaja.
- Susunan disimpan sebagai teks (`home_layout`), disegerakkan dengan app terpasang (`HomeLayout`, diuji unit).

## Kekangan / keputusan yang direkodkan
- Kegemaran dock kekal 3 slot (Bluetooth, Files, YouTube) seperti 6C.
- 800×480 pada density 160: grid menjadi ±6×3 sel; widget kiri menggunakan profil padat sedia ada. Jika head unit menggunakan density lebih tinggi, bilangan sel berkurang tetapi sasaran sentuh tetap ≥48 dp.
- Tilt (sensor) untuk specular **tidak** digunakan: sensor gerakan memakan kuasa dan banyak head unit tiada; sentuhan sahaja.
