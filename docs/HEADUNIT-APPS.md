# Head unit — app yang dipasang (daripada APK dump pemilik)

Sumber: 7 set RAR yang dimuat naik oleh pemilik (p1–p5, ExtractedApks), **141 fail APK, 117 pakej unik**, diekstrak dan dibaca dengan `aapt2` sahaja (tiada kod dijalankan). APK pihak ketiga ini **tidak** dimasukkan ke dalam repo.

## App berikon pelancar dan ikon gaya iOS iDash3

| Pakej | Label | Ikon iDash3 |
|---|---|---|
| `com.acloud.stub.localmusic` | localmusic | Music |
| `com.acloud.stub.localradio` | Radio | Radio |
| `com.acloud.stub.logoselector` | LogoSelector | Photos |
| `com.acloud.stub.video` | video | Video |
| `com.android.calendar` | Calendar | Calendar |
| `com.android.gallery3d` | Gallery | Photos |
| `com.android.soundrecorder` | Sound Recorder | Mic |
| `com.autochips.android.backcar` | Backcar | Reverse |
| `com.autochips.bluetooth` | Bluetooth | Bluetooth |
| `com.ext.ui` | Apk Extractor | Tools |
| `com.google.android.apps.maps` | Maps | Maps |
| `com.google.android.gms` | Google Play services | Settings |
| `com.google.android.inputmethod.latin` | Gboard | Keyboard |
| `com.kyhero.car.myhost` | CarSet | Canbus |
| `com.kyhero.car.myhost2` | CanBus2 | Canbus |
| `com.mediatek.filemanager` | File Manager | Files |
| `com.pve.download` | Downloads | Download |
| `com.pve.naviguide` | Net Navi | Maps |
| `com.pve.theme` | Theme | Photos |
| `com.shazwan.nexadash.lab` | NexaDash Native Lab | (ikon asal) |
| `com.shazwan.nexadash.tizentube.lab` | TizenTube Native Lab | Youtube |
| `com.termux` | Termux | Tools |
| `com.xy.tda7729` | Sound Effects | Sound |
| `com.xy.zlinkactivationcode` | ZlinkActivationCode | Link |
| `com.xyauto.Settings` | Car Settings | Car |
| `org.videolan.vlc` | VLC | Video |

Chrome (`com.android.chrome`) dipasang sebagai split APK; ia dipadankan dengan ikon Browser.

## Penemuan yang mempengaruhi iDash3
- **Radio FM:** `com.android.fmradio` dan `com.acloud.stub.extradio` wujud dan mengandungi `com.android.xygala.ACTION_CONTROL_RADIO` / `FmtestService` / `RadioService` yang digunakan oleh panel FM iDash (`DashFmController`). App radio pengguna ialah `com.acloud.stub.localradio` (Qt, aksi `xy.android.start.fm`). Integrasi sedia ada sepadan; belum diuji secara fizikal.
- **Launcher asal:** `com.xygala.launcherall` — kekalkan sebagai sandaran.
- **Pengurus fail:** `com.mediatek.filemanager` — kini petunjuk pertama untuk kegemaran dock "Files".
- **Bluetooth:** `com.autochips.bluetooth` — sepadan dengan petunjuk kegemaran dock sedia ada.
- **WebView:** `com.google.android.webview` dan `com.mediatek.webview` wujud — panel YouTube web bergantung pada versi WebView ini (belum diuji).
- **Google Play services** wujud tetapi iDash3 tidak bergantung padanya.

## Audit keserasian (2026-11-07)

**Platform sebenar:** `framework-res` = **API 27 (Android 8.1)**, model TEST9278B, kernel 4.14. Paparan "Android 12.0" dalam Settings ialah label OEM sahaja. Play Store memasang split `armeabi_v7a` → ruang pengguna **32-bit**; app 64-bit sahaja tidak akan berjalan.

