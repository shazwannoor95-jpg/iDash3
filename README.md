# iDash3

Launcher Android "Liquid Glass" premium untuk head unit Proton Saga VVT 2016 (Android 8.1 / API 27, ARM32, ±1 GB RAM, 1024×600 dan 800×480, landscape).

iDash3 ialah projek **standalone** (`com.shazwan.idash3`): ia boleh dipasang bersama iDash/iDash2 dan tidak mengemas kini kedua-duanya. Ia berasal daripada kod iDash 0.1.8 (Fasa 6C) yang distabilkan dalam iDash2, tanpa aset pihak ketiga yang lesennya tidak jelas.

- Java Views asli, minSdk 26, tiada dependency runtime, tiada pustaka native, tiada Google Play Services.
- Dibina dengan JDK 17+, Android SDK 35, Gradle 8.9 (wrapper): `./gradlew :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest`
- Dokumen: [`docs/`](docs) — inventori sumber, audit, reka bentuk, pengesahan.
- Status ujian yang sebenar: [`docs/VALIDATION.md`](docs/VALIDATION.md). Prestasi pada head unit sebenar **belum diuji**.
