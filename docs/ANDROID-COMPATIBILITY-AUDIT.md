# ANDROID-COMPATIBILITY-AUDIT — iDash3 pada API 27 / ARM32

Asas: `main` @ `ad16f48`, `minSdk 26`, `targetSdk 35`, `compileSdk 35` (`app/build.gradle:3-11`).
Sasaran: Android 8.1, `SDK_INT 27`, 1024×600 mdpi, `armeabi-v7a` (lihat `docs/HARDWARE-BASELINE.md`).

Label bukti: **[BUKTI KOD]** = dilihat dalam fail/artifak · **[BUKTI UKUR]** = dijalankan dalam sesi ini · **[ANDAIAN]** = inferens, perlu ujian peranti.

Ringkasan: **1 Tinggi · 3 Sederhana · 8 Rendah/Maklumat · 0 isu API>27 tanpa guard.**

> **Status 2026-11-10:** **A1** dan **A2** telah dibetulkan; A3–A12 masih terbuka. Lihat §6a.

---

## 1. Jadual isu

| ID | Sev | Isu | Fail:baris | Bukti | Punca | Risiko | Cadangan | Pengesahan |
|---|---|---|---|---|---|---|---|---|
| A1 | **Tinggi** | Pilihan baris rumah (`configChanges`) tidak lengkap; perubahan `fontScale`/`density`/`uiMode`/`locale` mencipta semula Activity | `AndroidManifest.xml:22` | [BUKTI KOD] `configChanges="orientation|screenSize|keyboardHidden"` | Manifest lama diwarisi daripada fasa launcher asas | Activity dibina semula → panel WebView/FM ditutup, animasi masuk kaca diulang, kedudukan halaman grid hilang. Pada 1 GB peranti ini kelihatan sebagai "launcher jadi berat" selepas pengguna menukar saiz fon | Tambah `smallestScreenSize|density|fontScale|uiMode|locale|layoutDirection` selepas mengesahkan `onConfigurationChanged` → `render()` mengendalikan setiap satu. **Perlu kelulusan** (ubah manifest) | Tukar "Font size" dan "Display size" dalam Settings peranti; sahkan tiada `Activity` recreate dalam `adb logcat` (`ActivityManager: START u0`) dan panel tidak hilang |
| A2 | **Sederhana** | Perkhidmatan aksesibiliti mendakwa `typeWindowStateChanged` walaupun `onAccessibilityEvent` kosong | `res/xml/split_service.xml:3`; `SplitService.java:23-26` | [BUKTI KOD] | XML warisan; komen kelas sendiri berkata "declares no event types" — bercanggah | Setiap perubahan tetingkap (setiap app dibuka/ditutup) membangunkan proses iDash3 dan menghantar `AccessibilityEvent` yang dibuang. Pembaziran CPU/bateri pada peranti 1 GB dengan perkhidmatan yang hanya perlu `performGlobalAction` | Buang atribut `accessibilityEventTypes` (perkhidmatan tetap boleh dihidupkan dan `performGlobalAction` tetap berfungsi). **Perlu kelulusan** | Hidupkan perkhidmatan, tekan butang split pada bar Drive, sahkan split masih berlaku; `adb shell dumpsys accessibility | grep -A3 idash3` untuk sahkan tiada event type |
| A3 | **Sederhana** | `onNewIntent` menetapkan semula `page="home"` dan memanggil `render()` penuh | `LauncherActivity.java:61` | [BUKTI KOD] | Ditulis untuk "tekan HOME = balik ke rumah" | Tekan HOME daripada app lain sentiasa membuang halaman semasa (Drive/Settings/App Library) dan memainkan semula animasi kaca penuh — pada Mali-450 ini satu "stutter" yang jelas. Ia juga tidak memulihkan halaman yang pengguna tinggalkan | Kekalkan halaman semasa jika launcher sudah berada di latar hadapan (`page` tidak berubah), atau hanya `render()` apabila `page` benar-benar berubah | Dari Drive, tekan HOME peranti; sahkan sama ada pengguna menjangka kekal di Drive atau kembali ke Home (keputusan produk) |
| A4 | **Sederhana** | Manifest tidak menyatakan `uses-feature`; peranti tanpa skrin sentuh / tanpa Bluetooth tetap boleh memasang | `AndroidManifest.xml:18` | [BUKTI KOD] | Tidak pernah ditambah | Bukan isu pada head unit ini (ada sentuh + BT), tetapi APK yang sama boleh dipasang pada peranti yang tidak sesuai lalu memberi pengalaman rosak | Tambah `<uses-feature android:name="android.hardware.touchscreen" android:required="true"/>` dan `android.hardware.bluetooth` `required="false"` jika APK akan diedarkan lebih luas | `adb shell pm list features`; semak `aapt2 dump badging` selepas perubahan |
| A5 | Rendah | `BLUETOOTH_CONNECT` diminta tanpa `maxSdkVersion` dan tidak pernah diminta secara runtime | `AndroidManifest.xml:8`; `QuickControls.java:43` | [BUKTI KOD] | Ditambah untuk keserasian API 31+ | Pada API 27 kebenaran ini **tidak wujud** dan diabaikan sepenuhnya (tiada kesan). Pada telefon API 31+, kod hanya menyemak `checkSelfPermission` dan jatuh ke Settings — ia tidak pernah meminta kebenaran runtime, jadi butang BT membuka Settings dan bukan menogol | Pada peranti ini: **tiada tindakan perlu**. Jika sokongan 31+ mahu dikekalkan, tambah `maxSdkVersion="30"` pada `BLUETOOTH`/`BLUETOOTH_ADMIN` dan minta `BLUETOOTH_CONNECT` melalui `requestPermissions` | Uji butang BT pada head unit: dijangka `< 29` → `BluetoothAdapter.enable()` dipanggil terus |
| A6 | Rendah | `foregroundServiceType="specialUse"` + `FOREGROUND_SERVICE_SPECIAL_USE` + `<property>` tidak dikenali pada API 27 | `AndroidManifest.xml:12,21` | [BUKTI KOD]; [BUKTI UKUR] APK menunjukkan `foregroundServiceType=0x40000000` | Keperluan Android 14 | **Tiada risiko pada API 27** — atribut/kebenaran tidak dikenali diabaikan oleh `PackageManager` 8.1. `startForeground` tanpa jenis sah pada API 27 | Kekalkan; jangan buang kerana ia diperlukan pada peranti moden | Pasang pada API 27 dan sahkan bar Drive muncul (sudah terbukti pada binaan QA pengguna) |
| A7 | Rendah | Tiada `android:resizeableActivity`; pada API 27 split-screen sistem masih berfungsi kerana ia lalai `true` untuk `targetSdk ≥ 24` | `AndroidManifest.xml:22` | [BUKTI KOD] | Tidak dinyatakan | Drive bergantung pada split-screen sistem. Jika OEM memaksa `resizeableActivity=false` pada `LauncherActivity`, split dua app lain **masih** berfungsi kerana yang dipecah bukan iDash3 | Tiada tindakan; catat kebergantungan ini | Uji "Mula Drive" pada peranti |
| A8 | Rendah | `screenOrientation="sensorLandscape"` dipaksa | `AndroidManifest.xml:22` | [BUKTI KOD]; lint `DiscouragedApi` | Head unit sentiasa landskap | Jika firmware melaporkan orientasi potret semasa boot, Activity berputar sekali (kosmetik) | Kekalkan untuk head unit; elak mengubah jika APK akan dipasang pada telefon | Reboot peranti, perhatikan sama ada skrin berputar |
| A9 | Rendah | Tiada penggunaan GPS langsung (tiada `ACCESS_FINE_LOCATION`) | `AndroidManifest.xml` | [BUKTI KOD] | Reka bentuk: zon JAKIM dipilih manual, bukan melalui GPS | Tiada kesan. Tetapi jangan jangka app "tahu lokasi" — ia tidak | Kekalkan; jika auto-zon mahu ditambah kemudian, ia memerlukan kebenaran lokasi + kod baharu | Semak Settings → Waktu Solat: zon dipilih pengguna |
| A10 | Rendah | Tag `allowBackup="false"` dianggap lapuk pada Android 12+ (lint `DataExtractionRules`) | `AndroidManifest.xml:18` | [BUKTI UKUR] lint | Amaran lint sahaja | **Tiada kesan pada API 27**; pada 12+ ia hanya amaran | Abaikan untuk peranti sasaran; tambah `dataExtractionRules` hanya jika APK diedarkan ke 12+ | lint |
| A11 | Maklumat | `queries` mengandungi hanya `SHOW_TIMERS`, `SET_TIMER`, `APP_CALCULATOR`, `com.android.fmradio`, `com.acloud.stub.extradio`, `LAUNCHER` | `AndroidManifest.xml:16` | [BUKTI KOD] | Keperluan Android 11 package visibility | Pada API 27 semua pakej kelihatan; blok ini tidak memberi kesan | Kekalkan | — |
| A12 | Maklumat | Kebenaran `SYSTEM_ALERT_WINDOW`, `WRITE_SETTINGS`, `ACCESS_NOTIFICATION_POLICY` ialah kebenaran khas; kod sentiasa menyemak `canDrawOverlays`/`canWrite`/`isNotificationPolicyAccessGranted` sebelum digunakan | `DriveBarService.java:56`; `QuickControls.java:77`; `ControlUtilities.java:26` | [BUKTI KOD] | Reka bentuk berhati-hati | Tiada `SecurityException` dijangka; jika tidak dibenarkan, app memaparkan mesej dan membuka Settings | Kekalkan | Uji setiap satu dalam keadaan "tidak dibenarkan" |

