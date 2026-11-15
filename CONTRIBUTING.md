# Menyumbang kepada iDash3

Terima kasih! iDash3 ditujukan kepada head unit Android **lama** (Android 8.x, 32-bit, ±1 GB RAM). Setiap perubahan mesti kekal ringan dan selamat dipakai semasa memandu.

## Laporkan isu head unit
Buka [Issue](../../issues/new/choose) dan sertakan:
- Model head unit, versi Android **sebenar** (Settings sering menunjuk versi palsu; `adb shell getprop ro.build.version.sdk` memberi API sebenar), resolusi skrin.
- Versi iDash3 (Settings → About) dan mod prestasi.
- Gambar skrin / video, dan jika boleh `adb logcat -d > log.txt` selepas masalah berlaku.

## Persediaan pembangunan
```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
bash tools/ci/guard.sh app/build/outputs/apk/debug/app-debug.apk
```

## Peraturan
1. **Tiada dependency runtime baharu** dan tiada pustaka native tanpa perbincangan dalam issue.
2. **Tiada aset Apple atau aset berlesen tidak jelas** (SF Pro, ikon iOS, MRiOS). Ikon/glyph mesti dilukis sendiri atau berlesen terbuka.
3. **Tiada keystore, kata laluan atau token** dalam Git.
4. Kerja berat di luar UI thread; tiada pengiraan per-bingkai yang mahal. Setiap efek baharu mesti dimatikan dalam mod prestasi Rendah.
5. Sasaran sentuh ≥ 48 dp; elak interaksi kompleks (seret) untuk pemandu.
6. Tambah ujian: logik tulen → ujian unit JVM (`app/src/test`); UI → kelas instrumentation (`app/src/androidTest`, daftar dalam manifest androidTest dan `tools/ci/run-instrumentation.sh`).
7. Kemas kini `CHANGELOG.md` dan, jika perlu, `docs/`.
8. Jangan dakwa ujian/prestasi yang tidak benar-benar dijalankan — bezakan emulator dan head unit sebenar.

## Pull request
- Cawangan daripada `main`, satu topik setiap PR, CI mesti hijau.
- Terangkan apa yang diuji (emulator? head unit apa?).
