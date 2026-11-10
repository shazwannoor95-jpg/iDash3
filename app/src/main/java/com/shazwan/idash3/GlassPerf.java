package com.shazwan.idash3;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Low-performance mode. Decisions rest on measurable signals only:
 *  - user choice (pref "perf_mode": 0 auto, 1 always low, 2 always full);
 *  - ActivityManager.isLowRamDevice / small heap (read once);
 *  - frame time: p90 of real frames (FrameMetrics) over windows of 90 frames.
 * No per-frame polling is added; the watcher only receives frames the app already draws.
 */
final class GlassPerf {
    static final int AUTO = 0, LOW = 1, FULL = 2;
    static final long JANK_NS = 24_000_000L, GOOD_NS = 14_000_000L;
    static final int WINDOW = 90, DEGRADE_AFTER = 3, RECOVER_AFTER = 6;

    private static volatile int generation;
    private static volatile boolean autoLow, memoryLow;
    private static volatile int mode = AUTO;
    private static boolean loaded;

    private GlassPerf() {}

    static void load(Context c) {
        if (loaded) return;
        loaded = true;
        try {
            SharedPreferences p = c.getSharedPreferences("idash", 0);
            mode = GlassAppearance.clamp(GlassAppearance.integer(p, "perf_mode", AUTO), 0, 2);
            android.app.ActivityManager am = (android.app.ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
            memoryLow = (am != null && am.isLowRamDevice()) || Runtime.getRuntime().maxMemory() < 96L * 1024 * 1024;
        } catch (RuntimeException ignored) { /* keep defaults */ }
    }

    static int generation() { return generation; }
    static int mode() { return mode; }
    static boolean isMemoryLow() { return memoryLow; }

    static void setMode(Context c, int next) {
        load(c);
        mode = GlassAppearance.clamp(next, 0, 2);
        autoLow = false;
        c.getSharedPreferences("idash", 0).edit().putInt("perf_mode", mode).apply();
        generation++;
    }

    /** Heavy effects (lensing, dynamic specular, morphing, layered icon shading) are off when true. */
    static boolean low() { return mode == LOW || (mode == AUTO && (autoLow || memoryLow)); }

    static int capQuality(int requested) { return low() ? Math.min(requested, IosStyle.LOW) : requested; }

    static void reportDegrade(boolean degraded) {
        if (mode != AUTO || autoLow == degraded) return;
        autoLow = degraded;
        generation++;
    }

    static String describe() {
        return mode == LOW ? "Rendah (manual)" : mode == FULL ? "Penuh (manual)" : low() ? (memoryLow ? "Auto: rendah (memori)" : "Auto: rendah (bingkai perlahan)") : "Auto: penuh";
    }

    /** 90th percentile of n durations (nanoseconds). Mutates the array order. */
    static long p90(long[] ns, int n) {
        if (n <= 0) return 0;
        java.util.Arrays.sort(ns, 0, n);
        return ns[Math.min(n - 1, (int) Math.ceil(n * .9) - 1)];
    }

    /** Hysteresis state machine, pure so it can be unit-tested. */
    static final class Policy {
        private int bad, good;
        private boolean degraded;
        boolean degraded() { return degraded; }
        /** Feed one window's p90. Returns true if the degraded flag changed. */
        boolean update(long p90Ns) {
            if (p90Ns > JANK_NS) { bad++; good = 0; }
            else if (p90Ns < GOOD_NS) { good++; bad = 0; }
            else { bad = 0; good = 0; }
            boolean before = degraded;
            if (!degraded && bad >= DEGRADE_AFTER) degraded = true;
            else if (degraded && good >= RECOVER_AFTER) degraded = false;
            return before != degraded;
        }
    }

    /** Collects total frame durations from the window and feeds the policy. */
    static final class Watcher implements android.view.Window.OnFrameMetricsAvailableListener {
        private final long[] buf = new long[WINDOW];
        private int n, skip = 30;
        private final Policy policy = new Policy();
        @Override public void onFrameMetricsAvailable(android.view.Window w, android.view.FrameMetrics m, int dropped) {
            if (skip > 0) { skip--; return; }
            if (m.getMetric(android.view.FrameMetrics.FIRST_DRAW_FRAME) == 1) return;
            buf[n++] = m.getMetric(android.view.FrameMetrics.TOTAL_DURATION);
            if (n == WINDOW) { long p = p90(buf, n); n = 0; if (policy.update(p)) reportDegrade(policy.degraded()); }
        }
        void reset() { n = 0; skip = 30; }
    }
}