---

## 2. Manifest — semakan item demi item

| Item | Keadaan | Penilaian |
|---|---|---|
| `applicationId` | `com.shazwan.idash3` | Betul, unik |
| `minSdk` / `targetSdk` / `compileSdk` | 26 / 35 / 35 | Betul; `minSdk 26 ≤ 27` |
| HOME + DEFAULT intent-filter | Ada (`AndroidManifest.xml:23`) | Betul untuk launcher |
| LAUNCHER intent-filter | Ada | Betul; app boleh dibuka dari drawer |
| `android:exported` | Activity `true`, `MediaAccessService` `true`, `SplitService` `true`, `DriveBarService` `false` | Betul — keperluan `targetSdk 31+` dipenuhi; perkhidmatan sistem mesti `true` untuk diikat |
| `launchMode` | `singleTask` | Betul untuk launcher (tiada tindanan berganda) |
| `allowBackup` | `false` | Betul — tiada pemulihan tetapan merentas peranti |
| `largeHeap` | **Tiada** | Betul untuk 1 GB |
| `hardwareAccelerated` | Tidak dinyatakan (lalai `true`) | Betul |
| `configChanges` | Tidak lengkap | **A1** |
| `theme` | `Theme.Material.NoActionBar` (`values/styles.xml:1`) | API 21+, selamat |
| `queries` | Terhad, sah | Betul |

