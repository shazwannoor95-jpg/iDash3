package com.shazwan.idash3;
import android.app.*;import android.content.*;import android.os.*;import android.graphics.*;import android.graphics.drawable.*;import android.view.*;import java.util.*;
/** Real API27 loading, lifecycle/visibility and no-stacking checks for the supplied ULTRA kit. */
public class UltraGlassChecks extends Instrumentation {
 LauncherActivity a;int n;android.content.SharedPreferences prefs;Map<String,?> saved;
 void check(boolean b,String why){if(!b)throw new AssertionError(why);n++;}
 void ui(Runnable r){Throwable[] t={null};runOnMainSync(()->{try{r.run();}catch(Throwable e){t[0]=e;}});if(t[0]!=null)throw new AssertionError(t[0]);}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle out=new Bundle();int code=Activity.RESULT_CANCELED;try{
 prefs=getTargetContext().getSharedPreferences("idash",0);saved=new HashMap<>(prefs.getAll());prefs.edit().putBoolean("motion",false).putInt("dashboard_panel",0).putBoolean("dashboard_location",false).apply();a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));java.util.concurrent.CountDownLatch ready=new java.util.concurrent.CountDownLatch(2);a.worker.execute(()->a.main.post(ready::countDown));a.networkWorker.execute(()->a.main.post(ready::countDown));check(ready.await(45,java.util.concurrent.TimeUnit.SECONDS),"discovery ready");
 for(int i=0;i<100&&!a.hasWindowFocus();i++)SystemClock.sleep(100);check(a.hasWindowFocus(),"activity has real window focus for animation test");
 ui(()->{prefs.edit().putBoolean("motion",true).apply();a.page="apps";a.render();UltraGlassSkin.resume(a);check(a.shell.getBackground() instanceof UltraGlassScene,"new scene installed");UltraGlassSkin.Session s=UltraGlassSkin.session(a);check(s!=null&&s.playing,"focused resumed session runs");check(a.search.getForeground() instanceof UltraGlassOverlay,"search has single ULTRA overlay");
  View sample=new View(a);sample.setBackground(a.glass(0x55ffffff,18));Drawable original=new ColorDrawable(Color.RED);sample.setForeground(original);UltraGlassSkin.afterAppsFiltered(a,sample);check(sample.getForeground() instanceof LayerDrawable,"custom original foreground retained as layer");LayerDrawable layers=(LayerDrawable)sample.getForeground();check(layers.getDrawable(layers.getNumberOfLayers()-1)==original,"focus/ripple remains topmost");int count=s.decorated.size();for(int i=0;i<10;i++)UltraGlassSkin.afterAppsFiltered(a,sample);check(s.decorated.size()==count&&sample.getForeground()==layers,"refresh does not stack or duplicate bindings");
  UltraGlassOverlay search=(UltraGlassOverlay)a.search.getForeground();a.search.setVisibility(View.VISIBLE);s.run();check(search.isRunning(),"visible attached widget animates");a.search.setVisibility(View.GONE);s.run();check(!search.isRunning(),"hidden widget stops");
  UltraGlassSkin.apply(a,a.shell,a.page);check(s.disposed&&!s.playing,"reapply disposes old callback session");UltraGlassSkin.Session replacement=UltraGlassSkin.session(a);check(replacement!=s&&replacement.decorated.size()>0,"replacement session present");
  prefs.edit().putBoolean("motion",false).apply();UltraGlassSkin.resume(a);check(!replacement.playing,"Smooth Transitions off suppresses animation");prefs.edit().putBoolean("motion",true).apply();UltraGlassSkin.resume(a);check(replacement.playing,"motion may resume when enabled");
  a.onWindowFocusChanged(false);check(!replacement.playing,"focus loss pauses session");a.onWindowFocusChanged(true);check(replacement.playing,"focus regain resumes session");
  callActivityOnPause(a);check(!replacement.playing&&!a.resumed,"Activity pause stops callbacks");callActivityOnResume(a);check(replacement.playing&&a.resumed,"Activity resume restarts callbacks");
 });
 UltraGlassSkin.Session[] session={null};long[] frames={0};ui(()->{session[0]=UltraGlassSkin.session(a);frames[0]=session[0].frames;});SystemClock.sleep(150);ui(()->check(session[0].frames>frames[0],"single session ticker actually advances (not presentation FPS)"));
 ui(()->{
  a.page="drive";a.render();check(session[0].disposed&&!session[0].playing,"page change stops old Apps session");check(a.driveDashboard.map!=null&&!a.driveDashboard.listening,"real map remains, no GPS opt-in");a.driveDashboard.map.setUseDataConnection(false);Object map=a.driveDashboard.map;UltraGlassSkin.apply(a,a.shell,a.page);check(map==a.driveDashboard.map,"reapply preserves live map object");
  UltraGlassSkin.Session drive=UltraGlassSkin.session(a);a.page="home";a.render();check(drive.disposed&&UltraGlassSkin.session(a)==null,"Home releases skin completely");check(!(a.shell.getBackground() instanceof UltraGlassScene),"Home appearance is not skinned");
  UltraGlassOverlay fx=new UltraGlassOverlay(1,18);Bitmap b=Bitmap.createBitmap(240,140,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);fx.setBounds(20,20,220,120);fx.draw(c);check(b.getPixel(3,3)==0&&Color.alpha(b.getPixel(100,25))>0,"nonzero drawable bounds correctly positioned");b.eraseColor(0);fx.setAlpha(0);fx.draw(c);check(b.getPixel(100,25)==0,"overlay alpha respected");fx.setAlpha(255);fx.setBounds(0,0,2,2);fx.draw(c);check(b.getPixel(1,1)==0,"tiny resized drawable has no stale shader output");
  UltraGlassBlurPreview preview=new UltraGlassBlurPreview(a,b,8,12);check(preview.getParent()==null,"optional blur remains absent on API27");b.recycle();
 });
 out.putString("stream","PASS: "+n+" ULTRA skin assertions; AGSL/RenderEffect modern path not exercised on API27\n");code=Activity.RESULT_OK;
 }catch(Throwable t){out.putString("stream","FAIL after "+n+": "+t+"\n");}finally{if(a!=null)ui(a::finish);if(prefs!=null&&saved!=null)AppearanceChecks.restore(prefs,saved);}finish(code,out);}
}
