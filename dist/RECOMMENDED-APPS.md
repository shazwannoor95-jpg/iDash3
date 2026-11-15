# App disyorkan untuk head unit Proton Saga (Android 8.1 / API 27, 32-bit, ±1 GB RAM)

Berdasarkan audit dalam [`docs/HEADUNIT-APPS.md`](../docs/HEADUNIT-APPS.md). **APK pihak ketiga tidak dimuat naik ke repo ini**:
Waze dan Chrome ialah app proprietari Google (tidak boleh diedarkan semula), manakala NewPipe paling selamat dipasang terus daripada F-Droid (sudah ada pada head unit) supaya tandatangan dan kemas kini dijaga.

| App | Versi untuk head unit ini | Dapatkan dari | Nota |
|---|---|---|---|
| **iDash3** | 0.2.0-dev | [`dist/`](iDash3-0.2.0-dev-debug-QA.apk?raw=true) | Launcher |
| **iDash3 Debloat** | 1.0 | [`dist/`](iDash3-Debloat-1.0.apk?raw=true) | Pasang → jalankan → buang |
| **Waze** | **5.9.5.4** (Android 8.0+, arm-v7a) — versi terkini perlukan Android 10 | [APKMirror (tandatangan Waze disahkan)](https://www.apkmirror.com/apk/waze/waze-gps-maps-traffic-alerts-live-navigation/waze-navigation-live-traffic-5-9-5-4-release/) | Pilih varian `arm64-v8a + armeabi-v7a`. Matikan kemas kini auto untuk Waze |
| **NewPipe** (YouTube tanpa iklan, UI sentuh) | 0.29.x (Android 6+) | App **F-Droid** pada head unit → cari "NewPipe" ([halaman](https://f-droid.org/packages/org.schabi.newpipe/)) | Tiada Play services, ada build 32-bit |
| SmartTube (UI TV, D-pad) | ≥ 30.56 sahaja | [GitHub rasmi](https://github.com/yuliskov/SmartTube/releases) | Binaan lama pernah dijangkiti malware (Dis 2025); jangan guna binaan lama/laman APK lain |
| **Chrome** | **138** (sudah dipasang) | — | Versi terakhir untuk Android 8/9. Jangan buang. Tetapkan sebagai **WebView implementation** |
| Google Maps | (sedia ada) | — | Sangat berat untuk 1 GB RAM. Boleh dilumpuhkan dengan iDash3 Debloat jika guna Waze |

## Turutan yang dicadangkan
1. Pasang **iDash3 Debloat** → *Lumpuhkan yang ditanda* → *Optimumkan* → *Tetapan WebView → Chrome* → mulakan semula head unit → *Selesai — buang app ini*.
2. Pasang Waze 5.9.5.4 dan NewPipe; uji.
3. Jika puas hati, buka Debloat semula (atau guna skrip ADB) dan lumpuhkan pilihan Google Maps / YouTube.

Semua tindakan debloat **boleh diundur** (*Pulihkan semua* atau `tools/headunit/debloat-adb.sh restore`).
