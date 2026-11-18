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
 static class TestService extends DriveBarService {void attach(Context c){attachBaseContext(c);}}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle out=new Bundle();int result=Activity.RESULT_CANCELED;TestService service=null;CountDownLatch releaseDrive=new CountDownLatch(1);
 try{
  a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  CountDownLatch loaded=new CountDownLatch(1);a.worker.execute(()->a.main.post(loaded::countDown));check(loaded.await(45,TimeUnit.SECONDS),"initial discovery");waitForIdleSync();
  CountDownLatch networkStarted=new CountDownLatch(1);a.networkWorker.execute(()->{networkStarted.countDown();try{releaseNetwork.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}});
  check(networkStarted.await(45,TimeUnit.SECONDS),"network blocker started");CountDownLatch local=new CountDownLatch(1);a.worker.execute(local::countDown);check(local.await(15,TimeUnit.SECONDS),"local image/discovery worker progresses while network blocked");
  final android.view.View[] stage={null};ui(()->{a.page="settings";a.settingsSection="";a.render();stage[0]=a.stage;a.loadApps();a.loadApps();});
  CountDownLatch drained=new CountDownLatch(1);a.worker.execute(()->a.main.post(drained::countDown));check(drained.await(45,TimeUnit.SECONDS),"refresh completed");waitForIdleSync();
  ui(()->check(a.stage==stage[0],"app refresh does not rebuild Settings"));
  releaseNetwork.countDown();
  savedPrefs=new java.util.HashMap<>(a.prefs.getAll());ui(()->a.prefs.edit().putString(DriveController.PREF_MAP,"missing.maps/.Main").apply());
  service=new TestService();service.attach(getTargetContext());TestService target=service;
  Field loaderField=DriveBarService.class.getDeclaredField("loader");loaderField.setAccessible(true);ExecutorService loader=(ExecutorService)loaderField.get(target);
  CountDownLatch driveStarted=new CountDownLatch(1);loader.execute(()->{driveStarted.countDown();try{releaseDrive.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}});check(driveStarted.await(5,TimeUnit.SECONDS),"drive blocker started");
  Method load=DriveBarService.class.getDeclaredMethod("loadApps",Runnable.class);load.setAccessible(true);CountDownLatch ready=new CountDownLatch(1),pulse=new CountDownLatch(1);
  ui(()->{try{load.invoke(target,(Runnable)ready::countDown);}catch(Exception e){throw new AssertionError(e);}new Handler(Looper.getMainLooper()).post(pulse::countDown);});
  check(pulse.await(15,TimeUnit.SECONDS)&&ready.getCount()==1,"Drive scan does not block main thread");
  releaseDrive.countDown();check(ready.await(45,TimeUnit.SECONDS),"Drive scan completes");Field shortcutField=DriveBarService.class.getDeclaredField("shortcutIcons");shortcutField.setAccessible(true);check(!((java.util.Map<?,?>)shortcutField.get(target)).containsKey("missing.maps/missing.maps.Main"),"missing shortcut cannot be revived by pack artwork");
  CountDownLatch late=new CountDownLatch(1),gate=new CountDownLatch(1);loader.execute(()->{try{gate.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}});
  ui(()->{try{load.invoke(target,(Runnable)late::countDown);}catch(Exception e){throw new AssertionError(e);}target.onDestroy();});gate.countDown();check(loader.awaitTermination(10,TimeUnit.SECONDS),"Drive loader stops on destroy");waitForIdleSync();check(late.getCount()==1,"destroyed Drive ignores queued result");service=null;
  out.putString("stream","PASS: "+checks+" crash/isolation assertions\n");result=Activity.RESULT_OK;
 }catch(Throwable error){out.putString("stream","FAIL after "+checks+": "+error+"\n");}
 finally{releaseNetwork.countDown();releaseDrive.countDown();if(service!=null){TestService s=service;ui(s::onDestroy);}if(a!=null)ui(()->{if(savedPrefs!=null)AppearanceChecks.restore(a.prefs,savedPrefs);a.finish();});}
 finish(result,out);
 }
}
