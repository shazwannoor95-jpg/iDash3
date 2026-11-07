package com.shazwan.idash3;
import android.app.*;import android.content.*;import android.os.*;import android.view.*;import android.widget.*;import java.util.*;
/** Regresi: tetapan lama panel_mode=3 (SmartTube native, telah dibuang) mesti dipindahkan ke mod sah. Memulihkan preferences asal selepas ujian. Emulator pakai buang sahaja. */
public class PanelMigrationChecks extends Instrumentation {
 LauncherActivity a;SharedPreferences prefs;Map<String,?> before;int count;
 void check(boolean ok,String why){if(!ok)throw new AssertionError(why);count++;}
 void ui(Runnable r){Throwable[] e={null};runOnMainSync(()->{try{r.run();}catch(Throwable t){e[0]=t;}});waitForIdleSync();if(e[0]!=null)throw new AssertionError(e[0]);}
 boolean hasText(View v,String text){if(v instanceof TextView&&text.contentEquals(((TextView)v).getText()))return true;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(hasText(g.getChildAt(i),text))return true;}return false;}
 void restore(){if(before==null)return;SharedPreferences.Editor edit=prefs.edit().clear();for(Map.Entry<String,?> e:before.entrySet()){Object v=e.getValue();if(v instanceof Boolean)edit.putBoolean(e.getKey(),(Boolean)v);else if(v instanceof Integer)edit.putInt(e.getKey(),(Integer)v);else if(v instanceof Long)edit.putLong(e.getKey(),(Long)v);else if(v instanceof Float)edit.putFloat(e.getKey(),(Float)v);else if(v instanceof String)edit.putString(e.getKey(),(String)v);}edit.commit();}
 void launchWith(int mode,boolean v2)throws Exception{prefs.edit().putInt("panel_mode",mode).putBoolean("panel_modes_v2",v2).commit();a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));waitForIdleSync();}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle result=new Bundle();int code=Activity.RESULT_CANCELED;try{
  prefs=getTargetContext().getSharedPreferences("idash",0);before=new HashMap<>(prefs.getAll());
  // Kes utama: pengguna lama yang sudah melepasi migrasi v2 dan menyimpan mod 3.
  launchWith(3,true);
  check(prefs.getInt("panel_mode",-1)==0,"panel_mode=3 lama dipindahkan ke 0 (YouTube web) walaupun panel_modes_v2 sudah ditetapkan");
  ui(()->{new DashSettings(a).open("Panel");});
  ui(()->check(hasText(a.stage,"YouTube web"),"Settings memaparkan YouTube web, selaras dengan nilai tersimpan"));
  ui(()->check(!hasText(a.stage,"Kereta · Saga"),"Settings tidak lagi tersalah paparkan Kereta · Saga untuk mod 3"));
  ui(()->check(a.webPanel==null&&a.fmPanel==null&&a.carPanel==null,"migrasi tidak memulakan sebarang panel"));
  ui(()->a.finish());waitForIdleSync();
  // Mod luar julat lain (negatif / terlalu besar) juga mesti selamat.
  launchWith(99,true);check(prefs.getInt("panel_mode",-1)==0,"panel_mode=99 dipindahkan ke 0");ui(()->a.finish());waitForIdleSync();
  launchWith(-1,true);check(prefs.getInt("panel_mode",-1)==0,"panel_mode=-1 dipindahkan ke 0");ui(()->a.finish());waitForIdleSync();
  // Mod sah tidak boleh diubah.
  for(int m=0;m<=2;m++){final int mode=m;launchWith(mode,true);check(prefs.getInt("panel_mode",-1)==mode,"panel_mode="+mode+" sah dikekalkan");ui(()->a.finish());waitForIdleSync();}
  // Pemasangan pra-v2 dengan mod 3 juga selamat.
  launchWith(3,false);check(prefs.getInt("panel_mode",-1)==0&&prefs.getBoolean("panel_modes_v2",false),"panel_mode=3 pra-v2 dipindahkan dan v2 ditandakan");
  result.putString("stream","PASS: "+count+" semakan migrasi panel_mode\n");code=Activity.RESULT_OK;
 }catch(Throwable e){result.putString("stream","FAIL after "+count+": "+e+"\n");}
 finally{try{if(a!=null)runOnMainSync(()->a.finish());}catch(Throwable ignored){}restore();finish(code,result);}}
}
