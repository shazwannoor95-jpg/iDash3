# PERFORMANCE-AUDIT — RAM, CPU, ANR iDash3 pada 1 GB / ARM32

Asas: `main` @ `ad16f48` (audit awal, read-only). **Callback Drive** kemudiannya dibetulkan; **P1** dan **P2** dilaksanakan lalu di-revert kerana mengubah penampilan **Liquid Glass V2 / Fasa 3B** pada peranti 1 GB — lihat §5.

Label: **[BUKTI KOD]** = dibaca dalam source · **[BUKTI UKUR]** = dijalankan dalam sesi ini · **[ANDAIAN]** = perlu ukuran peranti.

Ringkasan: **2 Tinggi · 4 Sederhana · 4 Rendah · 3 Maklumat (disahkan baik)**.

> **Status 2026-11-10:** **callback Drive** (lihat §3) telah dibetulkan. **P1** dan **P2** dilaksanakan kemudian **di-revert semula** atas permintaan pengguna kerana kedua-duanya mengubah penampilan kaca pada peranti 1 GB:
> - **P1** menukar kualiti kaca ke **LOW** (tiada cincin lensa, refraksi, specular sentuhan, bevel, sorotan specular dan lapisan refleksi; `sheen`/`edge` pada 55%) — kaca kelihatan lebih rata pada **semua** panel, bukan hanya semasa animasi.
> - **P2** hanya mengubah fasa animasi masuk (frost + lensa kuat sepanjang spring), tetapi ia tidak lagi relevan kerana P1 (yang membolehkan refraksi diminta) juga dibuang.
>
> Kaca kini kembali sama seperti sebelum audit. Mod **Rendah** masih tersedia manual dalam Settings → Mod prestasi. Isu lain di bawah masih terbuka.

> Tiada ukuran `dumpsys meminfo`/`gfxinfo` pada head unit dalam audit ini (tiada peranti/adb). Semua severity di bawah berasaskan bukti kod dan aritmetik bitmap, bukan profil sebenar. Itu dijelaskan dalam `docs/HARDWARE-BASELINE.md §4` dan `docs/HEADUNIT-VALIDATION.md`.

---

## 1. Isu

