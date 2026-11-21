package com.shazwan.idash3;
import android.app.*;import android.content.*;import android.os.*;import android.graphics.*;
/** Shell harness enables split action service and installs an explicitly labelled external test fixture. */
public class DriveSideSmoke extends Instrumentation {
 DriveSideActivity a;int n;void check(boolean b,String s){if(!b)throw new AssertionError(s);n++;}
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle out=new Bundle();int code=Activity.RESULT_CANCELED;try{
 android.app.UiAutomation automation=getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
 for(String command:new String[]{"settings put secure enabled_accessibility_services ''","settings put secure enabled_accessibility_services com.shazwan.idash3/com.shazwan.idash3.DriveSideActions"}){try(java.io.InputStream in=new android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command))){byte[] b=new byte[256];while(in.read(b)!=-1){}}}
 for(int i=0;i<300&&!DriveSideActions.ready();i++)SystemClock.sleep(100);check(DriveSideActions.ready(),"split action service connected");ActivityMonitor mon=addMonitor(DriveSideActivity.class.getName(),null,false);getTargetContext().startActivity(new Intent(getTargetContext(),DriveSideActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));a=(DriveSideActivity)mon.waitForActivityWithTimeout(30000);removeMonitor(mon);check(a!=null,"side activity started");for(int i=0;i<200&&!a.isInMultiWindowMode();i++)SystemClock.sleep(100);for(int i=0;i<150&&!a.launched;i++)SystemClock.sleep(100);SystemClock.sleep(5000);
 runOnMainSync(()->{check(a.isInMultiWindowMode(),"actual Android multi-window enabled");check(a.launched,"external app launch requested only after docking");check(a.foregroundVisible,"left pane visible while other Activity focused");check(a.driveDashboard.map==null&&a.driveDashboard.bitChord==null&&a.driveDashboard.web==null,"left pane allocates no map/browser/player");check(a.driveDashboard.journey.getWidth()>150,"prayer widget has usable width");});
 long earlier=a.driveDashboard.journey.now;SystemClock.sleep(1500);runOnMainSync(()->check(a.driveDashboard.journey.now>earlier,"visible left pane clock continues while external app focused"));
 StringBuilder stacks=new StringBuilder();try(java.io.InputStream in=new android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand("am stack list"))){byte[] chunk=new byte[4096];int count;while((count=in.read(chunk))!=-1)stacks.append(new String(chunk,0,count,java.nio.charset.StandardCharsets.UTF_8));}
 boolean left=false,right=false;for(String line:stacks.toString().split("\\n")){if(line.contains("visible=true")&&line.contains("topActivity=ComponentInfo{com.shazwan.idash3/com.shazwan.idash3.DriveSideActivity}"))left=true;if(line.contains("visible=true")&&line.contains("topActivity=ComponentInfo{com.dev.bitchord/com.dev.bitchord.Main}"))right=true;}
 check(left&&right,"both dashboard and external app are top visible task windows: "+stacks);
 Bitmap b=automation.takeScreenshot();if(b!=null){try(java.io.OutputStream f=new java.io.FileOutputStream(new java.io.File(a.getFilesDir(),"side-fixture-native.png"))){b.compress(Bitmap.CompressFormat.PNG,100,f);}b.recycle();}out.putString("stream","PASS: "+n+" actual system split assertions; external app is a test fixture, not BitChord\n");code=Activity.RESULT_OK;
 }catch(Throwable t){out.putString("stream","FAIL after "+n+": "+t+"\n");}finally{if(a!=null)runOnMainSync(a::finish);}finish(code,out);}
}
