package com.shazwan.idash3;
import android.app.*;
import android.content.*;
import android.os.*;
import java.util.concurrent.*;
import java.lang.reflect.*;
/** Exercises isolation and stale asynchronous results without changing glass settings. */
public class CrashSafetyChecks extends Instrumentation {
 LauncherActivity a;java.util.Map<String,?> savedPrefs;int checks;final CountDownLatch releaseNetwork=new CountDownLatch(1);
 void check(boolean value,String why){if(!value)throw new AssertionError(why);checks++;}
 void ui(Runnable r){Throwable[] error={null};runOnMainSync(()->{try{r.run();}catch(Throwable t){error[0]=t;}});waitForIdleSync();if(error[0]!=null)throw new AssertionError(error[0]);}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle out=new Bundle();int result=Activity.RESULT_CANCELED;
 try{
  a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  CountDownLatch loaded=new CountDownLatch(1);a.worker.execute(()->a.main.post(loaded::countDown));check(loaded.await(45,TimeUnit.SECONDS),"initial discovery");waitForIdleSync();
  CountDownLatch networkStarted=new CountDownLatch(1);a.networkWorker.execute(()->{networkStarted.countDown();try{releaseNetwork.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}});
  check(networkStarted.await(45,TimeUnit.SECONDS),"network blocker started");CountDownLatch local=new CountDownLatch(1);a.worker.execute(local::countDown);check(local.await(15,TimeUnit.SECONDS),"local image/discovery worker progresses while network blocked");
  final android.view.View[] stage={null};ui(()->{a.page="settings";a.settingsSection="";a.render();stage[0]=a.stage;a.loadApps();a.loadApps();});
  CountDownLatch drained=new CountDownLatch(1);a.worker.execute(()->a.main.post(drained::countDown));check(drained.await(45,TimeUnit.SECONDS),"refresh completed");waitForIdleSync();
  ui(()->check(a.stage==stage[0],"app refresh does not rebuild Settings"));
  releaseNetwork.countDown();
  out.putString("stream","PASS: "+checks+" crash/isolation assertions\n");result=Activity.RESULT_OK;
 }catch(Throwable error){out.putString("stream","FAIL after "+checks+": "+error+"\n");}
 finally{releaseNetwork.countDown();if(a!=null)ui(()->{if(savedPrefs!=null)AppearanceChecks.restore(a.prefs,savedPrefs);a.finish();});}
 finish(result,out);
 }
}
