package com.shazwan.idash3;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;
import java.util.*;

/** Original-style image artwork with dynamic text, arc, and bounded native animation overlays. */
final class PrayerJourneyView extends View {
 final Paint glint=new Paint(3);final Matrix glintMatrix=new Matrix();final Shader glintShader=new LinearGradient(0,0,65,0,new int[]{0x00ffffff,0x33ffffff,0x00ffffff},null,Shader.TileMode.CLAMP);
 final LauncherActivity owner;final Paint ink=new Paint(3),imageInk=new Paint(3|Paint.FILTER_BITMAP_FLAG);final Path segment=new Path();final Rect visible=new Rect(),sunSource=new Rect();final RectF sunBounds=new RectF();final float[] point=new float[2];
 TreeMap<Long,PrayerPhase.Event> timeline=new TreeMap<>();Object data;String day="",zone="",message="Pilih zon JAKIM";PrayerPhase phase,previous;PrayerArt art,previousArt;long now,transition;double progress;boolean posted,allowedMotion,drawMotion;float opacity=1;
 final Runnable frame=()->{posted=false;if(moving()){invalidate();schedule();}};
 PrayerJourneyView(LauncherActivity a){super(a);owner=a;setContentDescription("Perjalanan waktu JAKIM");setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);setOnClickListener(v->a.jakimDialog());setFocusable(true);}
 void tick(long time){
  now=time;allowedMotion=IosStyle.motion(owner);String z=owner.prefs.getString("jakim_zone",""),date=PrayerPhase.key(time);
  if(data!=owner.jakimDays||!day.equals(date)||!zone.equals(z)){data=owner.jakimDays;day=date;zone=z;Map<String,String[]> days=new HashMap<>();if(z.equals(owner.loadedZone))for(Map.Entry<String,JakimCalendar.Day> e:owner.jakimDays.entrySet())days.put(e.getKey(),e.getValue().times);timeline=PrayerPhase.timeline(days,time);}
  PrayerPhase next=zone.equals(owner.loadedZone)&&owner.jakimDays.containsKey(date)?PrayerPhase.at(timeline,time):null;
  if(next==null||phase==null||next.current.at!=phase.current.at){previous=phase;previousArt=art;transition=SystemClock.uptimeMillis();}
  phase=next;progress=phase==null?0:phase.progress(now);int scene=phase==null?0:PrayerArtLayout.scene(phase.current.kind);
  if(art==null||art.index!=scene){art=PrayerArt.load(owner,scene);if(getParent() instanceof View&&((View)getParent()).getParent() instanceof HomeWidgets)requestLayout();}
  message=zone.isEmpty()?"Pilih zon JAKIM":owner.jakimLoading?"Memuat jadual…":"Jadual fasa belum lengkap";
  String label=phase==null?message:PrayerPhase.NAMES[phase.current.kind]+" "+phase.current.time+"; menuju "+PrayerPhase.NAMES[phase.next.kind]+" "+phase.next.time+"; "+phase.remaining(now)+" lagi"+(phase.current.kind==1||phase.next.kind==1?". Syuruk ialah penanda, bukan solat fardu":"");
  if(!label.contentEquals(getContentDescription()))setContentDescription(label);invalidate();sync();
 }
 boolean moving(){return owner.resumed&&hasWindowFocus()&&isShown()&&getWindowVisibility()==VISIBLE&&getGlobalVisibleRect(visible)&&allowedMotion&&!GlassPerf.low()&&phase!=null;}
 void schedule(){if(!posted){posted=true;postDelayed(frame,66);}}
 void stop(){removeCallbacks(frame);posted=false;previous=null;previousArt=null;}
 void sync(){if(moving())schedule();else stop();}
 @Override protected void onAttachedToWindow(){super.onAttachedToWindow();tick(System.currentTimeMillis());}
 @Override protected void onDetachedFromWindow(){stop();art=null;previousArt=null;super.onDetachedFromWindow();}
 @Override protected void onVisibilityChanged(View changed,int visibility){super.onVisibilityChanged(changed,visibility);if(owner!=null)sync();}
 @Override public void onVisibilityAggregated(boolean shown){super.onVisibilityAggregated(shown);if(owner!=null){if(shown)sync();else stop();}}
 @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)sync();else stop();}
 void text(Canvas c,String value,float x,float y,float size,int color){
  ink.setShader(null);ink.setStyle(Paint.Style.FILL);ink.setColor(color);ink.setAlpha(Math.round(Color.alpha(color)*opacity));ink.setTextAlign(Paint.Align.CENTER);ink.setTypeface(Typeface.DEFAULT_BOLD);ink.setTextSize(size);ink.setShadowLayer(2,0,1,0xaa000b20);c.drawText(value,x,y,ink);ink.clearShadowLayer();
 }
 void stroke(int color,float width){ink.setShader(null);ink.setStyle(Paint.Style.STROKE);ink.setStrokeCap(Paint.Cap.ROUND);ink.setStrokeWidth(width);ink.setColor(color);ink.setAlpha(Math.round(Color.alpha(color)*opacity));}
 @Override protected void onDraw(Canvas canvas){
  super.onDraw(canvas);drawMotion=moving();if(art==null){opacity=1;text(canvas,"Visual JAKIM tidak tersedia",getWidth()/2f,getHeight()/2f,14*getResources().getDisplayMetrics().scaledDensity,Color.WHITE);return;}
  float fade=drawMotion?Math.min(1,(SystemClock.uptimeMillis()-transition)/450f):1;
  if(previousArt!=null&&previous!=null&&fade<1){opacity=1;drawScene(canvas,previousArt,previous,false);opacity=fade;drawScene(canvas,art,phase,true);}else{opacity=1;drawScene(canvas,art,phase,true);previousArt=null;previous=null;}
  opacity=1;
 }
 void drawScene(Canvas canvas,PrayerArt scene,PrayerPhase p,boolean current){
  float scale=Math.min(getWidth()/scene.crop.width(),getHeight()/scene.crop.height());int save=canvas.save();canvas.translate((getWidth()-scene.crop.width()*scale)/2,(getHeight()-scene.crop.height()*scale)/2);canvas.scale(scale,scale);canvas.translate(-scene.crop.left,-scene.crop.top);canvas.clipPath(scene.cutout);imageInk.setAlpha(Math.round(255*opacity));canvas.drawBitmap(scene.bitmap,scene.source,scene.bounds,imageInk);
  if(p==null){text(canvas,message,500,scene.countY,30,Color.WHITE);text(canvas,"JAKIM · "+zone+" · tekan untuk jadual",500,scene.footerY,17,0xffdce9ff);canvas.restoreToCount(save);return;}
  PrayerPhase.Event left=p.current,right=p.next;
  if(scene.index==0){if(p.current.kind==0){Map.Entry<Long,PrayerPhase.Event> e=timeline.higherEntry(p.next.at);if(e!=null&&e.getValue().kind==2)right=e.getValue();}else{Map.Entry<Long,PrayerPhase.Event> e=timeline.lowerEntry(p.current.at);if(e!=null&&e.getValue().kind==0)left=e.getValue();}}
  float fraction=PrayerArtLayout.fraction(p.current.kind,current?progress:1);
  stroke(0x779fb8ef,8);canvas.drawPath(scene.arc,ink);segment.reset();scene.measure.getSegment(0,scene.measure.getLength()*fraction,segment,true);stroke(0x3331cfff,24);canvas.drawPath(segment,ink);stroke(0x9944dcff,13);canvas.drawPath(segment,ink);stroke(0xfff4ffff,8);canvas.drawPath(segment,ink);
  node(canvas,scene,0,0xff7cecff);node(canvas,scene,1,0xffeeeeff);
  if(scene.index==0){node(canvas,scene,.5f,0xffffdc77);text(canvas,"Syuruk",500,.378f*scene.height,31,Color.WHITE);PrayerPhase.Event syuruk=p.current.kind==1?p.current:p.next;text(canvas,syuruk.time,500,.45f*scene.height,34,0xffffcd36);}
  scene.measure.getPosTan(scene.measure.getLength()*fraction,point,null);float pulse=drawMotion?(.8f+.2f*(float)Math.sin(SystemClock.uptimeMillis()/1500d)):1;stroke(0x5538ddff,24*pulse);canvas.drawCircle(point[0],point[1],6,ink);stroke(Color.WHITE,5);canvas.drawCircle(point[0],point[1],6,ink);
  if(drawMotion&&current){float t=(SystemClock.uptimeMillis()%12000)/12000f;for(int j=3;j>=0;j--){float distance=Math.max(0,scene.measure.getLength()*fraction*t-j*scene.measure.getLength()*.018f);scene.measure.getPosTan(distance,point,null);stroke(j==0?Color.WHITE:0x668fefff,j==0?6:4);canvas.drawCircle(point[0],point[1],j==0?3:2,ink);}animateIcon(canvas,scene,p.current.kind);ambient(canvas,scene,p.current.kind);}
  text(canvas,PrayerPhase.NAMES[left.kind],scene.leftX,scene.nameY,scene.index==4?30:33,Color.WHITE);text(canvas,PrayerPhase.NAMES[right.kind],scene.rightX,scene.nameY,scene.index==4?30:33,Color.WHITE);
  text(canvas,left.time,scene.leftX,scene.timeY,40,scene.index==3?0xffffd72a:0xff00f0ff);text(canvas,right.time,scene.rightX,scene.timeY,40,0xff1aff77);
  text(canvas,"MENUJU "+PrayerPhase.NAMES[p.next.kind].toUpperCase(java.util.Locale.ROOT),500,scene.headingY,22,0xffe9edff);if(owner.prefs.getBoolean("prayer_countdown",true))text(canvas,p.remaining(now),500,scene.countY,58,Color.WHITE);
  text(canvas,"JAKIM · "+zone+(scene.index==0?" · Syuruk: penanda tambahan":""),500,scene.footerY,24,0xffdce9ff);canvas.restoreToCount(save);
 }
 void node(Canvas canvas,PrayerArt scene,float position,int color){scene.measure.getPosTan(scene.measure.getLength()*position,point,null);stroke((color&0x00ffffff)|0x55000000,18);canvas.drawCircle(point[0],point[1],13,ink);stroke(color,5);canvas.drawCircle(point[0],point[1],13,ink);}
 void ambient(Canvas canvas,PrayerArt scene,int kind){
  int save=canvas.save();canvas.clipPath(scene.pillClip);float t=(SystemClock.uptimeMillis()%11000)/11000f;glintMatrix.setTranslate(265+t*470,0);glintShader.setLocalMatrix(glintMatrix);glint.setShader(glintShader);glint.setAlpha(Math.round(255*opacity));canvas.drawRect(scene.bounds,glint);canvas.restoreToCount(save);
  if(kind==0||kind==4||kind==5)for(int i=0;i<4;i++){float wave=(float)(.5+.5*Math.sin(SystemClock.uptimeMillis()/1400d+i*1.7));float x=(.57f+.047f*i)*1000,y=(.16f+.022f*(i%2))*scene.height;stroke(0x99eaffff,1);float r=1+wave*2;canvas.drawLine(x-r,y,x+r,y,ink);canvas.drawLine(x,y-r,x,y+r,ink);}
 }
 void animateIcon(Canvas canvas,PrayerArt scene,int kind){
  Rect source=scene.icon;RectF dest=scene.iconBounds;
  if(kind==1){sunSource.set(Math.round(scene.bitmap.getWidth()*.30f),Math.round(scene.bitmap.getHeight()*.09f),Math.round(scene.bitmap.getWidth()*.70f),Math.round(scene.bitmap.getHeight()*.29f));sunBounds.set(300,.09f*scene.height,700,.29f*scene.height);source=sunSource;dest=sunBounds;}
  int save=canvas.save();canvas.clipRect(dest);float wave=(float)Math.sin(SystemClock.uptimeMillis()/2100d);canvas.scale(1.012f,1.012f,dest.centerX(),dest.centerY());canvas.translate(0,wave*2.5f);imageInk.setAlpha(Math.round(opacity*(52+20*wave)));canvas.drawBitmap(scene.bitmap,source,dest,imageInk);canvas.restoreToCount(save);
 }
}
