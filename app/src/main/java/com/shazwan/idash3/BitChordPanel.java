package com.shazwan.idash3;
import android.content.*;import android.graphics.Color;import android.net.Uri;import android.view.*;import android.widget.*;
/** Callback-only native remote. Full BitChord runs in its own Activity, never a hidden WebView. */
final class BitChordPanel extends LinearLayout implements AutoCloseable {
 final LauncherActivity a;final ControlMedia media;final TextView open,description;boolean closed;
 BitChordPanel(LauncherActivity host){super(host);a=host;setOrientation(VERTICAL);setGravity(Gravity.CENTER_VERTICAL);setPadding(a.dp(12),a.dp(8),a.dp(12),a.dp(8));setContentDescription("BitChord native media panel");
  addView(label("BitChord",24));description=label("",12);description.setMaxLines(2);addView(description);
  media=new ControlMedia(a,null);media.bitChordOnly=true;media.title=label("Not Playing",21);media.artist=label("",14);media.status=label("",12);addView(media.title);addView(media.artist);addView(media.status);
  LinearLayout row=a.row();addView(row,new LayoutParams(-1,a.dp(56)));for(int action:new int[]{6,7,8}){LinearLayout hit=a.column();hit.setGravity(Gravity.CENTER);hit.setFocusable(true);hit.setBackground(a.glass(0xff25324d,12));ControlGlyph glyph=new ControlGlyph(a,action);hit.addView(glyph,new LayoutParams(a.dp(24),a.dp(24)));hit.addView(label(action==6?"Previous":action==7?"Play":"Next",11));hit.setOnClickListener(v->media.action(action));LayoutParams lp=new LayoutParams(0,-1,1);lp.setMargins(a.dp(2),0,a.dp(2),0);row.addView(hit,lp);if(action==6)media.previous=hit;else if(action==8)media.next=hit;else{media.play=hit;media.playGlyph=glyph;}}
  open=button("Buka BitChord",this::launch);addView(open,new LayoutParams(-1,a.dp(48)));addView(button("Akses kawalan media",media::explain),new LayoutParams(-1,a.dp(48)));refresh();
 }
 TextView label(String s,int size){TextView t=a.text(s,size,Color.WHITE);t.setMaxLines(1);t.setEllipsize(android.text.TextUtils.TruncateAt.END);return t;}
 TextView button(String s,Runnable r){TextView t=label(s,14);t.setGravity(Gravity.CENTER);t.setFocusable(true);t.setContentDescription(s);t.setBackground(a.glass(0xff25324d,12));t.setOnClickListener(v->r.run());return t;}
 Intent launchIntent(){for(String pkg:new String[]{BitChordIntegration.RELEASE,BitChordIntegration.DEV}){Intent i=a.getPackageManager().getLaunchIntentForPackage(pkg);if(i!=null)return i;}return null;}
 void refresh(){boolean installed=launchIntent()!=null;open.setText(installed?"Buka BitChord":"Muat turun BitChord");open.setContentDescription(open.getText());description.setText(installed?"Kawalan lagu di sini · app penuh dibuka berasingan":"Pasang APK rasmi ARM32 (armeabi-v7a) pada head unit ARM32");media.render();}
 void launch(){Intent i=launchIntent();if(i==null)i=new Intent(Intent.ACTION_VIEW,Uri.parse(BitChordIntegration.DOWNLOAD));try{a.startActivity(i);}catch(ActivityNotFoundException|SecurityException unavailable){Toast.makeText(a,"Tiada app untuk membuka BitChord / pautan muat turun",Toast.LENGTH_LONG).show();}}
 void resume(){if(closed)return;refresh();media.resume();}
 void pause(){media.pause();}
 public void close(){if(closed)return;closed=true;pause();}
}
