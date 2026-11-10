# Drive — skrin gaya Android Auto (split-screen)

Peta di satu sisi, media (SmartTube/NewPipe/YouTube/muzik/radio) di sisi lain, dengan bar kawalan terapung berjam di bawah.
Ia menggunakan **split-screen Android sendiri**: tiada app yang di-embed, jadi setiap app berjalan seperti biasa (Waze, SmartTube, FM, dll.).

## Cara guna
1. Buka iDash3 → ikon hijau **Drive** pada dock.
2. Pilih app **PETA** dan app **MEDIA** (★ = cadangan; lalai: Waze › Google Maps › Net Navi, dan SmartTube › NewPipe › YouTube › Spotify › muzik/radio OEM). Butang ⇄ menukar sisi kiri/kanan.
3. Hidupkan dua kebenaran (sekali sahaja; baris merah pada skrin Drive boleh ditekan):
   - **Aksesibiliti → iDash3 Drive** (untuk menekan butang split-screen, Back, Home dan Recents). Tidak membaca kandungan skrin.
   - **Paparan di atas app lain** (untuk bar terapung). Pilihan: tanpanya Drive tetap berjalan tanpa bar.
4. Tekan **Mula Drive**. Urutan: app pertama dibuka → didok ke kiri → app kedua dibuka di sebelah kanan → bar terapung muncul.
5. **Keluar Drive** (atau ✕ pada bar) keluar dari split-screen dan kembali ke iDash3.

Hidupkan aksesibiliti melalui ADB (jika Settings sukar dicapai):
```bash
adb shell settings put secure enabled_accessibility_services com.shazwan.idash3/com.shazwan.idash3.SplitService
adb shell settings put secure accessibility_enabled 1
```

## Bar terapung (gaya Android Auto)
Bar bawah: **App launcher** (grid) · pintasan app **peta**, **media**, **telefon** dan **Settings** · **status** (bateri + jam).

- **App launcher:** grid **4–6 lajur tetap** (bilangan lajur ikut lebar skrin: 1024×600 = 5 lajur, 800×480 = 4) dengan tile **Exit** dahulu, app peta/media yang dipilih, kemudian semua app yang dipasang. Baris terakhir yang tidak penuh diisi dengan ruang kosong supaya ikon tidak meregang dan setiap lajur lurus. Butang **dashboard** (dua pane) menutup grid dan kembali ke split-screen.
- **Bar hilang bila Drive tamat:** sebaik launcher iDash3 muncul semula di hadapan (tekan Home, atau app split ditutup oleh sistem), bar terapung ditutup dan sesi Drive dianggap tamat — ia tidak lagi kelihatan terapung di atas skrin Home.
- **Tiada mikrofon dan tiada pembantu suara** — iDash3 tidak membuka mikrofon atau Assistant.
- Back / Home / Recents / volume dan ✕ keluar tidak lagi ada pada bar: gunakan kawalan pada peranti head unit, atau butang Home untuk kembali ke iDash3.

## Susun atur & skrin penuh (gaya Android Auto)
- **Change Layout:** butang **⇄** menukar antara **navigasi dekat pemandu** dan **media dekat pemandu**, sama seperti *Android Auto → Settings → Display → Change Layout*. Lalai: navigasi dekat pemandu; pemandu di sebelah **kanan** (RHD), jadi peta mula di kanan. Pilihan disimpan.
- **Hentikan split-screen (butang segi empat):** butang kotak di sebelah launcher pada bar **menghentikan** pasangan split-screen dan membiarkan app yang fokus mengambil seluruh skrin. Ia satu hala — tekan lagi tidak mendok semula pasangan itu — dan ia **tidak** menyimpan flag "sudah skrin penuh": setiap tekan bertanya kepada sistem sekali lagi, kerana ROM boleh menutup atau membina semula pasangan split di belakang tabir. Untuk kembali ke split, tekan pintasan peta/media (app itu terbuka semula dalam pane) atau buka halaman Drive dan tekan **Mula Drive**. Jika ROM langsung tidak menutup pasangan itu, **tekan lama** butang petak untuk keluar Drive sepenuhnya (ke launcher iDash3) — tindakan itu tidak memerlukan perkhidmatan aksesibiliti.
- **Video penuh skrin (YouTube dan lain-lain):** Android **tidak membenarkan** mana-mana app mengambil seluruh skrin semasa ia berada dalam split-screen — itu peraturan multi-window sistem, bukan had YouTube, jadi menukar app (SmartTube, VLC, MX Player) juga tidak akan menjadikannya penuh skrin **di dalam** split. Untuk menonton penuh skrin: tekan butang segi empat (split tamat, app fokus jadi skrin penuh), kemudian tekan pintasan media pada bar yang masih ada. App media terbuka penuh 1024×600 dan bar kekal di bawah untuk kembali ke peta.
- **Bar & launcher:** bar ialah pil kompak di tengah bawah (bukan jalur hitam selebar skrin, dan tiada `FLAG_LAYOUT_NO_LIMITS`). Butang launcher membuka panel 66% tinggi dengan senarai app, tile Exit dan butang ✕; tekan kawasan gelap di luar panel untuk tutup, jadi tiada lagi tetingkap hitam penuh skrin tanpa isi.
- **Automatik:** urutan split (app pertama → dok → app kedua) berjalan sendiri apabila **Mula Drive** ditekan, tanpa perlu drag atau susun app secara manual.

## Had dan nota
- Bergantung pada **split-screen ROM** head unit. Pemilik melaporkan split-screen berfungsi pada TEST9278B; urutan Drive **belum diuji pada head unit** itu.
- App yang tidak menyokong tetingkap saiz-boleh-ubah (`resizeableActivity=false`) mungkin dipaksa skrin penuh atau menolak split.
- Pemasaan urutan (2.3 s sebelum dok, 1.7 s sebelum app kedua) tetap; head unit perlahan mungkin perlu **Mulakan semula Drive**.
- Pembahagi dan saiz kedua-dua sisi dikawal oleh sistem Android, bukan iDash3.
- **Tidak ditiru daripada Android Auto:** mikrofon/Assistant (sengaja tiada), "third split" untuk mesej/panggilan masuk (perlukan akses kandungan notifikasi), dan tiling dinamik Coolwalk (ROM menentukan saiz panel). Kad "Home/Work 18 min" juga perlukan data navigasi AA.
- **Tukar app media:** halaman **Drive → kad MEDIA → tekan untuk tukar**. Senarai menyusun app video dahulu (★ YouTube, SmartTube, VLC dan lain-lain yang dipasang), jadi SmartTube boleh dipilih jika ia dipasang tanpa mengubah apa-apa pada YouTube.
- Hentikan split bergantung pada tindak balas ROM terhadap `GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN`; kebanyakan ROM menutup dok dan membiarkan app fokus skrin penuh. Jika aksesibiliti iDash3 mati, butang itu memaparkan toast dan tidak melakukan apa-apa.
- Panel **Aplikasi** memuat semula senarai app setiap kali dibuka dan kini bersaiz mengikut kandungan (grid menatal hanya selepas separuh skrin), jadi ia tidak boleh muncul sebagai panel kosong.
- Ujian: `DriveChecks` (emulator API 27) menghidupkan perkhidmatan, menjalankan urutan dan **merekod stack aktiviti** yang dilaporkan Android; hasilnya dalam log CI. Ia bukan bukti untuk ROM head unit.
