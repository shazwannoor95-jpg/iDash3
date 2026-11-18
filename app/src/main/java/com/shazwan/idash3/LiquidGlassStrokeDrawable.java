package com.shazwan.idash3;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** ZIP cyan/purple rim and gloss. Geometry/shaders are cached when bounds change. */
final class LiquidGlassStrokeDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path gloss = new Path();
    private final RectF box = new RectF(), inner = new RectF();
    private final float density, radiusDp;
    private float radius;
    private Shader edge, shine, shade;
    private int alpha = 255;

    LiquidGlassStrokeDrawable(float density, float radiusDp) {
        this.density = density;
        this.radiusDp = radiusDp;
    }

    private float dp(float value) { return value * density; }

    @Override protected void onBoundsChange(Rect b) {
        box.set(b.left + dp(1.2f), b.top + dp(1.2f), b.right - dp(1.2f), b.bottom - dp(1.2f));
        edge = shine = shade = null;
        gloss.reset();
        if (box.width() <= 0 || box.height() <= 0) return;
        radius = Math.min(dp(radiusDp), Math.min(box.width(), box.height()) / 2f);
        inner.set(box); inner.inset(dp(1.2f), dp(1.2f));
        edge = new LinearGradient(box.left, box.top, box.right, box.bottom,
                new int[]{0xaa37d8ff,0x99f255ff}, null, Shader.TileMode.CLAMP);
        RectF top = new RectF(box.left + dp(5), box.top + dp(4), box.right - dp(5), box.top + box.height() * .42f);
        if (top.width() > 0 && top.height() > 0) {
            gloss.addRoundRect(top, new float[]{radius,radius,radius,radius,dp(18),dp(18),dp(18),dp(18)}, Path.Direction.CW);
            shine = new LinearGradient(0, top.top, 0, top.bottom,
                    new int[]{0x38ffffff,0x10ffffff,0x00ffffff}, new float[]{0,.45f,1}, Shader.TileMode.CLAMP);
        }
        shade = new LinearGradient(0, box.centerY(), 0, box.bottom,
                new int[]{0x00000000,0x14000000}, null, Shader.TileMode.CLAMP);
    }

    @Override public void draw(Canvas canvas) {
        if (edge == null || alpha == 0) return;
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(dp(2.1f));
        paint.setShader(edge); paint.setAlpha(alpha);
        canvas.drawRoundRect(box, radius, radius, paint);
        paint.setShader(null); paint.setColor(0x7affffff); paint.setAlpha(alpha * 122 / 255);
        paint.setStrokeWidth(dp(1));
        if (inner.width() > 0 && inner.height() > 0)
            canvas.drawRoundRect(inner, Math.max(0,radius-dp(1.2f)), Math.max(0,radius-dp(1.2f)), paint);
        paint.setStyle(Paint.Style.FILL); paint.setColor(Color.WHITE); paint.setAlpha(alpha);
        if (shine != null) { paint.setShader(shine); canvas.drawPath(gloss, paint); }
        paint.setShader(shade); canvas.drawRoundRect(box, radius, radius, paint);
    }

    @Override public void setAlpha(int value) { alpha = Math.max(0,Math.min(255,value)); invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
