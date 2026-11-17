package com.shazwan.idash3;
import android.app.*;import android.content.*;import android.os.*;import android.view.*;import android.webkit.*;
/** Offline surface/lifecycle checks; does not claim successful YouTube streaming. */
public class PanelFullscreenChecks extends Instrumentation {
 LauncherActivity a;DashWebPlayer player;int checks;
 void check(boolean v,String why){if(!v)throw new AssertionError(why);checks++;}
 void ui(Runnable work){Throwable[] error={null};runOnMainSync(()->{try{work.run();}catch(Throwable t){error[0]=t;}});waitForIdleSync();if(error[0]!=null)throw new AssertionError(error[0]);}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle out=new Bundle();int code=Activity.RESULT_CANCELED;try{
  a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  java.util.concurrent.CountDownLatch catalog=new java.util.concurrent.CountDownLatch(1);a.worker.execute(()->a.main.post(catalog::countDown));check(catalog.await(45,java.util.concurrent.TimeUnit.SECONDS),"initial catalog ready before mounting fixture");waitForIdleSync();
  ui(()->{player=new DashWebPlayer(a);a.body.removeAllViews();a.body.addView(player,new android.widget.LinearLayout.LayoutParams(-1,-1));player.resume();check(player.ensureWeb()!=null,"offline WebView available");});
  int[] hidden={0};WebChromeClient.CustomViewCallback callback=()->hidden[0]++;View[] video={null};int[] flags={0},systemUi={0};
  ui(()->{flags[0]=a.getWindow().getAttributes().flags;systemUi[0]=a.getWindow().getDecorView().getSystemUiVisibility();video[0]=new android.widget.FrameLayout(a);player.showFullscreen(video[0],callback);check(player.isFullscreen(),"custom video active");check(video[0].getParent().getParent()==player.body,"video belongs to panel body");});
  ui(()->{View overlay=(View)video[0].getParent();check(overlay.getWidth()==player.body.getWidth()&&overlay.getHeight()==player.body.getHeight(),"video overlay fills panel");player.expandFullscreen();check(video[0].getParent().getParent()==a.getWindow().getDecorView(),"explicit expansion uses Activity");check(player.exitFullscreen(),"exit handled");check(hidden[0]==1,"provider callback once");check(video[0].getParent()==null,"surface detached");check(a.getWindow().getAttributes().flags==flags[0],"window flags restored");check(a.getWindow().getDecorView().getSystemUiVisibility()==systemUi[0],"system UI restored");check(!player.exitFullscreen()&&hidden[0]==1,"exit idempotent");});
  ui(()->{player.showFullscreen(new android.widget.FrameLayout(a),callback);player.pause();check(!player.isFullscreen()&&player.web==null,"pause releases surface and WebView");check(hidden[0]==2,"pause notifies provider");});
  out.putString("stream","PASS: "+checks+" panel fullscreen assertions\n");code=Activity.RESULT_OK;
 }catch(Throwable t){out.putString("stream","FAIL after "+checks+": "+t+"\n");}finally{if(a!=null)ui(()->{if(player!=null)player.close();a.finish();});}finish(code,out);}
}
