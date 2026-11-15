# AUDIT — Fasa 1B (pemasangan & kesesuaian head unit)

Skop: `iDash2@ci/fasa-8a-github-actions` (fa58865) dibandingkan dengan iDash `recovery/fasa-6d-a1-20261103`. Ini audit **source dan artifak**; tiada apa-apa diuji pada head unit atau ARM32. Setiap isu dilabel: **[BUKTI]** (dilihat dalam fail/log) atau **[ANDAIAN]** (inferens, perlu ujian).

Ringkasan severity: 1 Tinggi, 5 Sederhana, 8 Rendah/Maklumat.

## A. Manifest & konfigurasi
| ID | Sev | Isu | Lokasi | Bukti | Cadangan | Ujian |
|---|---|---|---|---|---|---|
| A1 | Rendah | `minSdk 26`, `targetSdk/compileSdk 35`. Selari dengan arahan Fasa 2 (minSdk 26). targetSdk 35 pada peranti 8.1 tidak mencetuskan tingkah laku baharu; ia menambah keperluan Play-policy sahaja. Jangan ubah untuk menyembunyikan isu | `app/build.gradle:4-8` | [BUKTI] | Kekal; catat justifikasi dalam dokumen | Lint |
| A2 | Rendah | Intent-filter HOME+DEFAULT dan LAUNCHER wujud; `exported=true`, `launchMode=singleTask`, `sensorLandscape` betul untuk launcher | `AndroidManifest.xml` | [BUKTI] | Kekal | Emulator HOME/Back |
| A3 | Sederhana | `configChanges` hanya `orientation|screenSize|keyboardHidden`. Perubahan `density`, `fontScale`, `uiMode`, `locale`, `smallestScreenSize` mencipta semula Activity (cache kaca/kawalan dibina semula; WebView hilang) | manifest | [BUKTI] kod; kesan [ANDAIAN] | Tambah `smallestScreenSize|density|fontScale|uiMode|locale|layoutDirection` selepas menguji `onConfigurationChanged` | Ubah density/font scale pada emulator |
| A4 | Rendah | `hardwareAccelerated` tidak dinyatakan (lalai true), tiada `largeHeap` (baik pada 1 GB). Tiada `uses-feature`; tiada penapisan peranti tanpa sentuhan/bluetooth | manifest | [BUKTI] | Kekal; elak `largeHeap`; tambah `uses-feature touchscreen required=false` jika perlu | Pasang pada profil emulator |
| A5 | Sederhana | Kebenaran luas: `WRITE_SETTINGS` (khas), `ACCESS_NOTIFICATION_POLICY`, `SET_ALARM`, `BLUETOOTH_CONNECT` (API 31+, tak relevan pada 8.1), `CHANGE_WIFI_STATE` (maxSdk 28). Setiap satu perlu dibuktikan digunakan | manifest | [BUKTI] kewujudan | Audit penggunaan; buang yang tidak perlu dalam iDash3 | grep penggunaan |
| A6 | Rendah | `MediaAccessService` `exported=true` dilindungi `BIND_NOTIFICATION_LISTENER_SERVICE` (corak piawai). Kebenaran signature `…permission.CONTROL` digunakan bagi siaran FM | manifest; `DashFmController` | [BUKTI] | Kekal | — |
| A7 | Sederhana | Penerima FM didaftar `RECEIVER_EXPORTED` hanya pada API ≥33; di bawah itu penerima tanpa perlindungan. Siaran bergantung firmware OEM | `DashFmController.java:65` | [BUKTI] | Hadkan dengan permission `CONTROL` jika boleh; semak pengendali tidak dipercayai | Ujian siaran palsu |
| A8 | Rendah | `allowBackup=false` baik. `queries` mengandungi pakej `org.smarttube.stable`, `com.shazwan.idash.smarttube` — sisa SmartTube | manifest | [BUKTI] | Buang dalam iDash3 | — |

