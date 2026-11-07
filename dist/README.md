# iDash3 — APK ujian (debug QA)

| Fail | Versi | Pakej | SHA256 |
|---|---|---|---|
| `iDash3-Debloat-1.0.apk` | 1.0 (code 1) | `com.shazwan.idash3.debloat` | `576f237a05793f521d14512220bfebd549c1f656a9f1aa5688a533a9bb9d8fcb` |
| `iDash3-0.2.0-dev-debug-QA.apk` | 0.2.0-dev (code 1) | `com.shazwan.idash3` | `53600b1ff8f775346163993da05c29142a9a57dcffb21ec965d10c6b6acfb403` |

- Dibina daripada kod terkini cawangan ini (tema ikon gaya iOS untuk app head unit, kaca refraksi baked, ikon squircle, font Inter, panel kanan kosong). minSdk 26, targetSdk 35, tiada pustaka native (jalan pada ARM32).
- **APK debug untuk ujian sahaja, bukan release.** Ditandatangani kunci debug generik (sijil SHA-256 `404cb209…500e`). Ia **tidak** mengemas kini iDash atau iDash2 — boleh dipasang bersama.
- APK debug seterusnya mungkin ditandatangani kunci debug lain; jika Android menolak kemas kini ("signatures do not match"), nyahpasang versi lama dahulu (susunan Home/tetapan akan hilang).

Pasang: muat turun fail ini (butang **Download raw** di GitHub), salin ke head unit, buka dengan pengurus fail dan benarkan pemasangan dari sumber tidak dikenali. Atau: `adb install -r iDash3-0.2.0-dev-debug-QA.apk`.

**iDash3 Debloat:** pasang → *Lumpuhkan yang ditanda* → *Optimumkan* → *Tetapan WebView → Chrome* → mulakan semula → *Selesai — buang app ini*. Dengan root: automatik; tanpa root: wizard membuka halaman setiap app (tekan DISABLE). Semua boleh diundur dengan *Pulihkan semua*. App sistem kereta (radio, Bluetooth, kamera undur, CAN, launcher asal) tidak disentuh. App lain yang disyorkan: [`RECOMMENDED-APPS.md`](RECOMMENDED-APPS.md).
