# HARDWARE-BASELINE — TEST9278B (ALPS / 8227L)

Status dokumen: **baseline rujukan**. Semua angka di bawah datang daripada aplikasi diagnostik pada head unit (bukan ADB), jadi ia dilabel **[DIAG]** dan bukan **[DISAHKAN ADB]**. Tiada peranti atau emulator ARM32 dalam sesi audit ini.

Tarikh audit: 9 November 2026 · Asas kod: `main` @ `ad16f48` · APK dirujuk: `dist/iDash3-0.2.0-dev-debug-QA.apk`

---

## 1. Spesifikasi sasaran

| Kategori | Nilai | Label |
|---|---|---|
| Model | TEST9278B | [DIAG] |
| Platform | ALPS / MT8227L | [DIAG] |
| Android dilaporkan | 9.1 | [DIAG] |
| `SDK_INT` sebenar | **27** (Android 8.1 Oreo) | [DIAG] |
| Kernel | 3.18.79 | [DIAG] |
| CPU | 4× ARM32 Cortex-A7, `armeabi-v7a` / `armeabi`, NEON | [DIAG] |
| GPU | ARM Mali-450 MP, OpenGL ES 2.0 | [DIAG] |
| RAM total | 1024 MB | [DIAG] |
| RAM tersedia semasa pemeriksaan | 315.2 MB | [DIAG] |
| Ambang low-memory sistem | 60 MB | [DIAG] |
| Paparan | 1024×600, **160 dpi / mdpi**, 60 Hz | [DIAG] |
| Storage | 12.12 GB total, 5.41 GB digunakan | [DIAG] |
| Bluetooth | Ya (klasik sahaja, **tiada BLE**) | [DIAG] |
| Wi-Fi | Ya, 2.4 GHz sahaja (tiada 5 GHz) | [DIAG] |
| GPS | Ya | [DIAG] |
| USB OTG | Ya | [DIAG] |
| Root | Dilaporkan ya (**tidak digunakan oleh iDash3**) | [DIAG] |
| Security patch | 2020-02-01 | [DIAG] |

### Perbezaan penting berbanding telefon moden

| Andaian biasa | Realiti peranti ini | Kesan pada iDash3 |
|---|---|---|
| xxhdpi (3×) | **mdpi (1×)** | Semua `dp` = `px`. Ikon 56 dp = 56 px, bukan 168 px. Saiz teks/label yang nampak "besar" pada tangkapan skrin anda ialah saiz dp sebenar. |
| GPU capable (ES 3.x, RenderEffect) | **ES 2.0, Mali-450 MP** | Tiada `RenderEffect`/`RuntimeShader` (API 31+), tiada Vulkan. Semua kaca mesti kekal pada `Canvas`/`BitmapShader` + bitmap "baked". |
| RAM ≥ 3 GB | **1 GB, ~315 MB bebas** | Setiap bitmap 1 MB bermakna. `largeHeap` sengaja **tidak** didedahkan (disemak: tiada dalam manifest). |
| Heap 256–512 MB | Lazimnya **96–128 MB** pada peranti 1 GB (perlu disahkan) | Bajet lamina kaca + wallpaper + WebView mesti dikira, bukan diandaikan. |
| Play Services tersedia | **Tidak diandaikan** | iDash3 tiada satu pun kebergantungan Play Services (disemak: `app/build.gradle` hanya `junit` untuk ujian). |
| WebView moden | WebView OEM lama (Chrome 6x–7x lazimnya) | Kod menangkap `RuntimeException|LinkageError` dan memaparkan mesej; `onRenderProcessGone` dikendalikan. YouTube web mungkin terhad — [BELUM DIUJI]. |

---

## 2. Bajet memori yang dikira daripada kod

Nilai di bawah dikira daripada saiz bitmap yang benar-benar diperuntukkan oleh kod [BUKTI KOD], bukan diukur pada peranti.

