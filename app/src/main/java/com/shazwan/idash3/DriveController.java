package com.shazwan.idash3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    static final String PREF_LAYOUT = "drive_layout", PREF_DRIVER_LEFT = "drive_driver_left";
    static final long DOCK_DELAY_MS = 2300, SECOND_DELAY_MS = 1700, BAR_DELAY_MS = 900;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    /** Every delayed step of a Drive start, so leaving Drive can cancel the rest of the sequence. */
    private static final java.util.ArrayList<Runnable> PENDING = new java.util.ArrayList<>();
    private static volatile boolean active, fullscreen;
    private static String lastMap, lastMedia;
    private static boolean lastMapLeft, lastBar;

    private DriveController() {}

    static boolean isActive() { return active; }

    private static void post(long delay, Runnable task) { PENDING.add(task); MAIN.postDelayed(task, delay); }

    /** Drops the pending dock/second-app/bar steps; a no-op when nothing is queued. */
    private static void cancelPending() {
        for (int i = 0; i < PENDING.size(); i++) MAIN.removeCallbacks(PENDING.get(i));
        PENDING.clear();
    }

    static boolean overlayAllowed(Context c) { return Settings.canDrawOverlays(c); }

    /** Malaysia drives on the right, so "closest to the driver" is the right-hand slot by default. */
    static boolean driverOnLeft(SharedPreferences p) { return p.getBoolean(PREF_DRIVER_LEFT, false); }

    /** Android Auto "Change Layout", migrated from the old swap-button preference. */
    static int layout(SharedPreferences p) {
        if (p.contains(PREF_LAYOUT)) return p.getInt(PREF_LAYOUT, DrivePlan.LAYOUT_NAV_CLOSER);
        if (p.contains(PREF_MAP_LEFT)) return DrivePlan.layoutFor(p.getBoolean(PREF_MAP_LEFT, true), driverOnLeft(p));
        return DrivePlan.LAYOUT_NAV_CLOSER;
    }

    static boolean mapLeft(SharedPreferences p) { return DrivePlan.mapLeft(layout(p), driverOnLeft(p)); }

    static boolean isFullscreen() { return fullscreen; }

    /** Android Auto's bottom-left square: the focused app takes the whole screen, tap again to re-dock. */
    static void toggleFullscreen(Context app) {
        Context c = app.getApplicationContext();
        if (!SplitService.isRunning() || lastMap == null || lastMedia == null) return;
        if (!fullscreen) { if (SplitService.toggleSplit()) fullscreen = true; }
        else if (start(c, lastMap, lastMedia, lastMapLeft, lastBar, false) == null) fullscreen = false;
    }

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
        cancelPending();
        lastMap = map; lastMedia = media; lastMapLeft = mapLeft; lastBar = bar; fullscreen = false;
        String[] order = DrivePlan.launchOrder(map, media, mapLeft);
        active = true;
        if (!launch(c, order[0], false)) { active = false; return "App pertama tidak dapat dibuka."; }
        post(DOCK_DELAY_MS, () -> {
            if (!SplitService.toggleSplit()) { active = false; Toast.makeText(c, "Split-screen gagal: perkhidmatan aksesibiliti terhenti.", Toast.LENGTH_LONG).show(); return; }
            post(SECOND_DELAY_MS, () -> {
                if (!launch(c, order[1], adjacent)) Toast.makeText(c, "App kedua tidak dapat dibuka.", Toast.LENGTH_LONG).show();
                if (bar && overlayAllowed(c)) post(BAR_DELAY_MS, () -> DriveBarService.start(c));
            });
        });
        return null;
    }

    /** Leaves split-screen (if we started it), removes the floating bar and returns to the launcher. */
    static void stop(Context app) {
        Context c = app.getApplicationContext();
        cancelPending();
        DriveBarService.stop(c);
        if (active) SplitService.toggleSplit();
        active = false; fullscreen = false;
        MAIN.postDelayed(() -> c.startActivity(new Intent(c, LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)), 600);
    }
}