| ID | Sev | Isu | Fail:baris | Bukti | Punca | Risiko | Cadangan | Pengesahan |
|---|---|---|---|---|---|---|---|---|
| P1 | **Tinggi** | Pengesanan "memori rendah" tidak akan mencetuskan pada peranti 1 GB | `GlassPerf.java:32` | [BUKTI KOD] `memoryLow = am.isLowRamDevice() \|\| Runtime.maxMemory() < 96L*1024*1024` | Ambang `< 96 MB` **ketat**. Peranti 1 GB lazimnya `dalvik.vm.heapgrowthlimit=96m` → `maxMemory()` ≈ 100,663,296 bait → syarat **palsu**. `isLowRamDevice()` pula hanya benar jika OEM menetapkan `ro.config.low_ram=true`, yang jarang pada head unit 8227L | Mod "Auto" kekal **Penuh**: lensing, specular dinamik dan bake setiap panel dihidupkan pada Mali-450 dengan ~315 MB bebas. Inilah punca paling mungkin bagi "launcher berat/tersekat" tanpa perlu mengubah kaca | Tukar kepada `<=` **atau** `am.getMemoryClass() <= 96` **atau** `maxMemory() < 128 MB`. Satu baris, tiada kesan visual, hanya memilih laluan LOW yang sudah wujud dan diuji (`GlassPerfTest`, `Idash3Checks`) | `adb logcat` selepas tambah log `GlassPerf.describe()`; dijangka "Auto: rendah (memori)". Bandingkan `dumpsys gfxinfo` Penuh vs Rendah |
| P2 | **Tinggi** | Banjir kerja "bake" semasa animasi masuk kaca (cadangan diuji lalu di-revert, §5) | `GlassSurface.java:64,77,81`; `GlassBake.java:44`; `IosStyle.java` (`enter` → `setReveal`) | [BUKTI KOD] `draw()` → `adaptRegion()` → `requestBake()`; kunci bake = `Math.round(left/top)` + saiz + `radius`; `IosStyle.enter` menganimasikan `translationY` 8dp → 0 selama ~300 ms, dan `setReveal` menginvalidasi setiap bingkai | Kedudukan panel berubah setiap bingkai semasa animasi, jadi **kunci berubah setiap bingkai** → satu tugas bake baharu dihantar setiap bingkai untuk setiap permukaan (sehingga 48 permukaan, `collectGlass` had 48). Setiap tugas memperuntukkan `int[sw*sh]` + `int[outW*outH]` + `Bitmap` ≈ **0.3–0.7 MB** dan mengira SDF+juling penuh | Beban CPU berterusan selepas setiap navigasi (bukan hanya 300 ms): hasil bake biasanya sudah basi (`k != bakeKey`), jadi kerja dibuang. Pada Mali-450 ini menjana jank, GC churn dan cache 6 MB yang sentiasa ditolak keluar. Risiko OOM rendah (peruntukan dalam tugas, bukan dalam baris gilir) tetapi GC pressure nyata | Jangan bake semasa `reveal < 1` (atau semasa `translationY != 0`): `if (reveal < 1f) return;` dalam `requestBake`, dan panggil semula selepas spring tamat. **Perlu kelulusan** kerana menyentuh `GlassSurface` (walaupun bukan rumus visual) | Kira bilangan `bake` dengan pembilang sementara; sebelum/selepas: `dumpsys gfxinfo` semasa tukar halaman. Keputusan dijangka: tugasan turun >90%, jank turun |
| P3 | **Sederhana** | Setiap navigasi membina semula seluruh pokok view + semua shader kaca | `LauncherActivity.java:84` (`render()`), dipanggil dari `:110,156-158,183,201` dan `onConfigurationChanged:60` | [BUKTI KOD] `render()` mencipta `stage`, `Wallpaper`, `ImageView`, `shell`, `body`, header, dock; setiap `a.glass(...)` mencipta `GlassSurface` yang membina ~8–10 `Shader` dalam `rebuild()` (`GlassSurface.java:35-60`) dan mendaftar dalam `backdrop.surfaces` | Seni bina "render penuh" yang mudah dan konsisten, tetapi mahal: 20–48 permukaan × ~10 shader = 200–480 peruntukan shader setiap navigasi, ditambah `IosStyle.enter` | Tukar tab Settings↔Home atau Drive↔Home berulang akan menyebabkan stutter yang konsisten pada GPU lemah. Bukan kebocoran, tetapi kos berulang | Jangka panjang: `View` kekal + `invalidate()` sahaja untuk perubahan tetapan. Jangka pendek: jangan `render()` apabila hanya `page` bertukar ke halaman yang strukturnya sama. **Perlu reka bentuk, bukan tampalan** | Ukur masa `render()` dengan `SystemClock.uptimeMillis()` sementara; `gfxinfo` semasa navigasi |
| P4 | **Sederhana** | Senarai app selesai dimuat → `render()` walaupun pengguna sudah di halaman lain | `LauncherActivity.java:201` | [BUKTI KOD] `main.post(... loaded=true; ... else if(!panelVisible && appearanceEditor==null) render();)` | Muat app berlaku di latar; apabila selesai, kod andaikan pengguna masih di Home | Jika pengguna membuka Drive/Settings dalam 300–800 ms pertama selepas mula, halaman itu **dibina semula**: panel WebView/FM ditutup, animasi dimainkan semula, dan pilihan skrol hilang. Pada peranti perlahan tetingkap ini lebih besar | `else if("home".equals(page) && !panelVisible && appearanceEditor==null) render(); else if("apps".equals(page)) filter();` dan untuk halaman lain, hanya kemas kini data | Lancar app → segera tekan Drive; sahkan halaman tidak "berkelip" selepas senarai app selesai |
| P5 | **Sederhana** | Satu worker untuk ikon + JAKIM + wallpaper + FM | `LauncherActivity.java:22`; digunakan di `:194-201` (ikon), `:277` (wallpaper), `:290` (JAKIM), `QuickControls.java:70` (Wi-Fi/BT) | [BUKTI KOD] `Executors.newSingleThreadExecutor()` | Mudah dan selamat (satu urutan), tetapi semua kerja bersaing | Muat turun JAKIM dengan timeout 8 s + 8 s (`JakimCalendar.java:57`) menyekat muat ikon, dekod wallpaper dan togol Wi-Fi/BT yang dihantar ke worker yang sama → butang nampak "tidak bertindak balas" sehingga 16 s | Pisahkan kepada dua executor (I/O rangkaian vs pemprosesan bitmap), atau hadkan rangkaian pada executor berasingan. **Perlu kelulusan** | Putuskan Internet, tukar zon JAKIM, serta-merta tekan Refresh pada App Library; ukur kelewatan |
| P6 | **Sederhana** | `VelocityTracker` tidak dikitar semula apabila anak view mengambil gerakan | `HomeGrid.java:190` (`obtain` pada `ACTION_DOWN`), `:206,222` (`recycle` hanya dalam `drag()`) | [BUKTI KOD] | Trek dibuat dalam `intercept()` setiap kali jari menyentuh grid, tetapi `recycle()` hanya berlaku dalam `drag()`; jika tile app/folder mengambil sentuhan (klik biasa), `drag()` tidak pernah melihat `ACTION_UP` | Setiap ketikan pada grid berpotensi meninggalkan satu `VelocityTracker` (sumber native). Dalam sesi panjang (head unit dihidupkan berjam-jam) ini terkumpul | Panggil `recycle()` dalam `onDetachedFromWindow` **dan** dalam `intercept()` apabila `ACTION_UP/CANCEL` diterima tanpa seret; atau hanya `obtain()` selepas slop dilalui | `adb shell dumpsys meminfo` Native Heap selepas 200 ketikan pada grid; dijangka stabil |
| P7 | Rendah | Kunci bake "tersangkut" dalam `pending` jika permintaan ditolak awal | `GlassBake.java:44` (`if (src == null \|\| outW < 2 \|\| outH < 2) return;`) | [BUKTI KOD] `begin(key)` menambah ke `pending` sebelum `request()`; laluan pulang awal tidak membuang kunci | Satu panel yang terlalu kecil untuk di-bake menyimpan kuncinya selama-lamanya | Kunci itu tidak akan pernah di-bake lagi (panel kekal pada frost fallback). Bukan kebocoran besar (kunci terhad), tetapi hasilnya tidak konsisten | `pending.remove(key)` sebelum `return` awal, atau panggil `begin` selepas semakan saiz | Semak `GlassBake.cache` selepas 10 min; panel kecil kekal tidak di-bake |
| P8 | Rendah | Cache bake 6 MB pada heap yang mungkin 96 MB | `GlassBake.java:19-21` | [BUKTI KOD] `BUDGET_BYTES = 6*1024*1024` | Saiz tetap, tidak berskala dengan heap | 6 MB + wallpaper 3.7 MB + konteks WebView boleh menolak jumlah ke siling heap → GC kerap atau `OutOfMemoryError` yang ditangkap (jatuh ke frost) | Skalakan bajet: `Math.min(6MB, maxMemory()/16)`. **Perlu kelulusan** | `dumpsys meminfo` dengan panel YouTube + Launcher; perhatikan "Java Heap" hampir had |
| P9 | Rendah | Dekod wallpaper pengguna tiada pengendalian `OutOfMemoryError` | `CustomWallpaper.java:13-17`; dipanggil `LauncherActivity.java:277` | [BUKTI KOD] `decodeFile` tanpa `catch (OutOfMemoryError)`; pemanggil hanya `catch (IOException)` | Imej besar (sehingga 16 MiB dibenarkan) pada heap kecil | OOM melarikan diri daripada tugas worker; `ThreadPoolExecutor` akan log dan menggantikan thread, app **tidak** crash, tetapi wallpaper gagal senyap dan pengguna tidak tahu sebab | Balut `decode` dengan `catch (OutOfMemoryError)` → `throw new IOException("Wallpaper terlalu besar")` supaya mesej sedia ada muncul | Pilih imej 8000×6000 pada peranti; dijangka Toast "Image unavailable…" bukan log OOM |
| P10 | Rendah | Ticker 1 s dan fallback audio 2 s | `LauncherActivity.java:39,56-57`; `QuickControls.java:49` | [BUKTI KOD] ticker dihentikan pada `onPause`/`onStop`, dimulakan semula pada `onResume`; `updateClock` hanya memformat bila minit/tz/locale berubah | Reka bentuk sedar | Kos idle rendah; tiada kebocoran. **Disahkan baik** | Kekalkan | `dumpsys gfxinfo` selepas 60 s idle: dijangka 0 bingkai baharu |
| P11 | Maklumat | Atlas Saga dimuatkan dengan sampel 2 dan had 2 MB | `SagaAngleView.java:102-109`; aset 1774×887 (1,159,103 bait PNG) | [BUKTI KOD] `inSampleSize=2` → 887×443 ≈ 1.57 MB; jika `>2 MB` bitmap dikitar semula dan dianggap gagal; `pause()` melepaskan rujukan tanpa `recycle` (selamat kerana display list) | Reka bentuk sedar | Tiada isu; **disahkan baik** | Kekalkan | — |
| P12 | Maklumat | Tiada perkhidmatan latar berterusan selain bar Drive (foreground) | `AndroidManifest.xml`; `DriveBarService.java:55` | [BUKTI KOD] | Direka begitu | Idle CPU rendah; tiada `AlarmManager`/`JobScheduler`/polling | Kekalkan | `adb shell dumpsys activity services com.shazwan.idash3` |
| P13 | Maklumat | `HomeWidgets.onMeasure` boleh mengukur sehingga 4 profil setiap layout | `HomeWidgets.java:22` | [BUKTI KOD] gelung `SCALE.length` | Menjamin teks muat tanpa memotong | 4× `super.onMeasure` pada lajur widget kecil — kos rendah, tetapi berlaku pada main thread setiap layout | Kekalkan; jika profil ke-0 sudah muat (biasa), hanya 1–2 laluan berlaku | `gfxinfo` semasa tukar saiz fon |

