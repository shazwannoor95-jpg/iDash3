package com.shazwan.idash3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Starts the Android-Auto-style layout using the system's own split-screen: launch the first app,
 * dock it with the split-screen global action, then launch the second app into the other side.
 * Nothing is embedded; both apps are ordinary resizable activities managed by Android.
 */
final class DriveController {
    static final String PREF_MAP = "drive_map", PREF_MEDIA = "drive_media", PREF_MAP_LEFT = "drive_map_left", PREF_BAR = "drive_bar", PREF_ADJACENT = "drive_adjacent";
    static final long DOCK_DELAY_MS = 2300, SECOND_DELAY_MS = 1700, BAR_DELAY_MS = 900;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static volatile boolean active;

    private DriveController() {}

    static boolean isActive() { return active; }

    static boolean overlayAllowed(Context c) { return Settings.canDrawOverlays(c); }

    static boolean launch(Context c, String component, boolean adjacent) {
        ComponentName name = component == null ? null : ComponentName.unflattenFromString(component);
        if (name == null) return false;
        Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(name)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | (adjacent ? Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT : 0));
        try { c.startActivity(i); return true; } catch (RuntimeException unavailable) { return false; }
    }

    /** @return null when started, otherwise a short Malay reason the user can act on. */
    static String start(Context app, String map, String media, boolean mapLeft, boolean bar, boolean adjacent) {
        if (map == null || media == null) return "Pilih app peta dan app media dahulu.";
        if (!SplitService.isRunning()) return "Hidupkan perkhidmatan aksesibiliti iDash3 dahulu (untuk butang split-screen).";
        final Context c = app.getApplicationContext();
        String[] order = DrivePlan.launchOrder(map, media, mapLeft);
        active = true;
        if (!launch(c, order[0], false)) { active = false; return "App pertama tidak dapat dibuka."; }
        MAIN.postDelayed(() -> {
            if (!SplitService.toggleSplit()) { active = false; Toast.makeText(c, "Split-screen gagal: perkhidmatan aksesibiliti terhenti.", Toast.LENGTH_LONG).show(); return; }
            MAIN.postDelayed(() -> {
                if (!launch(c, order[1], adjacent)) Toast.makeText(c, "App kedua tidak dapat dibuka.", Toast.LENGTH_LONG).show();
                if (bar && overlayAllowed(c)) MAIN.postDelayed(() -> DriveBarService.start(c), BAR_DELAY_MS);
            }, SECOND_DELAY_MS);
        }, DOCK_DELAY_MS);
        return null;
    }

    /** Leaves split-screen (if we started it), removes the floating bar and returns to the launcher. */
    static void stop(Context app) {
        Context c = app.getApplicationContext();
        DriveBarService.stop(c);
        if (active) SplitService.toggleSplit();
        active = false;
        MAIN.postDelayed(() -> c.startActivity(new Intent(c, LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)), 600);
    }
}
