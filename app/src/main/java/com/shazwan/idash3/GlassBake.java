package com.shazwan.idash3;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * "Baked" Liquid Glass for API 27: per-panel refraction computed ONCE (worker thread) instead of a
 * per-frame shader. On a launcher the only thing behind a panel is the static wallpaper, so a baked
 * result is visually identical to live refraction while costing one bitmap draw per frame.
 *
 * Per pixel: signed distance to the rounded rectangle gives a bevel profile; inside the edge band
 * the wallpaper is sampled displaced along the surface normal (lens bending, magnified toward the
 * rim), with vibrancy (saturation/brightness lift) and a light-direction bevel shade.
 */
final class GlassBake {
    static final int BUDGET_BYTES = 6 * 1024 * 1024;
    /** Results keyed by geometry + backdrop identity, so re-rendered panels at the same place reuse them. */
    static final android.util.LruCache<Integer, Bitmap> cache = new android.util.LruCache<Integer, Bitmap>(BUDGET_BYTES) {
        @Override protected int sizeOf(Integer k, Bitmap v) { return v.getByteCount(); }
    };
    private static final java.util.HashSet<Integer> pending = new java.util.HashSet<>();
    private static final ExecutorService worker = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "glass-bake"); t.setPriority(Thread.MIN_PRIORITY); return t; });
    private static final Handler main = new Handler(Looper.getMainLooper());

    interface Done { void baked(Bitmap result, int key); }

    private GlassBake() {}

    /** Main thread only. Returns true if a bake for this key was queued (false if already pending). */
    static boolean begin(int key) { return pending.add(key); }

    /**
     * @param src blurred backdrop (frame) pixels, w×h
     * @param map screen->src transform: srcX = (screenX - ox) / scale
     * @param left,top panel position on screen (px), pw/ph panel size (px), radius px, density
     * @param outW,outH output size (panel size scaled down to fit the budget)
     */
    static void request(final Bitmap src, final float scale, final float ox, final float oy, final float left, final float top,
                        final float pw, final float ph, final float radius, final float density, final float depth,
                        final int outW, final int outH, final int key, final Done done) {
        if (src == null || outW < 2 || outH < 2) return;
        worker.execute(() -> {
            Bitmap out = null;
            try { out = bake(src, scale, ox, oy, left, top, pw, ph, radius, density, depth, outW, outH); }
            catch (OutOfMemoryError | RuntimeException ignored) { /* stays on the frost fallback */ }
            final Bitmap result = out;
            main.post(() -> { pending.remove(key); if (result != null) cache.put(key, result); done.baked(result, key); });
        });
    }

    static Bitmap bake(Bitmap src, float scale, float ox, float oy, float left, float top, float pw, float ph,
                       float radius, float density, float depth, int outW, int outH) {
        if (src.isRecycled()) return null;
        int sw = src.getWidth(), sh = src.getHeight();
        int[] in = new int[sw * sh];
        src.getPixels(in, 0, sw, 0, 0, sw, sh);
        int[] px = new int[outW * outH];
        float sx = pw / outW, sy = ph / outH;          // output pixel -> panel px
        float hw = pw / 2f, hh = ph / 2f, r = Math.min(radius, Math.min(hw, hh));
        float band = Math.min(Math.min(hw, hh) * .9f, density * (12f + 14f * depth)); // refraction band
        float bend = density * (6f + 10f * depth);                                    // max displacement
        for (int y = 0; y < outH; y++) {
            float py = (y + .5f) * sy;
            for (int x = 0; x < outW; x++) {
                float pxl = (x + .5f) * sx;
                // Rounded-rect SDF (negative inside) and outward normal.
                float qx = Math.abs(pxl - hw) - (hw - r), qy = Math.abs(py - hh) - (hh - r);
                float nx, ny, d;
                if (qx > 0 && qy > 0) { float l = (float) Math.sqrt(qx * qx + qy * qy); d = l - r; nx = qx / Math.max(l, 1e-3f); ny = qy / Math.max(l, 1e-3f); }
                else if (qx > qy) { d = qx - r; nx = 1; ny = 0; }
                else { d = qy - r; nx = 0; ny = 1; }
                nx *= Math.signum(pxl - hw); ny *= Math.signum(py - hh);
                float inside = -d;                                    // distance from rim, px
                float t = inside >= band ? 0f : 1f - Math.max(0f, inside) / band; // 1 at rim, 0 inside
                float t2 = t * t;
                // Lens: content near the rim is pulled from further outward (magnifying bevel).
                float dx = nx * bend * t2 * 1.6f, dy = ny * bend * t2 * 1.6f;
                float wx = left + pxl + dx, wy = top + py + dy;
                int c = sample(in, sw, sh, (wx - ox) / scale, (wy - oy) / scale);
                int rr = (c >> 16) & 255, gg = (c >> 8) & 255, bb = c & 255;
                // Vibrancy: saturate and lift (iOS glass reads brighter/more colourful than the backdrop).
                float lum = .299f * rr + .587f * gg + .114f * bb;
                float sat = 1.35f;
                float fr = lum + (rr - lum) * sat, fg = lum + (gg - lum) * sat, fb = lum + (bb - lum) * sat;
                float lift = 14f;
                // Bevel shade: light from top-left; facing edges brighten, far edges darken.
                float facing = -(nx * .55f + ny * .83f);              // >0 when normal points up/left
                float shade = t2 * (facing > 0 ? 70f * facing : 34f * facing);
                // Thin bright rim refraction line right at the edge.
                float rimLine = t > .86f ? (t - .86f) / .14f * 40f : 0f;
                fr += lift + shade + rimLine; fg += lift + shade + rimLine; fb += lift + shade + rimLine;
                px[y * outW + x] = 0xff000000 | (clamp(fr) << 16) | (clamp(fg) << 8) | clamp(fb);
            }
        }
        Bitmap out = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888);
        out.setPixels(px, 0, outW, 0, 0, outW, outH);
        return out;
    }

    private static int clamp(float v) { return v < 0 ? 0 : v > 255 ? 255 : (int) v; }

    /** Bilinear sample with edge clamp. */
    static int sample(int[] in, int w, int h, float x, float y) {
        x = Math.max(0, Math.min(w - 1.001f, x - .5f)); y = Math.max(0, Math.min(h - 1.001f, y - .5f));
        int x0 = (int) x, y0 = (int) y, x1 = Math.min(w - 1, x0 + 1), y1 = Math.min(h - 1, y0 + 1);
        float fx = x - x0, fy = y - y0;
        int a = in[y0 * w + x0], b = in[y0 * w + x1], c = in[y1 * w + x0], d = in[y1 * w + x1];
        int r = lerp2((a >> 16) & 255, (b >> 16) & 255, (c >> 16) & 255, (d >> 16) & 255, fx, fy);
        int g = lerp2((a >> 8) & 255, (b >> 8) & 255, (c >> 8) & 255, (d >> 8) & 255, fx, fy);
        int bl = lerp2(a & 255, b & 255, c & 255, d & 255, fx, fy);
        return 0xff000000 | (r << 16) | (g << 8) | bl;
    }

    private static int lerp2(int a, int b, int c, int d, float fx, float fy) {
        float top = a + (b - a) * fx, bot = c + (d - c) * fx;
        return Math.round(top + (bot - top) * fy);
    }
}
