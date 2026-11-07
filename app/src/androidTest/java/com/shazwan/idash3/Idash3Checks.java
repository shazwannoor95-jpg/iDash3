package com.shazwan.idash3;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import java.util.HashMap;
import java.util.Map;

/** iDash3 Fasa 3/3B/4 checks on the real Activity: home grid, folders + liquid morph, perf mode, layered icons. */
public class Idash3Checks extends Instrumentation {
    int count; LauncherActivity a; SharedPreferences prefs; Map<String, ?> saved;
    void check(boolean ok, String why) { if (!ok) throw new AssertionError(why); count++; }

    View find(View v, String prefix) {
        CharSequence d = v.getContentDescription();
        if (d != null && d.toString().startsWith(prefix)) return v;
        if (v instanceof ViewGroup) { ViewGroup g = (ViewGroup) v; for (int i = 0; i < g.getChildCount(); i++) { View f = find(g.getChildAt(i), prefix); if (f != null) return f; } }
        return null;
    }
    int countType(View v, Class<?> type) {
        int n = type.isInstance(v) ? 1 : 0;
        if (v instanceof ViewGroup) { ViewGroup g = (ViewGroup) v; for (int i = 0; i < g.getChildCount(); i++) n += countType(g.getChildAt(i), type); }
        return n;
    }
    int minTouch(View v, int[] min) {
        CharSequence d = v.getContentDescription();
        if (d != null && d.toString().startsWith("Open ") && v.isShown()) { min[0] = Math.min(min[0], Math.min(v.getWidth(), v.getHeight())); }
        if (v instanceof ViewGroup) { ViewGroup g = (ViewGroup) v; for (int i = 0; i < g.getChildCount(); i++) minTouch(g.getChildAt(i), min); }
        return min[0];
    }
    void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }

    @Override public void onCreate(Bundle b) { super.onCreate(b); start(); }

    @SuppressWarnings("unchecked")
    @Override public void onStart() {
        Bundle r = new Bundle();
        prefs = getTargetContext().getSharedPreferences("idash", 0);
        saved = new HashMap<>(prefs.getAll());
        try {
            prefs.edit().remove(HomeGrid.PREF).putInt("panel_mode", 0).apply();
            a = (LauncherActivity) startActivitySync(new Intent(getTargetContext(), LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            waitForIdleSync();
            long end = SystemClock.elapsedRealtime() + 20000;
            while (!a.loaded && SystemClock.elapsedRealtime() < end) sleep(100);
            sleep(600); waitForIdleSync();
            runOnMainSync(() -> {
                check(a.loaded && a.apps.size() > 0, "apps discovered");
                check(a.homeGrid != null && find(a.stage, "Home apps") != null, "home grid present when panel hidden");
                int[] min = {Integer.MAX_VALUE};
                check(minTouch(a.homeGrid, min) >= a.dp(48), "home cells >= 48dp touch target, got " + min[0]);
                String layout = prefs.getString(HomeGrid.PREF, "");
                check(!layout.isEmpty() && layout.split("\n").length == a.apps.size(), "layout persisted with every installed app");
            });
            // Folder: build one from the first two apps, re-render, open (liquid morph) and close with Back.
            runOnMainSync(() -> {
                HomeLayout l = HomeLayout.parse(prefs.getString(HomeGrid.PREF, ""));
                if (l.items.size() >= 2) { l.addToFolder(0, "Ujian"); l.addToFolder(1, "Ujian"); }
                prefs.edit().putString(HomeGrid.PREF, l.serialize()).apply();
                a.homePage = 0; a.render();
            });
            sleep(800); waitForIdleSync();
            final boolean[] hasFolder = {false};
            runOnMainSync(() -> { View f = find(a.stage, "Folder Ujian"); hasFolder[0] = f != null; if (f != null) f.performClick(); });
            if (a.apps.size() >= 2) {
                check(hasFolder[0], "folder cell rendered");
                sleep(80);
                runOnMainSync(() -> check(a.homeGrid.folderOpen(), "folder opens"));
                sleep(1500); waitForIdleSync();
                runOnMainSync(() -> {
                    check(countType(a.stage, MorphOverlay.class) == 0, "morph overlay removes itself at rest");
                    a.onBackPressed();
                    check(!a.homeGrid.folderOpen(), "Back closes the folder before leaving home");
                });
                sleep(1500); waitForIdleSync();
                runOnMainSync(() -> check(countType(a.stage, MorphOverlay.class) == 0, "closing morph removed"));
            }
            // Performance mode switches are observable.
            runOnMainSync(() -> {
                GlassPerf.setMode(a, GlassPerf.LOW); check(GlassPerf.low() && GlassPerf.capQuality(IosStyle.HIGH) == IosStyle.LOW, "low mode caps glass quality");
                GlassPerf.setMode(a, GlassPerf.FULL); check(!GlassPerf.low() && GlassPerf.capQuality(IosStyle.HIGH) == IosStyle.HIGH, "full mode keeps quality");
                GlassPerf.setMode(a, GlassPerf.AUTO);
            });
            // Layered icons: every variant renders an opaque centre and transparent corners.
            runOnMainSync(() -> {
                android.graphics.drawable.Drawable d = a.getPackageManager().getDefaultActivityIcon();
                for (int v = 0; v < IconStyler.NAMES.length; v++) {
                    Bitmap b = IconStyler.render(d, a.dp(56), v, 0xff6f8fc8, a.getResources().getDisplayMetrics().density, v % 2 == 0);
                    check(b.getWidth() == a.dp(56), "icon size v" + v);
                    check(Color.alpha(b.getPixel(b.getWidth() / 2, b.getHeight() / 2)) > 0, "icon centre drawn v" + v);
                    check(Color.alpha(b.getPixel(0, 0)) == 0, "icon corner transparent v" + v);
                    b.recycle();
                }
            });
            // Glass reveal settles after the enter transition.
            sleep(1200);
            runOnMainSync(() -> {
                GlassBackdrop bd = IosStyle.backdrops.get(a); int settled = 0, live = 0;
                if (bd != null) for (java.lang.ref.WeakReference<GlassSurface> w : bd.surfaces) { GlassSurface g = w.get(); if (g == null || !(g.getCallback() instanceof View) || !((View) g.getCallback()).isAttachedToWindow()) continue; live++; if (g.reveal() == 1f) settled++; }
                check(live > 0 && settled == live, "all attached glass surfaces settled (" + settled + "/" + live + ")");
            });
            android.os.Debug.MemoryInfo mi = new android.os.Debug.MemoryInfo(); android.os.Debug.getMemoryInfo(mi);
            r.putString("stream", "PASS: " + count + " iDash3 checks; pss=" + mi.getTotalPss() + "KB perf=" + GlassPerf.describe() + "\n");
            restore();
            finish(Activity.RESULT_OK, r);
        } catch (Throwable e) {
            restore();
            r.putString("stream", "FAIL after " + count + ": " + e + "\n");
            finish(Activity.RESULT_CANCELED, r);
        }
    }

    @SuppressWarnings("unchecked")
    void restore() {
        SharedPreferences.Editor e = prefs.edit().clear();
        for (Map.Entry<String, ?> en : saved.entrySet()) {
            Object v = en.getValue();
            if (v instanceof Boolean) e.putBoolean(en.getKey(), (Boolean) v); else if (v instanceof Integer) e.putInt(en.getKey(), (Integer) v);
            else if (v instanceof Long) e.putLong(en.getKey(), (Long) v); else if (v instanceof Float) e.putFloat(en.getKey(), (Float) v);
            else if (v instanceof String) e.putString(en.getKey(), (String) v); else if (v instanceof java.util.Set) e.putStringSet(en.getKey(), (java.util.Set<String>) v);
        }
        e.commit();
        if (a != null) runOnMainSync(() -> a.finish());
    }
}
