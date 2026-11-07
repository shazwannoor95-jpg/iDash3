package com.shazwan.idash3;

/**
 * Geometry for "liquid" merging of two rounded rectangles (light metaball). Pure floats so it can
 * be tested on the JVM; GlassSurface turns the result into an android.graphics.Path.
 * Output polygon is a closed bridge between facing edges with concave fillets.
 */
final class LiquidShape {
    private LiquidShape() {}

    /** Distance between rectangles (0 if they overlap/touch). */
    static float gap(float al, float at, float ar, float ab, float bl, float bt, float br, float bb) {
        float dx = Math.max(0f, Math.max(bl - ar, al - br)), dy = Math.max(0f, Math.max(bt - ab, at - bb));
        return (float) Math.hypot(dx, dy);
    }

    /** 0..1: how strongly the two shapes are joined (1 = fully merged, 0 = separate). */
    static float merge(float gap, float reach) {
        if (!(reach > 0f)) return 0f;
        float t = 1f - gap / reach;
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3f - 2f * t);
    }

    /**
     * Fills out[] with a bridge quad (x0,y0 .. x7,y7 as 8 floats: four corners clockwise) between the
     * facing edges of a and b, narrowing by the neck factor (1 = full edge overlap width, smaller = thinner).
     * Returns false when the rectangles overlap or are diagonal (no straight bridge needed/possible).
     */
    static boolean bridge(float al, float at, float ar, float ab, float bl, float bt, float br, float bb, float neck, float[] out) {
        if (out == null || out.length < 8) return false;
        neck = Math.max(.05f, Math.min(1f, neck));
        float oy0 = Math.max(at, bt), oy1 = Math.min(ab, bb), ox0 = Math.max(al, bl), ox1 = Math.min(ar, br);
        if (oy1 > oy0 && (ar <= bl || br <= al)) { // side by side
            float mid = (oy0 + oy1) / 2f, half = (oy1 - oy0) / 2f * neck;
            float x0 = ar <= bl ? ar : br, x1 = ar <= bl ? bl : al;
            out[0] = x0; out[1] = mid - half; out[2] = x1; out[3] = mid - half;
            out[4] = x1; out[5] = mid + half; out[6] = x0; out[7] = mid + half;
            return true;
        }
        if (ox1 > ox0 && (ab <= bt || bb <= at)) { // stacked
            float mid = (ox0 + ox1) / 2f, half = (ox1 - ox0) / 2f * neck;
            float y0 = ab <= bt ? ab : bb, y1 = ab <= bt ? bt : at;
            out[0] = mid - half; out[1] = y0; out[2] = mid + half; out[3] = y0;
            out[4] = mid + half; out[5] = y1; out[6] = mid - half; out[7] = y1;
            return true;
        }
        return false;
    }

    static float lerp(float a, float b, float t) { return a + (b - a) * t; }
}