---

## 2. Analisis ringkas mengikut kategori yang diminta

| Kategori | Penemuan |
|---|---|
| **Memory leak** | Tiada kebocoran `Activity`/`Context` ditemui. `IosStyle.backdrops` ialah `WeakHashMap<Context,…>`; `GlassBackdrop.surfaces` menyimpan `WeakReference`; tema/ikon tiada rujukan statik kepada Activity. Satu sumber terkumpul: **P6** (`VelocityTracker`). Satu risiko senyap: **P9** |
| **Bitmap cache & wallpaper** | Wallpaper 1280×720 (3.7 MB) kekal semasa Activity hidup dan dilepaskan pada `onDestroy` (`IosStyle.releaseGlass`, `wallpaperBitmap` GC). Cache bake 6 MB global (**P8**). Pratonton galeri 8×192×108 ≈ 0.66 MB, dilepaskan selepas dialog ditutup |
| **App icon loading** | Diskripsi pada worker, ikon 56 px ARGB (12.5 KB/app), `catch (OutOfMemoryError)` dengan fallback `drawn` terus ke bitmap (**disahkan baik**, `LauncherActivity.java:198`). Tiada cache ikon merentas `loadApps()` — setiap Refresh membina semula (kos CPU, bukan kebocoran) |
| **Background services** | Hanya `DriveBarService` (foreground, `START_NOT_STICKY`, dimatikan oleh `DriveController.stop`). `SplitService`/`MediaAccessService` diikat sistem, tiada kerja |
| **Timer & Handler** | `ticker` 1 s (dijeda betul), `audioFallback` 2 s (hanya dialog terbuka), `settingsRefresh` debounce 160 ms, `volumeWrite` had 120 ms, `GlassBake.main` post, `DriveController.MAIN` (lihat **D-2** dalam dokumen install? tidak — lihat nota di bawah). Tiada `Timer`/`scheduleAtFixedRate` |
| **BroadcastReceiver** | Semua didaftar dengan flag API 33 guard dan dinyahdaftar dalam `pause()`/`close()`; `try/catch IllegalArgumentException` pada unregister. **Disahkan baik** |
| **Main-thread blocking** | `queryIntentActivities` + `loadIcon` pada worker (**baik**). Tetapi `render()` + `IosStyle.enter` + `GlassSurface.rebuild` berjalan pada main thread (**P3**). Tiada I/O rangkaian pada main thread |
| **Risiko ANR** | Rendah. Tiada operasi >5 s pada main thread. Satu-satunya calon: `render()` penuh pada peranti sangat perlahan dengan 48 permukaan kaca + animasi; masih jauh di bawah 5 s, tetapi menghasilkan jank, bukan ANR |
| **Risiko OOM** | Sederhana. Siling heap 96 MB (andaian) vs 13 MB static + WebView 20–60 MB + cache 6 MB. Ditangkap di banyak tempat (`GlassBackdrop.prepare`, `GlassBake.bake`, ikon), **kecuali** `CustomWallpaper.decode` (**P9**) dan `Wallpapers.decode` (ditangkap → `IOException`) |
| **CPU ketika idle** | Rendah. Tiada bingkai dilukis apabila tiada perubahan; `SpringDriver` berhenti apabila spring berehat; tiada polling kecuali `ticker` 1 s. **Disahkan baik** |

