package com.shazwan.idash3;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/**
 * Continuous-corner "squircle" (superellipse) used for app icons and dock tiles. The exponent is
 * rounder than the older iOS 18 shape, matching the current iOS icon grid, whose corners sit
 * concentric with the rest of the glass UI.
 * Also provides a drawable that fills it with a lit-from-above gradient, a diagonal sheen, a bright
 * top edge and a darker bottom edge.
 */
final class Squircle {
    /** 2/n exponents: n = 4.4 gives the rounder continuous corner of the current iOS icon grid. */
    private static final double N = 4.4;

    private Squircle() {}

    /** Superellipse inscribed in r; sampled densely so it stays smooth at any icon size. */
    static Path path(RectF r, Path out) {
        if (out == null) out = new Path(); else out.reset();
        float cx = r.centerX(), cy = r.centerY(), a = r.width() / 2f, b = r.height() / 2f;
        final int steps = 96;
        for (int i = 0; i <= steps; i++) {
            double t = 2 * Math.PI * i / steps, c = Math.cos(t), s = Math.sin(t);
            float x = cx + (float) (a * Math.signum(c) * Math.pow(Math.abs(c), 2 / N));
            float y = cy + (float) (b * Math.signum(s) * Math.pow(Math.abs(s), 2 / N));
            if (i == 0) out.moveTo(x, y); else out.lineTo(x, y);
        }
        out.close();
        return out;
    }

    /** Gradient squircle tile (dock favourites, Settings, Apps). */
    static final class Tile extends Drawable {
        private final int top, bottom; private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path shape = new Path(), inner = new Path(); private final RectF box = new RectF(); private final float density;
        private Shader fill, gloss, sheen, rim, edge;
        Tile(int top, int bottom, float density) { this.top = top; this.bottom = bottom; this.density = density; }
        @Override protected void onBoundsChange(Rect b) {
            box.set(b); box.inset(density * .5f, density * .5f); path(box, shape);
            fill = new LinearGradient(0, box.top, 0, box.bottom,
                new int[]{mix(top, 0xffffffff, .16f), top, bottom, mix(bottom, 0xff000000, .14f)},
                new float[]{0f, .38f, .88f, 1f}, Shader.TileMode.CLAMP);
            gloss = new LinearGradient(0, box.top, 0, box.top + box.height() * .5f, 0x40ffffff, 0x00ffffff, Shader.TileMode.CLAMP);
            sheen = new LinearGradient(box.left, box.top, box.right, box.bottom,
                new int[]{0x00ffffff, 0x2bffffff, 0x00ffffff}, new float[]{.12f, .36f, .64f}, Shader.TileMode.CLAMP);
            rim = new LinearGradient(box.left, box.top, box.right, box.bottom, new int[]{0x9affffff, 0x1affffff, 0x40ffffff}, new float[]{0, .55f, 1}, Shader.TileMode.CLAMP);
            float w = Math.max(1f, density * 1.6f); RectF in = new RectF(box); in.inset(w / 2f, w / 2f); path(in, inner);
            edge = new LinearGradient(0, in.top, 0, in.bottom, new int[]{0x66ffffff, 0x00ffffff, 0x3d000000}, new float[]{0f, .55f, 1f}, Shader.TileMode.CLAMP);
        }
        @Override public void draw(Canvas c) {
            if (fill == null) return;
            p.setStyle(Paint.Style.FILL); p.setShader(fill); c.drawPath(shape, p);
            p.setShader(gloss); c.drawPath(shape, p);
            p.setShader(sheen); c.drawPath(shape, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, density * .7f)); p.setShader(rim); c.drawPath(shape, p);
            p.setStrokeWidth(Math.max(1f, density * 1.6f)); p.setShader(edge); c.drawPath(inner, p);
            p.setShader(null);
        }
        @Override public void getOutline(Outline o) {
            if (getBounds().isEmpty()) { o.setEmpty(); return; }
            // API26/27 HWUI can abort while tessellating a dense 28dp path shadow.
            // Simplify only the shadow; the rendered continuous-corner icon stays unchanged.
            if (android.os.Build.VERSION.SDK_INT <= 27 && Math.min(box.width(),box.height()) <= 32*density)
                o.setRoundRect(getBounds(),Math.min(box.width(),box.height())*.22f);
            else o.setConvexPath(shape);
            o.setAlpha(.6f);
        }
        @Override public void setAlpha(int a) { p.setAlpha(a); }
        @Override public void setColorFilter(ColorFilter f) { p.setColorFilter(f); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
        private static int mix(int a, int b, float f) {
            return Color.argb(Math.round(Color.alpha(a) * (1 - f) + Color.alpha(b) * f), Math.round(Color.red(a) * (1 - f) + Color.red(b) * f), Math.round(Color.green(a) * (1 - f) + Color.green(b) * f), Math.round(Color.blue(a) * (1 - f) + Color.blue(b) * f));
        }
    }
}
