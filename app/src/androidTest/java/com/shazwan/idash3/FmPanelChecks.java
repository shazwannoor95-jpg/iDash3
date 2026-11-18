package com.shazwan.idash3;
import android.app.*;import android.content.*;import android.os.*;import android.view.*;import android.widget.*;import java.util.*;
/** Offline FM preset persistence/layout. Does not certify tuner hardware or audio. */
public class FmPanelChecks extends Instrumentation {
 LauncherActivity a;DashFmPanel panel;int n;SharedPreferences prefs;Map<String,?> before;
 void check(boolean v,String why){if(!v)throw new AssertionError(why);n++;}
 void ui(Runnable r){Throwable[] error={null};runOnMainSync(()->{try{r.run();}catch(Throwable t){error[0]=t;}});waitForIdleSync();if(error[0]!=null)throw new AssertionError(error[0]);}
 View channel(View v,int slot){String desc=String.valueOf(v.getContentDescription());if(desc.startsWith("FM channel "+slot+";"))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View hit=channel(((ViewGroup)v).getChildAt(i),slot);if(hit!=null)return hit;}return null;}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle out=new Bundle();int result=Activity.RESULT_CANCELED;try{
  prefs=getTargetContext().getSharedPreferences(DashFmController.PREFS,0);before=new HashMap<>(prefs.getAll());prefs.edit().putInt("preset_0",90500).apply();
  a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));java.util.concurrent.CountDownLatch ready=new java.util.concurrent.CountDownLatch(1);a.worker.execute(()->a.main.post(ready::countDown));check(ready.await(45,java.util.concurrent.TimeUnit.SECONDS),"catalog ready");waitForIdleSync();
  ui(()->{panel=new DashFmPanel(a,1,()->{});panel.resume();a.body.removeAllViews();a.body.addView(panel,new LinearLayout.LayoutParams(-1,-1));
   for(int[] size:new int[][]{{560,330},{440,260}}){panel.measure(View.MeasureSpec.makeMeasureSpec(size[0],View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(size[1],View.MeasureSpec.EXACTLY));panel.layout(0,0,size[0],size[1]);
    check(panel.power.getParent()==panel.source.getParent(),"power and Auto share header");check(panel.power.getWidth()>=48&&panel.power.getHeight()>=48,"power hit area");check(panel.power.getCompoundDrawables()[1]!=null&&panel.power.getAlpha()==1f,"power icon full opacity");
    for(int i=1;i<=10;i++){View v=channel(panel,i);check(v!=null&&v.getVisibility()==View.VISIBLE&&v.getWidth()>=48&&v.getHeight()>=48,"visible channel target "+i);}
    View first=channel(panel,1),sixth=channel(panel,6);check(first.getParent()!=sixth.getParent(),"two preset rows");check(((View)first.getParent()).getParent()==((View)sixth.getParent()).getParent(),"same inline grid");
   }
   channel(panel,1).performClick();check(panel.controller.frequency==90500,"existing preset retained and recalled");panel.controller.frequency(101100);channel(panel,10).performLongClick();check(prefs.getInt("preset_9",0)==101100,"tenth preset saved");panel.controller.frequency(87500);channel(panel,10).performClick();check(panel.controller.frequency==101100,"tenth preset recalled");check(channel(panel,10).isSelected(),"current preset has selection marker");panel.close();
   DashFmPanel reopened=new DashFmPanel(a,1,()->{});reopened.resume();channel(reopened,10).performClick();check(reopened.controller.frequency==101100,"preset persists across panel recreation");reopened.close();
  });android.graphics.Bitmap shot=getUiAutomation().takeScreenshot();if(shot!=null){try(java.io.OutputStream file=new java.io.FileOutputStream(new java.io.File(a.getFilesDir(),"fm-panel-rc7.png"))){shot.compress(android.graphics.Bitmap.CompressFormat.PNG,100,file);}shot.recycle();}out.putString("stream","PASS: "+n+" FM panel assertions\n");result=Activity.RESULT_OK;
 }catch(Throwable t){out.putString("stream","FAIL after "+n+": "+t+"\n");}finally{if(a!=null)ui(()->{if(panel!=null)panel.close();a.finish();});if(prefs!=null&&before!=null)AppearanceChecks.restore(prefs,before);}finish(result,out);}
}
