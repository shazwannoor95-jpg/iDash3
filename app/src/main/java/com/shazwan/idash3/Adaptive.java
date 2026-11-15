package com.shazwan.idash3;

/** Pure colour maths for adaptive glass (no android.graphics, so it runs in JVM unit tests). */
final class Adaptive {
    private Adaptive() {}

    static float luminance(int rgb) {
        return .2126f * lin(((rgb >> 16) & 255) / 255f) + .7152f * lin(((rgb >> 8) & 255) / 255f) + .0722f * lin((rgb & 255) / 255f);
    }

    private static float lin(float c) { return c <= .03928f ? c / 12.92f : (float) Math.pow((c + .055f) / 1.055f, 2.4f); }

    static float contrast(int a, int b) {
        float la = luminance(a), lb = luminance(b);
        return (Math.max(la, lb) + .05f) / (Math.min(la, lb) + .05f);
    }

    /**
     * Mean colour of a rectangle (in pixel coordinates of the w×h image) using at most grid×grid
     * samples. Rect is clamped to the image; an empty/invalid rect returns fallback.
     */
    static int averageRegion(int[] argb, int w, int h, float l, float t, float r, float b, int grid, int fallback) {
        if (argb == null || w <= 0 || h <= 0 || argb.length < w * h || grid <= 0) return fallback;
        l = Math.max(0, l); t = Math.max(0, t); r = Math.min(w, r); b = Math.min(h, b);
        if (!(r > l) || !(b > t)) return fallback;
        long sr = 0, sg = 0, sb = 0; int n = 0;
        for (int gy = 0; gy < grid; gy++) {
            int y = Math.min(h - 1, (int) (t + (b - t) * (gy + .5f) / grid));
            for (int gx = 0; gx < grid; gx++) {
                int x = Math.min(w - 1, (int) (l + (r - l) * (gx + .5f) / grid));
                int p = argb[y * w + x];
                sr += (p >> 16) & 255; sg += (p >> 8) & 255; sb += p & 255; n++;
            }
        }
        return n == 0 ? fallback : 0xff000000 | ((int) (sr / n) << 16) | ((int) (sg / n) << 8) | (int) (sb / n);
    }

    /** True when dark ink should be used on top of this (already blended) background. */
    static boolean preferDarkInk(int bgRgb, int darkInk, int lightInk) {
        return contrast(darkInk, bgRgb) >= contrast(lightInk, bgRgb);
    }
}