---

## 3. Nota tambahan: Drive boleh meninggalkan callback tertangguh (DIBETULKAN)

`DriveController.start()` menjadualkan rantaian `postDelayed` 2300 ms → 1700 ms → 900 ms (`DriveController.java:20,71-75`) dan `stop()` hanya menjadualkan satu lagi `postDelayed` 600 ms (`:87`). Tiada `removeCallbacks`. Jika pengguna menekan "Mula Drive" kemudian "Keluar Drive" dalam < 2.3 s, callback split/launch/bar masih akan berjalan selepas keluar.

Kesan: split-screen dipecah atau app kedua dibuka selepas pengguna sudah keluar Drive; bar terapung muncul semula. Ini isu kestabilan fungsi, bukan prestasi, dan dicatat di sini kerana ia melibatkan `Handler`. Severity **Sederhana**.

**Pembetulan dilaksanakan:** setiap langkah tertangguh didaftarkan dalam `DriveController.PENDING` melalui `post(delay, task)`, dan `cancelPending()` dipanggil di awal `start()` dan `stop()`, jadi menekan Keluar membatalkan baki rantaian (dock 2300 ms, app kedua 1700 ms, bar 900 ms). Disahkan melalui semakan kod + binaan/ujian; **belum disahkan pada peranti**.