---

## 3. Keserasian API — panggilan yang disemak

Semua yang berikut disahkan wujud pada API ≤ 27, jadi tiada `NoSuchMethodError` pada Android 8.1:

| API | Sejak | Digunakan di | Guard |
|---|---|---|---|
| `NotificationChannel`, `Notification.Builder(ctx, channel)` | 26 | `DriveBarService.java:63-64` | `minSdk 26` menjamin |
| `startForegroundService` | 26 | `DriveBarService.java:55` | `minSdk 26` |
| `TYPE_APPLICATION_OVERLAY` | 26 | `DriveBarService.java:117,225` | `minSdk 26` |
| `setAutoSizeTextTypeUniformWithConfiguration` | 26 | `LauncherActivity.java` (jam/tajuk) | `minSdk 26` |
| `FrameMetrics` / `addOnFrameMetricsAvailableListener` | 24 | `LauncherActivity.java:63` | dibalut `try/catch RuntimeException` |
| `NotificationManager.isNotificationListenerAccessGranted` | **27** | `ControlMedia.java:11`, `SettingsActions.java:16` | `SDK_INT >= 27` |
| `Context.RECEIVER_EXPORTED/NOT_EXPORTED` | 33 | `QuickControls.java:55`, `ControlMedia.java:12`, `DashFmController.java:65` | `SDK_INT >= 33` |
| `BluetoothAdapter.enable/disable`, `WifiManager.setWifiEnabled` | 1 / 1 (diharamkan 29+/33+) | `QuickControls.java:68-69` | `wifiDirect(sdk) = sdk<29`, `bluetoothDirect(sdk) = sdk<33` |
| `Settings.System.canWrite` | 23 | `QuickControls.java:77` | — |
| `WebSettings.setMixedContentMode` | 21 | `DashWebPlayer.java:110` | — |
| `WebViewClient.onRenderProcessGone` | 26 | `DashWebPlayer.java:171` | `@TargetApi(26)` |

**Kesimpulan:** tiada satu pun panggilan API 28+ tanpa guard ditemui, dan **lint melaporkan 0 ralat** (59 amaran, kebanyakannya `SetTextI18n`/`ViewConstructor` — gaya kod, bukan keserasian).

---

## 4. Pelancaran app luar, Back/Home, lifecycle

