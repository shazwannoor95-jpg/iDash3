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