---

## 4. Pelan pembetulan mengikut keutamaan

| Urutan | ID | Usaha | Kesan dijangka | Risiko |
|---|---|---|---|---|
| 1 | **P1** | 1 baris (`<=` atau `getMemoryClass()`) | Mod Auto betul-betul memilih LOW pada peranti ini → kurang lensing/bake | Sangat rendah (laluan LOW sudah diuji) |
| 2 | **P2** | 3–5 baris (`reveal < 1` guard) | Hilangkan banjir bake selepas setiap navigasi | Rendah; **menyentuh fail kaca**, perlu kelulusan |
| 3 | **D-2 / Drive** | 6 baris (simpan Runnable + removeCallbacks) | Drive tidak "hidup semula" selepas Keluar | Rendah |
| 4 | **P6** | 4 baris | Hentikan pengumpulan `VelocityTracker` | Sangat rendah |
| 5 | **P4** | 2 baris | Halaman Drive/Settings tidak dibina semula oleh muat app | Rendah |
| 6 | **P9** | 3 baris | Mesej jelas untuk wallpaper terlalu besar | Sangat rendah |
| 7 | **P3/P5/P8** | Reka bentuk semula kecil | Prestasi lebih baik | Sederhana; jadualkan berasingan |

Urutan 1–2 dilaksanakan lalu di-revert kerana menukar penampilan kaca; urutan 3 (callback Drive) kekal; urutan 4–7 belum (§5).

## 5. Status pelaksanaan (2026-11-10)

| ID | Status | Perubahan |
|---|---|---|
| **P1** | **DI-REVERT** | Dilaksanakan (`GlassPerf.memoryTight`: LOW bila heap ≤ 96 MB atau RAM ≤ 1.5 GB, + 2 ujian unit), kemudian dibuang semula: ia menjadikan `capQuality()` menurunkan **semua** panel ke LOW pada head unit 1 GB, iaitu tiada lensa, refraksi, specular sentuhan, bevel, sorotan dan refleksi — penampilan kaca berubah. `load()` kembali kepada `isLowRamDevice() \|\| maxMemory < 96 MB`, dan `memoryTight` serta dua ujiannya dibuang (44 ujian unit) |
| **P2** | **DI-REVERT** | Dilaksanakan (tunda bake sehingga `reveal == 1` + penetapan semula `adaptKey`), kemudian dibuang semula bersama P1: ia mengubah fasa reveal (frost + lensa kuat sepanjang spring, refraksi masuk di penghujung). `GlassSurface` kembali kepada versi `96ec577`; rentetan banjir bake kembali seperti sebelum audit |
| **Callback Drive** | **DIBETULKAN** | `DriveController.PENDING` + `post()` + `cancelPending()` dalam `start()`/`stop()` |
| P1, P2, P3, P4, P5, P6, P7, P8, P9 | Terbuka | P1/P2 hanya boleh diterima semula jika penampilan kaca dibenarkan berubah (atau jika ia dibuat pilihan dalam Settings). P8 menyentuh bajet cache kaca dan memerlukan kelulusan berasingan |

Tiada ukuran pada peranti dibuat dalam sesi ini (tiada peranti/adb), jadi kesan prestasi P1/P2 tidak pernah diukur — ia dibuang atas dasar penampilan, bukan atas dasar ukuran.
