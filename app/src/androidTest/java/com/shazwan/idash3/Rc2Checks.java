package com.shazwan.idash3;
import android.app.*;import android.content.*;import android.content.res.Configuration;import android.os.*;import android.view.*;import android.widget.*;import java.util.*;
/** Offline regressions for rc2. No claim of Maps search or YouTube network playback. */
public class Rc2Checks extends Instrumentation {
 LauncherActivity a;int n;String prefs;DashWebPlayer player;
 void check(boolean ok,String why){if(!ok)throw new AssertionError(why);n++;}
 void ui(Runnable r){Throwable[] error={null};runOnMainSync(()->{try{r.run();}catch(Throwable t){error[0]=t;}});waitForIdleSync();if(error[0]!=null)throw new AssertionError(error[0]);}
 View find(View v,String desc){if(desc.contentEquals(v.getContentDescription()==null?"":v.getContentDescription()))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View x=find(((ViewGroup)v).getChildAt(i),desc);if(x!=null)return x;}return null;}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle out=new Bundle();int result=Activity.RESULT_CANCELED;try{
  a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  java.util.concurrent.CountDownLatch ready=new java.util.concurrent.CountDownLatch(1);a.worker.execute(()->a.main.post(ready::countDown));check(ready.await(45,java.util.concurrent.TimeUnit.SECONDS),"catalog ready");waitForIdleSync();
  setInTouchMode(false);
  ui(()->{
   a.prefs.edit().remove(Readability.MODE).remove(Readability.CONTRAST).remove(Readability.OPAQUE).apply();
   GlassAppearance original=GlassAppearance.read(a);int material=GlassMaterial.read(a);
   check(Readability.apply(a,original)==original,"default appearance unchanged");
   check(Readability.tone(3,Configuration.UI_MODE_NIGHT_YES,0)==IosStyle.DARK,"auto night");check(Readability.tone(3,Configuration.UI_MODE_NIGHT_NO,0)==IosStyle.LIGHT,"auto day");check(Readability.tone(3,0,0)==0,"undefined falls back");
   a.prefs.edit().putInt(Readability.MODE,2).putBoolean(Readability.CONTRAST,true).apply();GlassAppearance e=GlassAppearance.active(a);
   check(e.tone==IosStyle.DARK&&e.transparency==10,"night plus contrast");check(GlassMaterial.read(a)==material&&GlassAppearance.read(a).same(original),"saved style and appearance untouched");
   a.prefs.edit().remove(Readability.MODE).remove(Readability.CONTRAST).remove(Readability.OPAQUE).apply();check(Readability.apply(a,GlassAppearance.read(a)).same(original),"override reversible");
   a.page="home";a.panelVisible=false;a.render();View root=a.stage;a.onNewIntent(new Intent(Intent.ACTION_MAIN));check(a.stage==root,"HOME avoids root allocation");
   a.page="apps";a.libraryPage=0;a.render();List<LauncherActivity.App> list=new ArrayList<>();for(int i=0;i<17;i++)list.add(new LauncherActivity.App("Long application name "+i,"test/.App"+i,null));
   AppLibraryGrid grid=(AppLibraryGrid)a.grid;boolean loaded=a.loaded;a.loaded=false;a.libraryPage=2;grid.setApps(Collections.emptyList());check(a.libraryPage==2,"cold discovery keeps restored page pending");a.loaded=loaded;grid.setApps(list);check(find(grid,"Open Long application name 16")!=null,"pending restored page applied after discovery");a.libraryPage=0;grid.setApps(list);find(grid,"Next app page").performClick();check(a.libraryPage==1,"page remembered");grid.setApps(list);View tile=find(grid,"Open Long application name 8");check(tile!=null&&tile.isFocusable(),"refresh preserves page and focusable tiles");
  });ui(()->{AppLibraryGrid grid=(AppLibraryGrid)a.grid;View tile=find(grid,"Open Long application name 8");check(tile.requestFocus(),"keyboard focus received after layout");tile.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_LEFT));check(a.libraryPage==0,"D-pad crosses page boundary");
  });ui(()->{check(find(a.grid,"Open Long application name 3").isFocused(),"focus retained after crossing page");
   a.query="";a.page="home";a.panelVisible=true;a.prefs.edit().putInt("panel_mode",0).apply();a.render();View stage=a.stage;LinearLayout panel=a.panelBox;a.rebuildPanel();check(stage==a.stage&&panel==a.panelBox,"panel change reuses root and panel container");
   player=a.webPanel;player.resume();check(player.ensureWeb()!=null,"WebView available");FrameLayout video=new FrameLayout(a);int[] callbacks={0};player.showFullscreen(video,()->callbacks[0]++);
   check(player.isFullscreen(),"fullscreen available");check(((FrameLayout.LayoutParams)video.getLayoutParams()).bottomMargin==0,"video has no reserved toolbar space");player.expandFullscreen();player.pause();check(callbacks[0]==1&&video.getParent()==null&&player.web==null,"expand/pause cleans surface once");
   Bundle state=new Bundle();a.libraryPage=2;a.homePage=1;a.onSaveInstanceState(state);check(state.getInt("library_page")==2&&state.getInt("home_page")==1,"pagination saved for process recreation");
  });out.putString("stream","PASS: "+n+" rc2 assertions\n");result=Activity.RESULT_OK;
 }catch(Throwable t){out.putString("stream","FAIL after "+n+": "+t+"\n");}finally{if(a!=null)ui(()->{a.prefs.edit().remove(Readability.MODE).remove(Readability.CONTRAST).remove(Readability.OPAQUE).apply();a.finish();});}finish(result,out);}
}
