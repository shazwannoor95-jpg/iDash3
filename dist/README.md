# iDash3 — APK ujian (debug QA)

## 0.2.1-rc1 — calon pembetulan crash/lag (branch berasingan)

[Download APK debug](iDash3-0.2.1-rc1-debug.apk?raw=true) · versionCode 2 · SHA256 `7c604b4380bf0bc4d4927fe703608d1282aa64f159b2ecd2ebe708f2f892e42d`.

Pembetulan wallpaper/ikon OOM, worker JAKIM berasingan, dan imbasan/ikon Drive di latar. Liquid Glass tidak diubah. Butiran dan had ujian: [laporan pembetulan](../docs/CRASH-LAG-FIX-0.2.1-rc1.md). Belum disahkan pada head unit fizikal.

Kunci debug binaan ini berbeza daripada QA lama; kemas kini terus mungkin ditolak. Sandarkan tetapan sebelum menggantikan pemasangan. Jangan buang launcher asal kereta.

| Fail | Versi | Pakej | SHA256 |
|---|---|---|---|
| `iDash3-Debloat-1.0.apk` | 1.0 (code 1) | `com.shazwan.idash3.debloat` | `576f237a05793f521d14512220bfebd549c1f656a9f1aa5688a533a9bb9d8fcb` |
| `iDash3-0.2.0-dev-debug-QA.apk` | 0.2.0-dev (code 1) | `com.shazwan.idash3` | `7d0a30d68b1af8e3a8dc9c25386da68703200510d88b81f5e5191b0ffc495b29` |

- Dibina daripada kod terkini `main`: tema ikon gaya iOS, kaca refraksi baked, ikon squircle, font Inter, dan **Drive (split-screen Android)** dengan kad PETA/MEDIA yang muat pada skrin rendah dan bar gaya Android Auto (launcher, pintasan app, bateri + jam; tiada mikrofon). minSdk 26, targetSdk 35, tiada pustaka native (jalan pada ARM32). 1,750,847 bait.
- **Kemas kini 2026-11-10 (ikon):** ikon app kini mengikut material ikon iOS semasa — squircle dengan sudut lebih bulat (eksponen 4.4), latar gradient 4 hentian (terang di atas, gelap di bawah), satu jalur kilauan pepenjuru yang tajam menggantikan lapisan putih penuh yang kabur, tepi dalam atas terang dan tepi dalam bawah gelap, glyph 5% lebih besar, serta jubin dock/Settings/Apps menggunakan material yang sama. Glyph masih dilukis sendiri dalam app (tiada aset Apple).
- **Kemas kini 2026-11-10:** `configChanges` lengkap (tukar saiz fon/resolusi tidak membina semula launcher), perkhidmatan aksesibiliti tidak lagi menerima `typeWindowStateChanged`, dan **Keluar Drive** membatalkan langkah split/bar yang masih tertangguh. Dua perubahan kaca daripada sesi audit (**P1** pengesanan memori, **P2** tunda bake) telah **di-revert**: kaca kembali sepenuhnya seperti sebelum audit, dan mod **Rendah** hanya aktif jika dipilih manual dalam Settings → Mod prestasi. Butang petak pada bar Drive kini **menghentikan** split-screen (satu hala), dan panel **Aplikasi** memuat semula senarai app setiap kali dibuka dengan panel yang bersaiz ikut kandungan.
- **APK debug untuk ujian sahaja, bukan release.** Ditandatangani kunci debug generik (sijil SHA-256 `cd73822cbb13abfb0c0f1bcdf97113b25faf3ed7163b55db3435a20d43c8d60d`). Ia **tidak** mengemas kini iDash atau iDash2 — boleh dipasang bersama.
- APK debug seterusnya mungkin ditandatangani kunci debug lain; jika Android menolak kemas kini ("signatures do not match"), nyahpasang versi lama dahulu (susunan Home/tetapan akan hilang).

Pasang: muat turun fail ini (butang **Download raw** di GitHub), salin ke head unit, buka dengan pengurus fail dan benarkan pemasangan dari sumber tidak dikenali. Atau: `adb install -r iDash3-0.2.0-dev-debug-QA.apk`.

**iDash3 Debloat:** pasang → *Lumpuhkan yang ditanda* → *Optimumkan* → *Tetapan WebView → Chrome* → mulakan semula → *Selesai — buang app ini*. Dengan root: automatik; tanpa root: wizard membuka halaman setiap app (tekan DISABLE). Semua boleh diundur dengan *Pulihkan semua*. App sistem kereta (radio, Bluetooth, kamera undur, CAN, launcher asal) tidak disentuh. App lain yang disyorkan: [`RECOMMENDED-APPS.md`](RECOMMENDED-APPS.md).