## B. Signing & pemasangan
| ID | Sev | Isu | Bukti | Cadangan |
|---|---|---|---|---|
| B1 | **Tinggi** | Keystore iDash asal belum dipulihkan. Hanya sijil debug generik digunakan oleh iDash2. APK iDash2 tidak boleh mengemas kini iDash asal (signer berbeza) | `MIGRASI-IDASH2-MS.md`; tiada keystore dalam git (disemak `git ls-files`) | Pasang selari dengan `applicationId` berbeza (cadangan `com.shazwan.idash3`). **Perlukan persetujuan anda** |
| B2 | Sederhana | Percanggahan rekod signer: fingerprint dirujuk `8D52F749…D2B3` ("signing stabil" dalam dokumen recovery). APK pratonton 0.1.7 sebenar ialah `CN=iDash Preview`, SHA256 `49:F6:AD:C6…:DE:73` (diukur dengan `keytool`). APK 0.1.8 (delivery) dan APK iDash2 tidak menunjukkan sijil v1 (`keytool -jarfile` kosong) dan `apksigner` tiada dalam persekitaran, jadi signer mereka **tidak dapat saya sahkan** | [BUKTI] ukuran 0.1.7; 0.1.8 [TIDAK DISAHKAN] | Sahkan dengan `apksigner verify --print-certs` pada mesin anda sebelum menuntut apa-apa tentang kemas kini |
| B3 | Rendah | Tiada `signingConfig` release; `minifyEnabled false`. Skim v1/v2/v3: AGP menandatangani debug dengan v1+v2 mengikut lalai pada minSdk 26; API 27 menyokong v2 | `app/build.gradle` | Tentukan keputusan signing sebelum APK release; jangan jana release tanpa itu |
| B4 | Rendah | `versionCode 9` / `0.1.8`. iDash3 perlu `versionCode` bermula sendiri kerana `applicationId` baharu | build.gradle | Mulakan 1 |

## C. Keserasian API & hardware
| ID | Sev | Isu | Bukti |
|---|---|---|---|
| C1 | Rendah | `Build.VERSION` digunakan di 11 tempat (`QuickControls`, `ControlMedia`, `DashWebPlayer`, dll.). Lint dalam CI run 1 lulus (0 ralat) dengan minSdk 26 → tiada panggilan API>26 tanpa guard yang dikesan lint [BUKTI: job "lintDebug" success] |
| C2 | Rendah | Tiada pustaka native dalam APK (`.so` tiada) → neutral ABI, jalan pada armeabi-v7a [BUKTI: `unzip -l`] ; prestasi ARM32 sebenar [TIDAK DIUJI] |
| C3 | Sederhana | WebView peranti lama: kod menangkap `RuntimeException|LinkageError` dan memaparkan mesej; `onRenderProcessGone` dikendali. Tetapi YouTube web mungkin tidak berfungsi pada WebView lama [ANDAIAN] — perlu uji pada head unit |
| C4 | Sederhana | Tiada Google Play Services dalam kebergantungan (Gradle hanya `com.android.application`, tiada `dependencies`) [BUKTI] |
| C5 | Rendah | Pelancaran aplikasi luar guna `ComponentName` + `FLAG_ACTIVITY_NEW_TASK`; kegagalan ditangkap `RuntimeException` (`LauncherActivity:183`) |
| C6 | Rendah | Back: `onBackPressed` memulangkan ke "home"; pada launcher HOME biasanya tiada keluar — betul |

## D. Stabiliti & prestasi
| ID | Sev | Isu | Lokasi | Punca/Bukti | Cadangan | Ujian |
|---|---|---|---|---|---|---|
| D1 | Sederhana | `GlassSurface.draw` melakukan `getLocationOnScreen` + `geometryHash` (traversal subpokok) setiap bingkai, untuk setiap permukaan kaca | `GlassSurface.java:45` | Kod [BUKTI]; kos [belum diukur] | Cache dengan dirty-flag dari `OnLayoutChange`, kemas kini hanya bila geometri berubah | `gfxinfo` sebelum/selepas |
| D2 | Rendah | Wallpaper peribadi: `inSampleSize` dikira, maks 1280 px, `decodeFile` tanpa pengendalian OOM | `CustomWallpaper.java:13-17` | Kod; 1280×720 ARGB_8888 ≈ 3.7 MB — selamat | Tangkap `OutOfMemoryError` → fallback gradient | Fail 20 MP |
| D3 | Rendah | `SagaAngleView` memuat atlas dengan `inSampleSize=2` dan menolak jika >2 MB; recycle pada tukar epoch | `SagaAngleView.java:103-109` | [BUKTI] baik | Kekal | — |
| D4 | Rendah | `worker.shutdownNow()` dipanggil pada `onDestroy`; penerima didaftar/dinyahdaftar dengan guard (`ControlUtilities`, `ControlMedia`, `DashFmController`) | — | [BUKTI] | Kekal | Putar skrin berulang + `dumpsys meminfo` |
| D5 | Rendah | Polling `audioFallback` (2 s) dan ticker (1 s) dijeda pada `onStop`/`pause` | `QuickControls:49`, `LauncherActivity:38` | [BUKTI] | Kekal | — |
| D6 | Sederhana | Fail terlalu padat (82 baris >400 aksara) menyukarkan semakan, ujian dan Fasa 3 | semua | `awk` | Pecah mengikut tanggungjawab semasa import | — |
| D7 | Sederhana | Tiada ujian unit; instrumentation CI tidak stabil (Smoke GAGAL, SystemControls TAMAT_MASA, emulator offline) | CI run 1 | [BUKTI] | Siasat artifact `instrumentation-logs`; pisahkan kelas | Ulang di iDash3 |
| D8 | Rendah | Risiko crash semasa startup: `migrateFavorites`, `migratePanelPreferences` dalam `onCreate`; `GlassAppearance.integer` menangkap `ClassCastException` (baik) | `LauncherActivity:46` | Kod | Kekal | `StabilityChecks` (LULUS di CI) |

