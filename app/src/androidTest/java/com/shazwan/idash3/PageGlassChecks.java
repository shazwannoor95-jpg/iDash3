package com.shazwan.idash3;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Scoped skin regression: search/paging/favorites, map input, foregrounds and page exit. */
public class PageGlassChecks extends Instrumentation {
 LauncherActivity a;int n;android.content.SharedPreferences prefs;Map<String,?> saved;
 void check(boolean b,String why){if(!b)throw new AssertionError(why);n++;}
 void ui(Runnable work){Throwable[] failure={null};runOnMainSync(()->{try{work.run();}catch(Throwable t){failure[0]=t;}});waitForIdleSync();if(failure[0]!=null)throw new AssertionError(failure[0]);}
 View find(View v,String label){if(label.contentEquals(v.getContentDescription()==null?"":v.getContentDescription()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View hit=find(g.getChildAt(i),label);if(hit!=null)return hit;}}return null;}
 void fixture(String page,int w,int h){
  for(int pass=0;pass<2;pass++){a.stage.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));a.stage.layout(0,0,w,h);}
  Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);a.stage.draw(new Canvas(b));
  try(java.io.OutputStream file=new java.io.FileOutputStream(new java.io.File(a.getFilesDir(),"page-skin-"+page+"-"+w+"x"+h+".png"))){b.compress(Bitmap.CompressFormat.PNG,100,file);}catch(Exception e){throw new AssertionError(e);}finally{b.recycle();}
 }
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle out=new Bundle();int result=Activity.RESULT_CANCELED;try{
  prefs=getTargetContext().getSharedPreferences("idash",0);saved=new HashMap<>(prefs.getAll());prefs.edit().putBoolean("motion",false).putBoolean("dashboard_location",false).putInt("dashboard_panel",0).putString("jakim_zone","SGR02").apply();
  a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  java.util.concurrent.CountDownLatch ready=new java.util.concurrent.CountDownLatch(2);a.worker.execute(()->a.main.post(ready::countDown));a.networkWorker.execute(()->a.main.post(ready::countDown));check(ready.await(45,java.util.concurrent.TimeUnit.SECONDS),"discovery and timetable ready");waitForIdleSync();
  ui(()->{
   String wallpaper=a.prefs.getString("wallpaper","");
   a.page="apps";a.render();check(a.shell.getBackground() instanceof UltraGlassScene,"scenic Apps installed");
   check(a.search.getBackground() instanceof GlassSurface&&a.search.getForeground() instanceof UltraGlassOverlay,"search keeps original glass plus rim");
   check(find(a.shell,"Panel").getForeground() instanceof InsetDrawable,"Apps header styled");
   if(!a.apps.isEmpty()){
    LauncherActivity.App app=a.apps.get(0);a.search.setText(app.label);check(find(a.grid,"Open "+app.label)!=null,"search finds installed app with skin");
    a.assign(1,app);check(app.component.equals(a.prefs.getString("favorite_Files","")),"favorite assignment survives styled re-render");
   }
   a.search.setText("no-app-matches-7654321");check(find(a.grid,"No app results")!=null,"empty search works");a.search.setText("");
   for(int[] size:new int[][]{{1024,600},{800,480}})fixture("apps",size[0],size[1]);
   List<LauncherActivity.App> list=new ArrayList<>();for(int i=0;i<17;i++)list.add(new LauncherActivity.App("Fixture "+i,"test/.App"+i,null));
   ((AppLibraryGrid)a.grid).setApps(list);PageGlassSkin.refreshAppsGrid(a,a.grid);View tile=find(a.grid,"Open Fixture 0");Drawable focus=tile.getBackground();check(focus instanceof StateListDrawable&&tile.getForeground()==null,"tile keeps unobscured focus state");
   find(a.grid,"Next app page").performClick();check(find(a.grid,"Open Fixture 8")!=null,"styled grid pages forward");
   find(a.grid,"Previous app page").performClick();check(find(a.grid,"Open Fixture 0")!=null,"styled grid pages back");
   a.page="drive";a.render();DriveDashboard drive=a.driveDashboard;check(a.shell.getBackground() instanceof UltraGlassScene,"Drive early render path styled");
   check(drive.map!=null,"native map preserved");drive.map.setUseDataConnection(false);Object map=drive.map;
   Drawable railFocus=find(drive,"Dashboard Peta").getBackground();Object marker=drive.marker;
   PageGlassSkin.apply(a,a.shell,a.body,a.page);check(map==drive.map&&marker==drive.marker&&!drive.listening,"reapply preserves map instance and GPS opt-in");
   check(railFocus==find(drive,"Dashboard Peta").getBackground(),"rail input/focus state preserved");
   check(find(drive,"Dashboard calendar").getForeground() instanceof UltraGlassOverlay,"calendar rim installed");
   for(int[] size:new int[][]{{1024,600},{800,480}})fixture("drive",size[0],size[1]);
   find(drive,"Dashboard Radio FM").performClick();check(drive.fm!=null&&drive.map==null,"FM rail click works");
   find(drive,"Dashboard YouTube").performClick();check(drive.web!=null&&drive.fm==null,"YouTube rail click works");
   find(drive,"Dashboard Media").performClick();check(drive.largeMedia!=null&&drive.web==null,"media rail click works");
   find(drive,"Dashboard Home").performClick();check("home".equals(a.page)&&drive.closed,"Home rail click releases Drive");
   check(!(a.shell.getBackground() instanceof UltraGlassScene),"skin removed on Home");check(find(a.shell,"Clock widget").getForeground() instanceof NeonGlassRim,"original Home rim unchanged");
   check(wallpaper.equals(a.prefs.getString("wallpaper","")),"global wallpaper preference unchanged");
   LinearLayout sample=a.column();sample.setBackground(a.glass(0x55ffffff,18));Drawable original=new ColorDrawable(Color.RED);sample.setForeground(original);PageGlassSkin.refreshDriveMain(a,sample);check(original==sample.getForeground(),"non-target refresh does not replace foreground");
   a.page="drive";PageGlassSkin.refreshDriveMain(a,sample);check(original==sample.getForeground(),"existing foreground preserved on target page");a.page="home";
   a.page="settings";a.render();check(!(a.shell.getBackground() instanceof UltraGlassScene),"skin scoped away from Settings");
  });
  out.putString("stream","PASS: "+n+" page skin assertions\n");result=Activity.RESULT_OK;
 }catch(Throwable t){out.putString("stream","FAIL after "+n+": "+t+"\n");}finally{if(a!=null)ui(a::finish);if(prefs!=null&&saved!=null)AppearanceChecks.restore(prefs,saved);}finish(result,out);}
}
