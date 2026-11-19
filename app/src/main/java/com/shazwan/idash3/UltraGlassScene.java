package com.shazwan.idash3;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** Kit scene with cached geometry/shaders; never substitutes for the live map. */
final class UltraGlassScene extends Drawable {
 private final Bitmap backdrop;private final boolean scenic;
 private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
 private final Paint[] glow=new Paint[5];private final float[] x=new float[5],y=new float[5],r=new float[5];
 private final Path[] hills={new Path(),new Path(),new Path()};
 private final Rect src=new Rect();private final RectF dst=new RectF();
 private static final int[] HILL_COLORS={0x45423c89,0x5536317f,0x55201552};
 private Shader sky,dim;private int alpha=255;
 UltraGlassScene(Bitmap bitmap,boolean useScenery){backdrop=bitmap;scenic=useScenery;for(int i=0;i<5;i++)glow[i]=new Paint(Paint.ANTI_ALIAS_FLAG);}
 @Override protected void onBoundsChange(Rect bounds){
  float w=bounds.width(),h=bounds.height();sky=null;if(w<=0||h<=0)return;
  sky=new LinearGradient(0,0,w,h,new int[]{0xff045080,0xff122a70,0xff452271,0xff9f347c},new float[]{0,.32f,.70f,1},Shader.TileMode.CLAMP);
  dim=new LinearGradient(0,0,0,h,new int[]{0x22031530,0x10000000,0x33061131},null,Shader.TileMode.CLAMP);
  if(backdrop!=null){float scale=Math.max(w/backdrop.getWidth(),h/backdrop.getHeight());int sw=Math.min(backdrop.getWidth(),Math.max(1,Math.round(w/scale))),sh=Math.min(backdrop.getHeight(),Math.max(1,Math.round(h/scale)));int left=(backdrop.getWidth()-sw)/2,top=(backdrop.getHeight()-sh)/2;src.set(left,top,left+sw,top+sh);}dst.set(0,0,w,h);
  glow(0,.1f*w,.1f*h,.43f*w,0x8861dfff);glow(1,.85f*w,.28f*h,.48f*w,0x88ff52d5);glow(2,.55f*w,.75f*h,.35f*w,0x557f87ff);glow(3,.2f*w,1.03f*h,.5f*w,0x5543bfff);glow(4,.83f*w,1.02f*h,.54f*w,0x77ff36dc);
  hill(hills[0],w,h,.78f,.08f);hill(hills[1],w,h,.87f,.13f);hill(hills[2],w,h,.95f,.08f);
 }
 private void glow(int i,float cx,float cy,float radius,int color){x[i]=cx;y[i]=cy;r[i]=radius;glow[i].setShader(new RadialGradient(cx,cy,Math.max(1,radius),new int[]{color,Color.TRANSPARENT},null,Shader.TileMode.CLAMP));}
 private void hill(Path p,float w,float h,float at,float v){p.reset();p.moveTo(0,h*at);p.cubicTo(w*.14f,h*(at-v),w*.24f,h*(at+v),w*.42f,h*at);p.cubicTo(w*.62f,h*(at-v),w*.79f,h*(at+v),w,h*(at-v*.3f));p.lineTo(w,h);p.lineTo(0,h);p.close();}
 @Override public void draw(Canvas c){if(sky==null||alpha==0)return;Rect bounds=getBounds();int saved=c.save();c.translate(bounds.left,bounds.top);c.clipRect(0,0,bounds.width(),bounds.height());paint.setColor(Color.WHITE);paint.setAlpha(alpha);paint.setShader(sky);c.drawRect(dst,paint);paint.setShader(null);
  if(scenic&&backdrop!=null&&!backdrop.isRecycled()){c.drawBitmap(backdrop,src,dst,paint);paint.setShader(dim);c.drawRect(dst,paint);paint.setShader(null);}else{for(int i=0;i<3;i++){glow[i].setAlpha(alpha);c.drawCircle(x[i],y[i],r[i],glow[i]);}for(int i=0;i<3;i++){paint.setColor(HILL_COLORS[i]);paint.setAlpha(alpha*Color.alpha(HILL_COLORS[i])/255);c.drawPath(hills[i],paint);}}
  for(int i=3;i<5;i++){glow[i].setAlpha(alpha);c.drawCircle(x[i],y[i],r[i],glow[i]);}c.restoreToCount(saved);
 }
 @Override public void setAlpha(int value){alpha=Math.max(0,Math.min(255,value));invalidateSelf();}
 @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);for(Paint p:glow)p.setColorFilter(filter);invalidateSelf();}
 @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