| Komponen | Sumber | Saiz ARGB_8888 |
|---|---|---|
| Wallpaper aktif (1280 px tepi panjang) | `LauncherActivity.ensureWallpaper` → `Wallpapers.decode(...,1280)` | 1280×720 ≈ **3.7 MB** |
| Bingkai frost kaca (BALANCED) | `IosStyle.FROST_EDGE[1]=224` → 224×126 | ≈ **0.11 MB** (+ `soft` 56×31) |
| Cache "baked glass" global | `GlassBake.BUDGET_BYTES` | **6.0 MB** (LruCache, boleh dikosongkan) |
| Atlas kereta Saga | `saga_angles.png` 1774×887, `inSampleSize=2` → 887×443 | ≈ **1.57 MB** (ditolak jika >2 MB) |
| Ikon app (56 px setiap satu) | `IconStyler.render(d, dp(56), …)` | 56×56×4 ≈ **12.5 KB/app** → 100 app ≈ 1.25 MB |
| Fon Inter (5 fail TTF) | `res/font` | ≈ 0.43 MB (mmap, bukan heap Java) |
| `classes.dex` + `classes2.dex` | APK | ≈ 0.36 MB (mmap) |
| **Jumlah static launcher (tanpa WebView)** | — | **≈ 13 MB** |
| WebView YouTube semasa aktif | Chrome OEM | **20–60 MB** [ANDAIAN, bergantung WebView] |

Kesimpulan bajet: launcher kosong ≈ 13 MB static + ~15–25 MB PSS asas. Dengan WebView aktif, jumlah boleh mencecah 60–80 MB — iaitu **hampir siling heap 96 MB** jika peranti menggunakan `heapgrowthlimit=96m`. Inilah sebab isu **P1** dan **P2** dalam `docs/PERFORMANCE-AUDIT.md` dikategorikan Tinggi.

---

## 3. Had kejuruteraan yang mengikat keputusan

1. **Tiada API > 27 tanpa guard.** `minSdk 26`, `targetSdk 35`; lint melaporkan **0 ralat** (lihat `docs/ANDROID-COMPATIBILITY-AUDIT.md` §5).
2. **Tiada pustaka native.** APK ada 20 entri, **tiada `lib/`** → ABI-neutral, selamat pada `armeabi-v7a` sahaja.
3. **Tiada root, tiada VirtualDisplay, tiada embedding.** `tools/ci/guard.sh:11` menyekat corak tersebut dan lulus.
4. **Kaca mesti kekal bitmap-based.** `GlassBake` mengira pembiasan sekali pada worker dan cache 6 MB; ia satu-satunya jalan yang munasabah pada Mali-450 ES 2.0.
5. **mdpi bermakna teks 12–13 sp = 12–13 px.** Saiz yang kelihatan "kecil" pada telefon ialah saiz normal pada 160 dpi.

---

## 4. Baseline yang perlu diukur pada peranti (belum dijalankan)

Perintah ini perlu dijalankan pada head unit (adb melalui USB OTG, atau shell tempatan jika ada) untuk menukar label [ANDAIAN] kepada [DISAHKAN]. **Tiada satu pun dijalankan dalam audit ini** — tiada peranti.

```bash
# Heap sebenar: adakah peranti ini 96 MB atau 128 MB?
adb shell getprop dalvik.vm.heapgrowthlimit
adb shell getprop dalvik.vm.heapsize
adb shell getprop ro.config.low_ram

# Baseline memori launcher kosong vs selepas Drive/panel WebView
adb shell dumpsys meminfo com.shazwan.idash3
adb shell dumpsys meminfo com.shazwan.idash3 | grep -E "TOTAL PSS|Java Heap|Native Heap"

# Masa mula sejuk (3 kali, ambil median)
adb shell am force-stop com.shazwan.idash3
adb shell am start -W -n com.shazwan.idash3/.LauncherActivity

# Bingkai semasa idle dan semasa tukar halaman
adb shell dumpsys gfxinfo com.shazwan.idash3 reset
# (gunakan launcher 3-4 saat)
adb shell dumpsys gfxinfo com.shazwan.idash3 framestats | head -40

# Crash/ANR
adb logcat -b crash -d
adb shell dumpsys activity processes | grep -i idash3
```

| Metrik | Sasaran pada 1 GB / Mali-450 | Status |
|---|---|---|
| Mula sejuk | < 2500 ms | NOT RUN |
| PSS launcher kosong | < 45 MB | NOT RUN |
| PSS dengan panel YouTube | < 80 MB | NOT RUN |
| p90 masa bingkai semasa leret halaman | < 24 ms (42 fps) | NOT RUN |
| Kadar "janky" `gfxinfo` selepas 30 s idle | < 30% | NOT RUN |
| Tiada ANR/crash dalam 30 min penggunaan | 0 | NOT RUN |

> Nota: angka emulator lama (`docs/VALIDATION.md`, run 42320648951) menunjukkan p90 **36 ms** pada GPU perisian SwiftShader. Itu **bukan** ukuran Mali-450 dan tidak boleh digunakan sebagai bukti prestasi head unit.
