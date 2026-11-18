package com.shazwan.idash3;
import android.graphics.*;import android.graphics.drawable.Drawable;import android.view.View;import android.view.ViewGroup;import android.widget.TextView;
import java.lang.ref.WeakReference;
/** Original API27 glass: wallpaper frost, cached shaders, rim/reflection and inset shadow bands. */
final class GlassSurface extends Drawable {
 private final Paint paint=new Paint(3);private final RectF box=new RectF(),rim=new RectF(),shadow=new RectF();private final Path clip=new Path();private final Matrix matrix=new Matrix();private final int[] position=new int[2];
 private float radius;private final float density;private final int tint,style,tone;private final int baseQuality;private int quality;private GlassAppearance appearance;private final boolean sheet,premium;private final GlassBackdrop backdrop;
 private final java.util.ArrayList<Protection> protections=new java.util.ArrayList<>();private int protectionCount;private int protectionGeometry;private boolean protectionDirty=true;private int bodyRgb,bodyAlpha;
 private final Path specular=new Path();private final RectF inner=new RectF();private Shader bevel,specularFade;private boolean light,contrastQueued;
 private int material=GlassMaterial.CLASSIC;private Shader materialGloss,materialShadow;
 GlassSurface material(int value){int next=value==GlassMaterial.IOS26?GlassMaterial.IOS26:GlassMaterial.CLASSIC;if(material!=next){material=next;if(!box.isEmpty())rebuild();invalidateSelf();}return this;}
 int material(){return material;}
 private Shader body,sheen,edge,reflection;private ColorFilter externalFilter;private BitmapShader frost;private int opacity=255,revision=-1;
 // Liquid Glass v2 state: region tint, edge lensing, touch specular, depth and reveal (blur/lens transition).
 private BitmapShader frostLens,frostSoft;private final Matrix lensMatrix=new Matrix(),softMatrix=new Matrix(),touchMatrix=new Matrix();private final Path ring=new Path();private final RectF ringInner=new RectF();
 private int regionTint,adaptKey=Integer.MIN_VALUE,fxGen=-1;private float reveal=1f,depth,lensK,touchX,touchY,touchA;private boolean lens,dynSpec,touching;private long touchStamp;private Shader touchShader;private final Paint fxPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
 // Baked refraction (GlassBake): replaces frost + lens ring when ready; frost stays the fallback.
 private Bitmap baked;private BitmapShader bakedShader;private final Matrix bakedMatrix=new Matrix();private volatile int bakeKey=Integer.MIN_VALUE;private volatile boolean bakeEnabled=true;private boolean bakeRetryQueued;
 static final ColorFilter BAKED_DARK_FILTER=new LightingColorFilter(0xffd2d2d2,0);static final int BAKED_DARK_MULTIPLIER=0xd2;static final float BAKED_BODY=.55f;
 /** Transient shapes (morph) change every frame and must not bake. */
 GlassSurface bakeEnabled(boolean on){bakeEnabled=on;if(!on){baked=null;bakedShader=null;}return this;}
 boolean isBaked(){return bakedShader!=null;}
 /** Tag a short accent label (e.g. "TODAY") to skip the local contrast scrim; it must have its own contrast. */
 static final String NO_SCRIM="glass-no-scrim";
 GlassSurface(float d,int tint,int radius,int style,boolean sheet){this(d,tint,radius,style,sheet,null,1,0);}
 GlassSurface(float d,int tint,int radius,int style,boolean sheet,GlassBackdrop backdrop,int quality,int tone){this(d,tint,radius,style,sheet,backdrop,quality,tone,false);}
 GlassSurface(float d,int tint,int radius,int style,boolean sheet,GlassBackdrop backdrop,int quality,int tone,boolean premium){this.premium=premium;this.density=d;this.radius=radius*d;this.tint=tint;this.style=style;this.sheet=sheet;this.backdrop=backdrop;this.quality=quality;this.baseQuality=quality;this.tone=tone;if(backdrop!=null){if(backdrop.surfaces.size()>128)for(int i=backdrop.surfaces.size()-1;i>=0;i--)if(backdrop.surfaces.get(i).get()==null)backdrop.surfaces.remove(i);backdrop.surfaces.add(new WeakReference<>(this));}}
 GlassSurface appearance(GlassAppearance value){if(appearance==value||(appearance!=null&&value!=null&&appearance.same(value)))return this;appearance=value;quality=value==null?baseQuality:value.quality;fxGen=-1;if(!box.isEmpty())rebuild();invalidateSelf();return this;}
 private static int mix(int a,int b,float f){return Color.rgb(Math.round(Color.red(a)*f+Color.red(b)*(1-f)),Math.round(Color.green(a)*f+Color.green(b)*(1-f)),Math.round(Color.blue(a)*f+Color.blue(b)*(1-f)));}
 @Override protected void onBoundsChange(Rect bounds){box.set(bounds);box.inset(2*density,2*density);rim.set(box);rim.inset(density*.5f,density*.5f);clip.reset();clip.addRoundRect(box,radius,radius,Path.Direction.CW);rebuild();}
 private void rebuild(){
  GlassBackdrop.Frame frame=backdrop==null?null:backdrop.frame;
  revision=backdrop==null?0:backdrop.revision;
  int sampled=regionTint!=0?regionTint:frame==null?tint:frame.tint;
  int selectedTone=appearance==null?tone:appearance.tone;
  light=selectedTone==IosStyle.LIGHT||(selectedTone==IosStyle.AUTO&&Color.luminance(sampled)>.35);
  int chosen=appearance==null?sampled:appearance.tintColor(sampled);
  bodyRgb=mix(chosen,light?IosStyle.LIGHT_BASE:IosStyle.DARK_BASE,IosStyle.GLASS_WALLPAPER_WEIGHT);
  if(material==GlassMaterial.IOS26&&style!=2)bodyRgb=mix(0xffd2f0ff,bodyRgb,light?.35f:.18f);
  // Existing percentages keep their absolute meaning. Only absent/reset defaults change.
  bodyAlpha=style==2?255:appearance!=null?Math.round(255*(1-appearance.transparency/100f)):style==1?65:IosStyle.GLASS_BODY_ALPHA;
  int bottom=style==2?255:style==1&&appearance==null?108:Math.min(245,bodyAlpha+8);
  body=new LinearGradient(0,box.top,0,box.bottom,new int[]{(bodyAlpha<<24)|(bodyRgb&0xffffff),(bottom<<24)|(bodyRgb&0xffffff)},null,Shader.TileMode.CLAMP);
  float strength=quality==IosStyle.LOW?.55f:appearance==null?1:appearance.reflection==0?.55f:appearance.reflection==2?1.25f:1;
  sheen=new LinearGradient(0,box.top,0,box.bottom,new int[]{scaled(0x14ffffff,strength),0x02ffffff,0x00ffffff},new float[]{0,.25f,1},Shader.TileMode.CLAMP);
  edge=new LinearGradient(box.left,box.top,box.right,box.bottom,new int[]{scaled(0xa0ffffff,strength),scaled(0x25ffffff,strength),0x08ffffff,scaled(0x45ffffff,strength)},new float[]{0,.24f,.72f,1},Shader.TileMode.CLAMP);
  reflection=new RadialGradient(box.left+radius,box.top-radius,Math.max(1,box.width()*.65f),new int[]{scaled(0x10ffffff,strength),0x00ffffff},null,Shader.TileMode.CLAMP);
  inner.set(rim);inner.inset(density*2,density*2);
  bevel=new LinearGradient(0,box.top,0,box.bottom,new int[]{0x18081120,0x00000000,0x0b000000,0x12ffffff},new float[]{0,.14f,.85f,1},Shader.TileMode.CLAMP);
  specularFade=new LinearGradient(box.left,box.top,box.left+box.width()*.55f,box.top+radius,new int[]{scaled(0x68ffffff,strength),scaled(0x20ffffff,strength),0x00ffffff},new float[]{0,.45f,1},Shader.TileMode.CLAMP);
  specular.reset();specular.moveTo(box.left+density*2,box.top+radius);
  specular.cubicTo(box.left+density*2,box.top+radius*.42f,box.left+radius*.42f,box.top+density*2,box.left+radius,box.top+density*2);
  specular.lineTo(box.left+Math.max(radius,box.width()*.46f),box.top+density*2);
  protectionDirty=true;contrastQueued=false;
  frost=frame==null||frame.bitmap==null||style==2?null:new BitmapShader(frame.bitmap,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
  materialGloss=material==GlassMaterial.IOS26&&style!=2?new LinearGradient(box.left,box.top,box.right,box.bottom,new int[]{0x00ffffff,0x2effffff,0x00ffffff},new float[]{.12f,.48f,.83f},Shader.TileMode.CLAMP):null;
  materialShadow=materialGloss==null?null:new LinearGradient(0,box.top,0,box.bottom,new int[]{0x00102a40,0x3a102a40},null,Shader.TileMode.CLAMP);
  buildFx(frame);
 }
 /** Depth, lens ring and touch shader. Cheap objects, rebuilt only with the material. */
 private void buildFx(GlassBackdrop.Frame frame){
  float shortest=Math.min(box.width(),box.height())/density;depth=Math.max(0f,Math.min(1f,(shortest-56f)/184f));
  lens=quality>=IosStyle.BALANCED&&style!=2&&frost!=null;dynSpec=quality>=IosStyle.BALANCED&&style!=2;
  if(lens){float lw=density*(5f+9f*depth);lensK=.018f+.05f*depth;ring.reset();ring.setFillType(Path.FillType.EVEN_ODD);ring.addRoundRect(box,radius,radius,Path.Direction.CW);ringInner.set(box);ringInner.inset(lw,lw);float ir=Math.max(0,radius-lw);ring.addRoundRect(ringInner,ir,ir,Path.Direction.CW);frostLens=new BitmapShader(frame.bitmap,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);}else frostLens=null;
  frostSoft=frame!=null&&frame.soft!=null&&style!=2?new BitmapShader(frame.soft,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP):null;
  float reach=Math.max(density*60,Math.min(box.width(),box.height())*.6f);
  touchShader=dynSpec?new RadialGradient(0,0,reach,new int[]{0xe0ffffff,0x60ffffff,0x00ffffff},new float[]{0,.35f,1},Shader.TileMode.CLAMP):null;
 }
 /** Adaptive tint: mean wallpaper colour under THIS panel, recomputed only when position/size/backdrop change. */
 private void adaptRegion(View owner){
  GlassBackdrop.Frame f=backdrop==null?null:backdrop.frame;
  if(f==null||f.bitmap==null||style==2){if(regionTint!=0){regionTint=0;adaptKey=Integer.MIN_VALUE;rebuild();}return;}
  owner.getLocationOnScreen(position);
  int key=31*(31*(31*position[0]+position[1])+(int)box.width())+(int)box.height();key=31*key+backdrop.revision;
  if(key==adaptKey)return;adaptKey=key;
  Bitmap bmp=f.bitmap;android.util.DisplayMetrics m=owner.getResources().getDisplayMetrics();
  float scale=Math.max(m.widthPixels/(float)bmp.getWidth(),m.heightPixels/(float)bmp.getHeight());
  float ox=(m.widthPixels-bmp.getWidth()*scale)/2f,oy=(m.heightPixels-bmp.getHeight()*scale)/2f;
  float l=(position[0]+box.left-ox)/scale,t=(position[1]+box.top-oy)/scale,r=(position[0]+box.right-ox)/scale,b=(position[1]+box.bottom-oy)/scale;
  int[] grid=new int[25];for(int gy=0;gy<5;gy++)for(int gx=0;gx<5;gx++){int px=Math.max(0,Math.min(bmp.getWidth()-1,(int)(l+(r-l)*(gx+.5f)/5)));int py=Math.max(0,Math.min(bmp.getHeight()-1,(int)(t+(b-t)*(gy+.5f)/5)));grid[gy*5+gx]=bmp.getPixel(px,py);}
  int avg=Adaptive.averageRegion(grid,5,5,0,0,5,5,5,f.tint);
  if(avg!=regionTint){regionTint=avg;rebuild();invalidateSelf();}
  requestBake(owner,f,scale,ox,oy);
 }
 /** Corner radius in px for morphing shapes; geometry is rebuilt on the next bounds change. */
 void setRadiusPx(float px){if(px!=radius){radius=Math.max(0,px);Rect b=getBounds();if(!b.isEmpty())onBoundsChange(b);invalidateSelf();}}
 private void requestBake(View owner,GlassBackdrop.Frame f,float scale,float ox,float oy){
  // Only on-screen panels have a real screen position to refract; detached/offscreen views (previews,
  // measurement fixtures) keep the frost path instead of baking the wrong region from (0,0).
  boolean eligible=bakeEnabled&&style!=2&&quality>=IosStyle.BALANCED&&Math.min(box.width(),box.height())>=36*density&&owner.isAttachedToWindow()&&owner.getWindowToken()!=null;
  if(!eligible){if(bakedShader!=null){baked=null;bakedShader=null;invalidateSelf();}return;}
  final float left=position[0]+box.left,top=position[1]+box.top,pw=box.width(),ph=box.height();
  int key=System.identityHashCode(f.bitmap);key=31*key+Math.round(left);key=31*key+Math.round(top);key=31*key+Math.round(pw);key=31*key+Math.round(ph);key=31*key+Math.round(radius);
  if(key==bakeKey&&bakedShader!=null)return;
  bakeKey=key;
  Bitmap hit=GlassBake.cache.get(key);
  if(hit!=null){applyBake(hit);return;}
  baked=null;bakedShader=null;
  if(!GlassBake.begin(key)){retryBake();return;}
  float s=Math.min(1f,360f/Math.max(pw,ph));int ow=Math.max(2,Math.round(pw*s)),oh=Math.max(2,Math.round(ph*s));
  final WeakReference<GlassSurface> reference=new WeakReference<>(this);final int requestKey=key;
  if(!GlassBake.schedule(f.bitmap,scale,ox,oy,left,top,pw,ph,radius,density,depth,ow,oh,key,()->{GlassSurface target=reference.get();return target!=null&&target.bakeKey==requestKey&&target.bakeEnabled;},(result,k)->{GlassSurface target=reference.get();if(target!=null&&k==target.bakeKey&&result!=null&&target.bakeEnabled){target.applyBake(result);target.invalidateSelf();}}))retryBake();
 }
 private void retryBake(){if(bakeRetryQueued)return;bakeRetryQueued=true;final WeakReference<GlassSurface> reference=new WeakReference<>(this);GlassBake.retry(()->{GlassSurface s=reference.get();if(s!=null){s.bakeRetryQueued=false;s.adaptKey=Integer.MIN_VALUE;s.invalidateSelf();}});}
 private void applyBake(Bitmap b){baked=b;bakedShader=new BitmapShader(b,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);protectionDirty=true;contrastQueued=false;}
 /** Drives the appear/disappear transition: 0 = strong lens and heavy blur, 1 = settled. */
 void setReveal(float value){float next=Math.max(0f,Math.min(1f,value));if(next!=reveal){reveal=next;invalidateSelf();}}
 float reveal(){return reveal;}
 /** Screen-coordinate touch feed from the Activity; no event is consumed. */
 void onScreenTouch(float sx,float sy,int action){
  if(!dynSpec||box.isEmpty())return;
  float lx=sx-position[0],ly=sy-position[1],slop=density*24;
  boolean inside=lx>=box.left-slop&&lx<=box.right+slop&&ly>=box.top-slop&&ly<=box.bottom+slop;
  if(action==android.view.MotionEvent.ACTION_UP||action==android.view.MotionEvent.ACTION_CANCEL||!inside){if(touching){touching=false;touchStamp=android.os.SystemClock.uptimeMillis();invalidateSelf();}return;}
  touchX=lx;touchY=ly;touching=true;touchA=Math.min(1f,Math.max(touchA,.01f)+.18f);touchStamp=android.os.SystemClock.uptimeMillis();invalidateSelf();
 }
 private static int scaled(int color,float value){return (Math.min(255,Math.round(Color.alpha(color)*value))<<24)|(color&0xffffff);}
 int foregroundColor(){return light?IosStyle.INK:Color.WHITE;}
 @Override public void draw(Canvas canvas){
  if(box.isEmpty())return;if(fxGen!=GlassPerf.generation()){fxGen=GlassPerf.generation();int eq=GlassPerf.capQuality(appearance==null?baseQuality:appearance.quality);if(eq!=quality){quality=eq;rebuild();}}if(body==null||(backdrop!=null&&revision!=backdrop.revision))rebuild();Drawable.Callback ownerCallback=getCallback();for(int depth=0;depth<4&&ownerCallback instanceof Drawable;depth++)ownerCallback=((Drawable)ownerCallback).getCallback();if(ownerCallback instanceof View)adaptRegion((View)ownerCallback);if(!contrastQueued&&ownerCallback instanceof View){contrastQueued=true;View owner=(View)ownerCallback;final boolean currentLight=light;final int generation=revision;final Drawable.Callback attached=getCallback();owner.post(()->{if(getCallback()==attached&&revision==generation)IosStyle.contrast(owner,currentLight);});}if(!(ownerCallback instanceof View))protectionCount=0;if(ownerCallback instanceof View){View owner=(View)ownerCallback;owner.getLocationOnScreen(position);int geometry=31*(31*geometryHash(owner)+position[0])+position[1];if(protectionDirty||geometry!=protectionGeometry){protectionCount=0;protectionDirty=false;collectProtection(owner,0,0);protectionGeometry=geometry;}}paint.setShader(null);paint.setStyle(Paint.Style.FILL);
  shadow.set(box);shadow.offset(0,density*(1.5f+2.5f*depth));paint.setColor(0x26000000);paint.setAlpha(opacity*(38+Math.round(30*depth))/255);canvas.drawRoundRect(shadow,radius,radius,paint);shadow.offset(0,-density*(.8f+1.5f*depth));paint.setAlpha(opacity*(24+Math.round(22*depth))/255);canvas.drawRoundRect(shadow,radius,radius,paint);
  int save=canvas.save();canvas.clipPath(clip);
  if(bakedShader!=null&&baked!=null&&!baked.isRecycled()){bakedMatrix.setScale(box.width()/baked.getWidth(),box.height()/baked.getHeight());bakedMatrix.postTranslate(box.left,box.top);bakedShader.setLocalMatrix(bakedMatrix);paint.setShader(bakedShader);paint.setColorFilter(externalFilter!=null?externalFilter:light?null:BAKED_DARK_FILTER);paint.setAlpha(opacity);canvas.drawRect(box,paint);paint.setColorFilter(externalFilter);if(frost!=null){if(getCallback() instanceof View){View view=(View)getCallback();view.getLocationOnScreen(position);android.util.DisplayMetrics metrics=view.getResources().getDisplayMetrics();GlassBackdrop.Frame f=backdrop.frame;float sc=Math.max(metrics.widthPixels/(float)f.bitmap.getWidth(),metrics.heightPixels/(float)f.bitmap.getHeight());matrix.setScale(sc,sc);matrix.postTranslate((metrics.widthPixels-f.bitmap.getWidth()*sc)/2-position[0],(metrics.heightPixels-f.bitmap.getHeight()*sc)/2-position[1]);}drawFxFrost(canvas);}}
  else if(frost!=null){GlassBackdrop.Frame f=backdrop.frame;Drawable.Callback callback=getCallback();if(callback instanceof View){View view=(View)callback;view.getLocationOnScreen(position);android.util.DisplayMetrics metrics=view.getResources().getDisplayMetrics();float scale=Math.max(metrics.widthPixels/(float)f.bitmap.getWidth(),metrics.heightPixels/(float)f.bitmap.getHeight());matrix.setScale(scale,scale);matrix.postTranslate((metrics.widthPixels-f.bitmap.getWidth()*scale)/2-position[0],(metrics.heightPixels-f.bitmap.getHeight()*scale)/2-position[1]);}else{float scale=Math.max(box.width()/f.bitmap.getWidth(),box.height()/f.bitmap.getHeight());matrix.setScale(scale,scale);matrix.postTranslate(box.centerX()-f.bitmap.getWidth()*scale/2,box.centerY()-f.bitmap.getHeight()*scale/2);}frost.setLocalMatrix(matrix);paint.setShader(frost);paint.setColorFilter(externalFilter!=null?externalFilter:light?null:IosStyle.DARK_FROST_FILTER);paint.setAlpha(opacity*(style==0||appearance!=null?255:(style==1?125:210))/255);canvas.drawRect(box,paint);paint.setColorFilter(externalFilter);drawFxFrost(canvas);}
  paint.setAlpha(Math.round(opacity*(.35f+.65f*reveal)*(bakedShader!=null?BAKED_BODY:1f)));paint.setShader(body);canvas.drawRect(box,paint);paint.setAlpha(opacity);if(style!=2){paint.setShader(sheen);canvas.drawRect(box,paint);if(quality>0){paint.setShader(reflection);canvas.drawRect(box,paint);}}
  if(style!=2&&quality>0){
   paint.setStyle(Paint.Style.STROKE);paint.setShader(bevel);paint.setStrokeWidth((style==0?2:IosStyle.BEVEL_DP)*density);canvas.drawRoundRect(inner,Math.max(0,radius-2*density),Math.max(0,radius-2*density),paint);
   paint.setShader(specularFade);paint.setStrokeWidth((style==0?(quality==2?1.8f:1.2f):(quality==2?5.5f:3.5f))*density);paint.setStrokeCap(Paint.Cap.ROUND);canvas.drawPath(specular,paint);paint.setStrokeCap(Paint.Cap.BUTT);
  }
  if(materialGloss!=null){paint.setStyle(Paint.Style.FILL);paint.setAlpha(opacity);paint.setShader(materialGloss);canvas.drawRect(box,paint);paint.setShader(materialShadow);canvas.drawRect(box,paint);paint.setShader(null);paint.setColor(Color.WHITE);paint.setAlpha(opacity*118/255);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(1,density));canvas.drawRoundRect(inner,Math.max(0,radius-2*density),Math.max(0,radius-2*density),paint);}
  if(style!=2){paint.setStyle(Paint.Style.FILL);paint.setAlpha(opacity);for(int i=0;i<protectionCount;i++){Protection p=protections.get(i);paint.setShader(p.shader);canvas.drawOval(p.bounds,paint);}}
  drawTouchWash(canvas);
  canvas.restoreToCount(save);
  paint.setShader(edge);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(IosStyle.RIM_OUTER_DP*density);paint.setAlpha(opacity);canvas.drawRoundRect(rim,Math.max(0,radius-density*.5f),Math.max(0,radius-density*.5f),paint);if(style!=2&&quality>0){paint.setShader(specularFade);paint.setStrokeWidth(IosStyle.RIM_INNER_DP*density);canvas.drawRoundRect(inner,Math.max(0,radius-2*density),Math.max(0,radius-2*density),paint);}
  drawTouchRim(canvas);
  paint.setShader(null);
 }
 private void drawFxFrost(Canvas canvas){
  GlassBackdrop.Frame f=backdrop.frame;
  float k=lens&&bakedShader==null?lensK*(reveal<1f?1f+(1f-reveal)*2.5f:1f):0f;
  if(lens&&k>0f&&frostLens!=null){
   lensMatrix.set(matrix);lensMatrix.postScale(1f+k,1f+k,box.centerX(),box.centerY());frostLens.setLocalMatrix(lensMatrix);
   int s=canvas.save();canvas.clipPath(ring);paint.setShader(frostLens);paint.setColorFilter(externalFilter!=null?externalFilter:light?null:IosStyle.DARK_FROST_FILTER);paint.setAlpha(opacity);canvas.drawRect(box,paint);paint.setColorFilter(externalFilter);canvas.restoreToCount(s);
  }
  if(reveal<1f&&frostSoft!=null&&f.soft!=null){
   softMatrix.set(matrix);softMatrix.preScale(f.bitmap.getWidth()/(float)f.soft.getWidth(),f.bitmap.getHeight()/(float)f.soft.getHeight());frostSoft.setLocalMatrix(softMatrix);
   paint.setShader(frostSoft);paint.setColorFilter(externalFilter!=null?externalFilter:light?null:IosStyle.DARK_FROST_FILTER);paint.setAlpha(Math.round(opacity*(1f-reveal)));canvas.drawRect(box,paint);paint.setColorFilter(externalFilter);
  }
 }
 private float touchLevel(){
  if(touchA<=0f||touchShader==null)return 0f;
  if(!touching){long idle=android.os.SystemClock.uptimeMillis()-touchStamp;touchA=Math.max(0f,touchA-idle/260f);touchStamp=android.os.SystemClock.uptimeMillis();if(touchA<=0f)return 0f;invalidateSelf();}
  return touchA; // a held, still finger draws nothing new; only the release fade animates
 }
 private void drawTouchWash(Canvas canvas){
  float a=touchLevel();if(a<=0f)return;
  touchMatrix.setTranslate(touchX,touchY);touchShader.setLocalMatrix(touchMatrix);fxPaint.setShader(touchShader);fxPaint.setStyle(Paint.Style.FILL);fxPaint.setAlpha(Math.round(opacity*a*.22f));canvas.drawRect(box,fxPaint);
 }
 private void drawTouchRim(Canvas canvas){
  if(touchA<=0f||touchShader==null)return;
  touchMatrix.setTranslate(touchX,touchY);touchShader.setLocalMatrix(touchMatrix);fxPaint.setShader(touchShader);fxPaint.setStyle(Paint.Style.STROKE);fxPaint.setStrokeWidth(IosStyle.RIM_OUTER_DP*density*2.4f);fxPaint.setAlpha(Math.round(opacity*touchA*.9f));
  canvas.drawRoundRect(rim,Math.max(0,radius-density*.5f),Math.max(0,radius-density*.5f),fxPaint);
 }
 // Geometry and nine low-resolution samples are cached per material/bounds revision.
 // This protects content locally rather than forcing the complete glass panel opaque.
 private void collectProtection(View view,float x,float y){
  if(!view.isEnabled()||NO_SCRIM.equals(view.getTag()))return;
  if(view.getWidth()==0||view.getHeight()==0){protectionDirty=true;return;}
  boolean text=view instanceof TextView, glyph=view instanceof ControlGlyph||view instanceof IosStyle.Icon;
  if(text){TextView label=(TextView)view;android.text.Layout layout=label.getLayout();if(layout!=null){
   int available=label.getHeight()-label.getExtendedPaddingTop()-label.getExtendedPaddingBottom();int gravity=label.getGravity()&android.view.Gravity.VERTICAL_GRAVITY_MASK;
   float dy=label.getExtendedPaddingTop()+(gravity==android.view.Gravity.CENTER_VERTICAL?Math.max(0,available-layout.getHeight())/2f:gravity==android.view.Gravity.BOTTOM?Math.max(0,available-layout.getHeight()):0);
   for(int line=0;line<layout.getLineCount();line++){
    float left=x+label.getCompoundPaddingLeft()+layout.getLineLeft(line),right=x+label.getCompoundPaddingLeft()+layout.getLineRight(line),top=y+dy+layout.getLineTop(line),bottom=y+dy+layout.getLineBottom(line);
    if(right>left&&bottom<=y+view.getHeight()+1 ){int fg=textInk(label);int alpha=protectionAlpha(left,top,Math.round(right-left),Math.round(bottom-top),fg,4.9);if(alpha>0)addProtection(left,top,right,bottom,fg,alpha);}
   }
  }}else if(glyph){int fg=foregroundColor(),alpha=protectionAlpha(x,y,view.getWidth(),view.getHeight(),fg,3.8);if(alpha>0)addProtection(x,y,x+view.getWidth(),y+view.getHeight(),fg,alpha);}
  if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++){View child=group.getChildAt(i);if(child.getVisibility()!=View.GONE&&(child.getBackground()==null||child instanceof android.widget.Spinner||child instanceof android.widget.Switch))collectProtection(child,x+child.getLeft()+child.getTranslationX()-group.getScrollX(),y+child.getTop()+child.getTranslationY()-group.getScrollY());}}
 }
 private int textInk(TextView text){Integer original=IosStyle.glassText.get(text);int color=original==null?text.getCurrentTextColor():original;return (original!=null||(Color.alpha(color)>128&&Color.luminance(color)>.6))&&light?IosStyle.INK:color;}
 private static final class Protection {
  final RectF bounds=new RectF();final Matrix matrix=new Matrix();Shader shader;int color;
  void update(float l,float t,float r,float b,float padding,int argb){
   float dx=(r-l)*.3f+padding,dy=(b-t)*.6f+padding;bounds.set(l-dx,t-dy,r+dx,b+dy);
   if(shader==null||color!=argb){color=argb;shader=new RadialGradient(0,0,1,new int[]{argb,argb,argb&0xffffff},new float[]{0,.8f,1},Shader.TileMode.CLAMP);}
   matrix.setScale(bounds.width()/2,bounds.height()/2);matrix.postTranslate(bounds.centerX(),bounds.centerY());shader.setLocalMatrix(matrix);
  }
 }
 private void addProtection(float l,float t,float r,float b,int fg,int alpha){
  Protection region;if(protectionCount==protections.size()){region=new Protection();protections.add(region);}else region=protections.get(protectionCount);protectionCount++;
  int rgb=Color.luminance(fg)>.4?0x07101c:0xffffff;region.update(l,t,r,b,10*density,(alpha<<24)|rgb);
 }
 private int geometryHash(View view){int h=31*(31*(31*view.getLeft()+view.getTop())+view.getWidth())+view.getHeight();h=31*h+view.getScrollX();h=31*h+view.getScrollY();h=31*h+(view.isEnabled()?1:0);h=31*h+Float.floatToIntBits(view.getAlpha());h=31*h+Float.floatToIntBits(view.getTranslationX());h=31*h+Float.floatToIntBits(view.getTranslationY());if(view instanceof TextView){android.text.Layout layout=((TextView)view).getLayout();if(layout!=null){h=31*h+layout.getLineCount();for(int i=0;i<layout.getLineCount();i++){h=31*h+Float.floatToIntBits(layout.getLineRight(i));h=31*h+Float.floatToIntBits(layout.getLineLeft(i));}}}if(view instanceof ViewGroup){ViewGroup g=(ViewGroup)view;for(int i=0;i<g.getChildCount();i++){View child=g.getChildAt(i);if(child.getBackground()==null||child instanceof android.widget.Spinner||child instanceof android.widget.Switch)h=31*h+geometryHash(child);}}return h;}
 private static double contrast(int fg,int bg){float a=Color.alpha(fg)/255f;int ink=mix(fg,bg,a);double l1=Color.luminance(ink),l2=Color.luminance(bg);return (Math.max(l1,l2)+.05)/(Math.min(l1,l2)+.05);}
 private int protectionAlpha(float x,float y,int w,int h,int foreground,double target){
  int maximum=0,scrim=Color.luminance(foreground)>.4?0xff07101c:Color.WHITE;
  if(backdrop==null||backdrop.frame.bitmap==null){int worst=Color.luminance(foreground)>.4?Color.WHITE:Color.BLACK;return materialAlpha(foreground,mix(bodyRgb,worst,bodyAlpha/255f),scrim,target);}
  // Baked glass shows its own (brighter, vibrancy-lifted) bitmap with a thinner body: estimate
  // contrast against exactly that, otherwise the scrim is sized for a darker backdrop than is drawn.
  Bitmap bk=bakedShader!=null?baked:null;
  if(bk!=null&&!bk.isRecycled()&&box.width()>0&&box.height()>0){float effective=bodyAlpha/255f*BAKED_BODY;
   for(int row=1;row<=3;row++)for(int col=1;col<=3;col++){
    float sx=x+w*((col-1)*.4f+.1f),sy=y+h*((row-1)*.4f+.1f);
    int px=Math.max(0,Math.min(bk.getWidth()-1,(int)((sx-box.left)/box.width()*bk.getWidth())));
    int py=Math.max(0,Math.min(bk.getHeight()-1,(int)((sy-box.top)/box.height()*bk.getHeight())));
    int substrate=bk.getPixel(px,py);if(!light){float tr=BAKED_DARK_MULTIPLIER/255f;substrate=Color.rgb(Math.round(Color.red(substrate)*tr),Math.round(Color.green(substrate)*tr),Math.round(Color.blue(substrate)*tr));}
    maximum=Math.max(maximum,materialAlpha(foreground,mix(bodyRgb,substrate,effective),scrim,target));
   }return maximum;}
  Bitmap image=backdrop.frame.bitmap;Drawable.Callback owner=getCallback();while(owner instanceof Drawable)owner=((Drawable)owner).getCallback();
  int screenW=Math.max(1,(int)box.width()),screenH=Math.max(1,(int)box.height());
  if(owner instanceof View){View v=(View)owner;v.getLocationOnScreen(position);screenW=v.getResources().getDisplayMetrics().widthPixels;screenH=v.getResources().getDisplayMetrics().heightPixels;}else{position[0]=position[1]=0;}
  float scale=Math.max(screenW/(float)image.getWidth(),screenH/(float)image.getHeight());
  for(int row=1;row<=3;row++)for(int col=1;col<=3;col++){
   int px=Math.max(0,Math.min(image.getWidth()-1,(int)((position[0]+x+w*((col-1)*.4f+.1f)-(screenW-image.getWidth()*scale)/2)/scale)));
   int py=Math.max(0,Math.min(image.getHeight()-1,(int)((position[1]+y+h*((row-1)*.4f+.1f)-(screenH-image.getHeight()*scale)/2)/scale)));
   int substrate=image.getPixel(px,py);if(!light){float transmission=IosStyle.DARK_FROST_MULTIPLIER/255f;substrate=Color.rgb(Math.round(Color.red(substrate)*transmission),Math.round(Color.green(substrate)*transmission),Math.round(Color.blue(substrate)*transmission));}int bg=mix(bodyRgb,substrate,bodyAlpha/255f);
   maximum=Math.max(maximum,materialAlpha(foreground,bg,scrim,target));
  }return maximum;
 }
 private int materialAlpha(int foreground,int bg,int scrim,double target){if(material!=GlassMaterial.IOS26||style==2)return requiredAlpha(foreground,bg,scrim,target);return Math.max(requiredAlpha(foreground,mix(Color.WHITE,bg,.28f),scrim,target),requiredAlpha(foreground,mix(0xff102a40,bg,.23f),scrim,target));}
 private static int requiredAlpha(int foreground,int bg,int scrim,double target){if(Color.luminance(foreground)>.4)bg=mix(Color.WHITE,bg,.08f);if(contrast(foreground,bg)>=target)return 0;int lo=0,hi=160;while(hi-lo>4){int mid=(lo+hi)/2;if(contrast(foreground,mix(scrim,bg,mid/255f))>=target)hi=mid;else lo=mid;}return hi;}
 @Override public void getOutline(Outline outline){outline.setRoundRect((int)box.left,(int)box.top,(int)box.right,(int)box.bottom,radius);outline.setAlpha(style==2?1:.45f);}
 @Override public void setAlpha(int alpha){opacity=Math.max(0,Math.min(255,alpha));invalidateSelf();}
 @Override public void setColorFilter(ColorFilter filter){externalFilter=filter;paint.setColorFilter(filter);invalidateSelf();}
 @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
