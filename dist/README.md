# iDash3 — APK ujian (debug QA)

| Fail | Versi | Pakej | SHA256 |
|---|---|---|---|
| `iDash3-0.2.0-dev-debug-QA.apk` | 0.2.0-dev (code 1) | `com.shazwan.idash3` | `432a39046ac79842420cb5db970cf7d97c5ebf400cc76c182a5f011f726c1b6b` |

- Dibina daripada commit `f2e97d0` (kod sama yang lulus CI run 42320648951). minSdk 26, targetSdk 35, tiada pustaka native (jalan pada ARM32).
- **APK debug untuk ujian sahaja, bukan release.** Ditandatangani kunci debug generik (sijil SHA-256 `404cb209…500e`). Ia **tidak** mengemas kini iDash atau iDash2 — boleh dipasang bersama.
- APK debug seterusnya mungkin ditandatangani kunci debug lain; jika Android menolak kemas kini ("signatures do not match"), nyahpasang versi lama dahulu (susunan Home/tetapan akan hilang).

Pasang: muat turun fail ini (butang **Download raw** di GitHub), salin ke head unit, buka dengan pengurus fail dan benarkan pemasangan dari sumber tidak dikenali. Atau: `adb install -r iDash3-0.2.0-dev-debug-QA.apk`.
