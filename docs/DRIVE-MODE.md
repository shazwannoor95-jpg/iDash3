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

- **App launcher:** grid 4 lajur dengan tile **Exit** dahulu, app peta/media yang dipilih, kemudian semua app yang dipasang. Butang **dashboard** (dua pane) menutup grid dan kembali ke split-screen.
- **Tiada mikrofon dan tiada pembantu suara** — iDash3 tidak membuka mikrofon atau Assistant.
- Back / Home / Recents / volume dan ✕ keluar tidak lagi ada pada bar: gunakan kawalan pada peranti head unit, atau butang Home untuk kembali ke iDash3.

## Had dan nota
- Bergantung pada **split-screen ROM** head unit. Pemilik melaporkan split-screen berfungsi pada TEST9278B; urutan Drive **belum diuji pada head unit** itu.
- App yang tidak menyokong tetingkap saiz-boleh-ubah (`resizeableActivity=false`) mungkin dipaksa skrin penuh atau menolak split.
- Pemasaan urutan (2.3 s sebelum dok, 1.7 s sebelum app kedua) tetap; head unit perlahan mungkin perlu **Mulakan semula Drive**.
- Pembahagi dan saiz kedua-dua sisi dikawal oleh sistem Android, bukan iDash3.
- Ujian: `DriveChecks` (emulator API 27) menghidupkan perkhidmatan, menjalankan urutan dan **merekod stack aktiviti** yang dilaporkan Android; hasilnya dalam log CI. Ia bukan bukti untuk ROM head unit.
