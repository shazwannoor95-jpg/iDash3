package com.shazwan.idash3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Lightweight cyan-to-violet luminous rim to be attached as View foreground.
 * The actual frost/refraction is provided by the existing GlassSurface background.
 * No per-frame blur, RenderScript, OpenGL or new dependencies.
 */
final class NeonGlassRim extends Drawable {
    private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlight = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float density;
    private final float cornerDp;
    private Shader colorSweep;
    private int alpha=255;

    NeonGlassRim(Context context, float radiusDp) {
        density=context.getResources().getDisplayMetrics().density;
        cornerDp=radiusDp;
        rim.setStyle(Paint.Style.STROKE);
        highlight.setStyle(Paint.Style.STROKE);
    }

    @Override protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        if (bounds.isEmpty()) { colorSweep=null; return; }
        colorSweep=new LinearGradient(bounds.left,bounds.top,bounds.right,bounds.bottom,
                new int[] {0xff49dcff,0xff438efc,0xffb36afe,0xfffa6eff},
                new float[] {0f,.38f,.69f,1f},Shader.TileMode.CLAMP);
    }

    @Override public void draw(Canvas c) {
        Rect b=getBounds();
        if (b.width()<=0||b.height()<=0||colorSweep==null) return;
        float rad=cornerDp*density;
        // Stacked translucent strokes resemble a glow without GPU blur/shadow layers.
        for (int layer=2;layer>=0;layer--) {
            float inset=(1.2f+layer*2.2f)*density;
            rect.set(b.left+inset,b.top+inset,b.right-inset,b.bottom-inset);
            if(rect.width()<=0||rect.height()<=0)continue;
            rim.setShader(colorSweep);
            rim.setStrokeWidth((layer==0?2.0f:layer==1?5.0f:9.0f)*density);
            rim.setAlpha(alpha*(layer==0?235:layer==1?65:20)/255);
            c.drawRoundRect(rect,Math.max(1f,rad-inset),Math.max(1f,rad-inset),rim);
        }
        rim.setShader(null);
        rect.set(b.left+2.6f*density,b.top+2.6f*density,b.right-2.6f*density,b.bottom-2.6f*density);
        highlight.setColor(0x5effffff);
        highlight.setAlpha(alpha*94/255);
        highlight.setStrokeWidth(.8f*density);
        c.drawRoundRect(rect,Math.max(1f,rad-3*density),Math.max(1f,rad-3*density),highlight);
        // Top inset reflection, short and crisp.
        highlight.setColor(0x84ffffff);
        highlight.setAlpha(alpha*132/255);
        c.drawArc(rect,196,93,false,highlight);
    }

    @Override public void setAlpha(int value) { alpha=Math.max(0,Math.min(255,value));invalidateSelf(); }
    @Override public void setColorFilter(android.graphics.ColorFilter filter) { rim.setColorFilter(filter);highlight.setColorFilter(filter);invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