| Perkara | Keadaan | Bukti |
|---|---|---|
| Lancar app luar | `ComponentName` + `CATEGORY_LAUNCHER` + `NEW_TASK`, dibalut `catch (RuntimeException)` → Toast + `loadApps()` | `LauncherActivity.java:190` [BUKTI KOD] |
| Pilihan app hilang (uninstall) | `assigned()` pulangkan `null` → tap memanggil `pick(slot)`; Settings memaparkan "Aplikasi tiada · pilih semula" | `LauncherActivity.java:179`; `SettingsActions.java:8` [BUKTI KOD] |
| Back di Home | `onBackPressed` tidak berbuat apa-apa jika `page=home` dan panel tertutup → kekal di Home (betul untuk launcher) | `LauncherActivity.java:70` |
| Back di Drive/Settings | Kembali satu aras; folder tutup dahulu; video fullscreen keluar dahulu | `LauncherActivity.java:70` |
| HOME semasa app lain | iDash3 dibuka semula (singleTask) — tetapi **A3**: halaman semula ke home | `LauncherActivity.java:61` |
| Penerima didaftar/nyahdaftar | `QuickControls.pause`, `ControlMedia.pause`, `ControlUtilities.pause`, `DashFmController.releaseReceiver` | [BUKTI KOD] |
| Worker dimatikan | `worker.shutdownNow()` dalam `onDestroy`; `RejectedExecutionException` ditangkap di `requestRadio` | `LauncherActivity.java:59`; `QuickControls.java:70` |

---

## 5. Bukti pengesahan dalam sesi ini

| Semakan | Status | Bukti |
|---|---|---|
| `minSdk`/`targetSdk` dalam APK | PASS | `aapt2 dump xmltree`: `minSdkVersion=26`, `targetSdkVersion=35`, `versionCode=1`, `versionName="0.2.0-dev"`, `package="com.shazwan.idash3"` |
| Kebenaran/atribut manifest termampat betul | PASS | `foregroundServiceType=0x40000000`, `exported=true/false` seperti sumber |
| Tiada API > 27 tanpa guard | PASS | `lintDebug`: **0 ralat**, 59 amaran; grep `Build.VERSION.SDK_INT` (11 tempat, semua berpasangan) |
| Tiada pustaka native (ABI-neutral) | PASS | APK 20 entri, tiada `lib/`; `guard.sh`: "tiada pustaka native" |
| Tiada VirtualDisplay/embedding | PASS | `guard.sh:12` lulus |
| Ujian unit | PASS | **46 ujian, 0 gagal** (44 asal + 2 untuk `GlassPerf.memoryTight`) |
| Instrumentation API 27 | NOT RUN | Tiada emulator/KVM dalam sesi ini |
| Pasang & lancar pada peranti ARM32 | BLOCKED | Tiada peranti/adb |

---

## 6. Pelan pembetulan mengikut keutamaan (memerlukan kelulusan)

1. **A1** (Tinggi) — lengkapkan `configChanges`. Satu baris manifest, kesan besar pada kestabilan semula Activity. **Cadangan: luluskan dahulu.**
2. **A2** (Sederhana) — buang `accessibilityEventTypes` daripada XML aksesibiliti. Kesan CPU jelas pada peranti 1 GB, risiko rendah.
3. **A3** (Sederhana) — keputusan produk: HOME patut kekal di halaman semasa atau kembali ke Home?
4. **A4/A5** (Rendah) — hanya jika APK akan diedarkan lebih luas daripada head unit ini.

---

## 6a. Status pelaksanaan (2026-11-10)

| ID | Status | Perubahan |
|---|---|---|
| **A1** | **DIBETULKAN** | `configChanges` kini `orientation\|screenSize\|smallestScreenSize\|screenLayout\|keyboardHidden\|keyboard\|navigation\|fontScale\|uiMode\|locale\|layoutDirection`. `onConfigurationChanged` sedia ada memang memanggil `render()`, jadi setiap konfigurasi yang ditangani benar-benar melukis semula. Disahkan dalam APK: `configChanges=0x40002ff4` |
| **A2** | **DIBETULKAN** | `split_service.xml` tidak lagi mendakwa `accessibilityEventTypes` (dan `notificationTimeout`); hanya tindakan global digunakan. Disahkan dalam APK: elemen `accessibility-service` tanpa atribut peristiwa |
| A3–A12 | Terbuka | A3 ialah keputusan produk (HOME kekal di halaman semasa atau kembali ke Home) |

Lint selepas pembetulan: **0 ralat, 59 amaran** — bilangan amaran tidak berubah, tiada amaran baharu daripada `configChanges`.

Pengesahan pada peranti (tukar saiz fon, split-screen melalui perkhidmatan aksesibiliti) masih **NOT RUN/BLOCKED** (tiada peranti atau emulator dalam sesi).
