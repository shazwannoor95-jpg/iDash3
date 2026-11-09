# INSTALL-SIGNING-AUDIT — iDash3

Asas: `main` @ `ad16f48`. APK diperiksa: `dist/iDash3-0.2.0-dev-debug-QA.apk`
(`sha256 339355e1d098b30e750eadbec93cea0221741d8612ecdee035eb2df2b98ebd45`, 1,748,672 bait).

Semua nilai di bawah **diukur** dengan `aapt2 dump badging`, `aapt2 dump xmltree`, `apksigner verify --print-certs` dan `unzip` dalam sesi ini, kecuali yang dilabel [ANDAIAN].

---

## 1. Identiti aplikasi

| Item | Nilai | Fail | Penilaian |
|---|---|---|---|
| `applicationId` | **`com.shazwan.idash3`** | `app/build.gradle:6` | PASS — unik, tidak bertembung dengan `com.shazwan.idash` (iDash) atau iDash2 |
| `namespace` | `com.shazwan.idash3` | `app/build.gradle:3` | PASS |
| `versionCode` | **1** | `app/build.gradle:9` | Lihat **S3** |
| `versionName` | **0.2.0-dev** | `app/build.gradle:10` | PASS (label QA) |
| `minSdk` | 26 | `app/build.gradle:7` | PASS — peranti API 27 |
| `targetSdk` | 35 | `app/build.gradle:8` | PASS — pada API 27 `targetSdk` hanya menandakan tingkah laku; tiada kesan runtime |
| `compileSdk` | 35 | `app/build.gradle:4` | PASS |
| Permission tanda tangan | `com.shazwan.idash3.permission.CONTROL`, `protectionLevel="signature"` | `AndroidManifest.xml:14` | PASS — nama berbeza, tiada `INSTALL_FAILED_DUPLICATE_PERMISSION` dengan iDash |
| `minifyEnabled` | false | `app/build.gradle:13` | PASS untuk QA (debug) |

**Pengesahan bahawa iDash/iDash2 tidak akan tersentuh:** tiada satu rujukan `com.shazwan.idash` atau `com.shazwan.idash2` dalam repo (grep kosong). `applicationId` berbeza + sijil berbeza bermakna Android menganggapnya **tiga aplikasi berasingan**. Memasang iDash3 **tidak boleh** memadam, menggantikan atau mengemas kini iDash/iDash2, dan menyahpasang iDash3 tidak menjejaskan mereka.

---

## 2. Isu