## E. Aset & lesen — **perlu keputusan anda**
| ID | Sev | Isu | Bukti |
|---|---|---|---|
| E1 | **Tinggi (undang-undang)** | 36 ikon, 24 wallpaper, 4 fon SF Pro Display dan 2 PNG diekstrak daripada `MRiOS V2.apk`; NOTICE sendiri menyatakan **lesen pengedaran tidak ditetapkan**. SF Pro ialah fon Apple dengan lesen terhad. Mengedarkan dalam repo (walaupun peribadi) berisiko | `NOTICE`, `docs/MRIOS_REVIEW.md` |
| E2 | Sederhana | Aset SmartTube/NexaDash: kod MIT dengan atribusi; artwork kereta Saga "diminta pemilik" tetapi sumbernya perlu disahkan | `NOTICE` |
| E3 | Rendah | Saiz aset: ikon 2.0 MB, wallpaper 2.4 MB, fon 1.3 MB, drawable-nodpi 1.3 MB (APK ≈ 6.4 MB). Fallback jika aset gagal: ada (gradient) |
| E4 | Rendah | WebP wallpaper memerlukan minSdk 18+ (lossy) — OK pada 26 |

Cadangan: iDash3 bermula **tanpa** aset MRiOS/SF Pro; gunakan aset sendiri/berlesen terbuka (fon: Inter atau Roboto; ikon: dilukis dalam app, selaras Fasa 3B #7).

## F. Perkara yang saya TIDAK dapat sahkan
- Prestasi/RAM pada head unit sebenar, ARM32, WebView sebenar.
- Isi artifact `instrumentation-logs` (punca Smoke GAGAL) dan keputusan run 2.
- Signer APK 0.1.8 (tiada `apksigner`).

## G. Status selepas Fasa 2–5 (iDash3)
| ID | Status dalam iDash3 | Bukti |
|---|---|---|
| A5 | **Terbuka**: kebenaran diwarisi tanpa perubahan (perlu ujian fungsi Control Center pada head unit sebelum dibuang) | `AndroidManifest.xml` |
| A6/A8 | Ditangani: permission signature dinamakan `com.shazwan.idash3.permission.CONTROL` (elak `INSTALL_FAILED_DUPLICATE_PERMISSION` bila dipasang bersama iDash); `queries` SmartTube dibuang | `guard.sh` |
| B1/B4 | Ditangani untuk pemasangan selari: `applicationId com.shazwan.idash3`, `versionCode 1`. Release/keystore **belum diputuskan**; tiada APK release dibina | `app/build.gradle` |
| B3 | Debug APK iDash3: v2 sahaja (minSdk 26 → v1 tidak diperlukan; API 27 menyokong v2). Disahkan `apksigner verify` | log sesi |
| C2 | Ditangani: APK tiada `native-code` (disemak `guard.sh` dengan aapt2) | CI |
| D7 | Ditangani sebahagian: 29+ ujian unit JVM sebenar (Spring, GlassPerf, Adaptive, LiquidShape, HomeLayout); runner instrumentation iDash3 jalankan kelas satu demi satu dengan had masa + semakan kesihatan emulator | `app/src/test`, `tools/ci` |
| D1 | **Terbuka**: `geometryHash` setiap bingkai dikekalkan (tingkah laku 6C yang diuji); kos tambahan Fasa 3 (adaptRegion) hanya hash + `getLocationOnScreen`, sampel piksel hanya bila berubah | `GlassSurface.java` |
| E1 | Ditangani: aset MRiOS, SF Pro dan SmartTube **tidak** dibawa; wallpaper dijana kod, font sistem, ikon = ikon app sendiri. Saiz APK 6.4 MB → ±1.3 MB | `guard.sh`, `NOTICE` |
| E2 | **Terbuka**: atlas kereta Saga (NexaDash) dikekalkan atas permintaan pemilik; pemegang hak asal belum disahkan — ganti sebelum edaran awam | `NOTICE` |
