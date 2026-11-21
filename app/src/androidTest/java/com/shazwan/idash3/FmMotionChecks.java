package com.shazwan.idash3;
import android.app.*;import android.content.*;import android.os.*;import android.graphics.*;import android.view.*;import android.widget.*;import java.util.*;
/** Compact FM text geometry and real, focused Home/Drive animation frames. */
public class FmMotionChecks extends Instrumentation {
 LauncherActivity a;int n,oldMode;android.content.SharedPreferences p;Map<String,?> saved;
 void check(boolean b,String m){if(!b)throw new AssertionError(m);n++;}
 void ui(Runnable r){Throwable[] e={null};runOnMainSync(()->{try{r.run();}catch(Throwable t){e[0]=t;}});if(e[0]!=null)throw new AssertionError(e[0]);}
 TextView find(View v,String label){if(v instanceof TextView&&label.contentEquals(v.getContentDescription()==null?"":v.getContentDescription()))return (TextView)v;if(v instanceof android.view.ViewGroup)for(int i=0;i<((android.view.ViewGroup)v).getChildCount();i++){TextView x=find(((android.view.ViewGroup)v).getChildAt(i),label);if(x!=null)return x;}return null;}
 Bitmap frame(PrayerJourneyView v){Bitmap b=Bitmap.createBitmap(v.getWidth(),v.getHeight(),Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));return b;}
 void animate(String page)throws Exception{
  ui(()->{p.edit().putBoolean("motion",true).apply();GlassPerf.setMode(a,GlassPerf.FULL);a.page=page;a.panelVisible=false;a.render();a.main.removeCallbacks(a.ticker);if(a.driveDashboard!=null&&a.driveDashboard.map!=null)a.driveDashboard.map.setUseDataConnection(false);});for(int i=0;i<150&&!a.hasWindowFocus();i++)SystemClock.sleep(100);SystemClock.sleep(700);
  PrayerJourneyView w=page.equals("home")?a.prayerJourney:a.driveDashboard.journey;Bitmap[] frames=new Bitmap[2];
  ui(()->{w.tick(System.currentTimeMillis());check(a.hasWindowFocus(),"focused "+page);check(w.phase!=null&&w.moving()&&w.posted,"visible animation scheduled "+page);frames[0]=frame(w);});SystemClock.sleep(700);ui(()->frames[1]=frame(w));
  check(!frames[0].sameAs(frames[1]),"actual changing animation pixels "+page);for(Bitmap b:frames)b.recycle();
  ui(()->{p.edit().putBoolean("motion",false).apply();w.tick(System.currentTimeMillis());check(!w.posted,"motion off cancels "+page);p.edit().putBoolean("motion",true).apply();GlassPerf.setMode(a,GlassPerf.LOW);w.tick(System.currentTimeMillis());check(!w.posted,"LOW cancels "+page);GlassPerf.setMode(a,GlassPerf.FULL);w.setVisibility(View.GONE);w.sync();check(!w.posted,"hidden cancels "+page);w.setVisibility(View.VISIBLE);w.stop();});
 }
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle result=new Bundle();int code=Activity.RESULT_CANCELED;try{
 p=getTargetContext().getSharedPreferences("idash",0);saved=new HashMap<>(p.getAll());oldMode=GlassPerf.mode();p.edit().putBoolean("motion",false).putString("jakim_zone","SGR02").putBoolean("jakim",true).putBoolean("dashboard_location",false).putInt("dashboard_panel",0).apply();
 ActivityMonitor m=addMonitor(LauncherActivity.class.getName(),null,false);getTargetContext().startActivity(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));a=(LauncherActivity)m.waitForActivityWithTimeout(30000);removeMonitor(m);check(a!=null,"activity started");java.util.concurrent.CountDownLatch ready=new java.util.concurrent.CountDownLatch(2);a.worker.execute(()->a.main.post(ready::countDown));a.networkWorker.execute(()->a.main.post(ready::countDown));check(ready.await(45,java.util.concurrent.TimeUnit.SECONDS),"data ready");
 ui(()->{DashFmPanelPremium f=new DashFmPanelPremium(a,a.getResources().getDisplayMetrics().density,()->{});TextView freq=find(f,"Current FM frequency. Tap to enter frequency");for(int[] size:new int[][]{{400,230},{520,260},{600,340}}){f.measure(View.MeasureSpec.makeMeasureSpec(a.dp(size[0]),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(a.dp(size[1]),View.MeasureSpec.EXACTLY));f.layout(0,0,a.dp(size[0]),a.dp(size[1]));check(freq.getHeight()>0,"frequency has height");android.text.Layout l=freq.getLayout();check(l!=null&&l.getLineBottom(0)<=freq.getHeight()-freq.getCompoundPaddingTop()-freq.getCompoundPaddingBottom(),"frequency fully visible at "+size[1]);for(int i=1;i<=10;i++){TextView preset=find(f,"FM channel "+i+"; —; hold to save");check(preset!=null&&preset.getHeight()==a.dp(48)&&preset.getBackground() instanceof android.graphics.drawable.InsetDrawable,"compact preset "+i);}}f.close();});
 animate("home");animate("drive");result.putString("stream","PASS: "+n+" FM/motion assertions\n");code=Activity.RESULT_OK;
 }catch(Throwable t){result.putString("stream","FAIL after "+n+": "+t+"\n");}finally{if(a!=null)ui(()->{GlassPerf.setMode(a,oldMode);a.finish();});if(p!=null&&saved!=null)AppearanceChecks.restore(p,saved);}finish(code,result);}
}
