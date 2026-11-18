package com.shazwan.idash3;
import android.app.*;import android.graphics.*;import android.os.*;import java.lang.reflect.*;import java.util.concurrent.*;
/** Saturates the actual queue, rejects excess work and confirms stale requests do not bake/cache. */
public class BakeQueueChecks extends Instrumentation {
 int checks;CountDownLatch release=new CountDownLatch(1);
 void check(boolean v,String why){if(!v)throw new AssertionError(why);checks++;}
 void ui(Runnable work){Throwable[] e={null};runOnMainSync(()->{try{work.run();}catch(Throwable t){e[0]=t;}});if(e[0]!=null)throw new AssertionError(e[0]);}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle out=new Bundle();int code=Activity.RESULT_CANCELED;try{
  Field field=GlassBake.class.getDeclaredField("worker");field.setAccessible(true);ThreadPoolExecutor worker=(ThreadPoolExecutor)field.get(null);CountDownLatch started=new CountDownLatch(1);worker.execute(()->{started.countDown();try{release.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}});check(started.await(15,TimeUnit.SECONDS),"worker paused for deterministic saturation");
  Bitmap input=Bitmap.createBitmap(8,8,Bitmap.Config.ARGB_8888);CountDownLatch complete=new CountDownLatch(GlassBake.MAX_QUEUED);int[] accepted={0},rejected={0},pixels={0};
  ui(()->{for(int i=0;i<GlassBake.MAX_QUEUED+12;i++){int key=100000+i;check(GlassBake.begin(key),"unique request");boolean queued=GlassBake.schedule(input,1,0,0,0,0,8,8,2,1,0,8,8,key,()->false,(bitmap,k)->{if(bitmap!=null)pixels[0]++;complete.countDown();});if(queued)accepted[0]++;else rejected[0]++;}check(GlassBake.queued()==GlassBake.MAX_QUEUED,"queue has fixed capacity");check(accepted[0]==GlassBake.MAX_QUEUED&&rejected[0]==12,"excess requests rejected without growing queue");check(GlassBake.pendingCount()==GlassBake.MAX_QUEUED,"rejected keys removed from pending");});
  release.countDown();check(complete.await(30,TimeUnit.SECONDS),"stale accepted jobs complete");ui(()->{check(pixels[0]==0,"stale jobs produce no bitmap");check(GlassBake.pendingCount()==0,"pending keys drained");for(int i=0;i<GlassBake.MAX_QUEUED+12;i++)check(GlassBake.cache.get(100000+i)==null,"stale result not cached");});input.recycle();out.putString("stream","PASS: "+checks+" bounded bake queue assertions\n");code=Activity.RESULT_OK;
 }catch(Throwable e){out.putString("stream","FAIL after "+checks+": "+e+"\n");}finally{release.countDown();}finish(code,out);}
}
