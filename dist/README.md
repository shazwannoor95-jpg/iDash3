# iDash3 — APK ujian (debug QA)

| Fail | Versi | Pakej | SHA256 |
|---|---|---|---|
| `iDash3-Debloat-1.0.apk` | 1.0 (code 1) | `com.shazwan.idash3.debloat` | `576f237a05793f521d14512220bfebd549c1f656a9f1aa5688a533a9bb9d8fcb` |
| `iDash3-0.2.0-dev-debug-QA.apk` | 0.2.0-dev (code 1) | `com.shazwan.idash3` | `0e8a0591ed06e3b9aae6f6d3ad1d7ad0df1516c5c3e7fb8bbeca440dff681e94` |

- Dibina daripada kod terkini `main`: tema ikon gaya iOS, kaca refraksi baked, ikon squircle, font Inter, dan **Drive (split-screen Android)** dengan kad PETA/MEDIA yang muat pada skrin rendah dan bar gaya Android Auto (launcher, pintasan app, bateri + jam; tiada mikrofon). minSdk 26, targetSdk 35, tiada pustaka native (jalan pada ARM32). 1,749,350 bait.
- **Kemas kini 2026-11-10:** `configChanges` lengkap (tukar saiz fon/resolusi tidak membina semula launcher), perkhidmatan aksesibiliti tidak lagi menerima `typeWindowStateChanged`, dan **Keluar Drive** membatalkan langkah split/bar yang masih tertangguh. Dua perubahan kaca daripada sesi audit (**P1** pengesanan memori, **P2** tunda bake) telah **di-revert**: kaca kembali sepenuhnya seperti sebelum audit, dan mod **Rendah** hanya aktif jika dipilih manual dalam Settings → Mod prestasi. Butang petak pada bar Drive kini **menghentikan** split-screen (satu hala), dan panel **Aplikasi** memuat semula senarai app setiap kali dibuka dengan panel yang bersaiz ikut kandungan.
- **APK debug untuk ujian sahaja, bukan release.** Ditandatangani kunci debug generik (sijil SHA-256 `cd73822cbb13abfb0c0f1bcdf97113b25faf3ed7163b55db3435a20d43c8d60d`). Ia **tidak** mengemas kini iDash atau iDash2 — boleh dipasang bersama.
- APK debug seterusnya mungkin ditandatangani kunci debug lain; jika Android menolak kemas kini ("signatures do not match"), nyahpasang versi lama dahulu (susunan Home/tetapan akan hilang).

Pasang: muat turun fail ini (butang **Download raw** di GitHub), salin ke head unit, buka dengan pengurus fail dan benarkan pemasangan dari sumber tidak dikenali. Atau: `adb install -r iDash3-0.2.0-dev-debug-QA.apk`.

**iDash3 Debloat:** pasang → *Lumpuhkan yang ditanda* → *Optimumkan* → *Tetapan WebView → Chrome* → mulakan semula → *Selesai — buang app ini*. Dengan root: automatik; tanpa root: wizard membuka halaman setiap app (tekan DISABLE). Semua boleh diundur dengan *Pulihkan semua*. App sistem kereta (radio, Bluetooth, kamera undur, CAN, launcher asal) tidak disentuh. App lain yang disyorkan: [`RECOMMENDED-APPS.md`](RECOMMENDED-APPS.md).
