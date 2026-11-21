package com.shazwan.idash3;
import android.app.*;import android.content.*;import android.content.res.Configuration;import android.os.*;import android.provider.Settings;import android.widget.Toast;
/** A regular, resizable Activity for the left system split, separate from Android's Home task. */
public final class DriveSideActivity extends LauncherActivity {
 boolean launched,attempted;static long lastOpen;AlertDialog splitPrompt;
 final Runnable dockCheck=this::finishDock;
 final Runnable dockAttempt=()->{if(isFinishing())return;if(isInMultiWindowMode())showBitChord();else if(!DriveSideActions.dock())unavailable();else main.postDelayed(dockCheck,6000);};
 final Runnable visibilityTick=new Runnable(){public void run(){if(!foregroundVisible||isFinishing())return;updateClock();main.postDelayed(this,1000);}};
 static Intent target(Activity a){for(String pkg:new String[]{BitChordIntegration.RELEASE,BitChordIntegration.DEV}){Intent i=a.getPackageManager().getLaunchIntentForPackage(pkg);if(i!=null)return i;}return null;}
 static void open(LauncherActivity a){
  if(target(a)==null){Toast.makeText(a,"Pasang BitChord dahulu",Toast.LENGTH_LONG).show();return;}
  if(Build.VERSION.SDK_INT>30){Toast.makeText(a,"Mod percubaan ini untuk Android API26–30; guna split-screen sistem secara manual",Toast.LENGTH_LONG).show();return;}
  if(!DriveSideActions.ready()){new AlertDialog.Builder(a).setTitle("Benarkan tindakan split-screen?").setMessage("Untuk UI penuh BitChord di sebelah dashboard, hidupkan iDash split actions dalam Accessibility. Servis ini hanya menekan tindakan split-screen atas permintaan anda; tiada kandungan tetingkap atau acara skrin dibaca. Selepas hidupkan, kembali dan tekan ikon B sekali lagi.").setPositiveButton("Buka Accessibility",(d,w)->a.openSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS)).setNegativeButton("Batal",null).show();return;}
  long moment=SystemClock.uptimeMillis();if(moment-lastOpen<1500)return;lastOpen=moment;
  a.startActivity(new Intent(a,DriveSideActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
 }
 @Override public void onCreate(Bundle state){Bundle initial=state==null?new Bundle():new Bundle(state);initial.putString("page","drive");initial.putBoolean("panel",false);super.onCreate(initial);}
 @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus&&!attempted){attempted=true;main.postDelayed(dockAttempt,250);}}
 void finishDock(){if(isFinishing())return;if(isInMultiWindowMode())showBitChord();else unavailable();}
 void unavailable(){if(splitPrompt!=null||isFinishing())return;splitPrompt=new AlertDialog.Builder(this).setTitle("Split-screen belum aktif").setMessage("Firmware belum mendok dashboard. Cuba split-screen manual melalui Recent Apps, kemudian pilih BitChord di sebelahnya. Divider Android mengawal saiz; ini bukan app yang ditanam dalam View.").setPositiveButton("Recent Apps",(d,w)->{if(DriveSideActions.ready())DriveSideActions.recents();}).setNegativeButton("Kembali",(d,w)->finish()).create();splitPrompt.setOnDismissListener(d->splitPrompt=null);splitPrompt.show();}
 void showBitChord(){if(launched||isFinishing()||!isInMultiWindowMode())return;Intent intent=target(this);if(intent==null){Toast.makeText(this,"BitChord tidak lagi dipasang",Toast.LENGTH_LONG).show();finish();return;}if(splitPrompt!=null)splitPrompt.dismiss();main.removeCallbacks(dockCheck);launched=true;intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);try{getApplicationContext().startActivity(intent);}catch(ActivityNotFoundException|SecurityException failed){launched=false;Toast.makeText(this,"Firmware tidak dapat membuka BitChord bersebelahan",Toast.LENGTH_LONG).show();}}
 @Override public void onMultiWindowModeChanged(boolean split,Configuration c){super.onMultiWindowModeChanged(split,c);if(split&&!launched){main.removeCallbacks(dockCheck);main.postDelayed(dockCheck,2500);}}
 @Override protected void onPause(){super.onPause();if(isInMultiWindowMode()&&foregroundVisible){if(driveDashboard!=null)driveDashboard.media.resume();main.removeCallbacks(visibilityTick);main.post(visibilityTick);}}
 @Override protected void onResume(){super.onResume();main.removeCallbacks(visibilityTick);}
 @Override protected void onStop(){main.removeCallbacks(visibilityTick);super.onStop();}
 @Override protected void onDestroy(){main.removeCallbacks(dockAttempt);main.removeCallbacks(dockCheck);main.removeCallbacks(visibilityTick);if(splitPrompt!=null)splitPrompt.dismiss();super.onDestroy();}
 @Override public void onBackPressed(){finish();}
}
