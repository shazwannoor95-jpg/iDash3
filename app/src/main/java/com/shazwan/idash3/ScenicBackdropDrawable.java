package com.shazwan.idash3;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** ZIP scenic blue/magenta landscape, cached per bounds; no live blur or frame loop. */
final class ScenicBackdropDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint[] glows = new Paint[3];
    private final Path hill1 = new Path(), hill2 = new Path(), hill3 = new Path();
    private final float[] cx = new float[3], cy = new float[3], radius = new float[3];
    private Shader sky;
    private int alpha = 255;

    ScenicBackdropDrawable(float density) {
        for (int i = 0; i < glows.length; i++) glows[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override protected void onBoundsChange(Rect b) {
        int w = b.width(), h = b.height();
        sky = null;
        hill1.reset(); hill2.reset(); hill3.reset();
        if (w <= 0 || h <= 0) return;
        sky = new LinearGradient(0,0,w,h,
                new int[]{0xff1e7ec2,0xff2d58b6,0xff6c3eb8,0xffb64d87},
                new float[]{0,.32f,.70f,1},Shader.TileMode.CLAMP);
        glow(0,w*.12f,h*.10f,w*.22f,0x663ee8ff);
        glow(1,w*.82f,h*.14f,w*.24f,0x55ff52d9);
        glow(2,w*.55f,h*.80f,w*.30f,0x33ff88c8);
        hill3.moveTo(0,h*.82f);
        hill3.cubicTo(w*.18f,h*.72f,w*.36f,h*.92f,w*.52f,h*.84f);
        hill3.cubicTo(w*.72f,h*.73f,w*.86f,h*.90f,w,h*.78f);
        hill3.lineTo(w,h); hill3.lineTo(0,h); hill3.close();
        hill2.moveTo(0,h*.88f);
        hill2.cubicTo(w*.15f,h*.80f,w*.28f,h*.96f,w*.46f,h*.86f);
        hill2.cubicTo(w*.64f,h*.76f,w*.82f,h*.94f,w,h*.83f);
        hill2.lineTo(w,h); hill2.lineTo(0,h); hill2.close();
        hill1.moveTo(0,h*.93f);
        hill1.cubicTo(w*.20f,h*.86f,w*.42f,h,w*.60f,h*.92f);
        hill1.cubicTo(w*.76f,h*.85f,w*.90f,h*.98f,w,h*.90f);
        hill1.lineTo(w,h); hill1.lineTo(0,h); hill1.close();
    }

    private void glow(int i,float x,float y,float r,int color) {
        cx[i]=x; cy[i]=y; radius[i]=r;
        glows[i].setShader(new RadialGradient(x,y,r,new int[]{color,0x00ffffff},null,Shader.TileMode.CLAMP));
    }

    @Override public void draw(Canvas canvas) {
        if (sky == null || alpha == 0) return;
        Rect b = getBounds();
        int save = canvas.save(); canvas.translate(b.left,b.top);
        paint.setShader(sky); paint.setAlpha(alpha);
        canvas.drawRect(0,0,b.width(),b.height(),paint);
        for (int i=0;i<glows.length;i++) {
            glows[i].setAlpha(alpha); canvas.drawCircle(cx[i],cy[i],radius[i],glows[i]);
        }
        paint.setShader(null);
        paint.setColor(0x4017264f); paint.setAlpha(alpha*64/255); canvas.drawPath(hill3,paint);
        paint.setColor(0x2a0d1735); paint.setAlpha(alpha*42/255); canvas.drawPath(hill2,paint);
        paint.setColor(0x26081426); paint.setAlpha(alpha*38/255); canvas.drawPath(hill1,paint);
        canvas.restoreToCount(save);
    }

    @Override public void setAlpha(int value) { alpha=Math.max(0,Math.min(255,value)); invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter filter) {
        paint.setColorFilter(filter); for (Paint glow:glows) glow.setColorFilter(filter); invalidateSelf();
    }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
