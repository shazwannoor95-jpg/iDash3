package com.shazwan.idash3;

import android.graphics.*;
import android.os.SystemClock;
import android.view.View;
import java.util.*;

/** Small native vector widget: no WebView, bitmap blur, network, or perpetual hidden animator. */
final class PrayerJourneyView extends View {
 final LauncherActivity owner;final Paint ink=new Paint(3);final Path arc=new Path(),segment=new Path(),land=new Path();final PathMeasure measure=new PathMeasure();final Rect visible=new Rect();final float[] point=new float[2];
 TreeMap<Long,PrayerPhase.Event> timeline=new TreeMap<>();Object data;String day="",zone="",message="Pilih zon JAKIM";PrayerPhase phase,previous;long now,transition;double progress;boolean posted,allowedMotion,drawMotion;float opacity=1;Shader backdrop;
 final Runnable frame=()->{posted=false;if(moving()){invalidate();schedule();}};
 PrayerJourneyView(LauncherActivity a){super(a);owner=a;setContentDescription("Perjalanan waktu JAKIM");setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);setOnClickListener(v->a.jakimDialog());setFocusable(true);arc.moveTo(72,49);arc.quadTo(180,-10,288,49);measure.setPath(arc,false);land.moveTo(0,139);land.lineTo(42,117);land.lineTo(90,134);land.lineTo(145,115);land.lineTo(202,137);land.lineTo(260,114);land.lineTo(318,132);land.lineTo(360,119);land.lineTo(360,156);land.lineTo(0,156);land.close();backdrop=new LinearGradient(0,0,360,156,new int[]{0xff093850,0xff171b43,0xff402366},null,Shader.TileMode.CLAMP);}
 void tick(long time){
  now=time;allowedMotion=IosStyle.motion(owner);String z=owner.prefs.getString("jakim_zone",""),date=PrayerPhase.key(time);
  if(data!=owner.jakimDays||!day.equals(date)||!zone.equals(z)){data=owner.jakimDays;day=date;zone=z;Map<String,String[]> days=new HashMap<>();if(z.equals(owner.loadedZone))for(Map.Entry<String,JakimCalendar.Day> e:owner.jakimDays.entrySet())days.put(e.getKey(),e.getValue().times);timeline=PrayerPhase.timeline(days,time);}
  PrayerPhase next=zone.equals(owner.loadedZone)&&owner.jakimDays.containsKey(date)?PrayerPhase.at(timeline,time):null;
  if(next==null||phase==null||next.current.at!=phase.current.at){previous=phase;transition=SystemClock.uptimeMillis();if(next!=null){int[] colors={0xff132d58,0xff624257,0xff135576,0xff683c68,0xff73354f,0xff171a48};backdrop=new LinearGradient(0,0,360,156,new int[]{0xff062338,colors[next.current.kind],0xff292451},null,Shader.TileMode.CLAMP);}}phase=next;progress=phase==null?0:phase.progress(now);
  message=zone.isEmpty()?"Pilih zon JAKIM":owner.jakimLoading?"Memuat jadual…":"Jadual fasa belum lengkap";
  String label=phase==null?message:PrayerPhase.NAMES[phase.current.kind]+" "+phase.current.time+"; menuju "+PrayerPhase.NAMES[phase.next.kind]+" "+phase.next.time+"; "+phase.remaining(now)+" lagi"+(phase.current.kind==1||phase.next.kind==1?". Syuruk ialah penanda, bukan solat fardu":"");
  if(!label.contentEquals(getContentDescription()))setContentDescription(label);invalidate();sync();
 }
 boolean moving(){return owner.resumed&&hasWindowFocus()&&isShown()&&getWindowVisibility()==VISIBLE&&getGlobalVisibleRect(visible)&&allowedMotion&&!GlassPerf.low()&&phase!=null;}
 void schedule(){if(!posted){posted=true;postDelayed(frame,66);}}
 void stop(){removeCallbacks(frame);posted=false;previous=null;}
 void sync(){if(moving())schedule();else stop();}
 @Override protected void onAttachedToWindow(){super.onAttachedToWindow();tick(System.currentTimeMillis());}
 @Override protected void onDetachedFromWindow(){stop();super.onDetachedFromWindow();}
 @Override protected void onVisibilityChanged(View changed,int visibility){super.onVisibilityChanged(changed,visibility);if(owner!=null)sync();}
 @Override public void onVisibilityAggregated(boolean shown){super.onVisibilityAggregated(shown);if(owner!=null){if(shown)sync();else stop();}}
 @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)sync();else stop();}
 void text(Canvas c,String s,float x,float y,float size,int color,boolean bold){ink.setShader(null);ink.setStyle(Paint.Style.FILL);ink.setColor(color);ink.setAlpha(Math.round(Color.alpha(color)*opacity));ink.setTextAlign(Paint.Align.CENTER);ink.setTypeface(bold?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);ink.setTextSize(size);c.drawText(s,x,y,ink);}
 void stroke(int color,float width){ink.setShader(null);ink.setStyle(Paint.Style.STROKE);ink.setStrokeCap(Paint.Cap.ROUND);ink.setStrokeWidth(width);ink.setColor(color);ink.setAlpha(Math.round(Color.alpha(color)*opacity));}
 @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);drawMotion=moving();float scale=Math.min(getWidth()/360f,getHeight()/156f);if(scale<=0)return;int save=canvas.save();canvas.translate((getWidth()-360*scale)/2,(getHeight()-156*scale)/2);canvas.scale(scale,scale);opacity=1;ink.setStyle(Paint.Style.FILL);ink.setShader(backdrop);ink.setAlpha(255);canvas.drawRoundRect(0,0,360,156,18,18,ink);ink.setShader(null);ink.setColor(0x553683bd);canvas.drawPath(land,ink);if(phase!=null&&(phase.current.kind==0||phase.current.kind==5)){ink.setColor(0x88e0eeff);for(int i=0;i<6;i++){float x=89+i*36,y=13+(i%3)*6;canvas.drawCircle(x,y,drawMotion?1+.15f*(float)Math.sin(SystemClock.uptimeMillis()/1700d+i):1,ink);}}
  ink.setColor(0x55202550);canvas.drawRect(163,132,197,156,ink);canvas.drawArc(163,116,197,148,180,180,true,ink);canvas.drawRect(155,128,159,156,ink);canvas.drawRect(201,128,205,156,ink);ink.setColor(0x66ffcf83);canvas.drawRoundRect(176,141,184,156,4,4,ink);
  stroke(0x887caeff,1);canvas.drawRoundRect(1,1,359,155,18,18,ink);
  if(phase==null){text(canvas,message,180,70,16,Color.WHITE,true);text(canvas,"JAKIM · "+zone+" · tekan untuk jadual",180,99,11,0xffd1d8ed,false);}
  else {float fade=drawMotion?Math.min(1,(SystemClock.uptimeMillis()-transition)/450f):1;if(previous!=null&&fade<1){opacity=1-fade;drawPhase(canvas,previous,false);}opacity=fade;drawPhase(canvas,phase,true);opacity=1;}
  canvas.restoreToCount(save);
 }
 void drawPhase(Canvas c,PrayerPhase p,boolean current){
  int active=p.current.kind;float fraction=current?(float)progress:1;float pulse=drawMotion?(.65f+.35f*(float)Math.sin(SystemClock.uptimeMillis()/1500d)):1;
  stroke(0x887e83b0,3);c.drawPath(arc,ink);segment.reset();measure.getSegment(0,measure.getLength()*fraction,segment,true);stroke(0x2236dfff,12);c.drawPath(segment,ink);stroke(0xffc3faff,3);c.drawPath(segment,ink);
  measure.getPosTan(measure.getLength()*fraction,point,null);stroke(0x444eeaff,12*pulse);c.drawCircle(point[0],point[1],4,ink);stroke(Color.WHITE,2);c.drawCircle(point[0],point[1],4,ink);
  if(drawMotion&&current){float t=(SystemClock.uptimeMillis()%14000)/14000f;measure.getPosTan(measure.getLength()*fraction*t,point,null);stroke(0x99ffffff,3);c.drawCircle(point[0],point[1],1.5f,ink);}
  icon(c,active,37,29,14,true);icon(c,p.next.kind,323,29,14,false);
  text(c,PrayerPhase.NAMES[active],40,61,13,Color.WHITE,true);text(c,p.current.time,40,79,14,0xff76efff,true);text(c,PrayerPhase.NAMES[p.next.kind],320,61,13,Color.WHITE,true);text(c,p.next.time,320,79,14,0xff75f1b5,true);
  stroke(0xaaadbaff,1);c.drawRoundRect(99,65,261,111,20,20,ink);text(c,"MENUJU "+PrayerPhase.NAMES[p.next.kind].toUpperCase(java.util.Locale.ROOT),180,80,10,0xffcdd8ff,true);if(owner.prefs.getBoolean("prayer_countdown",true))text(c,p.remaining(now),180,103,23,Color.WHITE,true);
  text(c,"JAKIM · "+zone+(active==1||p.next.kind==1?" · Syuruk: penanda":""),180,126,10,0xffcbd5ef,false);
  for(int i=0;i<6;i++){float x=30+60*i;icon(c,i,x,138,4,i==active);text(c,PrayerPhase.NAMES[i],x,151,8,i==active?0xff9ef5ff:0xffb5bdd6,i==active);}
 }
 void icon(Canvas c,int kind,float x,float y,float r,boolean active){int save=c.save();float bob=active&&drawMotion?(float)Math.sin(SystemClock.uptimeMillis()/2100d)*.7f:0;c.translate(x,y+bob);stroke(active?0xffa4f5ff:0xffeeeeff,Math.max(1,r/9));if(kind==5){c.drawArc(-r*.65f,-r*.7f,r*.65f,r*.6f,180,180,false,ink);c.drawRect(-r*.65f,0,r*.65f,r*.8f,ink);c.drawLine(-r,-r*.5f,-r,r*.8f,ink);c.drawLine(r,-r*.5f,r,r*.8f,ink);c.drawArc(-r*.25f,-r*1.35f,r*.25f,-r*.85f,45,270,false,ink);}else if(kind==0){c.drawArc(-r,-r,r,r,55,270,false,ink);c.drawArc(-r*.4f,-r,r*1.4f,r,90,150,false,ink);c.drawLine(r*.8f,-r,r*1.3f,-r,ink);c.drawLine(r*1.05f,-r*1.25f,r*1.05f,-r*.75f,ink);}else {if(kind==1||kind==4){c.drawArc(-r,-r,r,r,180,180,false,ink);c.drawLine(-r*1.3f,0,r*1.3f,0,ink);}else c.drawCircle(0,0,r*.6f,ink);for(int i=0;i<8;i++){double a=i*Math.PI/4;if((kind==1||kind==4)&&Math.sin(a)>0)continue;c.drawLine((float)Math.cos(a)*r*.85f,(float)Math.sin(a)*r*.85f,(float)Math.cos(a)*r*1.2f,(float)Math.sin(a)*r*1.2f,ink);}if(kind==3){c.drawArc(-r*.8f,0,r*.7f,r,180,180,false,ink);c.drawLine(-r*.8f,r*.5f,r,r*.5f,ink);}}c.restoreToCount(save);}
}
