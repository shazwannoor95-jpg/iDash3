package com.shazwan.idash3;

import android.graphics.*;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;

/**
 * Layered glass icons rendered once per app (worker thread) into a cached bitmap:
 * layer 1 background tile, layer 2 glyph (with its own soft shadow), layer 3 specular highlight + rim.
 * Uses the app's own icon artwork (AdaptiveIconDrawable layers when available).
 */
final class IconStyler {
    static final int DEFAULT = 0, DARK = 1, CLEAR = 2, TINT_LIGHT = 3, TINT_DARK = 4;
    static final String[] NAMES = {"Default", "Dark", "Clear", "Tinted · terang", "Tinted · gelap"};

    private IconStyler() {}

    static int variant(android.content.SharedPreferences p) { return GlassAppearance.clamp(GlassAppearance.integer(p, "icon_style", DEFAULT), 0, 4); }

    /** @param heavy false in low-performance mode: no glyph shadow and no specular arc. */
    static Bitmap render(Drawable src, int px, int variant, int tint, float density, boolean heavy) { return render(src, px, variant, tint, density, heavy, IosGlyphs.NONE); }

    /** @param ios IosGlyphs id to draw an original iOS-style icon instead of the app's artwork, or NONE. */
    static Bitmap render(Drawable src, int px, int variant, int tint, float density, boolean heavy, int ios) {
        Bitmap out = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(out);
        float pad = Math.max(1f, px * .045f), r = (px - 2 * pad) * .26f;
        RectF tile = new RectF(pad, pad, px - pad, px - pad - (heavy ? density : 0));
        Path shape = Squircle.path(tile, null);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        // Tile shadow (outside the tile, cheap: one offset round rect).
        if (heavy) { p.setColor(0x38000000); Path sh = new Path(shape); sh.offset(0, density * 1.2f); c.drawPath(sh, p); }

        boolean themed = ios != IosGlyphs.NONE;
        boolean adaptive = !themed && src instanceof AdaptiveIconDrawable;
        Drawable bg = adaptive ? ((AdaptiveIconDrawable) src).getBackground() : null;
        Drawable fg = adaptive ? ((AdaptiveIconDrawable) src).getForeground() : src;
        int dominant = dominant(fg != null ? fg : src);

        // Layer 1: background.
        int save = c.save(); c.clipPath(shape);
        int top, bottom;
        switch (variant) {
            case DARK: top = 0xff2a2f38; bottom = 0xff15181d; break;
            case CLEAR: top = 0x58ffffff; bottom = 0x26ffffff; break;
            case TINT_LIGHT: top = mix(tint, 0xffffffff, .82f); bottom = mix(tint, 0xffffffff, .62f); break;
            case TINT_DARK: top = mix(tint, 0xff000000, .55f); bottom = mix(tint, 0xff000000, .78f); break;
            default:
                if (themed) { top = IosGlyphs.top(ios); bottom = IosGlyphs.bottom(ios); }
                else { top = bg == null ? mix(dominant, 0xffffffff, .30f) : 0; bottom = bg == null ? mix(dominant, 0xff000000, .18f) : 0; }
        }
        if (variant == DEFAULT && bg != null) { Rect b = adaptiveBounds(tile); bg.setBounds(b); bg.draw(c); }
        else { p.setShader(new LinearGradient(0, tile.top, 0, tile.bottom, top, bottom, Shader.TileMode.CLAMP)); c.drawRect(tile, p); p.setShader(null); }

        // Layer 2: glyph, rendered to its own bitmap so it can cast a soft shadow and be recoloured.
        Bitmap glyph = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
        Canvas g = new Canvas(glyph);
        Drawable art = fg != null ? fg : src;
        if (themed) {
            boolean mono = variant != DEFAULT;
            int ink = !mono && IosGlyphs.lightTile(ios) ? 0xff1c1c1e : 0xffffffff;
            RectF gb = new RectF(tile); gb.inset(tile.width() * .06f, tile.height() * .06f);
            IosGlyphs.draw(g, gb, ios, ink, mono);
        }
        else if (adaptive) art.setBounds(adaptiveBounds(tile));
        else { float inset = variant == DEFAULT ? tile.width() * .12f : tile.width() * .16f; art.setBounds(Math.round(tile.left + inset), Math.round(tile.top + inset), Math.round(tile.right - inset), Math.round(tile.bottom - inset)); }
        if (!themed) art.draw(g);
        ColorFilter mono = null;
        if (variant == CLEAR) mono = monochrome(0xffffffff);
        else if (variant == TINT_LIGHT) mono = monochrome(mix(tint, 0xff000000, .35f));
        else if (variant == TINT_DARK) mono = monochrome(mix(tint, 0xffffffff, .45f));
        else if (variant == DARK && !themed) { ColorMatrix m = new ColorMatrix(); m.setSaturation(.85f); mono = new ColorMatrixColorFilter(m); }
        if (heavy && (variant != DEFAULT || (themed && !IosGlyphs.lightTile(ios)))) { p.setColorFilter(new PorterDuffColorFilter(0x55000000, PorterDuff.Mode.SRC_IN)); c.drawBitmap(glyph, 0, density, p); }
        p.setColorFilter(mono); c.drawBitmap(glyph, 0, 0, p); p.setColorFilter(null);
        glyph.recycle();

        // Layer 3: highlight.
        p.setShader(new LinearGradient(0, tile.top, 0, tile.top + tile.height() * .55f, variant == DARK ? 0x26ffffff : 0x40ffffff, 0x00ffffff, Shader.TileMode.CLAMP));
        c.drawRect(tile, p);
        if (heavy) {
            p.setShader(new RadialGradient(tile.left + tile.width() * .25f, tile.top, tile.width() * .7f, new int[]{0x44ffffff, 0x00ffffff}, null, Shader.TileMode.CLAMP));
            c.drawRect(tile, p);
        }
        p.setShader(null);
        c.restoreToCount(save);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(1f, density * .8f));
        p.setShader(new LinearGradient(tile.left, tile.top, tile.right, tile.bottom, new int[]{0xb0ffffff, 0x22ffffff, 0x44ffffff}, new float[]{0, .55f, 1}, Shader.TileMode.CLAMP));
        RectF rim = new RectF(tile); float half = p.getStrokeWidth() / 2f; rim.inset(half, half);
        c.drawPath(Squircle.path(rim, null), p);
        return out;
    }

    private static Rect adaptiveBounds(RectF tile) {
        // Adaptive layers are 108dp with a 72dp safe zone: extend by 1/4 of the tile on each side.
        float ext = tile.width() * .25f;
        return new Rect(Math.round(tile.left - ext), Math.round(tile.top - ext), Math.round(tile.right + ext), Math.round(tile.bottom + ext));
    }

    private static ColorFilter monochrome(int color) {
        // Luminance-weighted alpha keeps glyph detail while recolouring it.
        float r = Color.red(color) / 255f, g = Color.green(color) / 255f, b = Color.blue(color) / 255f;
        return new ColorMatrixColorFilter(new float[]{
            0, 0, 0, 0, r * 255,
            0, 0, 0, 0, g * 255,
            0, 0, 0, 0, b * 255,
            .25f, .5f, .1f, .35f, -40});
    }

    static int dominant(Drawable d) {
        try {
            Bitmap tiny = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);
            Rect old = d.copyBounds(); d.setBounds(0, 0, 8, 8); d.draw(new Canvas(tiny)); d.setBounds(old);
            long r = 0, g = 0, b = 0, n = 0;
            for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) { int c = tiny.getPixel(x, y); if (Color.alpha(c) < 128) continue; r += Color.red(c); g += Color.green(c); b += Color.blue(c); n++; }
            tiny.recycle();
            return n == 0 ? 0xff5a6b84 : Color.rgb((int) (r / n), (int) (g / n), (int) (b / n));
        } catch (RuntimeException e) { return 0xff5a6b84; }
    }

    static int mix(int a, int b, float f) {
        return Color.argb(Math.round(Color.alpha(a) * (1 - f) + Color.alpha(b) * f), Math.round(Color.red(a) * (1 - f) + Color.red(b) * f), Math.round(Color.green(a) * (1 - f) + Color.green(b) * f), Math.round(Color.blue(a) * (1 - f) + Color.blue(b) * f));
    }
}
