package com.shazwan.idash3;

import android.graphics.*;
import android.view.View;
import android.view.ViewGroup;

/**
 * Liquid morph between two rectangles (e.g. folder icon -> folder panel). Each edge has its own
 * spring so the shape stretches like a fluid; a narrowing neck (LiquidShape) keeps it joined to the
 * source until it pulls away. Glass reveal (lens + blur) follows progress. Drawn only while moving.
 */
final class MorphOverlay extends View implements SpringDriver.Listener {
    private final GlassSurface glass;
    private final RectF from = new RectF(), to = new RectF(), now = new RectF();
    private final Spring left, top, right, bottom, round, progress;
    private final SpringDriver driver;
    private final Runnable done;
    private final Paint neckPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path neck = new Path();
    private final float[] quad = new float[8];
    private final float density, reach;
    private final boolean opening;

    /** Rects are in the coordinates of the parent this view is added to (full-screen stage). */
    MorphOverlay(LauncherActivity a, RectF source, RectF target, float sourceRadius, float targetRadius, boolean opening, Runnable done) {
        super(a);
        this.opening = opening; this.done = done;
        density = a.getResources().getDisplayMetrics().density;
        reach = 56 * density;
        from.set(opening ? source : target); to.set(opening ? target : source);
        float r0 = opening ? sourceRadius : targetRadius, r1 = opening ? targetRadius : sourceRadius;
        // Leading edges are stiffer; trailing edges lag -> fluid stretch.
        boolean growRight = to.centerX() >= from.centerX(), growDown = to.centerY() >= from.centerY();
        left = new Spring(growRight ? 300 : 520, .80f, from.left); left.target = to.left;
        right = new Spring(growRight ? 520 : 300, .80f, from.right); right.target = to.right;
        top = new Spring(growDown ? 300 : 520, .80f, from.top); top.target = to.top;
        bottom = new Spring(growDown ? 520 : 300, .80f, from.bottom); bottom.target = to.bottom;
        round = new Spring(380, .9f, r0); round.target = r1;
        progress = new Spring(420, 1f, opening ? 0f : 1f); progress.target = opening ? 1f : 0f;
        driver = new SpringDriver(this, left, top, right, bottom, round, progress);
        glass = ((GlassSurface) IosStyle.glass(a, 0x9a2b3d5c, 24, true)).bakeEnabled(false);
        glass.setCallback(this);
        neckPaint.setColor(0x8a2b3d5c);
        setWillNotDraw(false);
    }

    void start() {
        if (!IosStyle.motion(getContext()) || GlassPerf.low()) { driver.finish(); return; }
        driver.start();
    }

    @Override public void onSpringUpdate() { invalidate(); }

    @Override public void onSpringRest() {
        ViewGroup parent = (ViewGroup) getParent();
        glass.setCallback(null); // detached transient surface: no further invalidation or touch feed
        if (done != null) done.run();
        if (parent != null) parent.removeView(this);
    }

    @Override protected void onDetachedFromWindow() { driver.cancel(); super.onDetachedFromWindow(); }

    @Override protected boolean verifyDrawable(android.graphics.drawable.Drawable who) { return who == glass || super.verifyDrawable(who); }

    @Override protected void onDraw(Canvas canvas) {
        now.set(Math.min(left.value, right.value - density), Math.min(top.value, bottom.value - density), Math.max(right.value, left.value + density), Math.max(bottom.value, top.value + density));
        RectF anchor = opening ? from : to;
        float gap = LiquidShape.gap(anchor.left, anchor.top, anchor.right, anchor.bottom, now.left, now.top, now.right, now.bottom);
        float merge = LiquidShape.merge(gap, reach);
        if (merge > 0f && LiquidShape.bridge(anchor.left, anchor.top, anchor.right, anchor.bottom, now.left, now.top, now.right, now.bottom, .25f + .75f * merge, quad)) {
            neck.reset();
            // Concave sides: control points pulled toward the bridge centre line.
            float cx = (quad[0] + quad[2] + quad[4] + quad[6]) / 4f, cy = (quad[1] + quad[3] + quad[5] + quad[7]) / 4f, pull = .45f * (1f - merge);
            neck.moveTo(quad[0], quad[1]);
            neck.quadTo(LiquidShape.lerp((quad[0] + quad[2]) / 2f, cx, pull), LiquidShape.lerp((quad[1] + quad[3]) / 2f, cy, pull), quad[2], quad[3]);
            neck.lineTo(quad[4], quad[5]);
            neck.quadTo(LiquidShape.lerp((quad[4] + quad[6]) / 2f, cx, pull), LiquidShape.lerp((quad[5] + quad[7]) / 2f, cy, pull), quad[6], quad[7]);
            neck.close();
            neckPaint.setAlpha(Math.round(0x8a * merge));
            canvas.drawPath(neck, neckPaint);
        }
        glass.setRadiusPx(Math.max(0f, round.value));
        glass.setBounds(Math.round(now.left), Math.round(now.top), Math.round(now.right), Math.round(now.bottom));
        glass.setReveal(Math.max(0f, Math.min(1f, progress.value)));
        glass.draw(canvas);
    }
}
