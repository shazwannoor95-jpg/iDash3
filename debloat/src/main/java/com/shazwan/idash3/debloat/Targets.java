package com.shazwan.idash3.debloat;

/**
 * Packages found in the owner's Proton Saga head unit dump (TEST9278B, API 27) that are safe to
 * DISABLE (never uninstall). Car functions (radio, Bluetooth, reverse camera, brake check, CAN,
 * car settings, quick boot, stock launcher) and MediaTek op01 plugins are deliberately excluded.
 */
final class Targets {
    /** {package, label, reason, defaultChecked "1"/"0"} */
    static final String[][] LIST = {
        {"com.mediatek.mtklogger", "MTKLogger", "Logger diagnostik MediaTek; boleh berjalan di latar dan memenuhkan storan", "1"},
        {"com.mediatek.mtklogger.proxy", "MTKLoggerProxy", "Proksi untuk MTKLogger", "1"},
        {"com.mediatek.ygps", "YGPS", "Alat ujian GPS kilang", "1"},
        {"com.mediatek.bluetooth.dtt", "BT Tool", "Alat ujian Bluetooth kilang", "1"},
        {"com.xyauto.android.autotest", "AutoTest", "Ujian kilang head unit", "1"},
        {"com.bumblebee.remindeasy", "EasyRemind", "App peringatan tidak dikenali (versi 0.1)", "1"},
        {"com.android.exchange", "Exchange Services", "Emel Exchange — tidak digunakan", "1"},
        {"com.mediatek.calendarimporter", "VCalendar", "Import vCalendar — tidak digunakan", "1"},
        {"com.google.android.partnersetup", "Google Partner Setup", "Persediaan kali pertama", "1"},
        {"com.google.android.apps.work.oobconfig", "Device setup", "Persediaan peranti kerja", "1"},
        {"com.google.android.feedback", "Market Feedback Agent", "Maklum balas Play Store", "1"},
        {"com.android.soundrecorder", "Sound Recorder", "Pilihan: jarang digunakan dalam kereta", "0"},
        {"com.google.android.inputmethod.latin", "Gboard", "Pilihan: guna AOSP Keyboard yang lebih ringan (pastikan AOSP aktif dahulu)", "0"},
        {"com.google.android.youtube", "YouTube", "Pilihan: jika sudah guna NewPipe", "0"},
        {"com.google.android.apps.maps", "Google Maps", "Pilihan: jika sudah guna Waze (Maps sangat berat untuk 1 GB RAM)", "0"},
    };

    /** Folders that diagnostic loggers fill on external storage. */
    static final String[] LOG_DIRS = {"mtklog", "debuglogger", "mtklog_backup"};

    private Targets() {}
}
