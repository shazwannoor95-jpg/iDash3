package com.shazwan.idash3;
import android.app.*;import android.content.*;import android.os.*;import android.graphics.Rect;import android.view.accessibility.*;
/** Real local playback plus containment and Android activity-dispatch regressions. */
public class EmbeddedPlayerChecks extends NativePlaybackChecks {
 boolean checked;
 @Override java.io.File wave()throws Exception{
  java.io.File f=new java.io.File(getTargetContext().getFilesDir(),"full-native-test.wav");int bytes=22050*2*300;
  java.nio.ByteBuffer h=java.nio.ByteBuffer.allocate(44).order(java.nio.ByteOrder.LITTLE_ENDIAN);
  h.put("RIFF".getBytes("US-ASCII")).putInt(bytes+36).put("WAVEfmt ".getBytes("US-ASCII")).putInt(16).putShort((short)1).putShort((short)1).putInt(22050).putInt(44100).putShort((short)2).putShort((short)16).put("data".getBytes("US-ASCII")).putInt(bytes);
  try(java.io.FileOutputStream out=new java.io.FileOutputStream(f)){out.write(h.array());byte[] silence=new byte[44100];for(int i=0;i<300;i++)out.write(silence);}return f;
 }
 @Override public void onStart(){
  android.accessibilityservice.AccessibilityServiceInfo info=getUiAutomation().getServiceInfo();
  info.flags|=android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
  getUiAutomation().setServiceInfo(info);super.onStart();
 }
 AccessibilityNodeInfo find(AccessibilityNodeInfo root){if(root==null)return null;if("iDash native now playing".contentEquals(root.getContentDescription()==null?"":root.getContentDescription()))return root;for(int i=0;i<root.getChildCount();i++){AccessibilityNodeInfo hit=find(root.getChild(i));if(hit!=null)return hit;}return null;}
 AccessibilityNodeInfo find(){AccessibilityNodeInfo node=find(getUiAutomation().getRootInActiveWindow());if(node!=null)return node;for(AccessibilityWindowInfo window:getUiAutomation().getWindows()){node=find(window.getRoot());if(node!=null)return node;}return null;}
 @Override void await(java.util.function.BooleanSupplier done,String why)throws Exception{
  super.await(done,why);if(checked)return;checked=true;
  ui(()->{check(a.driveDashboard.musicExpanded,"music expands by default");com.music.bitchord.BitChordEmbedded.openPlayer();});
  AccessibilityNodeInfo node=null;for(int i=0;i<240&&node==null;i++){node=find();if(node==null)SystemClock.sleep(250);}
  check(node!=null,"actual inline now-playing is rendered");Rect bounds=new Rect();node.getBoundsInScreen(bounds);
  android.graphics.Bitmap screenshot=getUiAutomation().takeScreenshot();if(screenshot!=null){try(java.io.OutputStream out=new java.io.FileOutputStream(new java.io.File(a.getFilesDir(),"idash-inline-player.png"))){screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}screenshot.recycle();}
  ui(()->{android.view.View content=a.driveDashboard.bitChord.content;int[] xy=new int[2];content.getLocationOnScreen(xy);check(bounds.left>=xy[0]&&bounds.top>=xy[1]&&bounds.right<=xy[0]+content.getWidth()+1&&bounds.bottom<=xy[1]+content.getHeight()+1,"player stays inside native content bounds");check(a.hasWindowFocus(),"player does not create a focused dialog window");});
  IntentFilter filter=new IntentFilter(Intent.ACTION_MAIN);ActivityMonitor external=addMonitor(filter,null,true);
  try{ui(()->a.startActivity(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName("com.music.bitchord","com.music.bitchord.MainActivity"))));check(external.getHits()==0,"standalone BitChord intent never reaches Android activity dispatch");}finally{removeMonitor(external);}
  ui(()->a.getOnBackPressedDispatcher().onBackPressed());
  for(int i=0;i<80&&find()!=null;i++)SystemClock.sleep(250);
  check(find()==null,"Back closes inline player");
  ui(()->{check(a.driveDashboard.mode==4&&a.driveDashboard.musicExpanded,"Back retains expanded music page");check(player.isPlaying(),"opening and closing inline player retains real audio");});
  IntentFilter urls=new IntentFilter(Intent.ACTION_VIEW);urls.addDataScheme("https");ActivityMonitor resolver=addMonitor(urls,null,true);
  try{ui(()->a.startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://music.youtube.com/browse/MPRE_TEST_IDASH"))));check(resolver.getHits()==0,"music URL stays in native navigation instead of app resolver");}finally{removeMonitor(resolver);}

 }
}