| ID | Sev | Isu | Bukti | Punca | Risiko | Cadangan | Pengesahan |
|---|---|---|---|---|---|---|---|
| S1 | **Sederhana** | APK hanya ditandatangani **skim v2**; v1 tiada, v3 tidak | [BUKTI UKUR] `apksigner`: v1=false, v2=true, v3=false, v3.1=false, v4=false | AGP 8.7 menandatangani debug dengan v2 sahaja kerana `minSdk 26` (v1 tidak diperlukan) | **PASS untuk API 27** (v2 disokong sejak Android 7.0). Risk hanya jika installer OEM menggunakan laluan `pm install` lama atau salinan APK yang rosak | Tiada perubahan diperlukan untuk head unit ini. Jangan tambah `v1SigningEnabled` tanpa sebab | `apksigner verify -v` selepas setiap binaan (sekarang PASS) |
| S2 | **Sederhana** | Tiada keystore release; APK QA menggunakan sijil debug generik | [BUKTI UKUR] `Signer #1: C=US, O=Android, CN=Android Debug`; cert SHA-256 `cd73822cbb13abfb0c0f1bcdf97113b25faf3ed7163b55db3435a20d43c8d60d`; `guard.sh:8` menolak `signingConfig` | Belum diputuskan (projek masih fasa QA) | Sesiapa yang mempunyai debug keystore standard boleh menandatangani APK "kemas kini" untuk pakej ini. Untuk pemasangan peribadi pada head unit, risiko rendah; untuk pengedaran, tidak boleh diterima | **Jangan jana atau tukar kunci tanpa kelulusan anda** (peraturan projek 5). Jika mahu, jana satu keystore release peribadi, simpan di luar repo, dan tambah `signingConfig` melalui `gradle.properties` | `apksigner verify --print-certs` pada APK akhir; simpan cap jari dalam dokumen ini |
| S3 | **Sederhana** | `versionCode` kekal **1** untuk setiap binaan QA | `app/build.gradle:9`; [BUKTI UKUR] `aapt2`: `versionCode=1` | Tidak dinaikkan antara lepasan | Dua APK berbeza dengan `versionCode` sama: pemasang sesetengah head unit menolak "App not installed" jika APK lama dan baharu berbeza saiz/sijil dalam cara tertentu, dan Android tidak boleh membezakannya untuk *downgrade*. Pengguna juga tidak boleh mengesahkan versi yang dipasang | Naikkan `versionCode` (2, 3, …) dan `versionName` (0.2.1-dev, …) setiap kali APK QA baharu dimuat naik ke `dist/`. **Perlu kelulusan** kerana ia mengubah `build.gradle` | Semak `aapt2 dump badging` sepadan dengan label dalam `dist/README.md` |
| S4 | Rendah | `.gitignore` menyekat `*.apk`, tetapi APK dalam `dist/` ditambah dengan `git add -f` | `.gitignore:17`; `git ls-files dist` menunjukkan APK dijejak | Peraturan "jangan simpan artifak binari" vs keperluan memuat naik APK | Risiko sebenar: selepas binaan baharu, `git add .` **tidak** akan mengemas kini APK dalam `dist/` → pengguna memuat turun APK lama yang SHA-nya tidak sepadan dengan README | Sentiasa `git add -f dist/*.apk` dan kemas kini `dist/README.md` (SHA + saiz) dalam commit yang sama. Pertimbangkan pengecualian `!dist/*.apk` dalam `.gitignore` | `git show --stat HEAD` mesti menyenaraikan APK; bandingkan `sha256sum` dengan README |
| S5 | Rendah | Saiz APK didominasi satu PNG | [BUKTI UKUR] `saga_angles.png` = **1,159,103 bait** (66.3% daripada 1,748,672); APK 20 entri | Imej atlas 1774×887 disimpan tanpa mampatan dalam APK (PNG sudah termampat) | Bukan isu kestabilan (dimuat dengan `inSampleSize=2` dan had 2 MB), tetapi 1.75 MB untuk satu launcher pada storan 12 GB adalah boros dan memperlahankan muat turun | Jika mahu, tukar kepada WebP kualiti 85 atau kecilkan kepada 1280 lebar (jimat ~700 KB). **Perlu kelulusan** (aset visual) | `unzip -l` + `sha256sum`; sahkan panel "Kereta · Saga" masih jelas |
| S6 | Rendah | Tiada `lib/` — APK neutral ABI | [BUKTI UKUR] 20 entri, tiada `lib/**/*.so`; `guard.sh`: "tiada pustaka native" | Tiada kod native | **PASS** — berjalan pada `armeabi-v7a` tanpa isu ABI, dan juga pada `armeabi` kerana tiada `.so` | Kekalkan; jangan tambah `.so` tanpa keperluan | `aapt2 dump badging | grep native-code` (kosong) |
| S7 | Rendah | "Unknown sources" mesti didayakan pada head unit; app tidak (dan tidak boleh) meminta `INSTALL_PACKAGES` | `AndroidManifest.xml` tiada `INSTALL_PACKAGES` | Dasar Android | Jika tidak didayakan, pemasang memaparkan "Blocked by Play Protect"/"Install unknown apps" | Arahan pemasangan di §4 | `adb install -r` berjaya = PASS |
| S8 | Rendah | APK ujian dan modul debloat ialah aplikasi berasingan | [BUKTI UKUR] `app-debug-androidTest.apk` 124,856 B; `dist/iDash3-Debloat-1.0.apk` 20,098 B (`applicationId com.shazwan.idash3.debloat`) | Pisahan biasa | Memasang APK ujian/debloat menambah dua ikon lain; bukan konflik, tetapi mengelirukan pada head unit | Jangan pasang APK `androidTest` pada head unit pengguna; debloat hanya jika benar-benar digunakan | `adb shell pm list packages | grep idash3` |
| S9 | Informasi | APK tidak membawa metadata Play Store (tiada `BUNDLE-METADATA`), tiada `v4` signature | [BUKTI UKUR] | Debug build | Tiada — pemasangan `adb`/fail biasa tidak memerlukannya | Kekalkan | — |

