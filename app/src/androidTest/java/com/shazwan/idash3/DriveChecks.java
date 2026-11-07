package com.shazwan.idash3;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Drive (split-screen) on a real Android 8.1 system: enables the accessibility service with shell
 * rights, launches two apps through DriveController and records the activity stacks Android reports.
 * Whether the system really docked is REPORTED (stacks text), not assumed.
 */
public class DriveChecks extends Instrumentation {
    int count;
    void check(boolean ok, String why) { if (!ok) throw new AssertionError(why); count++; }

    String shell(String cmd) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (ParcelFileDescriptor fd = getUiAutomation().executeShellCommand(cmd); BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(fd.getFileDescriptor())))) {
            String line; while ((line = r.readLine()) != null && sb.length() < 6000) sb.append(line).append('\n');
        }
        return sb.toString();
    }

    View find(View v, String d) {
        if (d.equals(String.valueOf(v.getContentDescription()))) return v;
        if (v instanceof ViewGroup) { ViewGroup g = (ViewGroup) v; for (int i = 0; i < g.getChildCount(); i++) { View f = find(g.getChildAt(i), d); if (f != null) return f; } }
        return null;
    }

    @Override public void onCreate(Bundle b) { super.onCreate(b); start(); }

    @Override public void onStart() {
        Bundle r = new Bundle();
        try {
            android.content.Context c = getTargetContext();
            // 1. Drive page builds inside the real launcher.
            c.getSharedPreferences("idash", 0).edit().remove(DriveController.PREF_MAP).remove(DriveController.PREF_MEDIA).commit();
            final LauncherActivity a = (LauncherActivity) startActivitySync(new Intent(c, LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            long end = SystemClock.elapsedRealtime() + 20000; while (!a.loaded && SystemClock.elapsedRealtime() < end) Thread.sleep(100);
            runOnMainSync(() -> { a.page = "drive"; a.render(); });
            waitForIdleSync();
            final boolean[] seen = new boolean[2];
            runOnMainSync(() -> { seen[0] = find(a.stage, "Drive page") != null; seen[1] = find(a.stage, "Start Drive") != null; });
            check(seen[0] && seen[1], "Drive page and Start button render");
            // 2. Accessibility service can be enabled and connects.
            shell("settings put secure enabled_accessibility_services com.shazwan.idash3/com.shazwan.idash3.SplitService");
            shell("settings put secure accessibility_enabled 1");
            end = SystemClock.elapsedRealtime() + 25000; while (!SplitService.isRunning() && SystemClock.elapsedRealtime() < end) Thread.sleep(250);
            check(SplitService.isRunning(), "SplitService connects after being enabled");
            // 3. Two launchable apps and the split sequence.
            PackageManager pm = c.getPackageManager(); ArrayList<String> comps = new ArrayList<>();
            for (ResolveInfo ri : pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0))
                if (!ri.activityInfo.packageName.equals(c.getPackageName())) comps.add(new android.content.ComponentName(ri.activityInfo.packageName, ri.activityInfo.name).flattenToString());
            String map = null, media = null;
            for (String s : comps) { if (s.startsWith("com.android.settings/")) map = s; else if (s.startsWith("com.android.contacts/") || s.startsWith("com.android.calendar/") || s.startsWith("com.android.deskclock/")) media = s; }
            if (map == null || media == null) { r.putString("stream", "PASS: " + count + " checks; split sequence NOT RUN (no two suitable apps: " + comps + ")\n"); finish(Activity.RESULT_OK, r); return; }
            String before = shell("am stack list");
            String problem = DriveController.start(c, map, media, true, false, false);
            check(problem == null, "DriveController.start accepted: " + problem);
            Thread.sleep(DriveController.DOCK_DELAY_MS + DriveController.SECOND_DELAY_MS + 4500);
            String after = shell("am stack list");
            boolean docked = after.contains("Stack id=3") || after.toLowerCase().contains("docked") || after.contains("windowingMode=3");
            Log.i("DriveChecks", "STACKS BEFORE:\n" + before + "\nSTACKS AFTER:\n" + after);
            r.putString("stream", "PASS: " + count + " checks; split docked=" + docked + "; map=" + map + " media=" + media + "\nSTACKS_AFTER:\n" + after.replace('\n', '|') + "\n");
            DriveController.stop(c);
            finish(Activity.RESULT_OK, r);
        } catch (Throwable e) {
            r.putString("stream", "FAIL after " + count + ": " + e + "\n"); finish(Activity.RESULT_CANCELED, r);
        }
    }
}