**Keputusan:** semua 117 pakej mempunyai `minSdk ≤ 27` dan pustaka native (jika ada) dalam `armeabi-v7a` → semuanya *boleh dipasang dan dijalankan*. Masalah sebenar ialah **beban**, bukan keserasian:

| Komponen | Versi pada head unit | Penilaian |
|---|---|---|
| Google Play services | 26.37 (2026) | Sangat berat untuk ±1 GB RAM; diperlukan oleh Maps/Play Store |
| Google Maps | 25.32 (2025) | Berat (enjin 3D + Play services). Lambat dimuat, peta kosong dan carian gagal sepadan dengan tekanan memori/GPU — [ANDAIAN berasaskan versi & RAM; tiada logcat] |
| Android System WebView | Google 63 (2017) + MediaTek 58 | **Sangat lama**. Menjejaskan app yang guna WebView (termasuk panel YouTube web iDash3) |
| Chrome | 138 | Versi **terakhir** untuk Android 8/9; boleh dijadikan penyedia WebView |
| YouTube | 20.26 | Berfungsi tetapi berat dan beriklan |

### Cadangan
1. **WebView → Chrome:** Developer options → *WebView implementation* → **Chrome**. Android 7–9 membenarkan Chrome sebagai penyedia WebView; ini menggantikan WebView 63/58 yang lama.
2. **Navigasi:** Waze terkini memerlukan Android 10; binaan terakhir yang dilaporkan untuk Android 8.0+ ialah **5.9.5.4** (arm-v7a + arm64). Pasang daripada sumber yang mengesahkan tandatangan Waze asal dan jangan kemas kini automatik. Alternatif ringan: Google Maps Go (berasaskan Chrome) — perlu diuji kerana keperluan versinya tidak jelas.
3. **YouTube tanpa iklan:**
   - **NewPipe** (F-Droid, sudah ada F-Droid di head unit): antara muka sentuh, Android 6+, ada `armeabi-v7a`, tiada Play services → paling sesuai untuk skrin sentuh.
   - **SmartTube**: antara muka TV (D-pad). Binaan rasminya dijangkiti malware pada akhir 2025; hanya guna binaan GitHub rasmi **30.56 atau lebih baharu** (tandatangan baharu) dan buang binaan lama.
   - TizenTube ialah untuk TV (Tizen/Android TV), bukan antara muka sentuh.
4. **Debloat (lumpuhkan sahaja, boleh dipulihkan):** lihat jadual di bawah. Jangan sentuh app kereta (radio, Bluetooth, Backcar/kamera undur, brakecheck, CAN, Car Settings, quickboot, launcher asal) atau plugin `op01` (Settings/SystemUI boleh crash).

| Selamat dilumpuhkan | Sebab |
|---|---|
| `com.mediatek.mtklogger`, `com.mediatek.mtklogger.proxy` | Logger diagnostik MediaTek — boleh berjalan di latar dan menulis log ke storan |
| `com.mediatek.ygps`, `com.mediatek.bluetooth.dtt`, `com.xyauto.android.autotest` | Alat ujian kilang (GPS, BT, AutoTest) |
| `com.bumblebee.remindeasy` (EasyRemind 0.1) | App peringatan tidak dikenali, targetSdk 15 |
| `com.android.exchange`, `com.mediatek.calendarimporter` | Emel Exchange / import vCalendar — tidak digunakan dalam kereta |
| `com.google.android.partnersetup`, `com.google.android.apps.work.oobconfig`, `com.google.android.feedback` | Persediaan kali pertama / maklum balas Play |
| Gboard **atau** AOSP Keyboard (pilih satu) | Dua papan kekunci; AOSP lebih ringan |
| `com.google.android.youtube` (jika guna NewPipe) | Jimat RAM |

Arahan (ADB, boleh diundur dengan `pm enable <pakej>`):
```
adb shell pm disable-user --user 0 com.mediatek.mtklogger
```
