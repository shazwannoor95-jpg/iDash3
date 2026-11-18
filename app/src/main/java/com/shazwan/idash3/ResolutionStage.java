package com.shazwan.idash3;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Picture-in-picture style stage: the single child is laid out at a fixed virtual resolution
 * (dp, e.g. 854x480 = "480p") and uniformly scaled to fit this view, centred (letterboxed).
 * Touch input is mapped through the child's transform by ViewGroup, so taps land correctly.
 * width/height <= 0 means "fill" (no virtual resolution, child takes the stage size).
 */
final class ResolutionStage extends FrameLayout {
    static final String PREF = "panel_resolution";
    static final String[] NAMES = {"Auto (ikut panel)", "Layout 640×360", "Layout 854×480", "Layout 960×540", "Layout 1280×720"};
    static final int[][] SIZES = {{0, 0}, {640, 360}, {854, 480}, {960, 540}, {1280, 720}};
    static final int DEFAULT = 1;

    private int designW, designH;
    private final float density;

    ResolutionStage(Context c, int designWdp, int designHdp) {
        super(c);
        density = c.getResources().getDisplayMetrics().density;
        setClipChildren(true);
        setDesign(designWdp, designHdp);
    }

    static int preset(android.content.SharedPreferences p) { return GlassAppearance.clamp(GlassAppearance.integer(p, PREF, DEFAULT), 0, SIZES.length - 1); }

    void setDesign(int wDp, int hDp) { designW = wDp; designH = hDp; requestLayout(); }

    private boolean fixed() { return designW > 0 && designH > 0; }

    /** Uniform scale that fits the design size into w x h (never enlarges beyond 1:1 is not enforced: small designs scale up). */
    static float fit(float availW, float availH, float designWpx, float designHpx) {
        if (!(designWpx > 0) || !(designHpx > 0) || !(availW > 0) || !(availH > 0)) return 1f;
        return Math.min(availW / designWpx, availH / designHpx);
    }

    @Override protected void onMeasure(int wSpec, int hSpec) {
        int w = MeasureSpec.getSize(wSpec), h = MeasureSpec.getSize(hSpec);
        setMeasuredDimension(w, h);
        int cw = fixed() ? Math.round(designW * density) : w, ch = fixed() ? Math.round(designH * density) : h;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) child.measure(MeasureSpec.makeMeasureSpec(cw, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(ch, MeasureSpec.EXACTLY));
        }
    }

    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l, h = b - t;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            int cw = child.getMeasuredWidth(), ch = child.getMeasuredHeight();
            child.layout(0, 0, cw, ch);
            float s = fixed() ? fit(w, h, cw, ch) : 1f;
            child.setPivotX(0); child.setPivotY(0);
            child.setScaleX(s); child.setScaleY(s);
            child.setTranslationX((w - cw * s) / 2f); child.setTranslationY((h - ch * s) / 2f);
        }
    }
}
