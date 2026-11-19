package com.shazwan.idash3;

import android.graphics.*;
import android.graphics.drawable.*;
import android.os.Build;
import android.os.SystemClock;

/** Kit prism/halo/caustic foreground; one activity session drives all visible rims. */
final class UltraGlassOverlay extends Drawable implements Animatable {
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
 private final RectF box=new RectF(),inner=new RectF();
 private final Path rim=new Path(),inside=new Path();
 private final Matrix sweepMatrix=new Matrix();
 private final float density,requestedRadius;
 private Shader edge,top,film,sweep,agsl;
 private Bitmap halo;
 private float radius,haloScale=1;
 private boolean running;
 private long started;
 private int alpha=255;
 private ColorFilter filter;
 UltraGlassOverlay(float density,float radiusDp){
  this.density=Math.max(.5f,density);requestedRadius=radiusDp;
  if(UltraGlassConfig.ENABLE_AGSL&&Build.VERSION.SDK_INT>=33)try{agsl=Api33.create();}catch(RuntimeException|LinkageError unsupported){agsl=null;}
 }
 private float dp(float n){return n*density;}
 @Override protected void onBoundsChange(Rect bounds){
  halo=null;edge=top=film=sweep=null;rim.reset();inside.reset();
  float w=bounds.width(),h=bounds.height();if(w<=8||h<=8)return;
  radius=Math.min(dp(requestedRadius),Math.min(w,h)*.5f);box.set(dp(3),dp(3),w-dp(3),h-dp(3));inner.set(box);inner.inset(dp(2.6f),dp(2.6f));
  rim.setFillType(Path.FillType.EVEN_ODD);rim.addRoundRect(box,radius,radius,Path.Direction.CW);rim.addRoundRect(inner,Math.max(1,radius-dp(2.6f)),Math.max(1,radius-dp(2.6f)),Path.Direction.CW);inside.addRoundRect(box,radius,radius,Path.Direction.CW);
  edge=new LinearGradient(0,0,w,h,new int[]{0xff5ce9ff,0xff9ccfff,0xffdb5dff,0xffff79df},new float[]{0,.37f,.70f,1},Shader.TileMode.CLAMP);
  top=new LinearGradient(0,box.top,0,box.top+Math.max(1,h*.48f),new int[]{0x45ffffff,0x10ffffff,0x00ffffff},new float[]{0,.3f,1},Shader.TileMode.CLAMP);
  film=new LinearGradient(0,0,w,h,new int[]{0x1407bcef,0x070b204d,0x0eff56df},null,Shader.TileMode.CLAMP);
  sweep=new RadialGradient(0,0,Math.max(dp(45),w*.22f),new int[]{0xbbffffff,0x6656e7ff,0x00ffffff},new float[]{0,.32f,1},Shader.TileMode.CLAMP);
  if(UltraGlassConfig.ENABLE_STATIC_HALOS)try{
   haloScale=Math.min(1,Math.min(768f/w,512f/h));Bitmap b=Bitmap.createBitmap(Math.max(1,Math.round(w*haloScale)),Math.max(1,Math.round(h*haloScale)),Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);c.scale(haloScale,haloScale);Paint glow=new Paint(Paint.ANTI_ALIAS_FLAG);glow.setStyle(Paint.Style.STROKE);glow.setShader(edge);
   float[] widths={16,11,7,4.4f};int[] levels={13,19,29,45};for(int i=0;i<widths.length;i++){glow.setStrokeWidth(dp(widths[i]));glow.setAlpha(levels[i]);c.drawRoundRect(box,radius,radius,glow);}halo=b;
  }catch(OutOfMemoryError unavailable){halo=null;}
 }
 @Override public void draw(Canvas c){
  Rect bounds=getBounds();if(edge==null||alpha==0)return;
  int saved=c.save();c.translate(bounds.left,bounds.top);float w=bounds.width(),h=bounds.height();
  p.setColorFilter(filter);p.setColor(Color.WHITE);p.setAlpha(alpha);p.setStyle(Paint.Style.FILL);p.setShader(null);
  if(halo!=null){int s=c.save();c.scale(1/haloScale,1/haloScale);c.drawBitmap(halo,0,0,p);c.restoreToCount(s);}
  p.setShader(film);c.drawRoundRect(box,radius,radius,p);
  float seconds=running?(SystemClock.uptimeMillis()-started)/1000f:0;
  // Caustics stay on the narrow border, never over live text/map pixels.
  if(UltraGlassConfig.ENABLE_CAUSTICS&&agsl!=null&&c.isHardwareAccelerated()){
   int s=c.save();try{Api33.update(agsl,w,h,seconds);c.clipPath(rim);p.setShader(agsl);p.setAlpha(alpha*4/5);c.drawRect(0,0,w,h,p);}catch(RuntimeException|LinkageError failed){agsl=null;}finally{c.restoreToCount(s);p.setAlpha(alpha);}
  }
  int s=c.save();c.clipPath(inside);p.setShader(top);c.drawRect(0,0,w,h*.48f,p);c.restoreToCount(s);
  p.setStyle(Paint.Style.STROKE);p.setShader(edge);p.setStrokeWidth(dp(1.8f));c.drawRoundRect(box,radius,radius,p);
  p.setShader(null);p.setColor(Color.WHITE);p.setAlpha(alpha*170/255);p.setStrokeWidth(dp(.72f));c.drawRoundRect(inner,Math.max(1,radius-dp(2.6f)),Math.max(1,radius-dp(2.6f)),p);
  if(running){float x=w*(.1f+.8f*(.5f+.5f*(float)Math.sin(seconds*.76f)));sweepMatrix.setTranslate(x,box.top+dp(2));sweep.setLocalMatrix(sweepMatrix);p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);p.setAlpha(alpha);p.setShader(sweep);c.drawPath(rim,p);}
  c.restoreToCount(saved);
 }
 @Override public void start(){if(running||!UltraGlassConfig.ENABLE_ANIMATION)return;running=true;started=SystemClock.uptimeMillis();invalidateSelf();}
 @Override public void stop(){running=false;}
 @Override public boolean isRunning(){return running;}
 @Override public boolean setVisible(boolean visible,boolean restart){boolean changed=super.setVisible(visible,restart);if(!visible)stop();return changed;}
 @Override public void setAlpha(int value){alpha=Math.max(0,Math.min(255,value));invalidateSelf();}
 @Override public void setColorFilter(ColorFilter value){filter=value;invalidateSelf();}
 @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
 @android.annotation.TargetApi(33)
 private static final class Api33 {
  static Shader create(){return UltraGlassAgsl.create();}
  static void update(Shader shader,float w,float h,float t){UltraGlassAgsl.update((android.graphics.RuntimeShader)shader,w,h,t);}
 }
}
