package com.shazwan.idash3;
import android.app.*;import android.content.*;import android.os.*;import android.graphics.*;import java.util.*;
/** Settled native-window captures, separate from synchronous geometry fixtures. */
public class ToolbarPreview extends Instrumentation {
 LauncherActivity a;
 void shot(String name)throws Exception{waitForIdleSync();android.os.SystemClock.sleep(1000);Bitmap b=getUiAutomation().takeScreenshot();if(b==null)throw new AssertionError("screenshot unavailable");try(java.io.OutputStream out=new java.io.FileOutputStream(new java.io.File(a.getFilesDir(),name+".png"))){b.compress(Bitmap.CompressFormat.PNG,100,out);}finally{b.recycle();}}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle out=new Bundle();int code=Activity.RESULT_CANCELED;android.content.SharedPreferences p=getTargetContext().getSharedPreferences("idash",0);Map<String,?> saved=new HashMap<>(p.getAll());try{
 p.edit().putBoolean("motion",false).putInt("dock_size",0).putString("jakim_zone","SGR02").apply();a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));java.util.concurrent.CountDownLatch ready=new java.util.concurrent.CountDownLatch(2);a.worker.execute(()->a.main.post(ready::countDown));a.networkWorker.execute(()->a.main.post(ready::countDown));if(!ready.await(45,java.util.concurrent.TimeUnit.SECONDS))throw new AssertionError("data barrier");
 runOnMainSync(()->{a.page="apps";a.query="";a.render();});shot("apps-native-1024x600");
 runOnMainSync(()->{p.edit().putInt("dashboard_panel",0).putBoolean("dashboard_location",false).apply();a.page="drive";a.render();if(a.driveDashboard.map!=null)a.driveDashboard.map.setUseDataConnection(false);});shot("drive-native-1024x600");
 runOnMainSync(()->{a.page="home";a.panelVisible=false;a.render();});shot("home-native-1024x600");
 runOnMainSync(()->{p.edit().putInt("panel_mode",1).apply();a.page="home";a.panelVisible=true;a.render();});shot("home-panel-native-1024x600");out.putString("stream","PASS: captured Apps and Home FM panel native windows\n");code=Activity.RESULT_OK;
 }catch(Throwable t){out.putString("stream","FAIL: "+t+"\n");}finally{if(a!=null)runOnMainSync(a::finish);AppearanceChecks.restore(p,saved);}finish(code,out);}
}
