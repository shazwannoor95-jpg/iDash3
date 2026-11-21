package com.shazwan.idash3;
import android.app.*;import android.content.*;import android.os.*;import android.view.*;import org.osmdroid.util.GeoPoint;import java.util.*;
/** Real view identity, source selection, map camera and return-path regressions. */
public class NativeDriveFlowChecks extends Instrumentation {
 LauncherActivity a;int n;android.content.SharedPreferences prefs;Map<String,?> saved;
 void check(boolean b,String why){if(!b)throw new AssertionError(why);n++;}
 void ui(Runnable r){Throwable[] t={null};runOnMainSync(()->{try{r.run();}catch(Throwable e){t[0]=e;}});waitForIdleSync();if(t[0]!=null)throw new AssertionError(t[0]);}
 View find(View v,String name){if(name.contentEquals(v.getContentDescription()==null?"":v.getContentDescription()))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View hit=find(((ViewGroup)v).getChildAt(i),name);if(hit!=null)return hit;}return null;}
 void selected(DriveDashboard d,int mode){int count=0;for(int i=0;i<4;i++){if(d.modeButtons[i].isSelected())count++;check(d.modeButtons[i].isSelected()==(i==mode),"correct selected rail source "+i);}check(count==1,"exactly one selected source");}
 void noNetwork(DriveDashboard d){if(d.map!=null)d.map.setUseDataConnection(false);}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle out=new Bundle();int code=Activity.RESULT_CANCELED;try{
 prefs=getTargetContext().getSharedPreferences("idash",0);saved=new HashMap<>(prefs.getAll());prefs.edit().putBoolean("motion",false).putBoolean("dashboard_location",false).putInt("dashboard_panel",0).apply();a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));java.util.concurrent.CountDownLatch ready=new java.util.concurrent.CountDownLatch(2);a.worker.execute(()->a.main.post(ready::countDown));a.networkWorker.execute(()->a.main.post(ready::countDown));check(ready.await(45,java.util.concurrent.TimeUnit.SECONDS),"data ready");
 ui(()->{a.page="drive";a.render();DriveDashboard d=a.driveDashboard;noNetwork(d);selected(d,0);Object map=d.map;find(d,"Dashboard Peta").performClick();check(d.map==map,"selected Map preserves live map object");d.map.getController().setZoom(14.0);d.map.getController().setCenter(new GeoPoint(3.2,101.7));});
 ui(()->{DriveDashboard d=a.driveDashboard;d.captureViewport();check(a.driveViewport!=null&&Math.abs(a.driveViewport.latitude-3.2)<.001,"camera records visible map position");d.setMode(1);selected(d,1);Object media=d.largeMedia;find(d,"Dashboard Media").performClick();check(d.largeMedia==media,"selected Media preserves session bridge");a.onBackPressed();check("drive".equals(a.page)&&d.mode==0,"Back from media returns map inside Drive");noNetwork(d);});
 ui(()->{DriveDashboard d=a.driveDashboard;check(Math.abs(d.map.getMapCenter().getLatitude()-3.2)<.001&&Math.abs(d.map.getZoomLevelDouble()-14)<.01,"return restores camera and zoom");
 d.setMode(3);selected(d,3);Object web=d.web;find(d,"Dashboard YouTube").performClick();check(d.web==web,"selected YouTube preserves browser panel");a.onBackPressed();check(d.mode==0,"Back from idle YouTube returns map");noNetwork(d);
 d.setMode(2);selected(d,2);Object fm=d.fm;find(d,"Dashboard Radio FM").performClick();check(d.fm==fm,"selected FM does not release/rebuild controller");
 find(d,"Dashboard Apps").performClick();check("apps".equals(a.page)&&"drive".equals(a.libraryReturnPage),"Apps remembers Drive origin");find(a.shell,"Done").performClick();check("drive".equals(a.page)&&a.driveDashboard.mode==2,"Done restores Drive and selected FM source");selected(a.driveDashboard,2);
 a.showApps();a.onBackPressed();check("drive".equals(a.page),"Back from Apps restores Drive");a.showApps();find(a.shell,"Apps").performClick();check("drive".equals(a.page),"Apps dock toggle restores origin");
 a.onBackPressed();check("drive".equals(a.page)&&a.driveDashboard.mode==0,"first Back returns map");noNetwork(a.driveDashboard);a.onBackPressed();check("home".equals(a.page),"Back from map returns Home");a.showApps();find(a.shell,"Done").performClick();check("home".equals(a.page),"Home-origin Apps still returns Home");
 a.page="drive";a.render();noNetwork(a.driveDashboard);a.driveDashboard.setMode(1);a.showApps();Bundle state=new Bundle();a.onSaveInstanceState(state);check("drive".equals(state.getString("library_return"))&&state.getBoolean("drive_viewport"),"instance state preserves return origin and camera");
 });
 out.putString("stream","PASS: "+n+" native Drive flow assertions\n");code=Activity.RESULT_OK;
 }catch(Throwable t){out.putString("stream","FAIL after "+n+": "+t+"\n");}finally{if(a!=null)ui(a::finish);if(prefs!=null&&saved!=null)AppearanceChecks.restore(prefs,saved);}finish(code,out);}
}
