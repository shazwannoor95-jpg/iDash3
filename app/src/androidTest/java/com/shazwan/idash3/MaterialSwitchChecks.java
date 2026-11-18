package com.shazwan.idash3;
import android.app.*;import android.content.*;import android.graphics.*;import android.os.*;import java.util.*;import java.io.*;
/** Verifies persisted revert, renderer difference/round-trip, settings picker and preview consistency. */
public class MaterialSwitchChecks extends Instrumentation {
 LauncherActivity a;Map<String,?> saved;int count;
 void check(boolean v,String why){if(!v)throw new AssertionError(why);count++;}
 void ui(Runnable r){Throwable[] error={null};runOnMainSync(()->{try{r.run();}catch(Throwable t){error[0]=t;}});waitForIdleSync();if(error[0]!=null)throw new AssertionError(error[0]);}
 Bitmap render(GlassSurface s){Bitmap b=Bitmap.createBitmap(180,100,Bitmap.Config.ARGB_8888);s.setBounds(0,0,180,100);s.draw(new Canvas(b));return b;}
 void screenshot(String name)throws Exception{Bitmap b=getUiAutomation().takeScreenshot();if(b!=null){try(OutputStream out=new FileOutputStream(new File(a.getFilesDir(),name))){b.compress(Bitmap.CompressFormat.PNG,100,out);}b.recycle();}}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle out=new Bundle();int code=Activity.RESULT_CANCELED;try{
  a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));java.util.concurrent.CountDownLatch ready=new java.util.concurrent.CountDownLatch(1);a.worker.execute(()->a.main.post(ready::countDown));check(ready.await(45,java.util.concurrent.TimeUnit.SECONDS),"catalog ready");waitForIdleSync();saved=new HashMap<>(a.prefs.getAll());GlassAppearance original=GlassAppearance.read(a);
  ui(()->{
   GlassSurface surface=new GlassSurface(1,0xff456789,22,0,false);Bitmap classic=render(surface);surface.material(GlassMaterial.IOS26);Bitmap modern=render(surface);check(!classic.sameAs(modern),"iOS26 material visibly differs");surface.material(GlassMaterial.CLASSIC);Bitmap restored=render(surface);check(classic.sameAs(restored),"same drawable reverts pixel-exactly");classic.recycle();modern.recycle();restored.recycle();
   GlassMaterial.save(a,GlassMaterial.IOS26);a.render();check(((GlassSurface)a.glass(0xff456789,22)).material()==GlassMaterial.IOS26,"Home factory uses selected material");check(((GlassSurface)IosStyle.controlGlass(a,22,false)).material()==GlassMaterial.IOS26,"Control Center factory uses selected material");
   new DashSettings(a).open("Home & Dock");a.settingsScreen.material();check(a.settingsScreen.dialog.getListView().getCheckedItemPosition()==1,"picker shows current choice");a.settingsScreen.dialog.getListView().performItemClick(null,0,0);check(GlassMaterial.read(a)==GlassMaterial.CLASSIC,"picker persists Classic");check(original.same(GlassAppearance.read(a)),"material change preserves Appearance settings");check(((GlassSurface)a.glass(0xff456789,22)).material()==GlassMaterial.CLASSIC,"factory returns Classic after revert");a.page="home";a.render();
  });screenshot("glass-classic.png");
  ui(()->{new DashSettings(a).open("Home & Dock");a.settingsScreen.material();a.settingsScreen.dialog.getListView().performItemClick(null,1,1);check(GlassMaterial.read(a)==GlassMaterial.IOS26,"picker persists iOS26 again");check(original.same(GlassAppearance.read(a)),"round trip keeps saved appearance");a.page="home";a.render();});screenshot("glass-ios26.png");
  ui(()->new DashSettings(a).open("Appearance"));long until=SystemClock.elapsedRealtime()+30000;while((a.appearanceEditor==null||!a.appearanceEditor.cacheReady)&&SystemClock.elapsedRealtime()<until)Thread.sleep(100);check(a.appearanceEditor!=null&&a.appearanceEditor.cacheReady,"preview cache ready");ui(()->{check(a.appearanceEditor.surface.material()==GlassMaterial.IOS26&&a.appearanceEditor.buttonSurface.material()==GlassMaterial.IOS26,"preview matches selected material");a.appearanceEditor.cancel();});
  out.putString("stream","PASS: "+count+" reversible material assertions\n");code=Activity.RESULT_OK;
 }catch(Throwable e){out.putString("stream","FAIL after "+count+": "+e+"\n");}finally{if(a!=null)ui(()->{if(a.appearanceEditor!=null)a.appearanceEditor.close();if(saved!=null)AppearanceChecks.restore(a.prefs,saved);a.finish();});}finish(code,out);}
}
