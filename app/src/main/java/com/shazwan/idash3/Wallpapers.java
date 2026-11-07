package com.shazwan.idash3;

import android.content.res.AssetManager;
import android.graphics.*;
import java.io.IOException;

/**
 * Original, procedurally generated wallpapers. No third-party image is bundled; each id ("gen:N")
 * is rebuilt deterministically at the requested size, so nothing is stored in the APK.
 */
final class Wallpapers {
    static final String PREFIX = "gen:";
    static final String DEFAULT = PREFIX + "0";
    private static final String[] NAMES = {"Lautan", "Senja", "Hutan", "Aurora", "Pasir", "Malam", "Sakura", "Karbon"};
    // base top, base bottom, three blob colours (ARGB) per wallpaper.
    private static final int[][] PALETTE = {
        {0xff0b2a4a, 0xff12395f, 0xff2e7fd1, 0xff1b9aaa, 0xff5a4fcf},
        {0xff3a1c3f, 0xff6a2c3e, 0xfff08a4b, 0xffe05a7a, 0xff8f4fd0},
        {0xff0c2a22, 0xff123c2e, 0xff2fae7a, 0xff9acd32, 0xff1b7f8c},
        {0xff0a1630, 0xff102a44, 0xff35d6b0, 0xff4f7cf0, 0xffa24fe0},
        {0xff3a2a1c, 0xff5a4128, 0xffe0b060, 0xffc9803f, 0xff8fa05a},
        {0xff05070f, 0xff0c1224, 0xff2a3c8f, 0xff4a2a7f, 0xff1b5f8f},
        {0xff3a2030, 0xff5a2f48, 0xfff08fb0, 0xffd96fa0, 0xff9f7fd0},
        {0xff111418, 0xff1b2026, 0xff3a4552, 0xff2a6f7a, 0xff4a4f6a},
    };

    static int count() { return NAMES.length; }
    static String id(int index) { return PREFIX + index; }
    static String name(String id) { return NAMES[index(id)]; }

    static int index(String id) {
        if (id == null || !id.startsWith(PREFIX)) return 0;
        try { int n = Integer.parseInt(id.substring(PREFIX.length())); return n >= 0 && n < NAMES.length ? n : 0; }
        catch (NumberFormatException bad) { return 0; }
    }

    static String[] ids() {
        String[] out = new String[NAMES.length];
        for (int i = 0; i < out.length; i++) out[i] = id(i);
        return out;
    }

    /** Kept source-compatible with the former asset decoder; unknown ids fall back to the default. */
    static Bitmap decode(AssetManager unused, String id, int maxEdge) throws IOException {
        int w = Math.max(16, Math.min(maxEdge, 1280)), h = Math.max(9, Math.round(w * 9f / 16f));
        Bitmap out;
        try { out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); }
        catch (OutOfMemoryError oom) { throw new IOException("Wallpaper unavailable"); }
        int[] p = PALETTE[index(id)];
        Canvas c = new Canvas(out);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paint.setShader(new LinearGradient(0, 0, w * .35f, h, p[0], p[1], Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        float[][] blob = {{.18f, .30f, .62f}, {.78f, .22f, .55f}, {.60f, .88f, .70f}};
        int[] ox = {0, 0, 0};
        for (int i = 0; i < 3; i++) {
            float cx = w * blob[i][0], cy = h * blob[i][1], r = Math.max(w, h) * blob[i][2];
            int color = p[2 + i];
            paint.setShader(new RadialGradient(cx, cy, r, new int[]{(0xaa << 24) | (color & 0xffffff), (0x40 << 24) | (color & 0xffffff), color & 0xffffff}, new float[]{0, .5f, 1}, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h, paint);
        }
        paint.setShader(null);
        return out;
    }
}