---

## 3. Ringkasan artifak

| Artifak | Saiz | applicationId | Tandatangan | Guna |
|---|---|---|---|---|
| `dist/iDash3-0.2.0-dev-debug-QA.apk` | 1,748,672 B | `com.shazwan.idash3` | debug, v2 | Pasang pada head unit |
| `app-debug-androidTest.apk` | 124,856 B | `com.shazwan.idash3.test` | debug, v2 | Ujian instrumentation sahaja (jangan pasang pada head unit) |
| `dist/iDash3-Debloat-1.0.apk` | 20,098 B | `com.shazwan.idash3.debloat` | debug, v2 | Alat berasingan (pilihan) |

Had `guard.sh`: APK < 8 MB → **PASS** (1.75 MB).
Tiada `signingConfig` dalam Gradle → **PASS** (`guard.sh:8`), selaras peraturan "jangan tukar signing tanpa kelulusan".

---

## 4. Prosedur pemasangan yang disyorkan (head unit)

**A. Melalui fail (tanpa PC)**

1. Muat turun `iDash3-0.2.0-dev-debug-QA.apk` daripada `main` → `dist/`.
2. Pada head unit: Settings → Security → **Unknown sources / Install unknown apps** → benarkan untuk pengurus fail yang digunakan.
3. Buka APK → Install. Jika "App not installed":
   - nyahpasang versi iDash3 lama dahulu (tetapan iDash3 **tidak** disandarkan kerana `allowBackup=false` — tetapkan semula dijangka), atau
   - semak ruang storan (perlu ~5 MB bebas).
4. Selepas pasang: tekan HOME → pilih **iDash** sebagai launcher (jika dialog muncul).

**B. Melalui ADB (disyorkan untuk QA)**

```bash
adb install -r dist/iDash3-0.2.0-dev-debug-QA.apk
adb shell am start -W -n com.shazwan.idash3/.LauncherActivity
adb shell dumpsys package com.shazwan.idash3 | grep -E "versionCode|versionName|signatures"
```

**C. Sahkan tiada gangguan pada iDash asal**

```bash
adb shell pm list packages | grep -E "com.shazwan.idash"
# dijangka: com.shazwan.idash, com.shazwan.idash3 (dan idash2 jika ada)
adb shell pm path com.shazwan.idash      # masih ada = tidak ditimpa
```

**D. Nyahpasang / kembali**

```bash
adb uninstall com.shazwan.idash3         # hanya iDash3
```

Menyahpasang iDash3 **tidak** memadam iDash/iDash2 dan tidak memadam fail wallpaper pengguna di luar app (fail `wallpaper-image` dalam storan peribadi app akan hilang bersama app — itu dijangka).

---

## 5. Status pengesahan

| Semakan | Status | Bukti |
|---|---|---|
| `applicationId` dalam APK | **PASS** | `aapt2 dump xmltree`: `package="com.shazwan.idash3"` |
| `versionCode`/`versionName` dalam APK | **PASS** | `1` / `"0.2.0-dev"` |
| `minSdk`/`targetSdk` dalam APK | **PASS** | `26` / `35` |
| Skim tandatangan untuk API 27 | **PASS** | v2=true (disokong API 24+) |
| Sijil debug dikenal pasti | **PASS** | `CN=Android Debug`, SHA-256 `cd73822…c8d60d` |
| Tiada konflik permission dengan iDash | **PASS** | permission `…idash3.permission.CONTROL`; tiada rujukan `com.shazwan.idash` |
| Tiada pustaka native / ABI neutral | **PASS** | tiada `lib/` |
| Saiz < 8 MB | **PASS** | 1,748,672 B |
| Pemasangan sebenar pada head unit | **BLOCKED** | Tiada peranti/adb dalam sesi ini |
| Kemas kini atas versi lama (versionCode sama) | **NOT RUN** | Perlu peranti |
