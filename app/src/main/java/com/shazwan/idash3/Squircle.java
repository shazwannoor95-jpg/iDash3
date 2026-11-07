package com.shazwan.idash3;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/**
 * Continuous-corner "squircle" (superellipse, n = 5) used for app icons and dock tiles, plus a
 * drawable that fills it with a vertical gradient, inner highlight and hairline rim.
 */
final class Squircle {
    private Squircle() {}

    /** Superellipse inscribed in r; sampled densely so it stays smooth at any icon size. */
    static Path path(RectF r, Path out) {
        if (out == null) out = new Path(); else out.reset();
        float cx = r.centerX(), cy = r.centerY(), a = r.width() / 2f, b = r.height() / 2f;
        final int steps = 96; final double n = 5.0;
        for (int i = 0; i <= steps; i++) {
            double t = 2 * Math.PI * i / steps, c = Math.cos(t), s = Math.sin(t);
            float x = cx + (float) (a * Math.signum(c) * Math.pow(Math.abs(c), 2 / n));
            float y = cy + (float) (b * Math.signum(s) * Math.pow(Math.abs(s), 2 / n));
            if (i == 0) out.moveTo(x, y); else out.lineTo(x, y);
        }
        out.close();
        return out;
    }

    /** Gradient squircle tile (dock favourites, Settings, Apps). */
    static final class Tile extends Drawable {
        private final int top, bottom; private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path shape = new Path(); private final RectF box = new RectF(); private final float density;
        private Shader fill, gloss, rim;
        Tile(int top, int bottom, float density) { this.top = top; this.bottom = bottom; this.density = density; }
        @Override protected void onBoundsChange(Rect b) {
            box.set(b); box.inset(density * .5f, density * .5f); path(box, shape);
            fill = new LinearGradient(0, box.top, 0, box.bottom, top, bottom, Shader.TileMode.CLAMP);
            gloss = new LinearGradient(0, box.top, 0, box.top + box.height() * .5f, 0x38ffffff, 0x00ffffff, Shader.TileMode.CLAMP);
            rim = new LinearGradient(box.left, box.top, box.right, box.bottom, new int[]{0x9affffff, 0x1affffff, 0x40ffffff}, new float[]{0, .55f, 1}, Shader.TileMode.CLAMP);
        }
        @Override public void draw(Canvas c) {
            if (fill == null) return;
            p.setStyle(Paint.Style.FILL); p.setShader(fill); c.drawPath(shape, p);
            p.setShader(gloss); c.drawPath(shape, p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(1f, density * .7f)); p.setShader(rim); c.drawPath(shape, p);
            p.setShader(null);
        }
        @Override public void getOutline(Outline o) { o.setConvexPath(shape); o.setAlpha(.6f); }
        @Override public void setAlpha(int a) { p.setAlpha(a); }
        @Override public void setColorFilter(ColorFilter f) { p.setColorFilter(f); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
