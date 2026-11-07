# iDash3 — APK ujian (debug QA)

| Fail | Versi | Pakej | SHA256 |
|---|---|---|---|
| `iDash3-0.2.0-dev-debug-QA.apk` | 0.2.0-dev (code 1) | `com.shazwan.idash3` | `14aa6d17b21f6389575ff5f79d484c4950a46945bd487840fc0166bc29ad87ff` |

- Dibina daripada kod terkini cawangan ini (tema ikon gaya iOS untuk app head unit, kaca refraksi baked, ikon squircle, font Inter, panel kanan kosong). minSdk 26, targetSdk 35, tiada pustaka native (jalan pada ARM32).
- **APK debug untuk ujian sahaja, bukan release.** Ditandatangani kunci debug generik (sijil SHA-256 `404cb209…500e`). Ia **tidak** mengemas kini iDash atau iDash2 — boleh dipasang bersama.
- APK debug seterusnya mungkin ditandatangani kunci debug lain; jika Android menolak kemas kini ("signatures do not match"), nyahpasang versi lama dahulu (susunan Home/tetapan akan hilang).

Pasang: muat turun fail ini (butang **Download raw** di GitHub), salin ke head unit, buka dengan pengurus fail dan benarkan pemasangan dari sumber tidak dikenali. Atau: `adb install -r iDash3-0.2.0-dev-debug-QA.apk`.
